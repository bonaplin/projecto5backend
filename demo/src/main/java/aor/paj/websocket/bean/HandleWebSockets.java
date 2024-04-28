package aor.paj.websocket.bean;

import aor.paj.bean.MessageBean;
import aor.paj.bean.NotificationBean;
import aor.paj.bean.TaskBean;
import aor.paj.bean.UserBean;
import aor.paj.dao.MessageDao;
import aor.paj.dao.NotificationDao;
import aor.paj.dao.TokenDao;
import aor.paj.dto.MessageDto;
import aor.paj.dto.NotificationDto;
import aor.paj.entity.MessageEntity;
import aor.paj.entity.NotificationEntity;
import aor.paj.entity.UserEntity;
import aor.paj.gson.InstantAdapter;
//import aor.paj.websocket.dto.MessageSocketDto;
import aor.paj.utils.MessageType;
import aor.paj.websocket.Notifier;
import com.google.gson.*;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.websocket.EncodeException;
import jakarta.websocket.Session;

import javax.management.Notification;
import java.awt.*;
import java.io.IOException;
import java.time.Instant;

@Stateless
public class HandleWebSockets {

    @EJB
    MessageBean messageBean;
    @EJB
    UserBean userBean;
    @EJB
    TaskBean taskBean;
    @EJB
    MessageDao messageDao;
    @EJB
    TokenDao tokenDao;
    @EJB
    NotificationBean notificationBean;
    @EJB
    NotificationDao notificationDao;

    Gson gson = new GsonBuilder()
            .registerTypeAdapter(Instant.class, new InstantAdapter())
            .create();

    public void handleWebSocketJSON(Session session, String json) {
        JsonObject jsonObject = gson.fromJson(json, JsonObject.class);
        int typeValue = jsonObject.get("type").getAsInt();

        MessageType messageType = MessageType.fromValue(typeValue);

        switch (messageType) {
            case MESSAGE_RECEIVER:
                handleNewMessage(session, jsonObject);
                break;
            case TASK_MOVE:
                taskBean.handleTaskMove(session, jsonObject);
                break;
            case MESSAGE_READ_CONFIRMATION:
                handleReadConfirmation(session, jsonObject);
                break;
            default:
                System.out.println("Unknown type");
        }
    }

    public JsonObject convertStringToJsonObject(String jsonString) {
        return gson.fromJson(jsonString, JsonObject.class);
    }

    //MESSAGE -     - MESSAGE -     - MESSAGE -     - MESSAGE -     - MESSAGE
    private void handleNewMessage(Session session, JsonObject jsonObject) {
        try {
            String messageContent = jsonObject.get("message").getAsString();
            String receiver = jsonObject.get("receiver").getAsString();

//            String sender = findUserEntityBySession(session).getUsername();
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
            jsonObject.addProperty("id", messageEntity.getId());

            // Envia a mensagem para o destinatário
            messageBean.sendToUser(receiver, jsonObject.toString());
            sendNotify(sender, receiver, "New Message from " + sender + " at " + messageEntity.getTime().toString());

            // Envia a mensagem de volta para o remetente
            jsonObject.addProperty("type", MessageType.MESSAGE_SENDER.getValue());
            session.getBasicRemote().sendText(jsonObject.toString());

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    //MESSAGE -     - MESSAGE -     - MESSAGE -     - MESSAGE -     - MESSAGE
    //NOTIFICATION -- NOTIFICATION -- NOTIFICATION -- NOTIFICATION -- NOTIFICATION
    private void sendNotify(String sender, String receiver, String notify) {
        try {
            NotificationDto notificationDto = new NotificationDto();
            notificationDto.setReceiver(receiver);
            notificationDto.setMessage(notify);
            notificationDto.setSender(sender);

            // Grava a notificação na base de dados
            NotificationEntity notificationEntity = notificationBean.convertNotificationDtoToNotificationEntity(notificationDto);
            notificationDao.persist(notificationEntity);

            // Transforma a DTO em JSON
            String json = gson.toJson(notificationDto);

            // Cria um novo objeto JSON para enviar a notificação
            JsonObject jsonObject = gson.fromJson(json, JsonObject.class);
            jsonObject.add("time", gson.toJsonTree(notificationEntity.getTime()));
            jsonObject.add("read", gson.toJsonTree(notificationEntity.isRead()));
            jsonObject.add("type", gson.toJsonTree(MessageType.TYPE_40.getValue()));
            jsonObject.add("id", gson.toJsonTree(notificationEntity.getId()));

            messageBean.sendToUser(receiver, jsonObject.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    //NOTIFICATION -- NOTIFICATION -- NOTIFICATION -- NOTIFICATION -- NOTIFICATION
    private UserEntity findUserEntityBySession(Session session) {
        String token = session.getPathParameters().get("token");
        if (token == null) return null;
        return tokenDao.findUserByTokenString(token);
    }

    public boolean isProductOwner(String token) {
        return tokenDao.findUserByTokenString(token).getRole().equals("po");
    }

    public String convertToJsonString(Object object, MessageType messageType) {
        // Convert the original object to a JsonObject
        JsonObject jsonObject = gson.toJsonTree(object).getAsJsonObject();

        // Add the "type" property to the JsonObject
        jsonObject.addProperty("type", messageType.getValue());

        // Return the JSON representation of the modified JsonObject
        return gson.toJson(jsonObject);
    }

    public String convertListToJsonString(Object object, MessageType messageType) {
        // Convert the list to a JsonElement
        JsonElement jsonElement = gson.toJsonTree(object);

        // Create a new JsonObject to hold the array and the "type" property
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("data", jsonElement);
        jsonObject.addProperty("type", messageType.getValue());

        // Return the JSON representation of the JsonObject
        return gson.toJson(jsonObject);
    }

    public void handleReadConfirmation(Session session, JsonObject jsonObject) {
        int id = jsonObject.get("id").getAsInt();
        String token = session.getPathParameters().get("token");

        MessageEntity messageEntity = messageDao.findMessageById(id);
        if (messageEntity == null) return;

        messageEntity.setIsRead(true);
        messageDao.merge(messageEntity);

        jsonObject.addProperty("type", MessageType.MESSAGE_READ_CONFIRMATION.getValue());

        messageBean.sendToUser(messageEntity.getSender_id().getUsername(), jsonObject.toString());
    }


}
