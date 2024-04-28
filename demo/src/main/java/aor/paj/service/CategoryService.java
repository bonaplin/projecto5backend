package aor.paj.service;

import aor.paj.bean.CategoryBean;
import aor.paj.bean.TokenBean;
import aor.paj.bean.UserBean;
import aor.paj.dto.CategoryDto;
import aor.paj.dto.UserDto;
import aor.paj.responses.ResponseMessage;
import aor.paj.utils.JsonUtils;
import aor.paj.utils.TokenStatus;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import aor.paj.bean.Log;


@Path("/categories")
public class CategoryService {

    @Inject
    UserBean userBean;

    @Inject
    CategoryBean categoryBean;

    @Inject
    TokenBean tokenBean;
    //Service that gets all categories from database

    @Inject
    Log log;

    @GET
    @Path("/")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCategories(@HeaderParam("token") String token) {
        TokenStatus tokenStatus = tokenBean.isValidUserByToken(token);
        if (tokenStatus != TokenStatus.VALID) {
            log.logUserInfo(token,  "Unauthorized to get categories", 3);
            return Response.status(403).entity(JsonUtils.convertObjectToJson(new ResponseMessage(tokenStatus.getMessage()))).build();
        }
        return Response.status(200).entity(categoryBean.getAllCategories()).build();
    }

    @DELETE
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteCategory(@HeaderParam("token") String token, @PathParam("id") int id) {
        TokenStatus tokenStatus = tokenBean.isValidUserByToken(token);
        if (tokenStatus != TokenStatus.VALID || !tokenBean.getUserRole(token).equals("po")) {
        return Response.status(403).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Unauthorized"))).build();
    }

    if (categoryBean.deleteCategory(id)) {
        return Response.status(200).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Category deleted"))).build();
    } else {
        log.logUserInfo(token,  "Failed to delete category", 3);
        return Response.status(400).entity(JsonUtils.convertObjectToJson(new ResponseMessage("There are tasks with this category. Delete these tasks before deleting the category."))).build();
    }
}
    @POST
    @Path("/")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addCategory(@HeaderParam("token") String token, CategoryDto category) {
        TokenStatus tokenStatus = tokenBean.isValidUserByToken(token);
        if (tokenStatus != TokenStatus.VALID || !tokenBean.getUserRole(token).equals("po")) {
            return Response.status(403).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Unauthorized"))).build();
        }

        if (categoryBean.isValidCategory(category) && categoryBean.addCategory(category, token)) {
            return Response.status(200).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Category added"))).build();
        } else {
            log.logUserInfo(token, "Failed to add category", 3);
            return Response.status(400).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Invalid category"))).build();
        }
    }
//    @PUT
//    @Path("/")
//    @Consumes(MediaType.APPLICATION_JSON)
//    @Produces(MediaType.APPLICATION_JSON)
//    public Response updateCategory(@HeaderParam("token") String token, CategoryDto category, @QueryParam("title") String title) {
//        TokenStatus tokenStatus = tokenBean.isValidUserByToken(token);
//        if (tokenStatus != TokenStatus.VALID) {
//            return Response.status(403).entity(JsonUtils.convertObjectToJson(new ResponseMessage(tokenStatus.getMessage()))).build();
//        }
//
//        if (!tokenBean.getUserRole(token).equals("po")) {
//            log.logUserInfo(token,  "Unauthorized to update category with title: "+title, 3);
//            return Response.status(401).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Unauthorized"))).build();
//        }
//
//        if (!categoryBean.isValidCategoryUpdate(category, title)) {
//            return Response.status(400).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Invalid category"))).build();
//        }
//
//        if (categoryBean.updateCategory(category, title, token)) {
//            log.logUserInfo(token,  "Category updated with "+category.getId()+" title.", 1);
//            return Response.status(200).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Category updated"))).build();
//        } else {
//            return Response.status(400).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Failed to update category with"+category.getTitle()+" title."))).build();
//        }
//    }
    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateCategory(@HeaderParam("token") String token, CategoryDto category, @PathParam("id") int id) {
        TokenStatus tokenStatus = tokenBean.isValidUserByToken(token);
        if (tokenStatus != TokenStatus.VALID || !tokenBean.getUserRole(token).equals("po")) {
            log.logUserInfo(token,  "Unauthorized to update category", 3);
            return Response.status(403).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Unauthorized"))).build();
        }
        if (categoryBean.isValidCategory(category) && categoryBean.updateCategory(category, id, token)) {
            return Response.status(200).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Category updated with "+category.getTitle()+" title."))).build();
        } else {
            return Response.status(400).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Failed to update category"))).build();
        }
    }

}
