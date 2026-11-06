#!/usr/bin/env python3
"""Static, non-executing audit of an APK's libysmteam.so.
Reports library-relative *candidates*, not proven ESP/AIM hook addresses.
Requires: pip install pyelftools capstone
"""
from __future__ import annotations
import bisect
import hashlib
import io
import json
import re
import sys
import zipfile
from collections import defaultdict
from pathlib import Path
from capstone import Cs, CS_ARCH_ARM64, CS_MODE_ARM, CS_OP_REG, CS_OP_IMM
from elftools.elf.elffile import ELFFile

EXPECTED_LIB_SHA256 = "752bcdc20d88fe535b4de156790e7468bf58c61b21dfd1eba5ee2fbda0facb38"
LIB_PATH = "lib/arm64-v8a/libysmteam.so"
FEATURE_PATTERN = re.compile(
    r"(?i)(?:\besp\b|\baim\b|aimbot|aimlock|aimskill|hack.?map|"
    r"enemy|target|hero|skill|health|cooldown|minimap|vision|"
    r"visible|radar|distance|wallhack|skeleton|box.?esp)"
)
STR_ASCII = re.compile(rb"[\x20-\x7e]{5,}")
STR_UTF16 = re.compile(rb"(?:[\x20-\x7e]\x00){5,}")

