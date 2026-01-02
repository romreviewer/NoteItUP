package com.romreviewertools.noteitup.di

import com.romreviewertools.noteitup.data.cloud.OAuthHandler
import com.romreviewertools.noteitup.data.database.DriverFactory
import com.romreviewertools.noteitup.data.encryption.EncryptionService
import com.romreviewertools.noteitup.data.export.FileExporter
import com.romreviewertools.noteitup.data.export.FileImporter
import com.romreviewertools.noteitup.data.location.LocationService
import com.romreviewertools.noteitup.data.media.ImagePicker
import com.romreviewertools.noteitup.data.notification.NotificationManager
import com.romreviewertools.noteitup.data.security.BiometricAuthenticator
import com.romreviewertools.noteitup.data.preferences.PreferencesStorage
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val androidModule = module {
    single { DriverFactory(androidContext()) }
    single { PreferencesStorage(androidContext()) }
    single { FileExporter(androidContext()) }
    single { FileImporter(androidContext()) }
    single { NotificationManager(androidContext()) }
    single { BiometricAuthenticator(androidContext()) }
    single { OAuthHandler(androidContext()) }
    single { EncryptionService() }
    single { ImagePicker(androidContext()) }
    single { LocationService(androidContext()) }
    single {
        HttpClient(OkHttp) {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                })
            }
        }
    }
}
