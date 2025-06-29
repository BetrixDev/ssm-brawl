package dev.betrix.superSmashMobsBrawl.commands.argumentResolvers

import dev.betrix.superSmashMobsBrawl.enums.Queue
import dev.rollczi.litecommands.argument.Argument
import dev.rollczi.litecommands.argument.parser.ParseResult
import dev.rollczi.litecommands.argument.resolver.ArgumentResolver
import dev.rollczi.litecommands.invocation.Invocation
import dev.rollczi.litecommands.suggestion.SuggestionContext
import dev.rollczi.litecommands.suggestion.SuggestionResult
import org.bukkit.command.CommandSender

class QueueArgument : ArgumentResolver<CommandSender, Queue>() {
    override fun parse(
        invocation: Invocation<CommandSender?>?,
        context: Argument<Queue?>?,
        argument: String?,
    ): ParseResult<Queue?>? {
        if (argument == null) {
            return ParseResult.failure("Queue should not be empty value")
        }

        val queueEntry = Queue.fromId(argument)

        if (queueEntry == null) {
            val possibleMatch = Queue.findClosest(argument)

            return if (possibleMatch != null) {
                ParseResult.failure("Invalid queue id. Did you mean ${possibleMatch.id}?")
            } else {
                ParseResult.failure("Invalid queue id")
            }
        }

        return ParseResult.success(queueEntry)
    }

    override fun suggest(
        invocation: Invocation<CommandSender?>?,
        argument: Argument<Queue?>?,
        context: SuggestionContext?,
    ): SuggestionResult? {
        return SuggestionResult.of(Queue.entries.map { it.id })
    }
}
