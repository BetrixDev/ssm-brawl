package dev.betrix.superSmashMobsBrawl.kits

import dev.betrix.superSmashMobsBrawl.abilities.BoneExplosionAbility
import dev.betrix.superSmashMobsBrawl.abilities.BrawlAbility
import dev.betrix.superSmashMobsBrawl.abilities.ExplodeAbility
import dev.betrix.superSmashMobsBrawl.abilities.RopedArrowAbility
import dev.betrix.superSmashMobsBrawl.abilities.SulphurBombAbility
import dev.betrix.superSmashMobsBrawl.disguises.BrawlDisguise
import dev.betrix.superSmashMobsBrawl.disguises.CreeperDisguise
import dev.betrix.superSmashMobsBrawl.disguises.SkeletonDisguise
import dev.betrix.superSmashMobsBrawl.extensions.sendDebugMessage
import dev.betrix.superSmashMobsBrawl.passives.ArrowRechargePassive
import dev.betrix.superSmashMobsBrawl.passives.BarragePassive
import dev.betrix.superSmashMobsBrawl.passives.BrawlPassive
import dev.betrix.superSmashMobsBrawl.passives.DoubleJumpPassive
import dev.betrix.superSmashMobsBrawl.passives.HungerPassive
import dev.betrix.superSmashMobsBrawl.passives.RegenerationPassive
import dev.betrix.superSmashMobsBrawl.services.DataService
import dev.betrix.superSmashMobsBrawl.services.MinigameService
import java.util.logging.Logger
import org.bukkit.entity.Player
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

open class BrawlKit(val id: String, val player: Player) : KoinComponent {
    private val minigameService: MinigameService by inject()
    private val dataService: DataService by inject()
    private val logger: Logger by inject()

    protected val minigameInstance = minigameService.getMinigameForPlayer(player)
    protected val kitData =
        dataService.getKit(id) ?: throw RuntimeException("No kit found in DataService with id $id")

    protected val abilities = arrayListOf<BrawlAbility>()
    protected val passives = arrayListOf<BrawlPassive>()

    protected var disguise: BrawlDisguise? = null

    fun getPassive(id: String): BrawlPassive? {
        return passives.find { it.id == id }
    }

    fun getAbility(id: String): BrawlAbility? {
        return abilities.find { it.id == id }
    }

    open fun setup() {
        disguise =
            when (kitData.disguiseId) {
                "creeper" -> CreeperDisguise(player)
                "skeleton" -> SkeletonDisguise(player)
                else -> {
                    logger.severe("No disguise known with id ${kitData.disguiseId}")
                    null
                }
            }

        logger.info(kitData.abilities.toString())

        kitData.abilities.forEach {
            logger.info(it.toString())
            abilities.add(
                when (it.id) {
                    "sulphur_bomb" -> SulphurBombAbility(player)
                    "explode" -> ExplodeAbility(player)
                    "roped_arrow" -> RopedArrowAbility(player)
                    "bone_explosion" -> BoneExplosionAbility(player)
                    else -> {
                        logger.severe(
                            "No ability found with id ${it.id} reference on kit ${kitData.id}"
                        )
                        return@forEach
                    }
                }
            )
        }

        kitData.passives.forEach {
            if (minigameInstance?.isPassiveValid(it.id) == false) {
                return@forEach
            }

            passives.add(
                when (it.id) {
                    "double_jump" -> DoubleJumpPassive(player)
                    "regeneration" -> RegenerationPassive(player)
                    "hunger" -> HungerPassive(player)
                    "arrow_recharge" -> ArrowRechargePassive(player)
                    "barrage" -> BarragePassive(player)
                    else -> {
                        logger.severe(
                            "No passive found with id ${it.id} reference on kit ${kitData.id}"
                        )
                        return@forEach
                    }
                }
            )
        }

        abilities.forEach { ability -> ability.setup() }
        passives.forEach { passive -> passive.setup() }
        disguise?.setup()

        player.sendDebugMessage("You have been given the $id kit")
    }

    open fun teardown() {
        disguise?.teardown()

        abilities.forEach { it.teardown() }
        abilities.clear()

        passives.forEach { it.teardown() }
        passives.clear()

        player.sendDebugMessage("The $id kit has been removed")
    }
}
