package com.drivedash.v2
import android.os.Bundle
import android.widget.FrameLayout
interface NavigationEngine {
    fun initialize(host: FrameLayout, state: Bundle?, status: (String, Boolean) -> Unit)
    fun enable()
    fun route(destination: Destination)
    fun stop()
    fun onStart() {}
    fun onResume() {}
    fun onPause() {}
    fun onStop() {}
    fun onDestroy() {}
    fun save(state: Bundle) {}
    fun onTrimMemory(level: Int) {}
}
