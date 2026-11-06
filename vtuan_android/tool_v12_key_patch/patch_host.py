#!/usr/bin/env python3
"""Inject a VTUAN key-entry launcher into an Android APK supplied by its owner.

Needs: Python 3, Java, apktool jar, zipalign, apksigner, keytool, and a
prebuilt standalone VTUAN gate APK. This is a compatibility patcher, not
strong tamper-proof DRM. Original developer signatures cannot be preserved.
"""
from __future__ import annotations

import argparse
import copy
import hashlib
import re
import shutil
import subprocess
import tempfile
import xml.etree.ElementTree as ET
import zipfile
from pathlib import Path

ANDROID = "{http://schemas.android.com/apk/res/android}"
GATE = "com.vtuan.gate.KeyActivity"
KEY_META = "vtuan.original_launcher"
INTERNET = "android.permission.INTERNET"
ET.register_namespace("android", "http://schemas.android.com/apk/res/android")


def command(args: list[str]) -> None:
    print("+", " ".join(map(str, args)), flush=True)
    subprocess.run([str(a) for a in args], check=True)


def normalize_activity(package: str, name: str) -> str:
    if not name:
        raise ValueError("Activity missing android:name")
    if name.startswith("."):
        return package + name
    if "." not in name:
        return package + "." + name
    return name


def is_launcher(element: ET.Element) -> bool:
    actions = {n.get(ANDROID + "name") for n in element.findall("action")}
    categories = {n.get(ANDROID + "name") for n in element.findall("category")}
    return ("android.intent.action.MAIN" in actions
            and "android.intent.category.LAUNCHER" in categories)


def patch_manifest(path: Path) -> tuple[str, str]:
    tree = ET.parse(path)
    root = tree.getroot()
    package = root.get("package")
    if not package:
        raise ValueError("Decoded manifest has no package name")
    app = root.find("application")
    if app is None:
        raise ValueError("Decoded manifest has no <application>")
    if any(normalize_activity(package, a.get(ANDROID + "name", "")) == GATE
           for a in list(app) if a.tag in ("activity", "activity-alias")):
        raise ValueError("Host already contains the injected VTUAN gate")
    if any(meta.get(ANDROID + "name") == KEY_META for meta in app.findall("meta-data")):
        raise ValueError("Host already contains VTUAN launcher metadata")

    launchers = []
    for activity in list(app):
        if activity.tag not in ("activity", "activity-alias"):
            continue
        for filt in activity.findall("intent-filter"):
            if is_launcher(filt):
                launchers.append((activity, filt))
    if len(launchers) != 1:
        raise ValueError(f"Expected one launcher entry, found {len(launchers)}")

    holder, launch_filter = launchers[0]
    if holder.tag == "activity-alias":
        original = normalize_activity(package, holder.get(ANDROID + "targetActivity", ""))
    else:
        original = normalize_activity(package, holder.get(ANDROID + "name", ""))

    gate = ET.Element("activity")
    gate.set(ANDROID + "name", GATE)
    gate.set(ANDROID + "exported", "true" if holder.tag == "activity" else "false")
    gate.set(ANDROID + "theme", "@android:style/Theme.Material.Light.NoActionBar")

    if holder.tag == "activity-alias":
        # Keep an existing alias for its icon/label/shortcuts, redirecting it
        # to the new key gate. The original implementation stays in-app.
        holder.set(ANDROID + "targetActivity", GATE)
        app.insert(list(app).index(holder), gate)
        original_activity = next(
            (candidate for candidate in app.findall("activity")
             if normalize_activity(package, candidate.get(ANDROID + "name", "")) == original),
            None)
        if original_activity is None:
            raise ValueError("Launcher alias references an undeclared activity")
        if not original_activity.findall("intent-filter"):
            original_activity.set(ANDROID + "exported", "false")
    else:
        holder.remove(launch_filter)
        gate.append(copy.deepcopy(launch_filter))
        app.append(gate)
        # A direct launcher with no other public intent filters should not
        # remain externally startable after the VTUAN gate takes over.
        # Existing deep links or public intents are left unchanged.
        if not holder.findall("intent-filter"):
            holder.set(ANDROID + "exported", "false")

    meta = ET.SubElement(app, "meta-data")
    meta.set(ANDROID + "name", KEY_META)
    meta.set(ANDROID + "value", original)
    # Java code in the new classesN.dex needs app loading enabled.
    app.set(ANDROID + "hasCode", "true")

    if not any(p.get(ANDROID + "name") == INTERNET
               for p in root.findall("uses-permission")):
        perm = ET.Element("uses-permission")
        perm.set(ANDROID + "name", INTERNET)
        root.insert(0, perm)

    tree.write(path, encoding="utf-8", xml_declaration=True)
    print(f"Host package: {package}; original launcher: {original}", flush=True)
    return package, original


