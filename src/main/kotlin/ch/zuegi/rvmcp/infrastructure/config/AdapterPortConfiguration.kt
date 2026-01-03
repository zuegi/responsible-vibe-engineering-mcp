package ch.zuegi.rvmcp.infrastructure.config

import ch.zuegi.rvmcp.adapter.output.interaction.McpAwareInteractionAdapter
import ch.zuegi.rvmcp.domain.port.output.UserInteractionPort
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class AdapterPortConfiguration {
    /**
     * UserInteractionPort bean for MCP.
     *
     * Suspends workflow execution via PendingInteractionManager until user provides answer.
     */
    @Bean
    fun userInteractionPort(): UserInteractionPort = McpAwareInteractionAdapter()
}
