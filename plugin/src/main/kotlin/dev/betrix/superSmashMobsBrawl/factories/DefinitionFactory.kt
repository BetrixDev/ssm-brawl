package dev.betrix.superSmashMobsBrawl.factories

import dev.betrix.superSmashMobsBrawl.abilities.AbilityMetadata
import dev.betrix.superSmashMobsBrawl.abilities.AbilityType
import dev.betrix.superSmashMobsBrawl.abilities.AbilityUsageType
import dev.betrix.superSmashMobsBrawl.abilities.definitions.AbilityDefinition
import dev.betrix.superSmashMobsBrawl.abilities.instances.AbilityInstance
import dev.betrix.superSmashMobsBrawl.kits.KitMetadata
import dev.betrix.superSmashMobsBrawl.kits.definitions.KitDefinition
import dev.betrix.superSmashMobsBrawl.kits.instances.KitInstance
import dev.betrix.superSmashMobsBrawl.kits.kit
import dev.betrix.superSmashMobsBrawl.minigames.MinigameMetadata
import dev.betrix.superSmashMobsBrawl.minigames.definitions.MinigameDefinition
import dev.betrix.superSmashMobsBrawl.minigames.instances.MinigameInstance
import dev.betrix.superSmashMobsBrawl.models.MinigameTeam
import dev.betrix.superSmashMobsBrawl.models.brawlData.AbilityDef
import dev.betrix.superSmashMobsBrawl.models.brawlData.KitDef
import dev.betrix.superSmashMobsBrawl.models.brawlData.MinigameDef
import dev.betrix.superSmashMobsBrawl.passives.PassiveMetadata
import dev.betrix.superSmashMobsBrawl.passives.definitions.PassiveDefinition
import dev.betrix.superSmashMobsBrawl.passives.instances.PassiveInstance
import dev.betrix.superSmashMobsBrawl.registries.AbilityRegistry
import dev.betrix.superSmashMobsBrawl.registries.PassiveRegistry
import dev.betrix.superSmashMobsBrawl.services.DataService
import org.bukkit.entity.Player
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

object DefinitionFactory : KoinComponent {
    private val dataService: DataService by inject()
    
    /**
     * Creates a runtime AbilityDefinition from YAML data
     */
    fun createAbilityDefinition(abilityDef: AbilityDef): AbilityDefinition {
        return object : AbilityDefinition() {
            override val name: String = abilityDef.id.replace("_", " ").split(" ")
                .joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } }
            override val id: String = abilityDef.id
            override val metadata: AbilityMetadata = AbilityMetadata(
                cooldown = abilityDef.cooldown.toInt(),
                type = when (abilityDef.type) {
                    dev.betrix.superSmashMobsBrawl.models.brawlData.AbilityType.AOE -> AbilityType.AOE
                    dev.betrix.superSmashMobsBrawl.models.brawlData.AbilityType.PROJECTILE -> AbilityType.PROJECTILE
                },
                usageType = when (abilityDef.usage) {
                    dev.betrix.superSmashMobsBrawl.models.brawlData.AbilityUsage.RIGHT_CLICK -> AbilityUsageType.RIGHT_CLICK
                    dev.betrix.superSmashMobsBrawl.models.brawlData.AbilityUsage.LEFT_CLICK -> AbilityUsageType.LEFT_CLICK
                },
                hotbarItem = ItemStack(Material.valueOf(abilityDef.hotbarItem.uppercase())),
                hotbarItemSlot = abilityDef.itemSlot
            )
            
