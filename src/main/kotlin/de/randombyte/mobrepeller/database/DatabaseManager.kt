package de.randombyte.mobrepeller.database

import org.jetbrains.exposed.sql.*
import org.spongepowered.api.service.sql.SqlService
import org.spongepowered.api.world.Location
import org.spongepowered.api.world.World

object DatabaseManager {

    val sqlService: SqlService by SqlServiceDelegate() //lazyinit

    fun getDataSource() = sqlService.getDataSource("jdbc:h2:./MobRepeller.db")

    fun getAllRepellers() = Database.connect(getDataSource()).transaction { Repeller.fromQuery(Repellers.selectAll()) }

    fun updateRepellerRadius(id: Int, newRadius: Int) {
        Database.connect(getDataSource()).transaction {
            Repellers.update({ Repellers.id eq id }) { it[Repellers.radius] = newRadius }
        }
    }

    fun createRepeller(centerBlock: Location<World>, radius: Int) {
        Database.connect(getDataSource()).transaction {
            Repellers.insert {
                it[Repellers.worldUUID] = centerBlock.extent.uniqueId.toString()
                it[Repellers.x] = centerBlock.blockX
                it[Repellers.y] = centerBlock.blockY
                it[Repellers.z] = centerBlock.blockZ
                it[Repellers.radius] = radius
            }
        }
    }

    fun removeRepeller(id: Int) = Database.connect(getDataSource()).transaction { Repellers.deleteWhere { Repellers.id eq id } }
}
