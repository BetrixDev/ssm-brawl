```
dev/betrix/superSmashMobsBrawl/minigames/
├── BrawlMinigame.kt                           # Main abstract minigame class
├── components/
│   ├── MinigameComponent.kt                   # Base component class
│   ├── MinigameComponentFactory.kt            # Factory for creating components
│   ├── scoreboard/
│   │   ├── MinigameScoreboardManager.kt       # Abstract scoreboard strategy
│   │   └── impl/
│   │       ├── FfaScoreboardManager.kt
│   │       ├── TeamStocksScoreboardManager.kt
│   │       ├── ParkourScoreboardManager.kt
│   │       └── DefaultScoreboardManager.kt
│   ├── teleportation/
│   │   ├── MinigameTeleportationManager.kt    # Abstract teleportation strategy
│   │   ├── SpawnSelectionStrategy.kt          # Enum for spawn algorithms
│   │   └── impl/
│   │       ├── FfaTeleportationManager.kt
│   │       ├── TeamBasedTeleportationManager.kt
│   │       ├── ParkourTeleportationManager.kt
│   │       └── DefaultTeleportationManager.kt
│   └── kit/
│       ├── MinigameKitManager.kt              # Abstract kit management strategy
│       └── impl/
│           ├── DefaultKitManager.kt
│           ├── SwitchableKitManager.kt
│           ├── OverrideKitManager.kt
│           └── FixedKitManager.kt
├── events/
│   ├── MinigamePlayerDeathEvent.kt
│   ├── MinigamePlayerRespawnEvent.kt
│   ├── MinigameStateChangeEvent.kt
│   └── MinigameStatsUpdateEvent.kt
└── impl/
    ├── FfaMinigame.kt                         # Concrete FFA implementation
    ├── TeamBasedStocksMinigame.kt             # Concrete team stocks implementation
    └── PrototypingMinigame.kt                 # Concrete prototyping implementation
```