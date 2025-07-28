# Super Smash Mobs Brawl Plugin

A Minecraft Paper plugin for Super Smash Mobs Brawl minigame.

## Development Setup

### Prerequisites

- **Java 21** or higher
- **Gradle 8.11** (included via wrapper)
- **IntelliJ IDEA** (recommended) or any Kotlin-compatible IDE

### Building the Plugin

```bash
# Build the plugin
./gradlew build

# Build shadow JAR (fat JAR with dependencies)
./gradlew shadowJar

# Run code formatting
./gradlew formatCode

# Run code quality checks
./gradlew codeQuality

# Run the development server
./gradlew runServer
```

## Code Quality

This project uses several tools to maintain code quality:

### Code Formatting

- **ktfmt**: Kotlin code formatter with Google Kotlin style
- **EditorConfig**: Consistent formatting across different editors

```bash
# Format all Kotlin code
./gradlew ktfmtFormat

# Check formatting without applying changes
./gradlew ktfmtCheck
```

### Code Linting

- **ktlint**: Kotlin linter for style and convention checking

```bash
# Lint all Kotlin code
./gradlew ktlintCheck

# Auto-fix linting issues where possible
./gradlew ktlintFormat
```

### Combined Tasks

```bash
# Run all code quality checks
./gradlew codeQuality

# Apply all formatting tools
./gradlew formatCode
```

## Project Structure

```
src/main/kotlin/dev/betrix/superSmashMobsBrawl/
├── commands/          # Command implementations
├── maps/              # Map definitions and builders
├── models/            # Data models
├── passives/          # Passive ability system
├── registries/        # Component registries
└── SuperSmashMobsBrawl.kt  # Main plugin class
```

## Dependencies

- **Paper API**: Minecraft server API
- **Kotlin Standard Library**: Core Kotlin functionality
- **Twilight**: GUI framework by Flyte
- **LiteCommands**: Command framework
- **MCCoroutine**: Coroutine support for Bukkit
- **Kotlin Result**: Railway-oriented programming

## Development Guidelines

1. **Code Style**: Follow the Google Kotlin Style Guide
2. **Formatting**: Use `./gradlew formatCode` before committing
3. **Linting**: Ensure `./gradlew codeQuality` passes
4. **Testing**: Run `./gradlew test` before submitting PRs
5. **Documentation**: Document public APIs with KDoc

## IDE Setup

### IntelliJ IDEA

1. Import the project as a Gradle project
2. Set Project SDK to Java 21
3. Install the Kotlin plugin (usually pre-installed)
4. Configure code style:
   - Go to Settings → Editor → Code Style → Kotlin
   - Set to "Use ktfmt (Google Style)"

### VS Code

1. Install the Kotlin extension
2. Install the EditorConfig extension
3. The project will automatically use the formatting configuration



## License

[Add license information here]