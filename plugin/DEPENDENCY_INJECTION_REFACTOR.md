# Dependency Injection Refactoring Documentation

## Overview

This document describes the comprehensive refactoring that was done to implement Koin dependency injection throughout the Super Smash Mobs Brawl plugin and replace hardcoded definitions with data-driven definitions loaded from YAML files.

## Changes Made

### 1. Dependency Injection Implementation

#### Added Koin to All Instance Classes

The following instance classes now implement `KoinComponent` and inject `DataService`:

- **AbilityInstance**: Base class for all ability instances
- **KitInstance**: Base class for all kit instances  
- **MinigameInstance**: Base class for all minigame instances
- **PassiveInstance**: Base class for all passive instances
- **QueueService**: Service for managing game queues

Each of these classes can now access the injected `DataService` through:
```kotlin
protected val dataService: DataService by inject()
```

#### Updated Koin Module

The `KoinModules.kt` file now provides all major services as singletons:

```kotlin
val appModule = module {
    single { DataService }
    single { QueueService }
    single { KitService }
    single { MinigameService }
    single { HotbarService }
}
```

### 2. Data-Driven Definitions

#### Created DefinitionFactory

A new `DefinitionFactory` object was created that:
- Creates runtime definitions from YAML data
- Maps YAML data models to game definitions
- Uses reflection to instantiate the correct instance classes

#### Updated All Registries

All registries were refactored to load definitions from YAML data instead of hardcoded objects:

- **KitRegistry**: Loads kits from `kits.yml`
- **AbilityRegistry**: Loads abilities from `abilities.yml`
- **PassiveRegistry**: Loads passives from `passives.yml`
- **MinigameRegistry**: Loads minigames from `minigames.yml`

Each registry now:
1. Checks if a definition is already loaded
2. If not, loads it from DataService
3. Uses DefinitionFactory to create the runtime definition
4. Caches it for future use

#### Removed Hardcoded Definitions

The following hardcoded definition objects are no longer needed and can be removed:
- `CreeperKitDefinition`
- `SkeletonKitDefinition`
- `ExplosionAbilityDefinition`
- `SulphurBombAbilityDefinition`
- `BoneExplosionAbilityDefinition`
- `RopedArrowAbilityDefinition`
- `DoubleJumpPassiveDefinition`
- `RegenerationPassiveDefinition`
- `HungerPassiveDefinition`
- `TestingMinigameDefinition`
- `TwoPlayerDuelsMinigameDefinition`

### 3. Data Model Updates

#### Fixed KitDef Model

Updated the `KitDef` data model to match the YAML structure:
- Changed `passives` and `abilities` from single objects to lists
- Added optional metadata fields
- Added missing fields like `armor` and `knockbackMultiplier`

#### Added DataService Methods

Added utility methods to DataService:
- `getAllKitIds()`: Returns all kit IDs
- `getAllAbilityIds()`: Returns all ability IDs
- `getAllPassiveIds()`: Returns all passive IDs
- `getAllMinigameIds()`: Returns all minigame IDs

### 4. Service Initialization

#### Created RegistryService

A new `RegistryService` was created to initialize all registries after DataService has loaded the data:

```kotlin
object RegistryService : KoinComponent {
    fun initializeRegistries() {
        KitRegistry.getAllDefinitions()
        AbilityRegistry.getAllDefinitions()
        PassiveRegistry.getAllDefinitions()
        MinigameRegistry.getAllDefinitions()
    }
}
```

#### Updated Plugin Initialization

The main plugin class now initializes services in the correct order:
1. Start Koin
2. Load data with DataService
3. Initialize registries with RegistryService
4. Initialize other services

## Benefits

### 1. **Data-Driven Configuration**
- All game content is now defined in YAML files
- No need to recompile to add or modify kits, abilities, passives, or minigames
- Server operators can easily customize game content

### 2. **Clean Dependency Management**
- Dependencies are injected rather than accessed as singletons
- Easier to test and mock services
- Clear separation of concerns

### 3. **Extensibility**
- Easy to add new kits, abilities, passives, or minigames
- Just add the data to YAML and implement the instance class
- The factory will automatically handle the mapping

### 4. **Type Safety**
- Kotlin's type system ensures proper dependency resolution
- Compile-time checking of injected dependencies

## Usage Examples

### Accessing Data in Instances

Any instance class can now access game data through the injected DataService:

```kotlin
class MyAbilityInstance(definition: AbilityDefinition, player: Player) :
    AbilityInstance(definition, player) {
    
    override fun activate() {
        // Access kit data
        val kitData = dataService.getKit("creeper")
        
        // Access minigame data  
        val minigameData = dataService.getMinigame("two_player_duels")
        
        // Access other abilities
        val otherAbility = dataService.getAbility("sulphur_bomb")
    }
}
```

### Adding New Content

To add a new kit:

1. Add the kit data to `kits.yml`:
```yaml
- id: new_kit
  meleeDamage: 7.0
  armor: 5.0
  knockbackMultiplier: 1.5
  passives:
    - id: double_jump
  abilities:
    - id: sulphur_bomb
```

2. Create the instance class (if custom behavior is needed):
```kotlin
class NewKitInstance(definition: KitDefinition, player: Player, minigameDefinition: MinigameDefinition?) :
    KitInstance(definition, player, minigameDefinition) {
    // Custom kit behavior
}
```

3. Update DefinitionFactory to map the ID to the instance class

## Migration Notes

### For Developers

- All instance classes now have access to `dataService`
- Use injected services instead of accessing singletons directly
- When creating new instance classes, make sure to extend the appropriate base class and implement KoinComponent if needed

### For Server Operators

- All game content is now configured in YAML files in the `data` folder
- Changes to YAML files require a server restart to take effect
- Make sure YAML syntax is valid to avoid loading errors

## Future Improvements

1. **Hot Reloading**: Add ability to reload YAML data without server restart
2. **Validation**: Add comprehensive validation for YAML data
3. **Custom Instance Mapping**: Move instance class mapping to YAML configuration
4. **More Services**: Convert more singletons to injected services
5. **Scoped Injection**: Use Koin scopes for per-game or per-player instances