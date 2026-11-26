package com.drivedash.v2
import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.ActivityNotFoundException
import android.content.res.Configuration
import android.view.View
import android.os.Build
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.provider.Settings
import android.view.Gravity
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : Activity() {
    private val bg = Color.rgb(13,20,29)
    private val surface = Color.rgb(29,42,56)
    private val action = Color.rgb(38,51,66)
    private val ink = Color.rgb(243,247,252)
    private val muted = Color.rgb(167,184,202)
    private val blue = Color.rgb(105,170,255)
    private lateinit var engine: NavigationEngine
    private lateinit var speed: TextView
    private lateinit var time: TextView
    private lateinit var status: TextView
    private lateinit var route: Button
    private lateinit var mapPanel: FrameLayout
    private lateinit var dashboardPanel: LinearLayout
    private lateinit var modeLabel: TextView
    private lateinit var versionLabel: TextView
    private lateinit var mediaPanel: LinearLayout
    private lateinit var location: LocationManager
    private val handler = Handler(Looper.getMainLooper())
    private var fix: Location? = null
    private var listening = false
    private val listener = object : LocationListener {
        override fun onLocationChanged(l: Location) { fix=l; renderSpeed() }
        override fun onProviderDisabled(provider:String) { fix=null; renderSpeed() }
        override fun onProviderEnabled(provider:String) {}
        override fun onStatusChanged(provider:String?, status:Int, extras:Bundle?) {}
    }
    private val timer = object : Runnable {
        override fun run() { time.text=SimpleDateFormat("HH:mm",Locale.getDefault()).format(Date()); renderSpeed(); handler.postDelayed(this,1000) }
    }
    private fun dp(n: Int) = (n*resources.displayMetrics.density).toInt()
    private fun rounded(color:Int,r:Int=16)=GradientDrawable().apply { setColor(color); cornerRadius=dp(r).toFloat() }
    private fun text(s:String,size:Float,color:Int=ink,bold:Boolean=false)=TextView(this).apply {
        text=s; textSize=size; setTextColor(color); gravity=Gravity.CENTER
        if(bold) setTypeface(null,Typeface.BOLD)
    }
    private fun button(s:String,actionFn:()->Unit)=Button(this).apply {
        text=s; textSize=14f; isAllCaps=false; setTextColor(ink); background=rounded(action,14)
        minHeight=dp(48); setPadding(dp(10),0,dp(10),0); setOnClickListener { actionFn() }
    }
    private fun stack()=LinearLayout(this).apply { orientation=LinearLayout.VERTICAL; gravity=Gravity.CENTER }
    override fun onCreate(savedInstanceState:Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        location=getSystemService(LocationManager::class.java)
        val outer=LinearLayout(this).apply { orientation=LinearLayout.VERTICAL; setBackgroundColor(bg) }
        outer.setOnApplyWindowInsetsListener { v,i ->
            val b=if(android.os.Build.VERSION.SDK_INT>=30) i.getInsets(WindowInsets.Type.systemBars() or WindowInsets.Type.displayCutout()) else null
            v.setPadding((b?.left?:i.systemWindowInsetLeft)+dp(8),(b?.top?:i.systemWindowInsetTop)+dp(4),(b?.right?:i.systemWindowInsetRight)+dp(8),(b?.bottom?:i.systemWindowInsetBottom)+dp(6)); i
        }
        val header=LinearLayout(this).apply { gravity=Gravity.CENTER_VERTICAL }
        header.addView(text("DriveDash",20f,ink,true).apply { gravity=Gravity.START or Gravity.CENTER_VERTICAL },LinearLayout.LayoutParams(-2,dp(36)))
        versionLabel=text(" 2.0",16f,blue,true)
        header.addView(versionLabel,LinearLayout.LayoutParams(-2,dp(36)))
        modeLabel=text(if(BuildConfig.HAS_NAV) "НАВИГАЦИЯ" else "WAZE / ДЕМО",12f,muted)
        header.addView(modeLabel,LinearLayout.LayoutParams(0,dp(36),1f))
        time=text("--:--",18f,ink,true); header.addView(time,LinearLayout.LayoutParams(dp(68),dp(36)))
        outer.addView(header)
        val body=LinearLayout(this).apply { orientation=LinearLayout.HORIZONTAL }
        val map=FrameLayout(this).apply { background=rounded(surface); clipToOutline=true }
        body.addView(map,LinearLayout.LayoutParams(0,-1,7f))
        val panel=stack().apply { setPadding(dp(8),0,0,0) }
        body.addView(panel,LinearLayout.LayoutParams(0,-1,3f))
        mapPanel=map; dashboardPanel=panel
        val speedCard=stack().apply { background=rounded(surface); setPadding(dp(3),dp(4),dp(3),dp(4)) }
        speedCard.addView(text("GPS СКОРОСТ",11f,muted,true))
        speed=text("—",42f,ink,true).apply { includeFontPadding=false }
        speedCard.addView(speed,LinearLayout.LayoutParams(-1,0,1f))
        speed.setAutoSizeTextTypeUniformWithConfiguration(16,48,2,android.util.TypedValue.COMPLEX_UNIT_SP)
        speedCard.addView(text("km/h",12f,muted))
        panel.addView(speedCard,LinearLayout.LayoutParams(-1,0,1.7f))
        val media=stack().apply { background=rounded(surface); setPadding(dp(3),dp(2),dp(3),dp(2)) }
        media.addView(text("МУЗИКА",10f,muted,true))
        media.addView(text("Очаквайте",12f,ink))
        mediaPanel=media
        panel.addView(media,LinearLayout.LayoutParams(-1,0,0.8f).apply { topMargin=dp(5) })
        status=text("Инициализация…",11f,muted); status.maxLines=2; status.setPadding(dp(2),dp(2),dp(2),dp(2))
        panel.addView(status,LinearLayout.LayoutParams(-1,-2))
        route=button(if(BuildConfig.HAS_NAV) "Дестинация" else "Отвори Waze") { if(BuildConfig.HAS_NAV) destinationDialog() else openWaze() }
        route.isEnabled=!BuildConfig.HAS_NAV; route.alpha=if(route.isEnabled)1f else .55f
        panel.addView(route,LinearLayout.LayoutParams(-1,dp(48)))
        if(BuildConfig.HAS_NAV) panel.addView(button("Спри маршрута") { engine.stop() },LinearLayout.LayoutParams(-1,dp(48)).apply { topMargin=dp(4) })
        panel.addView(button("Настройки / GPS") { showSettings() },LinearLayout.LayoutParams(-1,dp(48)).apply { topMargin=dp(4) })
        outer.addView(body,LinearLayout.LayoutParams(-1,0,1f))
        setContentView(outer)
        engine=Engine(this); engine.initialize(map,savedInstanceState) { msg,ready -> runOnUiThread { if(!isDestroyed) { status.text=if(BuildConfig.HAS_NAV)msg else "Waze · отделно приложение"; route.isEnabled=if(BuildConfig.HAS_NAV)ready else true; route.alpha=if(route.isEnabled)1f else .55f } } }
        refreshLayout()
    }
    private fun refreshLayout() {
        if(!::mapPanel.isInitialized) return
        val compact=isInMultiWindowMode && !BuildConfig.HAS_NAV
        mapPanel.visibility=if(compact) View.GONE else View.VISIBLE
        dashboardPanel.setPadding(if(compact)0 else dp(8),0,0,0)
        versionLabel.visibility=if(compact && resources.configuration.screenWidthDp < 330) View.GONE else View.VISIBLE
        modeLabel.text=if(compact) "" else if(BuildConfig.HAS_NAV) "НАВИГАЦИЯ" else "WAZE / ДЕМО"
        mediaPanel.visibility=if(compact && resources.configuration.screenHeightDp < 260) View.GONE else View.VISIBLE
    }
    override fun onMultiWindowModeChanged(inMultiWindowMode: Boolean, newConfig: Configuration) {
        super.onMultiWindowModeChanged(inMultiWindowMode,newConfig)
        refreshLayout()
    }
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        refreshLayout()
    }
    private fun openWaze() {
        val intent=packageManager.getLaunchIntentForPackage("com.waze")
        if(intent==null) {
            AlertDialog.Builder(this).setTitle("Waze не е наличен")
                .setMessage("Инсталирайте официалното приложение Waze от Google Play и опитайте отново.")
                .setPositiveButton("OK",null).show()
            return
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        if(Build.VERSION.SDK_INT >= 32 || isInMultiWindowMode) intent.addFlags(Intent.FLAG_ACTIVITY_LAUNCH_ADJACENT)
        try { startActivity(intent) } catch(_: ActivityNotFoundException) {
            Toast.makeText(this,"Не може да се отвори Waze",Toast.LENGTH_LONG).show()
        } catch(_: SecurityException) {
            Toast.makeText(this,"Android отказа стартирането на Waze",Toast.LENGTH_LONG).show()
        }
    }
    private fun showSettings() {
        AlertDialog.Builder(this).setTitle("Настройки")
            .setItems(arrayOf("Разреши GPS скоростомер","Настройки на местоположението","За приложението","Помощ за Waze")) { _,i ->
                when(i) {
                    0 -> requestGps()
                    1 -> startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                    2 -> AlertDialog.Builder(this).setTitle("DriveDash 2.0 Alpha").setMessage("Тестова версия. Не използвайте демонстрационната карта за шофиране. Настройвайте само при спрял автомобил.").setPositiveButton("OK",null).show()
                    3 -> AlertDialog.Builder(this).setTitle("Waze и DriveDash")
                        .setMessage("Бутонът Отвори Waze опитва да стартира Waze до DriveDash. Ако се отвори на цял екран, използвайте системното меню за последните приложения и Split screen. Размерът и редът се определят от телефона. Настройвайте само когато сте спрели.")
                        .setPositiveButton("OK",null).show()
                }
            }.show()
    }
    private fun hasGps()=checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED
    private fun requestGps() { if(hasGps()) { startGps(); engine.enable() } else requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION),101) }
    override fun onRequestPermissionsResult(requestCode:Int,permissions:Array<out String>,grantResults:IntArray) {
        super.onRequestPermissionsResult(requestCode,permissions,grantResults)
        if(requestCode==101) { if(hasGps()) { startGps(); engine.enable() } else Toast.makeText(this,"Нужно е точно местоположение за GPS.",Toast.LENGTH_LONG).show() }
    }
    private fun startGps() {
        if(listening||!hasGps()) return
        try { location.requestLocationUpdates(LocationManager.GPS_PROVIDER,1000L,0f,listener,Looper.getMainLooper()); listening=true } catch(_:Exception) { fix=null; renderSpeed() }
    }
    private fun renderSpeed() {
        val l=fix; val age=l?.let { SystemClock.elapsedRealtime()-it.elapsedRealtimeNanos/1_000_000 }?:Long.MAX_VALUE
        speed.text=speedKmh(l?.takeIf{it.hasSpeed()}?.speed,age,l?.takeIf{it.hasAccuracy()}?.accuracy)?.toString()?:"—"
    }
    private fun destinationDialog() {
        val edit=EditText(this).apply { hint="42.6977, 23.3219"; inputType=android.text.InputType.TYPE_CLASS_TEXT; setSingleLine(true) }
        val d=AlertDialog.Builder(this).setTitle("Координати на дестинация").setMessage("Въведете ширина, дължина, само при спрял автомобил.").setView(edit).setNegativeButton("Отказ",null).setPositiveButton("Маршрут",null).create()
        d.setOnShowListener { d.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener { val dest=parseDestination(edit.text.toString()); if(dest==null) edit.error="Невалидни координати" else { d.dismiss(); engine.route(dest) } } }; d.show()
    }
    override fun onStart() { super.onStart(); engine.onStart(); if(hasGps()) { startGps(); engine.enable() }; handler.post(timer) }
    override fun onResume() { super.onResume(); engine.onResume() }
    override fun onPause() { engine.onPause(); super.onPause() }
    override fun onStop() { handler.removeCallbacks(timer); if(listening) location.removeUpdates(listener); listening=false; fix=null; engine.onStop(); super.onStop() }
    override fun onSaveInstanceState(outState:Bundle) { super.onSaveInstanceState(outState); engine.save(outState) }
    override fun onTrimMemory(level:Int) { super.onTrimMemory(level); if(::engine.isInitialized) engine.onTrimMemory(level) }
    override fun onDestroy() { if(::engine.isInitialized) engine.onDestroy(); super.onDestroy() }
}
