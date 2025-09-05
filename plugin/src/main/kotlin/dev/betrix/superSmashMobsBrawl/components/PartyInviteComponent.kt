package dev.betrix.superSmashMobsBrawl.components

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import org.bukkit.entity.Entity

data class PartyInviteComponent(
    val inviter: Entity,
    val partyId: String,
    val expiryTime: Long = System.currentTimeMillis() + 60000L, // 60 second expiry
) : Component<PartyInviteComponent> {
    override fun type() = PartyInviteComponent

    companion object : ComponentType<PartyInviteComponent>()
}
