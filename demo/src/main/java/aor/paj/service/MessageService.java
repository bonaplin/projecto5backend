package aor.paj.service;

import aor.paj.bean.MessageBean;
import aor.paj.bean.TokenBean;
import aor.paj.dto.MessageDto;
import aor.paj.dto.UserDto;
import aor.paj.entity.TokenEntity;
import aor.paj.entity.UserEntity;
import aor.paj.responses.ResponseMessage;
import aor.paj.utils.JsonUtils;
import aor.paj.websocket.Notifier;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;

import java.util.List;

@Path("/messages")
public class MessageService {

    @Inject
    MessageBean messageBean;
    @Inject
    TokenBean tokenBean;

    @POST
    @Path("/")
    public Response sendMessage(MessageDto messageDto, @HeaderParam("token") String token){
        if(token == null) return Response.status(401).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Tem de fazer login 1º."))).build();

        UserDto userDto = tokenBean.getUserByToken(token);
        if(userDto == null) return Response.status(401).entity(JsonUtils.convertObjectToJson(new ResponseMessage("User não encontrado."))).build();

        messageBean.sendMessage(messageDto, userDto.getUsername());
        return Response.ok().entity(JsonUtils.convertObjectToJson(new ResponseMessage("Mensagem enviada."))).build();
    }

    @GET
    @Path("/{receiver}")
    public List<MessageDto> getMessages(@PathParam("receiver") String receiver){
        return messageBean.getMessagesByReceiver(receiver);
    }
}