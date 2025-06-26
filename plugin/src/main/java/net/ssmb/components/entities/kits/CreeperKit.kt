package net.ssmb.components.entities.kits

import br.com.devsrsouza.kotlinbukkitapi.extensions.item
import net.ssmb.blockwork.addTag
import net.ssmb.blockwork.annotations.Component
import net.ssmb.blockwork.components.EntityComponent
import net.ssmb.blockwork.getTags
import net.ssmb.blockwork.interfaces.OnDestroy
import net.ssmb.blockwork.interfaces.OnStart
import net.ssmb.blockwork.removeTag
import org.bukkit.Material
import org.bukkit.entity.Player

@Component("kit_creeper")
class CreeperKit: EntityComponent<Player>(), OnStart, OnDestroy {

    private val player = entity
    private val playerInv = player.inventory

    override fun onStart() {
        playerInv.clear()

        playerInv.boots = item(Material.IRON_BOOTS)
        playerInv.leggings = item(Material.LEATHER_LEGGINGS)
        playerInv.chestplate = item(Material.LEATHER_CHESTPLATE)
        playerInv.helmet = item(Material.LEATHER_HELMET)

        player.addTag("ability_sulphur_bomb")
        player.addTag("ability_explode")
        player.addTag("passive_double_jump")
        player.addTag("disguise_creeper")
    }

    override fun onDestroy() {
        player.getTags().forEach {
            player.removeTag(it)
        }
    }
}