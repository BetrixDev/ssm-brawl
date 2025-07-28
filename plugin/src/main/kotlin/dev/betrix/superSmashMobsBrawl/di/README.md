# Dependency Injection with Koin

This plugin uses the [Koin](https://insert-koin.io/) dependency injection framework to manage dependencies between classes and services. This makes the code more modular, testable, and easier to maintain.

## What is Dependency Injection?

Dependency injection is a design pattern where objects receive their dependencies from external sources rather than creating them internally. This provides several benefits:

- **Loose Coupling**: Classes don't need to know how to create their dependencies
- **Easier Testing**: Dependencies can be mocked or stubbed for unit tests
- **Better Separation of Concerns**: Each class focuses on its core responsibility
- **Centralized Configuration**: All dependencies are configured in one place

## Available Dependencies

The following dependencies are available for injection throughout the plugin:

### Core Dependencies
- `SuperSmashMobsBrawl` - The main plugin instance
- `JavaPlugin` - Plugin instance (same as above, but as the generic type)
- `Twilight` - The Twilight event/scheduler framework instance

### Services
- `DebugService` - Debug mode management
- `HubService` - Hub world and player management
- `WorldService` - World loading/unloading operations
- `HotbarService` - Hotbar item management
- `HubProtectionService` - Hub world protection
- `KitService` - Kit assignment and management
- `MinigameService` - Minigame instance management

## How to Use Dependency Injection

### Method 1: Implement Injectable Interface (Recommended)

The easiest way to use dependency injection is to implement the `Injectable` interface:

```kotlin
import dev.betrix.superSmashMobsBrawl.di.Injectable
import org.koin.core.component.inject

class MyService : Injectable {
    private val plugin: SuperSmashMobsBrawl by inject()
    private val twilight: Twilight by inject()
    private val hubService: HubService by inject()
    
    fun doSomething() {
        plugin.logger.info("Doing something...")
        // Use injected dependencies
    }
}
```

### Method 2: Direct Injection Functions

For one-off dependency retrieval without implementing an interface:

```kotlin
import dev.betrix.superSmashMobsBrawl.di.koinGet
import dev.betrix.superSmashMobsBrawl.di.koinInject

// Immediate retrieval
val plugin = koinGet<SuperSmashMobsBrawl>()

// Lazy retrieval (resolved when first accessed)
val hubService by koinInject<HubService>()
```

## Examples

### Service with Injected Dependencies

```kotlin
object MyCustomService : Manageable, Injectable {
    private val plugin: SuperSmashMobsBrawl by inject()
    private val worldService: WorldService by inject()
    private val twilight: Twilight by inject()
    
    override fun setup() {
        plugin.logger.info("Setting up MyCustomService")
        // Use worldService, twilight, etc.
    }
    
    override fun teardown() {
        plugin.logger.info("Tearing down MyCustomService")
    }
}
```

### Ability Instance with Injected Dependencies

```kotlin
class MyAbilityInstance(definition: AbilityDefinition, player: Player) :
    AbilityInstance(definition, player) {
    
    // AbilityInstance already implements Injectable and provides:
    // - protected val plugin: SuperSmashMobsBrawl by inject()
    // - protected val twilight: Twilight by inject()
    
    private val hubService: HubService by inject() // Additional dependency
    
    override fun activate() {
        plugin.logger.info("${player.name} activated ${definition.name}")
        
        // Use any injected dependencies
        if (hubService.isPlayerInHub(player)) {
            player.sendMessage("You can't use this ability in the hub!")
            return
        }
        
        // Ability logic here...
    }
}
```

### Command with Injected Dependencies

```kotlin
class MyCommand : Injectable {
    private val plugin: SuperSmashMobsBrawl by inject()
    private val kitService: KitService by inject()
    
    @Execute
    fun execute(player: Player) {
        if (kitService.hasKit(player)) {
            player.sendMessage("You already have a kit!")
        } else {
            player.sendMessage("You don't have a kit assigned.")
        }
    }
}
```

## Adding New Dependencies

To add new dependencies that can be injected:

1. Add the dependency to the Koin module in `AppModule.kt`:

```kotlin
val appModule = module {
    // ... existing dependencies ...
    
    single { MyNewService }  // For object singletons
    factory { MyNewClass() } // For new instances each time
}
```

2. If it's a manageable service, register it with the ServiceRegistry in `SuperSmashMobsBrawl.kt`:

```kotlin
ServiceRegistry.register(MyNewService)
```

## Best Practices

1. **Use the Injectable interface** for classes that need multiple dependencies
2. **Lazy injection** (`by inject()`) is preferred over immediate injection (`get()`)
3. **Keep dependencies focused** - only inject what you actually need
4. **Services should be singletons** - use `single { }` in the Koin module
5. **Implement Manageable** for services that need lifecycle management
6. **Document dependencies** - make it clear what each class depends on

## Troubleshooting

### Common Issues

1. **"No definition found"** error:
   - Make sure the dependency is defined in `AppModule.kt`
   - Check that Koin is properly initialized before trying to inject

2. **Circular dependencies**:
   - Avoid having services that depend on each other directly
   - Consider using events or interfaces to break the dependency cycle

3. **Late initialization**:
   - Some dependencies might not be available immediately during plugin startup
   - Use lazy injection (`by inject()`) rather than immediate injection

### Testing

When writing unit tests, you can override dependencies by starting Koin with test modules:

```kotlin
@Test
fun testMyService() {
    startKoin {
        modules(module {
            single<SuperSmashMobsBrawl> { mockk<SuperSmashMobsBrawl>() }
            // ... other mocked dependencies
        })
    }
    
    // Test code here
    
    stopKoin()
}
```

This dependency injection system makes the codebase much more maintainable and testable while keeping the code clean and focused on business logic rather than dependency management.