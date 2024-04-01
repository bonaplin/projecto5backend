package aor.paj.dao;

import aor.paj.entity.UserEntity;
import jakarta.ejb.Schedule;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;

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

    public List<UserEntity> findAllUsers() {
        try{
            return em.createNamedQuery("User.findAllUsers").getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }
    // Find all unconfirmed users and remove them
    public List<UserEntity> findUnconfirmedUsers() {
        System.out.println("Finding unconfirmed users");
        List<UserEntity> unconfirmedUsers = em.createQuery("SELECT u FROM UserEntity u WHERE u.confirmed = false", UserEntity.class)
                .getResultList();
        return unconfirmedUsers;
    }
    @Schedule(hour = "*", persistent = false)
    public void removeUnconfirmedUsers() {

        List<UserEntity> unconfirmedUsers = findUnconfirmedUsers();

        for (UserEntity user : unconfirmedUsers) {
            System.out.println("Removing user" + user.getId());
            em.remove(user);
        }
    }
}

