package dev.betrix.supersmashmobsbrawl.commands

import dev.betrix.supersmashmobsbrawl.SuperSmashMobsBrawl
import dev.rollczi.litecommands.annotations.command.Command
import dev.rollczi.litecommands.annotations.context.Context
import dev.rollczi.litecommands.annotations.execute.Execute
import dev.rollczi.litecommands.annotations.permission.Permission
import org.bukkit.entity.Player

@Command(name = "shutdown")
@Permission("ssmb.shutdown")
class ShutdownCommand {
    private val plugin = SuperSmashMobsBrawl.instance

    @Execute
    fun shutdown(@Context sender: Player) {
        plugin.prepareShutdown()
    }
}