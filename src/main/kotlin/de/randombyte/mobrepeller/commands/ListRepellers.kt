package de.randombyte.mobrepeller.commands

import com.flowpowered.math.vector.Vector3d
import de.randombyte.mobrepeller.State
import de.randombyte.mobrepeller.commands.PlayerCommunicator.error
import de.randombyte.mobrepeller.commands.PlayerCommunicator.warn
import org.spongepowered.api.Sponge
import org.spongepowered.api.command.CommandResult
import org.spongepowered.api.command.CommandSource
import org.spongepowered.api.command.args.CommandContext
import org.spongepowered.api.command.spec.CommandExecutor
import org.spongepowered.api.entity.living.player.Player
import org.spongepowered.api.service.pagination.PaginationService
import org.spongepowered.api.text.Text
import org.spongepowered.api.text.action.TextActions
import org.spongepowered.api.text.format.TextColors.GREEN
import org.spongepowered.api.text.format.TextColors.YELLOW

class ListRepellers : CommandExecutor {
    override fun execute(src: CommandSource?, ctx: CommandContext?): CommandResult? {
        val player = when (src) {
            null -> return CommandResult.empty()
            !is Player -> return src.error("Command must be executed by a player!")
            else -> src
        }

        val repellersInWorld = State.repellers.filter { it.key.inExtent(player.location.extent) }
        if (repellersInWorld.size == 0) return src.warn("No MobRepeller in this world!")

        val repellerTexts = repellersInWorld.map { repeller ->
            Text.builder()
                    .append(Text.of(YELLOW, "Position: ${repeller.key.position.toString()} "))
                    .append(Text.of(GREEN, "Radius: ${repeller.value.radius}"))
                    .onHover(TextActions.showText(getHoverText(repeller.key.position, player.location.position,
                            repeller.value.radius)))
                    .build()
        }

        val paginationServcie = Sponge.getServiceManager().provide(PaginationService::class.java).get()
        paginationServcie.builder()
            .header(Text.of(GREEN, "== ${repellersInWorld.size} MobRepeller(s) in this world: =="))
            .contents(repellerTexts)
            .sendTo(src)

        return CommandResult.success()
    }

    fun getHoverText(repellerPos: Vector3d, playerPos: Vector3d, repellerRadius: Int): Text {
        val blocksToRepeller = repellerPos.distance(playerPos).toInt()
        return if (blocksToRepeller <= repellerRadius) {
            Text.of(GREEN, "In radius; $blocksToRepeller blocks away")
        } else Text.of(GREEN, "In radius; $blocksToRepeller blocks away")
    }
}