package dev.betrix.superSmashMobsBrawl.abilities

import dev.betrix.superSmashMobsBrawl.events.BrawlDamageEvent
import dev.betrix.superSmashMobsBrawl.events.BrawlDamageType
import dev.betrix.superSmashMobsBrawl.events.Damager
import dev.betrix.superSmashMobsBrawl.extensions.isAirOrFoliage
import dev.betrix.superSmashMobsBrawl.extensions.isOnBlock
import dev.betrix.superSmashMobsBrawl.extensions.setVelocity
import gg.flyte.twilight.scheduler.TwilightRunnable
import gg.flyte.twilight.scheduler.delay
import gg.flyte.twilight.scheduler.repeatingTask
import kotlin.math.min
import kotlin.math.sqrt
import org.bukkit.Bukkit
import org.bukkit.Effect
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.data.BlockData
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.util.Vector

class FissureAbility(player: Player) : BrawlAbility("fissure", player) {
    private var fissureTask: TwilightRunnable? = null

    override fun activate() {
        if (!player.isOnBlock()) {
            return
        }
        super.activate()
        val startLocation = player.location.clone()
        val data = FissureData(startLocation)
        fissureTask?.cancel()
        fissureTask =
            repeatingTask(0, 1) {
                if (data.update()) {
                    data.clear()
                    cancel()
                    fissureTask = null
                }
            }
        fissureTask?.let { runnables.add(it) }
    }

    override fun teardown() {
        fissureTask?.cancel()
        fissureTask = null
        super.teardown()
    }

