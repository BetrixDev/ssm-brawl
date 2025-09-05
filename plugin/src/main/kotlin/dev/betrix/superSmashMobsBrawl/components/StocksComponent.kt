package dev.betrix.superSmashMobsBrawl.components

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType

data class StocksComponent(var remainingStocks: Int, val initialStocks: Int) :
    Component<StocksComponent> {
    override fun type() = StocksComponent

    companion object : ComponentType<StocksComponent>()

    fun hasStocksRemaining(): Boolean = remainingStocks > 0

    fun useStock(): Boolean {
        return if (remainingStocks > 0) {
            remainingStocks--
            true
        } else false
    }
}
