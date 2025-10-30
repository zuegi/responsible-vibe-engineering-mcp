package ch.zuegi.rvmcp.domain.model.id

import java.util.UUID

@JvmInline
value class ExecutionId(val value: String) {
    companion object {
        fun generate(): ExecutionId = ExecutionId(UUID.randomUUID().toString())
        fun of(value: String): ExecutionId = ExecutionId(value)
    }
}
