package dev.betrix.superSmashMobsBrawl.systems

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import com.github.quillraven.fleks.World.Companion.inject
import com.github.quillraven.fleks.collection.EntityBag
import dev.betrix.superSmashMobsBrawl.components.*
import dev.betrix.superSmashMobsBrawl.services.LangService

/**
 * System will handle all scenarios when a player issues the `/leave` command
 */
class PlayerLeaveSystem(private val lang: LangService = inject()) : IteratingSystem(
    family { all(PlayerComponent, PlayerTryLeaveComponent) }
) {
    private val queuedEntities = family {
        all(InQueueComponent, PlayerComponent)
    }

    private val inMinigameEntities = family {
        all(InMinigameComponent, PlayerComponent)
    }

    private val partiedEntities = family {
        all(InPartyComponent, PlayerComponent)
    }

    private val parties = family {
        all(PartyComponent)
    }

    private val minigames = family {
        all(MinigameComponent)
    }

    override fun onTickEntity(entity: Entity) {
        val player = entity[PlayerComponent].player
        val specifier = entity[PlayerTryLeaveComponent].specifier

        val isInQueue = queuedEntities.contains(entity)
        val isInMinigame = queuedEntities.contains(entity)
        val isInParty = queuedEntities.contains(entity)

        if (isInQueue || specifier == LeaveSpecifier.QUEUE) {
            if (specifier == LeaveSpecifier.QUEUE && !isInQueue) {
                player.sendMessage(lang.t("messages.leave.queue.notInAQueue"))
                return
            }

            val minigameId = queuedEntities.find { it == entity }!![InQueueComponent].minigameId

            if (isInParty) {
                val partyId = partiedEntities.find { it == entity }!![InPartyComponent].partyId
                val partyEntity = parties.find { it[PartyComponent].partyId == partyId }
                val party = partyEntity?.getOrNull(PartyComponent)

                if (party == null) {
                    entity.configure {
                        it -= InPartyComponent
                    }
                } else {
                    if (party.isLeader(entity)) {
                        getPartyMembers(partyEntity).forEach { partyMember ->
                            partyMember.configure {
                                it -= InQueueComponent
                            }

                            val partyMemberPlayer = partyMember[PlayerComponent].player

                            partyMemberPlayer.sendMessage(lang.t("messages.leave.queue.party.leaderLeft") {
                                "leaderName" to player.name
                                "minigameId" to minigameId
                            })
                        }
                    } else {
                        player.sendMessage(lang.t("messages.leave.queue.onlyLeaderCanLeave"))
                    }
                }
            }

            entity.configure {
                it -= InQueueComponent
                it -= PlayerTryLeaveComponent
            }

            player.sendMessage(lang.t("messages.leave.queue.success") { "minigameId" to minigameId })

            return
        } else if (isInParty || specifier == LeaveSpecifier.PARTY || specifier == LeaveSpecifier.DISBAND) {
            if (specifier == LeaveSpecifier.DISBAND) {
                if (!isInParty) {
                    player.sendMessage(lang.t("messages.leave.party.notInParty"))
                    return
                }

                val partyId = partiedEntities.find { it == entity }!![InPartyComponent].partyId
                val partyEntity = parties.find { it[PartyComponent].partyId == partyId }
                val party = partyEntity?.getOrNull(PartyComponent)

                if (party == null) {
                    entity.configure {
                        it -= InPartyComponent
                    }
                } else {
                    val partyLeaderName = party.leader[PlayerComponent].player.name

                    if (party.leader != entity) {
                        player.sendMessage(lang.t("message.leave.party.onlyLeaderCanDisband") { "leaderName" to partyLeaderName })
                        return
                    }

                    getPartyMembers(partyEntity).forEach { partyMember ->
                        partyMember.configure {
                            it -= InPartyComponent
                        }

                        val partyMemberPlayer = partyMember[PlayerComponent].player

                        partyMemberPlayer.sendMessage(lang.t("messages.leave.queue.party.leaderDisbanded") {
                            "leaderName" to partyLeaderName
                        })
                    }

                    partyEntity.remove()
                }
            } else if (specifier == LeaveSpecifier.PARTY) {
                if (!isInParty) {
                    player.sendMessage(lang.t("messages.leave.party.notInParty"))
                    return
                }

                val partyId = partiedEntities.find { it == entity }!![InPartyComponent].partyId
                val partyEntity = parties.find { it[PartyComponent].partyId == partyId }
                val party = partyEntity?.getOrNull(PartyComponent)

                if (party == null) {
                    entity.configure {
                        it -= InPartyComponent
                    }
                } else {
                    if (party.leader == entity) {
                        player.sendMessage(lang.t("messages.leave.party.leaderMustDisband"))
                        return
                    }

                    if (getPartyMembers(partyEntity).size > 1) {
                        player.sendMessage(lang.t("messages.leave.party.tooManyPlayers"))
                        return
                    }

                    entity.configure {
                        it -= InPartyComponent
                    }

                    if (getPartyMembers(partyEntity).isEmpty()) {
                        partyEntity.remove()
                    }

                    player.sendMessage(lang.t("messages.leave.party.success"))
                }
            }
        }

        entity.configure {
            it -= PlayerTryLeaveComponent
        }
    }

    private fun getPartyMembers(partyEntity: Entity): EntityBag {
        val partyId = partyEntity[PartyComponent].partyId

        return partiedEntities.filter {
            it.getOrNull(InPartyComponent)?.partyId == partyId
        }
    }
}