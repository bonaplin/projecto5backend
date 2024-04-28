package aor.paj.dao;

import aor.paj.entity.UserEntity;
import jakarta.ejb.Schedule;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Stateless
public class UserDao extends AbstractDao<UserEntity> {

    private static final long serialVersionUID = 1L;

    public UserDao() {
        super(UserEntity.class);
    }

//    public UserEntity findUserByToken(String token) {
//        try {
//            return (UserEntity) em.createNamedQuery("User.findUserByToken").setParameter("token", token)
//                    .getSingleResult();
//
//        } catch (NoResultException e) {
//            return null;
//        }
//    }

    public UserEntity findUserByEmail(String email) {
        try {
            return (UserEntity) em.createNamedQuery("User.findUserByEmail").setParameter("email", email)
                    .getSingleResult();

        } catch (NoResultException e) {
            return null;
        }
    }

    public UserEntity findUserByUsername(String username) {
        try {
            return (UserEntity) em.createNamedQuery("User.findUserByUsername").setParameter("username", username)
                    .getSingleResult();

        } catch (NoResultException e) {
            return null;
        }
    }

    public UserEntity findUserById(int id) {
        try {
            return (UserEntity) em.createNamedQuery("User.findUserById").setParameter("id", id)
                    .getSingleResult();

        } catch (NoResultException e) {
            return null;
        }
    }

    public UserEntity findUserByToken(String token) {
        try {
            return (UserEntity) em.createNamedQuery("User.findUserByToken").setParameter("token", token)
                    .getSingleResult();

        } catch (NoResultException e) {
            return null;
        }
    }

    public List<UserEntity> findAllUsers() {
        try{
            return em.createNamedQuery("User.findAllUsers").getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }
    // Find all unconfirmed users and remove them
    public List<UserEntity> findUnconfirmedUsers() {
        List<UserEntity> unconfirmedUsers = em.createQuery("SELECT u FROM UserEntity u WHERE u.confirmed = false", UserEntity.class)
                .getResultList();
        return unconfirmedUsers;
    }
    @Schedule(hour = "*", persistent = false)
    public void removeUnconfirmedUsers() {

        List<UserEntity> unconfirmedUsers = findUnconfirmedUsers();

        for (UserEntity user : unconfirmedUsers) {
            if(user.getToken_expiration().isBefore(Instant.now())) {
                em.remove(user);
            }
        }
    }

    //STATISTICS - STATISTICS - STATISTICS - STATISTICS - STATISTICS - STATISTICS
    public int getUserCount() {
        return ((Number) em.createQuery("SELECT COUNT(u) FROM UserEntity u").getSingleResult()).intValue();
    }
    public int getActiveUserCount() {
        return ((Number) em.createQuery("SELECT COUNT(u) FROM UserEntity u WHERE u.active = true").getSingleResult()).intValue();
    }
    public int getUnconfirmedUserCount() {
        return ((Number) em.createQuery("SELECT COUNT(u) FROM UserEntity u WHERE u.confirmed = false").getSingleResult()).intValue();
    }
    public List<LocalDate> getActiveUserCreationDates() {
        return em.createQuery("SELECT u.created FROM UserEntity u WHERE u.active = true ORDER BY u.created", LocalDate.class).getResultList();
    }

    public List<Object[]> getRegistrationByTime() {
        return em.createQuery("SELECT YEAR(u.created), MONTH(u.created), COUNT(u) " +
                "FROM UserEntity u " +
                "GROUP BY YEAR(u.created), MONTH(u.created) " +
                "ORDER BY YEAR(u.created), MONTH(u.created)").getResultList();
    }



    public int getInactiveUserCount() {
        return ((Number) em.createQuery("SELECT COUNT(u) FROM UserEntity u WHERE u.active = false").getSingleResult()).intValue();
    }

    public int getConfirmedUserCount() {
        return ((Number) em.createQuery("SELECT COUNT(u) FROM UserEntity u WHERE u.confirmed = true").getSingleResult()).intValue();
    }
}

