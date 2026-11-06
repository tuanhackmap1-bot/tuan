# V4 — đường gọi mã native (xác minh tĩnh)

- SHA-256 thư viện khớp V4 gốc: \`752bcdc20d88fe535b4de156790e7468bf58c61b21dfd1eba5ee2fbda0facb38\`.
- **785,928** lệnh ARM64; **42,742** lệnh gọi trực tiếp BL; **3,839** lệnh gọi BLR gián tiếp.
- **8,522** khoảng hàm dựa trên bảng ELF unwind.

**Chú ý:** Các offset dưới đây là RVA của \`libysmteam.so\`, không phải offset game. Gọi trực tiếp tới một RVA khi chưa biết kiểu dữ liệu tham số có thể làm ứng dụng crash.

| Mục liên quan | Đầu hàm (theo FDE) | Hàm gọi trực tiếp | Hàm gọi đến nó |
|---|---|---:|---:|
| Aim configuration / ImGui UI | \`0xf7444\` | 40 | 1 |
| Aim hero-choice UI | \`0x10d0b4\` | 28 | 1 |
| Skill reflection / callbacks | \`0x11b65c\` | 5 | 1 |
| Target state checks | \`0x12de68\` | 40 | 1 |
| Target-choice logic candidate | \`0x138a20\` | 0 | 1 |
| Target-replacement candidate | \`0x13934c\` | 0 | 1 |
| Aim HP % reference A | \`0x3bbe18\` | 20 | 8 |
| Aim HP % reference B | \`0x3be654\` | 4 | 1 |
| Florentino skill reference A | \`0x13dfd8\` | 5 | 1 |
| Florentino skill reference B | \`0x13e2a8\` | 10 | 1 |
| JNI bootstrap | \`0xf5088\` | 10 | 0 |
| JNI rendering frame | \`0xe90e4\` | 40 | 0 |
| JNI touch | \`0xe9d40\` | 8 | 0 |
| DobbyHook exported function | \`0x25648c\` | 8 | 0 |
| Old auth-success candidate | \`0x19d1c4\` | 40 | 2 |
| Old auth-branch candidate | \`0x1dd280\` | 8 | 0 |

## Quan hệ gọi giữa các ứng viên

- Aim configuration / ImGui UI → Aim hero-choice UI (1 lệnh BL)
- Target state checks → Target-choice logic candidate (1 lệnh BL)
- Target state checks → Target-replacement candidate (1 lệnh BL)
- Target state checks → Florentino skill reference A (1 lệnh BL)
- Target state checks → Florentino skill reference B (1 lệnh BL)
- Aim HP % reference A → Aim HP % reference B (1 lệnh BL)
- Aim HP % reference B → Aim HP % reference A (2 lệnh BL)
- JNI rendering frame → Aim configuration / ImGui UI (1 lệnh BL)
- Old auth-branch candidate → Old auth-success candidate (1 lệnh BL)

## Đường gọi cụ thể

### Aim configuration / ImGui UI — \`0xf7444\`
- Hàm nằm trong vùng 88,560 byte.
- Gọi tới: \`0x256474\`, \`0x256480\`, \`0x21213c\`, \`0xf5d38\`, \`0x20ef5c\`, \`0x2124c0\`, \`0x200ee4\`, \`0x1fe474\`, \`0x1e0fc0\`, \`0x1df624\`, \`0x1e1fa8\`, \`0xf5f70\`, \`0x20ee00\`, \`0x1fb824\`
- Được gọi từ: \`0xe90e4\` (Java_com_ysmteam_imgui_GLES3JNIView_step)

### Aim hero-choice UI — \`0x10d0b4\`
- Hàm nằm trong vùng 1,540 byte.
- Gọi tới: \`0xf5d38\`, \`0x2119c0\`, \`0x211d04\`, \`0x1ff184\`, \`0xe66e8\`, \`0x1ff3b4\`, \`0x20f04c\`, \`0x20e9a0\`, \`0x20ee00\`, \`0x200ee4\`, \`0x20fe8c\`, \`0xcfc70\`, \`0xf6b70\`, \`0x116408\`
- Được gọi từ: \`0xf7444\`

### Skill reflection / callbacks — \`0x11b65c\`
- Hàm nằm trong vùng 14,684 byte.
- Gọi tới: \`0xd1670\`, \`0x255fc4\`, \`0x255cc4\`, \`0xcf200\`, \`0x256138\`
- Được gọi từ: \`0xead50\`

### Target state checks — \`0x12de68\`
- Hàm nằm trong vùng 26,840 byte.
- Gọi tới: \`0x125324\`, \`0xcfc70\`, \`0x116cc4\`, \`0x139de8\`, \`0x12dc20\`, \`0xd7280\`, \`0x1382b8\`, \`0xce3f0\`, \`0xd6f30\`, \`0x13e7ec\`, \`0x127060\`, \`0x13dd64\`, \`0x134740\`, \`0x139c54\`
- Được gọi từ: \`0x11efb8\`

### Target-choice logic candidate — \`0x138a20\`
- Hàm nằm trong vùng 2,348 byte.
- Gọi tới: không tìm được gọi trực tiếp
- Được gọi từ: \`0x12de68\`

### Target-replacement candidate — \`0x13934c\`
- Hàm nằm trong vùng 1,816 byte.
- Gọi tới: không tìm được gọi trực tiếp
- Được gọi từ: \`0x12de68\`

### Aim HP % reference A — \`0x3bbe18\`
- Hàm nằm trong vùng 7,188 byte.
- Gọi tới: \`0x3bbe18\`, \`0xcec10\`, \`0xd2810\`, \`0x3b7a94\`, \`0x3b9f28\`, \`0x3c1584\`, \`0x3be2e4\`, \`0x3bacdc\`, \`0x3bfb50\`, \`0x3bf524\`, \`0x3bdaf0\`, \`0x3b9d50\`, \`0x3be654\`, \`0x3bda2c\`
- Được gọi từ: \`0x3bbe18\`, \`0x3bfb50\`, \`0x3b7a94\`, \`0x3be654\`, \`0x3bf474\`, \`0x3b9d50\`, \`0x3baf6c\`, \`0x3c5718\`

### Aim HP % reference B — \`0x3be654\`
- Hàm nằm trong vùng 3,616 byte.
- Gọi tới: \`0xd2810\`, \`0x3bbe18\`, \`0xcec10\`, \`0xd86a0\`
- Được gọi từ: \`0x3bbe18\`

### Florentino skill reference A — \`0x13dfd8\`
- Hàm nằm trong vùng 720 byte.
- Gọi tới: \`0xce110\`, \`0xcfc70\`, \`0xce3f0\`, \`0xe06d0\`, \`0x3ca688\`
- Được gọi từ: \`0x12de68\`

### Florentino skill reference B — \`0x13e2a8\`
- Hàm nằm trong vùng 1,348 byte.
- Gọi tới: \`0xce110\`, \`0xcfc70\`, \`0xce3f0\`, \`0xd6f30\`, \`0xe10f4\`, \`0xd8010\`, \`0xe06d0\`, \`0xe0aa8\`, \`0xcf080\`, \`0x3ca688\`
- Được gọi từ: \`0x12de68\`

### JNI bootstrap — \`0xf5088\`
- Hàm nằm trong vùng 460 byte.
- Gọi tới: \`0xce3f0\`, \`0xcfc70\`, \`0x3ca688\`, \`0xce230\`, \`0xd3b60\`, \`0xd1cf0\`, \`0xd2720\`, \`0xcf8c0\`, \`0xd4cf0\`, \`0xcf080\`
- Được gọi từ: không tìm được gọi trực tiếp

### JNI rendering frame — \`0xe90e4\`
- Hàm nằm trong vùng 2,604 byte.
- Gọi tới: \`0x23a3bc\`, \`0x2005b0\`, \`0x1ed7a8\`, \`0x23a63c\`, \`0x155494\`, \`0x1e0fc0\`, \`0x1e1300\`, \`0x1fe39c\`, \`0x1f4944\`, \`0x1f4970\`, \`0x1fe420\`, \`0x20fed4\`, \`0x1ecd3c\`, \`0x1fe358\`
- Được gọi từ: không tìm được gọi trực tiếp

### JNI touch — \`0xe9d40\`
- Hàm nằm trong vùng 460 byte.
- Gọi tới: \`0x1dda18\`, \`0x1dffd4\`, \`0x1ed7a8\`, \`0x155248\`, \`0x1dddd4\`, \`0x1dd83c\`, \`0xdc2e8\`, \`0x1551b4\`
- Được gọi từ: không tìm được gọi trực tiếp

### DobbyHook exported function — \`0x25648c\`
- Hàm nằm trong vùng 344 byte.
- Gọi tới: \`0xd02f0\`, \`0x2565e4\`, \`0xce3f0\`, \`0x256614\`, \`0xd2f00\`, \`0x256688\`, \`0x256650\`, \`0x256a30\`
- Được gọi từ: không tìm được gọi trực tiếp

### Old auth-success candidate — \`0x19d1c4\`
- Hàm nằm trong vùng 157,572 byte.
- Gọi tới: \`0xcfc70\`, \`0xd81a0\`, \`0xce3f0\`, \`0x256474\`, \`0x256480\`, \`0xd6f30\`, \`0xe0aa8\`, \`0xe0bf0\`, \`0x18ea78\`, \`0x167778\`, \`0xd3d60\`, \`0x18b9c0\`, \`0xe05e8\`, \`0xd5810\`
- Được gọi từ: \`0x19d0d8\`, \`0x1dd280\`

### Old auth-branch candidate — \`0x1dd280\`
- Hàm nằm trong vùng 276 byte.
- Gọi tới: \`0xcfc70\`, \`0x15b3fc\`, \`0xd4cf0\`, \`0xd68f0\`, \`0xd2640\`, \`0x19d1c4\`, \`0xe05e8\`, \`0x3ca688\`
- Được gọi từ: không tìm được gọi trực tiếp

## Tình trạng triển khai

Đã có đồ thị lời gọi native để xác định các chuỗi chức năng và hành vi gọi mã. **Chưa có chữ ký ABI của các hàm nội bộ, chưa có offset libil2cpp.so được đối chiếu với game 1.64.1.7, và chưa chạy thử trong trận đấu.** Không thể khẳng định chuyển các offset này sang menu mới sẽ hoạt động.
