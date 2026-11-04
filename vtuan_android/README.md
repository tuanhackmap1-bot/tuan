# VTUAN Android v1.4 — APK key độc lập

V1.4 triển khai Android Keystore AES/GCM để lưu key, che thông tin khi nhập, thử lại khi mất mạng, và hiển thị rõ hơn trạng thái server.

## Source packaging
Để đảm bảo nội dung nguồn được chuyển nguyên vẹn tới CI, hai Java source V1.4 được lưu dưới dạng zlib có kiểm tra SHA256 trong `vtuan_android/source_v1_4/`. Workflow giải nén chúng vào `vtuan_android/app/src/main/java/com/vtuan/lqmenu/` trước khi build. Có thể xem mã nguồn thuần trong file `VTUAN_ANDROID_V1_4_SOURCE.zip` đi kèm hội thoại.

## Chú ý
Ứng dụng này **không phải APK Liên Quân chỉnh sửa** và không tích hợp ESP/AIM hoặc thư viện menu native vào game. Server V10 không cung cấp ràng buộc thiết bị (machine code) qua hai API hiện hữu. Chưa có kiểm thử thực tế trên điện thoại.

## APK
Artifacts tại GitHub Actions, nhánh `vtuan-android-build`. Android 6.0 trở lên. APK debug được ký theo runner, có thể cần gỡ bản debug cũ trước khi cài do khác chữ ký; hãy giữ lại key ở nơi an toàn trước khi gỡ.
