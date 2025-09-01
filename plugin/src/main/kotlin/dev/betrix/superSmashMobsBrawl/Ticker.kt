package dev.betrix.superSmashMobsBrawl

class Ticker(private val maxValue: Int) {
    private var currentValue = 0

    fun nextTick(): Int {
        if (currentValue >= maxValue) {
            currentValue = 0
        }

        return currentValue++
    }

    fun nextPercent(): Double = nextTick().toDouble() / maxValue.toDouble()
}