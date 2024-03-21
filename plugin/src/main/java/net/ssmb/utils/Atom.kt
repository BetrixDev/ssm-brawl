package net.ssmb.utils

class Atom<T>(initialState: T) {
    private var state: T = initialState
    private val subscribers = mutableListOf<(T, T) -> Unit>()

    fun get(): T {
        if (state == null) throw IllegalStateException("Atom has not yet been initialized")

        return state
    }

    fun set(newState: T) {
        val oldValue = state
        state = newState

        subscribers.forEach {
            it.invoke(newState, oldValue)
        }
    }

    fun subscribe(callback: (newValue: T, oldValue: T) -> Unit): () -> Unit {
        subscribers.add(callback)

        return {
            subscribers.remove(callback)
        }
    }
}