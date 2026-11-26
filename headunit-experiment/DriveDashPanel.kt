// SPDX-License-Identifier: AGPL-3.0-only
package com.andrerinas.openheadunit.drivedash

import android.Manifest
import android.app.KeyguardManager
import android.content.Context
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
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextClock
import android.widget.TextView
import kotlin.math.roundToInt

/** Local-only dashboard: never invents speed, never handles projection touches. */
class DriveDashPanel @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : LinearLayout(context, attrs) {
    private val main = Handler(Looper.getMainLooper())
    private val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    private val keyguard = context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
    private var fix: Location? = null
    private var listening = false
    private var attached = false
    private val speedLabel: TextView
    private val statusLabel: TextView
    private fun dp(x: Int) = (x * resources.displayMetrics.density).roundToInt()
    private fun label(s: String, sp: Float, color: Int, bold: Boolean = false) =
        TextView(context).apply {
            text = s; textSize = sp; setTextColor(color); gravity = Gravity.CENTER
            if (bold) setTypeface(null, Typeface.BOLD)
        }
    private val listener = object : LocationListener {
        override fun onLocationChanged(location: Location) { fix = location; render() }
        override fun onProviderDisabled(provider: String) { fix = null; render() }
        override fun onProviderEnabled(provider: String) {}
        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
    }
    private val ticker = object : Runnable {
        override fun run() {
            if (!attached) return
            if (isShown && windowVisibility == View.VISIBLE && !keyguard.isKeyguardLocked &&
                context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) startGps()
            else stopGps()
            render()
            main.postDelayed(this, 1000L)
        }
    }
    init {
        orientation = VERTICAL
        gravity = Gravity.CENTER
        setPadding(dp(8), dp(8), dp(8), dp(8))
        setBackgroundColor(Color.rgb(13,20,29))
        val white = Color.rgb(243,247,252)
        val muted = Color.rgb(167,184,202)
        addView(label("DriveDash 3.0", 16f, white, true).apply {
            maxLines = 1; setAutoSizeTextTypeUniformWithConfiguration(9,16,1,TypedValue.COMPLEX_UNIT_SP)
        }, LayoutParams(-1, dp(34)))
        val clock = TextClock(context).apply {
            format24Hour = "HH:mm"; format12Hour = "HH:mm"; textSize = 22f
            gravity = Gravity.CENTER; setTextColor(white); setTypeface(null, Typeface.BOLD)
        }
        addView(clock, LayoutParams(-1, dp(42)))
        val card = LinearLayout(context).apply {
            orientation = VERTICAL; gravity = Gravity.CENTER
            background = GradientDrawable().apply { setColor(Color.rgb(29,42,56)); cornerRadius = dp(14).toFloat() }
            setPadding(dp(4),dp(8),dp(4),dp(8))
        }
        card.addView(label("GPS СКОРОСТ", 12f, muted, true))
        speedLabel = label("—", 44f, white, true).apply {
            maxLines = 1; setAutoSizeTextTypeUniformWithConfiguration(16,60,2,TypedValue.COMPLEX_UNIT_SP)
        }
        card.addView(speedLabel, LayoutParams(-1,0,1f))
        card.addView(label("km/h", 13f, muted))
        addView(card, LayoutParams(-1,0,1f))
        statusLabel = label("Изчакване на GPS", 11f, muted).apply { maxLines = 2 }
        addView(statusLabel, LayoutParams(-1,dp(38)))
        addView(label("Android Auto", 13f, white, true), LayoutParams(-1,dp(30)))
    }
    override fun onAttachedToWindow() {
        super.onAttachedToWindow(); attached = true
        main.removeCallbacks(ticker); main.post(ticker)
    }
    override fun onDetachedFromWindow() {
        attached = false; main.removeCallbacks(ticker); stopGps()
        super.onDetachedFromWindow()
    }
    override fun onWindowVisibilityChanged(visibility: Int) {
        super.onWindowVisibilityChanged(visibility)
        if (visibility != View.VISIBLE && attached) stopGps()
    }
    private fun startGps() {
        if (listening) return
        try {
            if (!lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) return
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000L, 0f, listener, Looper.getMainLooper())
            listening = true
        } catch (_: Exception) { fix = null }
    }
    private fun stopGps() {
        if (listening) runCatching { lm.removeUpdates(listener) }
        listening = false; fix = null
    }
    private fun render() {
        val loc = fix
        val age = loc?.let { SystemClock.elapsedRealtime() - it.elapsedRealtimeNanos / 1_000_000L }
        val value = loc?.takeIf {
            age != null && age in 0L..5000L && it.hasSpeed() && it.speed.isFinite() &&
                it.speed >= 0f && it.hasAccuracy() && it.accuracy in 0f..30f
        }?.let { (it.speed * 3.6f).roundToInt() }
        speedLabel.text = value?.toString() ?: "—"
        statusLabel.text = when {
            context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED -> "GPS: няма разрешение"
            value == null -> "Изчакване на GPS"
            else -> "GPS сигнал"
        }
    }
}
