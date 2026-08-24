package uk.gov.justice.digital.hmpps.justicedataagentworker.service

import com.fasterxml.uuid.Generators
import com.openai.models.chat.completions.ChatCompletion
import io.swagger.v3.core.util.Json
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import org.springframework.ai.chat.client.ChatClientResponse
import org.springframework.ai.chat.messages.SystemMessage
import org.springframework.ai.chat.messages.UserMessage
import org.springframework.ai.chat.prompt.Prompt
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest
import tools.jackson.databind.ObjectMapper
import uk.gov.justice.digital.hmpps.justicedataagentworker.dto.request.JdaRequest
import uk.gov.justice.digital.hmpps.justicedataagentworker.dto.response.JdaResponse
import uk.gov.justice.digital.hmpps.justicedataagentworker.dto.response.MetaData
import uk.gov.justice.digital.hmpps.justicedataagentworker.dto.response.RequestType
import uk.gov.justice.digital.hmpps.justicedataagentworker.exception.LiteLlmException
import uk.gov.justice.digital.hmpps.justicedataagentworker.exception.NotFoundException
import uk.gov.justice.digital.hmpps.justicedataagentworker.exception.SqsQueueException
import uk.gov.justice.digital.hmpps.justicedataagentworker.exception.ValidationException
import uk.gov.justice.digital.hmpps.justicedataagentworker.model.RequestHistory
import uk.gov.justice.digital.hmpps.justicedataagentworker.model.Status
import uk.gov.justice.digital.hmpps.justicedataagentworker.service.event.JdaMessagePublisher
import uk.gov.justice.digital.hmpps.justicedataagentworker.service.integration.LiteLlmService
import uk.gov.justice.digital.hmpps.justicedataagentworker.validator.JsonSchemaValidator
import uk.gov.justice.hmpps.sqs.HmppsQueueService
import java.time.LocalDateTime
import java.time.ZoneOffset

