package app.orm

import app.model.DatabaseProvider
import app.model.Subjects
import app.model.Widgets
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.application.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.statements.InsertStatement
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import org.reflections.Reflections
import org.slf4j.LoggerFactory
import java.util.stream.Collector
import java.util.stream.Collectors
import javax.sql.DataSource
import kotlin.reflect.full.findAnnotation

interface Model<DTO>{
    fun toDto() :DTO
}

interface Dto<Model>{
    fun toModel() : Model
}

abstract class TableWithId : Table(){
    val id = long("id").autoIncrement()
    override val primaryKey = PrimaryKey(id)
}

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class Ignore()

abstract class AbstractDatabaseProvider(dataSource: DataSource) {

    private val log = LoggerFactory.getLogger(this::class.java)

    init {
        log.info("Initialising database")
        Database.connect(dataSource)
        val tables : List<TableWithId> = Reflections(this.javaClass.packageName).getSubTypesOf(TableWithId::class.java).stream().toList().mapNotNull {
            if(it.kotlin.findAnnotation<Ignore>() == null){
                log.info("Detecting table : ${it.name}")
                it.kotlin.objectInstance
            }else{
                log.info("Detecting and Ignoring table : ${it.name}")
                null
            }
        }
        transaction{
            SchemaUtils.createMissingTablesAndColumns(*tables.toTypedArray())
        }
    }

    suspend fun <T> dbQuery(
        block: suspend () -> T
    ): T =
        newSuspendedTransaction { block() }

}

abstract class CrudService<T : TableWithId,DTO: Dto<M>,M: Model<DTO>>(private val table: T, private val rowToModel : (ResultRow)->M, private val modelToInsertStmt:T.(InsertStatement<Number>, M) -> Unit, protected val dbFactory: DatabaseProvider) {
    suspend fun getAll(): List<M> = dbFactory.dbQuery {
        table.selectAll().map { rowToModel(it) }
    }

    suspend fun getById(id: Long): M? = dbFactory.dbQuery {
        table.select {
            (table.id eq id)
        }.mapNotNull { rowToModel(it) }
            .singleOrNull()
    }

    suspend fun add(model : M): M {
        var key = 0L
        dbFactory.dbQuery {
            key = (table.insert { table.modelToInsertStmt(it,model) } get table.id)
        }
        return getById(key)!!
    }

    suspend fun delete(id: Long): Boolean {
        return dbFactory.dbQuery {
            table.deleteWhere { table.id eq id } > 0
        }
    }
}