package de.randombyte.mobrepeller.commands

import de.randombyte.mobrepeller.CrossShapeChecker
import de.randombyte.mobrepeller.MobRepeller
import de.randombyte.mobrepeller.commands.PlayerCommunicator.fail
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

        val groundBlock = player.location.getRelative(Direction.DOWN)
        val repellerRadius = CrossShapeChecker.checkCross(groundBlock)
        //Ignore repeller with 0 radius
        if (repellerRadius == 0) return src.fail("Not standing on a repeller!")
        MobRepeller.repellers[groundBlock] = repellerRadius
        src.success("Added cross with value $repellerRadius at ${groundBlock.position.toInt()}")

        return CommandResult.success()
    }
}