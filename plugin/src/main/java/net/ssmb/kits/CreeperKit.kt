package net.ssmb.kits

import br.com.devsrsouza.kotlinbukkitapi.extensions.item
import net.ssmb.SSMB
import net.ssmb.abilities.SSMBAbility
import net.ssmb.dtos.minigame.MinigameStartSuccess
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.EquipmentSlot

class CreeperKit(
    private val plugin: SSMB,
    private val player: Player,
    private val kitData: MinigameStartSuccess.PlayerData.KitData
) : IKit {
    private val abilities = arrayListOf<SSMBAbility>()
    private val playerInv = player.inventory

    override fun initializeKit() {
        playerInv.clear()

        playerInv.setItem(EquipmentSlot.HEAD, item(Material.LEATHER_HELMET))
        playerInv.setItem(EquipmentSlot.CHEST, item(Material.LEATHER_CHESTPLATE))
        playerInv.setItem(EquipmentSlot.LEGS, item(Material.LEATHER_LEGGINGS))
        playerInv.setItem(EquipmentSlot.FEET, item(Material.IRON_BOOTS))

        kitData.abilities.forEachIndexed { idx, it ->
            val ability = SSMBAbility.getAbilityFromId(
                it.ability.id,
                player,
                plugin,
                it.ability.cooldown,
                it.ability.meta,
                idx
            )

            ability.initializeAbility()

            abilities.add(ability)
        }
    }

    override fun destroyKit() {
        abilities.forEach {
            it.destroyAbility()
        }
    }
}