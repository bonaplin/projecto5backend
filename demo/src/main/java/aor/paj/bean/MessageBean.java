package aor.paj.bean;

import aor.paj.dao.MessageDao;
import aor.paj.dao.TokenDao;
import aor.paj.dao.UserDao;
import aor.paj.dto.MessageDto;
import aor.paj.websocket.dto.MessageSocketDto;
import aor.paj.entity.MessageEntity;
import aor.paj.entity.UserEntity;
import aor.paj.websocket.Notifier;
import jakarta.ejb.Stateless;
import jakarta.websocket.EncodeException;
import jakarta.websocket.Session;
import com.google.gson.Gson;
import jakarta.ejb.EJB;

import java.io.IOException;
import java.util.List;

@Stateless
public class MessageBean {

    @EJB
    UserDao userDao;
    @EJB
    MessageDao messageDao;
    @EJB
    TokenDao tokenDao;
    @EJB
    Notifier notifier;
    private Gson gson = new Gson();

    public MessageDto convertMessageEntityToMessageDto(MessageEntity messageEntity){
        MessageDto messageDto = new MessageDto();

        messageDto.setMessage(messageEntity.getMessage());
        messageDto.setSender(messageEntity.getSender_id().getUsername());
        messageDto.setReceiver(messageEntity.getReceiver_id().getUsername());
        messageDto.setTime(messageEntity.getTime());
        messageDto.setRead(messageEntity.isRead());
        messageDto.setTime(messageEntity.getTime());

        return messageDto;
    }

    public MessageEntity convertMessageDtoToMessageEntity(MessageDto messageDto){
        UserEntity senderEntity = userDao.findUserByUsername(messageDto.getSender());
        UserEntity receiverEntity = userDao.findUserByUsername(messageDto.getReceiver());

        MessageEntity messageEntity = new MessageEntity();

        messageEntity.setMessage(messageDto.getMessage());
        messageEntity.setSender_id(senderEntity);
        messageEntity.setReceiver_id(receiverEntity);
        messageEntity.setIsRead(messageDto.isRead());
        messageEntity.setTime(messageDto.getTime());

        return messageEntity;
    }

    public MessageSocketDto jsonToMessageSocketDto(String json){
        MessageSocketDto msg = null;
        try{
            msg = gson.fromJson(json, MessageSocketDto.class);
        }
        catch (Exception e){
            e.getMessage();
            System.out.println("--- "+e.getMessage());
            //tratar o erro...
        }
        return msg;
    }

    public String getMessageToSend(MessageSocketDto msg) {
        return gson.toJson(msg);
    }

    public void sendToUser(String receiver, String messageJson){
        UserEntity user = userDao.findUserByUsername(receiver);
        if(user == null) return;
        List<String> userTokens = getUserTokens(user);
        sendMessageToUserTokens(userTokens, messageJson);
    }

    private List<String> getUserTokens(UserEntity user) {
        int userId = user.getId();
        return tokenDao.findTokensByUserId(userId);
    }

    private void sendMessageToUserTokens(List<String> userTokens, String messageJson) {
        for (String token : userTokens) {
            Session session = notifier.getSessions().get(token);
            if (session != null) {
                try {
                    session.getBasicRemote().sendObject(messageJson);
                } catch (IOException e) {
                    System.out.println("Something went wrong!");
                } catch (EncodeException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }


}
