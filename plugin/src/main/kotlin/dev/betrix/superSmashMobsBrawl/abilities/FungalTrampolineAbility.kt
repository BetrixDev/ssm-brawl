package dev.betrix.superSmashMobsBrawl.abilities

import dev.betrix.superSmashMobsBrawl.extensions.isAirOrFoliage
import dev.betrix.superSmashMobsBrawl.extensions.setVelocity
import gg.flyte.twilight.scheduler.TwilightRunnable
import gg.flyte.twilight.scheduler.delay
import gg.flyte.twilight.scheduler.repeatingTask
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.block.Block
import org.bukkit.block.data.BlockData
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.util.Vector

class FungalTrampolineAbility(player: Player) : BrawlAbility("fungal_trampoline", player) {

    private val platformSize = metadata.int("platformSize") ?: 3
    private val durationMs = metadata.long("durationMs") ?: 4000
    private val bounceStrength = metadata.double("bounceStrength") ?: 1.2
    private val bounceYAdd = metadata.double("bounceYAdd") ?: 0.8
    private val particleIntervalTicks = metadata.long("particleIntervalTicks") ?: 5

    private val platformBlocks = mutableListOf<Block>()
    private val originalBlockData = mutableMapOf<Block, BlockData>()
    private var platformTask: TwilightRunnable? = null

    override fun activate() {
        super.activate()

        val centerLocation = player.location.clone().subtract(0.0, 1.0, 0.0)
        val centerBlock = centerLocation.block

        if (!centerBlock.type.isSolid && !centerBlock.isAirOrFoliage()) {
            return
        }

        createPlatform(centerBlock)

        if (platformBlocks.isEmpty()) {
            return
        }

        player.world.playSound(
            centerLocation,
            Sound.BLOCK_FUNGUS_PLACE,
            1.0f,
            0.8f,
        )

        startPlatformEffects()

        val durationTicks = durationMs / 50L
        delay(durationTicks) { removePlatform() }
    }

    override fun teardown() {
        removePlatform()
        platformTask?.cancel()
        platformTask = null
        super.teardown()
    }

    private fun createPlatform(centerBlock: Block) {
        val radius = platformSize / 2
        val centerX = centerBlock.x
        val centerY = centerBlock.y
        val centerZ = centerBlock.z

        for (x in -radius..radius) {
            for (z in -radius..radius) {
                val block = centerBlock.world.getBlockAt(centerX + x, centerY, centerZ + z)

                if (block.type == Material.AIR || block.isAirOrFoliage()) {
                    originalBlockData[block] = block.blockData.clone()
                    block.type = Material.BARRIER
                    platformBlocks.add(block)
                }
            }
        }
    }

    private fun startPlatformEffects() {
        platformTask?.cancel()

        platformTask =
            repeatingTask(0, particleIntervalTicks) {
                if (!player.isValid || platformBlocks.isEmpty()) {
                    cancel()
                    return@repeatingTask
                }

                platformBlocks.forEach { block ->
                    val blockCenter = block.location.clone().add(0.5, 1.0, 0.5)

                    Particle.SPORE_BLOSSOM_AIR.builder()
                        .location(blockCenter)
                        .count(2)
                        .offset(0.4, 0.1, 0.4)
                        .extra(0.0)
                        .receivers(96, true)
                        .spawn()

                    checkForBounce(blockCenter)
                }
            }

        platformTask?.let { runnables.add(it) }
    }

    private fun checkForBounce(blockCenter: Location) {
        blockCenter.world
            .getNearbyEntities(blockCenter, 0.6, 0.8, 0.6)
            .filterIsInstance<LivingEntity>()
            .forEach { entity ->
                if (entity.velocity.y < 0.0 && entity.location.y >= blockCenter.y - 0.3) {
                    bounceEntity(entity)
                }
            }
    }

    private fun bounceEntity(entity: LivingEntity) {
        entity.setVelocity(
            velocity = Vector(0.0, 1.0, 0.0),
            strength = bounceStrength,
            ySet = true,
            yBase = bounceStrength,
            yAdd = bounceYAdd,
            yMax = 10.0,
            groundBoost = false,
        )

        entity.world.playSound(
            entity.location,
            Sound.ENTITY_SLIME_SQUISH,
            0.5f,
            1.5f,
        )

        Particle.GUST_EMITTER_SMALL.builder()
            .location(entity.location)
            .count(5)
            .offset(0.3, 0.1, 0.3)
            .extra(0.0)
            .receivers(96, true)
            .spawn()
    }

    private fun removePlatform() {
        platformBlocks.forEach { block ->
            val originalData = originalBlockData[block]
            if (originalData != null && block.world.isChunkLoaded(block.chunk.x, block.chunk.z)) {
                block.setBlockData(originalData, false)
            }
        }

        platformBlocks.clear()
        originalBlockData.clear()

        platformTask?.cancel()
        platformTask = null
    }
}

