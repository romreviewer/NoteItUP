package com.romreviewertools.noteitup.di

import com.romreviewertools.noteitup.data.cloud.OAuthHandler
import com.romreviewertools.noteitup.data.database.DriverFactory
import com.romreviewertools.noteitup.data.encryption.EncryptionService
import com.romreviewertools.noteitup.data.export.FileExporter
import com.romreviewertools.noteitup.data.export.FileImporter
import com.romreviewertools.noteitup.data.notification.NotificationManager
import com.romreviewertools.noteitup.data.security.BiometricAuthenticator
import com.romreviewertools.noteitup.data.preferences.PreferencesStorage
import io.ktor.client.HttpClient
import io.ktor.client.engine.java.Java
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.dsl.module

val jvmModule = module {
    single { DriverFactory() }
    single { PreferencesStorage() }
    single { FileExporter() }
    single { FileImporter() }
    single { NotificationManager() }
    single { BiometricAuthenticator() }
    single { OAuthHandler() }
    single { EncryptionService() }
    single {
        HttpClient(Java) {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                })
            }
        }
    }
}
