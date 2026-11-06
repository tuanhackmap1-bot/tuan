VTUAN V12.1 - WINDOWS PATCH KIT

1. Extract all ZIP contents into one folder.
2. Put your original TTS_TOOL_V12.1_BYPASS_NO_KEY.apk in that same folder.
3. Install Python 3.12 and Java JDK 17 on Windows, if not installed:
   winget install Python.Python.3.12
   winget install EclipseAdoptium.Temurin.17.JDK
   Close and reopen PowerShell after installation.
4. Double-click RUN_PATCH_WINDOWS.bat.
5. The script checks verified dependency checksums and fetches Android build-tools
   from Google when needed, rebuilds the APK and verifies the new signature.
6. Output: TTS_TOOL_V12.1_VTUAN_KEY_PATCHED.apk

The original APK is never uploaded or overwritten. It stays on your PC.
Back up original application data before installing the modified APK.
Re-signing changes the signing certificate. You may need to uninstall the old
app, which can erase application data, before installing the patched APK.

This is a launcher-level key screen, not native AIM/ESP or a guarantee
the original V12.1 tool will still work after its APK signature changes.
You must validate the result on an actual Android device.
