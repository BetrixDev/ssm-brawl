package dev.betrix.superSmashMobsBrawl.disguises

import dev.betrix.superSmashMobsBrawl.Manageable
import dev.betrix.superSmashMobsBrawl.extensions.disguise
import dev.betrix.superSmashMobsBrawl.models.Hitbox
import gg.flyte.twilight.event.event
import java.util.concurrent.ConcurrentHashMap
import me.libraryaddict.disguise.DisguiseAPI
import me.libraryaddict.disguise.disguisetypes.MobDisguise
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.util.BoundingBox

abstract class BrawlDisguise(protected val player: Player) : Manageable() {
    protected abstract val disguise: MobDisguise
    protected abstract val hitbox: Hitbox

    val boundingBox: BoundingBox
        get() {
            val location = player.location
            val centerX = location.x
            val centerY = location.y + (hitbox.height / 2.0)
            val centerZ = location.z

            val halfWidth = hitbox.width / 2.0
            val halfHeight = hitbox.height / 2.0

            return BoundingBox.of(
                location.toVector().setX(centerX).setY(centerY).setZ(centerZ),
                halfWidth,
                halfHeight,
                halfWidth,
            )
        }

    companion object {
        private val playerDisguises = ConcurrentHashMap<Player, BrawlDisguise>()

        fun getDisguise(player: Player): BrawlDisguise? {
            return playerDisguises[player]
        }

        fun hasDisguise(player: Player): Boolean {
            return playerDisguises.containsKey(player)
        }

        /** Removes a disguise from the registry (internal use only) */
        internal fun removeDisguise(player: Player) {
            playerDisguises.remove(player)
        }

        /** Registers a disguise in the registry (internal use only) */
        internal fun registerDisguise(player: Player, disguise: BrawlDisguise) {
            playerDisguises[player] = disguise
        }
    }

    override fun setup() {
        registerDisguise(player, this)

        disguise.entity = player
        disguise.notifyBar = null
        disguise.isSelfDisguiseVisible = false

        DisguiseAPI.disguiseToAll(player, disguise)
        disguise.startDisguise()

        listeners.add(
            event<PlayerQuitEvent> {
                if (player != this@BrawlDisguise.player) {
                    return@event
                }

                player.disguise?.teardown()
            }
        )
    }

    override fun teardown() {
        disguise.stopDisguise()
        disguise.removeDisguise()

        removeDisguise(player)
    }
}
