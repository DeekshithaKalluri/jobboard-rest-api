package com.jobboard.api.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Service
public class AiMatchingService {

    private final ChatClient chatClient;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${adzuna.app.id}")
    private String adzunaAppId;

    @Value("${adzuna.app.key}")
    private String adzunaAppKey;

    public AiMatchingService(ChatClient.Builder builder) {
        this.chatClient = builder
                .defaultOptions(OpenAiChatOptions.builder()
                        .model("llama-3.1-8b-instant")
                        .build())
                .build();
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    public String matchJobs(String resumeText) {
        // Step 1: Extract keywords from resume for search
        String keywords = extractKeywords(resumeText);

        // Step 2: Fetch real jobs from Adzuna
        List<AdzunaJob> jobs = fetchAdzunaJobs(keywords);

        if (jobs.isEmpty()) {
            return "[{\"rank\":1,\"title\":\"No jobs found\",\"company\":\"\",\"match_reason\":\"No jobs matched your search. Try different keywords.\",\"match_score\":0,\"apply_url\":\"\"}]";
        }

        // Step 3: Build job list string for the prompt
        StringBuilder jobList = new StringBuilder();
        for (int i = 0; i < jobs.size(); i++) {
            AdzunaJob j = jobs.get(i);
            jobList.append(i).append(". ID:").append(i)
                    .append(" Title:").append(j.title)
                    .append(" Company:").append(j.company)
                    .append(" Location:").append(j.location)
                    .append(" Salary:").append(j.salary)
                    .append(" Description:").append(
                            j.description.length() > 200
                                    ? j.description.substring(0, 200)
                                    : j.description)
                    .append("\n");
        }

        // Step 4: Ask AI to rank top 3
        String prompt = """
            You are a job matching assistant. Given this resume and job list, pick the top 3 best matches.
            
            RESUME:
            %s
            
            JOBS:
            %s
            
            Return ONLY a JSON array with exactly 3 objects, no other text:
            [
              {"rank":1,"index":0,"title":"...","company":"...","match_score":85,"match_reason":"2 sentence explanation"},
              {"rank":2,"index":1,"title":"...","company":"...","match_score":75,"match_reason":"2 sentence explanation"},
              {"rank":3,"index":2,"title":"...","company":"...","match_score":65,"match_reason":"2 sentence explanation"}
            ]
            Use the exact index numbers from the job list above.
            """.formatted(resumeText, jobList.toString());

        String aiResponse = chatClient.prompt()
                .user(prompt)
                .call()
                .content();

        // Step 5: Inject apply_url into AI response
        return injectApplyUrls(aiResponse, jobs);
    }

    private String extractKeywords(String resumeText) {
        String lower = resumeText.toLowerCase();
        // Check for common tech keywords first
        if (lower.contains("java")) return "java developer";
        if (lower.contains("python")) return "python developer";
        if (lower.contains("react") || lower.contains("frontend")) return "react developer";
        if (lower.contains("data analyst") || lower.contains("tableau")) return "data analyst";
        if (lower.contains("machine learning") || lower.contains("ml engineer")) return "machine learning engineer";
        if (lower.contains("devops") || lower.contains("kubernetes")) return "devops engineer";
        if (lower.contains("spring boot")) return "spring boot developer";
        // Fallback: take first 3 meaningful words
        String[] words = resumeText.trim().split("\\s+");
        StringBuilder kw = new StringBuilder();
        int count = 0;
        for (String word : words) {
            String clean = word.replaceAll("[^a-zA-Z]", "");
            if (clean.length() > 2) {
                if (kw.length() > 0) kw.append(" ");
                kw.append(clean);
                if (++count >= 3) break;
            }
        }
        return kw.length() > 0 ? kw.toString() : "software engineer";
    }

    private List<AdzunaJob> fetchAdzunaJobs(String keywords) {
        List<AdzunaJob> jobs = new ArrayList<>();
        try {
            String encodedKeywords = keywords.replace(" ", "%20");
            String urlStr = String.format(
                    "https://api.adzuna.com/v1/api/jobs/us/search/1" +
                            "?app_id=%s&app_key=%s&results_per_page=10&what=%s&content-type=application/json",
                    adzunaAppId, adzunaAppKey, encodedKeywords
            );

            java.net.URL url = new java.net.URL(urlStr);
            java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);

            java.io.BufferedReader reader = new java.io.BufferedReader(
                    new java.io.InputStreamReader(conn.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);
            reader.close();

            String response = sb.toString();
            JsonNode root = objectMapper.readTree(response);
            JsonNode results = root.get("results");

            if (results != null && results.isArray()) {
                for (JsonNode node : results) {
                    AdzunaJob job = new AdzunaJob();
                    job.title = getText(node, "title");
                    job.company = node.has("company")
                            ? getText(node.get("company"), "__display_name") : "Unknown";
                    job.location = node.has("location")
                            ? getText(node.get("location"), "display_name") : "Unknown";
                    job.description = getText(node, "description");
                    job.applyUrl = getText(node, "redirect_url");
                    JsonNode salaryMin = node.get("salary_min");
                    job.salary = salaryMin != null && !salaryMin.isNull()
                            ? "$" + String.format("%.0f", salaryMin.asDouble()) + "/yr"
                            : "Not specified";
                    jobs.add(job);
                }
            }
        } catch (Exception e) {
            System.err.println("Adzuna fetch error: " + e.getClass().getName() + ": " + e.getMessage());
        }
        return jobs;
    }

    private String injectApplyUrls(String aiResponse, List<AdzunaJob> jobs) {
        try {
            String jsonStr = aiResponse;
            int start = aiResponse.indexOf('[');
            int end = aiResponse.lastIndexOf(']');
            if (start >= 0 && end > start) {
                jsonStr = aiResponse.substring(start, end + 1);
            }

            JsonNode arr = objectMapper.readTree(jsonStr);
            List<JsonNode> result = new ArrayList<>();
            for (JsonNode match : arr) {
                int index = match.has("index") ? match.get("index").asInt() : 0;
                String applyUrl = (index < jobs.size())
                        ? jobs.get(index).applyUrl : "";
                ((com.fasterxml.jackson.databind.node.ObjectNode) match)
                        .put("apply_url", applyUrl);
                result.add(match);
            }
            return objectMapper.writeValueAsString(result);
        } catch (Exception e) {
            return aiResponse;
        }
    }

    private String getText(JsonNode node, String field) {
        JsonNode f = node.get(field);
        return f != null && !f.isNull() ? f.asText() : "";
    }

    static class AdzunaJob {
        String title, company, location, description, applyUrl, salary;
    }
}