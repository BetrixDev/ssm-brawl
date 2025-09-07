package dev.betrix.superSmashMobsBrawl.systems

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import com.github.quillraven.fleks.World.Companion.inject
import dev.betrix.superSmashMobsBrawl.components.*
import dev.betrix.superSmashMobsBrawl.extensions.sendErrorMessage
import dev.betrix.superSmashMobsBrawl.services.LangService

class QueueJoinSystem(private val lang: LangService = inject()) :
    IteratingSystem(family { all(PlayerTryQueueComponent, PlayerComponent) }) {
    private val queuedEntities = family { all(InQueueComponent) }

    private val partiedEntities = family { all(InPartyComponent) }

    private val parties = family { all(PartyComponent) }

    override fun onTickEntity(entity: Entity) {
        val player = entity[PlayerComponent].player

        if (queuedEntities.contains(entity)) {
            player.sendErrorMessage(
                lang.t("messages.queue.join.alreadyInQueue") {
                    "minigameId" to entity[InQueueComponent].minigame.id
                }
            )
            return
        }

        if (partiedEntities.contains(entity)) {
            val partyId = partiedEntities.find { it == entity }!![InPartyComponent].partyId
            val partyEntity = parties.find { it[PartyComponent].partyId == partyId }

            if (partyEntity == null) {
                entity.configure { it -= InPartyComponent }
            } else {
                TODO(
                    "check for party leader, if leader see if party can queue for minigame, if not leader, don't let queue at all"
                )
            }
        }

        TODO(
            "Handle adding player to queue. Make sure to remove the PlayerTryQueueComponent component"
        )
    }
}
