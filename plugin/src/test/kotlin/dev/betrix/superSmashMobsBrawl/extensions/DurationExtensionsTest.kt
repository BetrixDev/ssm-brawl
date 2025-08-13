package dev.betrix.superSmashMobsBrawl.extensions

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class DurationExtensionsTest :
    DescribeSpec({
        describe("Duration.ticks") {
            it("converts milliseconds to ticks with truncation at 50ms per tick") {
                0.milliseconds.ticks shouldBe 0L
                49.milliseconds.ticks shouldBe 0L
                50.milliseconds.ticks shouldBe 1L
                99.milliseconds.ticks shouldBe 1L
                100.milliseconds.ticks shouldBe 2L
            }

            it("converts common durations correctly") {
                1.seconds.ticks shouldBe 20L
                2.seconds.ticks shouldBe 40L
                2500.milliseconds.ticks shouldBe 50L
                1.minutes.ticks shouldBe 1200L
            }
        }
    })
