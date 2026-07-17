package uk.gov.justice.digital.hmpps.justicedataagentworker.integration.health

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.info.BuildProperties
import uk.gov.justice.digital.hmpps.justicedataagentworker.integration.IntegrationTestBase

class InfoTest(
  @Autowired private val buildProperties: BuildProperties,
) : IntegrationTestBase() {
  @Test
  fun `info page is accessible`() {
    webTestClient.get()
      .uri("/info")
      .exchange()
      .expectStatus()
      .isOk
      .expectBody()
      .jsonPath("build.name").isEqualTo("hmpps-justice-data-agent-worker")
  }

  @Test
  fun `info page reports version`() {
    webTestClient.get().uri("/info")
      .exchange()
      .expectStatus().isOk
      .expectBody().jsonPath("build.version").isEqualTo(buildProperties.version)
  }
}
