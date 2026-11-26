# DriveDash 2.0 — Alpha 02
Native Kotlin Android app (Android 10+). This development branch replaces the old ZIP-based layout with source files. The main branch remains the legacy overlay prototype.
Design: https://www.figma.com/design/bBelL229Fp68cWMMwMuXp7?node-id=1-4

## Demo
Landscape 70/30 map/dashboard, clock, GPS-derived speed after granting fine location, settings, and a clearly labelled illustrative map. No fake speed, speed limit, ETA or active guidance. Music and destination controls are placeholders/disabled without live configuration. Test while parked. No Google API key or paid map service is needed.

## Build
JDK 17, Gradle 8.13, Android SDK 36. Run:
    gradle :app:testDemoDebugUnitTest :app:assembleDemoDebug
Demo APK: app/build/outputs/apk/demo/debug/app-demo-debug.apk
CI artifact: DriveDash2-demo-APK.
Live compile check: gradle :app:assembleLiveDebug (does not need a key to compile).

## Live Navigation SDK
Uses Google Navigation SDK 7.6.1. To use it on-device, configure an authorized Google Cloud project, enable Navigation SDK and billing, restrict the Android API key to package com.drivedash.v2 and signing certificate SHA-1, and supply DRIVEDASH_MAPS_KEY as a local environment variable or Gradle property; never commit secrets. A mobile API key can be extracted from the APK; restrictions and quotas are essential. No paid service is enabled by this repository. Destination input is coordinates only, not address search. Live integration requires compile and device validation; navigation behavior and SDK terms/permissions are not yet verified on hardware.

## Scope
Spotify integration, real speed limits outside SDK UI, day theme, weather, address search, store publishing and iOS are deferred. No Android Auto emulation or overlay permissions. No location uploads implemented by the demo app. GPS listeners are removed when the activity stops.
