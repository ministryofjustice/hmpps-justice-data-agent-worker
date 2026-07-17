package uk.gov.justice.digital.hmpps.justicedataagentworker.validator

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import uk.gov.justice.digital.hmpps.justicedataagentworker.exception.ValidationException

@SpringBootTest(classes = [JsonSchemaValidator::class])
@EnableAutoConfiguration
@ExtendWith(SpringExtension::class)
@ActiveProfiles("test")
class JsonSchemaValidatorTest {

  @Autowired private lateinit var validator: JsonSchemaValidator

  @Test
  fun `validate json value with json schema and no exception thrown`() {
    val jsonSchema = "\n" +
      "\n" +
      "\n" +
      "{\n" +
      "  \"\$schema\": \"https://json-schema.org/draft/2020-12/schema\",\n" +
      "  \"title\": \"BehaviourRiskAssessments\",\n" +
      "  \"type\": \"array\",\n" +
      "  \"items\": {\n" +
      "    \"type\": \"object\",\n" +
      "    \"properties\": {\n" +
      "      \"item_id\": { \"type\": \"integer\" },\n" +
      "      \"usual_behaviour_presentation\": {\n" +
      "        \"type\": \"integer\",\n" +
      "        \"minimum\": 0,\n" +
      "        \"maximum\": 5\n" +
      "      },\n" +
      "      \"risks_and_triggers\": { \"type\": \"integer\", \"minimum\": 0, \"maximum\": 5 },\n" +
      "      \"protective_factors\": { \"type\": \"integer\", \"minimum\": 0, \"maximum\": 5 },\n" +
      "      \"comment\": { \"type\": \"string\" },\n" +
      "      \"confidence_level\": {\n" +
      "        \"type\": \"string\",\n" +
      "        \"enum\": [\"low\", \"medium\", \"high\"]\n" +
      "      },\n" +
      "      \"justifying_spans\": {\n" +
      "        \"type\": \"array\",\n" +
      "        \"items\": {\n" +
      "          \"type\": \"object\",\n" +
      "          \"properties\": {\n" +
      "            \"text\": { \"type\": \"string\" },\n" +
      "            \"justifies\": {\n" +
      "              \"type\": \"string\",\n" +
      "              \"enum\": [\n" +
      "                \"usual_behaviour_presentation\",\n" +
      "                \"risks_and_triggers\",\n" +
      "                \"protective_factors\"\n" +
      "              ]\n" +
      "            }\n" +
      "          },\n" +
      "          \"required\": [\"text\", \"justifies\"],\n" +
      "          \"additionalProperties\": false\n" +
      "        }\n" +
      "      }\n" +
      "    },\n" +
      "    \"required\": [\n" +
      "      \"item_id\",\n" +
      "      \"usual_behaviour_presentation\",\n" +
      "      \"risks_and_triggers\",\n" +
      "      \"comment\",\n" +
      "      \"confidence_level\",\n" +
      "      \"justifying_spans\"\n" +
      "    ],\n" +
      "    \"additionalProperties\": false\n" +
      "  }\n" +
      "}"

    val json = "[\n" +
      "    {\n" +
      "        \"item_id\": 1,\n" +
      "        \"usual_behaviour_presentation\": 3,\n" +
      "        \"risks_and_triggers\": 2,\n" +
      "        \"protective_factors\": 3,\n" +
      "        \"comment\": \"Low spirits on arrival, history of SASH, gang affiliations, seeks family support and prison transfer, understands debt avoidance.\",\n" +
      "        \"confidence_level\": \"high\",\n" +
      "        \"justifying_spans\": [\n" +
      "            {\n" +
      "                \"text\": \"appears in quite low in spirits\",\n" +
      "                \"justifies\": \"usual_behaviour_presentation\"\n" +
      "            },\n" +
      "            {\n" +
      "                \"text\": \"He stated he has a history of SASH but no current thoughts of this\",\n" +
      "                \"justifies\": \"risks_and_triggers\"\n" +
      "            },\n" +
      "            {\n" +
      "                \"text\": \"Test User states that he was gang affiliations\",\n" +
      "                \"justifies\": \"risks_and_triggers\"\n" +
      "            },\n" +
      "            {\n" +
      "                \"text\": \"He also wants to move to another prison nearer to where he live so he can have support from his family\",\n" +
      "                \"justifies\": \"protective_factors\"\n" +
      "            },\n" +
      "            {\n" +
      "                \"text\": \"He is aware of avenues of support available to him while he is here and stated he would ask staff for any help or support if any was needed\",\n" +
      "                \"justifies\": \"protective_factors\"\n" +
      "            },\n" +
      "            {\n" +
      "                \"text\": \"Test User thanked me for coming to speak to him\",\n" +
      "                \"justifies\": \"usual_behaviour_presentation\"\n" +
      "            }\n" +
      "        ]\n" +
      "    }\n" +
      "]"
    Assertions.assertDoesNotThrow { validator.validateJson(jsonSchema, json) }
  }

