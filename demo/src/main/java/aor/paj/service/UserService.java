package aor.paj.service;

import java.util.List;

import aor.paj.bean.TokenBean;
import aor.paj.bean.UserBean;
import aor.paj.controller.EmailRequest;
import aor.paj.controller.EmailSender;
import aor.paj.dto.*;
import aor.paj.entity.UserEntity;
import aor.paj.responses.ResponseMessage;
import aor.paj.utils.JsonUtils;
import aor.paj.utils.TokenStatus;
import aor.paj.validator.UserValidator;
import aor.paj.websocket.Notifier;
import com.sun.tools.jconsole.JConsoleContext;
import com.sun.tools.jconsole.JConsolePlugin;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

//@Path("/user")
@Path("/users")

public class UserService {

    public UserService() {

    }

    @Inject
    UserBean userBean;
    @Inject
    TokenBean tokenBean;


    //Service that receives a user object and adds it to the list of users
    @POST
    @Path("/")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addUser(UserDto u, @HeaderParam("token") String token, @HeaderParam("role") String roleNewUser) {
        TokenStatus tokenStatus = tokenBean.isValidUserByToken(token);
        if (tokenStatus != TokenStatus.VALID) {
            return Response.status(403).entity(JsonUtils.convertObjectToJson(new ResponseMessage(tokenStatus.getMessage()))).build();
        }

        if (UserValidator.isNullorBlank(u) ||
                (!UserValidator.isValidEmail(u.getEmail()) && !userBean.isSameUserEmail(u.getEmail(), u.getId())) ||
                !UserValidator.isValidPhoneNumber(u.getPhone()) ||
                !UserValidator.isValidURL(u.getPhotoURL()) ||
                userBean.userExists(u)) {
            return Response.status(400).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Invalid input"))).build();
        }

        String role = tokenBean.getUserRole(token);
        if (role.equals("po")) {
            userBean.addUserPO(u, roleNewUser);}
//        } else {
//            userBean.addUser(u);
//        }

        return Response.status(200).entity(JsonUtils.convertObjectToJson(new ResponseMessage("A new user is created"))).build();
    }

    //Service that manages the login of the user, sets the token for the user and sends the token and the role of the user
    @POST
    @Path("/login")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response login(@HeaderParam("username") String username, @HeaderParam("password") String password) {
        String token = tokenBean.login(username, password);
        if (token == null || !userBean.getUserByUsername(username).isActive()) {
            return Response.status(401).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Login Failed"))).build();
        }

        return Response.status(200).entity(JsonUtils.convertObjectToJson(new TokenAndRoleDto(token, userBean.getUserByUsername(username).getRole(), tokenBean.getUserByToken(token).getUsername()))).build();
    }


    @POST
    @Path("/logout")
    @Produces(MediaType.APPLICATION_JSON)
    public Response logout(@HeaderParam("token") String token) {
        TokenStatus tokenStatus = tokenBean.isValidUserByToken(token);
        if (tokenStatus != TokenStatus.VALID) {
            return Response.status(403).entity(JsonUtils.convertObjectToJson(new ResponseMessage(tokenStatus.getMessage()))).build();
        }

        tokenBean.logout(token);
        return Response.status(200).entity(JsonUtils.convertObjectToJson(new ResponseMessage("User is logged out"))).build();
    }


    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllUsers(@HeaderParam("token") String token) {
        TokenStatus tokenStatus = tokenBean.isValidUserByToken(token);
        if (tokenStatus != TokenStatus.VALID) {
            return Response.status(403).entity(JsonUtils.convertObjectToJson(new ResponseMessage(tokenStatus.getMessage()))).build();
        }

        List<UserDto> userDtos = userBean.getAllUsersDB();
        if (userDtos == null || userDtos.isEmpty()) {
            return Response.status(404).entity(JsonUtils.convertObjectToJson(new ResponseMessage("No users found"))).build();
        }

        return Response.status(200).entity(userDtos).build();
    }

    //Service that receives the token to validate and sends the userPartialDto object
    @GET
    @Path("{username}/partial")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUserPartial(@HeaderParam("token") String token, @PathParam("username") String username) {
        TokenStatus tokenStatus = tokenBean.isValidUserByToken(token);
        if (tokenStatus != TokenStatus.VALID) {
            return Response.status(403).entity(JsonUtils.convertObjectToJson(new ResponseMessage(tokenStatus.getMessage()))).build();
        }

        UserDto userDto = tokenBean.getUserByToken(token);
        UserPartialDto userPartialDTO = userBean.mapUserToUserPartialDTO(userDto);
        return Response.status(200).entity(userPartialDTO).build();
    }

