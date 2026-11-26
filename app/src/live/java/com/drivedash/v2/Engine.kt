package com.drivedash.v2
import android.app.Activity
import android.os.Bundle
import android.widget.FrameLayout
import com.google.android.libraries.navigation.NavigationApi
import com.google.android.libraries.navigation.NavigationView
import com.google.android.libraries.navigation.Navigator
import com.google.android.libraries.navigation.Waypoint
class Engine(private val activity: Activity): NavigationEngine {
    private var view: NavigationView? = null
    private var navigator: Navigator? = null
    private var started = false
    private var dead = false
    private var pending = false
    private var generation = 0
    private var arrival: Navigator.ArrivalListener? = null
    private lateinit var status: (String, Boolean) -> Unit
    override fun initialize(host: FrameLayout, state: Bundle?, status: (String, Boolean) -> Unit) {
        this.status = status
        if (!BuildConfig.HAS_NAV) { host.addView(DemoMap(activity), FrameLayout.LayoutParams(-1,-1)); status("Липсва API ключ · демо", false); return }
        val v = NavigationView(activity); view = v; host.addView(v, FrameLayout.LayoutParams(-1,-1)); v.onCreate(state)
        status("Разрешете точно местоположение", false)
    }
    override fun enable() {
        if (view == null || started || dead) return
        started = true
        NavigationApi.getNavigator(activity, object: NavigationApi.NavigatorListener {
            override fun onNavigatorReady(n: Navigator) {
                if (dead) { n.cleanup(); return }
                navigator = n
                n.setTaskRemovedBehavior(Navigator.TaskRemovedBehavior.QUIT_SERVICE)
                arrival = Navigator.ArrivalListener { stop(); status("Пристигнахте", true) }
                n.addArrivalListener(arrival!!)
                status("Навигацията е готова", true)
            }
            override fun onError(errorCode: Int) { if (!dead) { started = false; status("Грешка в SDK: $errorCode · опитайте отново", false) } }
        })
    }
    override fun route(destination: Destination) {
        val n = navigator ?: return
        if (pending || dead) return
        try {
            val waypoint = Waypoint.builder().setLatLng(destination.lat,destination.lng).build()
            val request = ++generation; pending = true; status("Изчисляване на маршрут…", false)
            n.setDestination(waypoint).setOnResultListener { code ->
                if (!dead && request == generation) {
                    pending = false
                    if (code == Navigator.RouteStatus.OK) { n.setAudioGuidance(Navigator.AudioGuidance.VOICE_ALERTS_AND_GUIDANCE); n.startGuidance(); status("Активен маршрут", true) }
                    else status("Маршрутът не е наличен: $code", true)
                }
            }
        } catch (e: Exception) { pending = false; status("Маршрутът не може да бъде стартиран", true) }
    }
    override fun stop() { generation++; pending = false; navigator?.stopGuidance(); navigator?.clearDestinations(); if (!dead && navigator != null) status("Маршрутът е спрян", true) }
    override fun onStart() { view?.onStart() }
    override fun onResume() { view?.onResume() }
    override fun onPause() { view?.onPause() }
    override fun onStop() { view?.onStop() }
    override fun onDestroy() { dead = true; stop(); arrival?.let { navigator?.removeArrivalListener(it) }; navigator?.cleanup(); view?.onDestroy(); navigator = null }
    override fun save(state: Bundle) { view?.onSaveInstanceState(state) }
    override fun onTrimMemory(level: Int) { view?.onTrimMemory(level) }
}
