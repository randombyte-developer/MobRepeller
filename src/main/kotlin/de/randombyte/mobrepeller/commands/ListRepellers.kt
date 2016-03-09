package de.randombyte.mobrepeller.commands

import de.randombyte.mobrepeller.MobRepeller
import de.randombyte.mobrepeller.commands.PlayerCommunicator.fail
import org.spongepowered.api.Sponge
import org.spongepowered.api.command.CommandResult
import org.spongepowered.api.command.CommandSource
import org.spongepowered.api.command.args.CommandContext
import org.spongepowered.api.command.spec.CommandExecutor
import org.spongepowered.api.entity.living.player.Player
import org.spongepowered.api.service.pagination.PaginationService
import org.spongepowered.api.text.Text
import org.spongepowered.api.text.action.TextActions

class ListRepellers : CommandExecutor {
    override fun execute(src: CommandSource?, ctx: CommandContext?): CommandResult? {
        when (src) {
            null -> return CommandResult.empty()
            !is Player -> return src.fail("Command must be executed by a player!")
        }

        val repellerTexts = MobRepeller.repellers.map { repeller ->
            Text.of("${repeller.key.position.toString()} with radius ${repeller.value}",
                    TextActions.executeCallback { src ->
                        src.sendMessage(Text.of("Click on repeller at ${repeller.key.position.toInt()}"))
                    })
        }

        val paginationServcie = Sponge.getServiceManager().provide(PaginationService::class.java).get()
        paginationServcie.builder()
            .header(Text.of("Registered MobRepellers in this world"))
            .contents(repellerTexts)
            .sendTo(src)

        return CommandResult.success()
    }
}