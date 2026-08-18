package uk.gov.justice.digital.hmpps.justicedataagentworker.service.event

import io.awspring.cloud.sqs.annotation.SqsListener
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import tools.jackson.databind.ObjectMapper
import uk.gov.justice.digital.hmpps.justicedataagentworker.dto.request.JdaRequest
import uk.gov.justice.digital.hmpps.justicedataagentworker.dto.response.JdaResponse
import uk.gov.justice.digital.hmpps.justicedataagentworker.service.JdaWorkerService

@Component
class JdaMessageListenerImpl(
  private val jdaWorkerService: JdaWorkerService,
) : JdaMessageListener {
  companion object {
    private val logger = LoggerFactory.getLogger(JdaMessageListenerImpl::class.java)
  }

  @SqsListener("jdarequestqueus", factory = "hmppsQueueContainerFactoryProxy")
  override  fun onJdaRequestMessageReceived(message: JdaRequest) {
    logger.info("Sqs jda request message received")
    runBlocking {
      jdaWorkerService.handleSynchronousRequest(message)
    }
  }

  /*@SqsListener("jdaresponsequeus", factory = "hmppsQueueContainerFactoryProxy")
  override  fun onJdaResponseMessageReceived(jdaResponse: JdaResponse) {
    jdaMessagePublisher.publishJdaResponse(jdaResponse)
  }*/

}