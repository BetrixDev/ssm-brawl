package dev.betrix.superSmashMobsBrawl.commands

import dev.betrix.superSmashMobsBrawl.services.KitService
import dev.betrix.superSmashMobsBrawl.services.LangService
import dev.rollczi.litecommands.annotations.command.Command
import dev.rollczi.litecommands.annotations.context.Context
import dev.rollczi.litecommands.annotations.execute.Execute
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@Command(name = "kit")
class KitCommand : KoinComponent {
    private val lang: LangService by inject()

    @Execute(name = "cow")
    fun cowKit(@Context player: Player) {
        KitService.playerSelectKit(player, "cow")

        onSuccess(player, "cow")
    }

    @Execute(name = "creeper")
    fun creeperKit(@Context player: Player) {
        KitService.playerSelectKit(player, "creeper")

        onSuccess(player, "creeper")
    }

    @Execute(name = "skeleton")
    fun skeletonKit(@Context player: Player) {
        KitService.playerSelectKit(player, "skeleton")

        onSuccess(player, "skeleton")
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

    private fun onSuccess(player: Player, kitId: String) {
        player.sendMessage(lang.t("messages.kits.select.success") { "kitId" to kitId })
        player.playSound(player.location, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f)
    }
}
