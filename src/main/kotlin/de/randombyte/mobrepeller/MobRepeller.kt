package de.randombyte.mobrepeller

import com.google.inject.Inject
import de.randombyte.mobrepeller.commands.ListRepellers
import de.randombyte.mobrepeller.database.DatabaseManager
import org.slf4j.Logger
import org.spongepowered.api.Sponge
import org.spongepowered.api.command.spec.CommandSpec
import org.spongepowered.api.config.ConfigDir
import org.spongepowered.api.entity.living.monster.Monster
import org.spongepowered.api.event.Listener
import org.spongepowered.api.event.entity.ConstructEntityEvent
import org.spongepowered.api.event.game.state.GameInitializationEvent
import org.spongepowered.api.event.world.LoadWorldEvent
import org.spongepowered.api.plugin.Plugin
import org.spongepowered.api.text.Text
import java.nio.file.Path

@Plugin(id = PluginInfo.ID, name = PluginInfo.NAME, version = PluginInfo.VERSION, authors = arrayOf(PluginInfo.AUTHOR))
class MobRepeller @Inject constructor(val logger: Logger, @ConfigDir(sharedRoot = false) val pluginConfigDir: Path) {

    @Listener
    fun onInit(event: GameInitializationEvent) {
        State.logger = logger
        DatabaseManager.databasePath = pluginConfigDir.resolve(PluginInfo.ID).toAbsolutePath().toString()

        Sponge.getCommandManager().register(this, CommandSpec.builder()
                .executor(ListRepellers())
                .description(Text.of("Lists all MobRepellers in this world"))
                .build(), "listRepellers")

        Sponge.getEventManager().registerListeners(this, ChangeBlockListener())

        logger.info("Loaded ${PluginInfo.NAME}: ${PluginInfo.VERSION}!")
    }

    @Listener
    fun onLoadWorldEvent(event: LoadWorldEvent) {
        loadConfigurations()
        logger.info("Reloaded world's MobRepellers due to new loaded world!")
    }

    fun loadConfigurations() {
        State.repellers = DatabaseManager.getAllRepellers() // creates database if it doesn't exist yet
    }

    @Listener
    fun onConstructEntity(event: ConstructEntityEvent.Pre) {
        event.isCancelled =
                Monster::class.java.isAssignableFrom(event.targetType.entityClass) && // is Monster
                State.repellers.filter { it.key.inExtent(event.transform.extent) } // is in same Extent as one repeller
                .any { it.key.position.distance(event.transform.position) <= it.value.radius } // is in radius of that repeller
    }
}