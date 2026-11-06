# V4: Kết quả đối chiếu luồng đăng nhập -> trạng thái menu

**Nguồn:** \`libysmteam.so\` đã xác thực SHA-256 \`752bcdc20d88fe535b4de156790e7468bf58c61b21dfd1eba5ee2fbda0facb38\`; trích xuất lệnh ARM64 tự động và kiểm tra từ \`report/V4_LOGIN_DISASM.md\`.

## Nhánh sau đăng nhập ở 0x1DD280

- \`0x1DD2B4 bl 0x19D1C4\`: gọi hàm xác thực native, truyền chuỗi ở \`x0\`.
- \`0x1DD2B8 cmp w0,#1\`, \`0x1DD2BC b.ne 0x1DD2E8\`: chỉ khi trả \`1\` mới vào nhánh được chấp nhận.
- **Nhưng** ngay sau đó: \`0x1DD2C0 ldrb w8,[x19,#0x20]\`, \`0x1DD2C4 cbz w8,0x1DD2F8\`. Nếu cờ trong đối tượng đầu vào bằng 0, nhánh được chấp nhận *bỏ qua* cập nhật dữ liệu và phát tín hiệu trạng thái.
- Nếu cờ khác 0: \`0x1DD2C8–0x1DD2D4\` chuẩn bị và gọi \`0xE05E8\` với chuỗi đích tại RVA \`0x8A8DF8\` và nguồn \`x20\`; sau đó \`0x1DD2D8–0x1DD2E0\` gọi \`0x15B3FC\` đối với địa chỉ \`0x8A8DE0\`.
- Nhánh trả về khác 1: \`0x1DD2E8–0x1DD2F4\` ghi 0 vào byte \`0x8A8DE0\` rồi gọi \`0x15B3FC\`.
- Sau cùng \`0x1DD2F8–0x1DD300\` đặt 0 vào \`0x8A8C14\` bằng \`STLRB\` (thao tác ghi có thứ tự bộ nhớ).

## Bên trong hàm xác thực (0x19D1C4)

- Đầu hàm có truy cập \`0x8A8C00\` và \`0x8A8C68\` bằng các phép load đồng bộ, sau đó một đường nhánh ở \`0x1C23C4\` gọi \`0xE0CB8\` với đối số đích \`0x8A8CD8\` rồi đặt \`w20=1\`. Việc giải nghĩa ngữ nghĩa của các trường này cần truy dấu toàn bộ đường dữ liệu.
- Hàm này có kích thước **157.572 byte** theo unwind FDE. Một bản vá chỉ trả \`w0=1\` tại đầu hàm sẽ bỏ qua hàng loạt nhánh nội bộ; không có cơ sở khẳng định trạng thái menu được khởi tạo đầy đủ.
- **Chưa chứng minh** trường nào là 'menu unlocked', cách ký hiệu thời hạn phiên hay hợp đồng ABI của hàm \`0xE05E8\`/\`0x15B3FC\`.

## Hướng kiểm chứng cần thiết trước khi tạo APK mới

1. Đối chiếu luồng thành công V4 gốc và VTUAN bằng log không lộ key, đặc biệt giá trị cờ \`[x19+0x20]\` và trạng thái nhận callback.
2. Xác định kiểu dữ liệu và vòng đời \`0x8A8DF8\`, \`0x8A8DE0\`, \`0x8A8CD8\`; tránh ghi bừa vào địa chỉ toàn cục.
3. Chỉ thử trên bản APK cập nhật đè, giữ nguyên dữ liệu game; kiểm chứng key đúng/sai/hết hạn, bàn phím và giao diện menu.
4. Kiểm tra ESP/AIM thực tế trong game, không suy luận từ tên hàm.

**Tình trạng:** Đây là kết quả phân tích mã tĩnh, **không phải bản sửa APK hoàn chỉnh**. Đường gọi menu Aim đã xác định trong \`report/V4_CALL_GRAPH_VI.md\`.
