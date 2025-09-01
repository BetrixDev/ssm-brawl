package dev.betrix.superSmashMobsBrawl.systems.scoreboards

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import com.github.quillraven.fleks.World.Companion.inject
import dev.betrix.superSmashMobsBrawl.SuperSmashMobsBrawl
import dev.betrix.superSmashMobsBrawl.components.PlayerComponent
import dev.betrix.superSmashMobsBrawl.components.ScoreboardComponent
import dev.betrix.superSmashMobsBrawl.services.LangService
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.minimessage.MiniMessage

class ScoreboardSystem(
    val lang: LangService = inject(),
    val plugin: SuperSmashMobsBrawl = inject()
) : IteratingSystem(
    family { all(ScoreboardComponent) }
) {
    override fun onTickEntity(entity: Entity) {
        val scoreboardComponent = entity[ScoreboardComponent]
        val player = entity[PlayerComponent].player

        val playerTier = "Supporter"
        val playerLevel = 15
        val playerKills = 0
        val playerDeaths = 0
        val playerWinStreak = 0
        val onlinePlayers = plugin.server.onlinePlayers.size
        val maxPlayers = plugin.server.maxPlayers

        scoreboardComponent.scoreboard.apply {
            updateSidebarTitle(
                MiniMessage.miniMessage().deserialize(
                    "<gradient:#89b4fa:#cba6f7:#f5c2e7>SSM Brawl</gradient>"
                )
            )

            updateSidebarLines(
                MiniMessage.miniMessage().deserialize("<#cdd6f4>Player <#a6e3a1>${player.name}"),
                MiniMessage.miniMessage().deserialize("<#f9e2af>Tier <#fab387>$playerTier"),
                MiniMessage.miniMessage().deserialize("<#b4befe>Level <#89b4fa>$playerLevel"),
                MiniMessage.miniMessage().deserialize("<#f38ba8>Kills <#a6e3a1>$playerKills"),
                MiniMessage.miniMessage().deserialize("<#eba0ac>Deaths <#f2cdcd>$playerDeaths"),
                MiniMessage.miniMessage().deserialize("<#94e2d5>Win Streak <#a6e3a1>$playerWinStreak"),
                MiniMessage.miniMessage().deserialize("<#6c7086>"),
                MiniMessage.miniMessage()
                    .deserialize("<#b4befe>Online <#89dceb>$onlinePlayers<#b4befe> / <#89b4fa>$maxPlayers")
            )

            updateTabList(
                header = {
                    MiniMessage.miniMessage().deserialize(
                        "<gradient:#89b4fa:#cba6f7:#f5c2e7>Super Smash Mobs: Brawl</gradient><newline><#a6e3a1>Welcome, <#f5c2e7>${player.name}<newline>"
                    )
                },
                footer = {
                    Component.text()
                        .append(Component.text("Players: ", NamedTextColor.WHITE))
                        .append(Component.text("$onlinePlayers", NamedTextColor.AQUA))
                        .append(Component.text(" / ", NamedTextColor.DARK_GRAY))
                        .append(Component.text("$maxPlayers", NamedTextColor.LIGHT_PURPLE))
                        .appendNewline()
                        .append(Component.text("Kills: ", NamedTextColor.LIGHT_PURPLE))
                        .append(Component.text("$playerKills", NamedTextColor.GREEN))
                        .append(Component.text("  Deaths: ", NamedTextColor.LIGHT_PURPLE))
                        .append(Component.text("$playerDeaths", NamedTextColor.RED))
                        .append(Component.text("  Streak: ", NamedTextColor.LIGHT_PURPLE))
                        .append(Component.text("$playerWinStreak", NamedTextColor.GOLD))
                        .appendNewline()
                        .append(Component.text("play.ssmbrawl.com", NamedTextColor.YELLOW))
                        .build()
                }
            )
        }
    }
}