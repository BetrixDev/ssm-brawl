package dev.betrix.supersmashmobsbrawl.commands

import dev.betrix.supersmashmobsbrawl.SuperSmashMobsBrawl
import dev.rollczi.litecommands.annotations.argument.Arg
import dev.rollczi.litecommands.annotations.command.Command
import dev.rollczi.litecommands.annotations.context.Context
import dev.rollczi.litecommands.annotations.execute.Execute
import dev.rollczi.litecommands.annotations.permission.Permission
import org.bukkit.entity.Player

@Command(name = "schematic")
@Permission("ssmb.schematic")
class SchematicCommand {
    private val plugin = SuperSmashMobsBrawl.instance

    @Execute(name = "edit")
    fun schematicEdit(@Context sender: Player, @Arg mapId: String) {
        plugin.editorManager.createEditorSession(arrayListOf(sender), mapId)
    }

    @Execute(name = "save")
    fun schematicSave(@Context sender: Player) {
        plugin.editorManager.saveSession(sender)
    }

    @Execute(name = "leave")
    fun schematicLeave(@Context sender: Player) {
        plugin.editorManager.removePlayerFromSession(sender)
    }
}