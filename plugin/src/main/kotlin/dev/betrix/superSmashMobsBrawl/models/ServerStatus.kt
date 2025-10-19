package dev.betrix.superSmashMobsBrawl.models

import kotlinx.serialization.Serializable

@Serializable
data class ServerStatus(val playerCount: Int, val tps: Double, val memoryUsageMb: Long, val loadedChunks: Int, val loadedWorlds: Int, val averagePlayerPing: Int)
