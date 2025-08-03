package dev.betrix.superSmashMobsBrawl.disguises

import dev.betrix.superSmashMobsBrawl.Manageable
import dev.betrix.superSmashMobsBrawl.models.Hitbox
import me.libraryaddict.disguise.DisguiseAPI
import me.libraryaddict.disguise.disguisetypes.MobDisguise
import org.bukkit.entity.Player
import org.bukkit.util.BoundingBox

open class Disguise(private val player: Player) : Manageable() {
    protected lateinit var disguise: MobDisguise
    protected lateinit var hitbox: Hitbox

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
        private val playerDisguises = mutableMapOf<Player, Disguise>()

        fun getDisguise(player: Player): Disguise? {
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
        internal fun registerDisguise(player: Player, disguise: Disguise) {
            playerDisguises[player] = disguise
        }
    }

    override fun setup() {
        if (!::disguise.isInitialized) {
            throw RuntimeException("The inheritor of this class should set the disguise property")
        }

        if (!::hitbox.isInitialized) {
            throw RuntimeException("The inheritor of this class should set the hitbox property")
        }

        registerDisguise(player, this)

        disguise.entity = player
        disguise.notifyBar = null

        DisguiseAPI.disguiseToAll(player, disguise)
        disguise.startDisguise()
    }

    override fun teardown() {
        disguise.stopDisguise()
        disguise.removeDisguise()

        removeDisguise(player)
    }
}
