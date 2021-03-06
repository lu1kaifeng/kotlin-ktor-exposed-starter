package app.test.common

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.application.*
import io.ktor.config.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.restassured.RestAssured
import io.restassured.http.ContentType
import io.restassured.response.ResponseBodyExtractionOptions
import io.restassured.specification.RequestSpecification
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import app.model.Subjects
import app.module
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.koin.ktor.ext.inject
import app.util.JsonMapper.defaultMapper
import java.util.concurrent.TimeUnit
abstract class ServerTest {

    protected inline fun <reified T> ResponseBodyExtractionOptions.to(): T {
        return defaultMapper.decodeFromString(this.asString())
    }

    protected inline fun <reified T> RequestSpecification.bodyJson(obj: T): RequestSpecification {
        this.contentType(ContentType.JSON)
        return this.body(defaultMapper.encodeToString(obj))
    }

    companion object {

        private var serverStarted = false

        @JvmStatic
        protected lateinit var server: ApplicationEngine

        @ExperimentalCoroutinesApi
        @BeforeAll
        @JvmStatic
        fun startServer() {
            if (!serverStarted) {
                server = embeddedServer(Netty, 8081, module = Application::module)
                (server.environment.config as MapApplicationConfig).apply {
                    put("jwt.upload.dir", "uploads")
                    put("jwt.secret" ,"secret")
                    put("jwt.issuer","http://0.0.0.0:8080/")
                    put("jwt.audience" ,"http://0.0.0.0:8080/hello")
                    put("jwt.realm","Access to 'hello'")
                    put("db.driverClassName","org.h2.Driver")
                    put("db.jdbcUrl","jdbc:h2:mem:test")
                    put("db.maximumPoolSize","3")
                    put("db.isAutoCommit","false")
                    put("db.transactionIsolation","TRANSACTION_REPEATABLE_READ")
                }
                server.start()
                serverStarted = true

                RestAssured.baseURI = "http://localhost"
                RestAssured.port = 8081
                Runtime.getRuntime().addShutdownHook(Thread { server.stop(0, 0, TimeUnit.SECONDS) })
            }
        }
    }

    protected inline fun <reified T : Any> serverInject() =
        server.application.inject<T>()
    @BeforeEach
    fun before() = runBlocking {
        val appConfig = server.application.environment.config
        Database.connect(
            HikariDataSource(HikariConfig().apply {
                driverClassName = appConfig.property("db.driverClassName").getString()
                jdbcUrl = appConfig.property("db.jdbcUrl").getString()
                maximumPoolSize = appConfig.property("db.maximumPoolSize").getString().toInt()
                isAutoCommit = appConfig.property("db.isAutoCommit").getString().toBoolean()
                transactionIsolation = appConfig.property("db.transactionIsolation").getString()
                validate()
            }))
        newSuspendedTransaction {
            SchemaUtils.create(Subjects)
            Subjects.insert {
                it[username] = "app/test"
                it[password] = "app/test"
            }
            Unit
        }
    }
}
