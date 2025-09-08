package dev.betrix.superSmashMobsBrawl.systems

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import com.github.quillraven.fleks.World.Companion.inject
import com.github.quillraven.fleks.collection.EntityBag
import dev.betrix.superSmashMobsBrawl.components.*
import dev.betrix.superSmashMobsBrawl.extensions.sendErrorMessage
import dev.betrix.superSmashMobsBrawl.extensions.sendSuccessMessage
import dev.betrix.superSmashMobsBrawl.models.brawlData.FfaMinigameDef
import dev.betrix.superSmashMobsBrawl.models.brawlData.TeamBasedStocksMinigameDef
import dev.betrix.superSmashMobsBrawl.services.LangService

class QueueJoinSystem(private val lang: LangService = inject()) :
    IteratingSystem(family { all(PlayerTryQueueComponent, PlayerComponent) }) {
    private val queuedEntities = family { all(InQueueComponent) }

    private val entitiesInMinigame = family { all(InMinigameComponent) }

    private val partiedEntities = family { all(InPartyComponent) }

    private val parties = family { all(PartyComponent) }

    override fun onTickEntity(entity: Entity) {
        val player = entity[PlayerComponent].player
        val minigame = entity[PlayerTryQueueComponent].minigame

        if (queuedEntities.contains(entity)) {
            player.sendErrorMessage(
                lang.t("messages.queue.join.alreadyInQueue") {
                    "minigameId" to entity[InQueueComponent].minigame.id
                }
            )

            entity.configure { it -= PlayerTryQueueComponent }

            return
        }

        if (entitiesInMinigame.contains(entity)) {
            player.sendErrorMessage(
                lang.t("messages.queue.join.alreadyInMinigame") {
                    "minigameId" to
                        entity[InMinigameComponent].minigameEntity[MinigameComponent].minigame.id
                }
            )

            entity.configure { it -= PlayerTryQueueComponent }

            return
        }

        if (partiedEntities.contains(entity)) {
            val partyId = partiedEntities.find { it == entity }!![InPartyComponent].partyId
            val partyEntity = parties.find { it[PartyComponent].partyId == partyId }

            if (partyEntity == null) {
                entity.configure { it -= InPartyComponent }
            } else {
                val party = partyEntity[PartyComponent]

                if (!party.isLeader(entity)) {
                    val leaderName = party.leader[PlayerComponent].player.name
                    player.sendErrorMessage(
                        lang.t("messages.queue.join.party.onlyLeader") {
                            "leaderName" to leaderName
                        }
                    )

                    entity.configure { it -= PlayerTryQueueComponent }
                    return
                }

                // Disallow parties for ranked modes regardless of allowParties
                if (minigame.isRanked) {
                    player.sendErrorMessage(
                        lang.t("messages.queue.join.party.notAllowedRanked") {
                            "minigameId" to minigame.id
                        }
                    )

                    entity.configure { it -= PlayerTryQueueComponent }
                    return
                }

                if (!minigame.allowParties) {
                    player.sendErrorMessage(
                        lang.t("messages.queue.join.party.notAllowed") {
                            "minigameId" to minigame.id
                        }
                    )

                    entity.configure { it -= PlayerTryQueueComponent }
                    return
                }

                val partyMembers = getPartyMembers(partyEntity)
                val capacity =
                    when (minigame) {
                        is TeamBasedStocksMinigameDef ->
                            minigame.playersPerTeam * minigame.amountOfTeams
                        is FfaMinigameDef -> minigame.maxPlayers
                        else -> Int.MAX_VALUE
                    }

                if (partyMembers.size > capacity) {
                    player.sendErrorMessage(
                        lang.t("messages.queue.join.party.tooLarge") {
                            "minigameId" to minigame.id
                            "max" to capacity
                            "size" to partyMembers.size
                        }
                    )

                    entity.configure { it -= PlayerTryQueueComponent }
                    return
                }

                val memberInQueue = partyMembers.firstOrNull { queuedEntities.contains(it) }
                if (memberInQueue != null) {
                    val name = memberInQueue[PlayerComponent].player.name
                    player.sendErrorMessage(
                        lang.t("messages.queue.join.party.memberInQueue") { "playerName" to name }
                    )

                    entity.configure { it -= PlayerTryQueueComponent }
                    return
                }

                val memberInGame = partyMembers.firstOrNull { entitiesInMinigame.contains(it) }
                if (memberInGame != null) {
                    val name = memberInGame[PlayerComponent].player.name
                    player.sendErrorMessage(
                        lang.t("messages.queue.join.party.memberInMinigame") {
                            "playerName" to name
                        }
                    )

                    entity.configure { it -= PlayerTryQueueComponent }
                    return
                }

                // Queue the entire party
                partyMembers.forEach { member ->
                    member.configure { it += InQueueComponent(minigame) }

                    if (member != entity) {
                        val memberPlayer = member[PlayerComponent].player
                        memberPlayer.sendSuccessMessage(
                            lang.t("messages.queue.join.party.queuedByLeader") {
                                "leaderName" to player.name
                                "minigameId" to minigame.id
                            }
                        )
                    }
                }

                // Finalize leader handling and stop further solo-queue logic
                entity.configure { it -= PlayerTryQueueComponent }
                player.sendSuccessMessage(
                    lang.t("messages.queue.join.success") { "minigameId" to minigame.id }
                )
                return
            }
        }

        entity.configure {
            it -= PlayerTryQueueComponent
            it += InQueueComponent(minigame)
        }

        player.sendSuccessMessage(
            lang.t("messages.queue.join.success") { "minigameId" to minigame.id }
        )
    }

    private fun getPartyMembers(partyEntity: Entity): EntityBag {
        val partyId = partyEntity[PartyComponent].partyId
        return partiedEntities.filter { it.getOrNull(InPartyComponent)?.partyId == partyId }
    }
}
