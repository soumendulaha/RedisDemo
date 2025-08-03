package com.example;


import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;

@Path("/api/auth")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AuthorizationResource {

    private static final Logger LOG = Logger.getLogger(AuthorizationResource.class);

    @Inject
    AuthorizationService authorizationService;

    /**
     * Test endpoint to verify the API is working
     */
    @GET
    @Path("/test")
    public Response test() {
        return Response.ok("{\"message\": \"API is working\"}").build();
    }

    /**
     * Store user permissions in Redis
     * POST /api/auth/permissions
     */
    @POST
    @Path("/permissions")
    public Response storePermissions(UserPermissions permissions) {
        try {
            if (permissions == null || permissions.getSessionId() == null || permissions.getSessionId().isBlank()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"error\": \"Invalid permissions data or missing sessionId\"}")
                        .build();
            }

            authorizationService.storePermissions(permissions);

            return Response.status(Response.Status.CREATED)
                    .entity("{\"message\": \"Permissions stored successfully\", \"sessionId\": \"" + permissions.getSessionId() + "\"}")
                    .build();

        } catch (Exception e) {
            LOG.errorf("Error storing permissions: %s", e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Failed to store permissions\"}")
                    .build();
        }
    }

    /**
     * Retrieve user permissions from Redis
     * GET /api/auth/permissions/{sessionId}
     */
    @GET
    @Path("/permissions/{sessionId}")
    public Response getPermissions(@PathParam("sessionId") String sessionId) {
        try {
            if (sessionId == null || sessionId.isBlank()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"error\": \"SessionId is required\"}")
                        .build();
            }

            UserPermissions permissions = authorizationService.getPermissions(sessionId);

            if (permissions == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"error\": \"Permissions not found for session: " + sessionId + "\"}")
                        .build();
            }

            return Response.ok(permissions).build();

        } catch (Exception e) {
            LOG.errorf("Error retrieving permissions for session %s: %s", sessionId, e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Failed to retrieve permissions\"}")
                    .build();
        }
    }

    /**
     * Delete user permissions from Redis
     * DELETE /api/auth/permissions/{sessionId}
     */
    @DELETE
    @Path("/permissions/{sessionId}")
    public Response deletePermissions(@PathParam("sessionId") String sessionId) {
        try {
            if (sessionId == null || sessionId.isBlank()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"error\": \"SessionId is required\"}")
                        .build();
            }

            boolean deleted = authorizationService.deletePermissions(sessionId);

            if (deleted) {
                return Response.ok()
                        .entity("{\"message\": \"Permissions deleted successfully\", \"sessionId\": \"" + sessionId + "\"}")
                        .build();
            } else {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"error\": \"Permissions not found for session: " + sessionId + "\"}")
                        .build();
            }

        } catch (Exception e) {
            LOG.errorf("Error deleting permissions for session %s: %s", sessionId, e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Failed to delete permissions\"}")
                    .build();
        }
    }

    /**
     * Check specific permission for a resource and action
     * GET /api/auth/check/{sessionId}/{resource}/{action}
     */
    @GET
    @Path("/check/{sessionId}/{resource}/{action}")
    public Response checkPermission(
            @PathParam("sessionId") String sessionId,
            @PathParam("resource") String resource,
            @PathParam("action") String action) {

        try {
            boolean hasPermission = authorizationService.hasPermission(sessionId, "/" + resource, action);

            return Response.ok()
                    .entity("{\"sessionId\": \"" + sessionId + "\", " +
                            "\"resource\": \"/" + resource + "\", " +
                            "\"action\": \"" + action + "\", " +
                            "\"hasPermission\": " + hasPermission + "}")
                    .build();

        } catch (Exception e) {
            LOG.errorf("Error checking permission: %s", e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Failed to check permission\"}")
                    .build();
        }
    }
}