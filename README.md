# DriveDash Android 0.2 — Handoff prototype

Native Kotlin Android 10+ (minSdk 29), target 34, compile SDK 35. v0.2 retains the v0.1 floating Spotify dashboard and adds an optional personal destination inbox. No root, Android Auto emulation, accessibility service, or GPS permission. The Android dashboard works without setting up the relay.

## Build and install

Extract this ZIP into the root of a separate GitHub repository (include the hidden `.github` and `.gitignore` entries; `settings.gradle` must be at repository root). GitHub Actions builds a debug-signed APK. Alternatively use JDK 17, Gradle 8.9 and Android SDK 35: `gradle :app:assembleDebug`. Download the `DriveDash-Android-APK` artifact, extract and install `app-debug.apk`. Allow installation from the chosen source only as needed. Debug builds from different GitHub-hosted runners may have different signing keys; uninstalling the prior prototype may be required before updating (loses local settings). This does not alter the existing iOS repo.

On Android, enable the floating dashboard permission, optionally enable DriveDash notification access, start Spotify and use Start Waze / Start Google Maps. Drag or tap the panel header. Use Received route on the panel to return to DriveDash or Stop to end the foreground service. Rotate the phone manually; navigation controls its own orientation. Test while parked. Notification access is broad; the code uses it to query Spotify media sessions and does not upload media or notification contents. Revoke the permissions when no longer needed.

## Destination Handoff: optional setup

See [Handoff setup](relay/SETUP.md). Requires a personal HTTPS Cloudflare Worker with D1, a randomly generated 32-byte pairing key encoded as 43 base64url characters, and an iPhone Shortcut configured with that endpoint and key. Neither a relay nor a Shortcut has been deployed/installed yet.

From Waze, Google Maps or Apple Maps on iPhone, share the destination link to the Shortcut. Open DriveDash on Android (or tap Received route on its floating panel). DriveDash fetches immediately and every 15 seconds **only while its activity is visible**, or on Check for destination. Review the destination and tap the supported navigation app. No background polling, push notifications, or automatic route switching. Both phones need internet but need not share Wi-Fi. The server stores one latest link for up to 12 hours of availability; the next send replaces it. Clear received destination removes the live row, and an hourly scheduled job clears expired live rows. Provider backups may retain data longer. Shared map URLs may contain sensitive locations and route details. The relay uses HTTPS and a secret, not end-to-end encryption. Use only a private pairing key and never commit it or publish screenshots of it. Rotate the key on both devices and the Worker if exposed.

Explicit destination parameters can be converted to either Google Maps or Waze; simple address searches are not guaranteed to resolve to the identical point in both providers. Opaque short links are handed to the source provider only; if Android cannot open them directly, copy the link and open it manually. Opaque Apple Maps links and Apple links with only map center coordinates cannot be converted safely: use an address or share from Google Maps/Waze instead. Google links with waypoints/place IDs stay with Google Maps. A shared link is not an active navigation session: route preferences, stops, ETAs, progress and starting point may not be preserved. Confirm the destination before driving.

## Validation

- Local Kotlin/JVM tests of link parsing and conversion: `kotlinc app/src/main/java/com/drivedash/android/RouteLinks.kt tests/RouteLinksTest.kt -include-runtime -d /tmp/drivedash-route-tests.jar && java -jar /tmp/drivedash-route-tests.jar`.
- Local Worker tests with a real in-memory SQLite database and a D1 API adapter: `node --test relay/test/relay.test.mjs` (Node 22+). These run in GitHub Actions too.
- Not yet compiled against Android SDK, deployed to Cloudflare or tested on either phone. The floating dashboard is not a true 70/30 split-screen. Background route push and automatic launch are not implemented.
