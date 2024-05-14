package aor.paj.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;

import aor.paj.bean.ImageBean;
import aor.paj.bean.Log;
import aor.paj.bean.TokenBean;
import aor.paj.bean.UserBean;
import aor.paj.dto.*;
import aor.paj.entity.UserEntity;
import aor.paj.responses.ResponseMessage;
import aor.paj.utils.JsonUtils;
import aor.paj.utils.ResetPasswordStatus;
import aor.paj.utils.TokenStatus;
import aor.paj.validator.UserValidator;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.apache.logging.log4j.LogManager;

//@Path("/user")
@Path("/users")

public class UserService {
    @Inject
    UserBean userBean;
    @Inject
    TokenBean tokenBean;
    @Inject
    ImageBean imageBean;

    @Inject
    Log log;

    public UserService() {

    }

    private static final org.apache.logging.log4j.Logger logger = LogManager.getLogger(UserService.class);
    @POST
    @Path("/")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addUser(UserDto u, @HeaderParam("token") String token, @HeaderParam("role") String roleNewUser) {
        TokenStatus tokenStatus = tokenBean.isValidUserByToken(token);
        if (tokenStatus != TokenStatus.VALID) {
            log.logUserInfo(token, "Try add user.",3);
            return Response.status(403).entity(JsonUtils.convertObjectToJson(new ResponseMessage(tokenStatus.getMessage()))).build();
        }

        if (UserValidator.isNullorBlank(u) ||
                (!UserValidator.isValidEmail(u.getEmail()) && !userBean.isSameUserEmail(u.getEmail(), u.getId())) ||
                !UserValidator.isValidPhoneNumber(u.getPhone()) ||
                !UserValidator.isValidURL(u.getPhotoURL()) ||
                userBean.userExists(u)) {
            log.logUserInfo(token, "Try add user.",2);
            return Response.status(400).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Invalid input"))).build();
        }

        String role = tokenBean.getUserRole(token);
        if (role.equals("po")) {
            userBean.addUserPO(u, roleNewUser);
            log.logUserInfo(token, "User "+u.getUsername()+" added.", 1);
        }

        return Response.status(200).entity(JsonUtils.convertObjectToJson(new ResponseMessage("A new user is created"))).build();
    }