  @Test
  fun `validate json value with json schema and exception thrown`() {
    val jsonSchema = "\n" +
      "\n" +
      "\n" +
      "{\n" +
      "  \"\$schema\": \"https://json-schema.org/draft/2020-12/schema\",\n" +
      "  \"title\": \"BehaviourRiskAssessments\",\n" +
      "  \"type\": \"array\",\n" +
      "  \"items\": {\n" +
      "    \"type\": \"object\",\n" +
      "    \"properties\": {\n" +
      "      \"item_id_x\": { \"type\": \"integer\" },\n" +
      "      \"usual_behaviour_presentation\": {\n" +
      "        \"type\": \"integer\",\n" +
      "        \"minimum\": 0,\n" +
      "        \"maximum\": 5\n" +
      "      },\n" +
      "      \"risks_and_triggers\": { \"type\": \"integer\", \"minimum\": 0, \"maximum\": 5 },\n" +
      "      \"protective_factors\": { \"type\": \"integer\", \"minimum\": 0, \"maximum\": 5 },\n" +
      "      \"comment\": { \"type\": \"string\" },\n" +
      "      \"confidence_level\": {\n" +
      "        \"type\": \"string\",\n" +
      "        \"enum\": [\"low\", \"medium\", \"high\"]\n" +
      "      },\n" +
      "      \"justifying_spans\": {\n" +
      "        \"type\": \"array\",\n" +
      "        \"items\": {\n" +
      "          \"type\": \"object\",\n" +
      "          \"properties\": {\n" +
      "            \"text\": { \"type\": \"string\" },\n" +
      "            \"justifies\": {\n" +
      "              \"type\": \"string\",\n" +
      "              \"enum\": [\n" +
      "                \"usual_behaviour_presentation\",\n" +
      "                \"risks_and_triggers\",\n" +
      "                \"protective_factors\"\n" +
      "              ]\n" +
      "            }\n" +
      "          },\n" +
      "          \"required\": [\"text\", \"justifies\"],\n" +
      "          \"additionalProperties\": false\n" +
      "        }\n" +
      "      }\n" +
      "    },\n" +
      "    \"required\": [\n" +
      "      \"item_id\",\n" +
      "      \"usual_behaviour_presentation\",\n" +
      "      \"risks_and_triggers\",\n" +
      "      \"comment\",\n" +
      "      \"confidence_level\",\n" +
      "      \"justifying_spans\"\n" +
      "    ],\n" +
      "    \"additionalProperties\": false\n" +
      "  }\n" +
      "}"

    val json = "[\n" +
      "    {\n" +
      "        \"item_id\": 1,\n" +
      "        \"usual_behaviour_presentation\": 3,\n" +
      "        \"risks_and_triggers\": 2,\n" +
      "        \"protective_factors\": 3,\n" +
      "        \"comment\": \"Low spirits on arrival, history of SASH, gang affiliations, seeks family support and prison transfer, understands debt avoidance.\",\n" +
      "        \"confidence_level\": \"high\",\n" +
      "        \"justifying_spans\": [\n" +
      "            {\n" +
      "                \"text\": \"appears in quite low in spirits\",\n" +
      "                \"justifies\": \"usual_behaviour_presentation\"\n" +
      "            },\n" +
      "            {\n" +
      "                \"text\": \"He stated he has a history of SASH but no current thoughts of this\",\n" +
      "                \"justifies\": \"risks_and_triggers\"\n" +
      "            },\n" +
      "            {\n" +
      "                \"text\": \"Test User states that he was gang affiliations\",\n" +
      "                \"justifies\": \"risks_and_triggers\"\n" +
      "            },\n" +
      "            {\n" +
      "                \"text\": \"He also wants to move to another prison nearer to where he live so he can have support from his family\",\n" +
      "                \"justifies\": \"protective_factors\"\n" +
      "            },\n" +
      "            {\n" +
      "                \"text\": \"He is aware of avenues of support available to him while he is here and stated he would ask staff for any help or support if any was needed\",\n" +
      "                \"justifies\": \"protective_factors\"\n" +
      "            },\n" +
      "            {\n" +
      "                \"text\": \"Test User thanked me for coming to speak to him\",\n" +
      "                \"justifies\": \"usual_behaviour_presentation\"\n" +
      "            }\n" +
      "        ]\n" +
      "    }\n" +
      "]"
    Assertions.assertThrows(ValidationException::class.java) { validator.validateJson(jsonSchema, json) }
  }
}
