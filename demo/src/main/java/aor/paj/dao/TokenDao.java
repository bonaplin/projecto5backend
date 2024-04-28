package aor.paj.dao;

import aor.paj.bean.Log;
import aor.paj.bean.MessageBean;
import aor.paj.entity.TokenEntity;
import aor.paj.entity.UserEntity;
import aor.paj.utils.MessageType;
import jakarta.ejb.EJB;
import jakarta.ejb.Schedule;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceException;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Stateless
public class TokenDao extends AbstractDao<TokenEntity>{
    private static final long serialVersionUID = 1L;
    @EJB
    private MessageBean messageBean;
    @Inject
    Log log;
    public TokenDao() {
        super(TokenEntity.class);
    }

    public TokenEntity findTokenByToken(String token) {
        try {
            return (TokenEntity) em.createNamedQuery("Token.findTokenByToken").setParameter("token", token)
                    .getSingleResult();

        } catch (NoResultException e) {
            return null;
        }
    }

    public TokenEntity findTokenByUserId(int id) {
        try {
            return (TokenEntity) em.createNamedQuery("Token.findTokenByUserId").setParameter("id", id)
                    .getSingleResult();

        } catch (NoResultException e) {
            return null;
        }
    }

    public UserEntity findUserByTokenString(String token) {
        TokenEntity tokenEntity = findTokenByToken(token);
        if (tokenEntity != null) {
            return tokenEntity.getUser();
        }
        return null;
    }

    public List<UserEntity> findLoggedUsers(){
        List<TokenEntity> validTokens = findValidTokens(Instant.now());
        List<UserEntity> loggedUsers = new ArrayList<>();

        for(TokenEntity t : validTokens){
            loggedUsers.add(t.getUser());
        }

        return loggedUsers;
    }

    public List<TokenEntity> findValidTokens(Instant now) {
        try {
            List<TokenEntity> validTokens = em.createQuery("SELECT t FROM TokenEntity t WHERE t.expiration > :now", TokenEntity.class)
                    .setParameter("now", now)
                    .getResultList();
            return validTokens;
        }catch (PersistenceException e){
            return new ArrayList<>();
        }

    }

    public List<TokenEntity> findExpiredTokens(Instant now) {
        List<TokenEntity> expiredTokens = em.createQuery("SELECT t FROM TokenEntity t WHERE t.expiration < :now", TokenEntity.class)
                .setParameter("now", now)
                .getResultList();
        return expiredTokens;
    }

    // Remove expired tokens every 5 minutes */5 <- every 5 minutes, * <- every time, hour, minute or second.
//    @Schedule(hour = "*", minute = "*/5", persistent = false)
    @Schedule(hour = "*", minute = "*/1", persistent = false)
    public void removeExpiredTokens() {
        List<TokenEntity> expiredTokens = findExpiredTokens(Instant.now());
        for (TokenEntity token : expiredTokens) {
            UserEntity user = token.getUser();
            if(user != null){
                log.logUserInfo(token.getToken(), "Removing token: " + token.getToken()+" to logout user.", 3);
                String username = user.getUsername();
                messageBean.sendInfo(username, "Your account has been disconnected due to inactivity",
                        MessageType.LOGOUT, token.getToken()
                );
                em.remove(token);
            }
        }
    }

    public List<String> findTokensByUserId(int id) {
        List<TokenEntity> tokens = em.createQuery("SELECT t FROM TokenEntity t WHERE t.user.id = :id", TokenEntity.class)
                .setParameter("id", id)
                .getResultList();
        List<String> tokenStrings = new ArrayList<>();
        for(TokenEntity t : tokens){
            tokenStrings.add(t.getToken());
        }
        return tokenStrings;
    }
}
