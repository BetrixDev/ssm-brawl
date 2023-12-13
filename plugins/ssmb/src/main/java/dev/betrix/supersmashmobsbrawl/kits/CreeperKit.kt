package dev.betrix.supersmashmobsbrawl.kits

import br.com.devsrsouza.kotlinbukkitapi.extensions.item
import br.com.devsrsouza.kotlinbukkitapi.extensions.unregisterListener
import com.github.shynixn.mccoroutine.bukkit.launch
import com.github.shynixn.mccoroutine.bukkit.ticks
import dev.betrix.supersmashmobsbrawl.SuperSmashMobsBrawl
import kotlinx.coroutines.delay
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.entity.ThrownPotion
import org.bukkit.event.EventHandler
import org.bukkit.event.block.Action
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.PotionSplashEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerToggleFlightEvent
import org.bukkit.event.player.PlayerToggleSneakEvent
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

class CreeperKit constructor(
    private val player: Player,
) : BaseKit {
    private val plugin = SuperSmashMobsBrawl.instance
    private val audience = Audience.audience(player)

    private val ironAxe = item(Material.IRON_AXE)
    private val ironShovel = item(Material.IRON_SHOVEL)
    private val compass = item(Material.COMPASS)

    private val helmet = item(Material.LEATHER_HELMET)
    private val chestplate = item(Material.LEATHER_CHESTPLATE)
    private val leggings = item(Material.LEATHER_LEGGINGS)
    private val boots = item(Material.IRON_BOOTS)

    private var sulphurBombCooldown = 3000
    private var lastSulphurBombTime: Long = 0

    private var explodeCooldown = 8000
    private var lastExplodeTime: Long = 0
    private var isExplodeActive = false

    private var isLightingActive = false

    override fun equipKit() {
        plugin.server.pluginManager.registerEvents(this, plugin)

        player.inventory.clear()
        player.inventory.setItem(0, ironAxe)
        player.inventory.setItem(1, ironShovel)
        player.inventory.setItem(2, compass)
        player.inventory.helmet = helmet
        player.inventory.chestplate = chestplate
        player.inventory.leggings = leggings
        player.inventory.boots = boots

        player.allowFlight = true
    }

    override fun removeKit() {
        this.unregisterListener()
        player.inventory.clear()
    }

    @EventHandler
    fun onPlayerInteract(event: PlayerInteractEvent) {
        if (event.player !== player) return

        val action = event.action
        val heldItem = event.item

        if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
            if (heldItem == ironAxe) {
                tryUseSulphurBomb()
            } else if (heldItem == ironShovel) {
                tryUseExplode()
            }
        }
    }

    @EventHandler
    fun onEntityDamage(event: EntityDamageEvent) {
        if (event.entity != player) {
            return
        }

        if (event.cause == EntityDamageEvent.DamageCause.ENTITY_ATTACK) {
            if (isLightingActive) {
                event.isCancelled = true
                player.world.strikeLightningEffect(player.location)
                setUnpowered()
                plugin.launch {

                }
            }
            return
        }

        if (
            event.cause == EntityDamageEvent.DamageCause.FIRE_TICK ||
            event.cause == EntityDamageEvent.DamageCause.STARVATION ||
            event.cause == EntityDamageEvent.DamageCause.POISON
        ) {
            return
        }

        tryUseLightning()
    }

    @EventHandler
    fun onPotionSlash(event: PotionSplashEvent) {
        val splashedItem = event.entity
        val splashedLocation = splashedItem.location

        if (splashedItem.customName() != Component.text(player.uniqueId.toString())) {
            return
        }

        event.isCancelled = true
        splashedItem.remove()

        if (event.hitEntity !== null && event.hitEntity is Player && event.hitEntity != player) {
            val target = event.hitEntity as Player

            target.damage(6.5)
        } else if (event.hitBlock !== null) {
            player.world.players.filter {
                it.location.distance(splashedLocation) < 3 && it.player != player
            }.forEach {
                it.damage(6.5)
            }
        }

        player.world.spawnParticle(Particle.EXPLOSION_LARGE, splashedLocation, 1)
        player.world.playSound(splashedLocation, Sound.ENTITY_GENERIC_EXPLODE, 1f, 1.5f)
    }

    @EventHandler
    fun onPlayerToggleSneak(event: PlayerToggleSneakEvent) {
        if (event.player != player) {
            return
        }

        if (isExplodeActive) {
            isExplodeActive = false
            player.exp = 0F
            player.level = 0
            player.walkSpeed = 0.2F
        }
    }

    @EventHandler()
    fun onPlayerToggleFlight(event: PlayerToggleFlightEvent) {
        val target = event.player

        if (event.player != player || target.gameMode == GameMode.CREATIVE) {
            return
        }

        event.isCancelled = true

        target.isFlying = true
        target.allowFlight = false
        target.fallDistance = 0F

        player.playSound(player.location, Sound.ENTITY_BLAZE_SHOOT, 1f, 1f)

        var vec = player.location.direction
        vec.y = 0.9
        vec = vec.normalize()
        vec = vec.multiply(0.9)
        if (vec.y > 0.9) {
            vec.y = 0.9
        }
        if (player.isOnGround) {
            vec.y += 0.2
        }
        player.fallDistance = 0F
        player.velocity = vec.multiply(1.5)
        target.allowFlight = true
    }

    private fun tryUseLightning() {
        if (isLightingActive) {
            return
        }
        player.removePotionEffect(PotionEffectType.SPEED)
        player.addPotionEffect(PotionEffect(PotionEffectType.SPEED, 80, 1, false, false))
        player.world.playSound(player.location, Sound.ENTITY_CREEPER_HURT, 3F, 1.25F)
        // send message (https://github.com/Whoneedspacee/SSMOS/blob/master/ssm/attributes/LightningShield.java#L44)
        plugin.launch {
            delay(40.ticks)
            setUnpowered()
            player.world.playSound(player.location, Sound.ENTITY_CREEPER_HURT, 3F, 0.75F)
        }
    }

    private fun setPowered() {
        isLightingActive = true
        // Make actor charged creeper
    }

    private fun setUnpowered() {
        isLightingActive = false
        // Make actor normal creeper now
    }

    private fun tryUseSulphurBomb() {
        val currentTime = System.currentTimeMillis()

        if (currentTime < lastSulphurBombTime + sulphurBombCooldown) {
            val timeLeft = ((lastSulphurBombTime + sulphurBombCooldown) - currentTime).toDouble() / 1000.0

            audience.sendMessage(
                MiniMessage.miniMessage()
                    .deserialize("<blue>Recharge></blue> <gray>You cannot use <green>Sulphur Bomb</green> for <green>$timeLeft seconds</green></gray>")
            )

            return
        }

        val location = player.eyeLocation
        val direction = location.direction

        val projectile = player.world.spawn(location, ThrownPotion::class.java)
        projectile.velocity = direction.multiply(1.55)
        projectile.shooter = player
        projectile.customName(Component.text(player.uniqueId.toString()))
        projectile.item = item(Material.COAL)

        lastSulphurBombTime = currentTime
    }

    private fun tryUseExplode() {
        val currentTime = System.currentTimeMillis()
        isExplodeActive = true

        if (currentTime < lastExplodeTime + explodeCooldown) {
            val timeLeft = ((lastExplodeTime + explodeCooldown) - currentTime).toDouble() / 1000.0

            audience.sendMessage(
                MiniMessage.miniMessage()
                    .deserialize("<blue>Recharge></blue> <gray>You cannot use <green>Explode</green> for <green>$timeLeft seconds</green></gray>")
            )

            return
        }

        plugin.launch {
            player.walkSpeed = 0.05F
            player.level = 0
            player.exp = 0F
            repeat(30) { index ->
                if (isExplodeActive) {
                    player.exp = (index + 1) / 30F
                    delay(1.ticks)
                }
            }
            player.walkSpeed = 0.2F
            player.level = 0
            player.exp = 0F

            if (isExplodeActive) {
                player.world.spawnParticle(Particle.EXPLOSION_HUGE, player.location, 3)
                player.world.playSound(player.location, Sound.ENTITY_GENERIC_EXPLODE, 1f, 1f)

                lastExplodeTime = currentTime
                isExplodeActive = false
            }
        }
    }
}