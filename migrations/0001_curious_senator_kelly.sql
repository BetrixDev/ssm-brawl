DO $$ BEGIN
 ALTER TABLE "abilities_to_kits" ADD CONSTRAINT "abilities_to_kits_kit_id_kits_id_fk" FOREIGN KEY ("kit_id") REFERENCES "public"."kits"("id") ON DELETE no action ON UPDATE no action;
EXCEPTION
 WHEN duplicate_object THEN null;
END $$;
--> statement-breakpoint
DO $$ BEGIN
 ALTER TABLE "abilities_to_kits" ADD CONSTRAINT "abilities_to_kits_ability_id_abilities_id_fk" FOREIGN KEY ("ability_id") REFERENCES "public"."abilities"("id") ON DELETE no action ON UPDATE no action;
EXCEPTION
 WHEN duplicate_object THEN null;
END $$;
--> statement-breakpoint
DO $$ BEGIN
 ALTER TABLE "friendships" ADD CONSTRAINT "friendships_uuid_1_basic_player_data_uuid_fk" FOREIGN KEY ("uuid_1") REFERENCES "public"."basic_player_data"("uuid") ON DELETE no action ON UPDATE no action;
EXCEPTION
 WHEN duplicate_object THEN null;
END $$;
--> statement-breakpoint
DO $$ BEGIN
 ALTER TABLE "friendships" ADD CONSTRAINT "friendships_uuid_2_basic_player_data_uuid_fk" FOREIGN KEY ("uuid_2") REFERENCES "public"."basic_player_data"("uuid") ON DELETE no action ON UPDATE no action;
EXCEPTION
 WHEN duplicate_object THEN null;
END $$;
--> statement-breakpoint
DO $$ BEGIN
 ALTER TABLE "message_viewers" ADD CONSTRAINT "message_viewers_channel_id_message_channels_id_fk" FOREIGN KEY ("channel_id") REFERENCES "public"."message_channels"("id") ON DELETE no action ON UPDATE no action;
EXCEPTION
 WHEN duplicate_object THEN null;
END $$;
--> statement-breakpoint
DO $$ BEGIN
 ALTER TABLE "message_viewers" ADD CONSTRAINT "message_viewers_player_uuid_basic_player_data_uuid_fk" FOREIGN KEY ("player_uuid") REFERENCES "public"."basic_player_data"("uuid") ON DELETE no action ON UPDATE no action;
EXCEPTION
 WHEN duplicate_object THEN null;
END $$;
--> statement-breakpoint
DO $$ BEGIN
 ALTER TABLE "messages" ADD CONSTRAINT "messages_channel_id_message_channels_id_fk" FOREIGN KEY ("channel_id") REFERENCES "public"."message_channels"("id") ON DELETE no action ON UPDATE no action;
EXCEPTION
 WHEN duplicate_object THEN null;
END $$;
--> statement-breakpoint
DO $$ BEGIN
 ALTER TABLE "messages" ADD CONSTRAINT "messages_author_uuid_basic_player_data_uuid_fk" FOREIGN KEY ("author_uuid") REFERENCES "public"."basic_player_data"("uuid") ON DELETE no action ON UPDATE no action;
EXCEPTION
 WHEN duplicate_object THEN null;
END $$;
--> statement-breakpoint
DO $$ BEGIN
 ALTER TABLE "parties" ADD CONSTRAINT "parties_owner_uuid_basic_player_data_uuid_fk" FOREIGN KEY ("owner_uuid") REFERENCES "public"."basic_player_data"("uuid") ON DELETE cascade ON UPDATE no action;
EXCEPTION
 WHEN duplicate_object THEN null;
END $$;
--> statement-breakpoint
DO $$ BEGIN
 ALTER TABLE "party_guests" ADD CONSTRAINT "party_guests_player_uuid_basic_player_data_uuid_fk" FOREIGN KEY ("player_uuid") REFERENCES "public"."basic_player_data"("uuid") ON DELETE no action ON UPDATE no action;
