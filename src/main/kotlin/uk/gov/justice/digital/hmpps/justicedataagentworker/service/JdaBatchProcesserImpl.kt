package uk.gov.justice.digital.hmpps.justicedataagentworker.service

import io.swagger.v3.core.util.Json
import org.springframework.stereotype.Component
import tools.jackson.databind.ObjectMapper
import tools.jackson.databind.node.ArrayNode
import uk.gov.justice.digital.hmpps.justicedataagentworker.dto.request.JdaRequest
import uk.gov.justice.digital.hmpps.justicedataagentworker.exception.JdaValidationException

@Component
class JdaBatchProcesserImpl(
  private val objectMapper: ObjectMapper,
) : JdaBatchProcesser {

  override suspend fun processJdaRequest(jdaRequest: JdaRequest, batchSize: Int?): List<JdaRequest> {
    val jdaRequests = mutableListOf<JdaRequest>()
    if (batchSize == null || batchSize == 0) {
      jdaRequests.add(jdaRequest)
      return jdaRequests
    }
    val data = Json.pretty(jdaRequest.requestData)
    val jsonNode = objectMapper.readTree(data)
    if (jsonNode.isArray) {
      val arrayNode = jsonNode as ArrayNode
      val list = arrayNode.elements().chunked(batchSize)
      list.forEach { x ->
        val req = JdaRequest(
          jdaRequest.correlationId,
          jdaRequest.prompt,
          x,
        )
        jdaRequests.add(req)
      }
    } else {
      throw JdaValidationException("Request data is not array")
    }
    return jdaRequests
  }
}
