package com.drivedash.v2
import org.junit.Test
import org.junit.Assert.*
class DriveDataTest {
    @Test fun destination() { assertEquals(Destination(42.7,23.3),parseDestination(" 42.7, 23.3 ")); listOf("NaN,0","91,0","0,181","1,2,3","bad","Infinity,0").forEach { assertNull(parseDestination(it)) } }
    @Test fun coordinateLimits() { assertEquals(Destination(-90.0,180.0),parseDestination("-90,180")); assertEquals(Destination(0.0,0.0),parseDestination("0,0")) }
    @Test fun speed() { assertEquals(36,speedKmh(10f,1000,5f)); assertNull(speedKmh(10f,5001,5f)); assertNull(speedKmh(10f,10,31f)); assertNull(speedKmh(null,10,5f)); assertNull(speedKmh(-1f,10,5f)); assertNull(speedKmh(Float.NaN,10,5f)) }
    @Test fun speedLimits() { assertEquals(0,speedKmh(0f,0,0f)); assertEquals(18,speedKmh(5f,5000,30f)); assertNull(speedKmh(5f,-1,4f)); assertNull(speedKmh(5f,0,null)) }
}
