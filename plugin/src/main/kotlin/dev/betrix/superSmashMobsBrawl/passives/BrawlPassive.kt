package dev.betrix.superSmashMobsBrawl.passives

import dev.betrix.superSmashMobsBrawl.Manageable
import dev.betrix.superSmashMobsBrawl.extensions.getAs
import dev.betrix.superSmashMobsBrawl.extensions.sendDebugMessage
import dev.betrix.superSmashMobsBrawl.interfaces.MetadataAccessor
import dev.betrix.superSmashMobsBrawl.services.DataService
import dev.betrix.superSmashMobsBrawl.services.KitService
import dev.betrix.superSmashMobsBrawl.services.MinigameService
import org.bukkit.entity.Player
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

abstract class BrawlPassive(val id: String, val player: Player) : Manageable(), KoinComponent {
    protected val dataService: DataService by inject()
    protected val minigameService: MinigameService by inject()
    protected val kitService: KitService by inject()

    protected val passiveData by lazy {
        dataService.getPassive(id)
            ?: throw RuntimeException("No passive found in DataService with id $id")
    }

    protected val minigameData by lazy {
        val minigameId =
            minigameService.getMinigameForPlayer(player)?.minigameId ?: return@lazy null

        return@lazy dataService.getMinigame(minigameId)
    }

    protected val kitData by lazy {
        val kitId = kitService.getKitForPlayer(player)?.id ?: return@lazy null

        return@lazy dataService.getKit(kitId)
    }

    protected val metadata: MetadataAccessor =
        object : MetadataAccessor {
            override fun string(name: String): String? = getValue<String>(name)

            override fun double(name: String): Double? = getValue<Double>(name)

            override fun int(name: String): Int? = getValue<Int>(name)

            override fun float(name: String): Float? = getValue<Float>(name)

            override fun long(name: String): Long? = getValue<Long>(name)

            override fun boolean(name: String): Boolean? = getValue<Boolean>(name)

            inline fun <reified T> getValue(name: String): T? {
                try {
                    if (kitData != null) {
                        minigameData?.overrides?.kits?.get(kitData?.id)?.passives?.get(id)?.let {
                            it.metadata?.getAs<T>(name)?.let { value ->
                                return value
                            }
                        }

                        kitData
                            ?.passives
                            ?.find { it.id == id }
                            ?.overrides
                            ?.metadata
                            ?.getAs<T>(name)
                            ?.let { value ->
                                return value
                            }
                    }

                    return passiveData.metadata.getAs<T>(name)
                } catch (e: Exception) {
                    e.printStackTrace()
                    return null
                }
            }
        }

    override fun setup() {
        super.setup()
        if (passiveData.userFacing) {
            player.sendDebugMessage("You have been given the $id passive")
        }
    }

    override fun teardown() {
        super.teardown()
        if (passiveData.userFacing) {
            player.sendDebugMessage("The $id passive has been removed")
        }
    }
}
