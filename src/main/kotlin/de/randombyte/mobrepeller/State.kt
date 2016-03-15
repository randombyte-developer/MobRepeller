package de.randombyte.mobrepeller

import de.randombyte.mobrepeller.State.RepellerRegistrationResult.*
import de.randombyte.mobrepeller.database.DatabaseManager
import de.randombyte.mobrepeller.database.Repeller
import org.slf4j.Logger
import org.spongepowered.api.world.Location
import org.spongepowered.api.world.World
import java.util.*

/**
 * Singleton for storing the current state of the plugin(data, ...) and doing centralized things(database, ...)
 */
object State {

    var logger: Logger? = null

    //<centerBlockLocation, repellerObject>
    var repellers: MutableMap<Location<World>, Repeller> = HashMap()

    enum class RepellerRegistrationResult {
        CREATED, DUPLICATE, UPDATED, NO_REPELLER, REMOVED
    }

    fun tryRegisteringRepeller(centerBlock: Location<World>): RepellerRegistrationResult {
        val crossBlocks = CrossShapeChecker.getCrossBlocks(centerBlock)
        val currentRadius = CrossShapeChecker.calculateRadius(centerBlock.extent, crossBlocks)

        val alreadyRegisteredRepeller = repellers[centerBlock.toInt()]
        return if (alreadyRegisteredRepeller != null) {
            when (currentRadius) {
                0 -> {
                    DatabaseManager.removeRepeller(alreadyRegisteredRepeller.id)
                    REMOVED
                }
                alreadyRegisteredRepeller.radius -> {
                    DatabaseManager.updateRepellerRadius(alreadyRegisteredRepeller.id, currentRadius)
                    UPDATED
                }
                else -> DUPLICATE
            }
        } else {
            when (currentRadius) {
                0 -> NO_REPELLER
                else -> {
                    DatabaseManager.createRepeller(centerBlock.toInt(), currentRadius)
                    CREATED
                }
            }
        }
    }

    fun Location<World>.toInt(): Location<World> = Location(extent, blockPosition)

}