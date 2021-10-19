import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.routing.*
import io.ktor.util.pipeline.*

class NotAuthenticatedException: IllegalStateException("Authentication must took place before Authorization")

inline fun <reified T : Principal> Route.authorize(crossinline authorizer : suspend ( ApplicationCall, T)->Unit, build: Route.() -> Unit): Route {
    val authorizePhase = PipelinePhase("Authorize")
    this.insertPhaseAfter(this.items[4],authorizePhase)
    this.intercept(authorizePhase) {
        val principal : T? = this.call.principal()
        if(principal != null){
            authorizer(call,principal!!)
        }else{
            throw NotAuthenticatedException()
        }
    }
    this.build()
    return this
}