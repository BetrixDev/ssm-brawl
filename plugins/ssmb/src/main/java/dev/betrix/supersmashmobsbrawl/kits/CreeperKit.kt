package dev.betrix.supersmashmobsbrawl.kits

import br.com.devsrsouza.kotlinbukkitapi.extensions.item
import com.github.shynixn.mccoroutine.bukkit.launch
import com.github.shynixn.mccoroutine.bukkit.ticks
import dev.betrix.supersmashmobsbrawl.SuperSmashMobsBrawl
import dev.betrix.supersmashmobsbrawl.disguises.CreeperDisguise
import dev.betrix.supersmashmobsbrawl.enums.LangEntry
import dev.betrix.supersmashmobsbrawl.enums.TaggedKeyBool
import dev.betrix.supersmashmobsbrawl.enums.TaggedKeyStr
import dev.betrix.supersmashmobsbrawl.extensions.*
import dev.betrix.supersmashmobsbrawl.managers.api.payloads.StartGameResponse
import dev.betrix.supersmashmobsbrawl.passives.DoubleJumpPassive
import dev.betrix.supersmashmobsbrawl.passives.RegenerationPassive
import dev.betrix.supersmashmobsbrawl.passives.SSMBPassive
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.entity.ThrownPotion
import org.bukkit.event.EventHandler
import org.bukkit.event.HandlerList
import org.bukkit.event.block.Action
import org.bukkit.event.entity.PotionSplashEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerToggleSneakEvent
import org.bukkit.inventory.EquipmentSlot

