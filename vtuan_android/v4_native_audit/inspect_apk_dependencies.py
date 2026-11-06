#!/usr/bin/env python3
"""Read-only native dependency and APK layout audit for the verified V4 package.

This is a packaging diagnosis, not an APK patch, gameplay verification, or an
implementation of native AIM/ESP behavior. Do not load or execute the APK.
"""
import hashlib
import io
import json
import re
import sys
import zipfile
from collections import Counter
from pathlib import Path

from elftools.elf.elffile import ELFFile

V4_LIB = "lib/arm64-v8a/libysmteam.so"
EXPECTED_SHA256 = "752bcdc20d88fe535b4de156790e7468bf58c61b21dfd1eba5ee2fbda0facb38"
INTERESTING = ("libysmteam.so", "libil2cpp.so", "libgcloud.so", "libunity.so",
               "libmain.so", "libc++_shared.so", "libdobby.so")

def needed_dependencies(data):
    """Return ELF DT_NEEDED names, or a descriptive static-analysis error."""
    try:
        elf = ELFFile(io.BytesIO(data))
        dynamic = elf.get_section_by_name(".dynamic")
        if dynamic is None:
            return [], "ELF has no .dynamic section"
        deps = [str(tag.needed) for tag in dynamic.iter_tags()
                if tag.entry.d_tag == "DT_NEEDED"]
        return sorted(set(deps)), None
    except Exception as exc:
        return [], "ELF parser: " + type(exc).__name__

def collect_native_entries(names):
    return sorted(n for n in names
                  if re.fullmatch(r"lib/[^/]+/[^/]+\.so", n))

def count_dex_entries(names):
    return sum(bool(re.fullmatch(r"classes(?:[2-9]|[1-9][0-9]+)?\.dex", n))
               for n in names)

def inspect(path):
    with zipfile.ZipFile(path) as apk:
        names = set(apk.namelist())
        if V4_LIB not in names:
            raise ValueError("Native V4 library is absent")
        raw = apk.read(V4_LIB)
        sha = hashlib.sha256(raw).hexdigest()
        if sha != EXPECTED_SHA256:
            raise ValueError("V4 library SHA-256 mismatch: " + sha)
        libs = collect_native_entries(names)
        if V4_LIB not in libs:
            raise ValueError("Native ZIP inventory inconsistent: missing verified V4 library")
        by_abi = {}
        for n in libs:
            abi, name = n.split("/")[1:]
            by_abi.setdefault(abi, []).append(name)
        # Probe the V4 menu .so and, if packaged, selected game/native runtime libs.
        dependency_info = {}
        for n in libs:
            if n.rsplit("/", 1)[-1] not in INTERESTING:
                continue
            entry = apk.getinfo(n)
            if entry.file_size > 250_000_000:
                dependency_info[n] = {"error": "Skipped: over 250 MB"}
                continue
            needed, error = needed_dependencies(apk.read(n))
            dependency_info[n] = {"needed": needed, "error": error,
                                  "bytes": entry.file_size}
        all_names = Counter(Path(n).name for n in libs)
        missing = [name for name in ("libgcloud.so", "libil2cpp.so")
                   if not any(Path(n).name == name for n in libs)]
        result = {
            "apk_name": Path(path).name,
            "apk_bytes": Path(path).stat().st_size,
            "v4_lib_sha256": sha,
            "v4_lib_bytes": len(raw),
            "dex_count": count_dex_entries(names),
            "native_so_count": len(libs),
            "abi_counts": {abi: len(entries) for abi, entries in sorted(by_abi.items())},
            "interesting_libraries_packaged": {x: all_names[x] for x in INTERESTING},
            "libraries_not_in_base_archive": missing,
            "dependency_info": dependency_info,
            "caveat": "An absent .so in a single APK can exist in a split APK, dynamic download, or app-private storage. "
                      "DT_NEEDED lists are static and do not cover dlopen, JNI reflection, or game behavior."
        }
        return result

def write_report(data, output):
    destination = Path(output)
    destination.mkdir(parents=True, exist_ok=True)
    (destination / "V4_APK_DEPENDENCIES.json").write_text(
        json.dumps(data, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    lines = [
        "# V4 — Kiểm tra cấu trúc APK và thư viện native",
        "",
        f"- Tệp: `{data['apk_name']}` ({data['apk_bytes']:,} bytes)",
        f"- libysmteam.so SHA-256: `{data['v4_lib_sha256']}` — khớp bản phân tích",
        f"- Số classes*.dex: **{data['dex_count']}**",
        f"- Số .so đóng gói: **{data['native_so_count']}**",
        "- ABI: " + (", ".join(f"`{abi}`: {n}" for abi, n in data["abi_counts"].items()) or "không có"),
        "",
        "## Các thư viện cần kiểm tra",
        "",
        "| Tên | Có trong APK nền? | Số bản sao |",
        "|---|---|---:|",
    ]
    for name, count in data["interesting_libraries_packaged"].items():
        lines.append(f"| `{name}` | {'Có' if count else '**Không thấy**'} | {count} |")
    lines += ["", "## Quan hệ ELF DT_NEEDED", ""]
    for path, info in data["dependency_info"].items():
        lines.append(f"### `{path}`")
        lines.append("- Phụ thuộc trực tiếp: " +
                     (", ".join(f"`{n}`" for n in info.get("needed", [])) or "không xác định"))
        if info.get("error"):
            lines.append("- Ghi chú: " + info["error"])
        lines.append("")
    lines += ["## Giới hạn", "", data["caveat"], "",
              "Báo cáo là phân tích tĩnh. Chưa chứng minh menu native được khởi tạo, "
              "khả năng tương thích với game hoặc hành vi AIM/ESP trong trận đấu.",
              ""]
    (destination / "V4_APK_DEPENDENCIES_VI.md").write_text(
        "\n".join(lines), encoding="utf-8")

if __name__ == "__main__":
    if len(sys.argv) != 3:
        raise SystemExit("Usage: inspect_apk_dependencies.py original-v4.apk report_dir")
    result = inspect(sys.argv[1])
    write_report(result, sys.argv[2])
    print("PASS: verified original native SHA and wrote APK/ELF dependency inventory")
