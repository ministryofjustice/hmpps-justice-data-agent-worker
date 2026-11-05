package uk.gov.justice.digital.hmpps.justicedataagentworker.validator

import io.swagger.v3.core.util.Json
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import tools.jackson.databind.JsonNode
import tools.jackson.databind.ObjectMapper

@Component
class JsonPointerProcessor(private val objectMapper: ObjectMapper) {

  companion object {
    private val logger = LoggerFactory.getLogger(this::class.java)
  }

  suspend fun getIdentifierFromJsonPath(data: Any, jsonPointer: String): Set<String> {
    var jsonNode: JsonNode?
    if (data is JsonNode) {
      jsonNode = data
    } else if (data is String) {
      jsonNode = objectMapper.readTree(data)
    } else {
      val jsonData = Json.pretty(data)
      jsonNode = objectMapper.readTree(jsonData)
    }
    val identifiers = mutableSetOf<String>()
    if (jsonNode.isArray) {
      val arrayNode = jsonNode as tools.jackson.databind.node.ArrayNode
      arrayNode.forEach { x ->
        val elementNode = x.at(jsonPointer)
        if (!elementNode.isMissingNode) {
          logger.info("Identifier node found using json pointer $jsonPointer")
          identifiers.add(elementNode.asString())
        } else {
          logger.error("Identifier node not found using json pointer $jsonPointer")
        }
      }
    } else {
      val elementNode = jsonNode.at(jsonPointer)
      if (!elementNode.isMissingNode) {
        logger.info("Identifier node found using json pointer $jsonPointer")
        identifiers.add(elementNode.asString())
      } else {
        logger.error("Identifier node not found using json pointer $jsonPointer")
      }
    }
    return identifiers
  }
}