EXCEPTION
 WHEN duplicate_object THEN null;
END $$;
--> statement-breakpoint
DO $$ BEGIN
 ALTER TABLE "party_guests" ADD CONSTRAINT "party_guests_party_id_parties_party_id_fk" FOREIGN KEY ("party_id") REFERENCES "public"."parties"("party_id") ON DELETE no action ON UPDATE no action;
EXCEPTION
 WHEN duplicate_object THEN null;
END $$;
--> statement-breakpoint
DO $$ BEGIN
 ALTER TABLE "party_invites" ADD CONSTRAINT "party_invites_party_id_parties_party_id_fk" FOREIGN KEY ("party_id") REFERENCES "public"."parties"("party_id") ON DELETE no action ON UPDATE no action;
EXCEPTION
 WHEN duplicate_object THEN null;
END $$;
--> statement-breakpoint
DO $$ BEGIN
 ALTER TABLE "party_invites" ADD CONSTRAINT "party_invites_inviter_uuid_basic_player_data_uuid_fk" FOREIGN KEY ("inviter_uuid") REFERENCES "public"."basic_player_data"("uuid") ON DELETE no action ON UPDATE no action;
EXCEPTION
 WHEN duplicate_object THEN null;
END $$;
--> statement-breakpoint
DO $$ BEGIN
 ALTER TABLE "party_invites" ADD CONSTRAINT "party_invites_invitee_uuid_basic_player_data_uuid_fk" FOREIGN KEY ("invitee_uuid") REFERENCES "public"."basic_player_data"("uuid") ON DELETE no action ON UPDATE no action;
EXCEPTION
 WHEN duplicate_object THEN null;
END $$;
--> statement-breakpoint
DO $$ BEGIN
 ALTER TABLE "passives_to_kits" ADD CONSTRAINT "passives_to_kits_kit_id_kits_id_fk" FOREIGN KEY ("kit_id") REFERENCES "public"."kits"("id") ON DELETE no action ON UPDATE no action;
EXCEPTION
 WHEN duplicate_object THEN null;
END $$;
--> statement-breakpoint
DO $$ BEGIN
 ALTER TABLE "passives_to_kits" ADD CONSTRAINT "passives_to_kits_passive_id_passives_id_fk" FOREIGN KEY ("passive_id") REFERENCES "public"."passives"("id") ON DELETE no action ON UPDATE no action;
EXCEPTION
 WHEN duplicate_object THEN null;
END $$;
--> statement-breakpoint
DO $$ BEGIN
 ALTER TABLE "queue" ADD CONSTRAINT "queue_player_uuid_basic_player_data_uuid_fk" FOREIGN KEY ("player_uuid") REFERENCES "public"."basic_player_data"("uuid") ON DELETE no action ON UPDATE no action;
EXCEPTION
 WHEN duplicate_object THEN null;
END $$;
--> statement-breakpoint
DO $$ BEGIN
 ALTER TABLE "queue" ADD CONSTRAINT "queue_party_id_parties_party_id_fk" FOREIGN KEY ("party_id") REFERENCES "public"."parties"("party_id") ON DELETE no action ON UPDATE no action;
EXCEPTION
 WHEN duplicate_object THEN null;
END $$;
--> statement-breakpoint
DO $$ BEGIN
 ALTER TABLE "queue" ADD CONSTRAINT "queue_minigame_id_minigames_id_fk" FOREIGN KEY ("minigame_id") REFERENCES "public"."minigames"("id") ON DELETE no action ON UPDATE no action;
EXCEPTION
 WHEN duplicate_object THEN null;
END $$;
--> statement-breakpoint
DO $$ BEGIN
 ALTER TABLE "usercache" ADD CONSTRAINT "usercache_uuid_basic_player_data_uuid_fk" FOREIGN KEY ("uuid") REFERENCES "public"."basic_player_data"("uuid") ON DELETE no action ON UPDATE no action;
EXCEPTION
 WHEN duplicate_object THEN null;
END $$;
