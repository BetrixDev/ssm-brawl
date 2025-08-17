package dev.betrix.superSmashMobsBrawl.abilities

import dev.betrix.superSmashMobsBrawl.disguises.EndermanDisguise
import dev.betrix.superSmashMobsBrawl.extensions.disguise
import dev.betrix.superSmashMobsBrawl.projectiles.BlockProjectile
import gg.flyte.twilight.scheduler.TwilightRunnable
import gg.flyte.twilight.scheduler.repeatingTask
import org.bukkit.Effect
import org.bukkit.Material
import org.bukkit.block.data.BlockData
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot

class BlockTossAbility(player: Player) : BrawlAbility("block_toss", player) {

    private var holdingBlockData: BlockData? = null
    private var pickupTimeMs: Long = 0
    private var tossRunnable: TwilightRunnable? = null
    private val chargeTimeMs = metadata.long("chargeTimeMs") ?: 1200L

    override fun setup() {
        super.setup()
    }

    override fun onPlayerInteract(event: PlayerInteractEvent) {
        if (event.hand != null && event.hand != EquipmentSlot.HAND) return

        if (!isCorrectActionForUsage(event.action)) {
            return
        }

        val item = event.item ?: return

        if (!isCorrectItemForAbility(item)) {
            return
        }

        event.isCancelled = true

        if (!canActivate()) {
            return
        }

        val block = event.clickedBlock ?: return
        val material = block.type

        if (!material.isSolid) {
            return
        }

        setDisguiseBlock(material)
        holdingBlockData = material.createBlockData()
        pickupTimeMs = System.currentTimeMillis()

        player.world.playEffect(block.location, Effect.STEP_SOUND, 0)

        tossRunnable?.cancel()

        var clicked = false

        tossRunnable = repeatingTask(1) {
            if (!player.isBlocking) {
                super.activate()
            }

            if (System.currentTimeMillis() - pickupTimeMs > chargeTimeMs && !clicked) {
                player.world.playEffect(player.location, Effect.CLICK1, 0)
                clicked = true
            }
        }
    }

    override fun activate() {
        if (holdingBlockData == null) {
            return
        }

        super.activate()

        val charge = System.currentTimeMillis() - pickupTimeMs
        val multiplier = Math.min(1.4, 1.4 * (charge.toDouble() / chargeTimeMs))

        BlockProjectile(player, charge, multiplier, holdingBlockData!!).launch()

        setDisguiseBlock(null)
    }

    private fun setDisguiseBlock(material: Material?) {
        (player.disguise as? EndermanDisguise)?.setHeldBlock(material)
    }

    private fun cancelTossTask() {
        tossRunnable?.cancel()
        tossRunnable = null
    }
}
