package dev.betrix.superSmashMobsBrawl.commands.resolvers

import dev.betrix.superSmashMobsBrawl.components.LeaveSpecifier
import dev.rollczi.litecommands.argument.Argument
import dev.rollczi.litecommands.argument.parser.ParseResult
import dev.rollczi.litecommands.argument.resolver.ArgumentResolver
import dev.rollczi.litecommands.invocation.Invocation
import dev.rollczi.litecommands.suggestion.SuggestionContext
import dev.rollczi.litecommands.suggestion.SuggestionResult
import org.bukkit.command.CommandSender

class LeaveSpecifierArgument : ArgumentResolver<CommandSender, LeaveSpecifier>() {

    override fun parse(
        invocation: Invocation<CommandSender?>?,
        context: Argument<LeaveSpecifier?>?,
        argument: String?,
    ): ParseResult<LeaveSpecifier?>? {
        if (argument.isNullOrEmpty()) {
            return null
        }

        val enumEntry = LeaveSpecifier.entries.find { it.name.lowercase() == argument.lowercase() }

        return ParseResult.success(enumEntry)
    }

    override fun suggest(
        invocation: Invocation<CommandSender?>?,
        argument: Argument<LeaveSpecifier?>?,
        context: SuggestionContext?,
    ): SuggestionResult? {
        return SuggestionResult.of(
            LeaveSpecifier.entries.map { it.name.lowercase() }
        )
    }
}
