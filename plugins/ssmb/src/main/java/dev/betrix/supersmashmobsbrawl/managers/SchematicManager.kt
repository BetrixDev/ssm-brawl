package dev.betrix.supersmashmobsbrawl.managers

import com.sk89q.worldedit.EditSession
import com.sk89q.worldedit.WorldEdit
import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats
import com.sk89q.worldedit.function.operation.Operations
import com.sk89q.worldedit.session.ClipboardHolder
import com.sk89q.worldedit.util.SideEffectSet
import org.bukkit.Location
import org.bukkit.World
import java.io.File
import java.io.FileInputStream

class SchematicManager(private val bukkitWorld: World, private val schematicPath: String) {

    fun pasteSchematic(origin: Location) {
        try {
            val session = createEditSession()
            val schematicFile = File(schematicPath)

            session.use {
                val format = ClipboardFormats.findByFile(schematicFile)!!
                val reader = format.getReader(FileInputStream(schematicFile))
                val schematic = reader.read()
                val operation = ClipboardHolder(schematic)
                    .createPaste(session)
                    .to(BukkitAdapter.asBlockVector(origin))
                    .build()

                Operations.complete(operation)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun saveSchematic() {

    }

    private fun createEditSession(): EditSession {
        val world = BukkitAdapter.adapt(bukkitWorld)
        val session = WorldEdit.getInstance().newEditSession(world)

        session.sideEffectApplier = SideEffectSet.defaults()

        return session!!
    }
}