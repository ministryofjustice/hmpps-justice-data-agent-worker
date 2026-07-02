package uk.gov.justice.digital.hmpps.justicedataagentworker

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class JusticeDataAgentWorker

fun main(args: Array<String>) {
  runApplication<JusticeDataAgentWorker>(*args)
}
