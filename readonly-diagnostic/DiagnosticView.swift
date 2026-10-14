import SwiftUI
import UIKit

@main
struct MinDoReadOnlyCheckApp: App {
    var body: some Scene {
        WindowGroup { DiagnosticView() }
    }
}

struct DiagnosticView: View {
    @State private var report = "Chạm KIỂM TRA để tạo báo cáo trên iPad."
    @State private var completed = false

    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(alignment: .leading, spacing: 18) {
                    Text("MinDo CHECK")
                        .font(.largeTitle.bold())
                    Text("Kiểm tra chỉ đọc • Không cần key • Không can thiệp Free Fire")
                        .font(.subheadline)
                        .foregroundStyle(.secondary)

                    GroupBox("Kết quả") {
                        Text(report)
                            .frame(maxWidth: .infinity, alignment: .leading)
                            .font(.system(.body, design: .monospaced))
                            .textSelection(.enabled)
                            .padding(.vertical, 7)
                    }

                    Button(action: runCheck) {
                        Label("KIỂM TRA NGAY", systemImage: "checkmark.shield")
                            .font(.headline)
                            .frame(maxWidth: .infinity)
                            .padding()
                    }
                    .buttonStyle(.borderedProminent)

                    if completed {
                        ShareLink(item: report) {
                            Label("Chia sẻ kết quả", systemImage: "square.and.arrow.up")
                                .frame(maxWidth: .infinity)
                        }
                        .buttonStyle(.bordered)
                    }

                    Text("Giới hạn: Ứng dụng iOS thông thường không được quyền đọc dữ liệu riêng của game khác. Kết quả đọc thư mục gốc không chứng minh có quyền truy cập thư mục Free Fire. Không có INJECT trong ứng dụng này.")
                        .font(.footnote)
                        .foregroundStyle(.secondary)
                }
                .padding(20)
            }
            .navigationTitle("Chẩn đoán iPad")
        }
        .preferredColorScheme(.dark)
    }

    private func runCheck() {
        let v = ProcessInfo.processInfo.operatingSystemVersion
        let ios = "\(v.majorVersion).\(v.minorVersion).\(v.patchVersion)"
        let root = "/var/mobile/Containers/Data/Application"
        let readable = (try? FileManager.default.contentsOfDirectory(atPath: root)) != nil
        let gameScheme = URL(string: "freefire://")!
        let gameUrlVisible = UIApplication.shared.canOpenURL(gameScheme)
        let supportedByUpstream = v.majorVersion >= 17
        report = [
            "iOS: \(ios)",
            "Thiết bị: \(UIDevice.current.model)",
            "Free Fire URL scheme: \(gameUrlVisible ? "Có phản hồi" : "Không xác nhận")",
            "Có thể liệt kê thư mục app: \(readable ? "CÓ" : "KHÔNG")",
            "Nhánh exploit của mã nguồn gốc nhận iOS này: \(supportedByUpstream ? "Cần kiểm tra phiên bản chi tiết" : "KHÔNG")",
            "Quyền ghi vào game: CHƯA XÁC ĐỊNH",
            "Trạng thái INJECT: KHÔNG CÓ trong bản kiểm tra",
            "",
            "Không tải dữ liệu, không ghi file và không chạy exploit."
        ].joined(separator: "\n")
        completed = true
    }
}
