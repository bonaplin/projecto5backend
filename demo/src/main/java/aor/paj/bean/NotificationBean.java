package aor.paj.bean;

import aor.paj.dao.NotificationDao;
import aor.paj.dao.TokenDao;
import aor.paj.dao.UserDao;
import aor.paj.dto.NotificationDto;
import aor.paj.entity.NotificationEntity;
import aor.paj.entity.TokenEntity;
import aor.paj.entity.UserEntity;
import aor.paj.websocket.Notifier;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.ArrayList;
import java.util.List;

@Stateless
public class NotificationBean {

    @EJB
    NotificationDao notificationDao;

    @EJB
    TokenDao tokenDao;

    @EJB
    UserDao userDao;

    @EJB
    Notifier notifier;


    public NotificationEntity convertNotificationDtoToNotificationEntity(NotificationDto notificationDto){
        UserEntity receiver = userDao.findUserByUsername(notificationDto.getReceiver());
        UserEntity sender = userDao.findUserByUsername(notificationDto.getSender());

        NotificationEntity notificationEntity = new NotificationEntity();
        notificationEntity.setId(notificationDto.getId());
        notificationEntity.setSender(sender);
        notificationEntity.setMessage(notificationDto.getMessage());
        notificationEntity.setReceiver(receiver);
        notificationEntity.setIsRead(notificationDto.isRead());

        return notificationEntity;
    }

    public NotificationDto convertNotificationEntityToNotificationDto(NotificationEntity notificationEntity){
        NotificationDto notificationDto = new NotificationDto();
        notificationDto.setId(notificationEntity.getId());
        notificationDto.setMessage(notificationEntity.getMessage());
        notificationDto.setReceiver(notificationEntity.getReceiver().getUsername());
        notificationDto.setSender(notificationEntity.getSender().getUsername());
        notificationDto.setTime(notificationEntity.getTime());
        notificationDto.setRead(notificationEntity.isRead());

        return notificationDto;
    }


    public void sendNotification(NotificationDto notification) {
        UserEntity userEntity = userDao.findUserByUsername(notification.getReceiver());
        if(userEntity == null){
            return;
        }
        NotificationEntity notificationEntity = convertNotificationDtoToNotificationEntity(notification);
        notificationDao.persist(notificationEntity);
    }

    public void markAsRead(String token, Integer id) {
        UserEntity receiver = tokenDao.findUserByTokenString(token);

        if (id != null) {
            NotificationEntity notificationEntity = notificationDao.find(id);
            if (notificationEntity != null) {
                notificationEntity.setIsRead(true);
                notificationDao.merge(notificationEntity);
            }
        }

        if(id == null && receiver != null) {
            List<NotificationEntity> notificationEntities = notificationDao.findNotificationsByReceiverUnread(receiver.getUsername());

            for(NotificationEntity n : notificationEntities){
                n.setIsRead(true);
                notificationDao.merge(n);
            }
        }
    }


    public List<NotificationDto> getAllNotificationsBytoken(String token) {
        List<NotificationDto> notificationDtos = new ArrayList<>();
        TokenEntity tokenEntity = tokenDao.findTokenByToken(token);
        UserEntity userEntity = tokenEntity.getUser();
        List<NotificationEntity> notificationEntities = notificationDao.findNotificationsByReceiver(userEntity.getUsername());
        for(NotificationEntity n : notificationEntities){
            NotificationDto ndto = convertNotificationEntityToNotificationDto(n);
            notificationDtos.add(ndto);
        }
        return notificationDtos;
    }
    public List<NotificationDto> getUnreadNotificationsByToken(String token) {
        List<NotificationDto> notificationDtos = new ArrayList<>();
        TokenEntity tokenEntity = tokenDao.findTokenByToken(token);
        UserEntity userEntity = tokenEntity.getUser();
        List<NotificationEntity> notificationEntities = notificationDao.findNotificationsByReceiver(userEntity.getUsername());
        for(NotificationEntity n : notificationEntities){
            if (!n.isRead()) {
                NotificationDto notificationDto = convertNotificationEntityToNotificationDto(n);
                notificationDtos.add(notificationDto);
            }
        }
        return notificationDtos;
    }
}
