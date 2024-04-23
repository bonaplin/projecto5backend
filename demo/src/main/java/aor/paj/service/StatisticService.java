package aor.paj.service;

import aor.paj.bean.StatisticBean;
import aor.paj.bean.TaskBean;
import aor.paj.bean.TokenBean;
import aor.paj.bean.UserBean;
import aor.paj.dto.CategoryCountDto;
import aor.paj.dto.CategoryDto;
import aor.paj.dto.RegistrationDataDto;
import aor.paj.dto.UserStatisticsDto;
import aor.paj.entity.CategoryEntity;
import aor.paj.utils.TokenStatus;
import aor.paj.websocket.Notifier;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;

import java.util.List;

@Path("/statistic")
public class StatisticService {
    @Inject
    private StatisticBean statisticBean;
    @Inject
    private Notifier notifier;
    @Inject
    private TokenBean tokenBean;
    @Inject
    private UserBean userBean;
    @Inject
    private TaskBean taskBean;

    @GET
    @Path("/user")
    @Produces("application/json")
    public Response getUserStatistics(@HeaderParam("token") String token) {
        System.out.println("Token: "+token);
        TokenStatus tokenStatus = tokenBean.isValidUserByToken(token);

        if(tokenStatus != TokenStatus.VALID) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        if(tokenBean.isProductOwner(token)){
            return Response.ok(statisticBean.getStatisticsUsers()).build();
        }
        return Response.status(Response.Status.FORBIDDEN).build();
    }

    @GET
    @Path("/tasks")
    @Produces("application/json")
    public Response getTaskStatistics(@HeaderParam("token") String token) {
        TokenStatus tokenStatus = tokenBean.isValidUserByToken(token);

        if(tokenStatus != TokenStatus.VALID) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        if(tokenBean.isProductOwner(token)){
            return Response.ok(statisticBean.getAllStatisticsFromTasks()).build();
        }
        return Response.status(Response.Status.FORBIDDEN).build();
    }

    @GET
    @Path("/userchart")
    @Produces("application/json")
    public Response getUserChart(@HeaderParam("token") String token) {
        TokenStatus tokenStatus = tokenBean.isValidUserByToken(token);

        if(tokenStatus != TokenStatus.VALID) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        if(tokenBean.isProductOwner(token)){
            List<RegistrationDataDto> registrationData = statisticBean.getRegisteredUserOverTime();
            return Response.ok(registrationData).build();
        }
        return Response.status(Response.Status.FORBIDDEN).build();
    }

    @GET
    @Path("/taskschart")
    @Produces("application/json")
    public Response getTaskChart(@HeaderParam("token") String token) {
        TokenStatus tokenStatus = tokenBean.isValidUserByToken(token);

        if(tokenStatus != TokenStatus.VALID) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        if(tokenBean.isProductOwner(token)){
            List<RegistrationDataDto> taskStatistics = statisticBean.getCompletedTasksOverTime();
            return Response.ok(taskStatistics).build();

        }
        return Response.status(Response.Status.FORBIDDEN).build();
    }

    @GET
    @Path("/orderCategories")
    @Produces("application/json")
    public Response getOrderCategories(@HeaderParam("token") String token) {
        TokenStatus tokenStatus = tokenBean.isValidUserByToken(token);

        if(tokenStatus != TokenStatus.VALID) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        if(tokenBean.isProductOwner(token)){
        List<CategoryCountDto> categories = statisticBean.getTasksPerCategory();
            return Response.ok(categories).build();
        }
        return Response.status(Response.Status.FORBIDDEN).build();
    }


}
