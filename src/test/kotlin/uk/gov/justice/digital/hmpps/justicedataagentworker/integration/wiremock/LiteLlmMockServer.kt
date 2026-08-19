package uk.gov.justice.digital.hmpps.justicedataagentworker.integration.wiremock

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.post
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import com.github.tomakehurst.wiremock.http.HttpHeader
import com.github.tomakehurst.wiremock.http.HttpHeaders
import org.junit.jupiter.api.extension.AfterAllCallback
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext

class LiteLlmApiExtension :
  BeforeAllCallback,
  AfterAllCallback,
  BeforeEachCallback {
  companion object {
    @JvmField
    val liteLlm = LiteLlmMockServer()
  }

  override fun beforeAll(context: ExtensionContext) {
    liteLlm.start()
    liteLlm.stubChatCompletion()
  }

  override fun beforeEach(context: ExtensionContext) {
    liteLlm.resetRequests()
  }

  override fun afterAll(context: ExtensionContext) {
    liteLlm.stop()
  }
}

class LiteLlmMockServer : WireMockServer(WIREMOCK_PORT) {
  companion object {
    private const val WIREMOCK_PORT = 8091
  }

  fun stubChatCompletion() {
    stubFor(
      post(urlEqualTo("/chat/completions"))
        .willReturn(
          aResponse()
            .withHeaders(HttpHeaders(HttpHeader("Content-Type", "application/json")))
            .withBody(
              """
                {
                    "id": "chatcmpl-b41af546-94fa-4a22-ae90-1ad28b9d0622",
                    "created": 1787087240,
                    "model": "bedrock-claude-sonnet-4-5",
                    "object": "chat.completion",
                    "choices": [
                        {
                            "finish_reason": "stop",
                            "index": 0,
                            "message": {
                                "content": "```json\n{\n  \"query\": \"History of Boston\",\n  \"summary\": \"Boston is one of the oldest cities in the United States with a rich colonial and revolutionary history\",\n  \"key_periods\": [\n    {\n      \"period\": \"Founding (1630)\",\n      \"description\": \"Boston was founded by Puritan colonists from England, led by John Winthrop. It was named after Boston, Lincolnshire in England.\"\n    },\n    {\n      \"period\": \"Colonial Era (1630-1770s)\",\n      \"description\": \"Boston became a major colonial port and center of commerce, education, and religion in New England.\"\n    },\n    {\n      \"period\": \"American Revolution (1770s-1780s)\",\n      \"description\": \"Boston played a crucial role in the American Revolution, hosting events like the Boston Massacre (1770), Boston Tea Party (1773), and the Battle of Bunker Hill (1775).\"\n    },\n    {\n      \"period\": \"19th Century\",\n      \"description\": \"Boston became a major manufacturing and cultural center, known for its strong abolitionist movement and intellectual community.\"\n    },\n    {\n      \"period\": \"20th-21st Century\",\n      \"description\": \"Boston evolved into a global center for higher education, medicine, technology, and finance, while preserving its historical heritage.\"\n    }\n  ],\n  \"notable_events\": [\n    \"Boston Massacre (1770)\",\n    \"Boston Tea Party (1773)\",\n    \"Battle of Bunker Hill (1775)\",\n    \"Great Boston Fire (1872)\",\n    \"Boston Police Strike (1919)\",\n    \"Busing Crisis (1970s)\"\n  ],\n  \"cultural_significance\": \"Boston is known as the 'Cradle of Liberty' for its role in the American Revolution and remains an important educational and cultural center\"\n}\n```",
                                "role": "assistant"
                            }
                        }
                    ],
                    "usage": {
                        "completion_tokens": 436,
                        "prompt_tokens": 27,
                        "total_tokens": 463,
                        "completion_tokens_details": {
                            "reasoning_tokens": 0,
                            "text_tokens": 436
                        },
                        "prompt_tokens_details": {
                            "cached_tokens": 0,
                            "text_tokens": 27,
                            "cache_creation_tokens": 0
                        },
                        "cache_creation_input_tokens": 0,
                        "cache_read_input_tokens": 0
                    }
                }
              """.trimIndent(),
            ),
        ),
    )
  }
}
