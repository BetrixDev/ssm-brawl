package dev.betrix.superSmashMobsBrawl.models

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World
import dev.betrix.superSmashMobsBrawl.components.PlayerComponent
import dev.betrix.superSmashMobsBrawl.components.StocksComponent
import dev.betrix.superSmashMobsBrawl.components.Tags
import net.kyori.adventure.audience.Audience

data class TeamData(
    val teamId: String,
    val teamName: String,
    val members: MutableList<Entity> = mutableListOf(),
    var isEliminated: Boolean = false,
    var stocks: Int = 0,
) {
    enum class MemberState {
        ANY,
        ALIVE,
        DEAD,
        ELIMINATED,
        DEAD_OR_ELIM,
    }

    fun getMemberAudience(world: World, memberState: MemberState = MemberState.ANY): Audience {
        with(world) {
            val players =
                members
                    .filter { member ->
                        when (memberState) {
                            MemberState.ANY -> true
                            MemberState.ALIVE ->
                                member hasNo Tags.DEAD && member hasNo Tags.ELIMINATED
                            MemberState.DEAD -> member has Tags.DEAD
                            MemberState.ELIMINATED -> member has Tags.ELIMINATED
                            MemberState.DEAD_OR_ELIM ->
                                member has Tags.DEAD || member has Tags.ELIMINATED
                        }
                    }
                    .map { it[PlayerComponent].player }

            return Audience.audience(players)
        }
    }

    fun getAliveMembers(world: World): List<Entity> {
        with(world) {
            return members.filter { member ->
                member hasNo Tags.DEAD && member hasNo Tags.ELIMINATED
            }
        }
    }

    fun useStock(world: World): Boolean {
        with(world) {
            val consumedMemberStocks =
                members.all { it.getOrNull(StocksComponent)?.useStock() == true }

            return if (consumedMemberStocks) {
                stocks--
                true
            } else {
                false
            }
        }
    }

    fun addMember(entity: Entity) {
        if (!members.contains(entity)) {
            members.add(entity)
        }
    }

    fun removeMember(entity: Entity) {
        members.remove(entity)
    }
}
