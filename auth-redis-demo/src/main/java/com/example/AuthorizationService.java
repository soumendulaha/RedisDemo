package com.example;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.redis.datasource.value.ValueCommands;
import io.quarkus.redis.datasource.keys.KeyCommands;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import java.time.Duration;

@ApplicationScoped
public class AuthorizationService {

    private static final Logger LOG = Logger.getLogger(AuthorizationService.class);
    private static final String REDIS_KEY_PREFIX = "auth:";
    private static final Duration DEFAULT_TTL = Duration.ofHours(24);

    @Inject
    RedisDataSource redisDataSource;

    @Inject
    ObjectMapper objectMapper;

    private ValueCommands<String, String> valueCommands() {
        return redisDataSource.value(String.class);
    }

    private KeyCommands<String> keyCommands() {
        return redisDataSource.key();
    }

    /**
     * Store user permissions in Redis with TTL
     */
    public void storePermissions(UserPermissions permissions) {
        try {
            String key = REDIS_KEY_PREFIX + permissions.getSessionId();
            String jsonValue = objectMapper.writeValueAsString(permissions);

            valueCommands().setex(key, DEFAULT_TTL.getSeconds(), jsonValue);

            LOG.infof("Stored permissions for user %s with session %s",
                    permissions.getUserId(), permissions.getSessionId());

        } catch (JsonProcessingException e) {
            LOG.errorf("Failed to serialize permissions for user %s: %s",
                    permissions.getUserId(), e.getMessage());
            throw new RuntimeException("Failed to store permissions", e);
        }
    }

    /**
     * Retrieve user permissions from Redis
     */
    public UserPermissions getPermissions(String sessionId) {
        try {
            String key = REDIS_KEY_PREFIX + sessionId;
            String jsonValue = valueCommands().get(key);

            if (jsonValue == null) {
                LOG.warnf("No permissions found for session: %s", sessionId);
                return null;
            }

            UserPermissions permissions = objectMapper.readValue(jsonValue, UserPermissions.class);
            LOG.infof("Retrieved permissions for session %s", sessionId);

            return permissions;

        } catch (JsonProcessingException e) {
            LOG.errorf("Failed to deserialize permissions for session %s: %s",
                    sessionId, e.getMessage());
            throw new RuntimeException("Failed to retrieve permissions", e);
        }
    }

    /**
     * Delete user permissions from Redis
     */
    public boolean deletePermissions(String sessionId) {
        String key = REDIS_KEY_PREFIX + sessionId;
        Long deletedCount = (long) keyCommands().del(key);

        boolean wasDeleted = deletedCount > 0;

        if (wasDeleted) {
            LOG.infof("Deleted permissions for session: %s", sessionId);
        } else {
            LOG.warnf("No permissions found to delete for session: %s", sessionId);
        }

        return wasDeleted;
    }

    /**
     * Check if user has specific permission for a resource
     */
    public boolean hasPermission(String sessionId, String resource, String action) {
        UserPermissions permissions = getPermissions(sessionId);

        if (permissions == null) {
            return false;
        }

        var resourcePermissions = permissions.getPermissions().get(resource);
        if (resourcePermissions == null) {
            return false;
        }

        boolean hasAccess = resourcePermissions.contains(action);
        LOG.debugf("Permission check - Session: %s, Resource: %s, Action: %s, Result: %s",
                sessionId, resource, action, hasAccess);

        return hasAccess;
    }
}