package app
import io.ktor.application.*
import org.koin.core.module.Module
import org.koin.dsl.module
import app.service.DatabaseFactory
import app.service.SubjectService
import app.service.WidgetService

val depModule: (app: Application) -> Module = { app ->
    module {
        single {
            DatabaseFactory(app)

        }
        single {
            SubjectService(get())

        }
        single { WidgetService(get()) }

    }
}