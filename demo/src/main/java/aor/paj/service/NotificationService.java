package aor.paj.service;

import aor.paj.bean.NotificationBean;
import aor.paj.bean.TokenBean;
import aor.paj.dto.NotificationDto;
import aor.paj.responses.ResponseMessage;
import aor.paj.utils.JsonUtils;
import aor.paj.utils.TokenStatus;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;

import java.util.List;

@Path("/notifications")
public class NotificationService {
        @Inject
        NotificationBean notificationBean;
        @Inject
        TokenBean tokenBean;

        @POST
        @Path("/")
        public void sendNotification(NotificationDto notificationDto){
            notificationBean.sendNotification(notificationDto);
            System.out.println("Notification send/: " + notificationDto);
        }

        @GET
        @Path("/")
        @Produces("application/json")
        public Response getNotifications(@HeaderParam("token") String token){
            TokenStatus tokenStatus = tokenBean.isValidUserByToken(token);
            if(tokenStatus != TokenStatus.VALID){
                return Response.status(Response.Status.UNAUTHORIZED).entity(tokenStatus.getMessage()).build();
            }
            List<NotificationDto> notifications = notificationBean.getAllNotifications();
            System.out.println("Notifications get/: " + notifications);
            return Response.status(200).entity(notifications).build();
        }

}
