CREATE TABLE "player_ability_stats" (
	"player_uuid" text NOT NULL,
	"ability_id" text NOT NULL,
	"stat_id" text NOT NULL,
	"value" jsonb NOT NULL,
	CONSTRAINT "player_ability_stats_pk" PRIMARY KEY("player_uuid","ability_id","stat_id")
);
--> statement-breakpoint
CREATE TABLE "player_ability_stats_history" (
	"id" serial PRIMARY KEY NOT NULL,
	"player_uuid" text NOT NULL,
	"ability_id" text NOT NULL,
	"stat_id" text NOT NULL,
	"value" jsonb NOT NULL,
	"timestamp" timestamp DEFAULT now() NOT NULL
);
--> statement-breakpoint
CREATE TABLE "player_general_stats" (
	"player_uuid" text NOT NULL,
	"stat_id" text NOT NULL,
	"value" jsonb NOT NULL,
	CONSTRAINT "player_general_stats_pk" PRIMARY KEY("player_uuid","stat_id")
);
--> statement-breakpoint
CREATE TABLE "player_general_stats_history" (
	"id" serial PRIMARY KEY NOT NULL,
	"player_uuid" text NOT NULL,
	"stat_id" text NOT NULL,
	"value" jsonb NOT NULL,
	"timestamp" timestamp DEFAULT now() NOT NULL
);
--> statement-breakpoint
CREATE TABLE "player_kit_stats" (
	"player_uuid" text NOT NULL,
	"kit_id" text NOT NULL,
	"stat_id" text NOT NULL,
	"value" jsonb NOT NULL,
	CONSTRAINT "player_kit_stats_pk" PRIMARY KEY("player_uuid","kit_id","stat_id")
);
--> statement-breakpoint
CREATE TABLE "player_kit_stats_history" (
	"id" serial PRIMARY KEY NOT NULL,
	"player_uuid" text NOT NULL,
	"kit_id" text NOT NULL,
	"stat_id" text NOT NULL,
	"value" jsonb NOT NULL,
	"timestamp" timestamp DEFAULT now() NOT NULL
);
--> statement-breakpoint
CREATE TABLE "player_minigame_stats" (
	"player_uuid" text NOT NULL,
	"minigame_id" text NOT NULL,
	"stat_id" text NOT NULL,
	"value" jsonb NOT NULL,
	CONSTRAINT "player_minigame_stats_pk" PRIMARY KEY("player_uuid","minigame_id","stat_id")
);
--> statement-breakpoint
CREATE TABLE "player_minigame_stats_history" (
	"id" serial PRIMARY KEY NOT NULL,
	"player_uuid" text NOT NULL,
	"minigame_id" text NOT NULL,
	"stat_id" text NOT NULL,
	"value" jsonb NOT NULL,
	"timestamp" timestamp DEFAULT now() NOT NULL
);
--> statement-breakpoint
CREATE TABLE "player_parkour_stats" (
	"player_uuid" text NOT NULL,
	"map_id" text NOT NULL,
	"stat_id" text NOT NULL,
	"value" jsonb NOT NULL,
	CONSTRAINT "player_parkour_stats_pk" PRIMARY KEY("player_uuid","map_id","stat_id")
);
--> statement-breakpoint
CREATE TABLE "player_parkour_stats_history" (
	"id" serial PRIMARY KEY NOT NULL,
	"player_uuid" text NOT NULL,
	"map_id" text NOT NULL,
	"stat_id" text NOT NULL,
	"value" jsonb NOT NULL,
	"timestamp" timestamp DEFAULT now() NOT NULL
);
--> statement-breakpoint
CREATE TABLE "player_passive_stats" (
	"player_uuid" text NOT NULL,
	"passive_id" text NOT NULL,
	"stat_id" text NOT NULL,
	"value" jsonb NOT NULL,
	CONSTRAINT "player_passive_stats_pk" PRIMARY KEY("player_uuid","passive_id","stat_id")
);
--> statement-breakpoint
CREATE TABLE "player_passive_stats_history" (
	"id" serial PRIMARY KEY NOT NULL,
	"player_uuid" text NOT NULL,
	"passive_id" text NOT NULL,
	"stat_id" text NOT NULL,
	"value" jsonb NOT NULL,
	"timestamp" timestamp DEFAULT now() NOT NULL
);
--> statement-breakpoint
ALTER TABLE "player_ability_stats" ADD CONSTRAINT "player_ability_stats_player_uuid_players_uuid_fk" FOREIGN KEY ("player_uuid") REFERENCES "public"."players"("uuid") ON DELETE cascade ON UPDATE no action;--> statement-breakpoint
ALTER TABLE "player_general_stats" ADD CONSTRAINT "player_general_stats_player_uuid_players_uuid_fk" FOREIGN KEY ("player_uuid") REFERENCES "public"."players"("uuid") ON DELETE cascade ON UPDATE no action;--> statement-breakpoint
ALTER TABLE "player_kit_stats" ADD CONSTRAINT "player_kit_stats_player_uuid_players_uuid_fk" FOREIGN KEY ("player_uuid") REFERENCES "public"."players"("uuid") ON DELETE cascade ON UPDATE no action;--> statement-breakpoint
ALTER TABLE "player_minigame_stats" ADD CONSTRAINT "player_minigame_stats_player_uuid_players_uuid_fk" FOREIGN KEY ("player_uuid") REFERENCES "public"."players"("uuid") ON DELETE cascade ON UPDATE no action;--> statement-breakpoint
ALTER TABLE "player_parkour_stats" ADD CONSTRAINT "player_parkour_stats_player_uuid_players_uuid_fk" FOREIGN KEY ("player_uuid") REFERENCES "public"."players"("uuid") ON DELETE cascade ON UPDATE no action;--> statement-breakpoint
ALTER TABLE "player_passive_stats" ADD CONSTRAINT "player_passive_stats_player_uuid_players_uuid_fk" FOREIGN KEY ("player_uuid") REFERENCES "public"."players"("uuid") ON DELETE cascade ON UPDATE no action;--> statement-breakpoint
CREATE INDEX "player_ability_stats_history_player_uuid_idx" ON "player_ability_stats_history" USING btree ("player_uuid");--> statement-breakpoint
CREATE INDEX "player_ability_stats_history_timestamp_idx" ON "player_ability_stats_history" USING btree ("timestamp");--> statement-breakpoint
CREATE INDEX "player_general_stats_history_player_uuid_idx" ON "player_general_stats_history" USING btree ("player_uuid");--> statement-breakpoint
CREATE INDEX "player_general_stats_history_timestamp_idx" ON "player_general_stats_history" USING btree ("timestamp");--> statement-breakpoint
CREATE INDEX "player_kit_stats_history_player_uuid_idx" ON "player_kit_stats_history" USING btree ("player_uuid");--> statement-breakpoint
CREATE INDEX "player_kit_stats_history_timestamp_idx" ON "player_kit_stats_history" USING btree ("timestamp");--> statement-breakpoint
CREATE INDEX "player_minigame_stats_history_player_uuid_idx" ON "player_minigame_stats_history" USING btree ("player_uuid");--> statement-breakpoint
CREATE INDEX "player_minigame_stats_history_timestamp_idx" ON "player_minigame_stats_history" USING btree ("timestamp");--> statement-breakpoint
CREATE INDEX "player_parkour_stats_history_player_uuid_idx" ON "player_parkour_stats_history" USING btree ("player_uuid");--> statement-breakpoint
CREATE INDEX "player_parkour_stats_history_timestamp_idx" ON "player_parkour_stats_history" USING btree ("timestamp");--> statement-breakpoint
CREATE INDEX "player_passive_stats_history_player_uuid_idx" ON "player_passive_stats_history" USING btree ("player_uuid");--> statement-breakpoint
CREATE INDEX "player_passive_stats_history_timestamp_idx" ON "player_passive_stats_history" USING btree ("timestamp");