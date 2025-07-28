package dev.betrix.superSmashMobsBrawl.di

import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.context.GlobalContext

/**
 * Extension functions and utilities for easier Koin dependency injection usage.
 */

/**
 * Interface that classes can implement to get easy access to dependency injection.
 * 
 * Usage:
 * ```kotlin
 * class MyService : Injectable {
 *     private val plugin: SuperSmashMobsBrawl by inject()
 *     private val twilight: Twilight by inject()
 * }
 * ```
 */
interface Injectable : KoinComponent

/**
 * Get a dependency from Koin without implementing Injectable.
 * Useful for one-off dependency retrieval.
 * 
 * Usage:
 * ```kotlin
 * val plugin = koinGet<SuperSmashMobsBrawl>()
 * ```
 */
inline fun <reified T> koinGet(): T = GlobalContext.get().get()

/**
 * Lazy inject a dependency from Koin without implementing Injectable.
 * The dependency will be resolved when first accessed.
 * 
 * Usage:
 * ```kotlin
 * val plugin by koinInject<SuperSmashMobsBrawl>()
 * ```
 */
inline fun <reified T> koinInject(): Lazy<T> = lazy { koinGet<T>() }