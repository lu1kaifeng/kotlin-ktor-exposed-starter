package model

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Table



object Subjects :TableWithId(){
    val username = varchar("name", 255)
    val password = varchar("password", 255)
}

@Serializable
data class Subject(
    val id : Long?,
    val username :String,
    val password :String
) : Model<SubjectDto>{
    override fun toDto() = SubjectDto(id,username)
}

@Serializable
data class SubjectDto (
    val id : Long?,
    val username :String,
)