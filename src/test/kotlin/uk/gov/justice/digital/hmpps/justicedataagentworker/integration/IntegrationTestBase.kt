package uk.gov.justice.digital.hmpps.justicedataagentworker.integration

import org.flywaydb.core.Flyway
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient
import org.springframework.http.HttpHeaders
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.web.reactive.server.WebTestClient
import uk.gov.justice.digital.hmpps.justicedataagentworker.config.PostgresContainer
import uk.gov.justice.digital.hmpps.justicedataagentworker.integration.wiremock.HmppsAuthApiExtension
import uk.gov.justice.digital.hmpps.justicedataagentworker.integration.wiremock.HmppsAuthApiExtension.Companion.hmppsAuth
import uk.gov.justice.hmpps.test.kotlin.auth.JwtAuthorisationHelper

@ExtendWith(HmppsAuthApiExtension::class)
@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureWebTestClient
abstract class IntegrationTestBase internal constructor() {

  @Autowired private lateinit var flyway: Flyway

  companion object {

    @JvmStatic
    private val container = PostgresContainer.postgres

    @BeforeAll
    @JvmStatic
    fun startContainer() {
      // postgresContainer.start()
      // flyway
    }

    @AfterAll
    @JvmStatic
    fun stopContainer() {
      // container.stop()
    }

    @DynamicPropertySource
    @JvmStatic
    fun setProperties(registry: DynamicPropertyRegistry) {
      container?.run {
        registry.add("spring.r2dbc.url", { container.getJdbcUrl().replace("jdbc", "r2dbc") })
        registry.add("spring.r2dbc.username", container::getUsername)
        registry.add("spring.r2dbc.password", container::getPassword)
        registry.add("spring.flyway.url", container::getJdbcUrl)
        registry.add("spring.flyway.user", container::getUsername)
        registry.add("spring.flyway.password", container::getPassword)
      }
    }
  }

  @Autowired
  protected lateinit var webTestClient: WebTestClient

  @Autowired
  protected lateinit var jwtAuthHelper: JwtAuthorisationHelper

  internal fun setAuthorisation(
    username: String? = "AUTH_ADM",
    roles: List<String> = listOf(),
    scopes: List<String> = listOf("read"),
  ): (HttpHeaders) -> Unit = jwtAuthHelper.setAuthorisationHeader(username = username, scope = scopes, roles = roles)

  protected fun stubPingWithResponse(status: Int) {
    hmppsAuth.stubHealthPing(status)
  }
}
