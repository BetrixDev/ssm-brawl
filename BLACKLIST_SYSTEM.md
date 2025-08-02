# Minigame Blacklist System

This document explains the passive and ability blacklist system for minigame definitions.

## Overview

The blacklist system allows minigame definitions to specify which passives and abilities should be disabled for all kits within that minigame. This provides fine-grained control over gameplay mechanics for different minigame types.

## Implementation

### MinigameDefinition Changes

The `MinigameDefinition` abstract class now includes two new properties:

```kotlin
/**
 * List of passive definitions that are blacklisted in this minigame.
 * These passives will not be enabled for any kit in this minigame.
 */
open val blacklistedPassives: List<PassiveDefinition> = emptyList()

/**
 * List of ability definitions that are blacklisted in this minigame.
 * These abilities will not be enabled for any kit in this minigame.
 */
open val blacklistedAbilities: List<AbilityDefinition> = emptyList()
```

### TestingMinigameDefinition Example

The `TestingMinigameDefinition` demonstrates how to use the blacklist system:

```kotlin
object TestingMinigameDefinition : MinigameDefinition() {
    override val name = "Testing"
    override val id = "testing"

    override val metadata = minigame {
        description = "A simple testing minigame"
        isHidden = true
        playersPerTeam = 1
        amountOfTeams = 1
        allowKitSwitching = true

        whitelistMap("campsite")
    }

    override val blacklistedPassives = listOf(
        RegenerationPassiveDefinition,
        HungerPassiveDefinition
    )

    override fun createInstance(teams: List<MinigameTeam>): MinigameInstance {
        return TestingMinigameInstance(this, teams)
    }
}
```

### KitInstance Filtering

The `KitInstance.setup()` method now filters out blacklisted passives and abilities:

```kotlin
open fun setup() {
    // Filter out blacklisted abilities
    val allowedAbilities = definition.metadata.abilities.filter { ability ->
        minigameDefinition?.blacklistedAbilities?.contains(ability) != true
    }

    allowedAbilities.forEach {
        val abilityInstance = it.createInstance(player)
        abilityInstances.add(abilityInstance)
        abilityInstance.setup()
    }

    // Filter out blacklisted passives
    val allowedPassives = definition.metadata.passives.filter { passive ->
        minigameDefinition?.blacklistedPassives?.contains(passive) != true
    }

    allowedPassives.forEach {
        val passiveInstance = it.createInstance(player)
        passiveInstances.add(passiveInstance)
        passiveInstance.setup()
    }

    // Setup hotbar items for abilities
    HotbarService.setupHotbarItems(this)

    player.sendDebugMessage("You have been given the ${definition.name} kit")
}
```

### KitService Integration

The `KitService` now supports passing minigame context when assigning kits:

```kotlin
fun assignKit(
    player: Player,
    minigameDefinition: MinigameDefinition,
): Result<KitInstance, AssignKitError>
```

This ensures that when kits are assigned within a minigame, the blacklist filtering is applied.

## Usage

### Adding Blacklisted Items to a Minigame

To blacklist passives or abilities in a minigame:

1. Override the `blacklistedPassives` and/or `blacklistedAbilities` properties in your minigame definition
2. Add the passive/ability definitions to the list
3. The filtering will automatically apply when kits are assigned within that minigame

### Example: Blacklisting Multiple Items

```kotlin
object MyCustomMinigameDefinition : MinigameDefinition() {
    // ... other properties ...

    override val blacklistedPassives = listOf(
        RegenerationPassiveDefinition,
        HungerPassiveDefinition,
        SomeOtherPassiveDefinition
    )

    override val blacklistedAbilities = listOf(
        ExplosionAbilityDefinition,
        SomeOtherAbilityDefinition
    )
}
```

## Benefits

1. **Gameplay Balance**: Disable overpowered or inappropriate mechanics for specific minigame types
2. **Customization**: Each minigame can have its own set of rules and restrictions
3. **Flexibility**: Easy to add or remove blacklisted items without changing kit definitions
4. **Maintainability**: Centralized control over what's allowed in each minigame

## Current Implementation

- **TestingMinigame**: Blacklists `RegenerationPassiveDefinition` and `HungerPassiveDefinition`
- **Other Minigames**: Can be configured with their own blacklists as needed
- **Hub/Outside Minigames**: No blacklist filtering applied (full kit functionality)