# VibeRoom Android

VibeRoom is a Kotlin + Jetpack Compose language-learning and live voice-room Android app built with Firebase (Auth, Firestore, Storage, AI Logic) and WebRTC.

## Build & Download APK Directly on GitHub

1. Push this repository to GitHub (on the `main` or `master` branch) using AI Studio's **Push to GitHub** menu or `git push`.
2. Open your repository on GitHub and click the **Actions** tab.
3. Wait for the **VibeRoom APK Builder** workflow to finish (or click **VibeRoom APK Builder** -> **Run workflow** -> **Run workflow**).
4. Open the completed workflow run and scroll to **Artifacts** at the bottom to download `VibeRoom-v16.0-debug.zip` (unzip it to install `VibeRoom-v16.0-debug.apk` on Android).

### Optional Signed Release Build
To build a `release` APK via **Run workflow -> build_type: release**, add these repository secrets under **Settings -> Secrets and variables -> Actions**:
- `RELEASE_KEYSTORE_BASE64`: Base64-encoded `.jks` upload keystore (with key alias `upload`)
- `STORE_PASSWORD`: Keystore password
- `KEY_PASSWORD`: Key password
