package dev.betrix.superSmashMobsBrawl.utils

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result

fun <T> resultRunCatching(cb: () -> T): Result<T, Exception> {
    return try {
        Ok(cb())
    } catch (e: Exception) {
        Err(e)
    }
}

fun <T, E> resultRunCatching(parseError: (e: Exception) -> E, cb: () -> T): Result<T, E> {
    return try {
        Ok(cb())
    } catch (e: Exception) {
        Err(parseError(e))
    }
}
