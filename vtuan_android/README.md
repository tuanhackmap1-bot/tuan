# VTUAN Android v1.5
Build riêng trên nhánh vtuan-android-build. APK là ứng dụng key VTUAN độc lập, **không phải APK game Liên Quân đã tích hợp menu**.
- Nguồn v1.4 được đóng gói tại source_v1_4/*.z với SHA-256 kiểm chứng.
- File tools/upgrade_v15.py.z chứa patch v1.5, có SHA-256 kiểm chứng trước khi áp dụng.
- Bản mã nguồn đã patch đầy đủ có trong VTUAN_ANDROID_V1_5_SOURCE.zip.
- V1.5 thêm GET /healthz kiểm tra máy chủ mà không gửi key; không nhầm tình trạng máy chủ với key hợp lệ.
- Android 6.0+, target 34, compile 35, JDK 17, Gradle 8.9.
- Debug APK có thể không cài đè được bản trước do khóa ký debug khác nhau giữa runner.
