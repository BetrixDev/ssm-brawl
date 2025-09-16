package dev.betrix.superSmashMobsBrawl.minigames.managers

import dev.betrix.superSmashMobsBrawl.Manageable
import dev.betrix.superSmashMobsBrawl.minigames.BrawlMinigame
import gg.flyte.twilight.scheduler.TwilightRunnable
import gg.flyte.twilight.scheduler.repeatingTask

interface ICountdownManager {
    /**
     * Starts a countdown timer with the given duration.
     * 
     * @param seconds Duration in seconds (must be >= 0)
     * @param onTick Callback invoked each second with remaining seconds
     * @param onComplete Callback invoked when countdown reaches zero
     * @return A cancellable handle that can be used to stop the countdown, or null if failed to start
     * @throws IllegalArgumentException if seconds < 0
     */
    fun startCountdown(seconds: Int, onTick: (secondsLeft: Int) -> Unit, onComplete: () -> Unit): TwilightRunnable?
}

class DefaultCountdownManager(private val minigame: BrawlMinigame) :
    Manageable(), ICountdownManager {
    
    private var currentCountdown: TwilightRunnable? = null
    
    override fun startCountdown(
        seconds: Int,
        onTick: (secondsLeft: Int) -> Unit,
        onComplete: () -> Unit,
    ): TwilightRunnable? {
        // Validate input
        require(seconds >= 0) { "Countdown seconds must be >= 0, got: $seconds" }
        
        // Cancel any existing countdown to prevent overlapping
        currentCountdown?.cancel()
        currentCountdown = null
        
        // Create new countdown task
        var remainingSeconds = seconds
        val countdownTask = repeatingTask(20) { // 20 ticks = 1 second
            if (remainingSeconds > 0) {
                onTick(remainingSeconds)
                remainingSeconds--
            } else {
                onComplete()
                currentCountdown = null
                this.cancel()
            }
        }
        
        currentCountdown = countdownTask
        runnables.add(countdownTask)
        
        return countdownTask
    }
}
