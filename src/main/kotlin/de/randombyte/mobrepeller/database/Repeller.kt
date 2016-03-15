package de.randombyte.mobrepeller.database

import de.randombyte.mobrepeller.State
import org.jetbrains.exposed.sql.Query
import org.spongepowered.api.Sponge
import org.spongepowered.api.world.Location
import org.spongepowered.api.world.World
import java.util.*

/**
 * Internal representation in database
 */
data class Repeller(val id: Int, val radius: Int) {
    companion object {

        fun fromQuery(query: Query): MutableMap<Location<World>, Repeller> {
            val map = mutableMapOf<Location<World>, Repeller>()
            for (row in query) {
                val worldUuid = row[Repellers.worldUUID]
                val worldOpt = Sponge.getServer().getWorld(UUID.fromString(worldUuid))
                if (!worldOpt.isPresent) {
                    State.logger?.warn("Removing MobRepellers of world with UUID $worldUuid because that world isn't loaded!")
                    //todo: delete
                    continue
                }
                map[Location<World>(worldOpt.get(), row[Repellers.x], row[Repellers.y], row[Repellers.z])] =
                        Repeller(row[Repellers.id], row[Repellers.radius])
            }
            return map
        }
    }

}