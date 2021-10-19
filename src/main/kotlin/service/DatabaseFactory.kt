package service

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.application.*
import model.Subjects
import model.Widgets
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.slf4j.LoggerFactory
import javax.sql.DataSource

class DatabaseFactory(private val app : Application) {

    private val log = LoggerFactory.getLogger(this::class.java)

    init {
        log.info("Initialising database")
        val pool = hikari()
        Database.connect(pool)
        SchemaUtils.createMissingTablesAndColumns(Subjects,Widgets)
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

    private fun runFlyway(datasource: DataSource) {
        val flyway = Flyway.configure()
            .dataSource(datasource)
            .load()
        try {
            flyway.info()
            flyway.migrate()
        } catch (e: Exception) {
            log.error("Exception running flyway migration", e)
            throw e
        }
        log.info("Flyway migration has finished")
    }

    suspend fun <T> dbQuery(
        block: suspend () -> T
    ): T =
        newSuspendedTransaction { block() }

}