    private inner class FissureData(startLocation: Location) {
        private val owner: Player = player
        private val world = startLocation.world
        private val direction: Vector =
            owner.location.direction.clone().apply {
                y = 0.0
                if (length() == 0.0) {
                    x = 0.0
                    z = 1.0
                }
                normalize()
                multiply(0.1)
            }
        private val currentLocation: Location =
            startLocation.clone().add(direction).add(0.0, -0.4, 0.0)
        private val path = mutableListOf<Block>()
        private val hitPlayers = mutableSetOf<Player>()
        private val trackedBlockStates = mutableMapOf<Block, BlockData>()
        private val trackedRestores = mutableMapOf<Block, MutableList<BlockData>>()
        private var height = 0
        private var handled = 0

        init {
            buildPath(startLocation.clone())
        }

        fun update(): Boolean {
            if (handled >= path.size) {
                return true
            }
            val block = path[handled]
            if (block.type == Material.TNT) {
                return false
            }
            val up = block.getRelative(0, height + 1, 0)
            if (!up.isAirOrFoliage()) {
                playStepEffect(up, block.type)
                height = 0
                handled++
                return handled >= path.size
            }
            applyBaseTransform(block)
            if (!applyColumnRise(block, up)) {
                height = 0
                handled++
                return handled >= path.size
            }
            playStepEffect(up, block.type)
            handleDamage(block, up)
            if (height >= min(2, handled / 3 + 1)) {
                height = 0
                handled++
            }
            return handled >= path.size
        }

        fun clear() {
            hitPlayers.clear()
            path.clear()
        }

        private fun buildPath(start: Location) {
            while (getHorizontalDistance(currentLocation, start) < 14.0) {
                currentLocation.add(direction)
                var block = currentLocation.block
                if (block == start.block) {
                    continue
                }
                if (path.contains(block)) {
                    continue
                }
                if (isSolid(block.getRelative(BlockFace.UP))) {
                    currentLocation.add(0.0, 1.0, 0.0)
                    block = currentLocation.block
                    if (isSolid(block.getRelative(BlockFace.UP))) {
                        return
                    }
                } else if (!isSolid(block)) {
                    currentLocation.add(0.0, -1.0, 0.0)
                    block = currentLocation.block
                    if (!isSolid(block)) {
                        return
                    }
                }
                val blockCenter = block.location.clone().add(0.5, 0.5, 0.5)
                if (blockCenter.distance(currentLocation) > 0.5) {
                    continue
                }
                path.add(block)
                playStepEffect(block, block.type)
                slowOwnerIfEnemyNearby(blockCenter)
            }
        }

        private fun slowOwnerIfEnemyNearby(blockCenter: Location) {
            world?.players?.forEach { other ->
                if (other == owner) {
                    return@forEach
                }
                if (blockCenter.distance(other.location) < 1.5) {
                    owner.removePotionEffect(PotionEffectType.SLOWNESS)
                    owner.addPotionEffect(PotionEffect(PotionEffectType.SLOWNESS, 80, 1))
                    owner.velocity = Vector(0.0, 0.0, 0.0)
                }
            }
        }

        private fun applyBaseTransform(block: Block) {
            when (block.type) {
                Material.STONE ->
                    scheduleBlockChange(block, Material.COBBLESTONE.createBlockData(), 14000L)
                Material.GRASS_BLOCK ->
                    scheduleBlockChange(block, Material.DIRT.createBlockData(), 14000L)
                Material.STONE_BRICKS ->
                    scheduleBlockChange(
                        block,
                        Material.CRACKED_STONE_BRICKS.createBlockData(),
                        14000L,
                    )
                Material.SNOW ->
                    scheduleBlockChange(block, Material.SNOW_BLOCK.createBlockData(), 10000L)
                else -> {}
            }
        }

        private fun applyColumnRise(base: Block, up: Block): Boolean {
            val maxHeight = world?.maxHeight ?: return false
            if (up.y >= maxHeight) {
                return false
            }
            if (base.type == Material.SNOW) {
                val duration = (10000L - (1000L * height)).coerceAtLeast(0L)
                scheduleBlockChange(base, Material.SNOW_BLOCK.createBlockData(), duration)
                scheduleBlockChange(up, Material.SNOW_BLOCK.createBlockData(), duration)
            } else {
                val duration = (10000L - (1000L * height)).coerceAtLeast(0L)
                scheduleBlockChange(up, base.blockData.clone(), duration)
            }
            height++
            return true
        }

        private fun handleDamage(base: Block, up: Block) {
            val upCenter = up.location.clone().add(0.5, 0.5, 0.5)
            Bukkit.getOnlinePlayers().forEach { target ->
                if (target == owner) {
                    return@forEach
                }
                if (target.location.block == base) {
                    target.teleport(target.location.add(0.0, 1.0, 0.0))
                }
                if (hitPlayers.contains(target)) {
                    return@forEach
                }
                if (upCenter.distance(target.location) < 1.5) {
                    hitPlayers.add(target)
                    val damage = 4.0 + handled
                    val damageEvent =
                        BrawlDamageEvent(
                            target,
                            Damager.DamagerLivingEntity(owner),
                            damage,
                            0.0,
                            BrawlDamageType.Explosion,
                        )
                    damageEvent.callEvent()
                    target.sendMessage(
                        lang.t("messages.abilities.hitByAbility") {
                            "abilityId" to id
                            "attacker" to owner.name
                        }
                    )
                    val handledSnapshot = handled
                    delay(4) { launchTarget(target, upCenter, handledSnapshot) }
                }
            }
        }

        private fun launchTarget(target: Player, origin: Location, handledSnapshot: Int) {
            if (!target.isValid || target.isDead) {
                return
            }
            val trajectory = target.location.toVector().subtract(origin.toVector())
            trajectory.y = 0.0
            if (trajectory.lengthSquared() <= 0.0) {
                return
            }
            val horizontal = trajectory.normalize()
            target.setVelocity(
                horizontal,
                1.0 + 0.1 * handledSnapshot,
                true,
                0.6 + 0.05 * handledSnapshot,
                0.0,
                10.0,
                true,
            )
        }

        private fun scheduleBlockChange(block: Block, newData: BlockData, durationMs: Long) {
            if (!trackedBlockStates.containsKey(block)) {
                trackedBlockStates[block] = block.blockData.clone()
            }
            val originalData = trackedBlockStates[block]
            val appliedData = newData.clone()
            block.setBlockData(appliedData, false)
            val delayTicks = (durationMs / 50L).coerceAtLeast(1L)
            delay(delayTicks) {
                val stored = trackedBlockStates.remove(block) ?: originalData
                if (stored != null && block.world.isChunkLoaded(block.chunk.x, block.chunk.z)) {
                    if (block.blockData.matches(appliedData)) {
                        block.setBlockData(stored, false)
                    } else {
                        trackedBlockStates[block] = stored
                    }
                }
            }
        }

        private fun playStepEffect(block: Block, material: Material) {
            block.world.playEffect(block.location, Effect.STEP_SOUND, material)
        }

        private fun isSolid(block: Block): Boolean {
            return block.type.isSolid || block.type == Material.SNOW
        }

        private fun getHorizontalDistance(a: Location, b: Location): Double {
            val dx = a.x - b.x
            val dz = a.z - b.z
            return sqrt(dx * dx + dz * dz)
        }
    }
}
