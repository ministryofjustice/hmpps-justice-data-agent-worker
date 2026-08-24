package uk.gov.justice.digital.hmpps.justicedataagentworker.exception

class SqsQueueException(override val message: String) : RuntimeException(message)
