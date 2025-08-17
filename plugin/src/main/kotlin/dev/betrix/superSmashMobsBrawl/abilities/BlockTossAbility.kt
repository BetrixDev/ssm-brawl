package dev.betrix.superSmashMobsBrawl.abilities

import gg.flyte.twilight.scheduler.TwilightRunnable
import gg.flyte.twilight.scheduler.repeatingTask
import org.bukkit.Effect
import org.bukkit.Sound
import org.bukkit.block.data.BlockData
import org.bukkit.entity.Player

class BlockTossAbility(player: Player) : BrawlAbility("block_toss", player) {

    private var holdingBlockData: BlockData? = null
    private var pickupTimeMs: Long = 0
    private var tossTask: TwilightRunnable? = null
    private val chargeTimeMs = metadata.long("chargeTimeMs") ?: 1200L

    override fun setup() {
        super.setup()
    }

    override fun activate() {
        val event = latestInteractEvent ?: return

        val block = event.clickedBlock ?: return
        val material = block.type

        if (!material.isSolid || !material.isOccluding) {
            return
        }

        holdingBlockData = block.blockData.clone()
        pickupTimeMs = System.currentTimeMillis()

        player.world.playEffect(block.location, Effect.STEP_SOUND, material)

        tossTask?.cancel()

        tossTask =
            repeatingTask(1) {
                if (!player.isBlocking) {
                    cancelTossTask()
                }

                if (System.currentTimeMillis() - pickupTimeMs > chargeTimeMs) {
                    player.playSound(player.eyeLocation, Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1f)
                }
            }
    }

    private fun doBlockToss() {
        super.activate()
    }

    private fun cancelTossTask() {
        tossTask?.cancel()
        tossTask = null
    }
}
