package dev.betrix.superSmashMobsBrawl.minigames

sealed class DeathDecision {
    data class Respawn(val delaySeconds: Int?) : DeathDecision()
    data object Eliminate : DeathDecision()
}