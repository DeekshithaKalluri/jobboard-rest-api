package com.jobboard.api.config;

import com.jobboard.api.service.JobSearchMcpService;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class McpConfig {

    @Bean
    public ToolCallbackProvider jobTools(JobSearchMcpService jobSearchMcpService) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(jobSearchMcpService)
                .build();
    }
}