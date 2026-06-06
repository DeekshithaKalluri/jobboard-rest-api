package com.jobboard.api.controller;

import com.jobboard.api.dto.JobResponse;
import java.util.List;
import com.jobboard.api.dto.MatchRequest;
import com.jobboard.api.model.Job;
import com.jobboard.api.service.AiMatchingService;
import com.jobboard.api.service.JobService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/jobs")
public class JobController {

    @Autowired
    private JobService jobService;

    @Autowired
    private AiMatchingService aiMatchingService;

    @GetMapping
    public ResponseEntity<Page<JobResponse>> getAllJobs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {

        Sort sort = direction.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(jobService.getAllJobs(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getJobById(@PathVariable Long id) {
        return jobService.getJobById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/search")
    public ResponseEntity<Page<JobResponse>> searchJobs(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) Job.JobType jobType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {

        Sort sort = direction.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(jobService.searchJobs(title, location, jobType, pageable));
    }

    @PostMapping
    public ResponseEntity<JobResponse> createJob(@Valid @RequestBody Job job,
                                                 Authentication authentication) {
        JobResponse created = jobService.createJob(job, authentication.getName());
        return ResponseEntity.ok(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateJob(@PathVariable Long id,
                                       @Valid @RequestBody Job job,
                                       Authentication authentication) {
        return jobService.updateJob(id, job, authentication.getName())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteJob(@PathVariable Long id,
                                       Authentication authentication) {
        boolean deleted = jobService.deleteJob(id, authentication.getName());
        if (deleted) {
            return ResponseEntity.ok(Map.of("message", "Job deleted successfully"));
        }
        return ResponseEntity.notFound().build();
    }
    @PostMapping("/ai-match")
    public ResponseEntity<String> aiMatch(
            @Valid @RequestBody MatchRequest request) {
        List<JobResponse> jobs = jobService.getAllJobsForAi();
        String result = aiMatchingService.matchJobs(request.resumeText(), jobs);
        return ResponseEntity.ok(result);
    }
}