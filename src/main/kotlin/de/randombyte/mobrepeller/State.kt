package de.randombyte.mobrepeller

import com.flowpowered.math.vector.Vector3i
import de.randombyte.mobrepeller.State.RepellerRegistrationResult.*
import org.slf4j.Logger
import org.spongepowered.api.world.Location
import org.spongepowered.api.world.World
import java.util.*
import kotlin.system.measureTimeMillis

/**
 * Singleton for storing the current state of the plugin(data, ...) and doing centralized things
 */
object State {
    //centerBlockLocation, <radius, blocksRepresentingCross>
    var repellers: MutableMap<Location<World>, Pair<Int, List<Vector3i>>> = HashMap() //Todo: SQL

    enum class RepellerRegistrationResult {
        CREATED, DUPLICATE, UPDATED, NO_REPELLER, REMOVED
    }

    fun tryRegisteringRepeller(centerBlock: Location<World>): RepellerRegistrationResult {
        val crossBlocks = CrossShapeChecker.getCrossBlocks(centerBlock)
        val currentRadius = CrossShapeChecker.calculateRadius(centerBlock.extent, crossBlocks)
        //Ignore repellers with 0 currentRadius
        if (currentRadius == 0) {
            return if (repellers.remove(centerBlock) != null) REMOVED else NO_REPELLER
        }

        val alreadyRegisteredRepeller = repellers[centerBlock.toInt()]
        val result = if (alreadyRegisteredRepeller != null) {
            if (alreadyRegisteredRepeller.first == currentRadius) DUPLICATE else UPDATED
        } else CREATED
        repellers[centerBlock.toInt()] = Pair(currentRadius, crossBlocks)
        return result
    }

    fun Location<World>.toInt(): Location<World> = Location(extent, blockPosition)
}