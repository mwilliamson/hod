package org.zwobble.hod.util

fun <T> Iterable<T>.allIndexed(predicate: (Int, T) -> Boolean): Boolean {
    this.forEachIndexed { index, element ->
        if (!predicate(index, element)) {
            return false
        }
    }
    return true
}
