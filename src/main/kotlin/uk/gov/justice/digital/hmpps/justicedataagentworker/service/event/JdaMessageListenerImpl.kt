package uk.gov.justice.digital.hmpps.justicedataagentworker.service.event

import io.awspring.cloud.sqs.annotation.SqsListener
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.justicedataagentworker.dto.request.JdaRequest
import uk.gov.justice.digital.hmpps.justicedataagentworker.service.JdaWorkerService

@Component
class JdaMessageListenerImpl(
  private val jdaWorkerService: JdaWorkerService,
  @param:Value("\${hmpps.sqs.queues.jdarequestqueues.dlqName}") private val requestDlqName: String,
) : JdaMessageListener {
  companion object {
    private val logger = LoggerFactory.getLogger(JdaMessageListenerImpl::class.java)
  }

  @SqsListener("jdarequestqueues", factory = "hmppsQueueContainerFactoryProxy")
  override fun onJdaRequestMessageReceived(jdaRequest: JdaRequest) {
    logger.info("Sqs request queue listener, jda request message received for correlation id: ${jdaRequest?.correlationId}")
    runBlocking {
      jdaWorkerService.handleAsynchronousRequest(jdaRequest)
    }
  }
}
