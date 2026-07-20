package uk.gov.justice.digital.hmpps.justicedataagentworker.model

import jakarta.persistence.CascadeType
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import java.time.LocalDateTime
import java.util.*

@Entity
data class Prompt(
  @Id
  val id: UUID,
  val promptKey: String,
  @OneToMany(mappedBy = "prompt", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
  val promptVersions: MutableSet<PromptVersion>,
  val description: String,
  val isDeleted: Boolean,
  val createdBy: UUID,
  val createdDate: LocalDateTime,
) {
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as Prompt

    return id == other.id
  }

  override fun hashCode(): Int = id.hashCode()
}
