"""Regression checks: never silently report zero native libraries for a valid V4 APK."""
import unittest

from inspect_apk_dependencies import collect_native_entries, count_dex_entries, V4_LIB


class ApkInventoryTests(unittest.TestCase):
    def test_expected_native_entries(self):
        names = {"AndroidManifest.xml", "lib/arm64-v8a/libysmteam.so",
                 "lib/arm64-v8a/libgcloud.so", "lib/armeabi-v7a/libunity.so",
                 "assets/libfake.so", "lib/libnotanabi.so/other", "classes.dex", "classes2.dex"}
        libs = collect_native_entries(names)
        self.assertEqual(len(libs), 3)
        self.assertIn(V4_LIB, libs)
        self.assertIn("lib/arm64-v8a/libgcloud.so", libs)

    def test_dex_entries(self):
        names = ["classes.dex", "classes2.dex", "classes10.dex",
                 "assets/classes.dex", "classes0.dex", "classes1.dex"]
        self.assertEqual(count_dex_entries(names), 3)

    def test_wrong_archive_layout(self):
        self.assertNotIn(V4_LIB, collect_native_entries(
            ["assets/libysmteam.so", "lib/arm64-v8a/not-library.txt"]))


if __name__ == "__main__":
    unittest.main()
