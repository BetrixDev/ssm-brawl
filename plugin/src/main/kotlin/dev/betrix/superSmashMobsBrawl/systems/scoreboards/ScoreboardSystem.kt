package dev.betrix.superSmashMobsBrawl.systems.scoreboards

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import com.github.quillraven.fleks.World.Companion.inject
import dev.betrix.superSmashMobsBrawl.SuperSmashMobsBrawl
import dev.betrix.superSmashMobsBrawl.Ticker
import dev.betrix.superSmashMobsBrawl.components.PlayerComponent
import dev.betrix.superSmashMobsBrawl.components.ScoreboardComponent
import dev.betrix.superSmashMobsBrawl.services.LangService
import gg.flyte.twilight.extension.toComponent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.minimessage.MiniMessage

class ScoreboardSystem(
    val lang: LangService = inject(),
    val plugin: SuperSmashMobsBrawl = inject()
) : IteratingSystem(
    family { all(ScoreboardComponent) }
) {
    private val ticker = Ticker(120)

    override fun onTickEntity(entity: Entity) {
        val scoreboardComponent = entity[ScoreboardComponent]
        val player = entity[PlayerComponent].player

        val playerKills = 0
        val playerDeaths = 0
        val playerWinStreak = 0
        val onlinePlayers = plugin.server.onlinePlayers.size
        val maxPlayers = plugin.server.maxPlayers

        scoreboardComponent.scoreboard.apply {
            updateSidebarTitle(
                MiniMessage.miniMessage().deserialize(
                    "<bold><transition:#f5c2e7:#cba6f7:#f5c2e7:${ticker.nextPercent()}>SSM Brawl</transition></bold>"
                )
            )

            updateSidebarLines(
                "<#1e1e2e><st>                           ".toComponent(),
                Component.empty(),
                MiniMessage.miniMessage().deserialize("<#45475a>» <#cdd6f4>Player <#a6e3a1>${player.name}"),
                Component.empty(),
                MiniMessage.miniMessage().deserialize("<#b4befe>Online <#89dceb>$onlinePlayers<#b4befe> / <#89b4fa>$maxPlayers"),
                Component.empty(),
                "<#1e1e2e><st>                           ".toComponent(),
            )

            updateTabList(
                header = {
                    MiniMessage.miniMessage().deserialize(
                        "<gradient:#89b4fa:#cba6f7:#f5c2e7>Super Smash Mobs: Brawl</gradient><newline><#a6e3a1>Welcome, <#f5c2e7>${player.name}<newline>"
                    )
                },
                footer = {
                    Component.text()
                        .appendNewline()
                        .append(Component.text("Players: ", NamedTextColor.WHITE))
                        .append(Component.text("$onlinePlayers", NamedTextColor.AQUA))
                        .append(Component.text(" / ", NamedTextColor.DARK_GRAY))
                        .append(Component.text("$maxPlayers", NamedTextColor.LIGHT_PURPLE))
                        .appendNewline()
                        .append(Component.text("play.ssmbrawl.com", NamedTextColor.YELLOW))
                        .build()
                }
            )
        }
    }
}