package de.randombyte.mobrepeller

import com.google.inject.Inject
import de.randombyte.mobrepeller.commands.ListRepellers
import de.randombyte.mobrepeller.commands.RegisterRepeller
import de.randombyte.mobrepeller.database.DatabaseManager
import me.flibio.updatifier.Updatifier
import org.h2.tools.Server
import org.slf4j.Logger
import org.spongepowered.api.Sponge
import org.spongepowered.api.command.spec.CommandSpec
import org.spongepowered.api.config.ConfigDir
import org.spongepowered.api.entity.living.monster.Monster
import org.spongepowered.api.event.Listener
import org.spongepowered.api.event.block.ChangeBlockEvent
import org.spongepowered.api.event.entity.ConstructEntityEvent
import org.spongepowered.api.event.game.state.GameInitializationEvent
import org.spongepowered.api.event.world.LoadWorldEvent
import org.spongepowered.api.plugin.Plugin
import org.spongepowered.api.text.Text
import java.nio.file.Path

@Plugin(id = PluginInfo.ID, name = PluginInfo.NAME, version = PluginInfo.VERSION, authors = arrayOf(PluginInfo.AUTHOR))
@Updatifier(repoOwner = "randombyte-developer", repoName = PluginInfo.NAME, version = PluginInfo.VERSION)
class MobRepeller {

    @Inject
    lateinit var logger: Logger

    @Inject
    @ConfigDir(sharedRoot = false)
    lateinit var pluginConfigDir: Path

    var wevServer: Server? = null

    @Listener
    fun onInit(event: GameInitializationEvent) {

        //DEBUGGING
        //webServer = Server.createWebServer("-web", "-webAllowOthers", "-webPort", "8082").start()

        State.logger = logger

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

        Sponge.getEventManager().registerListener(this, ChangeBlockEvent.Place::class.java, PlaceBlockListener())
        Sponge.getEventManager().registerListener(this, ChangeBlockEvent.Break::class.java, BreakBlockListener())

        DatabaseManager.databasePath = pluginConfigDir.resolve("MobRepeller").toAbsolutePath().toString()

        logger.info("Loaded ${PluginInfo.NAME}: ${PluginInfo.VERSION}!")
    }

    @Listener
    fun onLoadWorldEvent(event: LoadWorldEvent) {
        loadConfigurations()
        logger.info("Reloaded world's MobRepellers due to new loaded world!")
    }

    fun loadConfigurations() {
        State.repellers = DatabaseManager.getAllRepellers() //creates database if it doesn't exist yet
    }

    @Listener
    fun onConstructEntity(event: ConstructEntityEvent.Pre) {
        event.isCancelled =
                Monster::class.java.isAssignableFrom(event.targetType.entityClass) && //is Monster
                State.repellers.filter { it.key.inExtent(event.transform.extent) } //is in same Extent as one repeller
                .any { it.key.position.distance(event.transform.position) <= it.value.radius } //is in radius of that repeller
    }
}