    //Service that receives a token and a username and sends the photoURL of the usersame
    @GET
    @Path("{username}/photo")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPhoto(@HeaderParam("token") String token, @PathParam("username") String username) {
        TokenStatus tokenStatus = tokenBean.isValidUserByToken(token);
        if (tokenStatus != TokenStatus.VALID) {
            return Response.status(403).entity(JsonUtils.convertObjectToJson(new ResponseMessage(tokenStatus.getMessage()))).build();
        }

        UserDto userDto = userBean.getUserByUsername(username);
        return Response.status(200).entity(JsonUtils.convertObjectToJson((userDto.getPhotoURL()))).build();
    }

    //Service that receives the token and sends only the users that ownes the tasks in the database mysql
    @GET
    @Path("/owners") //users that own tasks
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUsersOwners(@HeaderParam("token") String token) {
        TokenStatus tokenStatus = tokenBean.isValidUserByToken(token);
        if (tokenStatus != TokenStatus.VALID) {
            return Response.status(403).entity(JsonUtils.convertObjectToJson(new ResponseMessage(tokenStatus.getMessage()))).build();
        }

        List<UserDto> userDtos = userBean.getUsersOwners();
        if (userDtos == null || userDtos.isEmpty()) {
            return Response.status(404).entity(JsonUtils.convertObjectToJson(new ResponseMessage("No users found"))).build();
        }

        return Response.status(200).entity(userDtos).build();
    }

    //Service that receives the token, role of user, and task id and sends if the user has permission to edit the task
    @GET
    @Path("/{username}/permissions/{taskId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response hasPermissionToEdit(@HeaderParam("token") String token, @PathParam("username") String username, @PathParam("taskId") int taskId) {
        TokenStatus tokenStatus = tokenBean.isValidUserByToken(token);
        if (tokenStatus != TokenStatus.VALID) {
            return Response.status(403).entity(JsonUtils.convertObjectToJson(new ResponseMessage(tokenStatus.getMessage()))).build();
        }

        if (tokenBean.hasPermissionToEdit(token, taskId)) {
            return Response.status(200).entity(JsonUtils.convertObjectToJson(new ResponseMessage("User has permission to edit"))).build();
        } else {
            return Response.status(401).entity(JsonUtils.convertObjectToJson(new ResponseMessage("You dont have permission to edit this task."))).build();
        }
    }

    //Service that receives username and password and sends the user object without the password
    @GET
    @Path("/{selectedUser}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUserDetails(@HeaderParam("token") String token, @PathParam("selectedUser") String selectedUser) {
        TokenStatus tokenStatus = tokenBean.isValidUserByToken(token);
        if (tokenStatus != TokenStatus.VALID) {
            return Response.status(403).entity(JsonUtils.convertObjectToJson(new ResponseMessage(tokenStatus.getMessage()))).build();
        }

        if (tokenBean.getUserByToken(token).getRole().equals("po") || tokenBean.getUserByToken(token).getUsername().equals(selectedUser)) {
            UserDto userDto = userBean.getUserByUsername(selectedUser);
            UserDetailsDto userDetails = new UserDetailsDto(
                    userDto.getUsername(),
                    userDto.getFirstname(),
                    userDto.getLastname(),
                    userDto.getEmail(),
                    userDto.getPhotoURL(),
                    userDto.getPhone(),
                    userDto.getRole()
            );
            return Response.status(200).entity(userDetails).build();
        } else {
            return Response.status(401).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Unauthorized"))).build();
        }
    }

    @PUT
    @Path("/{selectedUser}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateUser(UserUpdateDto u, @HeaderParam("token") String token, @PathParam("selectedUser") String selectedUser) {
        TokenStatus tokenStatus = tokenBean.isValidUserByToken(token);
        if (tokenStatus != TokenStatus.VALID) {
            return Response.status(403).entity(JsonUtils.convertObjectToJson(new ResponseMessage(tokenStatus.getMessage()))).build();
        }

        if (tokenBean.getUserByToken(token).getRole().equals("po") || tokenBean.getUserByToken(token).getUsername().equals(selectedUser)) {
            if (!UserValidator.isValidEmail(u.getEmail())) {
                return Response.status(400).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Invalid email format"))).build();
            } else if (!u.getEmail().equals(userBean.getUserByUsername(selectedUser).getEmail()) && UserValidator.emailExists(userBean.getAllUsersDB(), u.getEmail())
                    && (!tokenBean.getUserByToken(token).getEmail().equals(u.getEmail()) || !userBean.getUserByUsername(selectedUser).getEmail().equals(u.getEmail()))) {
                return Response.status(409).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Email already exists"))).build();
            } else if (!UserValidator.isValidPhoneNumber(u.getPhone())) {
                return Response.status(400).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Invalid phone number format"))).build();
            } else if (!UserValidator.isValidURL(u.getPhotoURL())) {
                return Response.status(400).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Invalid URL format"))).build();
            } else {
                userBean.updateUser(u);
                return Response.status(200).entity(JsonUtils.convertObjectToJson(new ResponseMessage("User is updated")).toString()).build();
            }
        }
        return Response.status(401).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Unauthorized"))).build();
    }


