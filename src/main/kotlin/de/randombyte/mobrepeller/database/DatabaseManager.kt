package de.randombyte.mobrepeller.database

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.spongepowered.api.service.sql.SqlService
import org.spongepowered.api.world.Location
import org.spongepowered.api.world.World

object DatabaseManager {

    var databasePath = "./MobRepeller"

    val sqlService: SqlService by SqlServiceDelegate() //lazyinit

    fun getDataSource() = sqlService.getDataSource("jdbc:h2:$databasePath")

    fun getAllRepellers(): Map<Location<World>, Repeller> {
        Database.connect(getDataSource())
        return transaction {
            if (!Repellers.exists()) SchemaUtils.create(Repellers)
            Repeller.fromQuery(Repellers.selectAll())
        }
    }

    fun updateRepellerRadius(id: Int, newRadius: Int) {
        Database.connect(getDataSource())
        transaction {
            Repellers.update(where = { Repellers.id eq id }) { it[Repellers.radius] = newRadius }
        }
    }

    fun createRepeller(centerBlock: Location<World>, radius: Int) {
        Database.connect(getDataSource())
        transaction {
            Repellers.insert {
                it[Repellers.worldUUID] = centerBlock.extent.uniqueId.toString()
                it[Repellers.x] = centerBlock.blockX
                it[Repellers.y] = centerBlock.blockY
                it[Repellers.z] = centerBlock.blockZ
                it[Repellers.radius] = radius
            }
        }
    }

    fun removeRepeller(id: Int) {
        Database.connect(getDataSource())
        transaction { Repellers.deleteWhere { Repellers.id eq id } }
    }
}
