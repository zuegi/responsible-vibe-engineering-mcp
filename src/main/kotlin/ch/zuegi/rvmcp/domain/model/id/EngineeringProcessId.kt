package ch.zuegi.rvmcp.domain.model.id

import java.util.UUID

@JvmInline
value class EngineeringProcessId(
    val value: String,
) {
    companion object {
        fun generate(): EngineeringProcessId = EngineeringProcessId(UUID.randomUUID().toString())

        fun of(value: String): EngineeringProcessId = EngineeringProcessId(value)
    }
}
