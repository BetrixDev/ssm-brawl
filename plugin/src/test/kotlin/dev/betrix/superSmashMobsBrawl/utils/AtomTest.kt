package dev.betrix.superSmashMobsBrawl.utils

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class AtomTest :
    DescribeSpec({
        describe("Atom initialization") {
            it("stores the initial value") {
                val atom = Atom(10)
                atom.value shouldBe 10
            }

            it("works with different types") {
                val intAtom = Atom(42)
                val stringAtom = Atom("hello")
                val boolAtom = Atom(true)

                intAtom.value shouldBe 42
                stringAtom.value shouldBe "hello"
                boolAtom.value shouldBe true
            }

            it("works with nullable types") {
                val nullableAtom = Atom<String?>(null)
                nullableAtom.value shouldBe null
            }
        }

        describe("Atom value updates") {
            it("updates the value when set") {
                val atom = Atom(10)
                atom.value = 20
                atom.value shouldBe 20
            }

            it("allows multiple updates") {
                val atom = Atom(1)
                atom.value = 2
                atom.value = 3
                atom.value = 4
                atom.value shouldBe 4
            }

            it("updates nullable values") {
                val atom = Atom<String?>("initial")
                atom.value = null
                atom.value shouldBe null
                atom.value = "updated"
                atom.value shouldBe "updated"
            }
        }

        describe("Atom subscriptions") {
            it("notifies subscriber when value changes") {
                val atom = Atom(10)
                var notifiedValue: Int? = null

                atom.subscribe { notifiedValue = it }
                atom.value = 20

                notifiedValue shouldBe 20
            }

            it("notifies subscriber multiple times") {
                val atom = Atom(0)
                val notifiedValues = mutableListOf<Int>()

                atom.subscribe { notifiedValues.add(it) }
                atom.value = 1
                atom.value = 2
                atom.value = 3

                notifiedValues shouldBe listOf(1, 2, 3)
            }

            it("does not notify when value is set to the same value") {
                val atom = Atom(10)
                var notificationCount = 0

                atom.subscribe { notificationCount++ }
                atom.value = 10
                atom.value = 10

                notificationCount shouldBe 0
            }

            it("notifies multiple subscribers") {
                val atom = Atom(10)
                var firstValue: Int? = null
                var secondValue: Int? = null

                atom.subscribe { firstValue = it }
                atom.subscribe { secondValue = it }
                atom.value = 20

                firstValue shouldBe 20
                secondValue shouldBe 20
            }

            it("does not notify after unsubscribing") {
                val atom = Atom(10)
                var notifiedValue: Int? = null

                val listener: (Int) -> Unit = { notifiedValue = it }
                atom.subscribe(listener)
                atom.value = 20
                notifiedValue shouldBe 20

                atom.unsubscribe(listener)
                atom.value = 30
                notifiedValue shouldBe 20
            }

            it("returns an unsubscribe function") {
                val atom = Atom(10)
                var notifiedValue: Int? = null

                val unsubscribe = atom.subscribe { notifiedValue = it }
                atom.value = 20
                notifiedValue shouldBe 20

                unsubscribe()
                atom.value = 30
                notifiedValue shouldBe 20
            }

            it("removes only the specified subscriber") {
                val atom = Atom(10)
                var firstValue: Int? = null
                var secondValue: Int? = null

                val firstListener: (Int) -> Unit = { firstValue = it }
                val secondListener: (Int) -> Unit = { secondValue = it }

                atom.subscribe(firstListener)
                atom.subscribe(secondListener)

                atom.value = 20
                firstValue shouldBe 20
                secondValue shouldBe 20

                atom.unsubscribe(firstListener)
                atom.value = 30
                firstValue shouldBe 20
                secondValue shouldBe 30
            }

            it("removes all subscribers with unsubscribeAll") {
                val atom = Atom(10)
                var firstValue: Int? = null
                var secondValue: Int? = null

                atom.subscribe { firstValue = it }
                atom.subscribe { secondValue = it }

                atom.value = 20
                firstValue shouldBe 20
                secondValue shouldBe 20

                atom.unsubscribeAll()
                atom.value = 30
                firstValue shouldBe 20
                secondValue shouldBe 20
            }

            it("handles unsubscribing a non-existent listener") {
                val atom = Atom(10)
                val listener: (Int) -> Unit = {}

                atom.unsubscribe(listener)
                atom.value = 20

                atom.value shouldBe 20
            }

            it("handles calling unsubscribeAll when no subscribers exist") {
                val atom = Atom(10)

                atom.unsubscribeAll()
                atom.value = 20

                atom.value shouldBe 20
            }
        }

        describe("Atom edge cases") {
            it("handles complex object types") {
                data class TestData(val id: Int, val name: String)

                val atom = Atom(TestData(1, "first"))
                var notifiedValue: TestData? = null

                atom.subscribe { notifiedValue = it }
                val expectedValue = TestData(2, "second")
                atom.value = expectedValue

                notifiedValue shouldBe expectedValue
            }

            it("does not notify for equal data class instances") {
                data class TestData(val id: Int)

                val atom = Atom(TestData(1))
                var notificationCount = 0

                atom.subscribe { notificationCount++ }
                atom.value = TestData(1)

                notificationCount shouldBe 0
            }

            it("notifies for different data class instances") {
                data class TestData(val id: Int)

                val atom = Atom(TestData(1))
                var notificationCount = 0

                atom.subscribe { notificationCount++ }
                atom.value = TestData(2)

                notificationCount shouldBe 1
            }

            it("handles resubscribing the same listener") {
                val atom = Atom(10)
                var notificationCount = 0

                val listener: (Int) -> Unit = { notificationCount++ }
                atom.subscribe(listener)
                atom.subscribe(listener)

                atom.value = 20

                notificationCount shouldBe 2
            }

            it("handles lists and collections") {
                val atom = Atom(listOf(1, 2, 3))
                var notifiedValue: List<Int>? = null

                atom.subscribe { notifiedValue = it }
                atom.value = listOf(4, 5, 6)

                notifiedValue shouldBe listOf(4, 5, 6)
            }
        }
    })

