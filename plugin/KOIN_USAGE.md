# Koin Dependency Injection Usage Guide

This document explains how Koin dependency injection has been integrated into the Super Smash Mobs Brawl plugin.

## Overview

Koin is a lightweight dependency injection framework for Kotlin. It has been integrated to provide clean dependency management, particularly for injecting the `DataService` into `AbilityInstance` classes.

## Setup

### 1. Dependencies

The Koin dependency has been added to `build.gradle.kts`:

```kotlin
implementation("io.insert-koin:koin-core:3.5.6")
```

### 2. Koin Module Configuration

The Koin modules are defined in `/di/KoinModules.kt`:

```kotlin
val appModule = module {
    single { DataService }
    // Add other services as needed
}
```

### 3. Koin Initialization

Koin is initialized in the main plugin class `SuperSmashMobsBrawl.kt`:

```kotlin
override suspend fun onEnableAsync() {
    // Initialize Koin
    startKoin {
        modules(appModule)
    }
    // ... rest of initialization
}

override suspend fun onDisableAsync() {
    // ... cleanup
    stopKoin()
}
```

## Using Dependency Injection in AbilityInstance

### Base AbilityInstance Class

The `AbilityInstance` class now implements `KoinComponent` and injects the `DataService`:

```kotlin
abstract class AbilityInstance(
    val definition: AbilityDefinition, 
    val player: Player
) : KoinComponent {
    protected val plugin = SuperSmashMobsBrawl.instance
    protected val dataService: DataService by inject()
    // ... rest of the class
}
```

### Using DataService in Abilities

Any ability that extends `AbilityInstance` can now access the `dataService` property:

```kotlin
class MyAbilityInstance(definition: AbilityDefinition, player: Player) :
    AbilityInstance(definition, player) {
    
    override fun activate() {
        super.activate()
        
        // Access ability configuration
        val abilityData = dataService.getAbility(definition.id)
        
        // Access kit data
        val kitData = dataService.getKit("some_kit_id")
        
        // Access map data
        val mapData = dataService.getGameMap("some_map_id")
        
        // Your ability logic here...
    }
}
```

## Adding New Dependencies

To add new services to the dependency injection container:

1. Add the service to the Koin module in `/di/KoinModules.kt`:

```kotlin
val appModule = module {
    single { DataService }
    single { MyNewService }  // Add your service here
}
```

2. Inject it in any class that implements `KoinComponent`:

```kotlin
class MyClass : KoinComponent {
    private val myService: MyNewService by inject()
}
```

## Benefits

1. **Decoupling**: Abilities no longer need to directly access the `DataService` singleton
2. **Testability**: Services can be easily mocked for unit testing
3. **Flexibility**: Dependencies can be swapped or configured differently for different environments
4. **Clean Code**: Clear separation of concerns and dependency management

## Example Implementation

See `ExampleDataAbilityInstance.kt` for a complete example of how to use the injected `DataService` in an ability.