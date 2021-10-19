package db.migration

import model.Subjects
import model.Widgets
import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction

class V1__create_widgets: BaseJavaMigration() {
    override fun migrate(context: Context?) {
        transaction {
            SchemaUtils.create(Widgets)
            SchemaUtils.create(Subjects)
            Subjects.insert {
                it[username] = "test"
                it[password] = "test"
            }
        }
    }
}
