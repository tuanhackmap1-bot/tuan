#!/usr/bin/env python3
"""Build read-only diagnostic variant of the V4 offline UI experiment."""
import runpy,sys
from pathlib import Path
if len(sys.argv)!=2: sys.exit("Usage: python3 apply_mindo_v5.py /path/to/upstream")
root=Path(sys.argv[1]).resolve()
runpy.run_path(str(Path(__file__).with_name("apply_mindo_v4.py")),run_name="__main__")
p=root/"BolaMinhDuc/App/HomeView.swift"
s=p.read_text(encoding="utf-8")
def once(a,b):
 global s
 if s.count(a)!=1: raise RuntimeError(f"Unexpected count={s.count(a)} for {a!r}")
 s=s.replace(a,b)
once("    @State private var appear = false\n",
"""    @State private var appear = false
    @State private var diagnosis = "Nhấn KIỂM TRA để xem khả năng truy cập game."
""")
once("                    hero\n                    resetGuestCard\n",
"""                    hero
                    diagnosticsCard
""")
once("    // MARK: hero\n",
"""    // MARK: read-only compatibility check

    private var diagnosticsCard: some View {
        VStack(alignment: .leading, spacing: 12) {
            Text("KIỂM TRA TƯƠNG THÍCH")
                .font(.headline.weight(.bold))
                .foregroundColor(.white)
            Text(diagnosis)
                .font(.subheadline)
                .foregroundColor(.white)
                .multilineTextAlignment(.leading)
                .fixedSize(horizontal: false, vertical: true)
            Button {
                let ff = Installer.hasAccess(to: .freefireTH)
                let max = Installer.hasAccess(to: .freefireMAX)
                let dataSize = PatchLibrary.bundled()?.size ?? 0
                let supported = ExploitRunner.isSupported()
                diagnosis = "iOS: \\(UIDevice.current.systemVersion)\\n" +
                    "Quyền đọc Free Fire: \\(ff ? "CÓ" : "KHÔNG")\\n" +
                    "Quyền đọc Free Fire MAX: \\(max ? "CÓ" : "KHÔNG")\\n" +
                    "Patch gói sẵn: \\(dataSize) byte\\n" +
                    "Kernel method hỗ trợ: \\(supported ? "CÓ" : "KHÔNG")\\n" +
                    "INJECT: không bật trong bản chẩn đoán"
            } label: {
                HStack {
                    Image(systemName: "checkmark.shield")
                    Text("KIỂM TRA NGAY")
                }
                .font(.subheadline.weight(.bold))
                .frame(maxWidth: .infinity)
                .padding(.vertical, 12)
                .background(Color.white)
                .foregroundColor(.black)
                .clipShape(RoundedRectangle(cornerRadius: 10))
            }
            .buttonStyle(.plain)
        }
        .padding(16)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(Theme.card)
        .clipShape(RoundedRectangle(cornerRadius: 16))
    }

    // MARK: hero
""")
p.write_text(s,encoding="utf-8")
# Change the identifier and display name from the base UI preview.
proj=root/"project.yml"
txt=proj.read_text(encoding="utf-8")
if txt.count("dev.mindo.offlineuitest")!=2:raise RuntimeError("Unexpected bundle IDs")
if txt.count("MinDo V4 UI")!=2:raise RuntimeError("Unexpected app display names")
proj.write_text(txt.replace("dev.mindo.offlineuitest","dev.mindo.readonlydiagnostic").replace("MinDo V4 UI","MinDo V5 Check"),encoding="utf-8")
print("Created MinDo V5 read-only diagnostics")
