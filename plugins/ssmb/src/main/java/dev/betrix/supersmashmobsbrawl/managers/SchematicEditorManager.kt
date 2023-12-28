package dev.betrix.supersmashmobsbrawl.managers

import com.github.shynixn.mccoroutine.bukkit.launch
import com.sk89q.worldedit.math.Vector3
import dev.betrix.supersmashmobsbrawl.SuperSmashMobsBrawl
import dev.betrix.supersmashmobsbrawl.maps.EditableMap
import kotlinx.coroutines.Dispatchers
import org.bukkit.entity.Player

class SchematicEditorManager {
    private val plugin = SuperSmashMobsBrawl.instance

    private val editorSessions = arrayListOf<EditorSession>()

    fun createEditorSession(players: ArrayList<Player>, mapId: String) {
        plugin.logger.info("attempting to create editor session")
        val canCreateSession = editorSessions.find { it.players.find { p -> players.contains(p) } != null } == null

        if (!canCreateSession) {
            players.forEach {
                it.sendMessage("You are already in an editor session")
            }

            return
        }

        val editableMap = EditableMap("editor-${editorSessions.size}", mapId, players)

        editorSessions.add(EditorSession(players, editableMap))
    }

    fun saveSession(player: Player) {
        val session = editorSessions.find {
            it.players.contains(player)
        } ?: return

        plugin.launch(Dispatchers.IO) {
            val mapMetadata = session.map.readMetadata()

            val schematicOrigin = Vector3.at(
                mapMetadata.schematicLocation.x,
                mapMetadata.schematicLocation.y,
                mapMetadata.schematicLocation.z
            )

            session.map.schematicManager.saveSchematic(
                schematicOrigin,
                mapMetadata.schematicRadius,
                mapMetadata.schematicLowerLimit,
                mapMetadata.schematicUpperLimit
            )

            session.players.forEach {
                plugin.hub.teleportPlayer(it)
            }

            session.map.destroyWorld()
            editorSessions.remove(session)
        }
    }

    fun removePlayerFromSession(player: Player) {
        val session = editorSessions.find {
            it.players.contains(player)
        } ?: return

        plugin.hub.teleportPlayer(player)

        if (session.players.size == 1) {
            destroySessionWithPlayer(player)
        } else {
            session.players.remove(player)
        }
    }

    private fun destroySessionWithPlayer(player: Player) {
        val session = editorSessions.find {
            it.players.contains(player)
        } ?: return

        session.players.forEach {
            plugin.hub.teleportPlayer(it)
        }

        session.map.destroyWorld()
        editorSessions.remove(session)
    }

    fun isPlayerInSession(player: Player): Boolean {
        return editorSessions.find {
            it.players.contains(player)
        } != null
    }
}

data class EditorSession(val players: ArrayList<Player>, val map: EditableMap)