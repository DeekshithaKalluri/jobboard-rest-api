package com.jobboard.api.service;

import com.jobboard.api.dto.JobResponse;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class JobSearchMcpService {

    private final JobService jobService;

    public JobSearchMcpService(JobService jobService) {
        this.jobService = jobService;
    }

    @Tool(description = "Search jobs by keyword, location, or job type. " +
            "Returns matching job listings with title, location, salary, and job type.")
    public String searchJobs(
            @ToolParam(description = "keyword to search in job title") String keyword,
            @ToolParam(description = "location filter e.g. Remote, New York") String location,
            @ToolParam(description = "job type: FULL_TIME, PART_TIME, CONTRACT") String jobType
    ) {
        List<JobResponse> results =
                jobService.searchJobsForMcp(keyword, location, jobType);

        if (results.isEmpty()) return "No jobs found matching your criteria.";

        return results.stream()
                .map(j -> "#" + j.getId() + ": " + j.getTitle()
                        + " | " + j.getLocation()
                        + " | " + j.getJobType()
                        + " | $" + j.getSalary())
                .collect(Collectors.joining("\n"));
    }

    @Tool(description = "Get total count of available jobs in the database")
    public String getJobCount() {
        int count = jobService.getAllJobsForAi().size();
        return "There are currently " + count + " jobs available.";
    }
}