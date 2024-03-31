package aor.paj.service;

import aor.paj.bean.TaskBean;
import aor.paj.bean.TokenBean;
import aor.paj.bean.UserBean;
import aor.paj.dto.StatusUpdate;
import aor.paj.dto.TaskDto;
import aor.paj.entity.TaskEntity;
import aor.paj.responses.ResponseMessage;
import aor.paj.utils.JsonUtils;
import aor.paj.utils.TokenStatus;
import aor.paj.validator.TaskValidator;
import aor.paj.validator.UserValidator;
import jakarta.inject.Inject;
import jakarta.json.bind.JsonbException;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.Objects;

@Path("/tasks")
public class TaskService {
    //
    @Inject
    TaskBean taskBean;

    @Inject
    UserBean userBean;

    @Inject
    TokenBean tokenBean;

    //Service that receives a taskdto and a token and creates a new task with the user in token and adds the task to the task table in the database mysql
    @POST
    @Path("/")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addTask(@HeaderParam("token") String token, TaskDto taskDto) {
        TokenStatus tokenStatus = tokenBean.isValidUserByToken(token);
        if (tokenStatus != TokenStatus.VALID) {
            return Response.status(401).entity(JsonUtils.convertObjectToJson(new ResponseMessage(tokenStatus.getMessage()))).build();
        }

        if (!TaskValidator.isValidTask(taskDto) || taskBean.taskTitleExists(taskDto)) {
            return Response.status(400).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Verify the fields. Cannot have the same title."))).build();
        }

        if (!taskBean.addTask(token, taskDto)) {
            return Response.status(400).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Cannot add task"))).build();
        }

        return Response.status(200).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Task is added"))).build();
    }
    // Em TaskService.java
    @GET
    @Path("/")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getTasks(@HeaderParam("token") String token, @QueryParam("category") String category, @QueryParam("username") String username, @QueryParam("active") Boolean active, @QueryParam("id") Integer id) {
        TokenStatus tokenStatus = tokenBean.isValidUserByToken(token);
        if (tokenStatus != TokenStatus.VALID) {
            return Response.status(401).entity(JsonUtils.convertObjectToJson(new ResponseMessage(tokenStatus.getMessage()))).build();
        }

        if (id != null) {
            return Response.status(200).entity(taskBean.getTaskById(id)).build();
        }

        List<TaskDto> tasks = taskBean.getTasksBasedOnQueryParams(category, username, active);
        return Response.status(200).entity(tasks).build();
    }
    @PUT
    @Path("/{id}/status")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateTaskStatus(@HeaderParam("token") String token, @PathParam("id") int id, StatusUpdate statusUpdate) {
        TokenStatus tokenStatus = tokenBean.isValidUserByToken(token);
        if (tokenStatus != TokenStatus.VALID) {
            return Response.status(401).entity(JsonUtils.convertObjectToJson(new ResponseMessage(tokenStatus.getMessage()))).build();
        }

        int status = statusUpdate.getStatus();
        if (!TaskValidator.isValidStatus(status)) {
            return Response.status(400).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Invalid status"))).build();
        }

        taskBean.updateTaskStatus(id, status);
        return Response.status(200).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Task status is updated"))).build();
    }
    @PUT
    @Path("/{id}/desactivate")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response desactivateTask(@HeaderParam("token") String token, @PathParam("id") int id) {
        TokenStatus tokenStatus = tokenBean.isValidUserByToken(token);
        if (tokenStatus != TokenStatus.VALID) {
            return Response.status(401).entity(JsonUtils.convertObjectToJson(new ResponseMessage(tokenStatus.getMessage()))).build();
        }

        String role = tokenBean.getUserRole(token);
        if (!role.equals("dev") && (taskBean.taskBelongsToUser(token, id) || role.equals("sm") || role.equals("po"))) {
            if (taskBean.desactivateTask(id)) {
                return Response.status(200).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Task is desactivated"))).build();
            } else {
                return Response.status(400).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Cannot desactivate task"))).build();
            }
        }

        return Response.status(403).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Forbidden"))).build();
    }
    //Service that receives a token, a taskdto and a task id and updates the task with the id that is received
//    @PUT
//    @Path("/update")
//    @Consumes(MediaType.APPLICATION_JSON)
//    @Produces(MediaType.APPLICATION_JSON)
//    public Response updateTask(@HeaderParam("token") String token, TaskDto t, @QueryParam("id") int id) {
//        if (userBean.isValidUserByToken(token)) {
//            if(userBean.hasPermissionToEdit(token, id)){
//                if (TaskValidator.isValidTaskEdit(t)) {
//                    taskBean.updateTask(t, id);
//                    return Response.status(200).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Task is updated"))).build();
//                } else {
//                    return Response.status(400).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Invalid task"))).build();
//                }
//            } else {
//                return Response.status(403).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Forbidden"))).build();
//            }
//        } else {
//            return Response.status(401).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Unauthorized"))).build();
//        }
//    }
    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateTask(TaskDto t, @HeaderParam("token") String token, @PathParam("id") int id) {
        TokenStatus tokenStatus = tokenBean.isValidUserByToken(token);
        if (tokenStatus != TokenStatus.VALID) {
            return Response.status(401).entity(JsonUtils.convertObjectToJson(new ResponseMessage(tokenStatus.getMessage()))).build();
        }

        if (!tokenBean.hasPermissionToEdit(token, id)) {
            return Response.status(403).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Forbidden"))).build();
        }

        if (!TaskValidator.isValidTaskEdit(t)) {
            return Response.status(400).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Verify your fields. Title is unique"))).build();
        }

        if(taskBean.taskTitleExists(t)){
            return Response.status(400).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Title already exists"))).build();
        }


        taskBean.updateTask(t, id);
        return Response.status(200).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Task is updated"))).build();
    }
    //Service that receives a token and a task name, validates the token and sets the active of that task to true
//    @PUT
//    @Path("/restore")
//    @Consumes(MediaType.APPLICATION_JSON)
//    @Produces(MediaType.APPLICATION_JSON)
//    public Response restoreTask(@HeaderParam("token") String token, @QueryParam("name") String name) {
//        if (userBean.isValidUserByToken(token)) {
//            String role = userBean.getUserRole(token);
//            if (role.equals("sm") || role.equals("po")) {
//                if (taskBean.restoreTask(name)) {
//                    return Response.status(200).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Task is restored"))).build();
//                } else {
//                    return Response.status(400).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Cannot restore task"))).build();
//                }
//            } else {
//                return Response.status(403).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Forbidden"))).build();
//            }
//        } else {
//            return Response.status(401).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Unauthorized"))).build();
//        }
//    }
    //Service that receives a token and a task name, validates the token, checks if user = po, and deletes the task from the database
//    @DELETE
//    @Path("/delete")
//    @Consumes(MediaType.APPLICATION_JSON)
//    @Produces(MediaType.APPLICATION_JSON)
//    public Response removeTask(@HeaderParam("token") String token, @QueryParam("name") String name) {
//        if (userBean.isValidUserByToken(token)) {
//            String role = userBean.getUserRole(token);
//            if (role.equals("po")) {
//                if (taskBean.deleteTask(name)) {
//                    return Response.status(200).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Task is deleted"))).build();
//                } else {
//                    return Response.status(400).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Cannot delete task"))).build();
//                }
//            } else {
//                return Response.status(403).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Forbidden"))).build();
//            }
//        } else {
//            return Response.status(401).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Unauthorized"))).build();
//        }
//    }
    @DELETE
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteTask(@HeaderParam("token") String token, @PathParam ("id") int id){
        TokenStatus tokenStatus = tokenBean.isValidUserByToken(token);
        if (tokenStatus != TokenStatus.VALID) {
            return Response.status(401).entity(JsonUtils.convertObjectToJson(new ResponseMessage(tokenStatus.getMessage()))).build();
        }

        String role = tokenBean.getUserRole(token);
        if (!role.equals("po")) {
            return Response.status(403).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Forbidden"))).build();
        }

        if (!taskBean.deleteTask(id)) {
            return Response.status(400).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Cannot delete task"))).build();
        }

        return Response.status(200).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Task is deleted"))).build();
    }

