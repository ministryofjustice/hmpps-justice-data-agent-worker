package uk.gov.justice.digital.hmpps.justicedataagentworker.service.event

import io.awspring.cloud.sqs.operations.SqsTemplate
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import tools.jackson.databind.ObjectMapper
import uk.gov.justice.digital.hmpps.justicedataagentworker.dto.response.JdaResponse
import uk.gov.justice.hmpps.sqs.HmppsQueueService

@Component
class JdaMessagePublisherImpl(
  @param:Value("\${hmpps.sqs.queues.jdaresponsequeues.queuename}") private val responseQueueName: String,
) : JdaMessagePublisher {
  @Autowired
  private lateinit var hmppsQueueService: HmppsQueueService

  @Autowired
  private lateinit var mapper: ObjectMapper
  companion object {
    val logger = LoggerFactory.getLogger(JdaMessagePublisherImpl::class.java)
  }

  override fun publishJdaResponse(jdaResponse: JdaResponse) {
    logger.info("Publishing JDA request for correlation id: ${jdaResponse.correlationId}")
    val sqsTemplate =
      SqsTemplate
        .newTemplate(
          hmppsQueueService
            .findByQueueId("jdaresponsequeues")!!.sqsClient,
        )
    logger.info("Sending jda response message to response queue: $responseQueueName")
    sqsTemplate.send { to -> to.queue(responseQueueName).payload(jdaResponse) }
  }
}
