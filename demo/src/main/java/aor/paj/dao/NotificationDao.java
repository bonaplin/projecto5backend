package aor.paj.dao;

import aor.paj.entity.NotificationEntity;
import aor.paj.entity.TaskEntity;
import jakarta.ejb.Stateless;

@Stateless
public class NotificationDao extends AbstractDao<NotificationEntity>{

    private static final long serialVersionUID = 1L;

    public NotificationDao() {
        super(NotificationEntity.class);
    }

    public NotificationDao(Class<NotificationEntity> clazz) {
        super(clazz);
    }

}
