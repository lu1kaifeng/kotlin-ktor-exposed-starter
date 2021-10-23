package app.model

import app.orm.Dto
import app.orm.Model
import app.orm.TableWithId
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Table



object Subjects : TableWithId(){
    val username = varchar("name", 255)
    val password = varchar("password", 255)
}

@Serializable
data class Subject(
    val id : Long?,
    val username :String,
    val password :String
) : Model<SubjectDto> {
    override fun toDto() = SubjectDto(id,username)
}

@Serializable
data class SubjectDto (
    val id : Long?,
    val username :String,
    val password: String?
): Dto<Subject> {
    constructor(id: Long?,username: String) :this(id,username,null)
    constructor(username: String,password: String) :this(null,username,password)
    override fun toModel() = password?.let { Subject(id,username, it) }?:throw NullPointerException()
}