package uk.gov.justice.digital.hmpps.justicedataagentworker.model

import com.fasterxml.jackson.annotation.JsonIgnore
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.annotation.Id
import org.springframework.data.domain.Persistable
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime
import java.util.*

@Table(name = "prompt")
data class Prompt(
  @Id
  @JvmField
  var id: UUID,
  val promptKey: String,
  val description: String,
  var isDeleted: Boolean,
  val createdBy: UUID,
  val createdDate: LocalDateTime,
  @Transient
  @Value("false")
  @JsonIgnore
  var new: Boolean = true,
) : Persistable<UUID> {

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as Prompt

    return id == other.id
  }

  override fun hashCode(): Int = id.hashCode()
  override fun getId(): UUID? = this.id

  override fun isNew(): Boolean = new
}
