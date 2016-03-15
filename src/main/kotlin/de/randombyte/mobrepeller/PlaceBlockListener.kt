package de.randombyte.mobrepeller

import de.randombyte.mobrepeller.State.RepellerRegistrationResult.CREATED
import de.randombyte.mobrepeller.State.RepellerRegistrationResult.UPDATED
import de.randombyte.mobrepeller.commands.PlayerCommunicator.success
import de.randombyte.mobrepeller.commands.PlayerCommunicator.warn
import org.spongepowered.api.entity.living.player.Player
import org.spongepowered.api.event.EventListener
import org.spongepowered.api.event.block.ChangeBlockEvent

/**
 * Exported in a single class to be able to dynamically (de)register the listener when config reloaded.
 */
class PlaceBlockListener : EventListener<ChangeBlockEvent.Place> {

    override fun handle(event: ChangeBlockEvent.Place?) {
        if (event == null) return

        val playerOpt = event.cause.first(Player::class.java)
        val player = if (playerOpt.isPresent) playerOpt.get() else null
        event.transactions.filter { it.final.location.isPresent
                && CrossShapeChecker.blockTypes.containsKey(it.final.state.type) }.forEach { transaction ->

            //Update all repellers; this isn't that bad for performance because it only checks this if the placed block
            //is the blockType of defined blockTypes(those blocks aren't common)
            State.repellers.forEach { repeller ->
                if (State.tryRegisteringRepeller(repeller.key) == UPDATED) {
                    player?.warn("Updated radius of MobRepeller to ${State.repellers[repeller.key]!!.radius}!")
                    return
                }
            }

            //Else try registering new repeller with centerBlock or 4 blocks around it
            val placedBlock = transaction.final.location.get()
            val blocksToTry = CrossShapeChecker.directions.map { placedBlock.getRelative(it) } + placedBlock
            blocksToTry.forEach {
                val r = State.tryRegisteringRepeller(it)
                if (r == CREATED) {
                    val a = State.repellers[it]
                    player?.success("Created MobRepeller with radius of ${a!!.radius}!")
                    return
                }
            }
        }
    }
}