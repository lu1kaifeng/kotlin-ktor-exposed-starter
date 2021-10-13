import org.koin.dsl.module
import service.WidgetService

val depModule = module {
    single {
        WidgetService()
    }

}