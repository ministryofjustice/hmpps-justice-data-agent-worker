package uk.gov.justice.digital.hmpps.justicedataagentworker.validator

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.networknt.schema.JsonSchema
import com.networknt.schema.JsonSchemaFactory
import com.networknt.schema.SpecVersion
import com.networknt.schema.ValidationMessage
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.justicedataagentworker.exception.JdaValidationException

@Component
class JsonSchemaValidator {
  companion object {
    private val logger = LoggerFactory.getLogger(this::class.java)
  }
  fun validateJson(jsonSchema: String, json: String) {
    val factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V4)
    val jsonSchema: JsonSchema = factory.getSchema(jsonSchema)
    val jsonNode: JsonNode? = ObjectMapper().readTree(json)
    val errors: MutableSet<ValidationMessage> = jsonSchema.validate(jsonNode)
    if (errors.isNotEmpty()) {
      logger.error("Error validating json schema")
      throw JdaValidationException("Schema validation failed:\n${errors.toList().joinToString("\n")}")
    }
  }
}
