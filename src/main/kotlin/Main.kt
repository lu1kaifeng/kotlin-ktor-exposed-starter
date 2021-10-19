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
import service.DatabaseFactory
import service.WidgetService
import util.JsonMapper
import web.User
import web.auth
import web.index
import web.widget

@ExperimentalCoroutinesApi
fun Application.module() {
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
        index()
        widget()
        auth()
    }

}

fun main(args: Array<String>) {
    embeddedServer(Netty, commandLineEnvironment(args)).start(wait = true)
}