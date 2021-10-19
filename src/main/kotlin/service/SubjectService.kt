package service

import model.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

class SubjectService(private val dbFactory: DatabaseFactory) {
    suspend fun getAllSubjects(): List<Subject> = dbFactory.dbQuery {
        Subjects.selectAll().map { toSubject(it) }
    }

    suspend fun getSubject(id: Long): Subject? = dbFactory.dbQuery {
        Subjects.select {
            (Subjects.id eq id)
        }.mapNotNull { toSubject(it) }
            .singleOrNull()
    }

    suspend fun getSubjectByNameAndPasswordOrNull(userName :String,password: String): User? = dbFactory.dbQuery {
        Subjects.select {
            ((Subjects.username eq userName) and (Subjects.password eq password))
        }.mapNotNull { toSubject(it).toUser() }
            .singleOrNull()
    }

    suspend fun addSubject(user :NewUser): Subject {
        var key = 0L
        dbFactory.dbQuery {
            key = (Subjects.insert {
                it[username] = user.username
                it[password] = user.password
            } get Subjects.id)
        }
        return getSubject(key)!!
    }

    suspend fun deleteWidget(id: Long): Boolean {
        return dbFactory.dbQuery {
            Subjects.deleteWhere { Subjects.id eq id } > 0
        }
    }
    private fun toSubject(row: ResultRow): Subject =
        Subject(
            id = row[Subjects.id],
            username = row[Subjects.username],
            password = row[Subjects.password]
        )
}