# CarHub

A personal Android app for a Hyundai i20 with Android Auto or wired CarPlay.

## What it does

| Where | What you get |
|---|---|
| **Phone** | Home screen with big tiles for YouTube, Prime Video, Netflix, JioHotstar and Airtel Xstream (opens the official apps). "My Streams": add your own HLS/MP4/MP3 links or an M3U channel list and watch them full screen. "Choose apps" controls exactly which apps are shown. |
| **Android Auto (car screen)** | "My Streams" as an **audio** app you can browse and control from the car. |
| **CarPlay AI box** (Android box plugged into the car's USB CarPlay port) | Install CarHub on the box and choose it as the box's home screen. The car display then shows only your chosen apps. Video works here, **only use it when parked**. |

## What it cannot do (platform rules, not missing code)

- Show video on the car screen through Android Auto or CarPlay from a phone. Google and Apple block this for phone apps.
- Play Netflix, Prime, Hotstar or similar inside CarHub. They are DRM-protected and only play in their own apps.
- Video while driving. Not supported, and illegal in India on a screen the driver can see.

## Build

**On GitHub (no software needed):** push this folder to a GitHub repository. The **Build APK** workflow runs automatically. Open the run under the **Actions** tab and download **CarHub-apk**.

**On a computer:** install Android Studio, open this folder, and choose **Build → Generate APKs**. Or from a terminal with the Android SDK installed:

```
./gradlew testDebugUnitTest assembleRelease
```

The APK is at `app/build/outputs/apk/release/app-release.apk`.

The APK is signed with the build machine's debug key. An APK from a different machine can't install over an existing copy; uninstall first (this clears saved streams).

## Install and use with Android Auto

1. Install the APK on the phone (allow "install unknown apps" for your browser/Files app).
2. Android Auto hides apps not installed from the Play Store. Two ways around it:
   - Enable Android Auto developer mode (Settings → Apps → Android Auto → tap *Version* 10 times), then ⋮ → *Developer settings* → tick **Unknown sources**.
   - If CarHub still doesn't appear, reinstall from a computer so Android records the Play Store as the installer:
     ```
     adb install -r -i com.android.vending app-release.apk
     ```
3. Open CarHub once, add a stream, then check Android Auto → **Customize launcher**.

## Privacy

No accounts, analytics or ads. Streams and app choices are stored only on the device (backups disabled). The only network traffic is the streams and playlist links you add.
