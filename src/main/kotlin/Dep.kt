import io.ktor.application.*
import org.koin.core.module.Module
import org.koin.dsl.module
import service.DatabaseFactory
import service.SubjectService
import service.WidgetService

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