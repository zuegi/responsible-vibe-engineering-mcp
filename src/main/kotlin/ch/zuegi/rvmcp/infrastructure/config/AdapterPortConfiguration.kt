package ch.zuegi.rvmcp.infrastructure.config

import ch.zuegi.rvmcp.adapter.output.interaction.McpAwareInteractionAdapter
import ch.zuegi.rvmcp.domain.port.output.UserInteractionPort
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
class AdapterPortConfiguration {
    /**
     * UserInteractionPort bean - MCP Mode (default).
     *
     * In MCP mode, throws InteractionRequiredException to pause workflow.
     */
    @Bean
    @Profile("!cli")
    fun userInteractionPortMcp(): UserInteractionPort = McpAwareInteractionAdapter(mcpMode = true)

    /**
     * UserInteractionPort bean - CLI Mode (for tests).
     *
     * In CLI mode, uses stdin/stdout for direct interaction.
     */
    @Bean
    @Profile("cli")
    fun userInteractionPortCli(): UserInteractionPort = McpAwareInteractionAdapter(mcpMode = false)
}
