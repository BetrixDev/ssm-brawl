package net.ssmb.kits

import br.com.devsrsouza.kotlinbukkitapi.extensions.item
import net.ssmb.abilities.IAbility
import net.ssmb.abilities.constructAbilityFromData
import net.ssmb.dtos.minigame.MinigameStartSuccess
import net.ssmb.extensions.metadata
import net.ssmb.minigames.IMinigame
import net.ssmb.passives.IPassive
import net.ssmb.passives.constructPassiveFromData
import net.ssmb.utils.TaggedKeyDouble
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.util.BoundingBox

class CreeperKit(
    override val player: Player,
    override val kitData: MinigameStartSuccess.PlayerData.KitData,
    private val minigame: IMinigame
) : IKit {
    private val abilities = arrayListOf<IAbility>()
    private val passives = arrayListOf<IPassive>()
    private val playerInv = player.inventory

    override fun initializeKit() {
        playerInv.clear()

        if (kitData.helmetId != null) playerInv.setItem(
            EquipmentSlot.HEAD,
            item(Material.getMaterial(kitData.helmetId)!!)
        )
        if (kitData.chestplateId != null) playerInv.setItem(
            EquipmentSlot.CHEST,
            item(Material.getMaterial(kitData.chestplateId)!!)
        )
        if (kitData.leggingsId != null) playerInv.setItem(
            EquipmentSlot.LEGS,
            item(Material.getMaterial(kitData.leggingsId)!!)
        )
        if (kitData.bootsId != null) playerInv.setItem(
            EquipmentSlot.FEET,
            item(Material.getMaterial(kitData.bootsId)!!)
        )

        player.metadata {
            set(TaggedKeyDouble("knockback_multiplier"), kitData.knockbackMult)
        }

        kitData.abilities.forEach {
            val ability = constructAbilityFromData(player, it)
            ability.initializeAbility()
            abilities.add(ability)
        }

        kitData.passives.forEach {
            val passive = constructPassiveFromData(player, it)
            passive.createPassive()
            passives.add(passive)
        }
    }

    override fun destroyKit() {
        player.metadata {
            remove(TaggedKeyDouble("knockback_multiplier"))
        }

        abilities.forEach {
            it.destroyAbility()
        }

        passives.forEach {
            it.destroyPassive()
        }
    }

    @EventHandler
    fun onPlayerInteract(event: PlayerInteractEvent) {
        if (event.player != player) return
        if (event.action != Action.LEFT_CLICK_AIR || event.action != Action.LEFT_CLICK_BLOCK) return

        val player = event.player

        val enemyToAttack = minigame.teamsStocks.asSequence().filter {
            !it.key.contains(player)
        }.map {
            it.key
        }.flatten().map {
            minigame.playerKits[it]!!
        }.find {
            val boundingBox = BoundingBox.of(
                it.player.location,
                it.kitData.hitboxWidth,
                it.kitData.hitboxHeight,
                it.kitData.hitboxWidth
            )

            val rayTraceResult = boundingBox.rayTrace(player.eyeLocation.toVector(), player.location.direction, 4.5)

            rayTraceResult != null && rayTraceResult.hitEntity != null
        }?.player

        if (enemyToAttack == null) return

        event.isCancelled = true
        minigame.damagePlayer(enemyToAttack, player, kitData.meleeDamage, kitData.knockbackMult)
    }
}