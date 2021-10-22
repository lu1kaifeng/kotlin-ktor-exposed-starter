package model

import org.jetbrains.exposed.sql.Table

interface Model<DTO>{
    fun toDto() :DTO
}

interface Dto<Model>{
    fun toModel() : Model
}

open class TableWithId : Table(){
    val id = long("id").autoIncrement()
    override val primaryKey = PrimaryKey(id)
}