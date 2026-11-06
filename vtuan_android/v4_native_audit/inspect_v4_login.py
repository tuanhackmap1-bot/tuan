#!/usr/bin/env python3
"""Read-only ARM64 instruction windows near V4 login and menu callback sites."""
import hashlib, io, sys, zipfile
from pathlib import Path
from capstone import Cs, CS_ARCH_ARM64, CS_MODE_ARM
from elftools.elf.elffile import ELFFile

EXPECTED="752bcdc20d88fe535b4de156790e7468bf58c61b21dfd1eba5ee2fbda0facb38"
WINDOWS=[
 ("V4 input/login click candidate",0x166404,0x100),
 ("V4 authentication function start",0x19d1c4,0x100),
 ("Post-auth state path candidate",0x1c23c4,0x130),
 ("V4 outer login branch",0x1dd280,0x130),
 ("JNI menu frame",0xe90e4,0x80),
 ("Aim UI start",0xf7444,0x80),
]
def main(src,out):
  with zipfile.ZipFile(src) as z:b=z.read("lib/arm64-v8a/libysmteam.so")
  if hashlib.sha256(b).hexdigest()!=EXPECTED: raise SystemExit("SHA-256 mismatch")
  e=ELFFile(io.BytesIO(b))
  sections=[x for x in e.iter_sections() if x.name==".text"]
  if len(sections)!=1: raise SystemExit("Unknown .text layout")
  x=sections[0];ra=int(x["sh_addr"]);offset=int(x["sh_offset"]);size=int(x["sh_size"])
  md=["# V4 ARM64 login and menu instructions","",f"Native SHA-256: {EXPECTED}","",
  "All listed locations are native-library RVAs, not offsets inside the game.",
  "These are instruction excerpts, NOT a working authentication patch.",""]
  c=Cs(CS_ARCH_ARM64,CS_MODE_ARM)
  for title,start,span in WINDOWS:
    if not (ra<=start<ra+size) or (start+span>ra+size):raise SystemExit(f"outside .text: {start:x}")
    raw=b[offset+(start-ra):offset+(start-ra)+span]
    md.extend([f"## {title}: 0x{start:x}","", "~~~asm"])
    for insn in c.disasm(raw,start):
      md.append(f"0x{insn.address:08x}  {insn.mnemonic:8s} {insn.op_str}")
    md.extend(["~~~",""])
  dest=Path(out);dest.mkdir(parents=True,exist_ok=True)
  (dest/"V4_LOGIN_DISASM.md").write_text("\n".join(md)+"\n",encoding="utf-8")
  print(f"PASS: {len(WINDOWS)} native code windows saved.")
if __name__=="__main__":
  if len(sys.argv)!=3:raise SystemExit("Usage: inspect_v4_login.py apk outdir")
  main(sys.argv[1],sys.argv[2])
