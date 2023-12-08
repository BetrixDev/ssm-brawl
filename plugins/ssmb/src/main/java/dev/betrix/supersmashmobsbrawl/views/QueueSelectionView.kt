package dev.betrix.supersmashmobsbrawl.views

import br.com.devsrsouza.kotlinbukkitapi.extensions.item
import br.com.devsrsouza.kotlinbukkitapi.menu.dsl.menu
import br.com.devsrsouza.kotlinbukkitapi.menu.dsl.slot
import dev.betrix.supersmashmobsbrawl.SuperSmashMobsBrawl
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.Listener

class QueueSelectionView constructor(plugin: SuperSmashMobsBrawl, private val player: Player) : Listener {
    companion object {
        suspend fun showMenuToPlayer(player: Player) {
            val view = menu("<bold><gold>Join a Queue", 3, SuperSmashMobsBrawl.instance, true) {
                slot(1, 3, item(Material.IRON_BLOCK, 1, 1) {
                    displayName(MiniMessage.miniMessage().deserialize("<bold><aqua>Casual Queue"))
                }) {
                    onClick {
                        Bukkit.dispatchCommand(player, "queue casual")
                        close()
                    }
                }

                slot(1, 5, item(Material.GOLD_BLOCK, 1, 1) {
                    displayName(MiniMessage.miniMessage().deserialize("<bold><aqua>Ranked Queue"))
                }) {
                    onClick {
                        Bukkit.dispatchCommand(player, "queue ranked")
                        close()
                    }
                }
            }

            view.openToPlayer(player)
        }
    }

}