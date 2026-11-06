# VTUAN – Bản đồ hàm native V4 (đã xác minh)

Kết quả trích xuất **tĩnh** từ APK LQ VIP ADROI V4 1.64.1.7, bằng cách kiểm tra chính xác SHA-256 của \`lib/arm64-v8a/libysmteam.so\`.

- SHA-256 của thư viện: \`752bcdc20d88fe535b4de156790e7468bf58c61b21dfd1eba5ee2fbda0facb38\`.
- Thư viện: 9.055.952 byte, ELF AArch64.
- Đã giải mã 785.928 lệnh ARM64 và 8.522 khoảng hàm (FDE trong bảng unwind).
- Đã tìm 113 chuỗi ứng viên; bảng chi tiết đầy đủ ở \`report/V4_NATIVE_AUDIT.md\` và \`report/v4_native_report.json\`.

## Các điểm bắt đầu hàm C++ đã xác nhận theo FDE

| RVA bắt đầu hàm trong libysmteam.so | Bằng chứng tham chiếu trong mã |
|---|---|
| \`0x0F7444\` | Mã trong hàm tham chiếu \`aimSkills\`, \`KC Aim\`, \`##DistanceAim\`, các ô \`Skill 1/2/3\`. Nhiều khả năng là hàm dựng phần giao diện Aim, **chưa khẳng định thực hiện Aim**. |
| \`0x10D0B4\` | Tham chiếu \`Aim C1\`, \`FlorentinoTarget\`, \`BijanTargetPicker\`, \`BijanTarget\`. Ứng viên hàm màn hình chọn mục tiêu. |
| \`0x11B65C\` | Tham chiếu tên lớp và phương thức \`SkillControl\`, \`LSkillComponent\`, \`RequestUseSkillSlot\`, \`GetSkillAttackRange\`, \`SkillButtonUp\`. Ứng viên xử lý/tra cứu API kỹ năng. Trong cùng vùng có chuỗi \`Native aim callback unavailable; Auto skills remain independent\`. |
| \`0x12DE68\` | Tham chiếu \`target_unavailable\` và \`input_or_target_changed\`. Ứng viên theo dõi mục tiêu. |
| \`0x138A20\` | Tham chiếu \`manual_target\`, \`no_reachable_target\`, \`keep_target\`, \`commit_target\`. Ứng viên logic chọn mục tiêu. |
| \`0x13934C\` | Tham chiếu \`keep_target\`, \`replace_missing_target\`, \`no_visible_target\`, \`commit_current_target\`. Ứng viên logic chuyển mục tiêu. |
| \`0x13AF5C\` | Tham chiếu \`target_or_owner_changed\`, \`c1_cooldown_confirmed\`. |
| \`0x3BBE18\`, \`0x3BE654\` | Cả hai vùng mã đều tham chiếu chuỗi \`Aim HP %\`. |
| \`0x13DFD8\`, \`0x13E2A8\` | Có chuỗi tham chiếu tới tài nguyên kỹ năng Florentino. |

**Độ tin cậy:** địa chỉ bắt đầu hàm ở trên được xác định từ ELF unwind, và tham chiếu chuỗi từ lệnh ARM64 ADR/ADRP+ADD. **Tên chức năng là suy luận từ nhãn**, chưa được chứng minh bằng phân tích dữ liệu đầu vào/đầu ra và thử nghiệm trong game.

## Điểm JNI có tên xuất khẩu rõ ràng

| RVA | Symbol |
|---|---|
| \`0x0E8B54\` | \`Java_com_ysmteam_imgui_GLES3JNIView_init\` |
| \`0x0E9098\` | \`Java_com_ysmteam_imgui_GLES3JNIView_resize\` |
| \`0x0E90E4\` | \`Java_com_ysmteam_imgui_GLES3JNIView_step\` |
| \`0x0E9B10\` | \`Java_com_ysmteam_imgui_GLES3JNIView_imgui_Shutdown\` |
| \`0x0E9D40\` | \`Java_com_ysmteam_imgui_GLES3JNIView_onTouch\` |
| \`0x0F5088\` | \`JNI_OnLoad\` |
| \`0x25648C\` | \`DobbyHook\` |

## Giới hạn hiện tại

- Đây là **RVA trong \`libysmteam.so\`**, **không phải offset trong \`libil2cpp.so\` của game**.
- Chưa xác định được chính xác các vị trí hook ESP/Hack Map từ thư viện này. Các chuỗi tương ứng có thể bị ẩn, mã hóa hoặc được dựng động.
- Các hàm V4 phụ thuộc trạng thái nội bộ, các hàm phụ trợ và đối tượng IL2CPP. Không thể chỉ cắt một đoạn ARM64 rồi gắn vào menu khác là chạy được.
- Chưa build hay kiểm thử một APK mới có các chức năng V4.

**Nguồn kết quả:** máy phân tích CI chỉ đọc file APK không chạy nó; tự động từ chối nếu SHA-256 thư viện không khớp; kết quả gốc lưu ở \`report/\` để kiểm tra lại.
