package aor.paj.websocket.bean;

import aor.paj.bean.MessageBean;
import aor.paj.bean.UserBean;
import aor.paj.dao.MessageDao;
import aor.paj.dao.TokenDao;
import aor.paj.dao.UserDao;
import aor.paj.dto.MessageDto;
import aor.paj.entity.MessageEntity;
import aor.paj.gson.InstantAdapter;
import aor.paj.websocket.dto.MessageSocketDto;
import com.fasterxml.jackson.datatype.jsr310.ser.InstantSerializer;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.json.Json;
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
        int type = jsonObject.get("type").getAsInt();

        switch (type) {
            case 10:
                System.out.println("Type 10");
                handletype10(session, jsonObject);
                break;
            case 20:
                System.out.println("Type 20");
                //converte para Dto de notificiação e faz algo
                break;
            case 30:
                System.out.println("Type 30");
                //converte para Dto de task e faz algo
                break;
            default:
                System.out.println("Unknown type");
        }
    }

    private void handletype10(Session session, JsonObject jsonObject) {
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
