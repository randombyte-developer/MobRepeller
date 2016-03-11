package de.randombyte.mobrepeller

import com.flowpowered.math.vector.Vector3i
import org.spongepowered.api.block.BlockTypes.*
import org.spongepowered.api.util.Direction
import org.spongepowered.api.world.Location
import org.spongepowered.api.world.World
import org.spongepowered.api.world.extent.Extent

object CrossShapeChecker {

    val directions = listOf(Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST)
    //BlockTypes mapped to the value they increase the radius by
    val blockTypes = mapOf(IRON_BLOCK to 2, LAPIS_BLOCK to 2, GOLD_BLOCK to 4, DIAMOND_BLOCK to 6)

    fun calculateRadius(extent: Extent, blocks: List<Vector3i>): Int {
        //A cross has at least 5 blocks
        return if (blocks.size < 5) 0 else blocks.sumBy { blockTypes[extent.getBlock(it).type] ?: 0 }
    }

    /**
     * @return All blocks belonging to the whole cross.
     */
    fun getCrossBlocks(centerBlock: Location<World>): List<Vector3i> {
        val centerPos = if (blockTypes.containsKey(centerBlock.blockType)) centerBlock.blockPosition else return emptyList()
        return directions.map { direction ->
            val armBlocks = getCrossArmBlocks(centerBlock, direction)
            if (armBlocks.size < 1) return emptyList() //Every arm has to have at least one block in arm
            return@map armBlocks
        }.flatMap { it } + centerPos
    }

    /**
     * @return Blocks that belong to a cross arm.
     */
    fun getCrossArmBlocks(centerBlock: Location<World>, direction: Direction): List<Vector3i> {
        val blockToCheck = centerBlock.getRelative(direction)
        return if (blockTypes.containsKey(blockToCheck.blockType)) {
            getCrossArmBlocks(blockToCheck, direction).plus(blockToCheck.blockPosition)
        } else emptyList()
    }
}