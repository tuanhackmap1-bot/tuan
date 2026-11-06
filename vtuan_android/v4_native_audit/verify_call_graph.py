#!/usr/bin/env python3
"""Read-only regression checks for the V4 native audit output.

This checks evidence consistency. It does NOT establish runtime behavior,
function signatures, key validity, or compatibility with a game APK.
"""
import argparse
import json
import sys
from pathlib import Path

EXPECTED_SHA = "752bcdc20d88fe535b4de156790e7468bf58c61b21dfd1eba5ee2fbda0facb38"
EDGES = [
    ("JNI rendering frame", "Aim configuration / ImGui UI"),
    ("Aim configuration / ImGui UI", "Aim hero-choice UI"),
    ("Old auth-branch candidate", "Old auth-success candidate"),
    ("Target state checks", "Target-choice logic candidate"),
    ("Target state checks", "Target-replacement candidate"),
]
KNOWN = {
    "JNI rendering frame": 0xE90E4,
    "Aim configuration / ImGui UI": 0xF7444,
    "Aim hero-choice UI": 0x10D0B4,
    "Old auth-branch candidate": 0x1DD280,
    "Old auth-success candidate": 0x19D1C4,
    "Target state checks": 0x12DE68,
    "Target-choice logic candidate": 0x138A20,
    "Target-replacement candidate": 0x13934C,
}

def run(report: dict) -> list[str]:
    errors = []
    if report.get("sha256") != EXPECTED_SHA:
        errors.append("Source library SHA-256 mismatch")
    if report.get("lib") != "lib/arm64-v8a/libysmteam.so":
        errors.append("Unexpected library path")
    stats = report.get("stats", {})
    if not isinstance(stats.get("direct_calls"), int) or stats["direct_calls"] < 40000:
        errors.append("Unexpected direct BL count")
    targets = report.get("targets", {})
    for label, addr in KNOWN.items():
        item = targets.get(label)
        if not isinstance(item, dict):
            errors.append("Missing target: " + label)
            continue
        if int(item.get("candidate_rva", "-1"), 0) != addr:
            errors.append("Wrong RVA: " + label)
        if not item.get("start_matches_candidate"):
            errors.append("FDE boundary mismatch: " + label)
    for caller_name, callee_name in EDGES:
        caller = targets.get(caller_name, {})
        callee = targets.get(callee_name, {})
        if not caller or not callee:
            continue
        src, dest = KNOWN[caller_name], KNOWN[callee_name]
        forward = any(int(x["rva"], 0) == dest and x.get("calls", 0) > 0
                      for x in caller.get("direct_callees", []))
        reverse = any(int(x["rva"], 0) == src and x.get("calls", 0) > 0
                      for x in callee.get("direct_callers", []))
        if not (forward and reverse):
            errors.append("Inconsistent edge: " + caller_name + " -> " + callee_name)
    return errors

def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("report", type=Path, help="report/v4_calls.json")
    args = parser.parse_args()
    try:
        data = json.loads(args.report.read_text(encoding="utf-8"))
        errors = run(data)
    except (OSError, ValueError, TypeError, KeyError) as exc:
        print(f"FAIL: invalid report: {exc}", file=sys.stderr)
        return 2
    if errors:
        for err in errors:
            print("FAIL:", err, file=sys.stderr)
        return 1
    print("PASS: known RVA identities, FDE boundaries and 5 bidirectional BL edges match.")
    print("LIMITATION: static graph only; no ABI, login-state or in-game function proof.")
    return 0

if __name__ == "__main__":
    raise SystemExit(main())
