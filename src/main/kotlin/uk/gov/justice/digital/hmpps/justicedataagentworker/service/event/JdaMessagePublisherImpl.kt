package uk.gov.justice.digital.hmpps.justicedataagentworker.service.event

import io.awspring.cloud.sqs.operations.SqsTemplate
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import tools.jackson.databind.ObjectMapper
import uk.gov.justice.digital.hmpps.justicedataagentworker.dto.request.JdaRequest
import uk.gov.justice.digital.hmpps.justicedataagentworker.dto.response.JdaResponse
import uk.gov.justice.hmpps.sqs.HmppsQueueService

@Component
class JdaMessagePublisherImpl(
  @param:Value("\${hmpps.sqs.queues.jdarequestqueues.queuename}") private val requestQueueName: String,
  @param:Value("\${hmpps.sqs.queues.jdarequestqueues.dlqName}") private val requestDlqName: String,
  @param:Value("\${hmpps.sqs.queues.jdaresponsequeues.queuename}") private val responseQueueName: String,
  @param:Value("\${hmpps.sqs.queues.jdaresponsequeues.dlqName}") private val responseDlqName: String,
) : JdaMessagePublisher {
  @Autowired
  private lateinit var hmppsQueueService: HmppsQueueService

  @Autowired
  private lateinit var mapper: ObjectMapper
  companion object {
    val logger = LoggerFactory.getLogger(JdaMessagePublisherImpl::class.java)
  }

  override suspend fun publishJdaRequest(jdaRequest: JdaRequest) {
    val awsSqsClient = hmppsQueueService
      .findByQueueId("jdarequestqueues")!!.sqsClient
    val sqsTemplate =
      SqsTemplate
        .newTemplate(
          awsSqsClient,
        )
    try {
      logger.info("Sending jda request message to queue: $requestQueueName with correlation id: ${jdaRequest.correlationId}")
      sqsTemplate.send { to -> to.queue(requestQueueName).payload(jdaRequest) }
      logger.info("Jda request message sent to queue: $requestQueueName with correlation id: ${jdaRequest.correlationId}")
    } catch (e: Exception) {
      logger.error("Exception occurred when sending message to queue: $requestQueueName with correlation id: ${jdaRequest.correlationId},  exception: ${e.message}")
      sqsTemplate.send { to -> to.queue(requestDlqName).payload(jdaRequest) }
      logger.info("Jda request message with correlation id: ${jdaRequest.correlationId} sent to dlq name: $requestDlqName")
    }
  }

  override suspend fun publishJdaResponse(jdaResponse: JdaResponse) {
    val awsSqsClient = hmppsQueueService
      .findByQueueId("jdaresponsequeues")!!.sqsClient
    val sqsTemplate =
      SqsTemplate
        .newTemplate(
          awsSqsClient,
        )
    try {
      logger.info("Sending Jda response message with correlation id: ${jdaResponse.correlationId} to queue: $responseQueueName")
      sqsTemplate.send { to -> to.queue(responseQueueName).payload(jdaResponse) }
      logger.info("Jda response message with correlation id: ${jdaResponse.correlationId} sent to queue: $responseQueueName")
    } catch (e: Exception) {
      logger.error("Exception occurred when sending message to queue: $responseQueueName with correlation id: ${jdaResponse.correlationId},  exception: ${e.message}")
      sqsTemplate.send { to -> to.queue(responseDlqName).payload(jdaResponse) }
      logger.info("Jda response message with correlation id: ${jdaResponse.correlationId} sent to dlq: $responseDlqName")
    }
  }
}
