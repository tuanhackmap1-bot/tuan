# VTUAN V4 – standalone Android key gate

This folder builds a separate Android app (package `com.vtuan.gate`) containing the purple VTUAN key entry UI.

## Build

From a checkout with Android SDK 35 and JDK 17:

```bash
gradle -p vtuan_android/gate_v4 :app:assembleDebug
```

The APK is produced at `vtuan_android/gate_v4/app/build/outputs/apk/debug/app-debug.apk`.

Alternatively, open the GitHub Actions workflow **Build standalone VTUAN V4 key gate APK**, then download the **VTUAN-V4-STANDALONE-KEY-APK** artifact from a successful run.

## What it does

- Validates a user-entered key using HTTPS at the VTUAN endpoint. Optional **Mã SV con** supports reseller servers; leave it blank for Admin keys. Both the key and the server identifier must be revalidated when changed.
- Can save a key and server identifier and recheck on the next launch (saved in ordinary app preferences; avoid using a sensitive password as a key).
- After successful validation, shows the key package duration and server-reported expiration date. Tap **Vào Liên Quân** to launch the separately installed `com.garena.game.kgvn` app, or enable **Tự Động Bật** to launch it immediately after validation.
- Displays a message if that application is not installed.

## Test status

CI builds run deterministic JVM checks for expiration parsing and safe Admin/CTV API routing before generating the debug APK. The live VPS and Android device still require integration testing.

## Important limitations

- This is **not** a modified Liên Quân APK, in-game overlay, or native V4 menu.
- It does not ship, load or patch `libysmteam.so`, `libil2cpp.so`, or game assets.
- The expiry field is displayed as supplied by the server, not independently trusted as a proof of validity; enforcement must occur on the server. A successful key check does not unlock game behavior or guarantee the game's launch.
- Build configuration was prepared and source-checked; an APK is not certified until CI passes, and device compatibility requires a real-device test.
- Do not install a debug-signed build over an unrelated package with a different signing key.
