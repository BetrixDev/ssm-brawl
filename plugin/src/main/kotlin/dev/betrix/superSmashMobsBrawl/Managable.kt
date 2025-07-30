package dev.betrix.superSmashMobsBrawl

import gg.flyte.twilight.event.TwilightListener
import gg.flyte.twilight.scheduler.TwilightRunnable
import kotlinx.coroutines.Job

abstract class Managable {
    protected val runnables = arrayListOf<TwilightRunnable>()
    protected val listeners = arrayListOf<TwilightListener>()
    protected val jobs = arrayListOf<Job>()

    open fun setupAsync() {
        setup()
    }

    open fun setup() {}

    open fun teardownAsync() {
        teardown()
    }

    open fun teardown() {
        runnables.forEach { it.cancel() }
        listeners.forEach { it.unregister() }
        jobs.forEach { it.cancel() }

        runnables.clear()
        listeners.clear()
        jobs.clear()
    }
}