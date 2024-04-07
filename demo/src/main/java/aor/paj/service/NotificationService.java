package aor.paj.service;

import aor.paj.bean.NotificationBean;
import aor.paj.dto.NotificationDto;
import jakarta.inject.Inject;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;

    @Path("/notifications")
public class NotificationService {
        @Inject
        NotificationBean notificationBean;

        @POST
        @Path("/")
        public void sendNotification(NotificationDto notificationDto){
            notificationBean.sendNotification(notificationDto);
            System.out.println("Notification send/: " + notificationDto);
        }
}
