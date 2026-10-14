#!/usr/bin/env python3
"""MinDo V4: experimental offline UI build, no assertion of working INJECT."""
from pathlib import Path
import difflib
import sys

if len(sys.argv) != 2:
    sys.exit("Usage: python3 apply_mindo_v4.py /path/to/DSMINHDUC")
root = Path(sys.argv[1]).resolve()
if not (root / "BolaMinhDuc/App").is_dir():
    sys.exit("Missing upstream BolaMinhDuc/App directory")
changes = {}

def exact(name, before, after, count=1):
    path = root / name
    original = changes[path][1] if path in changes else path.read_text(encoding="utf-8")
    actual = original.count(before)
    if actual != count:
        raise RuntimeError(f"{name}: expected {count} occurrences, got {actual}: {before!r}")
    changed = original.replace(before, after)
    changes[path] = (changes[path][0] if path in changes else original, changed)

entry = "BolaMinhDuc/App/BolaMinhDucApp.swift"
p = root / entry
original = p.read_text(encoding="utf-8")
if "LicenseGate.shared.start()" not in original or "@main" not in original:
    raise RuntimeError("Upstream entrypoint changed")
changes[p] = (original, """import SwiftUI

@main
struct BolaMinhDucApp: App {
    @StateObject private var model = AppModel()

    var body: some Scene {
        WindowGroup {
            ContentView()
                .environmentObject(model)
                .preferredColorScheme(.dark)
        }
    }
}
""")

# ContentView must not silently stop appearing when LicenseGate is unavailable.
exact("BolaMinhDuc/App/ContentView.swift",
      "guard LicenseGate.shared.revalidate() else { return }", "", 2)
exact("BolaMinhDuc/App/ContentView.swift",
      "model.bootstrap()", "model.bootstrapOffline()", 2)
exact("BolaMinhDuc/App/HomeView.swift",
      "        .safeAreaInset(edge: .bottom) { keyBar }\n", "", 1)

# UI boot skips license network checks and kernel exploits.
exact("BolaMinhDuc/App/AppModel.swift",
      "    func bootstrap() {\n",
      """    /// Experimental UI-only boot, no license or exploit requests.
    func bootstrapOffline() {
        guard !didBootstrap else { return }
        didBootstrap = true
        phase = .idle
        statusText = "Thử giao diện: INJECT chưa được kiểm chứng"
        refreshInstalled()
    }

    func bootstrap() {
""", 1)

# Do not modify a game from a UI-only preview.
exact("BolaMinhDuc/App/ConfigView.swift",
      "model.inject()",
      'model.showToast(model.tr("Bản thử giao diện — INJECT chưa khả dụng", "UI preview — INJECT unavailable"))', 1)
exact("BolaMinhDuc/App/ConfigView.swift", ': "INJECT")))', ': "DEMO")))', 1)

# Use distinct app identity, never impersonate Apple's MobileHouseArrest service.
exact("project.yml", "PRODUCT_BUNDLE_IDENTIFIER: com.apple.mobile.MobileHouseArrest",
      "PRODUCT_BUNDLE_IDENTIFIER: dev.mindo.offlineuitest", 2)
exact("project.yml", "CFBundleDisplayName: System Service",
      "CFBundleDisplayName: MinDo V4 UI", 2)

# Keep original only if every expected upstream edit matched.
for path, (old, new) in changes.items():
    if old == new: raise RuntimeError(f"No changes to {path}")
for path, (_, new) in changes.items():
    path.write_text(new, encoding="utf-8")
with (root / "MINDO_V4_SOURCE_DIFF.patch").open("w", encoding="utf-8") as out:
    for path, (old, new) in changes.items():
        out.writelines(difflib.unified_diff(
            old.splitlines(True), new.splitlines(True),
            fromfile="a/" + path.relative_to(root).as_posix(),
            tofile="b/" + path.relative_to(root).as_posix()))
print(f"Applied {len(changes)} UI-only source changes.")
