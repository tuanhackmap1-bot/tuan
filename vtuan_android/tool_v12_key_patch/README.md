# VTUAN key gate inside an existing APK

This directory prepares an **in-place key entry screen** for an Android APK.
It changes the host app's **launcher** to the VTUAN key Activity. After a
successful remote key check, that screen opens the **original** host Activity
(the value of the `vtuan.original_launcher` manifest metadata).
It does not launch a separate Liên Quân app in embedded mode.

The tool preserves original raw classes.dex files and inserts the gate's
compiled DEX into new classesN.dex entries, then rebuilds with Apktool and
re-signs the result. It recognizes both a normal launcher Activity and an
`activity-alias`. It refuses an ambiguous multi-launcher manifest.

## Validation

The `Validate VTUAN key injection into host APK` GitHub Actions workflow:
1. Executes manifest and DEX-merging unit tests.
2. Builds a small, separate Android host and the VTUAN key APK.
3. Applies the patch, rebuilds and signs the host APK.
4. Validates the new package, launcher, injected DEX and APK signature.

This **does not** confirm compatibility with the separately supplied 244 MB
`TTS_TOOL_V12.1_BYPASS_NO_KEY.apk`: that binary must be processed and tested
individually.

## Producing the real patched APK

GitHub Actions > **Build VTUAN key inside provided APK** > Run workflow:
provide a **direct HTTPS download URL** for an APK you may modify.
Optionally supply its SHA256. If successful, download the
`VTUAN-V12-INTEGRATED-KEY-APK` artifact (7-day retention).
No APK binary is committed into the source repository by this process.

Alternatively use Python 3, JDK 17, Apktool 2.11.1, Android SDK's zipalign
and apksigner. Build `vtuan_android/gate_v4` first and run:

```sh
python3 vtuan_android/tool_v12_key_patch/patch_host.py \
  --apk TTS_TOOL_V12.1_BYPASS_NO_KEY.apk \
  --gate vtuan_android/gate_v4/app/build/outputs/apk/debug/app-debug.apk \
  --apktool apktool_2.11.1.jar \
  --output TTS_TOOL_V12.1_VTUAN_KEY_PATCHED.apk
```

Pass `--apksigner` and `--zipalign` if those Android tools are not in PATH.

## Limitations and safety

- This is a new app signature. Android generally requires uninstalling a
  differently signed installation before installing the patched APK. Back up
  any app data first.
- The key screen is a launcher-level check, **not tamper-proof enforcement**:
  exported components or deep links may still reach the original app.
- A host may enforce its original signing certificate or use protected/native
  loading and crash after modification. The tool does not promise to defeat
  integrity checks or modify game behavior.
- The key endpoint must return the fields expected by the VTUAN gate. A
  successful build alone does not validate real server key responses.
- Only modify APKs you own or have permission to modify. Keep any tool APK
  and its generated APK private if redistribution is not authorized.
