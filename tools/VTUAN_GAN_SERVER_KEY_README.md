# VTUAN Windows APK key-server URL patcher

This script is a conservative static-URL replacement and signing tool. It does not add a login interface, bypass license checks, or translate incompatible authentication APIs.

## Requirements
- Windows 10/11, PowerShell 5.1+
- Java/JDK 17 on PATH (check with: java -version)
- Internet for the first run, to download official Apktool and Uber APK Signer releases
- An APK you have permission to modify

## Run
1. Download VTUAN_GAN_SERVER_KEY.ps1 from this folder.
2. Place it next to the original APK in a folder containing no other APKs.
3. Open PowerShell in the folder. Run:
   powershell -NoProfile -ExecutionPolicy Bypass -File .\VTUAN_GAN_SERVER_KEY.ps1
4. Open VTUAN_KEY_WORK\VTUAN_KET_QUA.txt. The first run only lists static URLs found and DOES NOT alter the APK.
5. If you know the exact original host URL, run:
   powershell -NoProfile -ExecutionPolicy Bypass -File .\VTUAN_GAN_SERVER_KEY.ps1 -OldBaseUrl "https://OLD-SERVER-HOST"
6. The script patches literal appearances in decoded smali/text/XML, rebuilds, debug-signs and writes VTUAN_KEY_SIGNED.apk ONLY if the replacement is found and the build succeeds.

## Limitations
- Domain replacement is NOT full authentication integration. API paths, payloads, device identity, response JSON and key formats must be compatible.
- For old URLs inside native .so, this script stops instead of making dangerous binary edits.
- If the app already has no key prompt at all, a full login UI and authentication client must be integrated using application source code or a compatible editable module.
- The APK is signed with a debug certificate and may not update a previously installed application.
- Successful APK signing does NOT establish that the game launches or that the VTUAN server works.
- Never publish personal keys or other secrets in GitHub issues.

## Automatic validation
The GitHub Actions workflow at .github/workflows/vtuan-powershell-smoke.yml runs Windows PowerShell syntax checks, verifies the release asset lookup, checks missing-APK failure handling, and builds a tiny synthetic Android APK with an old static auth host. It then runs this script and checks that the new host is embedded in the rebuilt, signed result. The synthetic APK test succeeded on the VTUAN branch. This does not prove that a third-party game's authentication protocol is compatible.
