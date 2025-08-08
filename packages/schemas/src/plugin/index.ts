import { z } from "zod";

// Common helpers
const numberLike = z.number();
const booleanLike = z.boolean();
const stringLike = z.string();

// Generic metadata: allow simple scalar values
export const metadataSchema = z.record(z.union([z.number(), z.string(), z.boolean()]));

// abilities.yml
export const abilityTypeSchema = z.enum(["projectile", "aoe"]).catch("projectile");
export const abilityUsageSchema = z.enum(["right_click", "left_click"]).catch("right_click");

export const abilitySchema = z.object({
  id: stringLike,
  cooldown: numberLike,
  type: abilityTypeSchema,
  itemSlot: z.number().int(),
  usage: abilityUsageSchema,
  hotbarItem: stringLike,
  displayItem: stringLike,
});

export const abilitiesFileSchema = z.object({
  abilities: z.array(abilitySchema),
});

// passives.yml
export const passiveSchema = z.object({
  id: stringLike,
  userFacing: booleanLike,
  displayItem: stringLike.optional(),
  metadata: metadataSchema.optional(),
});

export const passivesFileSchema = z.object({
  passives: z.array(passiveSchema),
});

// kits.yml
export const passiveRefOverrideSchema = z.object({
  metadata: metadataSchema.optional(),
});

export const passiveRefSchema = z.object({
  id: stringLike,
  overrides: passiveRefOverrideSchema.optional(),
});

export const abilityRefSchema = z.object({
  id: stringLike,
});

export const kitSchema = z.object({
  id: stringLike,
  meleeDamage: numberLike,
  armor: numberLike,
  knockbackMultiplier: numberLike,
  metadata: metadataSchema.optional(),
  passives: z.array(passiveRefSchema),
  abilities: z.array(abilityRefSchema),
});

export const kitsFileSchema = z.object({
  kits: z.array(kitSchema),
});

// maps.yml
export const spawnPointSchema = z.object({
  x: numberLike,
  y: numberLike,
  z: numberLike,
  yaw: numberLike.optional(),
  pitch: numberLike.optional(),
});

export const creatorsSchema = z.union([z.array(z.string().uuid()), z.null()]);

export const gameMapSchema = z.object({
  id: stringLike,
  voidLevel: numberLike,
  maxPlayers: z.number().int().optional(),
  worldBorderSize: numberLike,
  creators: creatorsSchema,
  spawnPoints: z.array(spawnPointSchema),
});

export const mapsFileSchema = z.object({
  gameMaps: z.array(gameMapSchema),
  hubMaps: z.array(
    z.object({
      id: stringLike,
      voidLevel: numberLike,
      worldBorderSize: numberLike,
      creators: creatorsSchema,
      spawnPoints: z.array(spawnPointSchema),
    })
  ),
});

// minigames.yml
export const minigameTypeSchema = z.enum(["ffa", "team_based_stocks"]).catch("ffa");

export const minigameSchema = z.object({
  id: stringLike,
  countdown: z.number().int(),
  type: minigameTypeSchema,
  isHidden: booleanLike,
  minPlayers: z.number().int().optional(),
  maxPlayers: z.number().int().optional(),
  allowKitSwitching: booleanLike.optional(),
  allowParties: booleanLike.optional(),
  playersPerTeam: z.number().int().optional(),
  amountOfTeams: z.number().int().optional(),
  stocks: z.number().int().optional(),
});

export const minigamesFileSchema = z.object({
  minigames: z.array(minigameSchema),
});

export type Ability = z.infer<typeof abilitySchema>;
export type Passive = z.infer<typeof passiveSchema>;
export type Kit = z.infer<typeof kitSchema>;
export type GameMap = z.infer<typeof gameMapSchema>;
export type Minigame = z.infer<typeof minigameSchema>;