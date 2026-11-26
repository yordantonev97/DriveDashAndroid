package com.drivedash.v2
import android.app.Activity
import android.os.Bundle
import android.widget.FrameLayout
class Engine(private val activity: Activity): NavigationEngine {
    override fun initialize(host: FrameLayout, state: Bundle?, status: (String, Boolean) -> Unit) {
        host.addView(DemoMap(activity), FrameLayout.LayoutParams(-1,-1)); status("Демо · без навигация", false)
    }
    override fun enable() {}
    override fun route(destination: Destination) {}
    override fun stop() {}
}
