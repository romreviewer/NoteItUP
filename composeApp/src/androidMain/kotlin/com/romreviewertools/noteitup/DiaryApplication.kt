package com.romreviewertools.noteitup

import android.app.Application
import com.romreviewertools.noteitup.di.androidModule
import com.romreviewertools.noteitup.di.commonModules
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class DiaryApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger(Level.DEBUG)
            androidContext(this@DiaryApplication)
            modules(commonModules + androidModule)
        }
    }
}
