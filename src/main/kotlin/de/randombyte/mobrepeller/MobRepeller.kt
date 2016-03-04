package de.randombyte.mobrepeller

import com.google.inject.Inject
import me.flibio.updatifier.Updatifier
import org.slf4j.Logger
import org.spongepowered.api.entity.living.player.Player
import org.spongepowered.api.event.Listener
import org.spongepowered.api.event.block.ChangeBlockEvent
import org.spongepowered.api.event.filter.cause.Root
import org.spongepowered.api.plugin.Plugin
import org.spongepowered.api.world.Location
import org.spongepowered.api.world.World
import java.util.*

@Plugin(id = PluginInfo.ID, name = PluginInfo.NAME, version = PluginInfo.VERSION, dependencies = "after: Updatifier")
@Updatifier(repoOwner = "randombyte-developer", repoName = PluginInfo.NAME, version = PluginInfo.VERSION)
class MobRepeller {

    @Inject
    private lateinit var logger: Logger

    //Where the cross' center is and what radius it protects
    private val repellers: MutableMap<Location<World>, Int> = HashMap() //Todo: SQL

    @Listener
    fun onPlaceBlock(event: ChangeBlockEvent.Place, @Root player: Player) {
        //To improve performance just iterate one time over the transactions
        event.transactions.filter { it.final.location.isPresent }.forEach {
            val location = it.final.location.get()
            repellers[location] = CrossShapeChecker.checkCross(location)
            logger.info("Added cross with value ${repellers[location]} at" +
                    "${location.position}")
        }
    }


}