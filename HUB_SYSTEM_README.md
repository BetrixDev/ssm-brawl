# Hub System for Super Smash Mobs Brawl

## Overview

The hub system provides a centralized lobby area for players to gather, access game features, and prepare for matches. Players are automatically teleported to the hub when they join the server and receive special abilities while in hub worlds.

## Features

### Core Functionality
- **Automatic Hub Teleportation**: Players are automatically sent to the hub when joining the server
- **Double Jump Ability**: Players receive a double jump passive ability while in hub worlds
- **Comprehensive Protection**: Players are protected from damage, block breaking/placing, and other interactions
- **Multiple Hub Support**: System is designed to support multiple hub worlds in the future
- **Map-Based Configuration**: Hub configuration is integrated with the existing map system

### Protection Features
- **Damage Protection**: Players cannot take damage from any source
- **Block Protection**: Players cannot break or place blocks (unless in creative mode)
- **Item Pickup Protection**: Players cannot accidentally pick up items
- **Weather Protection**: Hub worlds maintain clear weather
- **Entity Targeting Protection**: Mobs cannot target players in hub worlds

## Map Configuration

The hub system uses the existing map configuration system. Hub maps are defined using the same DSL as other maps:

```kotlin
val blueForestHub = defineMap {
    name = "Blue Forest Hub"
    id = "blue_forest"
    description = "The main hub world for players to gather and prepare for matches"
    voidLevel = 0
    type = MapType.HUB
    maxPlayers = null // Hubs don't have player limits

    addCreator("PLACEHOLDER_UUID")

    spawnPoints {
        at(0.0, 100.0, 0.0)
    }
}
```

### Map Types
- `MapType.MINIGAME` - For game maps
- `MapType.HUB` - For hub/lobby maps

## Architecture

### Services

#### HubService
- Manages hub world registration and player hub interactions
- Handles player teleportation to hub worlds
- Manages passive ability assignment and removal
- Tracks players currently in hub worlds

#### HubProtectionService
- Provides comprehensive protection for hub worlds
- Handles all protection-related events
- Configurable protection settings

#### MapRegistry
- Manages all maps including hub maps
- Provides utility methods for hub map operations
- Integrates with existing map system

### Data Classes

#### SsmbMap
```kotlin
data class SsmbMap(
    val name: String,
    val id: String,
    val type: MapType,
    val maxPlayers: Int?,
    val description: String,
    val voidLevel: Int,
    val creatorUuids: List<String>,
    val spawnPoints: List<Vector>
)
```

## Usage Examples

### Basic Hub Setup
1. Create a hub map definition in the maps directory
2. Register the map in MapRegistry
3. Ensure the world exists on your server
4. Players will be teleported to the hub when they join

### Adding Multiple Hubs
```kotlin
// Create additional hub maps
val lobbyHub = defineMap {
    name = "Lobby Hub"
    id = "lobby"
    description = "A secondary hub for special events"
    type = MapType.HUB
    maxPlayers = null
    
    addCreator("PLACEHOLDER_UUID")
    
    spawnPoints {
        at(10.0, 64.0, 10.0)
    }
}

// Register in MapRegistry
MapRegistry.register(lobbyHub)
```

### Working with Hub Maps
```kotlin
// Get all hub maps
val hubMaps = MapRegistry.getHubMaps()

// Get default hub
val defaultHub = MapRegistry.getDefaultHub()

// Check if a map is a hub
val isHub = MapRegistry.isHubMap("blue_forest")
```

## Integration with Existing Systems

### Passive System
The hub system integrates with the existing passive ability system:
- Uses `DoubleJumpPassiveDefinition` for the double jump ability
- Automatically manages passive lifecycle (setup/teardown)
- Prevents conflicts with other passive abilities

### Map System
- Integrates with the existing map definition system
- Uses the same DSL and builder pattern as other maps
- Maintains consistency with existing map architecture

## Error Handling

The hub system includes comprehensive error handling:
- Graceful handling of missing worlds
- Logging of important events and errors
- Fallback behavior when configuration is invalid
- Cleanup of resources on plugin disable

## Performance Considerations

- Efficient player tracking using sets and maps
- Minimal event overhead with early returns
- Configurable protection to reduce unnecessary event processing
- Clean resource management

## Future Enhancements

The hub system is designed to be extensible for future features:
- Multiple hub worlds with different themes
- Hub-specific passive abilities
- Hub teleportation portals
- Hub-specific game modes
- Hub statistics and analytics