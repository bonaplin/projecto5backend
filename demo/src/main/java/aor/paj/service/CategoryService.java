package aor.paj.service;

import aor.paj.bean.CategoryBean;
import aor.paj.bean.UserBean;
import aor.paj.dto.CategoryDto;
import aor.paj.responses.ResponseMessage;
import aor.paj.utils.JsonUtils;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/categories")
public class CategoryService {

    @Inject
    UserBean userBean;

    @Inject
    CategoryBean categoryBean;

    //Service that gets all categories from database
    @GET
    @Path("/")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCategories(@HeaderParam("token") String token) {
        if (userBean.isValidUserByToken(token)) {
            return Response.status(200).entity(categoryBean.getAllCategories()).build();
        } else {
            return Response.status(401).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Unauthorized"))).build();
        }
    }
//    //Service that sends the categorys of tasks that are active in the database mysql
//    @GET
//    @Path("/active")
//    @Consumes(MediaType.APPLICATION_JSON)
//    @Produces(MediaType.APPLICATION_JSON)
//    public Response getActiveCategories(@HeaderParam("token") String token) {
//        if (userBean.isValidUserByToken(token)) {
//            return Response.status(200).entity(categoryBean.getActiveCategories()).build();
//        } else {
//            return Response.status(401).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Unauthorized"))).build();
//        }
//    }
//    @DELETE
//    @Path("/delete")
//    @Consumes(MediaType.APPLICATION_JSON)
//    @Produces(MediaType.APPLICATION_JSON)
//    public Response deleteCategory(@HeaderParam("token") String token, @QueryParam("title") String title) {
//        if (userBean.isValidUserByToken(token)) {
//            if (userBean.getUserRole(token).equals("po")) {
//                if (categoryBean.deleteCategory(title)) {
//                    return Response.status(200).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Category deleted"))).build();
//                } else {
//                    return Response.status(400).entity(JsonUtils.convertObjectToJson(new ResponseMessage("There are tasks with this category. Delete this tasks before deleting the category."))).build();
//                }
//            } else {
//                return Response.status(401).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Unauthorized"))).build();
//            }
//        } else {
//            return Response.status(401).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Unauthorized"))).build();
//        }
//    }
    @DELETE
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteCategory(@HeaderParam("token") String token, @PathParam("id") int id) {
        if (userBean.isValidUserByToken(token)) {
            if (userBean.getUserRole(token).equals("po")) {
                if (categoryBean.deleteCategory(id)) {
                    return Response.status(200).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Category deleted"))).build();
                } else {
                    return Response.status(400).entity(JsonUtils.convertObjectToJson(new ResponseMessage("There are tasks with this category. Delete these tasks before deleting the category."))).build();
                }
            } else {
                return Response.status(401).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Unauthorized"))).build();
            }
        } else {
            return Response.status(401).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Unauthorized"))).build();
        }
    }
    @POST
    @Path("/")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addCategory(@HeaderParam("token") String token, CategoryDto category) {
        if (userBean.isValidUserByToken(token)) {
            if (userBean.getUserRole(token).equals("po")) {
                if (categoryBean.isValidCategory(category)) {
                    if (categoryBean.addCategory(category)) {
                        return Response.status(200).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Category added"))).build();
                    }
                } else
                    return Response.status(400).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Invalid category"))).build();
            } else {
                return Response.status(401).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Unauthorized"))).build();
            }
        } else {
            return Response.status(401).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Unauthorized"))).build();
        }
        return Response.status(401).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Unauthorized"))).build();
    }
    @PUT
    @Path("/")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateCategory(@HeaderParam("token") String token, CategoryDto
            category, @QueryParam("title") String title) {
        if (userBean.isValidUserByToken(token)) {
            if (userBean.getUserRole(token).equals("po")) {
                if (categoryBean.isValidCategoryUpdate(category, title)) {
                    if (categoryBean.updateCategory(category, title)) {
                        return Response.status(200).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Category updated"))).build();
                    }
                } else
                    return Response.status(400).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Invalid category"))).build();
            } else {
                return Response.status(401).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Unauthorized"))).build();
            }
        } else {
            return Response.status(401).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Unauthorized"))).build();
        }
        return Response.status(401).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Unauthorized"))).build();
    }
    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateCategory(@HeaderParam("token") String token, CategoryDto category, @PathParam("id") int id) {
        if (userBean.isValidUserByToken(token)) {
            if (userBean.getUserRole(token).equals("po")) {
                if (categoryBean.isValidCategory(category)) {
                    if (categoryBean.updateCategory(category, id)) {
                        return Response.status(200).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Category updated"))).build();
                    } else {
                        return Response.status(400).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Failed to update category"))).build();
                    }
                } else {
                    return Response.status(400).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Invalid category"))).build();
                }
            } else {
                return Response.status(401).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Unauthorized"))).build();
            }
        } else {
            return Response.status(401).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Unauthorized"))).build();
        }
    }
//    @GET
//    @Path("/tasksNumber")
//    @Consumes(MediaType.APPLICATION_JSON)
//    @Produces(MediaType.APPLICATION_JSON)
//    public Response getTasksByCategory(@HeaderParam("token") String token, @QueryParam("title") String title) {
//        if (userBean.isValidUserByToken(token)) {
//            if (userBean.getUserRole(token).equals("po")) {
//                return Response.status(200).entity(categoryBean.getNumberOfTasksByCategory(title)).build();
//            } else {
//                return Response.status(401).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Unauthorized"))).build();
//            }
//        } else {
//            return Response.status(401).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Unauthorized"))).build();
//        }
//    }

}
