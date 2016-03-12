package de.randombyte.mobrepeller

import com.google.inject.Inject
import de.randombyte.mobrepeller.State.RepellerRegistrationResult.*
import de.randombyte.mobrepeller.commands.ListRepellers
import de.randombyte.mobrepeller.commands.PlayerCommunicator.warn
import de.randombyte.mobrepeller.commands.RegisterRepeller
import me.flibio.updatifier.Updatifier
import org.slf4j.Logger
import org.spongepowered.api.Sponge
import org.spongepowered.api.command.spec.CommandSpec
import org.spongepowered.api.entity.Entity
import org.spongepowered.api.entity.living.monster.Monster
import org.spongepowered.api.entity.living.player.Player
import org.spongepowered.api.event.Listener
import org.spongepowered.api.event.block.ChangeBlockEvent
import org.spongepowered.api.event.entity.ConstructEntityEvent
import org.spongepowered.api.event.entity.SpawnEntityEvent
import org.spongepowered.api.event.game.state.GameInitializationEvent
import org.spongepowered.api.plugin.Dependency
import org.spongepowered.api.plugin.Plugin
import org.spongepowered.api.text.Text
import org.spongepowered.api.world.Location
import org.spongepowered.api.world.World
import java.util.*

@Plugin(id = PluginInfo.ID, name = PluginInfo.NAME, version = PluginInfo.VERSION)
@Updatifier(repoOwner = "randombyte-developer", repoName = PluginInfo.NAME, version = PluginInfo.VERSION)
class MobRepeller {

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

        /*Sponge.getCommandManager().register(this, CommandSpec.builder()
                .executor(ListRepellers())
                .description(Text.of("Lists all MobRepellers in this world"))
                .build(), "listRepellers")*/

        Sponge.getEventManager().registerListener(this, ChangeBlockEvent.Place::class.java, PlaceBlockListener())
        Sponge.getEventManager().registerListener(this, ChangeBlockEvent.Break::class.java, BreakBlockListener())

        logger.info("Loaded ${PluginInfo.NAME}: ${PluginInfo.VERSION}!")
    }

    @Listener
    fun onMobSpawn(event: ConstructEntityEvent.Pre) {
        event.isCancelled =
                Monster::class.java.isAssignableFrom(event.targetType.entityClass) && //is Monster
                State.repellers.filter { it.key.inExtent(event.transform.extent) } //is in same Extent as one repeller
                .any { it.key.position.distance(event.transform.position) <= it.value.first } //is in radius of that repeller
    }
}