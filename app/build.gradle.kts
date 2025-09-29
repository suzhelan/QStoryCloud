plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "top.suzhelan.qstorycloud"
    compileSdk = 36

    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        applicationId = "top.suzhelan.qstorycloud"
        minSdk = 26
        targetSdk = 36
        versionCode = 4
        versionName = "4.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    applicationVariants.all {
        outputs.all {
            if (this is com.android.build.gradle.internal.api.ApkVariantOutputImpl && buildType.name != "debug") {
                outputFileName = "QStory自动云更新_${versionName}-${buildType.name}.apk"
            }
        }
    }

    androidResources {
        additionalParameters.addAll(listOf("--allow-reserved-package-id", "--package-id", "0x25"))
    }

    kotlin {
        jvmToolchain(17)
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

}

dependencies {
    implementation("androidx.core:core-ktx:1.17.0")
    implementation("androidx.work:work-runtime:2.10.5")

    compileOnly("de.robv.android.xposed:api:82")

    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("com.google.android.material:material:1.13.0")

    implementation("com.squareup.okhttp3:okhttp:5.1.0")
    implementation("io.github.billywei01:fastkv:3.0.1")
    val fastjson = "2.0.59"
    implementation("com.alibaba.fastjson2:fastjson2:$fastjson")
    implementation("com.alibaba.fastjson2:fastjson2-kotlin:${fastjson}")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:2.2.20")
    implementation("org.jetbrains.kotlin:kotlin-reflect:2.2.20")
}
