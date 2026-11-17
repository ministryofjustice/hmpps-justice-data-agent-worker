package uk.gov.justice.digital.hmpps.justicedataagentworker.service.event

import io.awspring.cloud.sqs.operations.SqsTemplate
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import tools.jackson.databind.ObjectMapper
import uk.gov.justice.digital.hmpps.justicedataagentworker.dto.request.JdaRequest
import uk.gov.justice.digital.hmpps.justicedataagentworker.dto.response.JdaResponse
import uk.gov.justice.digital.hmpps.justicedataagentworker.model.AzureCustomEvents
import uk.gov.justice.digital.hmpps.justicedataagentworker.service.TelemetryService
import uk.gov.justice.hmpps.sqs.HmppsQueueService
import java.time.LocalDateTime
import java.time.ZoneOffset

@Component
class JdaMessagePublisherImpl(
  private val telemetryService: TelemetryService,
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
        telemetryService.sendTelemetryData(
          eventName = AzureCustomEvents.MESSAGE_SENT_TO_QUEUE.toString(),
          queueName = requestQueueName,
          requestId = jdaRequest.requestId!!,
          correlationId = jdaRequest.correlationId,
          promptKey = jdaRequest.prompt.key,
          promptVersion = jdaRequest.prompt.version?.toInt(),
          dateTime = LocalDateTime.now(ZoneOffset.UTC),
          inputItemNum = null,
          outputItemNum = null,
        )
      } catch (e: Exception) {
      logger.error("Exception occurred when sending message to queue: $requestQueueName with correlation id: ${jdaRequest.correlationId},  exception: ${e.message}")
      sqsTemplate.send { to -> to.queue(requestDlqName).payload(jdaRequest) }
      logger.info("Jda request message with correlation id: ${jdaRequest.correlationId} sent to dlq name: $requestDlqName")
      telemetryService.sendTelemetryData(
        eventName = AzureCustomEvents.MESSAGE_SENT_TO_QUEUE.toString(),
        queueName = requestDlqName,
        requestId = jdaRequest.requestId!!,
        correlationId = jdaRequest.correlationId,
        promptKey = jdaRequest.prompt.key,
        promptVersion = jdaRequest.prompt.version?.toInt(),
        dateTime = LocalDateTime.now(ZoneOffset.UTC),
        inputItemNum = null,
        outputItemNum = null,
      )
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
      telemetryService.sendTelemetryData(
        eventName = AzureCustomEvents.MESSAGE_SENT_TO_QUEUE.toString(),
        queueName = responseQueueName,
        requestId = jdaResponse.requestId,
        correlationId = jdaResponse.correlationId,
        promptKey = jdaResponse.prompt.key,
        promptVersion = jdaResponse.prompt.version?.toInt(),
        dateTime = LocalDateTime.now(ZoneOffset.UTC),
        inputItemNum = null,
        outputItemNum = null,
      )
    } catch (e: Exception) {
      logger.error("Exception occurred when sending message to queue: $responseQueueName with correlation id: ${jdaResponse.correlationId},  exception: ${e.message}")
      sqsTemplate.send { to -> to.queue(responseDlqName).payload(jdaResponse) }
      logger.info("Jda response message with correlation id: ${jdaResponse.correlationId} sent to dlq: $responseDlqName")
      telemetryService.sendTelemetryData(
        eventName = AzureCustomEvents.MESSAGE_SENT_TO_QUEUE.toString(),
        queueName = responseDlqName,
        requestId = jdaResponse.requestId,
        correlationId = jdaResponse.correlationId,
        promptKey = jdaResponse.prompt.key,
        promptVersion = jdaResponse.prompt.version?.toInt(),
        dateTime = LocalDateTime.now(ZoneOffset.UTC),
        inputItemNum = null,
        outputItemNum = null,
      )
    }
  }
}
