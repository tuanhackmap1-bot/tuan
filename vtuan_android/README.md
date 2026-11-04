# VTUAN Android v1.6 — client xác minh key + menu nổi

Ứng dụng key VTUAN độc lập. Sau khi **xác minh key trực tuyến thành công**, có thể bật bảng trạng thái VTUAN hiển thị phía trên ứng dụng khác, kể cả màn hình trò chơi, nếu Android cho phép quyền "Hiển thị trên ứng dụng khác".

**Menu nổi không phải mã chạy bên trong Liên Quân**: không có ESP/AIM, không can thiệp vào game, không đọc màn hình hoặc dữ liệu game, không sử dụng libysmteam.so.

- Biểu tượng VTUAN có thể kéo thả, chạm để mở rộng bảng trạng thái, tắt từ menu hoặc thông báo.
- Xác minh key lại trực tuyến trước khi bật; không đưa key vào Intent hay overlay service.
- Quyền SYSTEM_ALERT_WINDOW do người dùng tự cấp; foreground service để duy trì thông báo.
- Menu nổi tự tắt sau 10 phút; không tự khởi động theo máy.
- Lỗi xác minh key, đổi key hoặc xóa key: tắt menu.
- Giữ Android Keystore và chẩn đoán GET /healthz của V1.5.
- Android 6.0+, compile 35, target 34. Debug signing có thể khác khóa giữa các runner.

## Source
GitHub Actions giải nén baseline V1.4, áp dụng nâng cấp V1.5 rồi patch V1.6 theo SHA-256. Bản mã nguồn V1.6 đầy đủ được xuất riêng trong file ZIP tại ChatGPT.

## Chưa xác minh
Chưa kiểm thử trên điện thoại thật và chưa có xác minh API trực tuyến với VPS. Quyền menu nổi có thể bị hệ điều hành hoặc trò chơi chặn. Không hứa hẹn ESP/AIM hay sửa dữ liệu game.
