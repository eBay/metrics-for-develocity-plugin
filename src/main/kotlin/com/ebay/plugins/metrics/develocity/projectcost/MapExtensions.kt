package com.ebay.plugins.metrics.develocity.projectcost

/**
 * Extension function to merge two maps together.  If a key exists in both maps, the value will be
 * determined by the merge function.  If the value exists in only one map, it will be included in the
 * resulting map as-is.
 */
internal fun <K, V> Map<K, V>.merge(other: Map<K, V>, mergeFunction: (V, V) -> V): Map<K, V> {
    return this.toMutableMap().apply {
        other.forEach { (key, otherVal) ->
            this[key] = this[key]?.let { mergeFunction(it, otherVal) } ?: otherVal
        }
    }
}