package de.randombyte.mobrepeller

import de.randombyte.mobrepeller.State.RepellerRegistrationResult.*
import de.randombyte.mobrepeller.commands.PlayerCommunicator.error
import de.randombyte.mobrepeller.commands.PlayerCommunicator.warn
import org.spongepowered.api.entity.living.player.Player
import org.spongepowered.api.event.EventListener
import org.spongepowered.api.event.block.ChangeBlockEvent

class BreakBlockListener : EventListener<ChangeBlockEvent.Break> {

    override fun handle(event: ChangeBlockEvent.Break?) {
        if (event == null) return

        event.transactions.forEach {
            val removedBlockType = it.original.state.type
            val removedBlock = it.original.location.get()
            if (!CrossShapeChecker.blockTypes.containsKey(removedBlockType)) return@forEach
            State.repellers.filter { it.key.inExtent(removedBlock.extent) }.forEach { repeller ->
                //try reregistering every repeller in same world as removedBlock
                val result = State.tryRegisteringRepeller(repeller.key)
                //notify user
                val playerCauseOpt = event.cause.first(Player::class.java)
                if (playerCauseOpt.isPresent) {
                    val player = playerCauseOpt.get()
                    val registeredRepeller = State.repellers[repeller.key]
                    when (result) {
                        UPDATED -> player.warn("Updated radius of MobRepeller to ${registeredRepeller!!.radius}!")
                        DUPLICATE -> player.warn("MobRepeller already registered with radius ${registeredRepeller!!.radius}!")
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