def main(apk_name: str, output_dir: str):
    output = Path(output_dir)
    output.mkdir(parents=True, exist_ok=True)
    with zipfile.ZipFile(apk_name) as apk:
        binary = apk.read(LIB_PATH)
    sha = hashlib.sha256(binary).hexdigest()
    if sha != EXPECTED_LIB_SHA256:
        raise SystemExit(f"REFUSED: native library SHA-256 mismatch: {sha}")
    elf = ELFFile(io.BytesIO(binary))
    if elf.get_machine_arch() != "AArch64":
        raise SystemExit("REFUSED: expected ARM64 AArch64.")
    segments = []
    for seg in elf.iter_segments():
        if seg["p_type"] == "PT_LOAD":
            segments.append({
                "start": int(seg["p_offset"]), "end": int(seg["p_offset"])+int(seg["p_filesz"]),
                "vaddr": int(seg["p_vaddr"]), "flags": int(seg["p_flags"]),
            })
    def file_to_rva(off: int):
        for seg in segments:
            if seg["start"] <= off < seg["end"]:
                return seg["vaddr"] + (off - seg["start"])
        return None

    labels = {}
    for kind, regex in (("ascii", STR_ASCII), ("utf16le", STR_UTF16)):
        for m in regex.finditer(binary):
            try:
                s = m.group().decode("ascii" if kind == "ascii" else "utf-16le")
            except UnicodeDecodeError:
                continue
            if 4 <= len(s) <= 110 and FEATURE_PATTERN.search(s):
                rva = file_to_rva(m.start())
                if rva is not None:
                    labels[rva] = {"text": s, "kind": kind, "rva": rva, "file_offset": m.start(),
                                   "bytes_len": len(m.group())}
    starts = sorted(labels)
    crossrefs = defaultdict(set)
    def track(target: int, caller: int):
        idx = bisect.bisect_right(starts, target) - 1
        if idx >= 0:
            hit = labels[starts[idx]]
            if starts[idx] <= target < starts[idx] + hit["bytes_len"]:
                crossrefs[starts[idx]].add(caller)

    disassembler = Cs(CS_ARCH_ARM64, CS_MODE_ARM)
    disassembler.detail = True
    disassembler.skipdata = True
    insn_count = 0
    executable_sections = [section for section in elf.iter_sections() if int(section["sh_flags"]) & 4]
    if not executable_sections:
        raise SystemExit("REFUSED: ELF has no executable sections")
    for section in executable_sections:
        start = int(section["sh_offset"])
        end = start + int(section["sh_size"])
        code = binary[start:end]
        pages = {}
        for insn in disassembler.disasm(code, int(section["sh_addr"])):
            if insn.id == 0:
                pages.clear()
                continue
            insn_count += 1
            ops = insn.operands
            if insn.mnemonic == "adrp" and len(ops) >= 2 and ops[0].type == CS_OP_REG and ops[1].type == CS_OP_IMM:
                pages[ops[0].reg] = (ops[1].imm, insn.address)
            elif insn.mnemonic == "adr" and len(ops) >= 2 and ops[1].type == CS_OP_IMM:
                track(ops[1].imm, insn.address)
            elif (insn.mnemonic == "add" and len(ops) >= 3 and
                  ops[0].type == CS_OP_REG and ops[1].type == CS_OP_REG and
                  ops[2].type == CS_OP_IMM and ops[1].reg in pages):
                page, page_ref = pages[ops[1].reg]
                if insn.address - page_ref <= 64:
                    track(page + ops[2].imm, page_ref)
                    track(page + ops[2].imm, insn.address)
                pages.pop(ops[0].reg, None)
            elif insn.mnemonic.startswith("b.") or insn.mnemonic in ("bl", "ret", "b"):
                pages.clear()
    exports = []
    dynsym = elf.get_section_by_name(".dynsym")
    if dynsym:
        for sym in dynsym.iter_symbols():
            if sym.name and sym["st_value"] and sym["st_info"]["type"] == "STT_FUNC":
                exports.append({"name": sym.name, "rva": int(sym["st_value"])})
    items = []
    for rva, item in sorted(labels.items()):
        item = dict(item)
        item["code_refs_rva"] = sorted(crossrefs[rva])[:60]
        items.append(item)
    report = {
        "apk_file": Path(apk_name).name,
        "native_library": LIB_PATH,
        "native_sha256": sha,
        "native_bytes": len(binary),
        "analysis_kind": "static string + ARM64 ADR/ADRP-ADD cross-references",
        "addresses_are": "RVA relative to libysmteam.so, NOT IL2CPP/game offsets",
        "not_verified": "Which code refs implement ESP/AIM/hack map, game-version compatibility, in-game behavior",
        "decoded_arm64_instructions": insn_count,
        "exported_native_functions": exports,
        "candidate_feature_strings": items,
    }
    (output / "v4_native_report.json").write_text(json.dumps(report, ensure_ascii=False, indent=2), encoding="utf8")
    lines = [
        "# V4 native feature candidate audit",
        "",
        f"Verified libysmteam.so SHA-256: \`{sha}\`",
        f"Native bytes: {len(binary):,}; ARM64 instructions decoded: {insn_count:,}",
        "",
        "**Important:** All hexadecimal addresses below are *library-relative RVAs*. "
        "They are not proven offsets to game functions or working hooks.",
        "",
        f"Candidate strings: {len(items)}; exports: {len(exports)}",
        "",
        "## Candidate menu/feature references",
        "",
        "| String RVA | Text (truncated) | ARM64 code-reference RVAs |",
        "|---|---|---|",
    ]
    for item in items[:250]:
        raw = item["text"].replace("|", "/").replace("\n"," ")
        refs = ", ".join("0x%x" % x for x in item["code_refs_rva"][:10]) or "not found"
        lines.append(f"| \`0x{item['rva']:x}\` | {raw[:72]} | {refs} |")
    if len(items) > 250:
        lines.append(f"\n_Only first 250 strings displayed; JSON has all {len(items)} entries._")
    lines += ["", "## Exported native functions", ""]
    for e in exports[:200]:
        lines.append(f"- \`0x{e['rva']:x}\` \`{e['name']}\`")
    lines += ["", "## What this proves", "",
              "This audit finds exact embedded labels and instructions referencing them.",
              "It does NOT establish which functions implement ESP/AIM, how the hooks work, "
              "or that offsets can be transplanted into another APK. Further data-flow analysis "
              "and testing on the specific game build are required."]
    (output / "V4_NATIVE_AUDIT.md").write_text("\n".join(lines) + "\n", encoding="utf8")
    print(f"PASS: validated native hash; {len(items)} candidate labels, {len(exports)} exports; wrote audit.")

if __name__ == "__main__":
    if len(sys.argv) != 3:
        raise SystemExit("Usage: python audit_v4_native.py /path/LQ-V4.apk /path/report-dir")
    main(sys.argv[1], sys.argv[2])
