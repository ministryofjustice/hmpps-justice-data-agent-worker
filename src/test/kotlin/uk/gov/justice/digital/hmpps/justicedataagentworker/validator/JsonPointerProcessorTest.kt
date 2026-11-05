package uk.gov.justice.digital.hmpps.justicedataagentworker.validator

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNotNull
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.justicedataagentworker.integration.IntegrationTestBase

class JsonPointerProcessorTest(@Autowired private val jsonPointerProcessor: JsonPointerProcessor) : IntegrationTestBase() {

  @Test
  fun `get values from json pointer`() {
    val data = """
    [
    {
    "id": "1",
    "name": "x"
    },
    {
    "id": "2",
    "name": "y"
    },
    {
    "id": "3",
    "name": "z"
    }
    ]
    """.trimIndent()
    var identifiers: Set<String>? = null
    runBlocking {
      identifiers = jsonPointerProcessor.getIdentifierFromJsonPath(data, "/id")
    }
    assertNotNull(identifiers)
    assertEquals(setOf("1", "2", "3"), identifiers)
  }

  @Test
  fun `get values from invalid json pointer`() {
    val data = """
    [
    {
    "id": "1",
    "name": "x"
    },
    {
    "id": "2",
    "name": "y"
    },
    {
    "id": "3",
    "name": "z"
    }
    ]
    """.trimIndent()
    var identifiers: Set<String>? = null
    runBlocking {
      identifiers = jsonPointerProcessor.getIdentifierFromJsonPath(data, "/xx")
    }
    assertNotNull(identifiers)
    assertEquals(0, identifiers.size)
  }
}
