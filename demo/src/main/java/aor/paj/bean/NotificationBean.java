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

//    public void notifyAllLoggedUsers(String notification, UserDto userDto){
//    public void notifyAllLoggedUsers(String notification){
//        System.out.println("entra em notifyAllLoggedUsers");
////        UserEntity userEntity = UserMapper.convertUserDtoToUserEntity(userDto);
//        Set<UserEntity> uniqueUsers = new HashSet<>();
//
//        List<TokenEntity> loggedUsers = tokenDao.findValidTokens(Instant.now());
//        System.out.println("loggedUsers size: " + loggedUsers.size());
//        for(TokenEntity t : loggedUsers){
//            uniqueUsers.add(t.getUser());
//            notifier.send(t.getToken(), notification);
//        }
//        if(uniqueUsers.isEmpty()){
//            System.out.println("No users to notify");
//            return;
//        }
//        for(UserEntity u : uniqueUsers){
//            System.out.println("User: " + u.getUsername());
//            NotificationEntity notificationEntity = new NotificationEntity();
//            notificationEntity.setReceiver(u);
//            System.out.println("notificaçao: " + notification);
//            notificationEntity.setMessage(notification);
//            System.out.println("read"+notificationEntity.isRead());
//            notificationEntity.setIsRead(false);
//            System.out.println("time"+notificationEntity.getTime());
//
//
//            notificationDao.persist(notificationEntity);
//        }
//    }


    public void sendNotification(NotificationDto notification) {
        System.out.println("entra em sendNotification");
        UserEntity userEntity = userDao.findUserByUsername(notification.getReceiver());
        if(userEntity == null){
            System.out.println("User not found");
            return;
        }
        NotificationEntity notificationEntity = convertNotificationDtoToNotificationEntity(notification);
        notificationDao.persist(notificationEntity);
    }

    public List<NotificationDto> getAllNotifications(){

        List<NotificationEntity> notificationEntities = notificationDao.findNotificationsByReceiver("admin");
        System.out.println("notificationEntities: " + notificationEntities);
        if(notificationEntities == null || notificationEntities.isEmpty()){
            System.out.println("notifications not found, is null or empty");
            return new ArrayList<>();
        }
        List<NotificationDto> notificationDtos = new ArrayList<>();
        for(NotificationEntity n : notificationEntities){
            notificationDtos.add(convertNotificationEntityToNotificationDto(n));
            System.out.println("notificationDtos: " + notificationDtos);
        }
        return notificationDtos;
    }

    public List<NotificationDto> getUnreadNotifications(String receiver){
        List<NotificationEntity> notificationEntities = notificationDao.findNotificationsByReceiverUnread(receiver);
        if(notificationEntities == null || notificationEntities.isEmpty()){
            return new ArrayList<>();
        }
        List<NotificationDto> notificationDtos = new ArrayList<>();
        for(NotificationEntity n : notificationEntities){
            notificationDtos.add(convertNotificationEntityToNotificationDto(n));
        }
        return notificationDtos;
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
            System.out.println(">>>>>>receiver: " + receiver.getUsername());
            List<NotificationEntity> notificationEntities = notificationDao.findNotificationsByReceiverUnread(receiver.getUsername());
            List<NotificationEntity> notificationEntitie = notificationDao.findNotificationsByReceiver(receiver.getUsername());

            System.out.println(">>>>>>receiver: " + notificationEntities.size());
            for(NotificationEntity n : notificationEntities){
                System.out.println(">>>>>>receiver: antes de marcar como lida: " + n.isRead());
                n.setIsRead(true);
                System.out.println(">>>>>>receiver: depois de marcar como lida: " + n.isRead());
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
}
