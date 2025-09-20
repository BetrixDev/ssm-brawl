package dev.betrix.superSmashMobsBrawl.extensions

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import java.util.logging.Logger

class LoggerExtensionsTest :
    DescribeSpec({
        val logger = Logger.getLogger("TestLogger")

        describe("formatMessage function") {
            it("handles indexed placeholders correctly") {
                val template = "User {0} logged in at {1} and performed action {0}"
                val args = arrayOf("john_doe", "2023-01-01T10:00:00Z", "login")
                val (formatted, properties) = formatMessage(template, args)

                formatted shouldBe
                    "User john_doe logged in at 2023-01-01T10:00:00Z and performed action john_doe"
                properties.size shouldBe 2
                properties["0"] shouldBe kotlinx.serialization.json.JsonPrimitive("john_doe")
                properties["1"] shouldBe
                    kotlinx.serialization.json.JsonPrimitive("2023-01-01T10:00:00Z")
            }

            it("handles named placeholders correctly") {
                val template = "User {username} logged in at {timestamp}"
                val args = arrayOf("john_doe", "2023-01-01T10:00:00Z")
                val (formatted, properties) = formatMessage(template, args)

                formatted shouldBe "User john_doe logged in at 2023-01-01T10:00:00Z"
                properties.size shouldBe 2
                properties["username"] shouldBe kotlinx.serialization.json.JsonPrimitive("john_doe")
                properties["timestamp"] shouldBe
                    kotlinx.serialization.json.JsonPrimitive("2023-01-01T10:00:00Z")
            }

            it("handles mixed indexed and named placeholders") {
                val template = "User {0} with role {role} logged in at {1}"
                val args = arrayOf("admin_user", "2023-01-01T10:00:00Z", "admin")
                val (formatted, properties) = formatMessage(template, args)

                formatted shouldBe
                    "User admin_user with role admin_user logged in at 2023-01-01T10:00:00Z"
                properties.size shouldBe 3
                properties["0"] shouldBe kotlinx.serialization.json.JsonPrimitive("admin_user")
                properties["role"] shouldBe kotlinx.serialization.json.JsonPrimitive("admin_user")
                properties["1"] shouldBe
                    kotlinx.serialization.json.JsonPrimitive("2023-01-01T10:00:00Z")
            }

            it("handles out of bounds indexed placeholders") {
                val template = "User {0} logged in at {5}" // index 5 doesn't exist
                val args = arrayOf("john_doe", "2023-01-01T10:00:00Z")
                val (formatted, properties) = formatMessage(template, args)

                formatted shouldBe "User john_doe logged in at {5}"
                properties.size shouldBe 1
                properties["0"] shouldBe kotlinx.serialization.json.JsonPrimitive("john_doe")
            }

            it("handles insufficient args for named placeholders") {
                val template = "User {username} logged in at {timestamp} from {location}"
                val args = arrayOf("john_doe", "2023-01-01T10:00:00Z")
                val (formatted, properties) = formatMessage(template, args)

                formatted shouldBe "User john_doe logged in at 2023-01-01T10:00:00Z from {location}"
                properties.size shouldBe 2
                properties["username"] shouldBe kotlinx.serialization.json.JsonPrimitive("john_doe")
                properties["timestamp"] shouldBe
                    kotlinx.serialization.json.JsonPrimitive("2023-01-01T10:00:00Z")
            }

            it("handles duplicate indexed placeholders") {
                val template = "Processing {0} as {0} type"
                val args = arrayOf("document")
                val (formatted, properties) = formatMessage(template, args)

                formatted shouldBe "Processing document as document type"
                properties.size shouldBe 1
                properties["0"] shouldBe kotlinx.serialization.json.JsonPrimitive("document")
            }

            it("handles duplicate named placeholders") {
                val template = "Processing {item} as {item} type"
                val args = arrayOf("document", "file")
                val (formatted, properties) = formatMessage(template, args)

                formatted shouldBe "Processing document as file type"
                properties.size shouldBe 1
                properties["item"] shouldBe kotlinx.serialization.json.JsonPrimitive("file")
            }
        }
    })
