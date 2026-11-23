package uk.gov.justice.digital.hmpps.justicedataagentworker.service

import com.microsoft.applicationinsights.TelemetryClient
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

@Service
class TelemetryService(private val telemetryClient: TelemetryClient) {

  companion object {
    private val FORMATTER = DateTimeFormatter.ISO_DATE_TIME
    private val logger = LoggerFactory.getLogger(this::class.java)
  }

  suspend fun sendTelemetryData(
    eventName: String,
    queueName: String?,
    requestId: UUID,
    correlationId: UUID,
    promptKey: String,
    promptVersion: Int?,
    dateTime: LocalDateTime,
    batchSize: Int?,
    inputTokens: Int?,
    outputTokens: Int?,
    totalTokens: Int?,
    completionDuration: Int?,
    inputItemsNum: Int?,
    outputItemsNum: Int?,
    missingIdentifiersNum: Int?,
  ) {
    val map = mutableMapOf<String, String?>()
    map["requestId"] = requestId.toString()
    map["correlationId"] = correlationId.toString()
    map["promptKey"] = promptKey
    map["dateTime"] = dateTime.format(FORMATTER)
    if (queueName != null) {
      map["queueName"] = queueName
    }
    if (batchSize != null) {
      map["batchSize"] = batchSize.toString()
    }
    if (inputTokens != null) {
      map["inputTokens"] = inputTokens.toString()
    }
    if (outputTokens != null) {
      map["outputTokens"] = outputTokens.toString()
    }
    if (totalTokens != null) {
      map["totalTokens"] = totalTokens.toString()
    }
    if (completionDuration != null) {
      map["completionDuration"] = completionDuration.toString()
    }
    if (promptVersion != null) {
      map["promptVersion"] = promptVersion.toString()
    }
    if (inputItemsNum != null) {
      map["inputItemsNum"] = inputItemsNum.toString()
    }
    if (outputItemsNum != null) {
      map["outputItemsNum"] = outputItemsNum.toString()
    }
    if (missingIdentifiersNum != null) {
      map["missingIdentifiersNum"] = missingIdentifiersNum.toString()
    }
    try {
      telemetryClient.trackEvent(eventName, map, null)
    } catch (e: Exception) {
      logger.error("An error occurred sending telemetry event: $eventName, error: ${e.message}")
    }
  }
}
