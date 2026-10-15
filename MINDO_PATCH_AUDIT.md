# MinDo patch audit — 2026-10-15

**Scope:** Static analysis of the user's MinDoCheat IPA, V1, V2, V4 IPA copies and public [DSMINHDUC](https://github.com/Tranduc1124/DSMINHDUC) sources. No device exploit, no login, no entitlement manipulation, no game-file writes.

## Verified byte-for-byte

| App file | Patch byte count | SHA-256 |
|---|---:|---|
| Original IPA | 72,368 | `0626f718cf51660e2e6659470c5e61999078cb368f31d199ece9615c7f5b9940` |
| V1 UI test | 72,368 | Same |
| V2 state test | 72,368 | Same |
| V4 UI-only | 72,368 | Same |

**Conclusion:** None of the edited app builds changed the bundled patch's bytes.

## Identified format and content

- Header contains `IFix.ILFixInterfaceBridge` and assembly reference `Assembly-CSharp, Version=0.86.0.518`.
- Symbol references include `UnityEngine`, `CurrentMatch`, `IsLocalPlayer`, `GetHeadTF`, `OnGUI` and `/bolacfg.bin`.
- The patch contains no literal `BOLATK1` authentication trailer; the last 64 bytes terminate with iFix metadata including `GetScatterRate` and `OnGUI`.
- This confirms the file is a nontrivial Unity C# hotfix blob, *not* that it can execute correctly on the installed game.

## Upstream version history

- Commit [v213](https://github.com/Tranduc1124/DSMINHDUC/commit/aa49124c10e0d24a2821e1d0bd4ccb7bdaec3931), 2026-10-14: describes bundled patch v201 and `bolacfg.bin` in game Documents.
- Commit [v214](https://github.com/Tranduc1124/DSMINHDUC/commit/23198bb9bbff04688d932fc339373882b63088de), 2026-10-14: adds authorization seal and patch-trailer protections.
- Commit [v215](https://github.com/Tranduc1124/DSMINHDUC/commit/b7b66aed7a349c3c4d97cef7b0d29c7f5eb534e3), 2026-10-14: requires a key-screen root and session revalidation.

In the public code, both `PatchLibrary.latest()` and `Installer.install()` require a license-linked, unexpired personalized patch. `PatchClient.refreshPatch()` obtains a secret and encrypted patch from the licensing server, then decrypts it for the key/device combination. The raw bundled patch is not equivalent to the licensed patch.

## Compatibility and access

- `ExploitRunner.isSupported()` does not include iOS 16.5.
- The user's V5 CHECK installed through both ESign and TrollStore reported it cannot list the global application-container directory. This check **does not** test the original project's MobileHouseArrest activation path and cannot establish game-container write access.
- Whether the current Free Fire game version is compatible with this Unity patch remains unverified.
- The public repository does not include the `MINHDUC/patchsrc/` source directory or `build_patch.py` referenced in its README.

## Engineering decision

Do not label another unchanged-patch, UI-only IPA as functional INJECT. Testing a genuine feature build requires (1) licensed or independently authorized source/payload with verifiable provenance, (2) an installation route with verified and legitimate game-container access, and (3) runtime compatibility tests on the exact game build. No step has been verified so far.

This report does **not** assert that any mechanism is impossible in principle; it separates observed facts from remaining unknowns.
