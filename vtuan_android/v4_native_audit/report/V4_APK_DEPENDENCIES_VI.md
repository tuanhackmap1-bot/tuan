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

## Danh sách thư viện có trong APK

- `lib/arm64-v8a/libAkDelayWG.so`
- `lib/arm64-v8a/libBqCCS.so`
- `lib/arm64-v8a/libCrashSight.so`
- `lib/arm64-v8a/libINTLUtils.so`
- `lib/arm64-v8a/libINTLVideo.so`
- `lib/arm64-v8a/libLocalSocket.so`
- `lib/arm64-v8a/libMSDKCore.so`
- `lib/arm64-v8a/libMSDKUnityAdapter.so`
- `lib/arm64-v8a/libMcDSP.so`
- `lib/arm64-v8a/libNGameNative.so`
- `lib/arm64-v8a/libPixVideoCore.so`
- `lib/arm64-v8a/libRapidXml.so`
- `lib/arm64-v8a/libResources.so`
- `lib/arm64-v8a/libRoosterNN.so`
- `lib/arm64-v8a/libTDataMaster.so`
- `lib/arm64-v8a/libTextureCompress.so`
- `lib/arm64-v8a/libWGRecorder.so`
- `lib/arm64-v8a/lib_burst_generated.so`
- `lib/arm64-v8a/libanort.so`
- `lib/arm64-v8a/libaovnet.so`
- `lib/arm64-v8a/libapm.so`
- `lib/arm64-v8a/libencrypt.so`
- `lib/arm64-v8a/libextractor.so`
- `lib/arm64-v8a/libgem.so`
- `lib/arm64-v8a/liblz4.so`
- `lib/arm64-v8a/libmain.so`
- `lib/arm64-v8a/libnettool.so`
- `lib/arm64-v8a/libpandora.so`
- `lib/arm64-v8a/libpixuiCurl.so`
- `lib/arm64-v8a/libqimei.so`
- `lib/arm64-v8a/libqmp.so`
- `lib/arm64-v8a/libtgpa.so`
- `lib/arm64-v8a/libtransceiver.so`
- `lib/arm64-v8a/libwukong.so`
- `lib/arm64-v8a/libxlua.so`
- `lib/arm64-v8a/libysmteam.so`

## Tài nguyên có tên liên quan đến runtime

- `assets/GCloudVoice/config.json`
- `assets/GCloudVoice/libgvoicensmodel.bin`
- `assets/bin/Data/Resources/unity_builtin_extra`
- `assets/bin/Data/unity default resources`
- `assets/bin/Data/unity_app_guid`
- `assets/unity_obb_guid`
- `lib/arm64-v8a/libMSDKUnityAdapter.so`
- `lib/arm64-v8a/libanort.so`
- `res/drawable/unity_static_splash.png`

## Quan hệ ELF DT_NEEDED

### `lib/arm64-v8a/libmain.so`
- Phụ thuộc trực tiếp: `libanort.so`, `libc.so`, `libdl.so`, `liblog.so`, `libm.so`, `libstdc++.so`

### `lib/arm64-v8a/libysmteam.so`
- Phụ thuộc trực tiếp: `libEGL.so`, `libGLESv3.so`, `libandroid.so`, `libc.so`, `libdl.so`, `liblog.so`, `libm.so`, `libz.so`

## Giới hạn

An absent .so in a single APK can exist in a split APK, dynamic download, or app-private storage. DT_NEEDED lists are static and do not cover dlopen, JNI reflection, or game behavior.

Báo cáo là phân tích tĩnh. Chưa chứng minh menu native được khởi tạo, khả năng tương thích với game hoặc hành vi AIM/ESP trong trận đấu.
