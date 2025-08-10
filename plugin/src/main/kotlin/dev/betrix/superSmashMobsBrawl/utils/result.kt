package dev.betrix.superSmashMobsBrawl.utils

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result

inline fun <T> resultRunCatching(block: () -> T): Result<T, Exception> =
    try {
        Ok(block())
    } catch (e: Exception) {
        Err(e)
    }

suspend fun <T> resultRunCatchingSuspend(block: suspend () -> T): Result<T, Exception> =
    try {
        Ok(block())
    } catch (e: Exception) {
        Err(e)
    }
