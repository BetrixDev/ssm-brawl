package dev.betrix.superSmashMobsBrawl

import com.github.shynixn.mccoroutine.bukkit.launch
import dev.betrix.superSmashMobsBrawl.extensions.logJson
import dev.betrix.superSmashMobsBrawl.extensions.ticks
import dev.betrix.superSmashMobsBrawl.models.ServerStatus
import dev.betrix.superSmashMobsBrawl.services.ApiService
import dev.betrix.superSmashMobsBrawl.services.HubService
import gg.flyte.twilight.extension.round
import gg.flyte.twilight.scheduler.repeatingTask
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.lang.management.ManagementFactory
import kotlin.time.Duration.Companion.minutes
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject
import org.bukkit.Bukkit
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class StatisticsBroadcaster : KoinComponent {

    private val plugin: SuperSmashMobsBrawl by inject()
    private val hubService: HubService by inject()
    private val api: ApiService by inject()

    init {
        repeatingTask(5.minutes.ticks) {
            val tps = plugin.server.tps
            val mspt = plugin.server.averageTickTime

            val osBean =
                ManagementFactory.getOperatingSystemMXBean()
                    as? com.sun.management.OperatingSystemMXBean

            val freeMemory = osBean?.freeMemorySize
            val totalMemory = osBean?.totalMemorySize
            val cpuCores = osBean?.availableProcessors

            plugin.logger.logJson(
                buildJsonObject {
                    put("currentPlayerCount", plugin.server.onlinePlayers.size)
                    put("playersInHub", hubService.playersInHubCount())
                    putJsonObject("tps") {
                        put("1m", tps[0])
                        put("5m", tps[1])
                    }
                    put("mspt", mspt)
                    put("memoryUsageMb", bytesToMb(totalMemory?.minus(freeMemory ?: 0)))
                    put("totalMemoryMb", bytesToMb(totalMemory))
                    put("availableCpuCores", cpuCores)
                }
            )
        }

        repeatingTask(1.minutes.ticks) {
            val serverStatus = collectServerStatus()

            plugin.launch {
                withContext(Dispatchers.IO) {
                    api.serverStatusPostAsync(serverStatus)
                }
            }
        }

        repeatingTask(10.minutes.ticks) {
            val onlinePlayers = Bukkit.getOnlinePlayers().map { it.uniqueId.toString() }

            plugin.launch {
                withContext(Dispatchers.IO) {
                    api.serverSyncPlayerOnlineStatus(onlinePlayers)
                }
            }
        }
    }

    private fun bytesToMb(bytes: Long?): Double? {
        if (bytes == null) {
            return null
        }

        val bytesInGigabyte = 1024.0 * 1024.0
        return (bytes / bytesInGigabyte).round(2).toDouble()
    }

    private fun collectServerStatus(): ServerStatus {
        val runtime = Runtime.getRuntime()
        val memoryUsageMb = (runtime.totalMemory() - runtime.freeMemory()) / 1048576

        val onlinePlayers = Bukkit.getOnlinePlayers()
        val averagePlayerPing = if (onlinePlayers.isEmpty()) {
            0
        } else {
            onlinePlayers.sumOf { it.ping } / onlinePlayers.size
        }

        val worlds = Bukkit.getWorlds()

        return ServerStatus(
            playerCount = onlinePlayers.size,
            tps = Bukkit.getTPS()[0], // 1-minute average
            memoryUsageMb = memoryUsageMb,
            loadedChunks = worlds.sumOf { it.loadedChunks.size },
            loadedWorlds = worlds.size,
            averagePlayerPing = averagePlayerPing
        )
    }
}
