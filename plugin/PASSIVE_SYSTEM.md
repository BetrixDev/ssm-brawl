# Super Smash Mobs Brawl - Passive System Documentation

## Overview

The passive system in Super Smash Mobs Brawl provides core gameplay mechanics that automatically apply to all players based on their selected kit. This document covers the implementation of the essential **Regeneration** and **Hunger** passives.

## Core Passives

### 1. Regeneration Passive

The Regeneration passive provides automatic health regeneration to players after a damage delay, similar to the original Mineplex Super Smash Mobs.

#### Features:
- **Configurable regeneration rate**: Different kits can have different healing rates (HP per second)
- **Configurable max health**: Each kit can have different maximum health values
- **Damage delay**: Regeneration stops when taking damage and resumes after a delay
- **Attribute system compatibility**: Uses modern Bukkit attribute system for max health management

#### Configuration:
```kotlin
regenerationRate = 0.5 // 0.5 HP per second
maxHealth = 18.0 // 9 hearts (18 HP)
regenerationDelay = 100 // 5 seconds (100 ticks)
```

### 2. Hunger Passive

The Hunger passive ensures players never lose hunger and always maintain full food level and saturation.

#### Features:
- **Prevents hunger loss**: Food level stays at 20
- **Maintains saturation**: Saturation stays at maximum
- **Cancels food events**: Prevents any food level changes
- **Periodic maintenance**: Ensures food levels stay consistent

## Kit Configuration

### Adding Core Passives to Kits

All kits should include the core passives using the helper function:

```kotlin
override val metadata = kit {
    description = "Your kit description"
    meleeDamage = KitValues.YOUR_KIT.MELEE_DAMAGE
    
    // Configure regeneration values
    regenerationRate = KitValues.YOUR_KIT.REGENERATION_RATE
    maxHealth = KitValues.YOUR_KIT.MAX_HEALTH  
    regenerationDelay = KitValues.YOUR_KIT.REGENERATION_DELAY
    
    // Add core passives (Hunger + Regeneration)
    corePassives()
    
    // Add kit-specific passives and abilities
    passive(YourKitSpecificPassive)
    ability(YourKitAbility)
}
```

### Kit Values Reference

The `KitValues` object contains standardized configurations for different kit archetypes:

#### High Health Kits (Tanks)
- **Iron Golem**: 30 HP, 0.5 HP/s regeneration, 6s delay
- **Magma Cube**: 26 HP, 0.4 HP/s regeneration, 5s delay

#### Medium Health Kits (Balanced)
- **Creeper**: 18 HP, 0.5 HP/s regeneration, 5s delay
- **Skeleton**: 18 HP, 0.5 HP/s regeneration, 5s delay  
- **Zombie**: 20 HP, 0.5 HP/s regeneration, 5s delay

#### Low Health Kits (Glass Cannons)
- **Blaze**: 16 HP, 0.6 HP/s regeneration, 4s delay
- **Spider**: 14 HP, 0.6 HP/s regeneration, 4s delay

## Implementation Details

### Regeneration Mechanics

1. **Health Setting**: Uses `Attribute.MAX_HEALTH` to set player's maximum health
2. **Damage Tracking**: Monitors `EntityDamageEvent` to reset regeneration timer
3. **Regeneration Timer**: Uses `TwilightRunnable` to heal at regular intervals
4. **Rate Calculation**: Healing rate is divided by 5 to account for 1-second intervals

### Hunger Mechanics

1. **Event Cancellation**: Cancels `FoodLevelChangeEvent` to prevent hunger loss
2. **Periodic Maintenance**: Runs every 2 seconds to ensure food levels stay at 20
3. **Saturation Management**: Maintains both food level and saturation at maximum

### Error Handling

- **Graceful Cleanup**: All passives properly clean up resources on player quit/death
- **Safety Checks**: Null checks and online status verification prevent errors
- **Resource Management**: Tasks are cancelled and references cleared in teardown

## Creating New Kits

### Step 1: Define Kit Values
Add your kit's values to `KitValues.kt`:

```kotlin
object YOUR_KIT {
    const val REGENERATION_RATE = 0.5
    const val MAX_HEALTH = 20.0
    const val REGENERATION_DELAY = 100
    const val MELEE_DAMAGE = 6
}
```

### Step 2: Create Kit Definition
```kotlin
object YourKitDefinition : KitDefinition() {
    override val name = "Your Kit"
    override val id = "your_kit"

    override val metadata = kit {
        description = "Your kit description"
        meleeDamage = KitValues.YOUR_KIT.MELEE_DAMAGE
        
        regenerationRate = KitValues.YOUR_KIT.REGENERATION_RATE
        maxHealth = KitValues.YOUR_KIT.MAX_HEALTH
        regenerationDelay = KitValues.YOUR_KIT.REGENERATION_DELAY
        
        corePassives()
        
        // Add kit-specific content
    }

    override fun createInstance(player: Player): YourKitInstance {
        return YourKitInstance(this, player)
    }
}
```

### Step 3: Register Kit
Add your kit to the `KitRegistry`:

```kotlin
init {
    register(YourKitDefinition)
}
```

## Testing

### Regeneration Testing
1. Take damage and verify health doesn't regenerate immediately
2. Wait for the delay period and verify regeneration starts
3. Take damage during regeneration and verify it stops
4. Check that regeneration stops at the kit's max health

### Hunger Testing  
1. Verify food level stays at 20 during gameplay
2. Test that hunger-affecting events are cancelled
3. Confirm saturation remains at maximum

## Performance Considerations

- **Efficient Timers**: Uses 1-second intervals for regeneration to balance responsiveness with performance
- **Event Filtering**: Only processes events for the specific player instance
- **Resource Cleanup**: Proper cleanup prevents memory leaks
- **Attribute System**: Uses modern Bukkit attributes for better compatibility

## Common Issues

### Issue: Player health doesn't update visually
**Solution**: Ensure you're setting both the attribute base value and current health

### Issue: Regeneration doesn't stop on damage  
**Solution**: Verify the damage event listener is properly registered and filtering for the correct player

### Issue: Food level changes despite passive
**Solution**: Check that the event is being cancelled and the maintenance task is running

## Future Enhancements

- **Kit-specific hunger rates**: Different kits could have different food consumption rates
- **Regeneration effects**: Visual/audio effects during regeneration
- **Advanced regeneration**: Different rates based on combat status
- **Conditional regeneration**: Regeneration only in certain areas or situations