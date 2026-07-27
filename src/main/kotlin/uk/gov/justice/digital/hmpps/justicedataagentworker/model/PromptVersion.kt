package uk.gov.justice.digital.hmpps.justicedataagentworker.model

import com.fasterxml.jackson.annotation.JsonIgnore
import io.r2dbc.postgresql.codec.Json
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.annotation.Id
import org.springframework.data.domain.Persistable
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime
import java.util.UUID

@Table("prompt_version")
data class PromptVersion(
  @Id
  @JvmField
  var id: UUID? = null,
  val version: Int,

  val promptId: UUID,
  val llmModel: String,
  val promptTemplate: String,

  val requestContract: Json,

  val responseContract: Json? = null,
  val createdBy: UUID,
  val createdDate: LocalDateTime,
  @Transient
  @Value("false")
  @JsonIgnore
  var new: Boolean = true
) : Persistable<UUID> {

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as PromptVersion

    return id == other.id
  }

  override fun hashCode(): Int = id.hashCode()

  override fun getId(): UUID? = this.id

  override fun isNew(): Boolean {
    return new
  }
}
