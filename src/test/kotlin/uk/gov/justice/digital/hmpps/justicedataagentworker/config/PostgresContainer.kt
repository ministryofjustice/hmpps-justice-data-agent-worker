package uk.gov.justice.digital.hmpps.justicedataagentworker.config

import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.containers.wait.strategy.Wait

object PostgresContainer {
  val postgres = PostgreSQLContainer<Nothing>("postgres:latest").apply {
    // withEnv("HOSTNAME_EXTERNAL", "localhost")
    // withUsername("sa")
    // withPassword("password")
    setWaitStrategy(Wait.forListeningPort())
    withReuse(false)
    start()
  }
}
