package aor.paj.bean;

import aor.paj.dao.MessageDao;
import aor.paj.dao.UserDao;
import aor.paj.dto.MessageDto;
import aor.paj.entity.MessageEntity;
import aor.paj.entity.UserEntity;
import aor.paj.websocket.Notifier;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class MessageBean {

    @EJB
    UserDao userDao;
    @EJB
    MessageDao messageDao;
    @EJB
    Notifier notifier;


    //POSSO CRIAR UMA SWITCH COM O .getType() PARA VERIRICAR SE É UM CHAT, NOTIFICATION OU UPDATE!
 public void sendMessage(MessageDto messageDto){
        MessageEntity messageEntity = convertMessageDtoToMessageEntity(messageDto);
        messageDao.persist(messageEntity);
        notifier.sendToUser(messageDto.getReceiver(), messageDto.getMessage());
        System.out.println("Message send/: " + messageDto.getMessage());
 }

    public MessageDto convertMessageEntityToMessageDto(MessageEntity messageEntity){
        MessageDto messageDto = new MessageDto();

        messageDto.setMessage(messageEntity.getMessage());
        messageDto.setSender(messageEntity.getSender_id().getUsername());
        messageDto.setReceiver(messageEntity.getReceiver_id().getUsername());
        messageDto.setTime(messageEntity.getTime());
        messageDto.setRead(messageEntity.isRead());

        return messageDto;
    }

    public MessageEntity convertMessageDtoToMessageEntity(MessageDto messageDto){
        UserEntity sender = userDao.findUserByUsername(messageDto.getSender());
        UserEntity receiver = userDao.findUserByUsername(messageDto.getReceiver());

        MessageEntity messageEntity = new MessageEntity();

        messageEntity.setMessage(messageDto.getMessage());
        messageEntity.setSender_id(sender);
        messageEntity.setReceiver_id(receiver);
        messageEntity.setIsRead(messageDto.isRead());

        return messageEntity;
    }

    public List<MessageDto> getMessagesByReceiver(String receiver){
        List<MessageEntity> messages = messageDao.findMessagesByReceiver(receiver);
        if(messages == null)
            return null;
        return messages.stream()
                .map(this::convertMessageEntityToMessageDto)
                .collect(Collectors.toList());
    }

}
