#!/usr/bin/env python3
"""Unit tests for APK launcher rewriting and additive DEX merging."""
from __future__ import annotations

import sys
import tempfile
import unittest
import xml.etree.ElementTree as ET
import zipfile
from pathlib import Path

sys.path.insert(0, str(Path(__file__).parent))
from patch_host import ANDROID, GATE, KEY_META, append_gate_dex, patch_manifest, preflight_archive, verify_unchanged_payload

P = "com.example.vtuantesthost"
START = '<manifest xmlns:android="http://schemas.android.com/apk/res/android" package="'+P+'">'
ENDING = '</manifest>'
LAUNCH = '''<intent-filter>
<action android:name="android.intent.action.MAIN"/>
<category android:name="android.intent.category.LAUNCHER"/>
</intent-filter>'''


class Tests(unittest.TestCase):
    def manifest(self, body: str):
        folder = tempfile.TemporaryDirectory()
        self.addCleanup(folder.cleanup)
        path = Path(folder.name) / "AndroidManifest.xml"
        path.write_text(START+body+ENDING, encoding="utf-8")
        return path

    def test_real_activity_launcher(self):
        p = self.manifest('<application><activity android:name=".Original" android:exported="true">'
                          +LAUNCH+'</activity></application>')
        self.assertEqual(patch_manifest(p), (P,P+".Original"))
        root = ET.parse(p).getroot()
        app = root.find("application")
        original = next(x for x in app.findall("activity") if x.get(ANDROID+"name")==".Original")
        gate = next(x for x in app.findall("activity") if x.get(ANDROID+"name")==GATE)
        self.assertFalse(original.findall("intent-filter"))
        self.assertEqual(original.get(ANDROID+"exported"),"false")
        self.assertEqual(gate.get(ANDROID+"exported"), "true")
        self.assertEqual(len(gate.findall("intent-filter")),1)
        meta = next(x for x in app.findall("meta-data") if x.get(ANDROID+"name")==KEY_META)
        self.assertEqual(meta.get(ANDROID+"value"), P+".Original")
        self.assertEqual(app.get(ANDROID+"hasCode"), "true")
        self.assertTrue(any(x.get(ANDROID+"name")=="android.permission.INTERNET"
                            for x in root.findall("uses-permission")))

    def test_activity_alias_launcher(self):
        p = self.manifest('<application><activity android:name=".Original" />'
                          '<activity-alias android:name=".IconAlias" '
                          'android:targetActivity=".Original" android:exported="true">'
                          +LAUNCH+'</activity-alias></application>')
        self.assertEqual(patch_manifest(p), (P,P+".Original"))
        app = ET.parse(p).getroot().find("application")
        alias = app.find("activity-alias")
        original = next(x for x in app.findall("activity") if x.get(ANDROID+"name")==".Original")
        gate = next(x for x in app.findall("activity") if x.get(ANDROID+"name")==GATE)
        self.assertEqual(original.get(ANDROID+"exported"),"false")
        self.assertEqual(alias.get(ANDROID+"targetActivity"),GATE)
        self.assertEqual(gate.get(ANDROID+"exported"),"false")
        self.assertLess(list(app).index(gate),list(app).index(alias))

    def test_preserves_public_deep_links(self):
        p=self.manifest('<application><activity android:name=".Original" android:exported="true">'
                        + LAUNCH +
                        '<intent-filter><action android:name="android.intent.action.VIEW" /></intent-filter>'
                        '</activity></application>')
        patch_manifest(p)
        app=ET.parse(p).getroot().find("application")
        original=next(x for x in app.findall("activity") if x.get(ANDROID+"name")==".Original")
        self.assertEqual(original.get(ANDROID+"exported"),"true")
        self.assertEqual(len(original.findall("intent-filter")),1)

    def test_refuse_undeclared_alias_target(self):
        p=self.manifest('<application><activity-alias android:name=".IconAlias" '
                        'android:targetActivity=".Missing" android:exported="true">'
                        +LAUNCH+'</activity-alias></application>')
        with self.assertRaisesRegex(ValueError,"undeclared activity"):
            patch_manifest(p)

    def test_refuse_ambiguous_launchers(self):
        p = self.manifest('<application><activity android:name=".One">'
                          +LAUNCH+'</activity><activity android:name=".Two">'
                          +LAUNCH+'</activity></application>')
        with self.assertRaisesRegex(ValueError,"Expected one launcher"):
            patch_manifest(p)

    def test_refuse_duplicate_patch(self):
        p = self.manifest('<application><activity android:name=".One">'
                          +LAUNCH+'</activity></application>')
        patch_manifest(p)
        with self.assertRaisesRegex(ValueError,"already contains"):
            patch_manifest(p)

    def test_native_and_assets_are_preserved(self):
        with tempfile.TemporaryDirectory() as tmp:
            original=Path(tmp)/"original.apk"
            patched=Path(tmp)/"patched.apk"
            with zipfile.ZipFile(original,"w") as src:
                src.writestr("lib/arm64-v8a/libexample.so",b"ELF original bytes")
                src.writestr("assets/data.bin",b"original asset")
                src.writestr("classes.dex",b"original dex")
            with zipfile.ZipFile(patched,"w") as dst:
                dst.writestr("lib/arm64-v8a/libexample.so",b"ELF original bytes")
                dst.writestr("assets/data.bin",b"original asset")
                dst.writestr("classes.dex",b"changed dex allowed")
            self.assertEqual(verify_unchanged_payload(original,patched),3)

    def test_native_library_disappearance_is_a_fatal_error(self):
        with tempfile.TemporaryDirectory() as tmp:
            original=Path(tmp)/"original.apk"
            patched=Path(tmp)/"patched.apk"
            with zipfile.ZipFile(original,"w") as src:
                src.writestr("lib/arm64-v8a/libgcloud.so",b"native lib")
            with zipfile.ZipFile(patched,"w") as dst:
                dst.writestr("classes.dex",b"host bytecode")
            with self.assertRaisesRegex(ValueError,"libgcloud\\.so"):
                verify_unchanged_payload(original,patched)

    def test_native_library_corruption_is_a_fatal_error(self):
        with tempfile.TemporaryDirectory() as tmp:
            original=Path(tmp)/"original.apk"
            patched=Path(tmp)/"patched.apk"
            with zipfile.ZipFile(original,"w") as src:
                src.writestr("lib/arm64-v8a/libgcloud.so",b"valid native lib")
            with zipfile.ZipFile(patched,"w") as dst:
                dst.writestr("lib/arm64-v8a/libgcloud.so",b"corrupted lib")
            with self.assertRaisesRegex(ValueError,"modified.*libgcloud\\.so"):
                verify_unchanged_payload(original,patched)

    def test_original_dex_disappearance_is_fatal(self):
        with tempfile.TemporaryDirectory() as tmp:
            original=Path(tmp)/"original.apk"
            patched=Path(tmp)/"patched.apk"
            with zipfile.ZipFile(original,"w") as src:
                src.writestr("classes.dex",b"original one")
                src.writestr("classes10.dex",b"original tenth")
            with zipfile.ZipFile(patched,"w") as dst:
                dst.writestr("classes.dex",b"original one")
            with self.assertRaisesRegex(ValueError,"classes10.dex"):
                verify_unchanged_payload(original,patched)

    def test_original_dex_modification_is_fatal(self):
        with tempfile.TemporaryDirectory() as tmp:
            original=Path(tmp)/"original.apk"
            patched=Path(tmp)/"patched.apk"
            with zipfile.ZipFile(original,"w") as src:
                src.writestr("classes.dex",b"host code")
            with zipfile.ZipFile(patched,"w") as dst:
                dst.writestr("classes.dex",b"changed host code")
            with self.assertRaisesRegex(ValueError,"classes.dex"):
                verify_unchanged_payload(original,patched)

    def test_preflight_rejects_overwriting_original(self):
        with tempfile.TemporaryDirectory() as tmp:
            original=Path(tmp)/"original.apk"
            gate=Path(tmp)/"gate.apk"
            for apk in (original,gate):
                with zipfile.ZipFile(apk,"w") as z:
                    z.writestr("AndroidManifest.xml",b"manifest")
                    z.writestr("classes.dex",b"code")
            with self.assertRaisesRegex(ValueError,"different files"):
                preflight_archive(original,gate,original)
            with self.assertRaisesRegex(ValueError,"different files"):
                preflight_archive(original,gate,gate)
            preflight_archive(original,gate,Path(tmp)/"new.apk")

    def test_preflight_rejects_incomplete_apk(self):
        with tempfile.TemporaryDirectory() as tmp:
            original=Path(tmp)/"bad.apk"
            gate=Path(tmp)/"gate.apk"
            with zipfile.ZipFile(original,"w") as z:
                z.writestr("AndroidManifest.xml",b"manifest")
            with zipfile.ZipFile(gate,"w") as z:
                z.writestr("AndroidManifest.xml",b"manifest")
                z.writestr("classes.dex",b"code")
            with self.assertRaisesRegex(ValueError,"not a complete APK"):
                preflight_archive(original,gate,Path(tmp)/"out.apk")

    def test_add_dex_preserves_original(self):
        with tempfile.TemporaryDirectory() as tmp:
            folder = Path(tmp)
            (folder/"classes.dex").write_bytes(b"host-original")
            (folder/"classes2.dex").write_bytes(b"host-second")
            gate = folder/"gate.apk"
            with zipfile.ZipFile(gate,"w") as z:
                z.writestr("classes.dex",b"vtuan-classes")
                z.writestr("classes2.dex",b"vtuan-extra")
            self.assertEqual(append_gate_dex(folder,gate),4)
            self.assertEqual((folder/"classes.dex").read_bytes(),b"host-original")
            self.assertEqual((folder/"classes2.dex").read_bytes(),b"host-second")
            self.assertEqual((folder/"classes3.dex").read_bytes(),b"vtuan-classes")
            self.assertEqual((folder/"classes4.dex").read_bytes(),b"vtuan-extra")


if __name__=="__main__":
    unittest.main(verbosity=2)
