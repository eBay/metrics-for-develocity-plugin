package com.ebay.plugins.metrics.develocity.configcachemiss

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.hasEntry
import org.hamcrest.Matchers.hasSize
import org.hamcrest.Matchers.sameInstance
import org.testng.annotations.Test

class ConfigCacheMissReportTaskTest {

    @Test
    fun reduceKeyWithNoRegexes() {
        val expected = "original"
        val reducedKey = ConfigCacheMissReportTask.Companion.reduceKey(expected, emptyList())
        assertThat(reducedKey, sameInstance(expected))
    }

    @Test
    fun reduceKeyWithNoDefinedMatchGroups() {
        val expected = "one two three"
        val regex = Regex("two ")
        val reducedKey = ConfigCacheMissReportTask.Companion.reduceKey(expected, listOf(regex))
        assertThat(reducedKey, equalTo("one three"))
    }

    @Test
    fun reduceKeyWithOneMatchGroup() {
        val expected = "one two three"
        val regex = Regex(" (two) ")
        val reducedKey = ConfigCacheMissReportTask.Companion.reduceKey(expected, listOf(regex))
        assertThat(reducedKey, equalTo("onetwothree"))
    }

    @Test
    fun reduceKeyWithMultipleMatchGroups() {
        val expected = "xone two threex"
        val regex = Regex("(one).*(three)")
        val reducedKey = ConfigCacheMissReportTask.Companion.reduceKey(expected, listOf(regex))
        assertThat(reducedKey, equalTo("xonethreex"))
    }

    @Test
    fun reduceKeyWithMultipleMatchGroupsInMultiplePatterns() {
        val expected = "xone two threex"
        val regexes = listOf(
            Regex("one "),
            Regex(" three"),
        )
        val reducedKey = ConfigCacheMissReportTask.Companion.reduceKey(expected, regexes)
        assertThat(reducedKey, equalTo("xtwox"))
    }

    @Test
    fun reduceMap() {
        val inputMap = mapOf(
            "first second third" to 2,
            "first 2 third" to 3,
        )
        val regexes = listOf(
            " second".toRegex(),
            " 2".toRegex(),
        )
        val reducedMap = ConfigCacheMissReportTask.Companion.reduceMap(inputMap, regexes)
        assertThat(reducedMap.entries, hasSize(equalTo(1)))
        assertThat(reducedMap, allOf(
            hasEntry(equalTo("first third"), equalTo(5)),
        ))
    }
}