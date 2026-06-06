package com.jobboard.api.service;

import com.jobboard.api.dto.JobResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AiMatchingService {

    private final ChatClient chatClient;

    @Autowired
    public AiMatchingService(ChatClient.Builder builder) {
        this.chatClient = builder
                .defaultOptions(OpenAiChatOptions.builder()
                        .model("llama-3.1-8b-instant")
                        .build())
                .build();
    }

    public String matchJobs(String resumeText, List<JobResponse> jobs) {
        String jobList = jobs.stream()
                .map(j -> "ID: " + j.getId() + ", Title: " + j.getTitle() +
                        ", Company: " + j.getCompany() + ", Description: " + j.getDescription())
                .reduce("", (a, b) -> a + "\n" + b);

        return chatClient.prompt()
                .user("Given this resume:\n" + resumeText +
                        "\n\nRank these jobs by fit and explain why:\n" + jobList)
                .call()
                .content();
    }
}