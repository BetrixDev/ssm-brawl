package dev.betrix.superSmashMobsBrawl.kits

import dev.betrix.superSmashMobsBrawl.abilities.*
import dev.betrix.superSmashMobsBrawl.disguises.*
import dev.betrix.superSmashMobsBrawl.extensions.sendDebugMessage
import dev.betrix.superSmashMobsBrawl.passives.*
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

    private var invincible: Boolean = false

    fun getMeleeDamage(): Double = kitData.meleeDamage

    fun getMeleeReach(): Double = kitData.meleeReach

    fun getKnockbackMultiplier(): Double = kitData.knockbackMultiplier

    fun isInvincible(): Boolean = invincible

    fun setInvincible(value: Boolean) {
        invincible = value
    }

    fun getPassive(id: String): BrawlPassive? {
        return passives.find { it.id == id }
    }

    fun getAbility(id: String): BrawlAbility? {
        return abilities.find { it.id == id }
    }

    open fun setup() {
        disguise = kitData.disguiseId?.let { BrawlDisguiseFactory.create(player, it) }
        if (kitData.disguiseId != null && disguise == null) {
            logger.severe("No disguise known with id ${kitData.disguiseId}")
        }

        kitData.abilities.forEach {
            abilities.add(
                when (it.id) {
                    "sulphur_bomb" -> SulphurBombAbility(player)
                    "explode" -> ExplodeAbility(player)
                    "roped_arrow" -> RopedArrowAbility(player)
                    "bone_blast" -> BoneBlastAbility(player)
                    "angry_herd" -> AngryHerdAbility(player)
                    "milk_spiral" -> MilkSpiralAbility(player)
                    "blink" -> BlinkAbility(player)
                    "block_toss" -> BlockTossAbility(player)
                    "egg_blaster" -> EggBlasterAbility(player)
                    "chicken_missile" -> ChickenMissileAbility(player)
                    "needler" -> NeedlerAbility(player)
                    "spin_web" -> SpinWebAbility(player)
                    "daze_potion" -> DazePotionAbility(player)
                    "bat_wave" -> BatWaveAbility(player)
                    "ink_shotgun" -> InkShotgunAbility(player)
                    "super_squid" -> SuperSquidAbility(player)
                    "fish_flurry" -> FishFlurryAbility(player)
                    "bouncy_bacon" -> BouncyBaconAbility(player)
                    "baby_bacon_bomb" -> BabyBaconBombAbility(player)
                    "whirlpool_axe" -> WhirlpoolAxeAbility(player)
                    "water_splash" -> WaterSplashAbility(player)
                    "target_laser" -> TargetLaserAbility(player)
                    "bile_blaster" -> BileBlasterAbility(player)
                    "deaths_grasp" -> DeathsGraspAbility(player)
                    "slime_rocket" -> SlimeRocketAbility(player)
                    "slime_slam" -> SlimeSlamAbility(player)
                    "iron_hook" -> IronHookAbility(player)
                    "seismic_slam" -> SeismicSlamAbility(player)
                    "blizzard" -> BlizzardAbility(player)
                    "ice_path" -> IcePathAbility(player)
                    "snow_turret" -> SnowTurretAbility(player)
                    "fissure" -> FissureAbility(player)
                    "strawbury_swirl" -> StrawburySwirlAbility(player)
                    "moostrike" -> MoostrikeAbility(player)
                    "fungal_trampoline" -> FungalTrampolineAbility(player)
                    "hoof_bash" -> HoofBashAbility(player)
                    "harvest_rush" -> HarvestRushAbility(player)
                    "gliding_gallop" -> GlidingGallopAbility(player)
                    "static_laser" -> StaticLaserAbility(player)
                    "wool_mine" -> WoolMineAbility(player)
                    "wooly_rocket" -> WoolyRocketAbility(player)
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
                    "double_jump" -> DoubleJumpPassive(player = player)
                    "regeneration" -> RegenerationPassive(player)
                    "hunger" -> HungerPassive(player)
                    "arrow_recharge" -> ArrowRechargePassive(player)
                    "barrage" -> BarragePassive(player)
                    "stampede" -> StampedePassive(player)
                    "potion_effect" -> PotionEffectPassive(player)
                    "flap" -> FlapPassive(player)
                    "wall_climb" -> WallClimbPassive(player)
                    "spider_leap" -> SpiderLeapPassive(player)
                    "nether_pig" -> NetherPigPassive(player)
                    "corrupted_arrow" -> CorruptedArrowPassive(player)
                    "giga_slime" -> GigaSlimePassive(player)
                    "arctic_aura" -> ArcticAuraPassive(player)
                    "exp_charge" -> ExpChargePassive(player)
                    "overgrowth" -> OvergrowthPassive(player)
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
        player.clearActivePotionEffects()
        player.exp = 0f
        player.level = 0
        player.totalExperience = 0

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
            player.getAttribute(attackSpeedAttr)?.baseValue = 32.0
        } catch (_: Throwable) {}
        // Clear offhand to avoid shield mechanics
        try {
            player.inventory.setItemInOffHand(ItemStack.of(Material.AIR))
        } catch (_: Throwable) {}

        player.sendDebugMessage("You have been given the $id kit")
    }

    open fun teardown() {
        disguise?.teardown()
        disguise = null

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

        player.clearActivePotionEffects()

        player.sendDebugMessage("The $id kit has been removed")
    }
}
