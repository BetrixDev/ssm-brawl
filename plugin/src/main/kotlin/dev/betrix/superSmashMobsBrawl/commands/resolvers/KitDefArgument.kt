package dev.betrix.superSmashMobsBrawl.commands.resolvers

import dev.betrix.superSmashMobsBrawl.models.brawlData.KitDef
import dev.betrix.superSmashMobsBrawl.services.KitService
import dev.rollczi.litecommands.argument.Argument
import dev.rollczi.litecommands.argument.parser.ParseResult
import dev.rollczi.litecommands.argument.resolver.ArgumentResolver
import dev.rollczi.litecommands.invocation.Invocation
import dev.rollczi.litecommands.suggestion.SuggestionContext
import dev.rollczi.litecommands.suggestion.SuggestionResult
import org.bukkit.command.CommandSender
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class KitDefArgument : ArgumentResolver<CommandSender, KitDef>(), KoinComponent {
    private val kitService: KitService by inject()

    override fun parse(
        invocation: Invocation<CommandSender?>?,
        context: Argument<KitDef?>?,
        argument: String?,
    ): ParseResult<KitDef?>? {
        if (argument.isNullOrEmpty()) {
            return ParseResult.failure("Queue should not be empty value")
        }

        val kitDefinition = kitService.getKitData(argument)

        if (kitDefinition == null) {
            val possibleMatch = kitService.findClosestKitById(argument)

            return if (possibleMatch != null) {
                ParseResult.failure("Invalid kit id. Did you mean ${possibleMatch.id}?")
            } else {
                ParseResult.failure("Invalid kit id")
            }
        }

        return ParseResult.success(kitDefinition)
    }

    override fun suggest(
        invocation: Invocation<CommandSender?>?,
        argument: Argument<KitDef?>?,
        context: SuggestionContext?,
    ): SuggestionResult? {
        return SuggestionResult.of(kitService.getAllKitData().map { it.id })
    }
}
