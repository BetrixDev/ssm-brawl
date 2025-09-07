package dev.betrix.superSmashMobsBrawl.commands

import dev.betrix.superSmashMobsBrawl.SuperSmashMobsBrawl
import dev.betrix.superSmashMobsBrawl.components.LeaveSpecifier
import dev.betrix.superSmashMobsBrawl.components.PlayerTryLeaveComponent
import dev.betrix.superSmashMobsBrawl.extensions.ecsEntity
import dev.betrix.superSmashMobsBrawl.services.LangService
import dev.rollczi.litecommands.annotations.command.Command
import dev.rollczi.litecommands.annotations.context.Context
import dev.rollczi.litecommands.annotations.execute.Execute
import dev.rollczi.litecommands.annotations.optional.OptionalArg
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@Command(name = "leave")
class LeaveCommand(private val plugin: SuperSmashMobsBrawl) : KoinComponent {
    private val lang: LangService by inject()

    @Execute
    fun leave(@Context sender: CommandSender, @OptionalArg specifier: LeaveSpecifier?) {
        if (sender !is Player) {
            sender.sendMessage(lang.t("messages.commands.onlyPlayers"))
            return
        }

        with(plugin.ecsWorld) {
            sender.ecsEntity?.configure { it += PlayerTryLeaveComponent(specifier) }
        }
    }
}
