# SmashDamageEvent System

The SmashDamageEvent system provides a lightweight and extensible way to handle damage events in Super Smash Mobs Brawl mini-games. It allows mini-game instances to customize damage handling according to their specific rules and mechanics.

## Overview

The system consists of several components:

1. **SmashDamageEvent** - A custom TwilightEvent that contains all damage information
2. **SmashDamageService** - A service for firing damage events with convenience methods
3. **SmashDamageHandler** - A handler that converts Bukkit damage events into SmashDamageEvents
4. **OnSmashDamage** - An interface that mini-game instances can implement to handle damage

## Features

- **Lightweight**: Minimal overhead with efficient event handling
- **Extensible**: Easy to add extra data and custom damage sources
- **Type-safe**: Kotlin's type system ensures safe access to extra data
- **Cancellable**: Events can be cancelled to prevent damage
- **Modifiable**: Damage values can be modified before application
- **Post-processing**: Support for post-damage effects and logic

## Usage

### Basic Implementation

To use the SmashDamageEvent system in your mini-game:

1. Implement the `OnSmashDamage` interface
2. Register an event listener for `SmashDamageEvent`
3. Handle damage according to your mini-game's rules

```kotlin
class MyMinigameInstance : MinigameInstance(definition, teams), OnSmashDamage {
    
    init {
        event<SmashDamageEvent> {
            if (!isPlayerInMinigame(victim)) return@event
            
            val shouldProcess = onSmashDamage(this@event)
            if (!shouldProcess) {
                isCancelled = true
                return@event
            }
            
            val modifiedDamage = onPreSmashDamage(this@event)
            // Damage is applied automatically by the handler
            
            onPostSmashDamage(this@event, modifiedDamage)
        }
    }
    
    override fun onSmashDamage(event: SmashDamageEvent): Boolean {
        // Your damage handling logic here
        return true
    }
}
```

### Damage Sources

The system supports various damage sources:

- `MELEE_ATTACK` - Player melee attacks
- `ABILITY` - Ability-based damage
- `PASSIVE` - Passive ability damage
- `PROJECTILE` - Projectile damage
- `EXPLOSION` - Explosion damage
- `FALL` - Fall damage
- `ENVIRONMENT` - Environmental damage (fire, lava, etc.)
- `OTHER` - Other damage sources

### Extra Data

Events can carry extra data for additional context:

```kotlin
// Firing an event with extra data
val event = SmashDamageService.fireAbilityDamageEvent(
    victim = player,
    damager = damager,
    damage = 10.0,
    abilityName = "Fireball",
    extraData = mapOf(
        "combo_count" to 3,
        "critical_hit" to true,
        "element" to "fire"
    )
)

// Accessing extra data
if (event.hasExtraData("combo_count")) {
    val comboCount = event.getExtraData<Int>("combo_count")
    val element = event.getExtraData<String>("element", "none")
}
```

### Manual Event Firing

You can manually fire damage events for custom damage sources:

```kotlin
// Basic damage event
SmashDamageService.fireDamageEvent(
    victim = player,
    damager = damager,
    damage = 5.0,
    damageSource = SmashDamageEvent.DamageSource.ABILITY
)

// Ability damage event
SmashDamageService.fireAbilityDamageEvent(
    victim = player,
    damager = damager,
    damage = 8.0,
    abilityName = "Lightning Strike"
)

// Explosion damage event
SmashDamageService.fireExplosionDamageEvent(
    victim = player,
    damager = damager,
    damage = 12.0,
    explosionRadius = 5.0
)
```

## Advanced Usage

### Custom Damage Modifications

```kotlin
override fun onPreSmashDamage(event: SmashDamageEvent): Double {
    // Reduce explosion damage by 50%
    if (event.damageSource == SmashDamageEvent.DamageSource.EXPLOSION) {
        return event.damage * 0.5
    }
    
    // Increase melee damage by 20% for specific conditions
    if (event.damageSource == SmashDamageEvent.DamageSource.MELEE_ATTACK) {
        return event.damage * 1.2
    }
    
    return event.damage
}
```

### Post-Damage Effects

```kotlin
override fun onPostSmashDamage(event: SmashDamageEvent, finalDamage: Double) {
    // Apply special effects
    if (finalDamage > 10.0) {
        event.victim.sendMessage("§4§lCritical hit!")
    }
    
    // Track statistics
    if (event.damager is Player) {
        event.damager.sendMessage("§aYou dealt $finalDamage damage!")
    }
    
    // Apply knockback effects
    if (event.knockback != null) {
        // Custom knockback logic
    }
}
```

### Event Cancellation

```kotlin
override fun onSmashDamage(event: SmashDamageEvent): Boolean {
    // Cancel fall damage completely
    if (event.damageSource == SmashDamageEvent.DamageSource.FALL) {
        event.victim.sendMessage("§aFall damage is disabled!")
        return false
    }
    
    // Cancel damage from specific sources
    if (event.damageSource == SmashDamageEvent.DamageSource.ENVIRONMENT) {
        return false
    }
    
    return true
}
```

## Integration with Existing Systems

The SmashDamageEvent system integrates seamlessly with existing Bukkit damage events. The `SmashDamageHandler` automatically converts Bukkit damage events into SmashDamageEvents for players in mini-games, ensuring that all damage goes through your custom handling system.

## Performance Considerations

- Events are only fired for players in mini-games
- The system uses efficient event handling with minimal overhead
- Extra data is stored in a lightweight Map structure
- Type-safe access to extra data prevents runtime errors

## Safety Features

- All events are cancellable to prevent unwanted damage
- Type-safe access to extra data prevents casting errors
- Proper null handling for optional damagers
- Timestamp tracking for debugging and logging
- Comprehensive error handling in the service layer