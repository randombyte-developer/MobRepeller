package de.randombyte.mobrepeller.database

import org.jetbrains.exposed.sql.Table

object Repellers : Table() {
    val id = integer("id").primaryKey().autoIncrement()
    val worldUUID = varchar("world_uuid", 36)
    val x = integer("x")
    val y = integer("y")
    val z = integer("z")
    val radius = integer("radius")
}