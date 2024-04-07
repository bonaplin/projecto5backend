package aor.paj.dao;

import aor.paj.entity.MessageEntity;
import jakarta.ejb.Stateless;

@Stateless
public class MessageDao extends AbstractDao<MessageEntity> {

    private static final long serialVersionUID = 1L;

    public MessageDao() {
        super(MessageEntity.class);
    }



}