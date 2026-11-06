#!/usr/bin/env python3
"""Read-only validation of V4 JNI ABI and Java class packaging.

Checks the actual AArch64 branch to JNIEnv->NewStringUTF in getWindowRect.
Never executes or patches original game/native code.
"""
import argparse
import hashlib
import json
import re
import struct
import subprocess
import tempfile
import zipfile
from pathlib import Path

SHA256 = "752bcdc20d88fe535b4de156790e7468bf58c61b21dfd1eba5ee2fbda0facb38"
LIB = "lib/arm64-v8a/libysmteam.so"
JNI_PREFIX = "Java_com_ysmteam_imgui_GLES3JNIView_"
NATIVE_FUNCS = ("init", "resize", "step", "imgui_Shutdown", "onTouch", "getWindowRect")
NEWSTRINGUTF_RVA = 0xE9C98
# 0xf9429d08 = LDR X8,[X8,#0x538]. The next BLR X8 at
# 0xe9c9c calls JNIEnv->NewStringUTF, JNI table index 167.
NATIVE_CALL_BYTES = bytes.fromhex("089d42f900013fd6")


def file_offset(elf, vaddr):
    """Find ELF PT_LOAD file byte offset for an AArch64 virtual address."""
    if len(elf) < 0x40 or elf[:4] != b"\\x7fELF" or elf[4] != 2 or elf[5] != 1:
        raise ValueError("Not a little-endian ELF64 library")
    machine, = struct.unpack_from("<H", elf, 18)
    if machine != 183:
        raise ValueError("Not AArch64 ELF")
    phoff, = struct.unpack_from("<Q", elf, 32)
    phent, phnum = struct.unpack_from("<HH", elf, 54)
    if phent < 56 or phnum > 100 or phoff + phnum * phent > len(elf):
        raise ValueError("Invalid ELF program headers")
    for i in range(phnum):
        p_type, _, p_offset, p_vaddr, _, p_filesz, _, _ = struct.unpack_from(
            "<IIQQQQQQ", elf, phoff + phent * i)
        if p_type == 1 and p_vaddr <= vaddr < p_vaddr + p_filesz:
            offset = p_offset + vaddr - p_vaddr
            if offset >= len(elf):
                break
            return offset
    raise ValueError("RVA not mapped to ELF PT_LOAD")


def verify(apk_path):
    with zipfile.ZipFile(apk_path) as z:
        native = z.read(LIB)
        sha = hashlib.sha256(native).hexdigest()
        if sha != SHA256:
            raise ValueError("Wrong V4 JNI library SHA256: " + sha)
        dex = sorted(n for n in z.namelist() if re.fullmatch(r"classes(?:[2-9]|[1-9][0-9]+)?\\.dex", n))
        unexpected = [n for n in dex if b"Lcom/ysmteam/imgui/GLES3JNIView;" in z.read(n)]
        if unexpected:
            raise ValueError("Original V4 already defines JNI bridge; duplicate class in " + str(unexpected))
    offset = file_offset(native, NEWSTRINGUTF_RVA)
    if native[offset:offset + len(NATIVE_CALL_BYTES)] != NATIVE_CALL_BYTES:
        raise ValueError("getWindowRect JNI NewStringUTF call changed")
    with tempfile.TemporaryDirectory() as tmp:
        so = Path(tmp, "libysmteam.so")
        so.write_bytes(native)
        syms = subprocess.run(["readelf", "-Ws", str(so)], check=True, text=True,
                              capture_output=True).stdout
    exports = {}
    for name in NATIVE_FUNCS:
        symbol = JNI_PREFIX + name
        matches = re.findall(
            r"(?m)^\\s*\\d+:\\s*([0-9a-fA-F]+)\\s+\\d+\\s+FUNC\\s+GLOBAL\\s+DEFAULT\\s+\\d+\\s+" +
            re.escape(symbol) + r"\\s*$", syms)
        if len(matches) != 1:
            raise ValueError("Missing/ambiguous V4 JNI symbol " + symbol)
        exports[name] = "0x" + matches[0].lstrip("0")
    return {
        "native_sha256": sha,
        "jni_class": "com.ysmteam.imgui.GLES3JNIView",
        "exports": exports,
        "getWindowRect_jni_vtable_offset": "0x538",
        "getWindowRect_jni_return": "java.lang.String",
        "verified_original_dex_count": len(dex),
        "original_dex_defines_jni_class": False,
        "runtime_tested": False,
    }


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("apk")
    parser.add_argument("report")
    args = parser.parse_args()
    findings = verify(args.apk)
    path = Path(args.report)
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(findings, indent=2, ensure_ascii=False)+"\\n", encoding="utf-8")
    print("PASS: V4 JNI exports and NewStringUTF ABI verified. " +
          str(findings["verified_original_dex_count"]) + " original DEX files checked.")


if __name__ == "__main__":
    main()