def append_gate_dex(folder: Path, gate_apk: Path) -> int:
    existing = sorted(folder.glob("classes*.dex"))
    # apktool -s is critical: it retains the original bytecode as raw DEX.
    if not existing or not (folder / "classes.dex").exists():
        raise ValueError("Apktool failed to preserve host DEX (-s required)")
    indexes = [
        int(m.group(1)) if m.group(1) else 1
        for item in existing
        if (m := re.fullmatch(r"classes(\d*)\.dex", item.name))
    ]
    if not indexes:
        raise ValueError("No host classes.dex found")
    next_index = max(indexes) + 1
    with zipfile.ZipFile(gate_apk) as src:
        gate_dex = sorted((n for n in src.namelist()
                           if re.fullmatch(r"classes\d*\.dex", n)),
                          key=lambda n: (len(n), n))
        if not gate_dex:
            raise ValueError("VTUAN gate APK has no classes.dex")
        for name in gate_dex:
            destination = folder / f"classes{next_index}.dex"
            if destination.exists():
                raise ValueError(f"Refusing to overwrite {destination}")
            with src.open(name) as inp, destination.open("wb") as out:
                shutil.copyfileobj(inp, out)
            print(f"Inserted {name} -> {destination.name}", flush=True)
            next_index += 1
    return next_index - 1


def patch(args: argparse.Namespace) -> None:
    for f in (args.apk, args.gate, args.apktool):
        if not f.is_file():
            raise FileNotFoundError(f)
    for exe in (args.java, args.zipalign, args.apksigner, args.keytool):
        if not shutil.which(str(exe)) and not Path(exe).is_file():
            raise FileNotFoundError(f"Required executable unavailable: {exe}")

    with tempfile.TemporaryDirectory(prefix="vtuan_patch_") as tmp:
        scratch = Path(tmp)
        outdir = scratch / "decoded"
        unsigned = scratch / "unsigned.apk"
        aligned = scratch / "aligned.apk"

        # Never use the original APK as the output: preserve user's backup.
        command([args.java, "-jar", args.apktool, "d", "-f", "-s",
                 "-o", outdir, args.apk])
        patch_manifest(outdir / "AndroidManifest.xml")
        append_gate_dex(outdir, args.gate)
        command([args.java, "-jar", args.apktool, "b", outdir, "-o", unsigned])
        command([args.zipalign, "-f", "-p", "4", unsigned, aligned])

        keystore = args.keystore
        if not keystore.exists():
            keystore.parent.mkdir(parents=True, exist_ok=True)
            command([args.keytool, "-genkeypair", "-keystore", keystore,
                     "-alias", "vtuan", "-storepass", "android",
                     "-keypass", "android", "-keyalg", "RSA", "-keysize",
                     "2048", "-validity", "3650", "-dname", "CN=VTUAN Test Key"])
        args.output.parent.mkdir(parents=True, exist_ok=True)
        command([args.apksigner, "sign", "--ks", keystore,
                 "--ks-key-alias", "vtuan", "--ks-pass", "pass:android",
                 "--key-pass", "pass:android", "--out", args.output, aligned])
        command([args.apksigner, "verify", "--verbose", args.output])
        with zipfile.ZipFile(args.output) as z:
            invalid = z.testzip()
            if invalid is not None:
                raise ValueError(f"Bad APK zip entry: {invalid}")
            assert "AndroidManifest.xml" in z.namelist()
        digest = hashlib.sha256(args.output.read_bytes()).hexdigest()
        print(f"APK CREATED: {args.output} ({args.output.stat().st_size} bytes)")
        print(f"SHA256: {digest}")
        print("Warning: patched package is re-signed, original app signature is NOT preserved.")


def parse_args() -> argparse.Namespace:
    p = argparse.ArgumentParser(description=__doc__)
    p.add_argument("--apk", type=Path, required=True, help="Original tool APK")
    p.add_argument("--gate", type=Path, required=True, help="Built VTUAN gate APK")
    p.add_argument("--apktool", type=Path, required=True, help="apktool .jar path")
    p.add_argument("--output", type=Path, required=True, help="New signed APK path")
    p.add_argument("--keystore", type=Path, default=Path("vtuan-test.jks"))
    p.add_argument("--java", default="java")
    p.add_argument("--keytool", default="keytool")
    p.add_argument("--apksigner", default="apksigner")
    p.add_argument("--zipalign", default="zipalign")
    return p.parse_args()


if __name__ == "__main__":
    patch(parse_args())
