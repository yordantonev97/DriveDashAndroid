#!/usr/bin/env python3
# SPDX-License-Identifier: AGPL-3.0-only
"""Apply the DriveDash 3.0 experimental dashboard to the pinned Open Headunit source."""
import subprocess
import sys
from pathlib import Path
import xml.etree.ElementTree as ET

root = Path(sys.argv[1]).resolve()
pin = "7152adf13a1e5a55799da12f841c8016d1f5cfe0"
assert subprocess.check_output(["git", "rev-parse", "HEAD"], cwd=root, text=True).strip() == pin, "Unexpected upstream commit"

def edit(path, old, new):
    p = root / path
    s = p.read_text(encoding="utf-8")
    assert s.count(old) == 1, f"Patch anchor mismatch: {path} ({s.count(old)})"
    p.write_text(s.replace(old, new), encoding="utf-8")

base = "app/src/main/"
activity = base + "java/com/andrerinas/openheadunit/aap/AapProjectionActivity.kt"
layout = root / (base + "res/layout/activity_headunit.xml")
s = layout.read_text(encoding="utf-8")
assert 'android:id="@+id/container"' in s and s.rstrip().endswith("</FrameLayout>")
s = s.replace('<?xml version="1.0" encoding="utf-8"?>', '', 1).strip()
old = 'android:id="@+id/container"\n    android:layout_width="match_parent"'
assert s.count(old) == 1
s = s.replace(old, 'android:id="@+id/container"\n    android:layout_width="0dp"\n    android:layout_weight="7"', 1)
s = '''<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:orientation="horizontal"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="#0D141D">
''' + s + '''
    <com.andrerinas.openheadunit.drivedash.DriveDashPanel
        android:id="@+id/drivedash_panel"
        android:layout_width="0dp"
        android:layout_height="match_parent"
        android:layout_weight="3"
        android:visibility="gone" />
</LinearLayout>
'''
ET.fromstring(s)
layout.write_text(s, encoding="utf-8")
edit(activity, "        setContentView(R.layout.activity_headunit)", """        setContentView(R.layout.activity_headunit)
        updateDriveDashPanel(resources.configuration)
""")
edit(activity, "        App.isPiPActive = isInPictureInPictureMode", """        App.isPiPActive = isInPictureInPictureMode
        updateDriveDashPanel(newConfig)""")
edit(activity, "        super.onConfigurationChanged(newConfig)\n        nightOverlay?.updateTextSizes()", """        super.onConfigurationChanged(newConfig)
        updateDriveDashPanel(newConfig)
        nightOverlay?.updateTextSizes()""")
edit(activity, "    private fun scheduleShapeCheck() {", """    // DriveDash adds a sibling dashboard; the original projection container, renderer
    // and touch overlay stay intact. Portrait and picture-in-picture keep the original layout.
    private fun updateDriveDashPanel(config: android.content.res.Configuration) {
        val panel = findViewById<View>(R.id.drivedash_panel) ?: return
        panel.visibility = if (
            config.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE &&
            !App.isPiPActive
        ) View.VISIBLE else View.GONE
    }

    private fun scheduleShapeCheck() {""")
edit("app/build.gradle.kts", 'applicationId = "com.andrerinas.headunitrevived"', 'applicationId = "com.drivedash.headunit"')
edit("app/build.gradle.kts", 'versionName = "4.1.0-beta2"', 'versionName = "3.0-experiment01"')
edit("app/build.gradle.kts", "minSdk = 16\n        targetSdk", "minSdk = 29\n        targetSdk")
edit(base + "AndroidManifest.xml", 'android:label="@string/title"', 'android:label="DriveDash Headunit"')
# Rename the app-defined signature permission so the official upstream app can coexist.
for p in list((root / "app/src").rglob("*.kt")) + list((root / "app/src").rglob("*.java")) + list((root / "app/src").rglob("*.xml")) + list((root / "contract").rglob("*.kt")):
    s = p.read_text(encoding="utf-8")
    old = "com.andrerinas.openheadunit.permission.NAVIGATION_UPDATE"
    if old in s:
        p.write_text(s.replace(old, "com.drivedash.headunit.permission.NAVIGATION_UPDATE"), encoding="utf-8")
panel = root / (base + "java/com/andrerinas/openheadunit/drivedash/DriveDashPanel.kt")
assert not panel.exists()
panel.parent.mkdir(parents=True, exist_ok=True)
panel.write_text((Path(__file__).parent / "DriveDashPanel.kt").read_text(encoding="utf-8"), encoding="utf-8")
(root / "DRIVEDASH-MODIFICATIONS.md").write_text(
    "DriveDash 3.0 experimental derivative, based on Open Headunit commit " + pin +
    ".\nChanges: separately signed app identity, landscape dashboard beside the original projection surface, local GPS/clock display.\n"
    "Upstream copyright and license notices remain in place. The modified application is distributed under AGPL-3.0, subject to upstream third-party notices.\n"
    "Patch source: https://github.com/yordantonev97/DriveDashAndroid/tree/drivedash-3-headunit/headunit-experiment\n",
    encoding="utf-8")
print("Applied DriveDash dashboard patch to pinned Open Headunit source.")
