package aor.paj.websocket.bean;

import aor.paj.bean.MessageBean;
import aor.paj.bean.UserBean;
import aor.paj.dao.MessageDao;
import aor.paj.dao.TokenDao;
import aor.paj.dto.MessageDto;
import aor.paj.entity.MessageEntity;
import aor.paj.gson.InstantAdapter;
//import aor.paj.websocket.dto.MessageSocketDto;
import aor.paj.utils.MessageType;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.websocket.Session;

import java.time.Instant;

@Stateless
public class HandleWebSockets {

    @EJB
    MessageBean messageBean;
    @EJB
    UserBean userBean;
    @EJB
    MessageDao messageDao;
    @EJB
    TokenDao tokenDao;

    Gson gson = new GsonBuilder()
            .registerTypeAdapter(Instant.class, new InstantAdapter())
            .create();

    public void handleWebSocketJSON(Session session, String json) {
        System.out.println("Handling WebSocket JSON: " + json);
        JsonObject jsonObject = gson.fromJson(json, JsonObject.class);
        int typeValue = jsonObject.get("type").getAsInt();

        MessageType messageType = MessageType.fromValue(typeValue);

        switch (messageType) {
            case TYPE_10:
                System.out.println("Type 10");
                handleNewMessage(session, jsonObject);
                break;
            case TYPE_20:
                System.out.println("Type 20");
                //converte para Dto de notificiação e faz algo
                break;
            case LOGOUT:
                System.out.println("Type 30 - Logout");
                //converte para Dto de task e faz algo
                break;
            case TYPE_40:
                System.out.println("Type 30");
                //converte para Dto de logout e faz algo
                break;
            default:
                System.out.println("Unknown type");
        }
    }

    private void handleNewMessage(Session session, JsonObject jsonObject) {
        try{
            String messageContent = jsonObject.get("message").getAsString();
            String receiver = jsonObject.get("receiver").getAsString();

            String token = session.getPathParameters().get("token");
            String sender = tokenDao.findUserByTokenString(token).getUsername();

            // Cria um novo objeto MessageDto
            MessageDto messageDto = new MessageDto();
            messageDto.setMessage(messageContent);
            messageDto.setSender(sender);
            messageDto.setReceiver(receiver);

            // Grava a mensagem na base de dados
            MessageEntity messageEntity = messageBean.convertMessageDtoToMessageEntity(messageDto);
            messageDao.persist(messageEntity);

            // Cria um novo objeto JSON para enviar a mensagem
            jsonObject.addProperty("time", messageEntity.getTime().toString());
            jsonObject.addProperty("sender", messageEntity.getSender_id().getUsername());

            session.getBasicRemote().sendText(jsonObject.toString());
            messageBean.sendToUser(receiver, jsonObject.toString());

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Something went wrong!");
        }

    }
}
