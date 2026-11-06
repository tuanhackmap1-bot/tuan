#!/usr/bin/env python3
"""Trace V4 ARM64 native feature call chains without executing any APK code.

No patching, hooking or claims about compatibility.
Requirements: pyelftools, capstone; SHA-256 fixed to the verified V4 library.
"""
from __future__ import annotations
import bisect
from collections import Counter, defaultdict
import hashlib
import io
import json
import sys
import zipfile
from pathlib import Path
from capstone import Cs, CS_ARCH_ARM64, CS_MODE_ARM
from capstone.arm64 import ARM64_OP_IMM
from elftools.elf.elffile import ELFFile
from elftools.dwarf.callframe import FDE

SHA="752bcdc20d88fe535b4de156790e7468bf58c61b21dfd1eba5ee2fbda0facb38"
LIB="lib/arm64-v8a/libysmteam.so"
TARGETS={
    "Aim configuration / ImGui UI": 0x0F7444,
    "Aim hero-choice UI": 0x10D0B4,
    "Skill reflection / callbacks": 0x11B65C,
    "Target state checks": 0x12DE68,
    "Target-choice logic candidate": 0x138A20,
    "Target-replacement candidate": 0x13934C,
    "Aim HP % reference A": 0x3BBE18,
    "Aim HP % reference B": 0x3BE654,
    "Florentino skill reference A": 0x13DFD8,
    "Florentino skill reference B": 0x13E2A8,
    "JNI bootstrap": 0x0F5088,
    "JNI rendering frame": 0x0E90E4,
    "JNI touch": 0x0E9D40,
    "DobbyHook exported function": 0x25648C,
    "Old auth-success candidate": 0x1C23C4,
    "Old auth-branch candidate": 0x1DD2B8,
}

