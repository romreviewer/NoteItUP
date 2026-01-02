package com.romreviewertools.noteitup.data.database

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import java.io.File

actual class DriverFactory {
    actual fun createDriver(): SqlDriver {
        val databasePath = File(System.getProperty("user.home"), ".noteitup")
        databasePath.mkdirs()
        val databaseFile = File(databasePath, "diary.db")

        val driver = JdbcSqliteDriver("jdbc:sqlite:${databaseFile.absolutePath}")
        if (!databaseFile.exists()) {
            DiaryDatabase.Schema.create(driver)
        }
        return driver
    }
}
