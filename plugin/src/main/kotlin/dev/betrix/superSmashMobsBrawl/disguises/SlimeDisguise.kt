package dev.betrix.superSmashMobsBrawl.disguises

import dev.betrix.superSmashMobsBrawl.models.Hitbox
import dev.betrix.superSmashMobsBrawl.utils.Atom
import gg.flyte.twilight.scheduler.repeatingTask
import me.libraryaddict.disguise.disguisetypes.DisguiseType
import me.libraryaddict.disguise.disguisetypes.MobDisguise
import me.libraryaddict.disguise.disguisetypes.watchers.SlimeWatcher
import org.bukkit.entity.Player

class SlimeDisguise(player: Player) : BrawlDisguise(player) {
    override val disguise = MobDisguise(DisguiseType.SLIME)
    override val hitbox = Hitbox(0.6, 0.6)

    private val slimeSizeAtom = Atom(1)

    override fun setup() {
        super.setup()

        runnables.add(repeatingTask(1) {
            slimeSizeAtom.value = when(player.exp) {
                in 0.0..0.55 -> 1
                in 0.55..0.8 -> 2
                else -> 3
            }
        })

        slimeSizeAtom.subscribe { size ->
            setSize(size)
        }
    }

    override fun teardown() {
        super.teardown()
        slimeSizeAtom.unsubscribeAll()
    }

    fun setSize(size: Int) {
        (disguise.watcher as? SlimeWatcher)?.size = size
    }

    fun getSize(): Int = slimeSizeAtom.value
}