@Service
class JdaWorkerServiceImpl(
  private val jsonSchemaValidator: JsonSchemaValidator,
  private val liteLlmService: LiteLlmService,
  private val promptService: PromptService,
  private val requestHistoryService: RequestHistoryService,
  private val objectMapper: ObjectMapper,
  private val jdaMessagePublisher: JdaMessagePublisher,
  @param:Value("\${hmpps.sqs.queues.jdarequestqueues.queuename}") private val requestQueueName: String,
  @param:Value("\${hmpps.sqs.queues.jdarequestqueues.dlqName}") private val requestDlqName: String,
) : JdaWorkerService {

  @Autowired
  private lateinit var hmppsQueueService: HmppsQueueService

  companion object {
    private val logger = LoggerFactory.getLogger(this::class.java)
  }

  override suspend fun analyzeData(
    prompt: Prompt,
    model: String,
    jsonSchema: String?,
    useWebClient: Boolean,
  ): Any {
    var response = liteLlmService.connect(prompt, model, useWebClient)
    response = convertLlmResponseToApiResponse(response, null)
    if (!jsonSchema.isNullOrBlank()) {
      logger.info("Validating data with json schema: {}")
      jsonSchemaValidator.validateJson(jsonSchema!!, response as String)
    }
    return response
  }

  override suspend fun handleSynchronousRequest(jdaRequest: JdaRequest): JdaResponse {
    val promptVersionResponse = promptService.getPromptsByKeyAndVersion(jdaRequest.prompt.key, jdaRequest.prompt.version)
    val time = LocalDateTime.now(ZoneOffset.UTC)
    var llmResponse: Any? = null
    val requestHistory = RequestHistory(
      Generators.timeBasedEpochGenerator().generate(),
      true,
      jdaRequest.correlationId,
      promptVersionResponse.id,
      null,
      time,
      null,
      Status.PROCESSING,
      null,
      null,
    )
    var inputJson = jdaRequest.requestData
    inputJson = Json.pretty(inputJson)
    validateJsonDataWithJsonSchema(objectMapper.writeValueAsString(promptVersionResponse.promptVersion.requestContract), inputJson, requestHistory)
    coroutineScope {
      launch {
        requestHistoryService.saveRequestHistory(requestHistory)
      }
      launch {
        llmResponse = sendRequestToLlm(
          convertMessageToPrompt(
            promptVersionResponse.promptVersion.promptTemplate,
            inputJson,
          ),
          promptVersionResponse.promptVersion.llmModel,
          false,
          requestHistory,
        )
      }
    }
    val response = convertLlmResponseToApiResponse(llmResponse!!, requestHistory)
    if ((promptVersionResponse.promptVersion.responseContract?.isNull) == false) {
      validateJsonDataWithJsonSchema(objectMapper.writeValueAsString(promptVersionResponse.promptVersion.responseContract), response as String, requestHistory)
    }
    requestHistory.new = false
    requestHistory.status = Status.SUCCEEDED
    requestHistory.completedAt = LocalDateTime.now(ZoneOffset.UTC)
    requestHistoryService.saveRequestHistory(requestHistory)
    return JdaResponse(
      requestHistory.id,
      jdaRequest.correlationId,
      jdaRequest.prompt,
      objectMapper.readTree(response as String),
      MetaData(
        RequestType.SYNC,
        requestHistory.receivedAt,
        requestHistory.queuedAt,
        requestHistory.receivedAt,
        requestHistory.receivedAt,
        requestHistory.completedAt,
      ),
    )
  }

  override suspend fun handleAsynchronousRequest(jdaRequest: JdaRequest) {
    logger.info("Async request received for correlation id: ${jdaRequest.correlationId} to sent to Llm")
    val promptVersionResponse = promptService.getPromptsByKeyAndVersion(jdaRequest.prompt.key, jdaRequest.prompt.version)
    val time = LocalDateTime.now(ZoneOffset.UTC)
    var llmResponse: Any? = null
    val requestHistory = RequestHistory(
      Generators.timeBasedEpochGenerator().generate(),
      true,
      jdaRequest.correlationId,
      promptVersionResponse.id,
      null,
      time,
      null,
      Status.PROCESSING,
      null,
      null,
    )
    var inputJson = jdaRequest.requestData
    inputJson = Json.pretty(inputJson)
    validateJsonDataWithJsonSchema(objectMapper.writeValueAsString(promptVersionResponse.promptVersion.requestContract), inputJson, requestHistory)
    coroutineScope {
      launch {
        requestHistoryService.saveRequestHistory(requestHistory)
      }
      launch {
        llmResponse = sendRequestToLlm(
          convertMessageToPrompt(
            promptVersionResponse.promptVersion.promptTemplate,
            inputJson,
          ),
          promptVersionResponse.promptVersion.llmModel,
          false,
          requestHistory,
        )
      }
    }
    val response = convertLlmResponseToApiResponse(llmResponse!!, requestHistory)
    if (promptVersionResponse.promptVersion.responseContract?.isNull == false) {
      validateJsonDataWithJsonSchema(objectMapper.writeValueAsString(promptVersionResponse.promptVersion.responseContract), response as String, requestHistory)
    }
    requestHistory.new = false
    requestHistory.status = Status.SUCCEEDED
    requestHistory.completedAt = LocalDateTime.now(ZoneOffset.UTC)
    requestHistoryService.saveRequestHistory(requestHistory)
    val jdaResponse = JdaResponse(
      requestHistory.id,
      jdaRequest.correlationId,
      jdaRequest.prompt,
      objectMapper.readTree(response as String),
      MetaData(
        RequestType.ASYNC,
        requestHistory.receivedAt,
        requestHistory.queuedAt,
        requestHistory.receivedAt,
        requestHistory.receivedAt,
        requestHistory.completedAt,
      ),
    )
    jdaMessagePublisher.publishJdaResponse(jdaResponse)
  }

  override suspend fun submitAsynchronousRequest(jdaRequest: JdaRequest) {
    logger.info("Send async jda request to the jda request queue")
    jdaMessagePublisher.publishJdaRequest(jdaRequest)
  }

  override suspend fun dequeueResponse(): JdaResponse {
    try {
      logger.info("Dequeue jda response queue: $requestQueueName")
      val responseQueue = hmppsQueueService
        .findByQueueId("jdaresponsequeues")
      val sqsClient = responseQueue?.sqsClient
      val queueUrl = responseQueue?.queueUrl
      val messages = sqsClient?.receiveMessage(
        ReceiveMessageRequest.builder()
          .maxNumberOfMessages(1)
          .queueUrl(queueUrl)
          .build(),
      )?.join()
      if (messages?.hasMessages() == true) {
        val jdaResponse = objectMapper.readValue(messages.messages()[0]?.body(), JdaResponse::class.java)
        logger.info("Deleting message from the jda response queue: $requestQueueName with correlation id: ${jdaResponse.correlationId}")
        sqsClient.deleteMessage(
          DeleteMessageRequest.builder()
            .queueUrl(queueUrl)
            .receiptHandle(messages.messages()[0]?.receiptHandle())
            .build(),
        )
        logger.info("returning dequeued jda response with correlation id: ${jdaResponse.correlationId}")
        return jdaResponse
      }
      throw NotFoundException("Queue is empty, no message in queue.")
    } catch (e: Exception) {
      logger.error("Error during dequeue response : ${e.message}")
      if (e is NotFoundException) {
        throw NotFoundException("Queue is empty, no message in queue to dequeue.") // http response code for this will be 404
      }
      val message = "Unexpected Exception during dequeue response queue: ${e.message}" // http response code for this will be 500
      throw SqsQueueException(message)
    }
  }

  private suspend fun sendRequestToLlm(prompt: Prompt, model: String, useWebClient: Boolean, requestHistory: RequestHistory): Any {
    try {
      val llmResponse = liteLlmService.connect(
        prompt,
        model,
        useWebClient,
      )
      return llmResponse
    } catch (e: Exception) {
      logger.error("Error occurred while connecting to llm request: ${e.message}")
      requestHistory.new = false
      requestHistory.status = Status.FAILED
      requestHistory.errorAt = LocalDateTime.now(ZoneOffset.UTC)
      requestHistory.errorMessage = e.message
      requestHistoryService.saveRequestHistory(requestHistory)
      throw LiteLlmException("Error occurred connecting to LLM: ${e.message}")
    }
  }

  private fun convertMessageToPrompt(systemMessage: String, userMessage: String): Prompt {
    val list = mutableListOf<org.springframework.ai.chat.messages.Message>()
    list.add(SystemMessage(systemMessage))
    list.add(UserMessage(userMessage))
    return Prompt(list)
  }

  private suspend fun validateJsonDataWithJsonSchema(jsonSchema: String, data: String, requestHistory: RequestHistory) {
    try {
      jsonSchemaValidator.validateJson(jsonSchema, data)
    } catch (e: Exception) {
      logger.error("Error occurred while schema validation: ${e.message}")
      requestHistory.new = false
      requestHistory.status = Status.REJECTED
      requestHistory.errorAt = LocalDateTime.now(ZoneOffset.UTC)
      requestHistory.errorMessage = e.message
      requestHistoryService.saveRequestHistory(requestHistory)
      throw ValidationException("Error occurred validating with json schema: ${e.message}")
    }
  }

  private suspend fun convertLlmResponseToApiResponse(llmResponse: Any, requestHistory: RequestHistory?): Any {
    try {
      var response = llmResponse
      if (response is ChatCompletion) {
        val message = response.choices().get(0) as Map<String, Any>
        val content = message["message"] as Map<String, Any>
        response = content["content"] as String
        response = response.replace("```json", "")
        response = response.replace("```", "")
        response = response.replace("\n", "")
      }

      if (response is ChatClientResponse) {
        response = response.chatResponse!!.result!!.output.text!!
        response = response.replace("```json", "")
        response = response.replace("```", "")
        response = response.replace("\n", "")
      }
      return response
    } catch (e: Exception) {
      logger.error("Error occurred while processing llm response: ${e.message}")
      requestHistory?.new = false
      requestHistory?.status = Status.FAILED
      requestHistory?.errorAt = LocalDateTime.now(ZoneOffset.UTC)
      requestHistory?.errorMessage = e.message
      if (requestHistory != null) requestHistoryService.saveRequestHistory(requestHistory)
      throw LiteLlmException("Error occurred while processing llm response: ${e.message}")
    }
  }
}
