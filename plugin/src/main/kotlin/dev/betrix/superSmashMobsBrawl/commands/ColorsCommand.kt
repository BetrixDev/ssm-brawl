package dev.betrix.superSmashMobsBrawl.commands

import dev.betrix.superSmashMobsBrawl.services.LangService
import dev.rollczi.litecommands.annotations.command.Command
import dev.rollczi.litecommands.annotations.context.Context
import dev.rollczi.litecommands.annotations.execute.Execute
import org.bukkit.entity.Player
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@Command(name = "colors")
class ColorsCommand : KoinComponent {
    private val lang: LangService by inject()

    @Execute
    fun colors(@Context player: Player) {
        val colors = lang.getThemeColors()

        player.sendMessage(lang.t("messages.colors.header") { "count" to colors.size })

        player.sendMessage(lang.t("newline"))

        colors.forEach { (name, hex) ->
            player.sendMessage(
                lang.t("messages.colors.entry") {
                    "name" to name
                    "hex" to hex
                }
            )
        }

        player.sendMessage(lang.t("newline"))
    }
}

