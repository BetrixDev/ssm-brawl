package dev.betrix.superSmashMobsBrawl.commands

import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.onSuccess
import dev.betrix.superSmashMobsBrawl.kits.definitions.CreeperKitDefinition
import dev.betrix.superSmashMobsBrawl.services.KitService
import dev.rollczi.litecommands.annotations.command.Command
import dev.rollczi.litecommands.annotations.context.Context
import dev.rollczi.litecommands.annotations.execute.Execute
import org.bukkit.entity.Player

@Command(name = "kit")
class KitCommand {

    @Execute(name = "creeper")
    fun creeperKit(@Context player: Player) {
        // First, unassign any existing kit
        KitService.unassignKit(player)
        
        // Assign the Creeper kit
        KitService.assignKit(player, CreeperKitDefinition)
            .onSuccess {
                player.sendMessage("§aYou have been given the Creeper kit!")
                player.sendMessage("§7Use the items in your hotbar to activate abilities.")
            }
            .onFailure { error ->
                player.sendMessage("§cFailed to assign kit: $error")
            }
    }
    
    @Execute(name = "clear")
    fun clearKit(@Context player: Player) {
        val removedKit = KitService.unassignKit(player)
        if (removedKit != null) {
            player.sendMessage("§aYour kit has been cleared.")
        } else {
            player.sendMessage("§cYou don't have a kit to clear.")
        }
    }
}