package uk.gov.justice.digital.hmpps.justicedataagentworker.dto.response

import org.springframework.data.relational.core.mapping.Column
import java.time.LocalDateTime
import java.util.UUID

data class PromptResponse(
  var id: UUID? = null,
  @Column()
  val promptKey: String,
  // @OneToMany(mappedBy = "prompt", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
  // val promptVersions: MutableSet<PromptVersion>,
  val description: String,
  val isDeleted: Boolean,
  val createdBy: UUID,
  val createdDate: LocalDateTime,
)
