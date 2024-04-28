package aor.paj.service;

import aor.paj.bean.Log;
import aor.paj.bean.MessageBean;
import aor.paj.bean.TokenBean;
import aor.paj.bean.UserBean;
import aor.paj.dto.MessageDto;
import aor.paj.dto.UserDto;
import aor.paj.responses.ResponseMessage;
import aor.paj.utils.JsonUtils;
import aor.paj.utils.TokenStatus;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.ThreadContext;

import java.util.List;

@Path("/messages")
public class MessageService {

    @Inject
    MessageBean messageBean;
    @Inject
    TokenBean tokenBean;
    @Inject
    Log log;

    @POST
    @Path("/")
    public Response sendMessage(MessageDto messageDto, @HeaderParam("token") String token){
        if(token == null) return Response.status(401).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Tem de fazer login 1º."))).build();

        UserDto userDto = tokenBean.getUserByToken(token);
        if(userDto == null) return Response.status(401).entity(JsonUtils.convertObjectToJson(new ResponseMessage("User não encontrado."))).build();

        log.logUserInfo(token, "Mensagem enviada para "+messageDto.getReceiver(), 1);
        return Response.ok().entity(JsonUtils.convertObjectToJson(new ResponseMessage("Mensagem enviada."))).build();
    }

    @GET
    @Path("/{sender}/{receiver}")
    public Response getMessages(@PathParam("sender") String sender, @PathParam("receiver") String receiver, @HeaderParam("token") String token){
        TokenStatus tokenStatus = tokenBean.isValidUserByToken(token);
        if(tokenStatus != TokenStatus.VALID) return Response.status(401).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Tem de fazer login 1º."))).build();

        List<MessageDto> messages = messageBean.getMessagesBetweenUsers(sender, receiver, token);
        if(messages == null) return Response.status(404).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Mensagens não encontradas."))).build();

        if(tokenBean.getUserByToken(token).getUsername().equals(sender) || tokenBean.getUserByToken(token).getUsername().equals(receiver)) {
            log.logUserInfo(token, "Mensagens entre " + sender + " e " + receiver + " encontradas.", 1);
            return Response.ok().entity(JsonUtils.convertObjectToJson(messages)).build();
        } else {
            log.logUserInfo(token, "Não tem permissão para aceder a estas mensagens.", 2);
            return Response.status(403).entity(JsonUtils.convertObjectToJson(new ResponseMessage("Não tem permissão para aceder a estas mensagens."))).build();
        }

    }


}
