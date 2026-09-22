plugins { alias(libs.plugins.android.library); alias(libs.plugins.kotlin.android) }
android {
    namespace = "org.studiodroid.runtime.android"
    compileSdk = 36
    buildToolsVersion = "36.0.0"
    ndkVersion = "28.2.13676358"
    defaultConfig { minSdk = 29; ndk { abiFilters += "arm64-v8a" } }
    compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
    lint { abortOnError = true }
}
kotlin { jvmToolchain(17) }
dependencies {
    api(project(":core"))
    implementation(libs.coroutines.android)
    testImplementation(libs.junit)
}
