package ch.zuegi.rvmcp.domain.model.requirement

data class Stakeholder(
    val name: String,
    val role: String,
    val email: String? = null,
    val responsibilities: List<String> = emptyList(),
) {
    init {
        require(name.isNotBlank()) { "Name must not be blank" }
        require(role.isNotBlank()) { "Role must not be blank" }
    }
}
