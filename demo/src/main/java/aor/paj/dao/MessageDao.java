package aor.paj.dao;

import aor.paj.entity.MessageEntity;
import aor.paj.entity.UserEntity;
import jakarta.ejb.Stateless;
import jakarta.persistence.TypedQuery;

import java.util.List;

@Stateless
public class MessageDao extends AbstractDao<MessageEntity> {

    private static final long serialVersionUID = 1L;

    public MessageDao() {
        super(MessageEntity.class);
    }

    public List<MessageEntity> findMessagesByReceiver(String receiver) {
        try{
            return em.createNamedQuery("Message.findMessagesByReceiver").setParameter("receiver", receiver).getResultList();
        } catch (Exception e){
            return null;
        }

    }

    public List<MessageEntity> findMessagesBySenderAndReceiver(String senderUsername, String receiverUsername) {
        TypedQuery<MessageEntity> query = em.createNamedQuery("Message.findMessagesBySenderAndReceiver", MessageEntity.class);
        query.setParameter("sender", senderUsername);
        query.setParameter("receiver", receiverUsername);
        return query.getResultList();
    }
}