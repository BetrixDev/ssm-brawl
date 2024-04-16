import { trpcClient } from "../api.js";
import { command, selectMenu } from "../lib/index.js";

command(
  {
    name: "player",
    description: "Lookup stats for any player!",
    args: {
      uuid: {
        type: "string",
        description: "Player to lookup",
        required: true,
        autocomplete: async (interaction) => {
          const query = interaction.options.getFocused();

          return await trpcClient;
        },
      },
    },
  },
  (interaction, args, client) => {}
);
