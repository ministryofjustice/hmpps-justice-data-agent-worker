package uk.gov.justice.digital.hmpps.justicedataagentworker.dto.request

import io.swagger.v3.oas.annotations.media.Schema

data class JdaDequeReceipt(
  @Schema(description = "Receipt id of the message.", required = true, example = "39f262b1-a632-408a-9a71-7261225687af")
  val receiptId: String,
)
