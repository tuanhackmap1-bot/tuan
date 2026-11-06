# V4 — Kiểm tra cấu trúc APK và thư viện native

- Tệp: `v4_candidate.apk` (226,470,689 bytes)
- libysmteam.so SHA-256: `752bcdc20d88fe535b4de156790e7468bf58c61b21dfd1eba5ee2fbda0facb38` — khớp bản phân tích
- Số classes*.dex: **6**
- Số .so đóng gói: **36**
- ABI: `arm64-v8a`: 36

## Các thư viện cần kiểm tra

| Tên | Có trong APK nền? | Số bản sao |
|---|---|---:|
| `libysmteam.so` | Có | 1 |
| `libil2cpp.so` | **Không thấy** | 0 |
| `libgcloud.so` | **Không thấy** | 0 |
| `libunity.so` | **Không thấy** | 0 |
| `libmain.so` | Có | 1 |
| `libc++_shared.so` | **Không thấy** | 0 |
| `libdobby.so` | **Không thấy** | 0 |

## Quan hệ ELF DT_NEEDED

### `lib/arm64-v8a/libmain.so`
- Phụ thuộc trực tiếp: `libanort.so`, `libc.so`, `libdl.so`, `liblog.so`, `libm.so`, `libstdc++.so`

### `lib/arm64-v8a/libysmteam.so`
- Phụ thuộc trực tiếp: `libEGL.so`, `libGLESv3.so`, `libandroid.so`, `libc.so`, `libdl.so`, `liblog.so`, `libm.so`, `libz.so`

## Giới hạn

An absent .so in a single APK can exist in a split APK, dynamic download, or app-private storage. DT_NEEDED lists are static and do not cover dlopen, JNI reflection, or game behavior.

Báo cáo là phân tích tĩnh. Chưa chứng minh menu native được khởi tạo, khả năng tương thích với game hoặc hành vi AIM/ESP trong trận đấu.
