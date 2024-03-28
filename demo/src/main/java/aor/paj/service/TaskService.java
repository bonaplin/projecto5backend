package aor.paj.service;

import aor.paj.bean.TaskBean;
import aor.paj.bean.UserBean;
import aor.paj.dto.StatusUpdate;
import aor.paj.dto.TaskDto;
import aor.paj.entity.TaskEntity;
import aor.paj.responses.ResponseMessage;
import aor.paj.utils.JsonUtils;
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

    //Service that receives a taskdto and a token and creates a new task with the user in token and adds the task to the task table in the database mysql
    @POST
    @Path("/")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addTask(@HeaderParam("token") String token, TaskDto t) {
        if (userBean.isValidUserByToken(token)) {
            if (TaskValidator.isValidTask(t) && !taskBean.taskTitleExists(t)) {
                if (taskBean.addTask(token, t)) {
                    return Response.status(200).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Task is added"))).build();
                } else {
                    return Response.status(400).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Cannot add task"))).build();
                }
            } else {
                return Response.status(400).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Verify the fields. Title is unique"))).build();
            }
        } else {
            return Response.status(401).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Unauthorized"))).build();
        }
    }
    @GET
    @Path("/")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getTasks(@HeaderParam("token") String token, @QueryParam("category") String category, @QueryParam("username") String username, @QueryParam("active") Boolean active, @QueryParam("id") Integer id) {
        if (userBean.isValidUserByToken(token)) {
            if (id != null) {
                return Response.status(200).entity(taskBean.getTaskById(id)).build();
            } else {
                List<TaskDto> tasks;
                if (category != null && !category.isEmpty() && username != null && !username.isEmpty()) {
                    tasks = taskBean.getTasksByCategoryAndOwner(category, username);
                } else if (category != null && !category.isEmpty()) {
                    tasks = taskBean.getTasksByCategory(category);
                } else if (username != null && !username.isEmpty()) {
                    tasks = taskBean.getTasksByOwner(username);
                } else if (active != null) {
                    if (active) {
                        tasks = taskBean.getActiveTasks();
                    } else {
                        tasks = taskBean.getInactiveTasks();
                    }
                } else {
                    tasks = taskBean.getActiveTasks();
                }
                return Response.status(200).entity(tasks).build();
            }
        } else {
            return Response.status(401).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Unauthorized"))).build();
        }
    }
    @PUT
    @Path("/{id}/status")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateTaskStatus(@HeaderParam("token") String token, @PathParam("id") int id, StatusUpdate statusUpdate) {
        int status = statusUpdate.getStatus();
        if (userBean.isValidUserByToken(token) && TaskValidator.isValidStatus(status)) {
            taskBean.updateTaskStatus(id, status);
            return Response.status(200).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Task status is updated"))).build();
        } else {
            return Response.status(400).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Invalid status"))).build();
        }
    }
    @PUT
    @Path("/{id}/desactivate")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response desactivateTask(@HeaderParam("token") String token, @PathParam("id") int id) {
        if (userBean.isValidUserByToken(token)) {
            String role = userBean.getUserRole(token);
            if (taskBean.taskBelongsToUser(token, id) || role.equals("sm") || role.equals("po")){
                if (taskBean.desactivateTask(id)) {
                    return Response.status(200).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Task is desactivated"))).build();
                } else {
                    return Response.status(400).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Cannot desactivate task"))).build();
                }
            } else if (!role.equals("dev")) {
                if (taskBean.desactivateTask(id)) {
                    return Response.status(200).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Task is desactivated"))).build();
                } else {
                    return Response.status(400).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Cannot desactivate task"))).build();
                }
            } else {
                return Response.status(403).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Forbidden"))).build();
            }
        } else {
            return Response.status(401).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Unauthorized"))).build();
        }
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
        if (userBean.isValidUserByToken(token)) {
            if(userBean.hasPermissionToEdit(token, id)){
                if (TaskValidator.isValidTaskEdit(t)) {
                    taskBean.updateTask(t, id);
                    return Response.status(200).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Task is updated"))).build();
                } else {
                    return Response.status(400).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Verify your fields. Title is unique"))).build();
                }
            } else {
                return Response.status(403).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Forbidden"))).build();
            }
        } else {
            return Response.status(401).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Unauthorized"))).build();
        }
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
        if (userBean.isValidUserByToken(token)) {
            String role = userBean.getUserRole(token);
            if (role.equals("po")) {
                if (taskBean.deleteTask(id)) {
                    return Response.status(200).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Task is deleted"))).build();
                } else {
                    return Response.status(400).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Cannot delete task"))).build();
                }
            } else {
                return Response.status(403).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Forbidden"))).build();
            }
        } else {
            return Response.status(401).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Unauthorized"))).build();
        }
    }
    //Service that receives a token, checks if the user is valid, checks if user role = sm or po, and restore all tasks
    @PUT
    @Path("/restore")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response restoreAllTasks(@HeaderParam("token") String token) {
        if (userBean.isValidUserByToken(token)) {
            String role = userBean.getUserRole(token);
            if (role.equals("sm") || role.equals("po")) {
                if (taskBean.restoreAllTasks()) {
                    return Response.status(200).entity(JsonUtils.convertObjectToJson(new ResponseMessage("All tasks are restored"))).build();
                } else {
                    return Response.status(400).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Cannot restore all tasks"))).build();
                }
            } else {
                return Response.status(403).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Forbidden"))).build();
            }
        } else {
            return Response.status(401).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Unauthorized"))).build();
        }
    }
    @PUT
    @Path("/{id}/restore")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response restoreTask(@HeaderParam("token") String token, @PathParam("id") int id) {
        if (userBean.isValidUserByToken(token)) {
            String role = userBean.getUserRole(token);
            if (role.equals("sm") || role.equals("po")) {
                if (taskBean.restoreTask(id)) {
                    return Response.status(200).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Task is restored"))).build();
                } else {
                    return Response.status(400).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Cannot restore task"))).build();
                }
            } else {
                return Response.status(403).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Forbidden"))).build();
            }
        } else {
            return Response.status(401).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Unauthorized"))).build();
        }
    }

    //Service that receives a token, checks if the user is valid, checks if user role = po, and deletes all tasks
    @DELETE
    @Path("/")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteAllTasks(@HeaderParam("token") String token) {
        if (userBean.isValidUserByToken(token)) {
            String role = userBean.getUserRole(token);
            if (role.equals("po")) {
                if (taskBean.deleteAllTasks()) {
                    return Response.status(200).entity(JsonUtils.convertObjectToJson(new ResponseMessage("All tasks are deleted"))).build();
                } else {
                    return Response.status(400).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Cannot delete all tasks"))).build();
                }
            } else {
                return Response.status(403).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Forbidden"))).build();
            }
        } else {
            return Response.status(401).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Unauthorized"))).build();
        }
    }
}
