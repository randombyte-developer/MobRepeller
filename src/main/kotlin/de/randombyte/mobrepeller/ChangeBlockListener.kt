package de.randombyte.mobrepeller

import de.randombyte.mobrepeller.State.RepellerRegistrationResult.*
import de.randombyte.mobrepeller.commands.PlayerCommunicator.error
import de.randombyte.mobrepeller.commands.PlayerCommunicator.success
import de.randombyte.mobrepeller.commands.PlayerCommunicator.warn
import org.spongepowered.api.entity.living.player.Player
import org.spongepowered.api.event.Listener
import org.spongepowered.api.event.block.ChangeBlockEvent

class ChangeBlockListener {

    @Listener
    fun onPlaceBlock(event: ChangeBlockEvent.Place) {
        val playerOpt = event.cause.first(Player::class.java)
        val player = if (playerOpt.isPresent) playerOpt.get() else null
        event.transactions.filter { it.final.location.isPresent
                && CrossShapeChecker.blockTypes.containsKey(it.final.state.type) }.forEach { transaction ->

            // Update all repellers; this isn't that bad for performance because it only checks this if the placed block
            // is the blockType of defined blockTypes(those blocks aren't common)
            State.repellers.forEach { repeller ->
                if (State.tryRegisteringRepeller(repeller.key) == UPDATED) {
                    player?.warn("Updated radius of MobRepeller to ${State.repellers[repeller.key]!!.radius}!")
                    return
                }
            }

            // Else try registering new repeller with centerBlock or 4 blocks around it
            val placedBlock = transaction.final.location.get()
            val blocksToTry = CrossShapeChecker.directions.map { placedBlock.getRelative(it) } + placedBlock
            blocksToTry.forEach {
                val r = State.tryRegisteringRepeller(it)
                if (r == CREATED) {
                    player?.success("Created MobRepeller with radius of ${State.repellers[it]!!.radius}!")
                    return
                }
            }
        }
    }

    @Listener
    fun onBreakBlock(event: ChangeBlockEvent.Break) {
        event.transactions.forEach {
            val removedBlockType = it.original.state.type
            val removedBlock = it.original.location.get()
            if (!CrossShapeChecker.blockTypes.containsKey(removedBlockType)) return@forEach
            State.repellers.filter { it.key.inExtent(removedBlock.extent) }.forEach { repeller ->
                // try reregistering every repeller in same world as removedBlock
                val result = State.tryRegisteringRepeller(repeller.key)
                // notify user
                val playerCauseOpt = event.cause.first(Player::class.java)
                if (playerCauseOpt.isPresent) {
                    val player = playerCauseOpt.get()
                    val registeredRepeller = State.repellers[repeller.key]
                    when (result) {
                        UPDATED -> player.warn("Updated radius of MobRepeller to ${registeredRepeller!!.radius}!")
                        REMOVED -> player.warn("Removed MobRepeller!")
                        NO_REPELLER -> player.error("Undefined state: Tried removing repeller because repeller block " +
                                "was broken but that repeller isn't registered!")
                        CREATED -> player.warn("Undefined state: Created repeller by removing block!")
                    }
                }
            }
        }
    }
}