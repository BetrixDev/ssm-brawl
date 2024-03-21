package net.ssmb.kits

import br.com.devsrsouza.kotlinbukkitapi.extensions.item
import net.ssmb.abilities.IAbility
import net.ssmb.abilities.constructAbilityFromData
import net.ssmb.dtos.minigame.MinigameStartSuccess
import net.ssmb.extensions.metadata
import net.ssmb.passives.IPassive
import net.ssmb.passives.constructPassiveFromData
import net.ssmb.utils.TaggedKeyDouble
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.EquipmentSlot

class CreeperKit(
    private val player: Player,
    private val kitData: MinigameStartSuccess.PlayerData.KitData
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
}