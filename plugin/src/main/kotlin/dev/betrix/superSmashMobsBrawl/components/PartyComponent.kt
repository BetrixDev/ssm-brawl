package dev.betrix.superSmashMobsBrawl.components

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World

data class PartyComponent(
    val partyId: String,
    var leader: Entity,
    var isPublic: Boolean = false,
    val maxSize: Int = 8,
    val createdTime: Long = System.currentTimeMillis(),
) : Component<PartyComponent> {
    override fun type() = PartyComponent

    companion object : ComponentType<PartyComponent>()
    
    fun isLeader(entity: Entity) = leader == entity
}
