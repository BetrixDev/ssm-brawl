package net.ssmb.kits

import br.com.devsrsouza.kotlinbukkitapi.extensions.item
import net.ssmb.SSMB
import net.ssmb.abilities.IAbility
import net.ssmb.abilities.constructAbilityFromData
import net.ssmb.dtos.minigame.MinigameStartSuccess
import net.ssmb.passives.IPassive
import net.ssmb.passives.constructPassiveFromId
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.EquipmentSlot

class CreeperKit(
    private val player: Player,
    private val kitData: MinigameStartSuccess.PlayerData.KitData
) : IKit {
    private val plugin = SSMB.instance
    private val abilities = arrayListOf<IAbility>()
    private val passives = arrayListOf<IPassive>()
    private val playerInv = player.inventory

    override fun initializeKit() {
        playerInv.clear()

        playerInv.setItem(EquipmentSlot.HEAD, item(Material.LEATHER_HELMET))
        playerInv.setItem(EquipmentSlot.CHEST, item(Material.LEATHER_CHESTPLATE))
        playerInv.setItem(EquipmentSlot.LEGS, item(Material.LEATHER_LEGGINGS))
        playerInv.setItem(EquipmentSlot.FEET, item(Material.IRON_BOOTS))

        kitData.abilities.forEach {
            val ability = constructAbilityFromData(player, it)
            ability.initializeAbility()
            abilities.add(ability)
        }

        kitData.passives.forEach {
            val passive = constructPassiveFromId(player, it.passive.id, it.passive.meta)
            passive.createPassive()
            passives.add(passive)
        }
    }

    override fun destroyKit() {
        abilities.forEach {
            it.destroyAbility()
        }
    }
}