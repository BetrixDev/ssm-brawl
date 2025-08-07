package dev.betrix.superSmashMobsBrawl.commands.resolvers

import dev.betrix.superSmashMobsBrawl.models.brawlData.MinigameDef
import dev.betrix.superSmashMobsBrawl.services.MinigameService
import dev.rollczi.litecommands.argument.Argument
import dev.rollczi.litecommands.argument.parser.ParseResult
import dev.rollczi.litecommands.argument.resolver.ArgumentResolver
import dev.rollczi.litecommands.invocation.Invocation
import dev.rollczi.litecommands.suggestion.SuggestionContext
import dev.rollczi.litecommands.suggestion.SuggestionResult
import org.bukkit.command.CommandSender
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class MinigameDefinitionArgument : ArgumentResolver<CommandSender, MinigameDef>(), KoinComponent {
    private val minigameService: MinigameService by inject()

    override fun parse(
        invocation: Invocation<CommandSender?>?,
        context: Argument<MinigameDef?>?,
        argument: String?,
    ): ParseResult<MinigameDef?>? {
        if (argument.isNullOrEmpty()) {
            return ParseResult.failure("Queue should not be empty value")
        }

        val minigameDefinition = minigameService.getMinigameData(argument)

        if (minigameDefinition == null) {
            val possibleMatch = minigameService.findClosestMinigameById(argument)

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
        argument: Argument<MinigameDef?>?,
        context: SuggestionContext?,
    ): SuggestionResult? {
        return SuggestionResult.of(minigameService.getAllMinigameData().map { it.id })
    }
}
