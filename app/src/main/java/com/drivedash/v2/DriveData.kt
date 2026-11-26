package com.drivedash.v2
import kotlin.math.roundToInt
data class Destination(val lat: Double, val lng: Double)
fun parseDestination(input: String): Destination? {
    val parts = input.trim().split(',')
    if (parts.size != 2) return null
    val lat = parts[0].trim().toDoubleOrNull() ?: return null
    val lng = parts[1].trim().toDoubleOrNull() ?: return null
    return if (lat.isFinite() && lng.isFinite() && lat in -90.0..90.0 && lng in -180.0..180.0) Destination(lat, lng) else null
}
fun speedKmh(mps: Float?, ageMs: Long, accuracy: Float?): Int? =
    if (mps == null || !mps.isFinite() || mps < 0f || ageMs !in 0..5000 || accuracy == null || !accuracy.isFinite() || accuracy !in 0f..30f) null
    else (mps * 3.6f).roundToInt()
