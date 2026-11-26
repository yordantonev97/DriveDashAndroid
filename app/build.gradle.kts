plugins { id("com.android.application"); id("org.jetbrains.kotlin.android") }
android {
    namespace = "com.drivedash.v2"
    compileSdk = 36
    defaultConfig {
        applicationId = "com.drivedash.v2"
        minSdk = 29
        targetSdk = 36
        versionCode = 3
        versionName = "2.0-alpha03"
    }
    buildFeatures { buildConfig = true }
    flavorDimensions += "navigation"
    productFlavors {
        create("demo") { dimension = "navigation"; applicationIdSuffix = ".demo"; buildConfigField("boolean", "HAS_NAV", "false") }
        create("live") {
            dimension = "navigation"
            val key = providers.environmentVariable("DRIVEDASH_MAPS_KEY").orElse(providers.gradleProperty("DRIVEDASH_MAPS_KEY")).orElse("").get()
            manifestPlaceholders["mapsKey"] = key
            buildConfigField("boolean", "HAS_NAV", key.isNotBlank().toString())
        }
    }
    compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17; isCoreLibraryDesugaringEnabled = true }
    kotlinOptions { jvmTarget = "17" }
}
dependencies {
    add("liveImplementation", "com.google.android.libraries.navigation:navigation:7.6.1")
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs_nio:2.1.5")
    testImplementation("junit:junit:4.13.2")
}
