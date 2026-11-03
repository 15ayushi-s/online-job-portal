package com.jobhub.model;

import com.jobhub.util.JsonSerializable;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

public class Job implements JsonSerializable {
    private Long id;
    private String title;
    private String company;
    private String location;
    private String type;
    private String salary;
    private String description;
    private String skills;
    private JobStatus status = JobStatus.PENDING;
    private Long employerId;
    private LocalDateTime createdAt = LocalDateTime.now();

    public Job() {}

    public Long getId() { return id; }
    public void setId(Long v) { id = v; }
    public String getTitle() { return title; }
    public void setTitle(String v) { title = v; }
    public String getCompany() { return company; }
    public void setCompany(String v) { company = v; }
    public String getLocation() { return location; }
    public void setLocation(String v) { location = v; }
    public String getType() { return type; }
    public void setType(String v) { type = v; }
    public String getSalary() { return salary; }
    public void setSalary(String v) { salary = v; }
    public String getDescription() { return description; }
    public void setDescription(String v) { description = v; }
    public String getSkills() { return skills; }
    public void setSkills(String v) { skills = v; }
    public JobStatus getStatus() { return status; }
    public void setStatus(JobStatus v) { status = v; }
    public Long getEmployerId() { return employerId; }
    public void setEmployerId(Long v) { employerId = v; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime v) { createdAt = v; }

    @Override
    public Map<String, Object> toJson() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", id); m.put("title", title); m.put("company", company); m.put("location", location);
        m.put("type", type); m.put("salary", salary); m.put("description", description); m.put("skills", skills);
        m.put("status", status.name()); m.put("employerId", employerId); m.put("createdAt", createdAt);
        return m;
    }
}
