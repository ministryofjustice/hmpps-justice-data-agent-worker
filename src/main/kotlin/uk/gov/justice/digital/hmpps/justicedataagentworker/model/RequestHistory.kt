package uk.gov.justice.digital.hmpps.justicedataagentworker.model

import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import java.time.LocalDateTime
import java.util.UUID

@Entity
data class RequestHistory(
  @Id
  val id: UUID,
  val synchronousRequest: Boolean,
  val correlationId: UUID,
  val promptVersionId: UUID,
  val queuedAt: LocalDateTime? = null,
  val receivedAt: LocalDateTime? = null,
  val completedAt: LocalDateTime? = null,
  @Enumerated(EnumType.STRING)
  val status: Status,
  val errorMessage: String? = null,
  val errorAt: LocalDateTime? = null,
) {
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as RequestHistory

    return id == other.id
  }

  override fun hashCode(): Int = id.hashCode()
}
