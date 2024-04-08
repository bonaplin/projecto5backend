package aor.paj.dao;

import aor.paj.entity.MessageEntity;
import jakarta.ejb.Stateless;

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

}