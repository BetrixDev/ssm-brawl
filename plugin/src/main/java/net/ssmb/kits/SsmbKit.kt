package net.ssmb.kits

import br.com.devsrsouza.kotlinbukkitapi.extensions.item
import net.ssmb.SSMB
import net.ssmb.abilities.SsmbAbility
import net.ssmb.abilities.constructAbilityFromData
import net.ssmb.dtos.minigame.MinigameStartSuccess
import net.ssmb.events.BrawlDamageEvent
import net.ssmb.events.BrawlDamageType
import net.ssmb.extensions.getMetadata
import net.ssmb.extensions.metadata
import net.ssmb.minigames.IMinigame
import net.ssmb.passives.SsmbPassive
import net.ssmb.passives.constructPassiveFromData
import net.ssmb.utils.TaggedKeyBool
import net.ssmb.utils.TaggedKeyDouble
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.util.BoundingBox

open class SsmbKit(
    private val player: Player,
    val kitData: MinigameStartSuccess.PlayerData.KitData,
    private val minigame: IMinigame?
) : Listener {
    private val plugin = SSMB.instance
    private val playerInv = player.inventory

    private val abilities = arrayListOf<SsmbAbility>()
    private val passives = arrayListOf<SsmbPassive>()

    open fun initializeKit() {
        playerInv.clear()

        if (kitData.helmetId != null)
            playerInv.setItem(EquipmentSlot.HEAD, item(Material.getMaterial(kitData.helmetId)!!))
        if (kitData.chestplateId != null)
            playerInv.setItem(
                EquipmentSlot.CHEST,
                item(Material.getMaterial(kitData.chestplateId)!!)
            )
        if (kitData.leggingsId != null)
            playerInv.setItem(EquipmentSlot.LEGS, item(Material.getMaterial(kitData.leggingsId)!!))
        if (kitData.bootsId != null)
            playerInv.setItem(EquipmentSlot.FEET, item(Material.getMaterial(kitData.bootsId)!!))

        player.metadata {

            set(TaggedKeyDouble("knockback_multiplier"), kitData.knockbackMult)
            set(TaggedKeyDouble("hitbox_width"), kitData.hitboxWidth)
            set(TaggedKeyDouble("hitbox_height"), kitData.hitboxHeight)
            set(TaggedKeyBool("ssmb_entity"), true)
        }

        kitData.abilities.forEach {
            val ability = constructAbilityFromData(player, it)
            ability.initializeAbility()
            abilities.add(ability)
        }

        kitData.passives.forEach {
            val passive = constructPassiveFromData(player, it)
            passive.initializePassive()
            passives.add(passive)
        }

        plugin.server.pluginManager.registerEvents(this, plugin)
    }

    open fun destroyKit() {
        player.metadata {
            remove(TaggedKeyDouble("knockback_multiplier"))
            remove(TaggedKeyDouble("hitbox_width"))
            remove(TaggedKeyDouble("hitbox_height"))
            remove(TaggedKeyBool("ssmb_entity"))
        }

        abilities.forEach { it.destroyAbility() }

        passives.forEach { it.destroyPassive() }

        HandlerList.unregisterAll(this)
    }

    @EventHandler
    fun onPlayerInteract(event: PlayerInteractEvent) {
        if (minigame == null) return
        if (event.player != player) return
        if (event.action != Action.LEFT_CLICK_AIR || event.action != Action.LEFT_CLICK_BLOCK) return

        val player = event.player

        // TODO: Ray cast to every player and hit the closest one,
        //  just in case two players are directly in front of the player

        val enemyToAttack =
            player.world.getNearbyLivingEntities(player.location, 4.5)
                .filter { it.getMetadata(TaggedKeyBool("ssmb_entity")) == true }
                .find {
                    val hitboxWidth = it.getMetadata(TaggedKeyDouble("hitbox_width")) ?: 1.0
                    val hitboxHeight = it.getMetadata(TaggedKeyDouble("hitbox_heights")) ?: 1.0

                    val boundingBox =
                        BoundingBox.of(it.location, hitboxWidth, hitboxHeight, hitboxWidth)

                    val rayTraceResult =
                        boundingBox.rayTrace(
                            player.eyeLocation.toVector(),
                            player.location.direction,
                            4.5
                        )

                    rayTraceResult != null && rayTraceResult.hitEntity != null
                } ?: return

        event.isCancelled = true

        BrawlDamageEvent(enemyToAttack, player, kitData.meleeDamage, BrawlDamageType.MELEE)
            .callEvent()
    }
}
