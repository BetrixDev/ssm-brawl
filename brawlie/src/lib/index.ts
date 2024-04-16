import { env } from "env/brawlie";
import { Promisable, CamelCase } from "type-fest";
import { isRegExp } from "util/types";
import toCamelCase from "camelcase";
import {
  Client,
  CommandInteraction,
  LocalizationMap,
  ChannelType,
  AutocompleteInteraction,
  CommandInteractionOption,
  Interaction,
  ButtonInteraction,
  AnySelectMenuInteraction,
  StringSelectMenuInteraction,
  ChannelSelectMenuInteraction,
  MentionableSelectMenuInteraction,
  RoleSelectMenuInteraction,
  UserSelectMenuInteraction,
  ModalSubmitInteraction,
  ClientEvents,
  ApplicationCommandData,
  ApplicationCommandOptionType,
  ApplicationCommandOptionData,
  REST,
  Routes,
} from "discord.js";

const APP_COMMAND_OPTION_STRING_TO_ENUM = {
  attachment: ApplicationCommandOptionType.Attachment,
  boolean: ApplicationCommandOptionType.Boolean,
  channel: ApplicationCommandOptionType.Channel,
  integer: ApplicationCommandOptionType.Integer,
  mentionable: ApplicationCommandOptionType.Mentionable,
  number: ApplicationCommandOptionType.Number,
  user: ApplicationCommandOptionType.User,
  string: ApplicationCommandOptionType.String,
  subcommand: ApplicationCommandOptionType.Subcommand,
  subcommandGroup: ApplicationCommandOptionType.SubcommandGroup,
} as const;

type ApplicationCommandOptionTypeString =
  keyof typeof APP_COMMAND_OPTION_STRING_TO_ENUM;

type SelectMenuTypeString =
  | "string"
  | "user"
  | "channel"
  | "role"
  | "mentionable";

type SlashAutoCompleteOption = (
  interaction: AutocompleteInteraction,
  client: Client
) => void | Promise<void>;

type SlashOptionBaseOptions = {
  description: string;
  descriptionLocalizations?: LocalizationMap;
  nameLocalizations?: LocalizationMap;
  required?: boolean;
  type: ApplicationCommandOptionTypeString;
};

type SlashOptionStringOptions = {
  type: "string";
  autocomplete?: SlashAutoCompleteOption;
  maxLength?: number;
  minLength?: number;
} & SlashOptionBaseOptions;

type SlashOptionChannelOptions = {
  type: "channel";
  channelTypes?: ChannelType[];
} & SlashOptionBaseOptions;

type SlashOptionNumberOptions = {
  type: "number" | "integer";
  autocomplete?: SlashAutoCompleteOption;
  maxValue?: number;
  minValue?: number;
} & SlashOptionBaseOptions;

type SlashOptionOptions =
  | SlashOptionBaseOptions
  | SlashOptionStringOptions
  | SlashOptionChannelOptions
  | SlashOptionNumberOptions;

type CommandArguments = Record<string, SlashOptionOptions>;

type CommandOptions<T> = {
  name: string;
  args?: T;
} & ApplicationCommandData;

type ReturnTypeOfInteractionOption<T extends keyof CommandInteractionOption> =
  NonNullable<CommandInteractionOption[T]>;

type ReturnTypeOfType<T> = T extends "string"
  ? string
  : T extends "integer" | "number"
    ? number
    : T extends "attachment"
      ? ReturnTypeOfInteractionOption<"attachment">
      : T extends "boolean"
        ? boolean
        : T extends "user"
          ? ReturnTypeOfInteractionOption<"user">
          : T extends "channel"
            ? ReturnTypeOfInteractionOption<"channel">
            : unknown;

type CommandRunFunction = (
  interaction: CommandInteraction,
  args: any,
  client: Client
) => Promisable<void>;

type ComponentRunFunction<T extends Interaction = never> = (
  interaction: T,
  client: Client
) => Promisable<void>;

type StoredComponent = {
  id: RegExp | String;
  run: ComponentRunFunction;
};

type StoredCommand = {
  commandOptions: CommandOptions<CommandArguments>;
  commandFunc: CommandRunFunction;
};

export interface DiscordFPHandlerOptions {
  importPaths?: string[] | string;
}

let client: Client;
const commands = new Map<string, StoredCommand>();
const buttons: StoredComponent[] = [];
const selectMenus: StoredComponent[] = [];
const modals: StoredComponent[] = [];

export function command<T extends CommandArguments>(
  options: CommandOptions<T>,
  run: (
    interaction: CommandInteraction,
    args: {
      [K in keyof T as CamelCase<K>]: T[K]["required"] extends true
        ? ReturnTypeOfType<T[K]["type"]>
        : ReturnTypeOfType<T[K]["type"]> | undefined;
    },
    client: Client
  ) => Promisable<void>
) {
  commands.set(options.name, { commandOptions: options, commandFunc: run });
}

//  export function on<T extends keyof ClientEvents>(
//    eventName: T,
//    run: (...args: ClientEvents[T]) => Promisable<void>
//  ) {
//    if (client) {
//      client.on(eventName, run);
//    } else {
//      throw new Error(
//        `Unable to register client event ${eventName}, please call initInteractionHandler before calling this function`
//      );
//    }
//  }

