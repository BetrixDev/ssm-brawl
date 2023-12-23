package dev.betrix.supersmashmobsbrawl.managers

import com.sk89q.worldedit.EditSession
import com.sk89q.worldedit.WorldEdit
import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard
import com.sk89q.worldedit.extent.clipboard.io.BuiltInClipboardFormat
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats
import com.sk89q.worldedit.function.operation.ForwardExtentCopy
import com.sk89q.worldedit.function.operation.Operations
import com.sk89q.worldedit.math.BlockVector3
import com.sk89q.worldedit.math.Vector3
import com.sk89q.worldedit.regions.CuboidRegion
import com.sk89q.worldedit.session.ClipboardHolder
import com.sk89q.worldedit.util.SideEffectSet
import com.sk89q.worldedit.util.io.Closer
import org.bukkit.Location
import org.bukkit.World
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

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

    fun saveSchematic(origin: Vector3, radius: Double, lowerLimit: Double, upperLimit: Double) {
        try {
            Closer.create().use { closer ->
                val region = CuboidRegion(
                    BlockVector3.at(radius + origin.x, lowerLimit + origin.y, -radius + origin.z),
                    BlockVector3.at(-radius + origin.x, upperLimit + origin.y, radius + origin.z)
                )

                val editSession = createEditSession()

                val clipboard = BlockArrayClipboard(region)
                val copy = ForwardExtentCopy(editSession, region, clipboard, region.minimumPoint)

                Operations.complete(copy)

                val outputStream = closer.register(FileOutputStream(schematicPath))
                val writer = closer.register(BuiltInClipboardFormat.FAST.getWriter(outputStream))

                writer.write(clipboard)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun createEditSession(): EditSession {
        val world = BukkitAdapter.adapt(bukkitWorld)
        val session = WorldEdit.getInstance().newEditSession(world)

        session.sideEffectApplier = SideEffectSet.defaults()

        return session!!
    }
}