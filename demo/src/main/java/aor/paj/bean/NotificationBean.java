package aor.paj.bean;

import aor.paj.dao.NotificationDao;
import aor.paj.dao.TaskDao;
import aor.paj.dao.TokenDao;
import aor.paj.dao.UserDao;
import aor.paj.dto.MessageDto;
import aor.paj.dto.NotificationDto;
import aor.paj.entity.MessageEntity;
import aor.paj.entity.NotificationEntity;
import aor.paj.entity.TokenEntity;
import aor.paj.entity.UserEntity;
import aor.paj.websocket.Notifier;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@ApplicationScoped
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

        NotificationEntity notificationEntity = new NotificationEntity();

        notificationEntity.setMessage(notificationDto.getMessage());
        notificationEntity.setReceiver(receiver);
        notificationEntity.setIsRead(notificationDto.isRead());

        return notificationEntity;
    }

    public NotificationDto convertNotificationEntityToNotificationDto(NotificationEntity notificationEntity){
        NotificationDto notificationDto = new NotificationDto();

        notificationDto.setMessage(notificationEntity.getMessage());
        notificationDto.setReceiver(notificationEntity.getReceiver().getUsername());
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


}
