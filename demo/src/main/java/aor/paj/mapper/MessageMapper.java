package aor.paj.mapper;

import aor.paj.dao.UserDao;
import aor.paj.dto.MessageDto;
import aor.paj.entity.MessageEntity;
import aor.paj.entity.UserEntity;

import java.sql.Timestamp;

public class MessageMapper {

    private UserDao userDao;

    public MessageMapper (UserDao userDao){
        this.userDao = userDao;
    }

    public static MessageDto convertMessageEntityToMessageDto(MessageEntity messageEntity){
        MessageDto messageDto = new MessageDto();

        messageDto.setMessage(messageEntity.getMessage());
        messageDto.setSender(messageEntity.getSender_id().getUsername());
        messageDto.setReceiver(messageEntity.getReceiver_id().getUsername());
        messageDto.setCreatedAt(messageEntity.getCreatedAt());
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
        messageEntity.setCreatedAt(messageDto.getCreatedAt());
        messageEntity.setRead(messageDto.isRead());

        return messageEntity;
    }
}
