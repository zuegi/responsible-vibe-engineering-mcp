package ch.zuegi.rvmcp.domain.model.id

import java.util.UUID

@JvmInline
value class ProcessId(val value: String) {
    companion object {
        fun generate(): ProcessId = ProcessId(UUID.randomUUID().toString())

        fun of(value: String): ProcessId = ProcessId(value)
    }
}
