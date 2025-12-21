package ch.zuegi.rvmcp.adapter.output.workflow.model.node

import ch.zuegi.rvmcp.adapter.output.workflow.model.NodeType
import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Node that executes a system command (e.g. mvn test, ktlint).
 * Used for running build tools, tests, or other shell commands.
 */
data class SystemCommandNode(
    override val id: String,
    override val description: String?,
    val command: String,
    @JsonProperty("expected_output")
    val expectedOutput: String? = null,
    @JsonProperty("on_failure")
    val onFailure: String? = null,
    val next: String? = null,
) : WorkflowNode() {
    override fun getNodeType(): NodeType = NodeType.SYSTEM_COMMAND
}