//  export function once<T extends keyof ClientEvents>(
//    eventName: T,
//    run: (...args: ClientEvents[T]) => Promisable<void>
//  ) {
//    if (client) {
//      client.once(eventName, run);
//    } else {
//      throw new Error(
//        `Unable to register client event ${eventName}, please call initInteractionHandler before calling this function`
//      );
//    }
//  }
//
export function button(
  options: { id: RegExp | string },
  run: ComponentRunFunction<ButtonInteraction>
) {
  buttons.push({ id: options.id, run });
}

export function selectMenu<T extends SelectMenuTypeString>(
  options: {
    id: RegExp | string;
    type?: T;
  },
  run: ComponentRunFunction<
    T extends undefined
      ? AnySelectMenuInteraction
      : T extends "string"
        ? StringSelectMenuInteraction
        : T extends "channel"
          ? ChannelSelectMenuInteraction
          : T extends "mentionable"
            ? MentionableSelectMenuInteraction
            : T extends "role"
              ? RoleSelectMenuInteraction
              : T extends "user"
                ? UserSelectMenuInteraction
                : unknown
  >
) {
  selectMenus.push({ id: options.id, run });
}

export function modal(
  options: { id: RegExp | string },
  run: (
    interaction: ModalSubmitInteraction,
    client: Client
  ) => void | Promise<void>
) {
  modals.push({ id: options.id, run });
}

function getComponentsArray(interaction: Interaction) {
  if (interaction.isButton()) {
    return buttons;
  } else if (interaction.isAnySelectMenu()) {
    return selectMenus;
  } else {
    return modals;
  }
}

function getMatchingComponent(customId: string, components: StoredComponent[]) {
  const matchingComponent = components.find((c) => {
    if (typeof c.id === "string") {
      return c.id === customId;
    } else if (isRegExp(c.id)) {
      const match = customId.match(c.id);
      return match?.length ?? 0 > 0;
    }
  });

  if (!matchingComponent) {
    throw new Error(
      `Unable to find a matching component function for component with id: ${customId}`
    );
  }

  return matchingComponent;
}

export async function handleInteraction(interaction: Interaction) {
  if (interaction.isMessageComponent()) {
    const customId = interaction.customId;

    const componentsArray = getComponentsArray(interaction);
    const matchingComponent = getMatchingComponent(customId, componentsArray);

    matchingComponent.run(interaction as never, client);
  } else if (interaction.isCommand()) {
    const commandName = interaction.commandName;

    const foundCommand = commands.get(commandName);

    if (!foundCommand) {
      throw new Error(
        `Command object not found for interaction of name ${commandName}`
      );
    }

    const inputtedValues = interaction.options.data;
    const convertedArgs: Record<string, any> = {};

    inputtedValues.forEach((val) => {
      const [valName, , valValue] = Object.values(val);

      convertedArgs[toCamelCase(valName)] = valValue;
    });

    foundCommand.commandFunc(interaction, convertedArgs, client);
  } else if (interaction.isAutocomplete()) {
    const commandName = interaction.commandName;
    const focusedArg = interaction.options.getFocused(true).name;

    const foundCommand = commands.get(commandName);

    if (!foundCommand) {
      throw new Error(
        `Command object not found for interaction of name ${commandName}`
      );
    }

    const commandArgs = foundCommand.commandOptions.args;

    if (!commandArgs) {
      throw new Error(
        `Unable to find command with valid args for ${commandName}`
      );
    }

    const arg = commandArgs[focusedArg];

    if (!arg) {
      throw new Error(
        `Unable to find arg ${focusedArg} within the command object for ${commandName}`
      );
    }

    if (
      arg.type === "string" ||
      arg.type === "number" ||
      arg.type === "integer"
    ) {
      const autocomplete = (arg as SlashOptionStringOptions)?.autocomplete;

      if (!autocomplete) {
        throw new Error(
          `No autocomplete function exists for command object with name ${commandName} and arg ${focusedArg}`
        );
      } else {
        autocomplete(interaction, client);
      }
    }
  }
}

export async function registerCommands() {
  if (!client.application) {
    throw new Error(
      "Client not ready yet, please invoke this after after your client has started"
    );
  }

  // client.application.commands.set(
  //   [...commands.values()].map((c) => {
  //     const options: ApplicationCommandOptionData[] = [];

  //     if (c.commandOptions.args) {
  //       Object.entries(c.commandOptions.args).forEach(([key, val]) => {
  //         options.push({
  //           name: key,
  //           ...val,
  //           type: APP_COMMAND_OPTION_STRING_TO_ENUM[val.type],
  //         } as ApplicationCommandOptionData);
  //       });
  //     }

  //     return {
  //       ...c.commandOptions,
  //       options,
  //     };
  //   })
  // );

  const rest = new REST({ version: "10" }).setToken(env.BOT_TOKEN);

  await rest.put(Routes.applicationCommands(client.user!.id), { body: {} });

  console.log(`Registered ${commands.size} commands`);
}

export async function initInteractionHandler(
  discordClient: Client,
  options?: DiscordFPHandlerOptions
) {
  client = discordClient;

  // const importPaths = options?.importPaths ?? "*/**";
  // await importx(...importPaths);
}
