# DriveDash 3.0 Headunit experiment 01

This is an experimental, separately installable AGPL-3.0 derivative of [Open Headunit](https://github.com/andreknieriem/open-headunit), pinned to commit `7152adf13a1e5a55799da12f841c8016d1f5cfe0`. It is not a completed integration into DriveDash 2.0. Neither main nor drivedash-2 is modified by this experiment.

The build checks out the full upstream source, applies the local patch, retains the original renderer, touch routing, audio, session logic and setup screens, and adds a landscape 70/30 projection/dashboard layout with real local GPS speed and clock. Portrait and picture-in-picture keep the upstream layout. The original license, copyright and third-party notices are retained. New patch code is AGPL-3.0-only; the built derivative is subject to AGPL and upstream dependency licenses. The exact modified source is uploaded alongside successful builds. Source and APK are experimental; not intended for use while driving until tested.

## Build
Run `.github/workflows/headunit-experiment.yml`. Local equivalents: checkout the pinned upstream revision; run `python3 headunit-experiment/apply.py /path/to/upstream`; in upstream, use JDK 21, SDK 36, NDK 29.0.14206865 and CMake 3.22.1, then `./gradlew :app:assembleGithubDebug --no-daemon`.

## Device setup (parked)
Install the experimental APK (application ID com.drivedash.headunit). The upstream onboarding and connection settings are still present. Choose landscape projection. On the same phone, enable Android Auto developer options (tap Version ten times) and choose Start head unit server. In DriveDash Headunit use the Headunit Server connection option / WiFi loopback. Use original Waze from Android Auto's launcher if the session starts. If necessary, grant precise location for the optional speed display. Stop the headunit server when finished.

Recent Android Auto 17.4+ blocks legacy wireless triggers; Headunit Server is the upstream documented no-root route. No guarantee on Moto G54 until a real session is tested. The receiver does not embed the standalone Waze Android activity, and it does not bypass Google or Android permissions. No map API key is required for the experiment. Do not enable optional root, overlay, device-admin or privileged integrations for this test. Projection sizing, touch mapping, audio, and actual connection require device validation. If installation conflicts or upstream settings fail, report the issue; do not uninstall existing apps unnecessarily.
