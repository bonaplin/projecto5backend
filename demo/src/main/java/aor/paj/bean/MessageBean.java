package aor.paj.bean;

import aor.paj.dao.MessageDao;
import aor.paj.dao.TokenDao;
import aor.paj.dao.UserDao;
import aor.paj.dto.MessageDto;
//import aor.paj.websocket.dto.MessageSocketDto;
import aor.paj.entity.MessageEntity;
import aor.paj.entity.UserEntity;
import aor.paj.utils.MessageType;
import aor.paj.websocket.Notifier;
import aor.paj.websocket.bean.HandleWebSockets;
import aor.paj.websocket.dto.InfoSocket;
import com.google.gson.JsonObject;
import jakarta.ejb.Stateless;
import jakarta.websocket.EncodeException;
import jakarta.websocket.Session;
import com.google.gson.Gson;
import jakarta.ejb.EJB;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
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
    HandleWebSockets handleWebSockets;
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

    public List<MessageDto> getMessagesBetweenUsers(String usernameX, String usernameY, String token) {
        UserEntity user = tokenDao.findUserByTokenString(token);
        if(user == null) return null;
        // Obter mensagens de X para Y
        List<MessageEntity> messagesFromXtoY = messageDao.findMessagesBySenderAndReceiver(usernameX, usernameY);
        // Obter mensagens de Y para X
        List<MessageEntity> messagesFromYtoX = messageDao.findMessagesBySenderAndReceiver(usernameY, usernameX);

        if(!user.getUsername().equals(usernameX) && !user.getUsername().equals(usernameY)) {
            return null;
        }

        // Combina as duas listas em uma única lista
        List<MessageEntity> allMessages = new ArrayList<>();
        allMessages.addAll(messagesFromXtoY);
        allMessages.addAll(messagesFromYtoX);

        // Ordena todas as mensagens por tempo
        allMessages.sort(Comparator.comparing(MessageEntity::getTime, Comparator.nullsFirst(Comparator.naturalOrder())));

        // Converte as entidades de mensagem em DTOs
        List<MessageDto> allMessageDtos = new ArrayList<>();
        for (MessageEntity message : allMessages) {
            allMessageDtos.add(convertMessageEntityToMessageDto(message));

            if(message.getReceiver_id().getUsername().equals(user.getUsername()) ){
                message.setIsRead(true);
                messageDao.merge(message);
            }
        }

        if(allMessageDtos.size() > 0){
            MessageDto messageDto = allMessageDtos.get(0);
            String message = handleWebSockets.convertToJsonString(messageDto, MessageType.MESSAGE_READ);

            sendToUser(messageDto.getSender(), message);
            sendToUser(messageDto.getReceiver(), message);
        }

        return allMessageDtos;
    }


    /**
     * Envia uma mensagem de informação para um usuário
     * @param receiver
     * @param message informação a enviar
     */
    public void sendInfo(String receiver, String message, MessageType type){
        InfoSocket infoSocket = new InfoSocket();
        infoSocket.setType(type.getValue());
        infoSocket.setMessage(message);
        infoSocket.setReceiver(receiver);

        String messageJson = gson.toJson(infoSocket);
        sendToUser(receiver, messageJson);
    }
}