class CreeperKit constructor(
    private val player: Player,
    kitData: StartGameResponse.KitData
) : SSMBKit(player, kitData) {
    private val plugin = SuperSmashMobsBrawl.instance
    private val disguise = CreeperDisguise(player)

    private val passives = arrayListOf<SSMBPassive>()

    override fun equipKit() {
        plugin.server.pluginManager.registerEvents(this, plugin)

        val inventory = player.inventory

        inventory.clear()

        inventory.setItem(EquipmentSlot.HEAD, item(Material.LEATHER_HELMET))
        inventory.setItem(EquipmentSlot.CHEST, item(Material.LEATHER_CHESTPLATE))
        inventory.setItem(EquipmentSlot.LEGS, item(Material.LEATHER_LEGGINGS))
        inventory.setItem(EquipmentSlot.FEET, item(Material.IRON_BOOTS))

        inventory.setItem(0, item(Material.IRON_AXE) {
            displayName(MiniMessage.miniMessage().deserialize("Sulphur Bomb"))
            persistentDataContainer.setData {
                set(TaggedKeyStr.ABILITY_ITEM_ID, "sulphur_bomb")
            }
        })

        inventory.setItem(1, item(Material.IRON_SHOVEL) {
            displayName(MiniMessage.miniMessage().deserialize("Explode"))
            persistentDataContainer.setData {
                set(TaggedKeyStr.ABILITY_ITEM_ID, "explode")
            }
        })

        passives.add(DoubleJumpPassive(player))
        passives.add(RegenerationPassive(player, 0.4))

        disguise.createDisguise()
    }

    override fun destroyKit() {
        HandlerList.unregisterAll(this)
        disguise.destroyDisguise()
        passives.forEach {
            it.destroyPassive()
        }
    }

    @EventHandler
    fun onPlayerInteract(event: PlayerInteractEvent) {
        if (event.player != player) return

        val item = event.item ?: return
        val itemAbilityId = item.itemMeta.persistentDataContainer.get(TaggedKeyStr.ABILITY_ITEM_ID) ?: return

        if (event.action == Action.RIGHT_CLICK_AIR || event.action == Action.RIGHT_CLICK_BLOCK) {
            if (itemAbilityId == "sulphur_bomb") {
                tryUseSulphurBomb()
            } else if (itemAbilityId == "explode") {
                tryUseExplode()
            }
        }
    }

    private val sulphurBombCooldown: Long = 3 * 1000
    private var lastSulphurBombTime: Long = 0
    private fun tryUseSulphurBomb() {
        val currentTime = System.currentTimeMillis()
        if (currentTime < lastSulphurBombTime + sulphurBombCooldown) {
            val timeLeft = ((lastSulphurBombTime + sulphurBombCooldown) - currentTime) / 1000.0

            val cooldownMessage = plugin.lang.getComponent(
                LangEntry.ABILITY_COOLDOWN,
                player,
                hashMapOf("ability_name" to "Sulphur Bomb", "time_left" to "$timeLeft")
            )

            player.sendMessage(cooldownMessage)
            return
        }

        lastSulphurBombTime = currentTime

        val location = player.eyeLocation
        val direction = location.direction

        val projectile = player.world.spawn(location, ThrownPotion::class.java)
        projectile.velocity = direction.multiply(1.55)
        projectile.shooter = player
        projectile.item = item(Material.COAL)

        plugin.launch {
            val projectileSize = 0.65

            while (true) {
                val nearbyEntities = projectile.getNearbyEntities(projectileSize, projectileSize, projectileSize)

                nearbyEntities.forEach {
                    if (it !is Player || it == player) {
                        return@forEach
                    }

                    val splashEvent = PotionSplashEvent(projectile, it, null, null, mutableMapOf(it to 1.0))
                    splashEvent.callEvent()
                    this.cancel()
                }

                delay(1.ticks)
            }
        }
    }

    @EventHandler
    fun onPotionSplash(event: PotionSplashEvent) {
        if (event.entity.ownerUniqueId != player.uniqueId) return

        event.isCancelled = true

        val splashedItem = event.entity
        val splashedLocation = splashedItem.location

        val projKnockbackMultiplier = 2.5
        val projDamage = 6.5

        if (event.hitEntity != null && event.hitEntity is Player && event.hitEntity != player) {
            val target = event.hitEntity as Player
            target.doKnockback(
                projKnockbackMultiplier,
                projDamage,
                target.health,
                player.location.toVector(),
                null
            )
            target.damage(projDamage, splashedItem)
        }

        player.world.spawnParticle(
            Particle.EXPLOSION_LARGE,
            splashedLocation,
            1
        )

        player.world.playSound(
            splashedLocation,
            Sound.ENTITY_GENERIC_EXPLODE,
            1F,
            1.5F
        )
    }

    private val explodeCooldown: Long = 8 * 1000
    private var lastExplodeTime: Long = 0
    private var isExplodeActive = false
    private fun tryUseExplode() {
        val currentTime = System.currentTimeMillis()
        if (currentTime < lastExplodeTime + explodeCooldown) {
            val timeLeft = ((lastExplodeTime + explodeCooldown) - currentTime) / 1000.0

            val cooldownMessage = plugin.lang.getComponent(
                LangEntry.ABILITY_COOLDOWN,
                player,
                hashMapOf("ability_name" to "Explode", "time_left" to "$timeLeft")
            )

            player.sendMessage(cooldownMessage)
            return
        }

        isExplodeActive = true

        plugin.launch {
            player.walkSpeed = 0.05F
            player.level = 0
            player.exp = 0F

            disguise.setIgnited(true)

            repeat(30) { index ->
                if (isExplodeActive) {
                    player.exp = (index + 1) / 30F

                    val volume = 0.5F + index / 20
                    player.world.playSound(player.location, Sound.ENTITY_CREEPER_PRIMED, volume, volume)

                    delay(1.ticks)
                } else {
                    this.cancel()
                }
            }

            player.walkSpeed = 0.2F
            player.level = 0
            player.exp = 0F

            if (isExplodeActive) {
                isExplodeActive = false

                player.world.playSound(player.location, Sound.ENTITY_GENERIC_EXPLODE, 1F, 1F)
                player.world.spawnParticle(Particle.EXPLOSION_LARGE, player.location, 3)

                player.getLivingEntitiesInRadius(8.0).forEach {
                    if (it == player || it.getMetadata(TaggedKeyBool.PLAYER_CAN_BE_DAMAGED) == true) {
                        return@forEach
                    }

                    val distance = player.location.distance(it.location)
                    val damage = (0.1 + 0.9 * ((8 - distance) / 8)) * 0.75

                    it.doKnockback(2.5, damage, it.health, player.location.toVector(), null)
                    it.damage(damage, player)
                }

                disguise.setIgnited(false)
                lastExplodeTime = System.currentTimeMillis()
            }
        }
    }

    @EventHandler
    fun onPlayerToggleSneak(event: PlayerToggleSneakEvent) {
        if (event.player != player || !isExplodeActive) return

        isExplodeActive = false
        disguise.setIgnited(false)
        player.walkSpeed = 0.2F
        player.level = 0
        player.exp = 0F
    }
}