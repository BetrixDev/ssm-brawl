import { __export } from "./chunk-Cl8Af3a2.js";
import { z } from "zod";

//#region src/plugin/index.ts
var plugin_exports = {};
__export(plugin_exports, {
	abilitiesFileSchema: () => abilitiesFileSchema,
	abilityRefSchema: () => abilityRefSchema,
	abilitySchema: () => abilitySchema,
	abilityTypeSchema: () => abilityTypeSchema,
	abilityUsageSchema: () => abilityUsageSchema,
	creatorsSchema: () => creatorsSchema,
	gameMapSchema: () => gameMapSchema,
	kitSchema: () => kitSchema,
	kitsFileSchema: () => kitsFileSchema,
	mapsFileSchema: () => mapsFileSchema,
	metadataSchema: () => metadataSchema,
	minigameSchema: () => minigameSchema,
	minigameTypeSchema: () => minigameTypeSchema,
	minigamesFileSchema: () => minigamesFileSchema,
	passiveRefOverrideSchema: () => passiveRefOverrideSchema,
	passiveRefSchema: () => passiveRefSchema,
	passiveSchema: () => passiveSchema,
	passivesFileSchema: () => passivesFileSchema,
	spawnPointSchema: () => spawnPointSchema
});
const numberLike = z.number();
const booleanLike = z.boolean();
const stringLike = z.string();
const metadataSchema = z.record(z.union([
	z.number(),
	z.string(),
	z.boolean()
]));
const abilityTypeSchema = z.enum(["projectile", "aoe"]).catch("projectile");
const abilityUsageSchema = z.enum(["right_click", "left_click"]).catch("right_click");
const abilitySchema = z.object({
	id: stringLike,
	cooldown: numberLike,
	type: abilityTypeSchema,
	itemSlot: z.number().int(),
	usage: abilityUsageSchema,
	hotbarItem: stringLike,
	displayItem: stringLike
});
const abilitiesFileSchema = z.object({ abilities: z.array(abilitySchema) });
const passiveSchema = z.object({
	id: stringLike,
	userFacing: booleanLike,
	displayItem: stringLike.optional(),
	metadata: metadataSchema.optional()
});
const passivesFileSchema = z.object({ passives: z.array(passiveSchema) });
const passiveRefOverrideSchema = z.object({ metadata: metadataSchema.optional() });
const passiveRefSchema = z.object({
	id: stringLike,
	overrides: passiveRefOverrideSchema.optional()
});
const abilityRefSchema = z.object({ id: stringLike });
const kitSchema = z.object({
	id: stringLike,
	meleeDamage: numberLike,
	armor: numberLike,
	knockbackMultiplier: numberLike,
	metadata: metadataSchema.optional(),
	passives: z.array(passiveRefSchema),
	abilities: z.array(abilityRefSchema)
});
const kitsFileSchema = z.object({ kits: z.array(kitSchema) });
const spawnPointSchema = z.object({
	x: numberLike,
	y: numberLike,
	z: numberLike,
	yaw: numberLike.optional(),
	pitch: numberLike.optional()
});
const creatorsSchema = z.union([z.array(z.string().uuid()), z.null()]);
const gameMapSchema = z.object({
	id: stringLike,
	voidLevel: numberLike,
	maxPlayers: z.number().int().optional(),
	worldBorderSize: numberLike,
	creators: creatorsSchema,
	spawnPoints: z.array(spawnPointSchema)
});
const mapsFileSchema = z.object({
	gameMaps: z.array(gameMapSchema),
	hubMaps: z.array(z.object({
		id: stringLike,
		voidLevel: numberLike,
		worldBorderSize: numberLike,
		creators: creatorsSchema,
		spawnPoints: z.array(spawnPointSchema)
	}))
});
const minigameTypeSchema = z.enum(["ffa", "team_based_stocks"]).catch("ffa");
const minigameSchema = z.object({
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
	stocks: z.number().int().optional()
});
const minigamesFileSchema = z.object({ minigames: z.array(minigameSchema) });

//#endregion
export { abilitiesFileSchema, abilityRefSchema, abilitySchema, abilityTypeSchema, abilityUsageSchema, creatorsSchema, gameMapSchema, kitSchema, kitsFileSchema, mapsFileSchema, metadataSchema, minigameSchema, minigameTypeSchema, minigamesFileSchema, passiveRefOverrideSchema, passiveRefSchema, passiveSchema, passivesFileSchema, plugin_exports, spawnPointSchema };
//# sourceMappingURL=plugin-CVhdlvql.js.map