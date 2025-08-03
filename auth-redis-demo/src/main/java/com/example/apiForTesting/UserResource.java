package com.example.apiForTesting;


import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

@Path("/user")
@Consumes(MediaType.TEXT_PLAIN)
@Produces(MediaType.TEXT_PLAIN)
public class UserResource {

    @POST
    @Path("/create")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public UserResponse createUser(UserRequest request) {
        String msg = "User " + request.name + " is " + request.age + " years old.";
        return new UserResponse(msg, true);
    }
}

