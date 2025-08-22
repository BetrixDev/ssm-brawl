package dev.betrix.superSmashMobsBrawl.minigames.components

import dev.betrix.superSmashMobsBrawl.minigames.components.scoreboard.MinigameScoreboardManager
import dev.betrix.superSmashMobsBrawl.minigames.components.scoreboard.impl.DefaultScoreboardManager
import dev.betrix.superSmashMobsBrawl.models.brawlData.MinigameDef

object MinigameComponentFactory {

    fun createScoreboardManager(minigameDef: MinigameDef): MinigameScoreboardManager {
        return DefaultScoreboardManager()
    }

//    fun createScoreboardManager(minigameDef: MinigameDef): MinigameScoreboardManager {
//        return when (minigameDef) {
//            is FfaMinigameDef -> FfaScoreboardManager()
//            is TeamBasedStocksMinigameDef -> TeamStocksScoreboardManager()
//            // Add more as needed
//            else -> DefaultScoreboardManager()
//        }
//    }
//
//    fun createTeleportationManager(minigameDef: MinigameDef): MinigameTeleportationManager {
//        return when (minigameDef) {
//            is FfaMinigameDef -> FfaTeleportationManager()
//            is TeamBasedStocksMinigameDef -> TeamBasedTeleportationManager()
//            else -> DefaultTeleportationManager()
//        }
//    }
//
//    fun createKitManager(minigameDef: MinigameDef): MinigameKitManager {
//        // Check for kit overrides in minigame definition
//        val hasKitOverrides = minigameDef.overrides?.kits?.isNotEmpty() == true
//
//        return when {
//            hasKitOverrides -> OverrideKitManager(minigameDef.overrides!!)
//            minigameDef is FfaMinigameDef && minigameDef.allowKitSwitching ->
//                SwitchableKitManager()
//            else -> DefaultKitManager()
//        }
//    }
}