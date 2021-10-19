import org.koin.dsl.module
import service.DatabaseFactory
import service.WidgetService

val depModule = module {
    single {
        WidgetService(get())
        DatabaseFactory()
    }

}