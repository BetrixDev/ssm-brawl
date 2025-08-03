package dev.betrix.superSmashMobsBrawl

import gg.flyte.twilight.event.TwilightListener
import gg.flyte.twilight.scheduler.TwilightRunnable
import kotlinx.coroutines.Job

interface IManageable {
    fun setup() {}

    fun teardown()
}

abstract class Manageable : IManageable {
    protected val runnables = arrayListOf<TwilightRunnable>()
    protected val listeners = arrayListOf<TwilightListener>()
    protected val jobs = arrayListOf<Job>()

    open suspend fun setupAsync() {
        setup()
    }

    override fun setup() {}

    open suspend fun teardownAsync() {
        teardown()
    }

    override fun teardown() {
        runnables.forEach { it.cancel() }
        listeners.forEach { it.unregister() }
        jobs.forEach { it.cancel() }

        runnables.clear()
        listeners.clear()
        jobs.clear()
    }
}
