package com.example;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

public class UserPermissions {

    private String userId;
    private String sessionId;
    private Map<String, List<String>> permissions;
    private String timestamp;

    public UserPermissions() {}

    @JsonCreator
    public UserPermissions(
            @JsonProperty("userId") String userId,
            @JsonProperty("sessionId") String sessionId,
            @JsonProperty("permissions") Map<String, List<String>> permissions,
            @JsonProperty("timestamp") String timestamp
    ) {
        this.userId = userId;
        this.sessionId = sessionId;
        this.permissions = permissions;
        this.timestamp = timestamp;
    }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    public Map<String, List<String>> getPermissions() { return permissions; }
    public void setPermissions(Map<String, List<String>> permissions) { this.permissions = permissions; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
}