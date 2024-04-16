package aor.paj.dao;

import aor.paj.entity.NotificationEntity;
import aor.paj.entity.TaskEntity;
import aor.paj.entity.UserEntity;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;

import java.util.Collections;
import java.util.List;

@Stateless
public class NotificationDao extends AbstractDao<NotificationEntity>{

    @EJB
    UserDao userDao;
    private static final long serialVersionUID = 1L;

    public NotificationDao() {
        super(NotificationEntity.class);
    }


    public List<NotificationEntity> findNotificationsByReceiver(String receiver) {
        try{
            UserEntity receiverEntity = userDao.findUserByUsername(receiver);
            return em.createNamedQuery("Notification.findNotificationsByReceiver").setParameter("receiver", receiverEntity).getResultList();
        } catch (Exception e){
            return Collections.emptyList();
        }
    }

    public List<NotificationEntity> findNotificationsByReceiverUnread(String receiver) {
        try{
            UserEntity receiverEntity = userDao.findUserByUsername(receiver);
            System.out.println("receiverEntity por ler: " + receiverEntity);
            return em.createNamedQuery("Notification.findNotificationsByReceiverUnread").setParameter("receiver", receiverEntity).getResultList();
        } catch (Exception e){
            return Collections.emptyList();
        }
    }



}
