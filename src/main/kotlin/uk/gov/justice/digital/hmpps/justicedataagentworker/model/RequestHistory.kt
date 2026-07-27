package uk.gov.justice.digital.hmpps.justicedataagentworker.model

import org.springframework.data.annotation.Id
import org.springframework.data.domain.Persistable
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime
import java.util.UUID

@Table
data class RequestHistory(
  @Id
  @JvmField
  var id: UUID? = null,
  val synchronousRequest: Boolean,
  val correlationId: UUID,
  val promptVersionId: UUID,
  val queuedAt: LocalDateTime? = null,
  val receivedAt: LocalDateTime? = null,
  val completedAt: LocalDateTime? = null,
  val status: Status,
  val errorMessage: String? = null,
  val errorAt: LocalDateTime? = null,
) : Persistable<UUID> {
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as RequestHistory

    return id == other.id
  }

  override fun hashCode(): Int = id.hashCode()

  override fun getId(): UUID? = this.id

  override fun isNew(): Boolean {
    if (this.id == null) {
      return true
    }
    return false
  }
}
