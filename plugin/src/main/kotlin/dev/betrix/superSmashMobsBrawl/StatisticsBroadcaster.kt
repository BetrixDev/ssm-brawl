package dev.betrix.superSmashMobsBrawl

import dev.betrix.superSmashMobsBrawl.extensions.logJson
import dev.betrix.superSmashMobsBrawl.extensions.ticks
import dev.betrix.superSmashMobsBrawl.services.HubService
import gg.flyte.twilight.extension.round
import gg.flyte.twilight.scheduler.repeatingTask
import java.lang.management.ManagementFactory
import kotlin.time.Duration.Companion.minutes
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class StatisticsBroadcaster : KoinComponent {

    private val plugin: SuperSmashMobsBrawl by inject()
    private val hubService: HubService by inject()

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
    }

    private fun bytesToMb(bytes: Long?): Double? {
        if (bytes == null) {
            return null
        }

        val bytesInGigabyte = 1024.0 * 1024.0
        return (bytes / bytesInGigabyte).round(2).toDouble()
    }
}
