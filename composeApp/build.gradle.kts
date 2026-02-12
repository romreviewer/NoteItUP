import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

// Load local.properties for API keys
val localProperties = Properties().apply {
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        load(localPropertiesFile.inputStream())
    }
}

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
    alias(libs.plugins.kotlinxSerialization)
    alias(libs.plugins.sqldelight)
    alias(libs.plugins.googleServices)
    alias(libs.plugins.firebaseCrashlytics)
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
    
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }
    
    jvm()
    
    sourceSets {
        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)

            // SQLDelight Android driver
            implementation(libs.sqldelight.android.driver)

            // Koin Android
            implementation(libs.koin.android)

            // Biometric authentication
            implementation(libs.androidx.biometric)

            // Ktor Android engine
            implementation(libs.ktor.client.okhttp)

            // Location services
            implementation(libs.play.services.location)

            // Apache Commons Compress for TAR extraction
            implementation("org.apache.commons:commons-compress:1.25.0")

            // Chrome Custom Tabs for better browser experience
            implementation("androidx.browser:browser:1.8.0")

            // Play In-App Review
            implementation(libs.play.review)

            // Google Identity Services (native Google Sign-In for Drive OAuth)
            implementation(libs.play.services.auth)

            // Firebase (using BOM via dependencies block below)
        }
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.materialIconsExtended)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)

            // SQLDelight
            implementation(libs.sqldelight.runtime)
            implementation(libs.sqldelight.coroutines)

            // Koin
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)

            // Navigation
            implementation(libs.navigation.compose)

            // Kotlinx
            implementation(libs.kotlinx.datetime)
            implementation(libs.kotlinx.serialization.json)

            // UUID
            implementation(libs.uuid)

            // Markdown rendering
            implementation(libs.multiplatform.markdown.renderer)
            implementation(libs.multiplatform.markdown.renderer.m3)

            // Rich Text Editor
            implementation(libs.richeditor.compose)

            // Ktor HTTP Client (Cloud Sync)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)

            // Okio (ZIP compression)
            implementation(libs.okio)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        iosMain.dependencies {
            // SQLDelight iOS driver
            implementation(libs.sqldelight.native.driver)

            // Ktor iOS engine
            implementation(libs.ktor.client.darwin)
        }
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutinesSwing)

            // SQLDelight JVM driver
            implementation(libs.sqldelight.sqlite.driver)

            // Ktor JVM engine
            implementation(libs.ktor.client.java)

            // Apache Commons Compress for TAR extraction
            implementation("org.apache.commons:commons-compress:1.25.0")
        }
    }
}

android {
    namespace = "com.romreviewertools.noteitup"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.romreviewertools.noteitup"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 10
        versionName = "1.4.0"

        // API Keys from local.properties (gitignored)
        val dropboxAppKey = localProperties.getProperty("DROPBOX_APP_KEY", "")
        val dropboxAppSecret = localProperties.getProperty("DROPBOX_APP_SECRET", "")
        val googleClientId = localProperties.getProperty("GOOGLE_CLIENT_ID", "")
        val googleClientSecret = localProperties.getProperty("GOOGLE_CLIENT_SECRET", "")

        // BuildConfig fields for runtime access
        buildConfigField("String", "DROPBOX_APP_KEY", "\"$dropboxAppKey\"")
        buildConfigField("String", "DROPBOX_APP_SECRET", "\"$dropboxAppSecret\"")
        buildConfigField("String", "GOOGLE_CLIENT_ID", "\"$googleClientId\"")
        buildConfigField("String", "GOOGLE_CLIENT_SECRET", "\"$googleClientSecret\"")

        // Manifest placeholders for intent filter
        manifestPlaceholders["dropboxScheme"] = "db-$dropboxAppKey"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    debugImplementation(compose.uiTooling)

    // Firebase BOM and dependencies
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.crashlytics)
}

compose.desktop {
    application {
        mainClass = "com.romreviewertools.noteitup.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "com.romreviewertools.noteitup"
            packageVersion = "1.1.0"
        }
    }
}

// SQLDelight configuration
sqldelight {
    databases {
        create("DiaryDatabase") {
            packageName.set("com.romreviewertools.noteitup.data.database")
        }
    }
}
