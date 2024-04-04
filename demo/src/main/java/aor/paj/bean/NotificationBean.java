package aor.paj.bean;

import aor.paj.dao.NotificationDao;
import aor.paj.dao.TaskDao;
import aor.paj.dao.TokenDao;
import aor.paj.entity.NotificationEntity;
import aor.paj.entity.TokenEntity;
import aor.paj.entity.UserEntity;
import aor.paj.websocket.Notifier;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.ApplicationScoped;

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
    TaskDao taskDao;

    @EJB
    Notifier notifier;





    //    public void notifyAllLoggedUsers(String notification, UserDto userDto){
    public void notifyAllLoggedUsers(String notification){
        System.out.println("entra em notifyAllLoggedUsers");
//        UserEntity userEntity = UserMapper.convertUserDtoToUserEntity(userDto);
        Set<UserEntity> uniqueUsers = new HashSet<>();

        List<TokenEntity> loggedUsers = tokenDao.findValidTokens(LocalDateTime.now());
        System.out.println("loggedUsers size: " + loggedUsers.size());
        for(TokenEntity t : loggedUsers){
            uniqueUsers.add(t.getUser());
            notifier.send(t.getToken(), notification);
        }
        if(uniqueUsers.isEmpty()){
            System.out.println("No users to notify");
            return;
        }
        for(UserEntity u : uniqueUsers){
            System.out.println("User: " + u.getUsername());
            NotificationEntity notificationEntity = new NotificationEntity();
            notificationEntity.setUser(u);
            System.out.println("notificaçao: " + notification);
            notificationEntity.setMessage(notification);
            System.out.println("read"+notificationEntity.isRead());
            notificationEntity.setRead(false);
            System.out.println("time"+notificationEntity.getTimestamp());
            notificationEntity.setTimestamp(LocalDateTime.now());

            notificationDao.persist(notificationEntity);
        }

    }
}
