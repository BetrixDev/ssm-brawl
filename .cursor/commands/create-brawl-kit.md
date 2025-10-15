# Super Smash Mobs Brawl Feature Development Guide

## Development Workflow

### Phase 1: Analysis & Planning

1. **Understand the Request**
   - Clarify feature scope, goals, and acceptance criteria
   - Identify which components are needed (Kit, Abilities, Passives, Disguises, Projectiles)
   - Review similar existing features to understand patterns

2. **Working with Legacy Code Snippets**
   - If you are given code snippets, study them carefully and understand how they work
   - If the snippets are from a different version of the codebase, ensure the feature you are creating has the **exact same functionality**, but adapted to fit the new modern codebase
   - Identify core mechanics and behaviors that must be preserved
   - Recognize outdated patterns that need modernization (old APIs, deprecated methods, etc.)
   - Map old code structures to their modern equivalents in the current codebase

3. **Define Technical Approach**
   - Determine required data models and class structures
   - Identify necessary YAML configurations
   - Plan metadata fields for runtime customization
   - Consider edge cases

4. **Verify Requirements**
   - List all directories and files that will be created/modified
   - Confirm understanding with the user before implementation

### Phase 2: Implementation

Follow these steps in order:

1. **Create Disguise** (if needed)
   - Location: `plugin/src/main/kotlin/dev/betrix/superSmashMobsBrawl/disguises`
   - Add custom properties for mob-specific behaviors (e.g., Enderman holding blocks, Creeper primed state)

2. **Create Projectiles** (if needed)
   - Location: `plugin/src/main/kotlin/dev/betrix/superSmashMobsBrawl/projectiles`
   - First check if `BrawlProjectile` helper constructors can be used
   - Only extend `BrawlProjectile` if custom behavior is needed

3. **Create Abilities**
   - Location: `plugin/src/main/kotlin/dev/betrix/superSmashMobsBrawl/abilities`
   - Extend `BrawlAbility` class
   - Handle cooldowns, usage, and metadata
   - Add to factory in `BrawlKit` class

4. **Create Passives**
   - Location: `plugin/src/main/kotlin/dev/betrix/superSmashMobsBrawl/passives`
   - Extend `BrawlPassive` class
   - Configure triggers (events, cooldowns, or timers)
   - Add to factory in `BrawlKit` class

5. **Create/Configure Kit**
   - Location: `plugin/src/main/kotlin/dev/betrix/superSmashMobsBrawl/kits`
   - Use `BrawlKit` class for standard kits
   - Only create custom kit class if truly necessary (extend `BrawlKit` and add to `KitService.assignKit` factory)

6. **Create YAML Definitions**
   - Location: `plugin/src/main/resources/data/*.yml`
   - Define all configurable properties
   - Include metadata values

## Technical Components Reference

### Disguises

- Transform players into mobs with custom properties
- Methods for manipulation (e.g., `setHeldBlock()`, `setPrimed()`)

### Abilities

- Triggered by right/left click on hotbar tools
- Managed by `BrawlAbility` base class
- Handle cooldowns, usage conditions, and effects

### Passives

- Auto-applied enhancements/debuffs when kit is assigned
- Managed by `BrawlPassive` base class
- Can be event-based, cooldown-based, or timer-based

### Projectiles

- Common for ranged abilities
- Use `BrawlProjectile` helper constructors when possible
- Custom projectiles extend `BrawlProjectile`

### Metadata System

**Critical for flexibility**: Any value that could be useful for custom gamemodes should be metadata-driven.

**When adding metadata:**

- Add field to corresponding `*Def` data class
- Include in YAML definition
- Allow runtime customization without code changes

## Key Directories

```
plugin/src/main/kotlin/dev/betrix/superSmashMobsBrawl/
├── kits/
├── abilities/
├── passives/
├── projectiles/
├── disguises/
└── models/brawlData/

plugin/src/main/resources/data/*.yml
```

## Implementation Standards

### Code Consistency

- **Always** study existing similar features first
- Match naming conventions, code structure, and patterns
- Follow Kotlin idioms and best practices
- Use Prettier formatting (80 character line width)

### Legacy Code Migration

When adapting features from older code:

- **Preserve functionality**: The new implementation must behave identically to the original
- **Modernize implementation**: Use current APIs, patterns, and best practices
- **Maintain intent**: Understand _why_ the original code worked a certain way
- **Document changes**: Note any significant adaptations made from the legacy version

### Quality Checklist

- [ ] Studied similar existing implementations
- [ ] Follows existing code patterns and structure
- [ ] Extends appropriate base classes
- [ ] Added to necessary factories
- [ ] Metadata fields identified and implemented
- [ ] YAML definitions created
- [ ] Custom properties for disguises (if applicable)
- [ ] Cooldown and usage handling implemented
- [ ] Edge cases considered

## Remember

- Consistency with existing code is paramount
- When given legacy code, preserve functionality while modernizing implementation
- Metadata enables customization without code changes
- Always extend base classes rather than reimplementing functionality
- Ask clarifying questions before implementation if requirements are unclear
