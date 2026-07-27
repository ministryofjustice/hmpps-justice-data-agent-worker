package uk.gov.justice.digital.hmpps.justicedataagentworker.model

import org.springframework.data.annotation.Id
import org.springframework.data.domain.Persistable
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime
import java.util.*

@Table(name = "prompt")
data class Prompt(
  @Id
  @JvmField
  var id: UUID? = null,
  @Column()
  val promptKey: String,
  val description: String,
  val isDeleted: Boolean,
  val createdBy: UUID,
  val createdDate: LocalDateTime,
) : Persistable<UUID> {
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as Prompt

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
