package dev.betrix.superSmashMobsBrawl.components

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import org.bukkit.entity.Entity

data class PartyComponent(
    val partyId: String,
    val members: MutableList<Entity> = mutableListOf(),
    var leader: Entity,
    var isPublic: Boolean = false,
    val maxSize: Int = 8,
    val createdTime: Long = System.currentTimeMillis(),
) : Component<PartyComponent> {
    override fun type() = PartyComponent

    companion object : ComponentType<PartyComponent>()

    fun isFull() = members.size >= maxSize

    fun isLeader(entity: Entity) = leader == entity
}