            override fun createInstance(player: Player): AbilityInstance {
                // Use the existing ability instance classes based on ID
                // This is a temporary mapping until we fully refactor ability instances
                return when (id) {
                    "sulphur_bomb" -> createAbilityInstance("SulphurBombAbilityInstance", this, player)
                    "explode", "explosion" -> createAbilityInstance("ExplosionAbilityInstance", this, player)
                    "bone_explosion" -> createAbilityInstance("BoneExplosionAbilityInstance", this, player)
                    "roped_arrow" -> createAbilityInstance("RopedArrowAbilityInstance", this, player)
                    else -> throw IllegalArgumentException("Unknown ability ID: $id")
                }
            }
        }
    }
    
    /**
     * Creates a runtime KitDefinition from YAML data
     */
    fun createKitDefinition(kitDef: KitDef): KitDefinition {
        return object : KitDefinition() {
            override val name: String = kitDef.id.replaceFirstChar { it.uppercase() }
            override val id: String = kitDef.id
            override val metadata: KitMetadata = kit {
                description = "Kit loaded from data"
                meleeDamage = kitDef.meleeDamage.toInt()
                armor = kitDef.armor
                knockbackMultiplier = kitDef.knockbackMultiplier
                
                // Add passives from data
                kitDef.passives.forEach { passiveDef ->
                    val passiveData = dataService.getPassive(passiveDef.id)
                    if (passiveData != null) {
                        val passiveDefinition = createPassiveDefinition(passiveData)
                        passive(passiveDefinition)
                    }
                }
                
                // Add abilities from data
                kitDef.abilities.forEach { abilityDef ->
                    val abilityData = dataService.getAbility(abilityDef.id)
                    if (abilityData != null) {
                        val abilityDefinition = createAbilityDefinition(abilityData)
                        ability(abilityDefinition)
                    }
                }
            }
            
            override fun createInstance(
                player: Player,
                minigameDefinition: MinigameDefinition?
            ): KitInstance {
                // Use the existing kit instance classes based on ID
                return when (id) {
                    "creeper" -> createKitInstance("CreeperKitInstance", this, player, minigameDefinition)
                    "skeleton" -> createKitInstance("SkeletonKitInstance", this, player, minigameDefinition)
                    else -> KitInstance(this, player, minigameDefinition)
                }
            }
        }
    }
    
    /**
     * Creates a runtime PassiveDefinition from YAML data
     */
    fun createPassiveDefinition(passiveDef: dev.betrix.superSmashMobsBrawl.models.brawlData.PassiveDef): PassiveDefinition {
        return object : PassiveDefinition() {
            override val name: String = passiveDef.id.replace("_", " ").split(" ")
                .joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } }
            override val id: String = passiveDef.id
            override val metadata: PassiveMetadata = PassiveMetadata()
            
            override fun createInstance(player: Player): PassiveInstance {
                // Use the existing passive instance classes based on ID
                return when (id) {
                    "double_jump" -> createPassiveInstance("DoubleJumpPassiveInstance", this, player)
                    "regeneration" -> createPassiveInstance("RegenerationPassiveInstance", this, player)
                    "hunger" -> createPassiveInstance("HungerPassiveInstance", this, player)
                    "arrow_recharge" -> createPassiveInstance("ArrowRechargePassiveInstance", this, player)
                    "barrage" -> createPassiveInstance("BarragePassiveInstance", this, player)
                    else -> throw IllegalArgumentException("Unknown passive ID: $id")
                }
            }
        }
    }
    
    /**
     * Creates a runtime MinigameDefinition from YAML data
     */
    fun createMinigameDefinition(minigameDef: MinigameDef): MinigameDefinition {
        return object : MinigameDefinition() {
            override val name: String = minigameDef.id.replace("_", " ").split(" ")
                .joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } }
            override val id: String = minigameDef.id
            override val metadata: MinigameMetadata = when (minigameDef) {
                is dev.betrix.superSmashMobsBrawl.models.brawlData.FfaMinigameDef -> MinigameMetadata(
                    isHidden = minigameDef.isHidden,
                    playersPerTeam = 1,
                    amountOfTeams = minigameDef.maxPlayers,
                    stocks = 1
                )
                is dev.betrix.superSmashMobsBrawl.models.brawlData.TeamBasedStocksMinigameDef -> MinigameMetadata(
                    isHidden = minigameDef.isHidden,
                    playersPerTeam = minigameDef.playersPerTeam,
                    amountOfTeams = minigameDef.amountOfTeams,
                    stocks = minigameDef.stocks
                )
            }
            
            override fun createInstance(teams: List<MinigameTeam>): MinigameInstance {
                // Use the existing minigame instance classes based on ID
                return when (id) {
                    "prototyping" -> createMinigameInstance("TestingMinigameInstance", this, teams)
                    "two_player_duels" -> createMinigameInstance("TwoPlayerDuelsMinigameInstance", this, teams)
                    else -> throw IllegalArgumentException("Unknown minigame ID: $id")
                }
            }
        }
    }
    
    // Helper functions to create instances using reflection
    private fun createAbilityInstance(className: String, definition: AbilityDefinition, player: Player): AbilityInstance {
        val fullClassName = "dev.betrix.superSmashMobsBrawl.abilities.instances.$className"
        val clazz = Class.forName(fullClassName)
        val constructor = clazz.getConstructor(AbilityDefinition::class.java, Player::class.java)
        return constructor.newInstance(definition, player) as AbilityInstance
    }
    
    private fun createKitInstance(className: String, definition: KitDefinition, player: Player, minigameDefinition: MinigameDefinition?): KitInstance {
        val fullClassName = "dev.betrix.superSmashMobsBrawl.kits.instances.$className"
        val clazz = Class.forName(fullClassName)
        val constructor = clazz.getConstructor(KitDefinition::class.java, Player::class.java, MinigameDefinition::class.java)
        return constructor.newInstance(definition, player, minigameDefinition) as KitInstance
    }
    
    private fun createPassiveInstance(className: String, definition: PassiveDefinition, player: Player): PassiveInstance {
        val fullClassName = "dev.betrix.superSmashMobsBrawl.passives.instances.$className"
        val clazz = Class.forName(fullClassName)
        val constructor = clazz.getConstructor(PassiveDefinition::class.java, Player::class.java)
        return constructor.newInstance(definition, player) as PassiveInstance
    }
    
    private fun createMinigameInstance(className: String, definition: MinigameDefinition, teams: List<MinigameTeam>): MinigameInstance {
        val fullClassName = "dev.betrix.superSmashMobsBrawl.minigames.instances.$className"
        val clazz = Class.forName(fullClassName)
        val constructor = clazz.getConstructor(MinigameDefinition::class.java, List::class.java)
        return constructor.newInstance(definition, teams) as MinigameInstance
    }
}