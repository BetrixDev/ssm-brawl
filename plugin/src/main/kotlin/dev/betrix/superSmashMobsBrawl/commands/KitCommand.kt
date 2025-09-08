package dev.betrix.superSmashMobsBrawl.commands

import dev.betrix.superSmashMobsBrawl.models.brawlData.KitDef
import dev.betrix.superSmashMobsBrawl.models.brawlData.KitSwitchingMode
import dev.betrix.superSmashMobsBrawl.services.KitService
import dev.betrix.superSmashMobsBrawl.services.LangService
import dev.betrix.superSmashMobsBrawl.services.MinigameService
import dev.rollczi.litecommands.annotations.argument.Arg
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
    private val kitService: KitService by inject()
    private val minigameService: MinigameService by inject()

    @Execute
    fun kit(@Context player: Player) {
        kitService.openKitSelectionGui(player)
    }

    @Execute
    fun kit(@Context player: Player, @Arg kit: KitDef) {
        kitService.playerSelectKit(player, kit)

        // Determine the message to show based on the player's current minigame
        val currentMinigame = minigameService.getMinigameForPlayer(player)
        val messageKey =
            when (currentMinigame?.kitSwitchingMode) {
                KitSwitchingMode.NEVER -> "messages.kits.select.success_never"
                KitSwitchingMode.ON_DEATH -> "messages.kits.select.success_on_death"
                KitSwitchingMode.IMMEDIATE -> "messages.kits.select.success_immediate"
                null -> "messages.kits.select.success" // Player not in minigame
            }

        player.sendMessage(lang.t(messageKey) { "kitId" to kit.id })
        player.playSound(
            player.location,
            kit.selectionSound ?: Sound.ENTITY_EXPERIENCE_ORB_PICKUP,
            1f,
            1f,
        )
    }
}
