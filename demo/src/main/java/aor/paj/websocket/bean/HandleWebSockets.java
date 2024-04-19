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
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.websocket.EncodeException;
import jakarta.websocket.Session;

import javax.management.Notification;
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
//    @EJB
//    Notifier notifier;

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
            case TASK_CREATE:
//                handleCreateTask(session, jsonObject);
                System.out.println("Não é usado");
                break;
            case TASK_MOVE:
                System.out.println("Type 20 -> taskmove");
                taskBean.handleTaskMove(session, jsonObject);
                break;
            case LOGOUT:
                System.out.println("Type 30 - Logout");
                break;
            case TYPE_40:
                System.out.println("Type 30");
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
        try{
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


            // Envia a mensagem para o destinatário
            messageBean.sendToUser(receiver, jsonObject.toString());
            sendNotify(sender, receiver, "New Message from " + sender + " at "+ messageEntity.getTime().toString());

            // Envia a mensagem de volta para o remetente
            jsonObject.addProperty("type", MessageType.MESSAGE_SENDER.getValue());
            session.getBasicRemote().sendText(jsonObject.toString());

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Something went wrong!");
        }

    }
    //MESSAGE -     - MESSAGE -     - MESSAGE -     - MESSAGE -     - MESSAGE
    //NOTIFICATION -- NOTIFICATION -- NOTIFICATION -- NOTIFICATION -- NOTIFICATION
    private void sendNotify(String sender, String receiver, String notify) {
        try{
//            String receiver =  findUserEntityBySession(session).getUsername();

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
            System.out.println(jsonObject.toString());
            System.out.println("Notification sent!");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Something went wrong!");
        }

    }

    //NOTIFICATION -- NOTIFICATION -- NOTIFICATION -- NOTIFICATION -- NOTIFICATION
    private UserEntity findUserEntityBySession(Session session) {
        String token = session.getPathParameters().get("token");
        if(token == null) return null;
        return tokenDao.findUserByTokenString(token);
    }
}
