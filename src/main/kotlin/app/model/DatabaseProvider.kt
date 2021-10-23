package app.model

import app.orm.AbstractDatabaseProvider
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.application.*


class DatabaseProvider(private val app : Application) : AbstractDatabaseProvider(HikariDataSource(HikariConfig().apply {
    val appConfig = app.environment.config
    driverClassName = appConfig.property("db.driverClassName").getString()
    jdbcUrl = appConfig.property("db.jdbcUrl").getString()
    maximumPoolSize = appConfig.property("db.maximumPoolSize").getString().toInt()
    isAutoCommit = appConfig.property("db.isAutoCommit").getString().toBoolean()
    transactionIsolation = appConfig.property("db.transactionIsolation").getString()
    validate()
}))