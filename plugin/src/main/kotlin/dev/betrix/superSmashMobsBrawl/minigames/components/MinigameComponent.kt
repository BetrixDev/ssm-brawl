package dev.betrix.superSmashMobsBrawl.minigames.components

import dev.betrix.superSmashMobsBrawl.Manageable
import dev.betrix.superSmashMobsBrawl.minigames.BrawlMinigame

abstract class MinigameComponent : Manageable() {
    protected lateinit var minigame: BrawlMinigame<*>
        private set

    protected fun setMinigame(minigame: BrawlMinigame<*>) {
        this.minigame = minigame
    }
}