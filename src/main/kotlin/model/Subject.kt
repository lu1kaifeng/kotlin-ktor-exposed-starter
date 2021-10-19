package model

import kotlinx.serialization.Serializable
import model.Widgets.autoIncrement
import org.jetbrains.exposed.sql.Table

object Subjects :Table(){
    val id = long("id").autoIncrement()
    val username = varchar("name", 255)
    val password = varchar("name", 255)
    override val primaryKey = PrimaryKey(id)
}

@Serializable
data class Subject (
    val id : Long?,
    val username :String,
    val password :String
        ){
    fun toUser() = User(id,username)
}

@Serializable
data class User (
    val id : Long?,
    val username :String,
)

@Serializable
data class NewUser(
    val username :String,
    val password :String
){
    fun toSubject()  = Subject(null,username, password)
}