package net.ssmb.minigames

import com.github.shynixn.mccoroutine.bukkit.launch
import com.github.shynixn.mccoroutine.bukkit.registerSuspendingEvents
import com.github.shynixn.mccoroutine.bukkit.ticks
import io.papermc.paper.entity.LookAnchor
import java.time.Duration
import kotlinx.coroutines.delay
import net.kyori.adventure.key.Key
import net.kyori.adventure.sound.Sound
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.title.Title
import net.ssmb.SSMB
import net.ssmb.dtos.minigame.MinigameEndRequest
import net.ssmb.dtos.minigame.MinigameStartSuccess
import net.ssmb.enums.MinigameState
import net.ssmb.events.BrawlAbilityUseEvent
import net.ssmb.events.BrawlDamageEvent
import net.ssmb.events.BrawlDamageType
import net.ssmb.events.BrawlRespawnEvent
import net.ssmb.extensions.doKnockback
import net.ssmb.kits.IKit
import net.ssmb.kits.constructKitFromData
import net.ssmb.models.RecordedMinigameAbilityUseEvent
import net.ssmb.models.RecordedMinigameEvent
import net.ssmb.utils.Atom
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.util.Vector

class TwoPlayerSinglesMinigame(
    override val players: List<Player>,
    private val minigameData: MinigameStartSuccess
) : IMinigame, Listener {
    private val plugin = SSMB.instance
    private val minigameState = Atom(MinigameState.LOADING)
    private lateinit var minigameWorld: World
    private var startedAt = System.currentTimeMillis()
    override val playerKits = hashMapOf<Player, IKit>()
    override val teamsStocks = arrayListOf<Pair<ArrayList<Player>, Int>>()
    private val playersLeftInProgress = listOf<Player>()

    private val events = arrayListOf<RecordedMinigameEvent>()

    init {
        plugin.server.pluginManager.registerSuspendingEvents(this, plugin)
    }

    override suspend fun initializeMinigame() {
        minigameState.subscribe { state, _ ->
            when (state) {
                MinigameState.LOADING -> doMinigameLoading()
                MinigameState.COUNTDOWN -> doMinigameCountdown()
                MinigameState.RUNNING -> doMinigameRunning()
                MinigameState.ENDING -> doMinigameEnd()
                else -> throw IllegalStateException("Invalid minigame state")
            }
        }
    }

    override fun removePlayer(player: Player) {
        val playerKit = playerKits[player]
        playerKit?.destroyKit()
    }

    private fun doMinigameLoading() {
        minigameWorld = plugin.worlds.createSsmbWorld(minigameData.map.id, minigameData.gameId)

        players.forEachIndexed { idx, it ->
            val spawnCoords = minigameData.map.spawnPoints[idx]
            val tpLocation = Location(minigameWorld, spawnCoords.x, spawnCoords.y, spawnCoords.z)
            it.teleport(tpLocation)

            it.walkSpeed = 0.0f
            it.lookAt(minigameWorld.spawnLocation, LookAnchor.EYES)

            val playerData =
                minigameData.players.find { itt -> itt.uuid == it.uniqueId.toString() }!!
            val kit = constructKitFromData(it, playerData.selectedKit, this)
            kit.initializeKit()

            teamsStocks.add(Pair(arrayListOf(it), minigameData.minigame.stocks))

            playerKits[it] = kit
        }

        minigameState.set(MinigameState.COUNTDOWN)
    }

    private fun doMinigameCountdown() {
        val titleTimes =
            Title.Times.times(
                Duration.ofMillis(100),
                Duration.ofMillis(500),
                Duration.ofMillis(100)
            )

        plugin.launch {
            repeat(5) {
                val title =
                    Title.title(
                        Component.text("Starting in ${5 - it}", NamedTextColor.GOLD),
                        Component.empty(),
                        titleTimes
                    )

                minigameWorld.showTitle(title)
                minigameWorld.playSound(
                    Sound.sound(
                        Key.key("block.note_block.hat"),
                        Sound.Source.AMBIENT,
                        1f,
                        1 * (1 + 1 / 5).toFloat()
                    )
                )

                delay(20.ticks)
            }

            val startTitle =
                Title.title(
                    Component.text("Start!", NamedTextColor.GOLD),
                    Component.empty(),
                    titleTimes
                )

            minigameWorld.showTitle(startTitle)
            minigameWorld.playSound(
                Sound.sound(Key.key("block.note_block.chime"), Sound.Source.AMBIENT, 1f, 2f)
            )

            minigameState.set(MinigameState.RUNNING)
        }
    }

    private fun doMinigameRunning() {
        startedAt = System.currentTimeMillis()

        players.forEach { it.walkSpeed = 0.2f }
    }

    private fun doMinigameEnd() {
        val endedAt = System.currentTimeMillis()

        val winningPlayers = teamsStocks.first { it.second > 0 }.first
        val losingPlayers = teamsStocks.filter { it.second == 0 }.flatMap { it.first }

        winningPlayers.forEach { it.sendMessage(Component.text("You won!", NamedTextColor.GREEN)) }

        losingPlayers.forEach { it.sendMessage(Component.text("You lost!", NamedTextColor.RED)) }

        plugin.launch {
            delay(5)

            winningPlayers.forEach { it.teleport(plugin.hub.spawnLocation) }

            losingPlayers.forEach { it.teleport(plugin.hub.spawnLocation) }
        }

        plugin
            .launch {
                val winningPlayerUuids = winningPlayers.map { it.uniqueId.toString() }

                val allPlayers = winningPlayers.toMutableList()
                allPlayers.addAll(losingPlayers)

                val playerEntries =
                    allPlayers.map { plr ->
                        val playerKitData = playerKits[plr]!!
                        val stocksLeft = teamsStocks.find { it.first.contains(plr) }?.second ?: 0
                        val didLeaveInProgress = playersLeftInProgress.contains(plr)

                        val abilityUsage =
                            events
                                .filter { it is RecordedMinigameAbilityUseEvent && it.actor == plr }
                                .map {
                                    val event = it as RecordedMinigameAbilityUseEvent
                                    MinigameEndRequest.PlayerEntry.KitEntry.AbilityUsageEntry(
                                        event.abilityId,
                                        event.dateRecorded,
                                        event.damageDealt
                                    )
                                }

                        val kitEntry =
                            MinigameEndRequest.PlayerEntry.KitEntry(
                                playerKitData.kitData.id,
                                startedAt,
                                endedAt,
                                abilityUsage
                            )

                        MinigameEndRequest.PlayerEntry(
                            plr.uniqueId.toString(),
                            stocksLeft,
                            didLeaveInProgress,
                            listOf(kitEntry)
                        )
                    }

                val endRequest =
                    MinigameEndRequest(
                        minigameData.gameId,
                        minigameData.map.id,
                        minigameData.minigame.id,
                        winningPlayerUuids,
                        playerEntries
                    )

                plugin.api.minigameEnd(endRequest)
            }
            .invokeOnCompletion { plugin.minigames.onMinigameEnd(this) }
    }

    private fun checkShouldMinigameEnd() {
        val teamsWithStocksLeft = teamsStocks.filter { it.second > 0 }

        if (teamsWithStocksLeft.size == 1) {
            minigameState.set(MinigameState.ENDING)
        }
    }

    @EventHandler
    suspend fun onPlayerDeath(event: PlayerDeathEvent) {
        val player = event.entity

        if (!players.contains(player)) return

        event.isCancelled = true

        playerKits[player]!!.destroyKit()

        // TODO: use db coords
        val spectatorSpawnCoords = minigameWorld.spawnLocation.add(Vector(0.0, 35.0, 0.0))
        player.gameMode = GameMode.SPECTATOR
        player.teleport(spectatorSpawnCoords)

        val teamStocks = teamsStocks.find { it.first.contains(player) }!!

        // Pairs are immutable, so we create a new pair and override the existing using map
        teamsStocks.map {
            if (it.first.contains(player)) {
                Pair(it.first, it.second - 1)
            } else {
                it
            }
        }

        if (teamStocks.second == 0) {
            checkShouldMinigameEnd()

            teamStocks.first.forEach {
                it.sendMessage(Component.text("You are dead!", NamedTextColor.RED))
            }
        } else {
            plugin.launch {
                repeat(5) {
                    player.sendMessage(
                        Component.text("You died! You will respawn in ${5 - it} seconds")
                    )
                    delay(1.ticks)
                }

                val spawnCoords = minigameData.map.spawnPoints.random()
                val tpLocation =
                    Location(minigameWorld, spawnCoords.x, spawnCoords.y, spawnCoords.z)
                player.teleport(tpLocation)

                playerKits[player]!!.initializeKit()

                BrawlRespawnEvent(player).callEvent()
            }
        }
    }

    @EventHandler
    fun onBrawlAbilityUse(event: BrawlAbilityUseEvent) {
        if (!players.contains(event.player)) return

        events.add(
            RecordedMinigameAbilityUseEvent(
                System.currentTimeMillis(),
                event.player,
                event.abilityId,
                event.damageDealt
            )
        )
    }

    @EventHandler
    fun onBrawlRespawn(event: BrawlRespawnEvent) {
        if (!players.contains(event.player)) return

        // TODO: log player respawn for game logs
    }

    @EventHandler
    fun onBrawlDamage(event: BrawlDamageEvent) {
        if (!players.contains(event.victim) || !players.contains(event.attacker)) return

        val victimStartingHealth = event.victim.health

        if (event.damageType == BrawlDamageType.MELEE) {
            val victimKit = playerKits[event.victim]!!

            event.victim.damage(event.damage, event.attacker)
            event.victim.doKnockback(
                victimKit.kitData.knockbackMult,
                event.damage,
                victimStartingHealth,
                event.attacker.location.toVector(),
                null
            )
        }
    }
}
