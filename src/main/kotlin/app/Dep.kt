package app
import io.ktor.application.*
import org.koin.core.module.Module
import org.koin.dsl.module
import app.model.DatabaseProvider
import app.service.SubjectService
import app.service.WidgetService

val depModule: (app: Application) -> Module = { app ->
    module {
        single {
            DatabaseProvider(app)

        }
        single {
            SubjectService(get())

        }
        single { WidgetService(get()) }

    }
}