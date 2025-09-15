package dev.betrix.superSmashMobsBrawl.minigames.managers

import dev.betrix.superSmashMobsBrawl.Manageable
import dev.betrix.superSmashMobsBrawl.minigames.BrawlMinigame

interface ICountdownManager {
    fun startCountdown(seconds: Int, onTick: (secondsLeft: Int) -> Unit, onComplete: () -> Unit)
}

class DefaultCountdownManager(private val minigame: BrawlMinigame) :
    Manageable(), ICountdownManager {
    override fun startCountdown(
        seconds: Int,
        onTick: (secondsLeft: Int) -> Unit,
        onComplete: () -> Unit,
    ) {
        // Skeleton; will use scheduler later
    }
}
