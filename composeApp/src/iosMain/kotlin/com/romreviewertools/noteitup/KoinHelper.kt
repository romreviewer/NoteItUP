package com.romreviewertools.noteitup

import com.romreviewertools.noteitup.di.commonModules
import com.romreviewertools.noteitup.di.iosModule
import org.koin.core.context.startKoin

fun initKoin() {
    startKoin {
        modules(commonModules + iosModule)
    }
}
