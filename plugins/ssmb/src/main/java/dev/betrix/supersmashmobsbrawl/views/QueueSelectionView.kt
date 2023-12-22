package dev.betrix.supersmashmobsbrawl.views

import dev.betrix.supersmashmobsbrawl.SuperSmashMobsBrawl
import dev.betrix.supersmashmobsbrawl.enums.LangEntry
import dev.triumphteam.gui.builder.item.ItemBuilder
import dev.triumphteam.gui.guis.Gui
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.Listener

class QueueSelectionView constructor(private val player: Player) : Listener {

    private val plugin = SuperSmashMobsBrawl.instance

    fun showMenuToPlayer() {
        val guiTitle = plugin.lang.getComponent(LangEntry.GUI_QUEUE_TITLE)
        val gui = Gui.gui().title(guiTitle).rows(5).create()

        val twoPlayerSingles = ItemBuilder.from(Material.GOLD_BLOCK).asGuiItem {
            Bukkit.dispatchCommand(player, "queue two_player_singles")
        }

        gui.setItem(2, 4, twoPlayerSingles)

        gui.filler.fill(ItemBuilder.from(Material.BLACK_STAINED_GLASS_PANE).asGuiItem())

        gui.open(player)
    }
}