package aor.paj.service;

import java.util.List;

import aor.paj.bean.TokenBean;
import aor.paj.bean.UserBean;
import aor.paj.controller.EmailRequest;
import aor.paj.controller.EmailSender;
import aor.paj.dto.*;
import aor.paj.responses.ResponseMessage;
import aor.paj.utils.JsonUtils;
import aor.paj.validator.UserValidator;
import com.sun.tools.jconsole.JConsoleContext;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

//@Path("/user")
@Path("/users")

public class UserService {

    private final EmailSender emailSender;

    public UserService() {
        this.emailSender = new EmailSender();
    }
    @Inject
    UserBean userBean;
    @Inject
    TokenBean tokenBean;


    //Service that receives a user object and adds it to the list of users
    @POST
//    @Path("/add")
    @Path("/")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addUser(UserDto u, @HeaderParam("token") String token, @HeaderParam("role") String roleNewUser) {
        // Check if any parameter is null or blank
        if (UserValidator.isNullorBlank(u)) {
            return Response.status(400).entity(JsonUtils.convertObjectToJson(new ResponseMessage("One or more parameters are null or blank"))).build();
        }

        // Validate email format
        if (!UserValidator.isValidEmail(u.getEmail())) {
            return Response.status(400).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Invalid email format"))).build();
        }

        // Validate phone number format
        if (!UserValidator.isValidPhoneNumber(u.getPhone())) {
            return Response.status(400).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Invalid phone number format"))).build();
        }

        // Validate URL format
        if (!UserValidator.isValidURL(u.getPhotoURL())) {
            return Response.status(400).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Invalid URL format"))).build();
        }

        // Check if username or email already exists
        if (userBean.userExists(u)) {
            return Response.status(409).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Invalid Username or Email"))).build();
        }

        // Check if the user is a PO & if the token is valid and create the new user
        String userToken="";
        userToken = token;
        if(userToken!=null && roleNewUser != null && tokenBean.isValidUserByToken(userToken)){
            String role = tokenBean.getUserByToken(userToken).getRole();
            if(role.equals("po")){
                userBean.addUserPO(u, roleNewUser);
                return Response.status(200).entity(JsonUtils.convertObjectToJson(new ResponseMessage("A new user is created")).toString()).build();
            }

        }else{
        // If all checks pass, add the user
            userBean.addUser(u);
            return Response.status(200).entity(JsonUtils.convertObjectToJson(new ResponseMessage("A new user is created"))).build();
        }
        return Response.status(401).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Unauthorized")).toString()).build();
    }


    //Service that manages the login of the user, sets the token for the user and sends the token and the role of the user
    @POST
    @Path("/login")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response login(@HeaderParam("username") String username, @HeaderParam("password") String password) {
        String token = tokenBean.login(username, password);
        if (token != null) {
            if(userBean.getUserByUsername(username).isActive()){
                System.out.println("User is active");
                return Response.status(200).entity(JsonUtils.convertObjectToJson(new TokenAndRoleDto(token, userBean.getUserByUsername(username).getRole(), tokenBean.getUserByToken(token).getUsername()))).build();
            }else{
                return Response.status(403).entity(JsonUtils.convertObjectToJson(new ResponseMessage("User is not active")).toString()).build();
            }
        }
        return Response.status(401).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Login Failed"))).build();
    }

    @POST
    @Path("/logout")
    @Produces(MediaType.APPLICATION_JSON)
    public Response logout(@HeaderParam("token") String token) {
        if (tokenBean.isValidUserByToken(token)) {
            tokenBean.logout(token);
            return Response.status(200).entity(JsonUtils.convertObjectToJson(new ResponseMessage("User is logged out")).toString()).build();
        }
        return Response.status(401).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Unauthorized")).toString()).build();
    }

    @GET
//    @Path("/all")
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllUsers(@HeaderParam("token") String token) {

        if (token == null || token.isEmpty()) {
            return Response.status(400).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Invalid token"))).build();
        }

        if (!tokenBean.isValidUserByToken(token)) {
            return Response.status(401).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Unauthorized"))).build();
        }

        List<UserDto> userDtos = userBean.getAllUsersDB();

        if (userDtos == null || userDtos.isEmpty()) {
            return Response.status(404).entity(JsonUtils.convertObjectToJson(new ResponseMessage("No users found"))).build();
        }

        return Response.status(200).entity(userDtos).build();
    }
    //Service that receives the token to validate and sends the userPartialDto object
    @GET
//    @Path("/getPartial")
    @Path("{username}/partial")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUserPartial(@HeaderParam("token") String token, @PathParam("username") String username) {
        if (tokenBean.isValidUserByToken(token)) {
            UserDto userDto = tokenBean.getUserByToken(token);
            UserPartialDto userPartialDTO = userBean.mapUserToUserPartialDTO(userDto);
            return Response.status(200).entity(userPartialDTO).build();
        } else {
            return Response.status(401).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Unauthorized"))).build();
        }
    }

    //Service that receives a token and a username and sends the photoURL of the usersame
    @GET
    @Path("{username}/photo")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPhoto(@HeaderParam("token") String token, @PathParam("username") String username) {
        if (tokenBean.isValidUserByToken(token)) {
            UserDto userDto = userBean.getUserByUsername(username);
            return Response.status(200).entity(JsonUtils.convertObjectToJson((userDto.getPhotoURL()))).build();
        } else {
            return Response.status(401).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Unauthorized"))).build();
        }
    }

