package de.randombyte.mobrepeller

import com.google.inject.Inject
import de.randombyte.mobrepeller.commands.ListRepellers
import de.randombyte.mobrepeller.commands.RegisterRepeller
import me.flibio.updatifier.Updatifier
import org.slf4j.Logger
import org.spongepowered.api.Sponge
import org.spongepowered.api.command.spec.CommandSpec
import org.spongepowered.api.entity.living.monster.Monster
import org.spongepowered.api.event.Listener
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
        val repellers: MutableMap<Location<World>, Int> = HashMap() //Todo: SQL
    }

    @Inject
    private lateinit var logger: Logger

    @Listener
    fun onInit(event: GameInitializationEvent) {
        Sponge.getCommandManager().register(this, CommandSpec.builder()
            .executor(RegisterRepeller())
            .description(Text.of("Registers a repeller"))
            .extendedDescription(Text.of("Tries to register a repeller on block Player is standing on which has to be" +
                    "the center of the cross."))
            .build(), "registerRepeller")

        Sponge.getCommandManager().register(this, CommandSpec.builder()
                .executor(ListRepellers())
                .description(Text.of("List repellers in this world"))
                .build(), "listRepellers")

        logger.info("Loaded ${PluginInfo.NAME}: ${PluginInfo.VERSION}!")
    }

    @Listener
    fun onMobSpawn(event: SpawnEntityEvent) {
        //todo: Handle "custom spawn", don't prevent it
        val removedEntities = event.filterEntities { entity ->
            entity !is Monster || repellers.none { repeller ->
                //check if in same world
                repeller.key.extent.uniqueId.equals(entity.world.uniqueId) &&
                    //check if entity is in radius of repeller
                    repeller.key.position.distance(entity.location.position) <= repeller.value

            }
        }
        removedEntities.forEach {
            logger.info("Removed ${it.type.name}")
        }
    }
}