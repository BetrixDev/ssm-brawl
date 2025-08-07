package dev.betrix.superSmashMobsBrawl.commands

import dev.betrix.superSmashMobsBrawl.services.KitService
import dev.betrix.superSmashMobsBrawl.utils.mm
import dev.rollczi.litecommands.annotations.command.Command
import dev.rollczi.litecommands.annotations.context.Context
import dev.rollczi.litecommands.annotations.execute.Execute
import org.bukkit.Sound
import org.bukkit.entity.Player

@Command(name = "kit")
class KitCommand {

    @Execute(name = "creeper")
    fun creeperKit(@Context player: Player) {
        KitService.playerSelectKit(player, "creeper")

        onSuccess(player, "Creeper")
    }

    @Execute(name = "skeleton")
    fun skeletonKit(@Context player: Player) {
        KitService.playerSelectKit(player, "skeleton")

        onSuccess(player, "Skeleton")
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

    private fun onSuccess(player: Player, kitName: String) {
        player.sendMessage(mm("<light_purple>You have selected kit $kitName</light_purple>"))
        player.playSound(player.location, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f)
    }
}
