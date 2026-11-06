#!/usr/bin/env python3
"""Release blocker: never label V4 as finished while original key server remains.

Static checks are necessary, not sufficient. Passing static checks does not
prove native functionality, anti-cheat compatibility or real-device behavior.
"""
import argparse
import hashlib
import json
import sys
import zipfile
from pathlib import Path

LEGACY = b"https://keymanager.website/connect"
NATIVE = "lib/arm64-v8a/libysmteam.so"
VTUAN_ENDPOINT = b"vtuan-103-159-51-231.sslip.io"

def assess(path):
    problems=[]
    with zipfile.ZipFile(path) as apk:
        if NATIVE not in apk.namelist():
            problems.append("native V4 libysmteam.so absent")
            native = b""
        else:
            native = apk.read(NATIVE)
        if LEGACY in native:
            problems.append("BLOCKED: original third-party key server is still embedded in native auth")
        if not native:
            problems.append("BLOCKED: no native V4 functionality to assess")
        app_dex = [apk.read(n) for n in apk.namelist() if
                   n.startswith("classes") and n.endswith(".dex")]
        if not any(VTUAN_ENDPOINT in chunk for chunk in app_dex):
            problems.append("BLOCKED: VTUAN HTTPS client not found in executable dex")
    return {
        "apk_sha256":hashlib.sha256(Path(path).read_bytes()).hexdigest(),
        "old_key_server_present":LEGACY in native,
        "vtuan_client_present":any(VTUAN_ENDPOINT in chunk for chunk in app_dex),
        "native_runtime_verified":False,
        "aim_esp_verified":False,
        "release_ready":False,
        "blocking_issues":problems + [
            "BLOCKED: Android real-device verification and native feature checks not supplied"
        ],
    }

def main():
    p=argparse.ArgumentParser()
    p.add_argument("apk")
    p.add_argument("--json",dest="output",required=True)
    args=p.parse_args()
    report=assess(args.apk)
    Path(args.output).write_text(json.dumps(report,indent=2,ensure_ascii=False)+"\n")
    print("VTUAN native release:", "READY" if report["release_ready"] else "NOT READY")
    for issue in report["blocking_issues"]: print(issue)
    return 0 if report["release_ready"] else 2

if __name__=="__main__":
    sys.exit(main())
