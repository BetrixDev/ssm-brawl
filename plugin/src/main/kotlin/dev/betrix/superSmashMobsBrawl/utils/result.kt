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
