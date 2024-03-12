package net.ssmb.abilities

import br.com.devsrsouza.kotlinbukkitapi.extensions.item
import com.github.shynixn.mccoroutine.bukkit.launch
import com.github.shynixn.mccoroutine.bukkit.ticks
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import net.kyori.adventure.text.Component
import net.ssmb.SSMB
import net.ssmb.extensions.*
import net.ssmb.utils.TaggedKeyBool
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerToggleSneakEvent

class ExplodeAbility(
    private val player: Player,
    private val plugin: SSMB,
    private val cooldown: Long,
    private val meta: Map<String, String>?,
    private val index: Int
) : SSMBAbility(player, plugin, cooldown, meta) {
    private val playerInv = player.inventory

    private var lastTimeUsed: Long = 0
    private var isExplodeActive = false

    override fun initializeAbility() {
        super.initializeAbility()

        playerInv.setItem(index, item(Material.IRON_SHOVEL) {
            displayName(Component.text("Explode"))
            persistentDataContainer.setData {
                set("ability_item_id", "explode")
            }
        })
    }

    override fun tryActivateAbility() {
        val currentTime = System.currentTimeMillis()

        if (lastTimeUsed + cooldown > currentTime) {
            val timeLeft = ((lastTimeUsed + cooldown) - currentTime) / 1000.0
            player.sendMessage(Component.text("You have $timeLeft seconds till use again"))
            return
        }

        plugin.launch {
            player.walkSpeed = 0.05f
            player.level = 0
            player.exp = 0f

            repeat(30) { index ->
                if (isExplodeActive) {
                    player.exp = (index + 1) / 30f

                    val volume = 0.5f + index / 20
                    player.world.playSound(player.location, Sound.ENTITY_CREEPER_PRIMED, volume, volume)

                    delay(1.ticks)
                } else {
                    this.cancel()
                }
            }

            player.walkSpeed = 0.2f
            player.level = 0
            player.exp = 0f

            if (isExplodeActive) {
                isExplodeActive = false

                player.world.playSound(player.location, Sound.ENTITY_GENERIC_EXPLODE, 1f, 1f)
                player.world.spawnParticle(Particle.EXPLOSION_LARGE, player.location, 3)

                player.getLivingEntitiesInRadius(8.0).forEach {
                    if (it == player || it.getMetadata((TaggedKeyBool("player_can_be_damaged"))) == false) {
                        return@forEach
                    }

                    val distance = player.location.distance(it.location)
                    val damage = (0.1 + 0.9 * ((8 - distance) / 8)) * 0.75

                    it.doKnockback(2.5, damage, it.health, player.location.toVector(), null)
                    it.damage(damage, player)
                }

                lastTimeUsed = System.currentTimeMillis()
            }
        }
    }

    @EventHandler
    fun onPlayerInteract(event: PlayerInteractEvent) {
        if (event.player != player) return
        if (event.action != Action.RIGHT_CLICK_AIR || event.action != Action.RIGHT_CLICK_BLOCK) return

        val itemAbilityId = event.item?.itemMeta?.persistentDataContainer?.get("ability_item_id")
        if (itemAbilityId != "explode") return

        event.isCancelled = true

        tryActivateAbility()
    }

    @EventHandler
    fun onPlayerToggleSneak(event: PlayerToggleSneakEvent) {
        if (event.player != player || !isExplodeActive) return

        isExplodeActive = false
        player.walkSpeed = 0.2F
        player.level = 0
        player.exp = 0F
    }
}