    //Services tha receives a UserPasswordDto object, authenticates the user, sees if the user that is logged is the same as the one that is being updated and updates the user password
    @PUT
    @Path("/{username}/password")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updatePassword(UserPasswordUpdateDto u, @PathParam("username") @HeaderParam("token") String token) {
        TokenStatus tokenStatus = tokenBean.isValidUserByToken(token);
        if (tokenStatus != TokenStatus.VALID) {
            return Response.status(403).entity(JsonUtils.convertObjectToJson(new ResponseMessage(tokenStatus.getMessage()))).build();
        }

        boolean updateTry = userBean.updatePassword(u, token);
        if (!updateTry) {
            return Response.status(400).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Old password is incorrect"))).build();
        } else {
            return Response.status(200).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Password is updated")).toString()).build();
        }
    }

    @PUT
    @Path("/{username}/status")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response changeStatus(@HeaderParam("token") String token, @PathParam("username") String username, UserStatusUpdateDto userStatusUpdateDto) {
        TokenStatus tokenStatus = tokenBean.isValidUserByToken(token);
        if (tokenStatus != TokenStatus.VALID || !tokenBean.getUserByToken(token).getRole().equals("po")) {
            return Response.status(403).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Unauthorized"))).build();
        }

        if (userBean.changeStatus(username, userStatusUpdateDto.isActive())) {
            return Response.status(200).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Status changed")).toString()).build();
        } else {
            return Response.status(400).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Status not changed")).toString()).build();
        }
    }


    @DELETE
    @Path("/{selectedUser}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteUser(@HeaderParam("token") String token, @PathParam("selectedUser") String selectedUser) {
        TokenStatus tokenStatus = tokenBean.isValidUserByToken(token);
        if (tokenStatus != TokenStatus.VALID || !tokenBean.getUserByToken(token).getRole().equals("po")) {
            return Response.status(403).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Unauthorized"))).build();
        }

        if (userBean.deleteUser(selectedUser)) {
            return Response.status(200).entity(JsonUtils.convertObjectToJson(new ResponseMessage("User deleted")).toString()).build();
        } else {
            return Response.status(400).entity(JsonUtils.convertObjectToJson(new ResponseMessage("User not deleted")).toString()).build();
        }
    }

    //Delete all tasks of a user
    @DELETE
    @Path("/{selectedUser}/tasks")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteTasks(@HeaderParam("token") String token, @PathParam("selectedUser") String selectedUser) {
        TokenStatus tokenStatus = tokenBean.isValidUserByToken(token);
        if (tokenStatus != TokenStatus.VALID || !tokenBean.getUserByToken(token).getRole().equals("po")) {
            return Response.status(403).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Unauthorized"))).build();
        }

        if (userBean.deleteTasks(selectedUser)) {
            return Response.status(200).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Tasks deleted")).toString()).build();
        } else {
            return Response.status(400).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Tasks not deleted")).toString()).build();
        }
    }

//    @Path("/email/send")
//    @POST
//    @Consumes(MediaType.APPLICATION_JSON)
//    @Produces(MediaType.TEXT_PLAIN)
//    public Response sendEmail(EmailRequest emailRequest) {
//        emailSender.sendEmail(emailRequest.getTo(), emailRequest.getSubject(), emailRequest.getContent());
//        return Response.ok("Email sent successfully").build();
//    }

//    @GET
//    @Path("/confirm/{username}")
//    public Response confirmEmail(@PathParam("username") String username) {
//        System.out.println("Confirming user: " + username);
//        boolean isConfirmed = userBean.confirmUser(username);
//        if (isConfirmed) {
//            userBean.userConfirmed(username);
//            return Response.ok("User confirmed successfully").build();
//        } else {
//            return Response.status(Response.Status.BAD_REQUEST).entity("User not found").build();
//        }
//    }

    @GET
    @Path("/confirm/{token}")
    public Response confirmEmailByToken(@PathParam("token") String token) {
        System.out.println("Confirming token: " + token);
        boolean isConfirmed = userBean.confirmUser(token);
        if (isConfirmed) {
            userBean.userConfirmed(token);
            return Response.ok("User confirmed successfully").build();
        } else {
            return Response.status(Response.Status.BAD_REQUEST).entity("User not found").build();
        }
    }


    @Path("/populator/")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response test(UserDto u) {
        userBean.addUser(u);
        notify();
        return Response.status(200).entity(JsonUtils.convertObjectToJson(new ResponseMessage("A new user is created"))).build();
    }
}



