import { z } from "zod";

//#region src/plugin/index.d.ts
declare namespace index_d_exports {
  export { Ability, GameMap, Kit, Minigame, Passive, abilitiesFileSchema, abilityRefSchema, abilitySchema, abilityTypeSchema, abilityUsageSchema, creatorsSchema, gameMapSchema, kitSchema, kitsFileSchema, mapsFileSchema, metadataSchema, minigameSchema, minigameTypeSchema, minigamesFileSchema, passiveRefOverrideSchema, passiveRefSchema, passiveSchema, passivesFileSchema, spawnPointSchema };
}
declare const metadataSchema: z.ZodRecord<z.core.$ZodRecordKey, z.core.SomeType>;
declare const abilityTypeSchema: z.ZodCatch<z.ZodEnum<{
  projectile: "projectile";
  aoe: "aoe";
}>>;
declare const abilityUsageSchema: z.ZodCatch<z.ZodEnum<{
  right_click: "right_click";
  left_click: "left_click";
}>>;
declare const abilitySchema: z.ZodObject<{
  id: z.ZodString;
  cooldown: z.ZodNumber;
  type: z.ZodCatch<z.ZodEnum<{
    projectile: "projectile";
    aoe: "aoe";
  }>>;
  itemSlot: z.ZodNumber;
  usage: z.ZodCatch<z.ZodEnum<{
    right_click: "right_click";
    left_click: "left_click";
  }>>;
  hotbarItem: z.ZodString;
  displayItem: z.ZodString;
}, z.core.$strip>;
declare const abilitiesFileSchema: z.ZodObject<{
  abilities: z.ZodArray<z.ZodObject<{
    id: z.ZodString;
    cooldown: z.ZodNumber;
    type: z.ZodCatch<z.ZodEnum<{
      projectile: "projectile";
      aoe: "aoe";
    }>>;
    itemSlot: z.ZodNumber;
    usage: z.ZodCatch<z.ZodEnum<{
      right_click: "right_click";
      left_click: "left_click";
    }>>;
    hotbarItem: z.ZodString;
    displayItem: z.ZodString;
  }, z.core.$strip>>;
}, z.core.$strip>;
declare const passiveSchema: z.ZodObject<{
  id: z.ZodString;
  userFacing: z.ZodBoolean;
  displayItem: z.ZodOptional<z.ZodString>;
  metadata: z.ZodOptional<z.ZodRecord<z.core.$ZodRecordKey, z.core.SomeType>>;
}, z.core.$strip>;
declare const passivesFileSchema: z.ZodObject<{
  passives: z.ZodArray<z.ZodObject<{
    id: z.ZodString;
    userFacing: z.ZodBoolean;
    displayItem: z.ZodOptional<z.ZodString>;
    metadata: z.ZodOptional<z.ZodRecord<z.core.$ZodRecordKey, z.core.SomeType>>;
  }, z.core.$strip>>;
}, z.core.$strip>;
declare const passiveRefOverrideSchema: z.ZodObject<{
  metadata: z.ZodOptional<z.ZodRecord<z.core.$ZodRecordKey, z.core.SomeType>>;
}, z.core.$strip>;
declare const passiveRefSchema: z.ZodObject<{
  id: z.ZodString;
  overrides: z.ZodOptional<z.ZodObject<{
    metadata: z.ZodOptional<z.ZodRecord<z.core.$ZodRecordKey, z.core.SomeType>>;
  }, z.core.$strip>>;
}, z.core.$strip>;
declare const abilityRefSchema: z.ZodObject<{
  id: z.ZodString;
}, z.core.$strip>;
declare const kitSchema: z.ZodObject<{
  id: z.ZodString;
  meleeDamage: z.ZodNumber;
  armor: z.ZodNumber;
  knockbackMultiplier: z.ZodNumber;
  metadata: z.ZodOptional<z.ZodRecord<z.core.$ZodRecordKey, z.core.SomeType>>;
  passives: z.ZodArray<z.ZodObject<{
    id: z.ZodString;
    overrides: z.ZodOptional<z.ZodObject<{
      metadata: z.ZodOptional<z.ZodRecord<z.core.$ZodRecordKey, z.core.SomeType>>;
    }, z.core.$strip>>;
  }, z.core.$strip>>;
  abilities: z.ZodArray<z.ZodObject<{
    id: z.ZodString;
  }, z.core.$strip>>;
}, z.core.$strip>;
declare const kitsFileSchema: z.ZodObject<{
  kits: z.ZodArray<z.ZodObject<{
    id: z.ZodString;
    meleeDamage: z.ZodNumber;
    armor: z.ZodNumber;
    knockbackMultiplier: z.ZodNumber;
    metadata: z.ZodOptional<z.ZodRecord<z.core.$ZodRecordKey, z.core.SomeType>>;
    passives: z.ZodArray<z.ZodObject<{
      id: z.ZodString;
      overrides: z.ZodOptional<z.ZodObject<{
        metadata: z.ZodOptional<z.ZodRecord<z.core.$ZodRecordKey, z.core.SomeType>>;
      }, z.core.$strip>>;
    }, z.core.$strip>>;
    abilities: z.ZodArray<z.ZodObject<{
      id: z.ZodString;
    }, z.core.$strip>>;
  }, z.core.$strip>>;
}, z.core.$strip>;
declare const spawnPointSchema: z.ZodObject<{
  x: z.ZodNumber;
  y: z.ZodNumber;
  z: z.ZodNumber;
  yaw: z.ZodOptional<z.ZodNumber>;
  pitch: z.ZodOptional<z.ZodNumber>;
}, z.core.$strip>;
declare const creatorsSchema: z.ZodUnion<readonly [z.ZodArray<z.ZodString>, z.ZodNull]>;
declare const gameMapSchema: z.ZodObject<{
  id: z.ZodString;
  voidLevel: z.ZodNumber;
  maxPlayers: z.ZodOptional<z.ZodNumber>;
  worldBorderSize: z.ZodNumber;
  creators: z.ZodUnion<readonly [z.ZodArray<z.ZodString>, z.ZodNull]>;
  spawnPoints: z.ZodArray<z.ZodObject<{
    x: z.ZodNumber;
    y: z.ZodNumber;
    z: z.ZodNumber;
    yaw: z.ZodOptional<z.ZodNumber>;
    pitch: z.ZodOptional<z.ZodNumber>;
  }, z.core.$strip>>;
}, z.core.$strip>;
declare const mapsFileSchema: z.ZodObject<{
  gameMaps: z.ZodArray<z.ZodObject<{
    id: z.ZodString;
    voidLevel: z.ZodNumber;
    maxPlayers: z.ZodOptional<z.ZodNumber>;
    worldBorderSize: z.ZodNumber;
    creators: z.ZodUnion<readonly [z.ZodArray<z.ZodString>, z.ZodNull]>;
    spawnPoints: z.ZodArray<z.ZodObject<{
      x: z.ZodNumber;
      y: z.ZodNumber;
      z: z.ZodNumber;
      yaw: z.ZodOptional<z.ZodNumber>;
      pitch: z.ZodOptional<z.ZodNumber>;
    }, z.core.$strip>>;
  }, z.core.$strip>>;
  hubMaps: z.ZodArray<z.ZodObject<{
    id: z.ZodString;
    voidLevel: z.ZodNumber;
    worldBorderSize: z.ZodNumber;
    creators: z.ZodUnion<readonly [z.ZodArray<z.ZodString>, z.ZodNull]>;
    spawnPoints: z.ZodArray<z.ZodObject<{
      x: z.ZodNumber;
      y: z.ZodNumber;
      z: z.ZodNumber;
      yaw: z.ZodOptional<z.ZodNumber>;
      pitch: z.ZodOptional<z.ZodNumber>;
    }, z.core.$strip>>;
  }, z.core.$strip>>;
}, z.core.$strip>;
declare const minigameTypeSchema: z.ZodCatch<z.ZodEnum<{
  ffa: "ffa";
  team_based_stocks: "team_based_stocks";
}>>;
declare const minigameSchema: z.ZodObject<{
  id: z.ZodString;
  countdown: z.ZodNumber;
  type: z.ZodCatch<z.ZodEnum<{
    ffa: "ffa";
    team_based_stocks: "team_based_stocks";
  }>>;
  isHidden: z.ZodBoolean;
  minPlayers: z.ZodOptional<z.ZodNumber>;
  maxPlayers: z.ZodOptional<z.ZodNumber>;
  allowKitSwitching: z.ZodOptional<z.ZodBoolean>;
  allowParties: z.ZodOptional<z.ZodBoolean>;
  playersPerTeam: z.ZodOptional<z.ZodNumber>;
  amountOfTeams: z.ZodOptional<z.ZodNumber>;
  stocks: z.ZodOptional<z.ZodNumber>;
}, z.core.$strip>;
declare const minigamesFileSchema: z.ZodObject<{
  minigames: z.ZodArray<z.ZodObject<{
    id: z.ZodString;
    countdown: z.ZodNumber;
    type: z.ZodCatch<z.ZodEnum<{
      ffa: "ffa";
      team_based_stocks: "team_based_stocks";
    }>>;
    isHidden: z.ZodBoolean;
    minPlayers: z.ZodOptional<z.ZodNumber>;
    maxPlayers: z.ZodOptional<z.ZodNumber>;
    allowKitSwitching: z.ZodOptional<z.ZodBoolean>;
    allowParties: z.ZodOptional<z.ZodBoolean>;
    playersPerTeam: z.ZodOptional<z.ZodNumber>;
    amountOfTeams: z.ZodOptional<z.ZodNumber>;
    stocks: z.ZodOptional<z.ZodNumber>;
  }, z.core.$strip>>;
}, z.core.$strip>;
type Ability = z.infer<typeof abilitySchema>;
type Passive = z.infer<typeof passiveSchema>;
type Kit = z.infer<typeof kitSchema>;
type GameMap = z.infer<typeof gameMapSchema>;
type Minigame = z.infer<typeof minigameSchema>;
//# sourceMappingURL=index.d.ts.map

//#endregion
export { Ability, GameMap, Kit, Minigame, Passive, abilitiesFileSchema, abilityRefSchema, abilitySchema, abilityTypeSchema, abilityUsageSchema, creatorsSchema, gameMapSchema, index_d_exports, kitSchema, kitsFileSchema, mapsFileSchema, metadataSchema, minigameSchema, minigameTypeSchema, minigamesFileSchema, passiveRefOverrideSchema, passiveRefSchema, passiveSchema, passivesFileSchema, spawnPointSchema };
//# sourceMappingURL=index-KYZMRiju.d.ts.map