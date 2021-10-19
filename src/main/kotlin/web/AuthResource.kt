package web

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.locations.*
import io.ktor.locations.post
import io.ktor.response.*
import io.ktor.routing.*
import kotlinx.serialization.Serializable
import org.koin.ktor.ext.inject
import service.SubjectService
import java.util.*

@Serializable
data class User(
    val sub: Long
) : Principal

@Location("/login")
data class Login(val username: String, val password: String)

@KtorExperimentalLocationsAPI
fun Route.auth() {

    val subjectService by inject<SubjectService>()

    val secret = application.environment.config.property("jwt.secret").getString()
    val issuer = application.environment.config.property("jwt.issuer").getString()
    val audience = application.environment.config.property("jwt.audience").getString()
    val myRealm = application.environment.config.property("jwt.realm").getString()

    post<Login> { login ->
        // val user = call.receive<User>()
        // Check username and password
        // ...
        val subject = subjectService.getSubjectByNameAndPasswordOrNull(login.username, login.password) ?: run {
            call.respond(HttpStatusCode.Forbidden)
            return@post
        }
        val token = JWT.create()
            .withAudience(audience)
            .withIssuer(issuer)
            .withSubject(subject.id.toString())
            .withExpiresAt(Date(System.currentTimeMillis() + 60000))
            .sign(Algorithm.HMAC256(secret))
        call.respond(hashMapOf("token" to token))
    }
}