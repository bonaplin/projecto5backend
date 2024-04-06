package aor.paj.service;

import aor.paj.bean.MessageBean;
import aor.paj.dto.MessageDto;
import jakarta.inject.Inject;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;

@Path("/messages")
public class MessageService {

    @Inject
    MessageBean messageBean;


    @POST
    @Path("/")
    public void sendMessage(MessageDto messageDto){
        messageBean.sendMessage(messageDto);
        System.out.println("Message send/: " + messageDto.getMessage());
    }
}
