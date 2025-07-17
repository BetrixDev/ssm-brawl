package dev.betrix.superSmashMobsBrawl.commands.argumentResolvers

import dev.betrix.superSmashMobsBrawl.minigames.definitions.MinigameDefinition
import dev.betrix.superSmashMobsBrawl.registries.MinigameRegistry
import dev.rollczi.litecommands.argument.Argument
import dev.rollczi.litecommands.argument.parser.ParseResult
import dev.rollczi.litecommands.argument.resolver.ArgumentResolver
import dev.rollczi.litecommands.invocation.Invocation
import dev.rollczi.litecommands.suggestion.SuggestionContext
import dev.rollczi.litecommands.suggestion.SuggestionResult
import org.bukkit.command.CommandSender

class MinigameDefinitionArgument : ArgumentResolver<CommandSender, MinigameDefinition>() {
    override fun parse(
        invocation: Invocation<CommandSender?>?,
        context: Argument<MinigameDefinition?>?,
        argument: String?,
    ): ParseResult<MinigameDefinition?>? {
        if (argument.isNullOrEmpty()) {
            return ParseResult.failure("Queue should not be empty value")
        }

        val minigameDefinition = MinigameRegistry.getDefinition(argument)

        if (minigameDefinition == null) {
            val possibleMatch = MinigameRegistry.findClosest(argument)

            return if (possibleMatch != null) {
                ParseResult.failure("Invalid queue id. Did you mean ${possibleMatch.id}?")
            } else {
                ParseResult.failure("Invalid queue id")
            }
        }

        return ParseResult.success(minigameDefinition)
    }

    override fun suggest(
        invocation: Invocation<CommandSender?>?,
        argument: Argument<MinigameDefinition?>?,
        context: SuggestionContext?,
    ): SuggestionResult? {
        return SuggestionResult.of(MinigameRegistry.getAllDefinitions().map { it.id })
    }
}
