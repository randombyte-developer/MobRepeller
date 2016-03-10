package de.randombyte.mobrepeller

import com.flowpowered.math.vector.Vector3i
import de.randombyte.mobrepeller.State.RepellerRegistrationResult.*
import org.slf4j.Logger
import org.spongepowered.api.world.Location
import org.spongepowered.api.world.World
import java.util.*

/**
 * Singleton for storing the current state of the plugin(data, ...) and doing centralized things
 */
object State {

    var logger: Logger? = null
    //centerBlockLocation, <radius, blocksRepresentingCross>
    var repellers: MutableMap<Location<World>, Pair<Int, List<Vector3i>>> = HashMap() //Todo: SQL

    enum class RepellerRegistrationResult {
        CREATED, UPDATED, NO_REPELLER
    }

    fun tryRegisteringRepeller(centerBlock: Location<World>): RepellerRegistrationResult {
        val crossBlocks = CrossShapeChecker.getCrossBlocks(centerBlock)
        val radius = CrossShapeChecker.calculateRadius(centerBlock.extent, crossBlocks)
        //Ignore repellers with 0 radius
        if (radius == 0) return NO_REPELLER

        val result = if (repellers.containsKey(centerBlock.toInt())) {
            UPDATED
        } else CREATED
        repellers[centerBlock.toInt()] = Pair(radius, crossBlocks)
        return result
    }

    fun Location<World>.toInt(): Location<World> = Location(extent, blockPosition)
}