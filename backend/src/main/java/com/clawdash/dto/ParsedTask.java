package com.clawdash.dto;

import java.io.Serializable;

public class ParsedTask implements Serializable {

    private String rawInput;
    private String title;
    private String description;
    private String priority;
    private Boolean explicitDecomposeRequested;

    public ParsedTask() {
        this.priority = "medium";
        this.explicitDecomposeRequested = false;
    }

    public String getRawInput() {
        return rawInput;
    }

    public void setRawInput(String rawInput) {
        this.rawInput = rawInput;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public Boolean getExplicitDecomposeRequested() {
        return explicitDecomposeRequested;
    }

    public void setExplicitDecomposeRequested(Boolean explicitDecomposeRequested) {
        this.explicitDecomposeRequested = explicitDecomposeRequested;
    }

    @Override
    public String toString() {
        return "ParsedTask{" +
                "rawInput='" + rawInput + '\'' +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", priority='" + priority + '\'' +
                ", explicitDecomposeRequested=" + explicitDecomposeRequested +
                '}';
    }
}
