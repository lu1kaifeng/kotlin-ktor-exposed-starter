package app.service

import app.model.Subjects
import app.model.Widgets
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.application.*

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory
import javax.sql.DataSource

class DatabaseFactory(private val app : Application) {

    private val log = LoggerFactory.getLogger(this::class.java)

    init {
        log.info("Initialising database")
        val pool = hikari()
        Database.connect(pool)
         transaction{
            SchemaUtils.createMissingTablesAndColumns(Subjects, Widgets)
        }
        findAllClass("")
    }

    private fun findAllClass(packageName :String){
        val stream = app.environment.classLoader
            .definedPackages
        return
    }

    private fun hikari(): HikariDataSource {
        val appConfig = app.environment.config
        val config = HikariConfig().apply {
            driverClassName = appConfig.property("db.driverClassName").getString()
            jdbcUrl = appConfig.property("db.jdbcUrl").getString()
            maximumPoolSize = appConfig.property("db.maximumPoolSize").getString().toInt()
            isAutoCommit = appConfig.property("db.isAutoCommit").getString().toBoolean()
            transactionIsolation = appConfig.property("db.transactionIsolation").getString()
            validate()
        }
        return HikariDataSource(config)
    }

    suspend fun <T> dbQuery(
        block: suspend () -> T
    ): T =
        newSuspendedTransaction { block() }

}