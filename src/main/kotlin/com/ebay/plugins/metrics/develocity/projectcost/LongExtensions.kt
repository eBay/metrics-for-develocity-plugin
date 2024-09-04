package com.ebay.plugins.metrics.develocity.projectcost

/**
 * Division with protection against divide-by-zero.  Attempts at dividing by zero
 * will return zero.
 */
fun Long.safeDiv(denominator: Long): Long {
    return if (denominator == 0L) 0L else {
        (toDouble() / denominator.toDouble()).toLong()
    }
}