    @PUT
    @Path("/restore")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response restoreAllTasks(@HeaderParam("token") String token) {
        TokenStatus tokenStatus = tokenBean.isValidUserByToken(token);
        if (tokenStatus != TokenStatus.VALID) {
            return Response.status(401).entity(JsonUtils.convertObjectToJson(new ResponseMessage(tokenStatus.getMessage()))).build();
        }

        String role = tokenBean.getUserRole(token);
        if (!role.equals("sm") && !role.equals("po")) {
            return Response.status(403).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Forbidden"))).build();
        }

        if (!taskBean.restoreAllTasks()) {
            return Response.status(400).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Cannot restore all tasks"))).build();
        }

        return Response.status(200).entity(JsonUtils.convertObjectToJson(new ResponseMessage("All tasks are restored"))).build();
    }

    @PUT
    @Path("/{id}/restore")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response restoreTask(@HeaderParam("token") String token, @PathParam("id") int id) {
        TokenStatus tokenStatus = tokenBean.isValidUserByToken(token);
        if (tokenStatus != TokenStatus.VALID) {
            return Response.status(401).entity(JsonUtils.convertObjectToJson(new ResponseMessage(tokenStatus.getMessage()))).build();
        }

        String role = tokenBean.getUserRole(token);
        if (!role.equals("sm") && !role.equals("po")) {
            return Response.status(403).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Forbidden"))).build();
        }

        if (!taskBean.restoreTask(id)) {
            return Response.status(400).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Cannot restore task"))).build();
        }

        return Response.status(200).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Task is restored"))).build();
    }

    @DELETE
    @Path("/")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteAllTasks(@HeaderParam("token") String token) {
        TokenStatus tokenStatus = tokenBean.isValidUserByToken(token);
        if (tokenStatus != TokenStatus.VALID) {
            return Response.status(401).entity(JsonUtils.convertObjectToJson(new ResponseMessage(tokenStatus.getMessage()))).build();
        }

        String role = tokenBean.getUserRole(token);
        if (!role.equals("po")) {
            return Response.status(403).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Forbidden"))).build();
        }

        if (!taskBean.deleteAllTasks()) {
            return Response.status(400).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Cannot delete all tasks"))).build();
        }

        return Response.status(200).entity(JsonUtils.convertObjectToJson(new ResponseMessage("All tasks are deleted"))).build();
    }
}
