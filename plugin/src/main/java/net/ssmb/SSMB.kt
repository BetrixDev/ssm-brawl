package net.ssmb

import net.ssmb.services.ApiService
import org.bukkit.plugin.java.JavaPlugin

class SSMB : JavaPlugin() {
    lateinit var api: ApiService

    override fun onEnable() {
        api = ApiService()
    }

    override fun onDisable() {
        // Plugin shutdown logic
    }
}
