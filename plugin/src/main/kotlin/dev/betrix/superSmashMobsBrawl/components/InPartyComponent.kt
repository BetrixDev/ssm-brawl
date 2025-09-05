package dev.betrix.superSmashMobsBrawl.components

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType

data class InPartyComponent(val partyId: String, val joinTime: Long = System.currentTimeMillis()) :
    Component<InPartyComponent> {
    override fun type() = InPartyComponent

    companion object : ComponentType<InPartyComponent>()
}
