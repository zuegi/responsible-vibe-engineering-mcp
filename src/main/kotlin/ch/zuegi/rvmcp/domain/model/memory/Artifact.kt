package ch.zuegi.rvmcp.domain.model.memory

import ch.zuegi.rvmcp.domain.model.status.ArtifactType

data class Artifact(
    val name: String,
    val type: ArtifactType,
    val path: String,
    val content: String? = null,
) {
    init {
        require(name.isNotBlank()) { "Name must not be blank" }
        require(path.isNotBlank()) { "Path must not be blank" }
    }
}
