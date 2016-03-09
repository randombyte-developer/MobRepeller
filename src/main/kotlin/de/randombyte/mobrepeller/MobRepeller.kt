package de.randombyte.mobrepeller

import com.google.inject.Inject
import de.randombyte.mobrepeller.commands.ListRepellers
import de.randombyte.mobrepeller.commands.PlayerCommunicator.warn
import de.randombyte.mobrepeller.commands.RegisterRepeller
import me.flibio.updatifier.Updatifier
import org.slf4j.Logger
import org.spongepowered.api.Sponge
import org.spongepowered.api.command.spec.CommandSpec
import org.spongepowered.api.entity.living.monster.Monster
import org.spongepowered.api.entity.living.player.Player
import org.spongepowered.api.event.Listener
import org.spongepowered.api.event.block.ChangeBlockEvent
import org.spongepowered.api.event.entity.SpawnEntityEvent
import org.spongepowered.api.event.game.state.GameInitializationEvent
import org.spongepowered.api.plugin.Plugin
import org.spongepowered.api.text.Text
import org.spongepowered.api.world.Location
import org.spongepowered.api.world.World
import java.util.*

@Plugin(id = PluginInfo.ID, name = PluginInfo.NAME, version = PluginInfo.VERSION, dependencies = "after: Updatifier")
@Updatifier(repoOwner = "randombyte-developer", repoName = PluginInfo.NAME, version = PluginInfo.VERSION)
class MobRepeller {

    companion object {
        //Where the cross' center is and what radius it protects
        var repellers: MutableMap<Location<World>, Int> = HashMap() //Todo: SQL
    }

    @Inject
    private lateinit var logger: Logger

    @Listener
    fun onInit(event: GameInitializationEvent) {
        Sponge.getCommandManager().register(this, CommandSpec.builder()
            .executor(RegisterRepeller())
            .description(Text.of("Registers a MobRepeller"))
            .extendedDescription(Text.of("Tries to register a MobRepeller on block Player is standing on which has to be" +
                    "the center of the cross."))
            .build(), "registerRepeller")

        Sponge.getCommandManager().register(this, CommandSpec.builder()
                .executor(ListRepellers())
                .description(Text.of("Lists all MobRepellers in this world"))
                .build(), "listRepellers")

        logger.info("Loaded ${PluginInfo.NAME}: ${PluginInfo.VERSION}!")
    }

    @Listener
    fun onMobSpawn(event: SpawnEntityEvent) {
        //todo: Don't prevent "custom spawning"
        event.filterEntities { entity ->
            entity !is Monster || repellers.none { repeller ->
                //check if in same world
                repeller.key.extent.uniqueId.equals(entity.world.uniqueId) &&
                    //check if entity is in radius of repeller
                    repeller.key.position.distance(entity.location.position) <= repeller.value

            }
        }
    }

    //todo: do it in a more elegant way
    @Listener
    fun onBreakBlock(event: ChangeBlockEvent.Break) {
        event.transactions.forEach {
            if (!it.final.location.isPresent) return@forEach //This instead 'filter' for better performance
            val removedBlock = it.final.location.get()

            if (repellers.remove(removedBlock) != null) {
                //Center block removed
                //todo: compare like below
                val rootCause = event.cause.root()
                if (rootCause is Player) rootCause.warn("Deregistered MobRepeller at ${removedBlock.position.toInt()}!")
            } else {
                val iterator = repellers.iterator()
                while (iterator.hasNext()) {
                    var repeller = iterator.next()
                    while (!repeller.key.extent.equals(removedBlock.extent)) {
                        if (!iterator.hasNext()) return
                        repeller = iterator.next()
                    }
                    CrossShapeChecker.directions.forEach { direction ->
                        val relative = removedBlock.getRelative(direction)
                        if (repeller.key.position.toInt().equals(relative.position.toInt())) {
                            //Needed block in cross shape removed
                            iterator.remove()
                            val rootCause = event.cause.root()
                            if (rootCause is Player) rootCause.warn("Deregistered MobRepeller at " +
                                    "${removedBlock.position.toInt()}!")
                            return
                        }
                    }
                }
            }
        }
    }
}