    //Service that receives the token and sends only the users that ownes the tasks in the database mysql
    @GET
    @Path("/owners") //users that own tasks
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUsersOwners(@HeaderParam("token") String token) {
        if (tokenBean.isValidUserByToken(token)) {
            List<UserDto> userDtos = userBean.getUsersOwners();
            if (userDtos == null || userDtos.isEmpty()) {
                return Response.status(404).entity(JsonUtils.convertObjectToJson(new ResponseMessage("No users found"))).build();
            }
            return Response.status(200).entity(userDtos).build();
        } else {
            return Response.status(401).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Unauthorized"))).build();
        }
    }

    //Service that receives the token, role of user, and task id and sends if the user has permission to edit the task
    @GET
    @Path("/{username}/permissions/{taskId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response hasPermissionToEdit(@HeaderParam("token") String token, @PathParam("username") String username, @PathParam("taskId") int taskId) {
        if (tokenBean.isValidUserByToken(token)) {
            if (tokenBean.hasPermissionToEdit(token, taskId)) {
                return Response.status(200).entity(JsonUtils.convertObjectToJson(new ResponseMessage("User has permission to edit"))).build();
            } else {
                return Response.status(401).entity(JsonUtils.convertObjectToJson(new ResponseMessage("You dont have permission to edit this task."))).build();
            }

        }
        return Response.status(401).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Unauthorized"))).build();
    }

    //Service that receives username and password and sends the user object without the password
    @GET
    @Path("/{selectedUser}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUserDetails(@HeaderParam("token") String token, @PathParam("selectedUser") String selectedUser) {
//        ~
        if (!tokenBean.isValidUserByToken(token)) {
            return Response.status(401).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Unauthorized"))).build();

        } else if (tokenBean.isValidUserByToken(token) && tokenBean.getUserByToken(token).getRole().equals("po") || tokenBean.getUserByToken(token).getUsername().equals(selectedUser)) {
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
        }}
    @PUT
    @Path("/{selectedUser}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateUser(UserUpdateDto u, @HeaderParam("token") String token, @PathParam("selectedUser") String selectedUser) {
        if (!tokenBean.isValidUserByToken(token)) {
            return Response.status(401).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Unauthorized"))).build();
        } else if (tokenBean.isValidUserByToken(token) && tokenBean.getUserByToken(token).getRole().equals("po") || tokenBean.getUserByToken(token).getUsername().equals(selectedUser)) {
            if (!UserValidator.isValidEmail(u.getEmail())) {
                return Response.status(400).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Invalid email format"))).build();
            } else if (!u.getEmail().equals(userBean.getUserByUsername(selectedUser).getEmail()) && UserValidator.emailExists(userBean.getAllUsersDB(),u.getEmail())
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
        if (!tokenBean.isValidUserByToken(token)) {
            return Response.status(401).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Unauthorized"))).build();
        }else if(tokenBean.isValidUserByToken(token)){
            boolean updateTry = userBean.updatePassword(u, token);
            if(!updateTry){
                return Response.status(400).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Old password is incorrect"))).build();
            }else{
                return Response.status(200).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Password is updated")).toString()).build();
            }
        }
        return Response .status(400).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Invalid Parameters")).toString()).build();
    }

    @PUT
    @Path("/{username}/status")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response changeStatus(@HeaderParam("token") String token, @PathParam("username") String username, UserStatusUpdateDto userStatusUpdateDto) {
        if(!tokenBean.isValidUserByToken(token) || !tokenBean.getUserByToken(token).getRole().equals("po")){
            return Response.status(401).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Unauthorized"))).build();
        } else if(tokenBean.getUserByToken(token).getRole().equals("po")){
            if(userBean.changeStatus(username, userStatusUpdateDto.isActive())){
                return Response.status(200).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Status changed")).toString()).build();
            } else {
                return Response.status(400).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Status not changed")).toString()).build();
            }
        }
        return Response.status(400).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Invalid Parameters")).toString()).build();
    }


    @DELETE
    @Path("/{selectedUser}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteUser(@HeaderParam("token") String token, @PathParam("selectedUser") String selectedUser) {
        if(!tokenBean.isValidUserByToken(token) && !tokenBean.getUserByToken(token).getRole().equals("po")){
            return Response.status(401).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Unauthorized"))).build();
        }else if(tokenBean.getUserByToken(token).getRole().equals("po")){
            if(userBean.deleteUser(selectedUser)){
                return Response.status(200).entity(JsonUtils.convertObjectToJson(new ResponseMessage("User deleted")).toString()).build();
            }else{
                return Response.status(400).entity(JsonUtils.convertObjectToJson(new ResponseMessage("User not deleted")).toString()).build();
            }
        }
        return Response .status(400).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Invalid Parameters")).toString()).build();
    }

    //Delete all tasks of a user
    @DELETE
    @Path("/{selectedUser}/tasks")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteTasks(@HeaderParam("token") String token, @PathParam("selectedUser") String selectedUser) {
        if(!tokenBean.isValidUserByToken(token) && !tokenBean.getUserByToken(token).getRole().equals("po")){
            return Response.status(401).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Unauthorized"))).build();
        }else if(tokenBean.getUserByToken(token).getRole().equals("po")){
            if(userBean.deleteTasks(selectedUser)){
                return Response.status(200).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Tasks deleted")).toString()).build();
            }else{
                return Response.status(400).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Tasks not deleted")).toString()).build();
            }
        }
        return Response .status(400).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Invalid Parameters")).toString()).build();
    }

//    @Path("/email/send")
//    @POST
//    @Consumes(MediaType.APPLICATION_JSON)
//    @Produces(MediaType.TEXT_PLAIN)
//    public Response sendEmail(EmailRequest emailRequest) {
//        emailSender.sendEmail(emailRequest.getTo(), emailRequest.getSubject(), emailRequest.getContent());
//        return Response.ok("Email sent successfully").build();
//    }
}



