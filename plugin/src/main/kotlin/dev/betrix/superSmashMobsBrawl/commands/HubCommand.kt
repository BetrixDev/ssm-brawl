package dev.betrix.superSmashMobsBrawl.commands

import dev.betrix.superSmashMobsBrawl.services.HubService
import dev.rollczi.litecommands.annotations.argument.Arg
import dev.rollczi.litecommands.annotations.command.Command
import dev.rollczi.litecommands.annotations.execute.Execute
import dev.rollczi.litecommands.annotations.permission.Permission
import org.bukkit.entity.Player

@Command(name = "hub")
class HubCommand {
    
    @Execute
    @Permission("ssmb.hub.use")
    fun hub(player: Player) {
        val hubWorld = HubService.getDefaultHub()
        if (hubWorld != null) {
            HubService.teleportToHub(player, hubWorld)
            player.sendMessage("§aWelcome to the hub!")
        } else {
            player.sendMessage("§cHub is not available at the moment.")
        }
    }
    
    @Execute(name = "list")
    @Permission("ssmb.hub.admin")
    fun listHubs(player: Player) {
        val hubs = HubService.getHubWorlds()
        if (hubs.isEmpty()) {
            player.sendMessage("§cNo hub worlds registered.")
            return
        }
        
        player.sendMessage("§6=== Hub Worlds ===")
        hubs.forEach { (id, hubWorld) ->
            val status = if (hubWorld.world.environment == org.bukkit.World.Environment.NORMAL) "§aOnline" else "§cOffline"
            player.sendMessage("§7- §f$id §7($status)")
        }
    }
    
    @Execute(name = "info")
    @Permission("ssmb.hub.admin")
    fun hubInfo(player: Player) {
        val hubWorld = HubService.getDefaultHub()
        if (hubWorld != null) {
            player.sendMessage("§6=== Hub Info ===")
            player.sendMessage("§7ID: §f${hubWorld.id}")
            player.sendMessage("§7World: §f${hubWorld.world.name}")
            player.sendMessage("§7Spawn: §f${hubWorld.spawnLocation.x}, ${hubWorld.spawnLocation.y}, ${hubWorld.spawnLocation.z}")
            player.sendMessage("§7Players in hub: §f${HubService.getHubWorlds().size}")
        } else {
            player.sendMessage("§cNo default hub configured.")
        }
    }
}