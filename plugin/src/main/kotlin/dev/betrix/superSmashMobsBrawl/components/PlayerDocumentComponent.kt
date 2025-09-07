package dev.betrix.superSmashMobsBrawl.components

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType

data class PlayerDocumentComponent(
    var level: Int = 1,
    var experience: Long = 0L,
    var gems: Long = 0L,
    var unlockedCosmetics: MutableSet<String> = mutableSetOf(),
    var selectedKitId: String = "creeper",
    var selectedCosmetics: MutableMap<String, String> = mutableMapOf(),
    var statistics: PersistentStatistics = PersistentStatistics(),
    var isDirty: Boolean = true,
) : Component<PlayerDocumentComponent> {
    override fun type() = PlayerDocumentComponent

    companion object : ComponentType<PlayerDocumentComponent>()
}

data class PersistentStatistics(
    var totalGamesPlayed: Int = 0,
    var totalGamesWon: Int = 0,
    var totalKills: Int = 0,
    var totalDeaths: Int = 0,
    var totalPlayTime: Long = 0L,
    var bestKillStreak: Int = 0,
    var favoriteKit: String = "default",
    var lastLoginTime: Long = System.currentTimeMillis(),
)
