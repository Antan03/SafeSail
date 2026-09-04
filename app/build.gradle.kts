import java.util.Properties

val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(localPropertiesFile.inputStream())
}


plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    kotlin("plugin.serialization") version "2.0.21"
}

android {
    namespace = "no.uio.ifi.in2000.sofiaalo.team44"
    compileSdk {
        version = release(36)
    }
    useLibrary("org.apache.http.legacy")

    defaultConfig {
        applicationId = "no.uio.ifi.in2000.sofiaalo.team44"
        minSdk = 29
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("String", "DRIFTY_USERNAME", "\"${localProperties["drifty_username"]}\"")
        buildConfigField("String", "DRIFTY_PASSWORD", "\"${localProperties["drifty_password"]}\"")

    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "META-INF/DEPENDENCIES"
            excludes += "META-INF/LICENSE"
            excludes += "META-INF/LICENSE.txt"
            excludes += "META-INF/NOTICE"
            excludes += "META-INF/NOTICE.txt"
            pickFirsts += "mozilla/public-suffix-list.txt"
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.play.services.maps)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.benchmark.common)
    implementation(libs.androidx.compose.remote.creation.compose)
    implementation(libs.androidx.compose.runtime)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.compiler)
    implementation(libs.firebase.crashlytics.buildtools)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.datastore.core)
    implementation(libs.androidx.compose.foundation.layout)
    implementation(libs.androidx.ui)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    val ktorVersion = "3.2.3"
    implementation("io.ktor:ktor-client-core:${ktorVersion}")
    implementation("io.ktor:ktor-client-okhttp:${ktorVersion}")
    implementation("io.ktor:ktor-client-content-negotiation:${ktorVersion}")
    implementation("io.ktor:ktor-serialization-kotlinx-json:${ktorVersion}")
    implementation("io.ktor:ktor-client-auth:${ktorVersion}")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")
    implementation("io.coil-kt:coil-compose:2.6.0")    //Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.1")
    implementation("androidx.navigation:navigation-compose:2.7.7")
    implementation("androidx.compose.material:material-icons-extended:1.6.0")
    // maplibre
    implementation("org.maplibre.gl:android-sdk:11.8.2")
    implementation("com.mapbox.mapboxsdk:mapbox-sdk-turf:6.12.0")
    //ViewModel
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")

    // Hovedbiblioteket for NetCDF simulering til Drifty
    implementation("edu.ucar:cdm-core:5.4.1")

    implementation("edu.ucar:netcdf4:5.4.1")

    implementation("org.slf4j:slf4j-android:1.7.36")

    //Testing dependency
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.11.0-rc01")
    testImplementation("androidx.test:core:1.5.0")
    testImplementation("org.robolectric:robolectric:4.10")
    testImplementation("org.mockito.kotlin:mockito-kotlin:6.3.0")
}