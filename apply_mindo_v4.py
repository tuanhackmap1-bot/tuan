#!/usr/bin/env python3
"""Offline UI-only proof of concept for upstream DSMINHDUC source.
Never claims that patch/injection functionality works.
"""
from pathlib import Path
import difflib,re,sys
if len(sys.argv)!=2: sys.exit("Usage: python3 apply_mindo_v4.py /path/to/DSMINHDUC")
root=Path(sys.argv[1]).resolve()
appdir=root/"BolaMinhDuc"/"App"
if not appdir.is_dir(): sys.exit("ERROR: Missing BolaMinhDuc/App")
updates={}
def replace(path,old,new,count=1):
 p=root/path;s=p.read_text()
 if s.count(old)!=count: raise RuntimeError(f"Unexpected {path}: count {s.count(old)} != {count}")
 updates[p]=(s,s.replace(old,new))
def replace_regex(path,pat,new):
 p=root/path;s=p.read_text()
 s2,n=re.subn(pat,lambda _:new,s,flags=re.M|re.S)
 if n!=1:raise RuntimeError(f"Unexpected regex count in {path}: {n}")
 updates[p]=(s,s2)
p=appdir/"BolaMinhDucApp.swift";s=p.read_text()
if s.count("struct BolaMinhDucApp: App")!=1 or "LicenseGate.shared.start()" not in s:
 raise RuntimeError("Upstream app entry changed")
updates[p]=(s,'''import SwiftUI
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
''')
replace("BolaMinhDuc/App/ContentView.swift","model.bootstrap()","model.bootstrapOffline()",2)
replace_regex("BolaMinhDuc/App/PatchLibrary.swift",
 r'^    static func latest\(\) -> PatchFile\? \{\n.*?^    \}\n(?=    /// Checks the 64-byte personalisation trailer:)',
 '''    // Local file for UI experiments only, not validated for runtime.
    static func latest() -> PatchFile? {
        return bundled()
    }
''')
p=appdir/"AppModel.swift";s=p.read_text()
a="    func bootstrap() {\n"
if s.count(a)!=1:raise RuntimeError("Upstream AppModel bootstrap changed")
init='''    /// Offline UI initialization; no service or exploit at startup.
    func bootstrapOffline() {
        guard !didBootstrap else { return }
        didBootstrap = true
        refreshInstalled()
        if Installer.hasAccess(to: game) {
            kernelDone = true
            phase = .active
            statusText = "Có quyền truy cập dữ liệu game"
        } else {
            phase = .idle
            statusText = "Chế độ thử giao diện — chưa có quyền truy cập game"
        }
    }
'''
s2=s.replace(a,init+a)
b="    func ensureKernel(_ completion: @escaping (Bool) -> Void) {\n"
if s2.count(b)!=1:raise RuntimeError("Upstream ensureKernel changed")
guard='''    func ensureKernel(_ completion: @escaping (Bool) -> Void) {
        if Installer.hasAccess(to: game) {
            kernelDone = true
            completion(true)
            return
        }
        if #unavailable(iOS 17.0) {
            append("iOS 16: không chạy exploit không hỗ trợ")
            completion(false)
            return
        }
'''
s2=s2.replace(b,guard);updates[p]=(s,s2)
replace("BolaMinhDuc/App/HomeView.swift","        .safeAreaInset(edge: .bottom) { keyBar }\n","")
replace("project.yml","PRODUCT_BUNDLE_IDENTIFIER: com.apple.mobile.MobileHouseArrest",
 "PRODUCT_BUNDLE_IDENTIFIER: dev.mindo.offlineexperiment",2)
for p,(a,b) in updates.items():
 if a==b:raise RuntimeError("No changes to "+str(p))
for p,(a,b) in updates.items():p.write_text(b)
with (root/"MINDO_V4_SOURCE_DIFF.patch").open("w") as f:
 for p,(a,b) in updates.items():
  f.writelines(difflib.unified_diff(a.splitlines(True),b.splitlines(True),
  fromfile="a/"+p.relative_to(root).as_posix(),tofile="b/"+p.relative_to(root).as_posix()))
print("Applied experimental UI-only changes to",len(updates),"files")