def main(apk_path: str, dst: str):
    out=Path(dst)
    out.mkdir(parents=True, exist_ok=True)
    with zipfile.ZipFile(apk_path) as z:
        lib=z.read(LIB)
    actual=hashlib.sha256(lib).hexdigest()
    if actual!=SHA: raise SystemExit(f"Wrong V4 .so SHA-256: {actual}")
    elf=ELFFile(io.BytesIO(lib))
    dwarf=elf.get_dwarf_info()
    funcs=sorted(set((int(x["initial_location"]),int(x["initial_location"])+int(x["address_range"]))
                 for x in dwarf.EH_CFI_entries() if isinstance(x,FDE) and x["address_range"]))
    starts=[s for s,_ in funcs]
    ends={s:e for s,e in funcs}
    def parent(addr):
        ix=bisect.bisect_right(starts,addr)-1
        return starts[ix] if ix>=0 and addr<funcs[ix][1] else None
    def hexs(v): return f"0x{v:x}" if v is not None else "not resolved"
    # The .text and .plt sections are decoded as ARM64 instructions. Whole
    # sections are not executed, and instruction offsets are RVA in ELF.
    engine=Cs(CS_ARCH_ARM64, CS_MODE_ARM)
    engine.detail=True
    engine.skipdata=True
    calls=defaultdict(Counter)
    unowned_calls=defaultdict(Counter)
    names=defaultdict(set)
    dynsym=elf.get_section_by_name(".dynsym")
    if dynsym:
        for sym in dynsym.iter_symbols():
            if sym.name and int(sym["st_value"]):
                names[int(sym["st_value"])].add(sym.name)
    counts=Counter()
    for section in elf.iter_sections():
        if section.name not in (".text",".plt"): continue
        start=int(section["sh_offset"]); size=int(section["sh_size"])
        for op in engine.disasm(lib[start:start+size],int(section["sh_addr"])):
            if op.id==0: continue
            counts["insns"]+=1
            if op.mnemonic in ("bl","b") and op.operands and op.operands[0].type==ARM64_OP_IMM:
                counts[op.mnemonic]+=1
                if op.mnemonic=="bl":
                    target=int(op.operands[0].imm)
                    caller=parent(op.address)
                    callee=parent(target) or target
                    if caller is not None:
                        calls[caller][callee]+=1
                    else:
                        unowned_calls[op.address][callee]+=1
            if op.mnemonic=="blr":
                counts["indirect_blr"]+=1
    incoming=defaultdict(Counter)
    for src, cs in calls.items():
        for to, n in cs.items(): incoming[to][src]+=n
    # Locate function starts by EHFDE; don't mistake arbitrary alleged addresses for function starts
    targets={}
    for label, guess in TARGETS.items():
        start=parent(guess)
        targets[label]={"candidate_rva":hexs(guess),
            "enclosing_fde_start_rva":hexs(start),"start_matches_candidate":start==guess,
            "size_bytes": ends[start]-start if start is not None else None,
            "direct_callees": [],
            "direct_callers": []}
        if start is None: continue
        for to,n in calls[start].most_common(40):
            targets[label]["direct_callees"].append({"rva":hexs(to),"calls":n,
                "exported_names": sorted(names[to])[:4]})
        for fr,n in incoming[start].most_common(40):
            targets[label]["direct_callers"].append({"rva":hexs(fr),"calls":n,
                "exported_names":sorted(names[fr])[:4]})
    # Determine if the candidate callback functions are directly linked. A missing
    # direct BL edge does not rule out dynamic registration or indirect calls.
    target_starts={label:parent(rva) for label,rva in TARGETS.items()}
    matched_edges=[]
    for a, sa in target_starts.items():
        if sa is None: continue
        for b, sb in target_starts.items():
            if a!=b and sb is not None and calls[sa][sb]:
                matched_edges.append({"source":a,"destination":b,"count":calls[sa][sb]})
    result={
      "sha256":actual,
      "apk":Path(apk_path).name,
      "lib":LIB,
      "analysis":"ELF FDE boundaries and AArch64 direct BL graph",
      "addresses":"RVAs in libysmteam.so, not game/libil2cpp.so",
      "guarantee":"none: callsites do not demonstrate feature behavior",
      "stats":{"bytes":len(lib),"fde_function_ranges":len(funcs),
               "decoded_instructions":counts["insns"],
               "direct_calls":counts["bl"],
               "indirect_calls":counts["indirect_blr"]},
      "targets":targets,
      "edges_between_candidates":matched_edges,
    }
    (out/"v4_calls.json").write_text(json.dumps(result,ensure_ascii=False,indent=2),encoding="utf-8")
    md=[
      "# V4 — đường gọi mã native (xác minh tĩnh)",
      "",
      f"- SHA-256 thư viện khớp V4 gốc: \`{actual}\`.",
      f"- **{counts['insns']:,}** lệnh ARM64; **{counts['bl']:,}** lệnh gọi trực tiếp BL; **{counts['indirect_blr']:,}** lệnh gọi BLR gián tiếp.",
      f"- **{len(funcs):,}** khoảng hàm dựa trên bảng ELF unwind.",
      "",
      "**Chú ý:** Các offset dưới đây là RVA của \`libysmteam.so\`, không phải offset game. "
      "Gọi trực tiếp tới một RVA khi chưa biết kiểu dữ liệu tham số có thể làm ứng dụng crash.",
      "",
      "| Mục liên quan | Đầu hàm (theo FDE) | Hàm gọi trực tiếp | Hàm gọi đến nó |",
      "|---|---|---:|---:|",
    ]
    for label,item in targets.items():
        md.append(f"| {label} | \`{item['enclosing_fde_start_rva']}\` | "
                  f"{len(item['direct_callees'])} | {len(item['direct_callers'])} |")
    md.extend(["","## Quan hệ gọi giữa các ứng viên",""])
    if matched_edges:
        for e in matched_edges:
            md.append(f"- {e['source']} → {e['destination']} ({e['count']} lệnh BL)")
    else:
        md.append("Không phát hiện BL trực tiếp giữa các điểm được chọn. Có thể dùng callback/hook hoặc con trỏ hàm.")
    md.extend(["","## Đường gọi cụ thể",""])
    for label,item in targets.items():
        md.append(f"### {label} — \`{item['enclosing_fde_start_rva']}\`")
        md.append(f"- Hàm nằm trong vùng {item['size_bytes'] or 0:,} byte.")
        for field, heading in (("direct_callees","Gọi tới"),("direct_callers","Được gọi từ")):
            things=item[field][:14]
            desc=", ".join(f"\`{x['rva']}\`"+(f" ({'/'.join(x['exported_names'])})" if x['exported_names'] else "") for x in things) or "không tìm được gọi trực tiếp"
            md.append(f"- {heading}: {desc}")
        md.append("")
    md.extend(["## Tình trạng triển khai", "",
        "Đã có đồ thị lời gọi native để xác định các chuỗi chức năng và hành vi gọi mã. "
        "**Chưa có chữ ký ABI của các hàm nội bộ, chưa có offset libil2cpp.so được đối chiếu với game 1.64.1.7, "
        "và chưa chạy thử trong trận đấu.** Không thể khẳng định chuyển các offset này sang menu mới sẽ hoạt động."
    ])
    (out/"V4_CALL_GRAPH_VI.md").write_text("\n".join(md)+"\n",encoding="utf-8")
    print(f"PASS: {len(funcs)} unwind ranges, {counts['bl']} direct calls; wrote native call graph.")

if __name__=="__main__":
    if len(sys.argv)!=3: raise SystemExit("Usage: trace_v4_calls.py original.apk output-directory")
    main(sys.argv[1],sys.argv[2])
