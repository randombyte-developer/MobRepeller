package de.randombyte.mobrepeller.commands

import de.randombyte.mobrepeller.State
import de.randombyte.mobrepeller.State.toInt
import de.randombyte.mobrepeller.State.RepellerRegistrationResult.*
import de.randombyte.mobrepeller.commands.PlayerCommunicator.fail
import de.randombyte.mobrepeller.commands.PlayerCommunicator.warn
import de.randombyte.mobrepeller.commands.PlayerCommunicator.success
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
            src !is Player -> return src.fail("Command must be executed by a player!")
            !src.isOnGround -> return src.fail("Command must be executed while standing on a block!")
            else -> src
        }

        val groundBlock = player.location.getRelative(Direction.DOWN).toInt()

        return when (State.tryRegisteringRepeller(groundBlock)) {
            CREATED -> {
                src.success("Added MobRepeller with radius of ${State.repellers[groundBlock]} at ${groundBlock.blockPosition}")
            }
            NO_REPELLER -> {
                src.fail("Not standing on a repeller!")
            }
            else -> src.warn("Undefined state!")
        }
    }
}