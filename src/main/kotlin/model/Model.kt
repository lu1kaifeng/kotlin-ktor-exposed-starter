package model

import org.jetbrains.exposed.sql.Table

interface Model<DTO>{
    fun toDto() :DTO
}

open class TableWithId : Table(){
    val id = long("id").autoIncrement()
    override val primaryKey = PrimaryKey(id)
}