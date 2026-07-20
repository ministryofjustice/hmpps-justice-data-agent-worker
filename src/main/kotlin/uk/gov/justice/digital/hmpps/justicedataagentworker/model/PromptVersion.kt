package uk.gov.justice.digital.hmpps.justicedataagentworker.model

import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.ManyToOne
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.LocalDateTime
import java.util.UUID

@Entity
data class PromptVersion(
  @Id
  val id: UUID,
  val version: Int,
  @ManyToOne(fetch = FetchType.EAGER)
  val prompt: Prompt,
  val promptTemplate: String,
  @JdbcTypeCode(SqlTypes.JSON)
  val requestContract: Any,
  @JdbcTypeCode(SqlTypes.JSON)
  val responseContract: Any? = null,
  val createdBy: UUID,
  val createdDate: LocalDateTime,
) {
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as PromptVersion

    return id == other.id
  }

  override fun hashCode(): Int = id.hashCode()
}
