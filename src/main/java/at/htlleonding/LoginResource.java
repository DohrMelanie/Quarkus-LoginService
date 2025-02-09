package at.htlleonding;

import at.htlleonding.dtos.LoginDto;
import at.htlleonding.dtos.RegisterDto;
import at.htlleonding.dtos.ResetPasswordDto;
import at.htlleonding.jwt.JWTService;
import jakarta.inject.Inject;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;

@Consumes("application/json")
@Path("/api/v1")
@Produces("application/json")
@Slf4j
public class LoginResource {
    private final LoginService loginService;
    
    @Inject
    JWTService jwtService;
    
    public LoginResource(@NotNull final LoginService loginService) {
        log.info("start");
        this.loginService = loginService;
    }

    @POST
    @Path("/register")
    public Response register(RegisterDto user) {
        log.info("register + hash and salt pw");
        try {
            loginService.addUser(new User(user.getUsername(), user.getPassword(), user.getTelephoneNumber()));
        } catch (IllegalArgumentException e) {
            return Response.status(400, e.getMessage()).build();
        }
        return Response.status(201).build();
    }

    @POST
    @Path("/login")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response login(LoginDto user) {
        log.info("login");
        try {
            if (loginService.checkPassword(user.getUsername(), user.getPassword())) {
                String token = jwtService.generateToken(user.getUsername(), 30);
                return Response.ok().header("Authorization", "Bearer " + token).build();
            } else {
                return Response.status(400).build();
            }
        } catch (IllegalArgumentException e) {
            return Response.status(400).build();
        } catch (Exception e) {
            return Response.status(401).build();
        }
    }

    @GET
    @Path("/resetpw/{username}")
    public Response resetPassword(@PathParam("username") String username) {
        log.info("reset password");
        String code = loginService.resetPassword(username);
        return Response.ok(code).build();
    }

    @POST
    @Path("/resetpw/code/")
    public Response resetPasswordWithCode(ResetPasswordDto resetPasswordDto) {
        log.info("reset password with code");
        try {
            boolean success = loginService.resetPasswordWithCode(resetPasswordDto.getUsername(), resetPasswordDto.getResetCode(), resetPasswordDto.getNewPassword());
            if (!success) {
                return Response.status(400).build();
            }
        } catch (IllegalArgumentException e) {
            return Response.status(400).build();
        }
        return Response.status(200).build();
    }
}
