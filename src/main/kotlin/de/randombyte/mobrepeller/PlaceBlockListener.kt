package de.randombyte.mobrepeller

import de.randombyte.mobrepeller.State.RepellerRegistrationResult.UPDATED
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
                && CrossShapeChecker.blockTypes.containsKey(it.final.location.get().blockType) }.forEach {
            //Update all repellers; this isn't that bad for performance because it only checks this if the placed block
            //is the blockType of defined blockTypes
            State.repellers.forEach {
                if (State.tryRegisteringRepeller(it.key) == UPDATED) {
                    player?.warn("Updated MobRepeller to radius ${State.repellers[it.key]!!.first}")
                }
            }
        }
    }
}