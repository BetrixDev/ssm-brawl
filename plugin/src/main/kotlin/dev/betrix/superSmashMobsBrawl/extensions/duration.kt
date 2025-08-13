package dev.betrix.superSmashMobsBrawl.extensions

import kotlin.time.Duration

val Duration.ticks: Long
    get() = this.inWholeMilliseconds / 50
