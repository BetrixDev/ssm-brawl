package dev.betrix.superSmashMobsBrawl.utils

class Atom<T>(initialValue: T) {
    private var _value: T = initialValue
    private val subscribers = mutableListOf<(T) -> Unit>()

    var value: T
        get() = _value
        set(newValue) {
            if (_value != newValue) {
                _value = newValue
                notifySubscribers()
            }
        }

    fun subscribe(listener: (T) -> Unit): () -> Unit {
        subscribers.add(listener)

        return { subscribers.remove(listener) }
    }

    fun unsubscribe(listener: (T) -> Unit) {
        subscribers.remove(listener)
    }

    fun unsubscribeAll() {
        subscribers.clear()
    }

    private fun notifySubscribers() {
        subscribers.forEach { it(_value) }
    }
}