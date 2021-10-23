package app
import app.orm.TableWithId
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.auth.jwt.*
import io.ktor.features.*
import io.ktor.locations.*
import io.ktor.routing.*
import io.ktor.serialization.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.websocket.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.koin.ktor.ext.Koin
import org.koin.logger.slf4jLogger
import org.slf4j.event.Level
import app.spa.SinglePageApplication
import app.util.JsonMapper
import app.web.User
import app.web.auth
import app.web.widget
import org.jetbrains.exposed.sql.Table
import org.reflections.Reflections

@ExperimentalCoroutinesApi
fun Application.module() {
    val reflections = Reflections(this.environment.classLoader.name)

    install(DefaultHeaders)
    install(Locations)
    install(CallLogging){
        level = Level.TRACE
    }
    install(WebSockets)

    install(ContentNegotiation) {
        json(JsonMapper.defaultMapper)
    }
    install(Koin) {
        slf4jLogger()
        modules(depModule(this@module))
    }

    val secret = environment.config.property("jwt.secret").getString()
    val issuer = environment.config.property("jwt.issuer").getString()
    val audience = environment.config.property("jwt.audience").getString()
    val myRealm = environment.config.property("jwt.realm").getString()
    install(Authentication) {
        jwt {
            //realm = myRealm
            verifier(
                JWT
                    .require(Algorithm.HMAC256(secret))
                    .withAudience(audience)
                    .withIssuer(issuer)
                    .build()
            )
            validate {credential ->
                if (credential.payload.getClaim("username").asString() != "") {
                    credential.payload.claims
                    User(1)
                } else {
                    null
                }
            }
        }
    }


    install(Routing) {
        widget()
        auth()
    }
    install(SinglePageApplication){
        defaultPage = "index.html"
        folderPath = "static"
    }
}


fun main(args: Array<String>) {
    embeddedServer(Netty, commandLineEnvironment(args)).start(wait = true)
}