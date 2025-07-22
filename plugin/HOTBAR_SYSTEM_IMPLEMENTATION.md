# Hotbar System Implementation

## Overview
I have implemented a comprehensive hotbar tool system that automatically provides players with ability items in their hotbar and handles activation through right-click interactions. This system is fully reusable and makes adding new kits with new abilities painless.

## System Architecture

### 1. HotbarService (Core Component)
**File**: `HotbarService.kt`

The central service that manages all hotbar functionality:

#### Key Features:
- **Automatic Item Management**: Assigns and removes hotbar items when kits are equipped/unequipped
- **Persistent Data Tracking**: Uses Bukkit's PersistentDataContainer to track which item corresponds to which ability
- **Real-time Updates**: Automatically updates item lore to show current cooldown status
- **Event Handling**: Listens for right-click interactions and routes them to the correct abilities
- **Performance Optimized**: Periodic updates (1-second intervals) instead of constant polling

#### Main Methods:
- `setupHotbarItems(kitInstance)` - Sets up hotbar items when a kit is assigned
- `clearHotbarItems(player)` - Removes hotbar items when a kit is unassigned  
- `updateHotbarItems(kitInstance)` - Updates items with current cooldown states
- `onPlayerInteract(event)` - Handles right-click activation of abilities

### 2. Enhanced KitService
**File**: `KitService.kt`

Updated to integrate with the hotbar system:

#### New Features:
- `getKitInstance(player)` - Retrieves a player's current kit instance
- `hasKit(player)` - Checks if a player has a kit assigned
- Automatic kit setup/teardown when assigning/unassigning kits
- Proper integration with HotbarService lifecycle

### 3. Updated KitInstance
**File**: `KitInstance.kt`

Modified to automatically integrate with the hotbar system:

#### Changes:
- Calls `HotbarService.setupHotbarItems(this)` during setup
- Calls `HotbarService.clearHotbarItems(player)` during teardown
- No additional code needed in individual kit implementations

### 4. Streamlined Ability Instances
**Files**: `*AbilityInstance.kt`

Removed individual PlayerInteractEvent handlers since activation is now centralized:

#### Benefits:
- **Cleaner Code**: No duplicate interaction handling code
- **Consistency**: All abilities activate the same way
- **Maintainability**: Changes to activation logic only need to be made in one place

## How It Works

### 1. Kit Assignment Process
```kotlin
KitService.assignKit(player, CreeperKitDefinition)
    └── Creates KitInstance
    └── Calls kitInstance.setup()
        └── Sets up abilities and passives  
        └── Calls HotbarService.setupHotbarItems(this)
            └── Clears hotbar
            └── Adds ability items to specified slots
            └── Sets persistent data on items for identification
            └── Updates item lore with descriptions and cooldown info
```

### 2. Ability Activation Process
```kotlin
Player right-clicks with hotbar item
    └── HotbarService.onPlayerInteract() event handler
        └── Checks if player has a kit
        └── Extracts ability ID from item's persistent data
        └── Finds corresponding AbilityInstance
        └── Calls abilityInstance.activate()
        └── Updates hotbar items to reflect new cooldown state
```

### 3. Real-time Updates
```kotlin
Every 1 second (20 ticks):
    └── HotbarService.updateAllPlayerHotbars()
        └── For each player with a kit:
            └── Updates item lore with current cooldown status
            └── Shows "Ready!" or "Cooldown: Xs" 
```

## Key Features

### ✅ **Automatic Item Management**
- Players automatically receive their ability items when assigned a kit
- Items are placed in the correct hotbar slots as defined by ability metadata
- Items are automatically removed when kits are unassigned

### ✅ **Smart Cooldown Display**
- Item lore dynamically updates to show cooldown status
- Shows "Ready to use!" when available
- Shows "Cooldown: Xs" when on cooldown
- Updates every second for real-time feedback

### ✅ **Persistent Data Tracking**
- Each item has a hidden identifier linking it to its ability
- Prevents activation of wrong abilities
- Survives inventory modifications

### ✅ **Event Cancellation**
- Right-clicks with ability items are cancelled to prevent normal item behavior
- Ensures abilities activate instead of item interactions

### ✅ **Flexible Item Types**
- Each ability can define its own hotbar item type and appearance
- Supports custom names, lore, and materials
- Automatically inherits from ability definitions

## Usage Examples

### For Players
```
/kit creeper          # Get the Creeper kit with all abilities
/kit clear           # Remove current kit and clear hotbar

# Then simply right-click hotbar items to use abilities!
```

### For Developers (Adding New Abilities)
```kotlin
// 1. Define the ability with hotbar metadata
override val metadata = ability {
    description = "Your ability description"
    type = AbilityType.PROJECTILE
    cooldown = 8
    hotbarItemSlot = 2  // Which slot (0-8)
    
    val item = ItemStack.of(Material.DIAMOND_SWORD)
    item.name("My Ability")
    hotbarItem = item
}

// 2. No additional code needed!
// The HotbarService automatically handles everything else
```

## Reusability

### ✅ **Zero Boilerplate for New Kits**
- Simply define abilities with hotbar metadata
- Add them to kit definitions
- HotbarService handles the rest automatically

### ✅ **Consistent Across All Kits**
- Same activation method (right-click) for all abilities
- Same visual feedback system for all players
- Same cooldown display format

### ✅ **Easy to Extend**
- Adding new ability types requires no changes to hotbar system
- New kits automatically inherit all hotbar functionality
- System scales with any number of abilities per kit

## Testing

### Available Commands
- `/kit creeper` - Assigns the complete Creeper kit for testing
- `/kit clear` - Removes the current kit

### Test the System
1. Join the server
2. Run `/kit creeper`
3. Check your hotbar for ability items
4. Right-click items to activate abilities
5. Watch cooldowns update in real-time
6. Test all three Creeper abilities:
   - **Slot 0**: Sulphur Bomb (Iron Axe)
   - **Slot 1**: Lightning Jump (Iron Sword) 
   - **Slot 2**: Explosion (TNT)
   - **Slot 3**: Stealth (Glass)

## Technical Benefits

### 🚀 **Performance Optimized**
- Uses efficient persistent data containers
- Periodic updates instead of constant polling
- Event filtering to only handle relevant interactions

### 🔧 **Maintainable Architecture**
- Centralized hotbar logic in one service
- Clean separation of concerns
- Easy to debug and modify

### 🔒 **Robust Error Handling**
- Graceful handling of missing kits or abilities
- Automatic cleanup when players disconnect
- Safe handling of inventory modifications

The hotbar system is now fully functional and ready for use with any number of kits and abilities!