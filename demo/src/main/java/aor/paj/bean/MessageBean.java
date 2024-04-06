package aor.paj.bean;

import aor.paj.dao.MessageDao;
import aor.paj.dao.UserDao;
import aor.paj.dto.MessageDto;
import aor.paj.entity.MessageEntity;
import aor.paj.entity.UserEntity;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class MessageBean {

    @EJB
    UserDao userDao;
    @EJB
    MessageDao messageDao;


 public void sendMessage(MessageDto messageDto){
        MessageEntity messageEntity = convertMessageDtoToMessageEntity(messageDto);
        messageDao.persist(messageEntity);
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
//        messageEntity.setTime(messageDto.getTime());
        messageEntity.setIsRead(messageDto.isRead());

        return messageEntity;
    }

}
