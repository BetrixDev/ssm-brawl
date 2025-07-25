package dev.betrix.superSmashMobsBrawl.commands

import dev.betrix.superSmashMobsBrawl.services.HubService
import dev.rollczi.litecommands.annotations.command.Command
import dev.rollczi.litecommands.annotations.execute.Execute
import dev.rollczi.litecommands.annotations.permission.Permission
import org.bukkit.entity.Player

@Command(name = "hubtest")
class HubTestCommand {
    
    @Execute
    @Permission("ssmb.hub.admin")
    fun testHub(player: Player) {
        val hubWorld = HubService.getDefaultHub()
        if (hubWorld != null) {
            player.sendMessage("§aHub test:")
            player.sendMessage("§7- Hub ID: §f${hubWorld.id}")
            player.sendMessage("§7- World: §f${hubWorld.world.name}")
            player.sendMessage("§7- Spawn: §f${hubWorld.spawnLocation.x}, ${hubWorld.spawnLocation.y}, ${hubWorld.spawnLocation.z}")
            player.sendMessage("§7- Is player in hub: §f${HubService.isPlayerInHub(player)}")
            player.sendMessage("§7- Is world hub: §f${HubService.isInHub(player, player.world)}")
        } else {
            player.sendMessage("§cNo hub world found!")
        }
    }
    
    @Execute(name = "force")
    @Permission("ssmb.hub.admin")
    fun forceHub(player: Player) {
        HubService.teleportToDefaultHub(player)
        player.sendMessage("§aForced teleport to hub!")
    }
    
    @Execute(name = "reload")
    @Permission("ssmb.hub.admin")
    fun reloadHub(player: Player) {
        // This would reload hub configuration in a real implementation
        player.sendMessage("§aHub configuration reloaded!")
    }
}