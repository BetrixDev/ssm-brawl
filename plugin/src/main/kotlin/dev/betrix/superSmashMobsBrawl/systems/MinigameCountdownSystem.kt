package dev.betrix.superSmashMobsBrawl.systems

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IntervalSystem
import com.github.quillraven.fleks.World.Companion.family
import com.github.quillraven.fleks.World.Companion.inject
import dev.betrix.superSmashMobsBrawl.components.MinigameComponent
import dev.betrix.superSmashMobsBrawl.components.MinigameState
import dev.betrix.superSmashMobsBrawl.components.PlayerComponent
import dev.betrix.superSmashMobsBrawl.extensions.getEquidistant
import dev.betrix.superSmashMobsBrawl.extensions.location
import dev.betrix.superSmashMobsBrawl.models.BrawlGameWorld
import dev.betrix.superSmashMobsBrawl.services.KitService
import dev.betrix.superSmashMobsBrawl.services.LangService
import java.time.Duration
import kotlin.math.max
import net.kyori.adventure.key.Key
import net.kyori.adventure.sound.Sound
import net.kyori.adventure.title.Title
import org.bukkit.GameMode

class MinigameCountdownSystem(
    private val lang: LangService = inject(),
    private val kitService: KitService = inject(),
) : IntervalSystem(interval = com.github.quillraven.fleks.Fixed(1f)) {

    private val starting = family { all(MinigameComponent) }

    override fun onTick() {
        starting.forEach { entity ->
            val minigame = entity[MinigameComponent]
            if (minigame.state != MinigameState.STARTING) return@forEach
            if (!minigame.hasLoadedWorld()) return@forEach

            val world = minigame.loadedWorld as? BrawlGameWorld ?: return@forEach

            if (minigame.remainingCountdownSeconds == null) {
                minigame.remainingCountdownSeconds =
                    max(0, minigame.minigame.startingCountdownSeconds ?: 0)

                if (minigame.minigame.startTeleportBeforeCountdown) {
                    val spawnPoints =
                        world.data.spawnPoints.getEquidistant(minigame.playerEntities.size)
                    minigame.playerEntities.forEachIndexed { idx, pEntity ->
                        val player = pEntity[PlayerComponent].player
                        val sp = spawnPoints[idx.coerceAtMost(spawnPoints.lastIndex)]
                        player.teleport(world.world.location(sp))
                        player.gameMode = GameMode.SURVIVAL
                        player.allowFlight = false
                        player.isFlying = false
                        player.fallDistance = 0f
                    }
                }
            }

            val secondsLeft = minigame.remainingCountdownSeconds ?: return@forEach

            if (secondsLeft > 0) {
                broadcastCountdown(entity, secondsLeft)
                minigame.remainingCountdownSeconds = secondsLeft - 1
            } else {
                if (minigame.minigame.startAnnounceGo) broadcastGo(entity)
                // hand off to start system which teleports/assigns kits if teleport-before wasn't
                // used
                minigame.state =
                    MinigameState.STARTING // keep in STARTING; start system will push to ONGOING
                // Set to 0 to prevent re-announcing
                minigame.remainingCountdownSeconds = 0
            }
        }
    }

    private fun broadcastCountdown(entity: Entity, secondsLeft: Int) {
        val minigame = entity[MinigameComponent]
        minigame.playerEntities.forEach { pEntity ->
            val player = pEntity[PlayerComponent].player
            val title =
                Title.title(
                    lang.t("messages.minigames.countdown.seconds") { "seconds" to secondsLeft },
                    lang.t("messages.minigames.countdown.prep"),
                    Title.Times.times(
                        Duration.ofMillis(150),
                        Duration.ofMillis(700),
                        Duration.ofMillis(150),
                    ),
                )
            player.showTitle(title)
            if (minigame.minigame.startSoundEachSecond) {
                player.playSound(
                    Sound.sound(
                        Key.key("minecraft:block.note_block.hat"),
                        Sound.Source.MASTER,
                        1f,
                        1.8f,
                    )
                )
            }
        }
    }

    private fun broadcastGo(entity: Entity) {
        val minigame = entity[MinigameComponent]
        minigame.playerEntities.forEach { pEntity ->
            val player = pEntity[PlayerComponent].player
            val title =
                Title.title(
                    lang.t("messages.minigames.countdown.go"),
                    lang.t("messages.minigames.countdown.good_luck"),
                    Title.Times.times(
                        Duration.ofMillis(150),
                        Duration.ofMillis(650),
                        Duration.ofMillis(150),
                    ),
                )
            player.showTitle(title)
            player.playSound(
                Sound.sound(
                    Key.key("minecraft:block.note_block.bell"),
                    Sound.Source.MASTER,
                    1f,
                    1.2f,
                )
            )
        }
    }
}
