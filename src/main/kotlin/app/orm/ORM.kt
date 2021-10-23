package app.orm

import app.model.DatabaseProvider
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.statements.InsertStatement
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import org.reflections.Reflections
import org.slf4j.LoggerFactory
import javax.sql.DataSource
import kotlin.reflect.full.findAnnotation

interface Model<DTO> {
    fun toDto(): DTO
}

interface Dto<Model> {
    fun toModel(): Model
}

abstract class TableWithId : Table() {
    val id = long("id").autoIncrement()
    override val primaryKey = PrimaryKey(id)
}

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class Ignore

abstract class AbstractDatabaseProvider(dataSource: DataSource) {

    private val log = LoggerFactory.getLogger(this::class.java)

    init {
        log.info("Initialising database")
        Database.connect(dataSource)
        val tables: List<TableWithId> =
            Reflections(this.javaClass.packageName).getSubTypesOf(TableWithId::class.java).stream().toList()
                .mapNotNull {
                    if (it.kotlin.findAnnotation<Ignore>() == null) {
                        log.info("Detecting table : ${it.name}")
                        it.kotlin.objectInstance
                    } else {
                        log.info("Detecting and Ignoring table : ${it.name}")
                        null
                    }
                }
        transaction {
            SchemaUtils.createMissingTablesAndColumns(*tables.toTypedArray())
        }
    }
    suspend operator fun <T> invoke(
        block: suspend Transaction.() -> T
    ): T =
        newSuspendedTransaction { block() }


}

abstract class CrudService<T : TableWithId, DTO : Dto<M>, M : Model<DTO>>(
    private val table: T,
    private val rowToModel: (ResultRow) -> M,
    private val modelToInsertStmt: T.(InsertStatement<Number>, M) -> Unit,
    protected val dbProvider: DatabaseProvider
) {

    suspend fun getAll(): List<M> = dbProvider{
        table.selectAll().map { rowToModel(it) }
    }

    suspend fun getById(id: Long): M? = dbProvider{
        table.select {
            (table.id eq id)
        }.mapNotNull { rowToModel(it) }
            .singleOrNull()
    }

    suspend fun add(model: M): M {
        var key = 0L
        dbProvider{
            key = (table.insert { table.modelToInsertStmt(it, model) } get table.id)
        }
        return getById(key)!!
    }

    suspend fun delete(id: Long): Boolean {
        return dbProvider{
            table.deleteWhere { table.id eq id } > 0
        }
    }
}