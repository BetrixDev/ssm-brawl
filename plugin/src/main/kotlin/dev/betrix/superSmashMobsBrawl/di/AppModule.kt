package dev.betrix.superSmashMobsBrawl.di

import dev.betrix.superSmashMobsBrawl.SuperSmashMobsBrawl
import dev.betrix.superSmashMobsBrawl.services.DebugService
import dev.betrix.superSmashMobsBrawl.services.HotbarService
import dev.betrix.superSmashMobsBrawl.services.HubProtectionService
import dev.betrix.superSmashMobsBrawl.services.HubService
import dev.betrix.superSmashMobsBrawl.services.KitService
import dev.betrix.superSmashMobsBrawl.services.MinigameService
import dev.betrix.superSmashMobsBrawl.services.WorldService
import gg.flyte.twilight.Twilight
import org.bukkit.plugin.java.JavaPlugin
import org.koin.dsl.module

/**
 * Koin dependency injection module for the Super Smash Mobs Brawl plugin.
 * 
 * This module defines all the dependencies that can be injected into classes
 * throughout the plugin, providing a clean separation of concerns and easier testing.
 */
val appModule = module {
    
    // Core plugin dependencies
    single<JavaPlugin> { get<SuperSmashMobsBrawl>() }
    single<SuperSmashMobsBrawl> { SuperSmashMobsBrawl.instance }
    single<Twilight> { get<SuperSmashMobsBrawl>().twilight }
    
    // Services - these are singletons that will be created once and reused
    single { DebugService }
    single { HubService }
    single { WorldService }
    single { HotbarService }
    single { HubProtectionService }
    single { KitService }
    single { MinigameService }
}