package service

import model.Model
import model.Subject
import model.SubjectDto
import model.TableWithId
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.statements.InsertStatement

abstract class CrudService<T : TableWithId,DTO,M: Model<DTO>>(private val table: T,private val rowToModel : (ResultRow)->M,private val modelToInsertStmt:T.(InsertStatement<Number>,M) -> Unit,protected val dbFactory: DatabaseFactory) {
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