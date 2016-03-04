package de.randombyte.mobrepeller

import org.spongepowered.api.block.BlockTypes.*
import org.spongepowered.api.util.Direction
import org.spongepowered.api.world.Location
import org.spongepowered.api.world.World

object CrossShapeChecker {

    private val directions = listOf(Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST)
    //BlockTypes mapped to the value they increase the radius by
    private val blockTypes = mapOf(IRON_BLOCK to 2, LAPIS_BLOCK to 2, GOLD_BLOCK to 4, DIAMOND_BLOCK to 6)

    /**
     * Calculates the radius this cross protects based on many and which blocks are in it.
     * @return The radius this cross protects.
     */
    fun checkCross(location: Location<World>): Int {
        return (blockTypes[location.blockType] ?: return 0) +
                directions.sumBy {
                    val radiusIncrease = checkForward(location, it)
                    //Abort if any direction isn't a valuable block
                    if (radiusIncrease == 0) return 0
                    return@sumBy radiusIncrease
                }
    }

    /**
     * @param location Center block.
     * @return Calculates the radius increase by checking how long and how valuable the blocks are.
     */
    private fun checkForward(location: Location<World>, direction: Direction): Int {
        val locToCheck = location.getRelative(direction)
        return (blockTypes[locToCheck.blockType] ?: return 0) +
                checkForward(locToCheck, direction)
    }
}