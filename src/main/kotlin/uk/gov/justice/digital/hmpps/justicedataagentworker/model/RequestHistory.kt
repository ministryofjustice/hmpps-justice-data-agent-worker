package uk.gov.justice.digital.hmpps.justicedataagentworker.model

import com.fasterxml.jackson.annotation.JsonIgnore
import org.springframework.beans.factory.annotation.Value
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
  var completedAt: LocalDateTime? = null,
  var status: Status,
  var errorMessage: String? = null,
  var errorAt: LocalDateTime? = null,
  @Transient
  @Value("false")
  @JsonIgnore
  var new: Boolean = true,
) : Persistable<UUID> {

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as RequestHistory

    return id == other.id
  }

  override fun hashCode(): Int = id.hashCode()

  override fun getId(): UUID? = this.id

  override fun isNew(): Boolean = new
}