    //Service that manages the login of the user, sets the token for the user and sends the token and the role of the user
    @POST
    @Path("/login")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response login(@HeaderParam("username") String username, @HeaderParam("password") String password) {
        String token = tokenBean.login(username, password);

        if (token == null) {
            return Response.status(401).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Login Failed"))).build();
        }
        if (!userBean.getUserByUsername(username).isActive()){
            return Response.status(401).entity(JsonUtils.convertObjectToJson(new ResponseMessage("User is not active"))).build();
        }


        return Response.status(200).entity(JsonUtils.convertObjectToJson(
                new TokenAndRoleDto(
                token,
                userBean.getUserByUsername(username).getRole(),
                tokenBean.getUserByToken(token).getUsername(),userBean.userConfirmed(token))
        )).build();
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
        log.logUserInfo(token,"User logged out.",1);
        return Response.status(200).entity(JsonUtils.convertObjectToJson(new ResponseMessage("User is logged out"))).build();
    }


    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllUsers(@HeaderParam("token") String token) {
        TokenStatus tokenStatus = tokenBean.isValidUserByToken(token);
        if (tokenStatus != TokenStatus.VALID) {
            log.logUserInfo(token, "Try get all users.",3);
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

    @GET
    @Path("/profile/{selectedUser}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUserProfile(@HeaderParam("token") String token, @PathParam("selectedUser") String selectedUser) {
        TokenStatus tokenStatus = tokenBean.isValidUserByToken(token);
        if (tokenStatus != TokenStatus.VALID) {
            return Response.status(403).entity(JsonUtils.convertObjectToJson(new ResponseMessage(tokenStatus.getMessage()))).build();
        }
            UserProfileDto userProfileDto = userBean.getUserProfileByUsername(selectedUser);

            return Response.status(200).entity(userProfileDto).build();

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
                log.logUserInfo(token,  "User "+u.getUsername()+" updated.",1);
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
            log.logUserInfo(token, "Try update password.",3);
            return Response.status(403).entity(JsonUtils.convertObjectToJson(new ResponseMessage(tokenStatus.getMessage()))).build();
        }
        boolean updateTry = userBean.updatePassword(u, token);
        if (!updateTry) {
            return Response.status(400).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Old password is incorrect"))).build();
        } else {
            log.logUserInfo(token, "User "+tokenBean.getUserRole(token)+" updated password.",1);
            return Response.status(200).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Password is updated")).toString()).build();
        }
    }

    @PUT
    @Path("/{username}/status")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response changeStatus(@HeaderParam("token") String token, @PathParam("username") String username, UserStatusUpdateDto userStatusUpdateDto) {
        TokenStatus tokenStatus = tokenBean.isValidUserByToken(token);
        if (tokenStatus != TokenStatus.VALID || !tokenBean.getUserByToken(token).getRole().equals("po")) {
            log.logUserInfo(token,  "Try change status in user "+username+"account.",2);
            return Response.status(403).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Unauthorized"))).build();
        }

        if (userBean.changeStatus(username, userStatusUpdateDto.isActive())) {
            log.logUserInfo(token, "User "+username+" status changed.",1);
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
            log.logUserInfo(token, "User "+selectedUser+" deleted.",1);
            return Response.status(200).entity(JsonUtils.convertObjectToJson(new ResponseMessage("User deleted")).toString()).build();
        } else {
            log.logUserInfo(token, "User "+selectedUser+" not deleted.",2);
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
            log.logUserInfo(token,  "Tasks of user "+selectedUser+" deleted.",1);
            return Response.status(200).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Tasks deleted")).toString()).build();
        } else {
            log.logUserInfo(token, "Tasks of user "+selectedUser+" not deleted.",2);
            return Response.status(400).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Tasks not deleted")).toString()).build();
        }
    }

    @POST
    @Path("/confirm/{token}")
    public Response confirmAccountByToken(@PathParam("token") String token, @HeaderParam("password") String password) {
        boolean isConfirmed = userBean.confirmUser(token);
        ResetPasswordStatus status = userBean.resetPassword(token, password);
        if (isConfirmed) {
            log.logUserInfo(token, "User confirmed and password reset.",1);
            return Response.ok(status.getMessage()).build();
        } else {
            return Response.status(Response.Status.BAD_REQUEST).entity(status.getMessage()).build();
        }
    }

    //ENDPOINT PARA ENVIAR EMAIL DE RESET DE PASSWORD
    @POST
    @Path("/password-reset/{email}")
    public Response resetPassword(@PathParam("email") String email) {
        if(userBean.sendPasswordResetEmail(email)){
            log.logUserInfo(null, "Email sent for password reset "+email,1);
            return Response.ok("Email sent for password reset").build();
        }
        return Response.status(Response.Status.BAD_REQUEST).entity("User not found").build();
    }

    //ENDPOINT PARA VALIDAR A MUDANÇA DE PASSWORD
    @POST
    @Path("/password/{token}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response resetPasswordByToken(@PathParam("token") String token, @HeaderParam("password") String password) {
        boolean isConfirmed = userBean.confirmUser(token);
        ResetPasswordStatus status = userBean.resetPassword(token, password);
        if (isConfirmed) {
            log.logUserInfo(token, "Password reset.",1);
            return Response.ok(status.getMessage()).build();
        } else {
            return Response.status(Response.Status.BAD_REQUEST).entity(status.getMessage()).build();
        }
    }

    @Path("/populator/")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addUserFromPopulator(UserDto u) {
        userBean.addUserFromPopulator(u);
        log.logUserInfo(null,  "User "+u.getUsername()+" added.",1);
        return Response.status(200).entity(JsonUtils.convertObjectToJson(new ResponseMessage("A new user is created"))).build();
    }

    @POST
    @Path("/{id}/image")
    @Consumes("image/*")
    public Response uploadUserImage(@PathParam("id") int id, @HeaderParam("filename") String originalFileName, InputStream imageData) {
        try {
            imageBean.saveUserProfileImage(id, imageData, originalFileName);
        } catch (IOException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
        return Response.ok().build();
    }

    @GET
    @Path("/{id}/image")
    @Produces("image/*")
    public Response getUserPicture(@PathParam("id") int id) {
        System.out.println("Getting user image...");
        UserEntity userEntity = userBean.getUserById(id);
        if (userEntity == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        String imagePath = userEntity.getProfileImagePath();
        System.out.println("Image path: " + imagePath);
        if (imagePath == null) {
            return Response.status(Response.Status.NOT_ACCEPTABLE).build();
        }

        System.out.println("Getting image data... bf imageData");
        byte[] imageData;
        try {
            imageData = imageBean.getImage(imagePath);
            System.out.println("Getting image data... af imageData");
        } catch (IOException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }

        String imageType = userEntity.getProfileImageType();

        System.out.println(imageType);
        return Response.ok(new ByteArrayInputStream(imageData)).type(imageType).build();
    }
}



