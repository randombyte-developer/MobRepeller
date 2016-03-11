package de.randombyte.mobrepeller

import de.randombyte.mobrepeller.State.RepellerRegistrationResult.*
import de.randombyte.mobrepeller.commands.PlayerCommunicator.warn
import de.randombyte.mobrepeller.commands.PlayerCommunicator.error
import org.spongepowered.api.entity.living.player.Player
import org.spongepowered.api.event.EventListener
import org.spongepowered.api.event.block.ChangeBlockEvent

class BreakBlockListener : EventListener<ChangeBlockEvent.Break> {

    override fun handle(event: ChangeBlockEvent.Break?) {
        if (event == null) return

        event.transactions.filter {it.final.location.isPresent }.forEach {
            val removedBlock = it.final.location.get()
            State.repellers.filter { it.key.inExtent(removedBlock.extent) }.forEach { repeller ->
                val centerBlock = repeller.key
                if (repeller.value.second.any { it.equals(removedBlock.blockPosition) }) {
                    //removedBlock belongs to repeller -> try reregistering
                    val result = State.tryRegisteringRepeller(repeller.key)
                    //notify user
                    val playerCauseOpt = event.cause.first(Player::class.java)
                    if (playerCauseOpt.isPresent) {
                        val player = playerCauseOpt.get()
                        val registeredRepeller = State.repellers[centerBlock]
                        when (result) {
                            UPDATED -> player.warn("Updated radius of MobRepeller to ${registeredRepeller!!.first}!")
                            DUPLICATE -> player.warn("Already registered with radius ${registeredRepeller!!.first}")
                            NO_REPELLER -> player.error("Undefined state: Tried removing repeller because repeller block " +
                                    "was broken but that repeller isn't registered!")
                            REMOVED -> player.warn("Removed MobRepeller!")
                            else -> player.warn("Undefined state!; centerBlockPos ${centerBlock.blockPosition}")
                        }
                    }
                }
            }
        }
    }
}