package com.vektorcontext.dto;

import java.util.List;

public class TaskRequest {
    private String title;
    private String status;
    private String priority;
    private String energyLevel;
    private String frequency;
    private List<String> tags;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public String getEnergyLevel() { return energyLevel; }
    public void setEnergyLevel(String energyLevel) { this.energyLevel = energyLevel; }

    public String getFrequency() { return frequency; }
    public void setFrequency(String frequency) { this.frequency = frequency; }

    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }
}
