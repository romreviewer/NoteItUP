package com.romreviewertools.noteitup.data.database

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver

actual class DriverFactory {
    actual fun createDriver(): SqlDriver {
        return NativeSqliteDriver(
            schema = DiaryDatabase.Schema,
            name = "diary.db"
        )
    }
}
