package dev.betrix.superSmashMobsBrawl.components

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType

data class CosmeticComponent(val equippedCosmeticIds: MutableMap<String, String> = mutableMapOf()) :
    Component<CosmeticComponent> {
    override fun type() = CosmeticComponent

    companion object : ComponentType<CosmeticComponent>()
}
