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
import tools.jackson.databind.node.ArrayNode
import tools.jackson.databind.node.JsonNodeFactory
import uk.gov.justice.digital.hmpps.justicedataagentworker.dto.request.JdaDequeReceipt
import uk.gov.justice.digital.hmpps.justicedataagentworker.dto.request.JdaRequest
import uk.gov.justice.digital.hmpps.justicedataagentworker.dto.response.Error
import uk.gov.justice.digital.hmpps.justicedataagentworker.dto.response.JdaResponse
import uk.gov.justice.digital.hmpps.justicedataagentworker.dto.response.MetaData
import uk.gov.justice.digital.hmpps.justicedataagentworker.dto.response.RequestType
import uk.gov.justice.digital.hmpps.justicedataagentworker.dto.response.Stage
import uk.gov.justice.digital.hmpps.justicedataagentworker.exception.JdaValidationException
import uk.gov.justice.digital.hmpps.justicedataagentworker.exception.LiteLlmException
import uk.gov.justice.digital.hmpps.justicedataagentworker.exception.NotFoundException
import uk.gov.justice.digital.hmpps.justicedataagentworker.exception.SqsQueueException
import uk.gov.justice.digital.hmpps.justicedataagentworker.model.RequestHistory
import uk.gov.justice.digital.hmpps.justicedataagentworker.model.Status
import uk.gov.justice.digital.hmpps.justicedataagentworker.service.event.JdaMessagePublisher
import uk.gov.justice.digital.hmpps.justicedataagentworker.service.integration.LiteLlmService
import uk.gov.justice.digital.hmpps.justicedataagentworker.validator.JsonSchemaValidator
import uk.gov.justice.hmpps.sqs.HmppsQueueService
import java.time.Duration
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
  private val jdaBatchProcesser: JdaBatchProcesser,
  @param:Value("\${hmpps.sqs.queues.jdarequestqueues.queuename}") private val requestQueueName: String,
  @param:Value("\${hmpps.sqs.queues.jdarequestqueues.dlqName}") private val requestDlqName: String,
  @param:Value("\${hmpps.sqs.message.visibility.timeout}") private val visibilityTimeOut: Int,
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
      logger.info("Validating data with json schema")
      jsonSchemaValidator.validateJson(jsonSchema, response as String)
    }
    return response
  }

  override suspend fun handleSynchronousRequest(jdaRequest: JdaRequest): JdaResponse {
    val promptVersionResponse = if (jdaRequest.prompt.version != null) {
      promptService.getPromptsByKeyAndVersion(jdaRequest.prompt.key, jdaRequest.prompt.version.toInt())
    } else {
      promptService.getPromptByKey(jdaRequest.prompt.key)
    }
    val version = promptVersionResponse.promptVersion.version
    val time = LocalDateTime.now(ZoneOffset.UTC)
    var llmResponse: Any? = null
    val totalInputItems = jdaBatchProcesser.getJsonArraySize(jdaRequest.requestData)
    val requestHistory = RequestHistory(
      id = Generators.timeBasedEpochGenerator().generate(),
      synchronousRequest = true,
      correlationId = jdaRequest.correlationId,
      promptVersionId = promptVersionResponse.id,
      queuedAt = null,
      receivedAt = time,
      completedAt = null,
      status = Status.PROCESSING,
      errorMessage = null,
      errorAt = null,
      promptTokens = null,
      completionTokens = null,
      totalTokens = null,
      totalInputItems = totalInputItems,
      totalOutputItems = null,
    )
    var inputJson = jdaRequest.requestData
    inputJson = Json.pretty(inputJson)
    validateJsonDataWithJsonSchema(objectMapper.writeValueAsString(promptVersionResponse.promptVersion.requestContract), inputJson, requestHistory)
    var error: Error? = null
    var response: LlmResponseData? = null
    var status: uk.gov.justice.digital.hmpps.justicedataagentworker.dto.response.Status? = null
    var historyStatus: Status? = null
    var dataResponse: Any? = null
    coroutineScope {
      launch {
        requestHistoryService.saveRequestHistory(requestHistory)
      }
      launch {
        try {
          llmResponse = sendRequestToLlm(
            convertMessageToPrompt(
              promptVersionResponse.promptVersion.promptTemplate,
              inputJson,
            ),
            promptVersionResponse.promptVersion.llmModel,
            false,
            requestHistory,
          )
          response = convertLlmResponseToApiResponse(llmResponse!!, requestHistory)
          if ((promptVersionResponse.promptVersion.responseContract?.isNull) == false) {
            validateJsonDataWithJsonSchema(objectMapper.writeValueAsString(promptVersionResponse.promptVersion.responseContract), response as String, requestHistory)
          }
        } catch (e: LiteLlmException) {
          error = Error(
            code = "LLM_REFUSED_REQUEST",
            message = e.message!!,
            stage = Stage.GATEWAY,
          )
          logger.error("Exception occurred when getting llm response with correlation id: ${jdaRequest.correlationId},  exception: ${e.message}")
        } catch (e: JdaValidationException) {
          error = Error(
            code = "RESPONSE_CONTRACT_VALIDATION_FAILED",
            message = e.message!!,
            stage = Stage.VALIDATION,
          )
          logger.error("Exception occurred when getting llm response with correlation id: ${jdaRequest.correlationId},  exception: ${e.message}")
        } catch (e: Exception) {
          error = Error(
            code = "LLM_REFUSED_REQUEST",
            message = e.message!!,
            stage = Stage.INTERNAL,
          )
          logger.error("Exception occurred when getting llm response with correlation id: ${jdaRequest.correlationId},  exception: ${e.message}")
        }
      }
    }
    if (error != null) {
      status = uk.gov.justice.digital.hmpps.justicedataagentworker.dto.response.Status.FAILED
      historyStatus = Status.FAILED
      dataResponse = error
    } else {
      status = uk.gov.justice.digital.hmpps.justicedataagentworker.dto.response.Status.SUCCEEDED
      historyStatus = Status.SUCCEEDED
      dataResponse = objectMapper.readTree(response!!.response)
    }
    val totalOutputItems = jdaBatchProcesser.getJsonArraySize(dataResponse as Any)
    requestHistory.new = false
    requestHistory.status = historyStatus
    requestHistory.completedAt = LocalDateTime.now(ZoneOffset.UTC)
    requestHistory.promptTokens = response!!.promptTokens
    requestHistory.completionTokens = response!!.completionTokens
    requestHistory.totalTokens = response!!.totalTokens
    requestHistory.totalOutputItems = totalOutputItems
    requestHistoryService.saveRequestHistory(requestHistory)
    return JdaResponse(
      requestHistory.id,
      jdaRequest.correlationId,
      uk.gov.justice.digital.hmpps.justicedataagentworker.dto.request.Prompt(jdaRequest.prompt.key, version.toLong()),
      status,
      null,
      dataResponse,
      MetaData(
        RequestType.SYNC,
        null,
        requestHistory.receivedAt,
        requestHistory.queuedAt,
        requestHistory.receivedAt,
        requestHistory.receivedAt,
        requestHistory.completedAt,
        Duration.between(requestHistory.receivedAt, requestHistory.completedAt).toMillis(),
      ),
    )
  }

  override suspend fun handleAsynchronousRequest(jdaRequest: JdaRequest) {
    var promptTokens = 0
    var completionTokens = 0
    var totalTokens = 0
    logger.info("Async request received for correlation id: ${jdaRequest.correlationId} to sent to Llm")
    val promptVersionResponse = if (jdaRequest.prompt.version != null) {
      promptService.getPromptsByKeyAndVersion(jdaRequest.prompt.key, jdaRequest.prompt.version.toInt())
    } else {
      promptService.getPromptByKey(jdaRequest.prompt.key)
    }
    val version = promptVersionResponse.promptVersion.version
    val time = LocalDateTime.now(ZoneOffset.UTC)
    var llmResponse: Any?
    val totalInputItems = jdaBatchProcesser.getJsonArraySize(jdaRequest.requestData)
    var totalOutputItems: Int? = null
    var requestHistory: RequestHistory?
    if (jdaRequest.requestId != null) {
      requestHistory = requestHistoryService.getRequestHistoryById(jdaRequest.requestId!!)
      requestHistory.status = Status.PROCESSING
      requestHistory.totalInputItems = totalInputItems
      requestHistory.new = false
    } else {
      requestHistory = RequestHistory(
        id = Generators.timeBasedEpochGenerator().generate(),
        synchronousRequest = false,
        correlationId = jdaRequest.correlationId,
        promptVersionId = promptVersionResponse.id,
        queuedAt = null,
        receivedAt = time,
        completedAt = null,
        status = Status.PROCESSING,
        errorMessage = null,
        errorAt = null,
      )
    }
    var inputJson = jdaRequest.requestData
    inputJson = Json.pretty(inputJson)
    val jdaRequests = jdaBatchProcesser.processJdaRequest(jdaRequest, promptVersionResponse.promptVersion.batchSize)
    var llmResponses = JsonNodeFactory.instance.arrayNode()
    var error: Error? = null
    var response: String? = null
    var status: uk.gov.justice.digital.hmpps.justicedataagentworker.dto.response.Status? = null
    var historyStatus: Status? = null
    var dataResponse: Any? = null
    coroutineScope {
      launch {
        requestHistoryService.saveRequestHistory(requestHistory)
      }
      launch {
        try {
          jdaRequests.forEach { request ->
            llmResponse = sendRequestToLlm(
              convertMessageToPrompt(
                promptVersionResponse.promptVersion.promptTemplate,
                Json.pretty(request.requestData),
              ),
              promptVersionResponse.promptVersion.llmModel,
              false,
              requestHistory,
            )
            val response = convertLlmResponseToApiResponse(llmResponse!!, requestHistory)
            if (response.promptTokens != null) {
              promptTokens += response.promptTokens
            }
            if (response.completionTokens != null) {
              completionTokens += response.completionTokens
            }
            if (response.totalTokens != null) {
              totalTokens += response.totalTokens
            }
            if (promptVersionResponse.promptVersion.responseContract?.isNull == false) {
              validateJsonDataWithJsonSchema(objectMapper.writeValueAsString(promptVersionResponse.promptVersion.responseContract), response as String, requestHistory)
            }
            val jsonNode = objectMapper.readTree(response.response)
            if (jsonNode.isArray) {
              val values = jsonNode as ArrayNode
              llmResponses.addAll(values)
            }
          }
          totalOutputItems = llmResponses.size()
        } catch (e: LiteLlmException) {
          error = Error(
            code = "LLM_REFUSED_REQUEST",
            message = e.message!!,
            stage = Stage.GATEWAY,
          )
          logger.error("Exception occurred when getting llm response with correlation id: ${jdaRequest.correlationId},  exception: ${e.message}")
        } catch (e: JdaValidationException) {
          error = Error(
            code = "RESPONSE_CONTRACT_VALIDATION_FAILED",
            message = e.message!!,
            stage = Stage.VALIDATION,
          )
          logger.error("Exception occurred when getting llm response with correlation id: ${jdaRequest.correlationId},  exception: ${e.message}")
        } catch (e: Exception) {
          error = Error(
            code = "LLM_REFUSED_REQUEST",
            message = e.message!!,
            stage = Stage.INTERNAL,
          )
          logger.error("Exception occurred when getting llm response with correlation id: ${jdaRequest.correlationId},  exception: ${e.message}")
        }
      }
    }
    if (error != null) {
      status = uk.gov.justice.digital.hmpps.justicedataagentworker.dto.response.Status.FAILED
      historyStatus = Status.FAILED
      dataResponse = error
    } else {
      status = uk.gov.justice.digital.hmpps.justicedataagentworker.dto.response.Status.SUCCEEDED
      historyStatus = Status.SUCCEEDED
      dataResponse = llmResponses
    }
    requestHistory.new = false
    requestHistory.status = historyStatus
    requestHistory.completedAt = LocalDateTime.now(ZoneOffset.UTC)
    requestHistory.promptTokens = promptTokens
    requestHistory.completionTokens = completionTokens
    requestHistory.totalTokens = totalTokens
    requestHistory.totalOutputItems = totalOutputItems
    requestHistoryService.saveRequestHistory(requestHistory)
    val jdaResponse = JdaResponse(
      requestHistory.id,
      jdaRequest.correlationId,
      uk.gov.justice.digital.hmpps.justicedataagentworker.dto.request.Prompt(jdaRequest.prompt.key, version.toLong()),
      uk.gov.justice.digital.hmpps.justicedataagentworker.dto.response.Status.SUCCEEDED,
      null,
      dataResponse,
      MetaData(
        RequestType.ASYNC,
        null,
        requestHistory.receivedAt,
        requestHistory.queuedAt,
        requestHistory.receivedAt,
        requestHistory.receivedAt,
        requestHistory.completedAt,
        Duration.between(requestHistory.receivedAt, requestHistory.completedAt).toMillis(),
      ),
    )
    jdaMessagePublisher.publishJdaResponse(jdaResponse)
  }

  override suspend fun submitAsynchronousRequest(jdaRequest: JdaRequest) {
    logger.info("Async jda request received to submit to request queue for correlation id: ${jdaRequest.correlationId} to sent to Llm")
    val promptVersionResponse = if (jdaRequest.prompt.version != null) {
      promptService.getPromptsByKeyAndVersion(jdaRequest.prompt.key, jdaRequest.prompt.version.toInt())
    } else {
      promptService.getPromptByKey(jdaRequest.prompt.key)
    }
    val version = promptVersionResponse.promptVersion.version
    var inputJson = jdaRequest.requestData
    inputJson = Json.pretty(inputJson)
    validateJsonDataWithJsonSchema(objectMapper.writeValueAsString(promptVersionResponse.promptVersion.requestContract), inputJson, null)
    val time = LocalDateTime.now(ZoneOffset.UTC)
    val requestHistory = RequestHistory(
      Generators.timeBasedEpochGenerator().generate(),
      true,
      jdaRequest.correlationId,
      promptVersionResponse.id,
      null,
      time,
      null,
      Status.QUEUED,
      null,
      null,
    )
    requestHistoryService.saveRequestHistory(requestHistory)
    jdaRequest.requestId = requestHistory.id
    logger.info("Send async jda request to the jda request queue")
    jdaMessagePublisher.publishJdaRequest(jdaRequest)
  }

  override suspend fun dequeueResponse(): JdaResponse {
    try {
      logger.info("Dequeue jda response queue")
      val responseQueue = hmppsQueueService
        .findByQueueId("jdaresponsequeues")
      val sqsClient = responseQueue?.sqsClient
      val queueUrl = responseQueue?.queueUrl
      val messages = sqsClient?.receiveMessage(
        ReceiveMessageRequest.builder()
          .visibilityTimeout(visibilityTimeOut)
          .maxNumberOfMessages(1)
          .queueUrl(queueUrl)
          .build(),
      )?.join()
      if (messages?.hasMessages() == true) {
        val jdaResponse = objectMapper.readValue(messages.messages()[0]?.body(), JdaResponse::class.java)
        jdaResponse.receiptId = messages.messages()[0]?.receiptHandle()
        jdaResponse.metaData.receiptId = jdaResponse.receiptId
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

  private suspend fun validateJsonDataWithJsonSchema(jsonSchema: String, data: String, requestHistory: RequestHistory?) {
    try {
      jsonSchemaValidator.validateJson(jsonSchema, data)
    } catch (e: Exception) {
      logger.error("Error occurred while schema validation: ${e.message}")
      if (requestHistory != null) {
        requestHistory.new = false
        requestHistory.status = Status.REJECTED
        requestHistory.errorAt = LocalDateTime.now(ZoneOffset.UTC)
        requestHistory.errorMessage = e.message
        requestHistoryService.saveRequestHistory(requestHistory)
      }
      throw JdaValidationException("Error occurred validating with json schema: ${e.message}")
    }
  }

  private suspend fun convertLlmResponseToApiResponse(llmResponse: Any, requestHistory: RequestHistory?): LlmResponseData {
    try {
      var response = llmResponse
      var promptTokens: Int?
      var completionTokens: Int?
      var totalTokens: Int?
      if (response is ChatCompletion) {
        val message = response.choices().get(0) as Map<String, Any>
        val content = message["message"] as Map<String, Any>
        promptTokens = response.usage().get().promptTokens().toInt()
        completionTokens = response.usage().get().completionTokens().toInt()
        totalTokens = response.usage().get().totalTokens().toInt()
        response = content["content"] as String
        response = response.replace("```json", "")
        response = response.replace("```", "")
        response = response.replace("\n", "")
      }
      if (response is ChatClientResponse) {
        promptTokens = response.chatResponse!!.metadata.usage.promptTokens
        completionTokens = response.chatResponse!!.metadata.usage.completionTokens
        totalTokens = response.chatResponse!!.metadata.usage.totalTokens
        response = response.chatResponse!!.result!!.output.text!!
        response = response.replace("```json", "")
        response = response.replace("```", "")
        response = response.replace("\n", "")
      } else {
        throw IllegalArgumentException("Unknown format response from LLM")
      }
      return LlmResponseData(response, promptTokens, completionTokens, totalTokens)
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

  override suspend fun deleteMessageFromResponseQueue(receipt: JdaDequeReceipt) {
    try {
      logger.info("Deleting message from response queue: ")
      val responseQueue = hmppsQueueService
        .findByQueueId("jdaresponsequeues")
      val sqsClient = responseQueue?.sqsClient
      val queueUrl = responseQueue?.queueUrl
      sqsClient?.deleteMessage(
        DeleteMessageRequest.builder()
          .queueUrl(queueUrl)
          .receiptHandle(receipt.receiptId)
          .build(),
      )
    } catch (e: Exception) {
      val message = "Error deleting message with receipt id: ${receipt.receiptId}, exception: ${e.message}"
      logger.error(message)
      throw SqsQueueException(message)
    }
  }
}

data class LlmResponseData(
  val response: String,
  val promptTokens: Int?,
  val completionTokens: Int?,
  val totalTokens: Int?,
)
