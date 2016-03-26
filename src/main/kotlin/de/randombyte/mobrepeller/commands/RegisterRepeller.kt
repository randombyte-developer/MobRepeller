package de.randombyte.mobrepeller.commands

import de.randombyte.mobrepeller.State
import de.randombyte.mobrepeller.State.RepellerRegistrationResult.*
import de.randombyte.mobrepeller.State.toInt
import de.randombyte.mobrepeller.commands.PlayerCommunicator.error
import de.randombyte.mobrepeller.commands.PlayerCommunicator.success
import de.randombyte.mobrepeller.commands.PlayerCommunicator.warn
import org.spongepowered.api.command.CommandResult
import org.spongepowered.api.command.CommandSource
import org.spongepowered.api.command.args.CommandContext
import org.spongepowered.api.command.spec.CommandExecutor
import org.spongepowered.api.entity.living.player.Player
import org.spongepowered.api.util.Direction

class RegisterRepeller : CommandExecutor {
    override fun execute(src: CommandSource?, ctx: CommandContext?): CommandResult? {
        val player: Player = when {
            src == null -> return CommandResult.empty()
            src !is Player -> return src.error("Command must be executed by a player!")
            !src.isOnGround -> return src.error("Command must be executed while standing on a block!")
            else -> src
        }

        val groundBlock = player.location.getRelative(Direction.DOWN).toInt()

        return when (State.tryRegisteringRepeller(groundBlock)) {
            CREATED -> {
                src.success("Created MobRepeller with radius of ${State.repellers[groundBlock]!!.radius} at ${groundBlock.blockPosition}!")
            }
            NO_REPELLER -> src.error("Not standing on a MobRepeller!")
            DUPLICATE -> src.warn("MobRepeller already registered with radius ${State.repellers[groundBlock]!!.radius}!")
            UPDATED -> src.warn("Updated MobRepeller to radius ${State.repellers[groundBlock]!!.radius}!")
            REMOVED -> src.error("Undefined state: Removed MobRepeller by placing a block!")
        }
    }
}