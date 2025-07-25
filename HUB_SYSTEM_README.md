# Hub System for Super Smash Mobs Brawl

## Overview

The hub system provides a centralized lobby area for players to gather, access game features, and prepare for matches. Players are automatically teleported to the hub when they join the server and receive special abilities while in hub worlds.

## Features

### Core Functionality
- **Automatic Hub Teleportation**: Players are automatically sent to the hub when joining the server
- **Double Jump Ability**: Players receive a double jump passive ability while in hub worlds
- **Comprehensive Protection**: Players are protected from damage, block breaking/placing, and other interactions
- **Multiple Hub Support**: System is designed to support multiple hub worlds in the future
- **Configurable Settings**: All hub behavior can be customized through configuration

### Protection Features
- **Damage Protection**: Players cannot take damage from any source
- **Block Protection**: Players cannot break or place blocks (unless in creative mode)
- **Item Pickup Protection**: Players cannot accidentally pick up items
- **Weather Protection**: Hub worlds maintain clear weather
- **Entity Targeting Protection**: Mobs cannot target players in hub worlds

## Configuration

The hub system uses the main plugin configuration file (`config.yml`). The following settings are available:

```yaml
hub:
  # Default hub world name
  default-world: "blue_forest"
  
  # Hub spawn location
  spawn:
    x: 0.0
    y: 100.0
    z: 0.0
    yaw: 0.0
    pitch: 0.0
  
  # Enable double jump ability in hubs
  enable-double-jump: true
  
  # Protection settings
  protection:
    enabled: true
    prevent-damage: true
    prevent-block-break: true
    prevent-block-place: true
    prevent-item-pickup: true
    prevent-weather: true
```

## Commands

### `/hub`
- **Permission**: `ssmb.hub.use`
- **Description**: Teleports the player to the default hub world
- **Usage**: `/hub`

### `/hub list`
- **Permission**: `ssmb.hub.admin`
- **Description**: Lists all registered hub worlds
- **Usage**: `/hub list`

### `/hub info`
- **Permission**: `ssmb.hub.admin`
- **Description**: Shows information about the current hub configuration
- **Usage**: `/hub info`

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

#### HubConfig
- Manages hub-related configuration settings
- Provides centralized access to hub configuration
- Handles default value initialization

### Data Classes

#### HubWorld
```kotlin
data class HubWorld(
    val id: String,
    val world: World,
    val spawnLocation: Location
)
```

## Usage Examples

### Basic Hub Setup
1. Ensure the `blue_forest` world exists on your server
2. The hub system will automatically register it on plugin startup
3. Players will be teleported to the hub when they join

### Adding Multiple Hubs (Future)
```kotlin
// Register additional hub worlds
HubService.registerHubWorld("lobby", HubWorld("lobby", world, spawnLocation))
HubService.registerHubWorld("vip", HubWorld("vip", vipWorld, vipSpawnLocation))
```

### Customizing Protection
```yaml
# Disable block breaking protection
hub:
  protection:
    prevent-block-break: false

# Disable double jump
hub:
  enable-double-jump: false
```

## Integration with Existing Systems

### Passive System
The hub system integrates with the existing passive ability system:
- Uses `DoubleJumpPassiveDefinition` for the double jump ability
- Automatically manages passive lifecycle (setup/teardown)
- Prevents conflicts with other passive abilities

### Command System
- Integrates with the existing LiteCommands framework
- Follows the same permission and command structure
- Maintains consistency with other plugin commands

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