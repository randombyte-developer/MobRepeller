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
import org.spongepowered.api.text.format.TextColors.*

class ListRepellers : CommandExecutor {
    companion object {
        val inRaduis = Text.of(DARK_GREEN, "X")
        val notInRaduis = Text.of(DARK_RED, "X")
        val listHeader = Text.builder()
                .append(Text.of(RESET, "=== "))
                .append(Text.of(YELLOW, "Position "))
                .append(Text.of(GREEN, "Radius "))
                .append(inRaduis)
                .append(Text.of(RESET, "/"))
                .append(notInRaduis)
                .append(Text.of(RESET, "(in radius)"))
                .append(Text.of(RESET, " ==="))
                .build()
    }

    override fun execute(src: CommandSource?, ctx: CommandContext?): CommandResult? {
        val player = when (src) {
            null -> return CommandResult.empty()
            !is Player -> return src.error("Command must be executed by a player!")
            else -> src
        }

        val repellersInWorld = State.repellers.filter { it.key.inExtent(player.location.extent) }
        if (repellersInWorld.size == 0) return src.warn("No MobRepeller in this world!")

        val repellerTexts = repellersInWorld.map { repeller ->
            val pos = repeller.key.position
            val isInRadius = pos.distance(player.location.position) <= repeller.value.radius
            Text.builder()
                    .append(Text.of(YELLOW, "${pos.floorX}, ${pos.floorY}, ${pos.floorZ} "))
                    .append(Text.of(GREEN, "${repeller.value.radius} "))
                    .append(if (isInRadius) inRaduis else notInRaduis)
                    .build()
        }

        player.sendMessage(Text.of(GREEN, "${repellersInWorld.size} MobRepeller(s) in this world"))
        val paginationServcie = Sponge.getServiceManager().provide(PaginationService::class.java).get()
        paginationServcie.builder()
            .header(listHeader)
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