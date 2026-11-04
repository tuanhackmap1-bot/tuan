# JNI V4 quick audit
Nguồn libysmteam.so (không bao gồm trong APK VTUAN v1.5); SHA-256 752bcdc20d88fe535b4de156790e7468bf58c61b21dfd1eba5ee2fbda0facb38.
ELF ARM64, Android API21, NDK r21e. Exports JNI_OnLoad and Java_com_ysmteam_imgui_GLES3JNIView_{init,resize,step,imgui_Shutdown,onTouch,getWindowRect}.
Phải có Java class GLES3JNIView, EGL lifecycle và ngữ cảnh render phù hợp. Chưa có hỗ trợ hoặc bằng chứng game cho phép tích hợp. App key không tự điều khiển game.
