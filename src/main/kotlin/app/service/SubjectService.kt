package app.service

import app.model.Subject
import app.model.SubjectDto
import app.model.Subjects

import org.jetbrains.exposed.sql.*

class SubjectService(dbFactory: DatabaseFactory) : CrudService<Subjects, SubjectDto, Subject>(Subjects, { row ->
    Subject(
        id = row[Subjects.id],
        username = row[Subjects.username],
        password = row[Subjects.password]
    )
}, { it, user ->
    it[username] = user.username
    it[password] = user.password
}, dbFactory) {

    suspend fun getSubjectByNameAndPasswordOrNull(userName: String, password: String): SubjectDto? = dbFactory.dbQuery {
        Subjects.select {
            ((Subjects.username eq userName) and (Subjects.password eq password))
        }.mapNotNull { toSubject(it).toDto() }
            .singleOrNull()
    }

    private fun toSubject(row: ResultRow): Subject =
        Subject(
            id = row[Subjects.id],
            username = row[Subjects.username],
            password = row[Subjects.password]
        )
}