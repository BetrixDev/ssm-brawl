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
import gg.flyte.twilight.extension.feed
import gg.flyte.twilight.extension.heal
import java.util.logging.Logger
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.attribute.Attribute
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
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

    fun getMeleeDamage(): Double = kitData.meleeDamage

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

        kitData.abilities.forEach {
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

        player.gameMode = GameMode.SURVIVAL
        player.heal()
        player.feed()

        // Equip armor if specified
        kitData.armorItems?.let { armor ->
            val helmetMat = armor.helmet?.let { Material.matchMaterial(it.uppercase()) }
            val chestMat = armor.chestplate?.let { Material.matchMaterial(it.uppercase()) }
            val legsMat = armor.leggings?.let { Material.matchMaterial(it.uppercase()) }
            val bootsMat = armor.boots?.let { Material.matchMaterial(it.uppercase()) }

            helmetMat?.let { player.inventory.helmet = ItemStack.of(it) }
            chestMat?.let { player.inventory.chestplate = ItemStack.of(it) }
            legsMat?.let { player.inventory.leggings = ItemStack.of(it) }
            bootsMat?.let { player.inventory.boots = ItemStack.of(it) }
        }

        // 1.8 PVP feel adjustments
        try {
            // Set high attack speed to remove 1.9+ cooldown
            val attackSpeedAttr = Attribute.valueOf("GENERIC_ATTACK_SPEED")
            player.getAttribute(attackSpeedAttr)?.baseValue = 16.0
        } catch (_: Throwable) {}
        // Clear offhand to avoid shield mechanics
        try {
            player.inventory.setItemInOffHand(ItemStack.of(Material.AIR))
        } catch (_: Throwable) {}

        player.sendDebugMessage("You have been given the $id kit")
    }

    open fun teardown() {
        disguise?.teardown()

        abilities.forEach { it.teardown() }
        abilities.clear()

        passives.forEach { it.teardown() }
        passives.clear()

        // Remove armor given by this kit, if any
        kitData.armorItems?.let {
            player.inventory.helmet = null
            player.inventory.chestplate = null
            player.inventory.leggings = null
            player.inventory.boots = null
        }

        player.sendDebugMessage("The $id kit has been removed")
    }
}
