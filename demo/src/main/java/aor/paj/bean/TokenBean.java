package aor.paj.bean;

import aor.paj.dao.TaskDao;
import aor.paj.dao.TokenDao;
import aor.paj.dao.UserDao;
import aor.paj.dto.UserDto;
import aor.paj.entity.TokenEntity;
import aor.paj.entity.UserEntity;
import aor.paj.mapper.UserMapper;
import aor.paj.utils.TokenStatus;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.mindrot.jbcrypt.BCrypt;
import java.time.LocalDateTime;


import java.time.LocalDateTime;
import java.util.UUID;

@ApplicationScoped
public class TokenBean  {

    @PersistenceContext
    private EntityManager em;
    @EJB
    private UserDao userDao;
    @EJB
    private TokenDao tokenDao;
    @EJB
    private TaskDao taskDao;
    @Transactional
    public TokenEntity createToken(String token, int userId) {

        TokenEntity tokenEntity = new TokenEntity();

        tokenEntity.setToken(token);

        tokenEntity.setUser(userDao.findUserById(userId));
        tokenEntity.setExpiration(LocalDateTime.now().plusMinutes(1));

        tokenDao.persist(tokenEntity);
        return tokenEntity;
    }


    public String login(String username, String password) {

        UserEntity userEntity = userDao.findUserByUsername(username);
        if (userEntity != null) {

            if (BCrypt.checkpw(password, userEntity.getPassword())) {
                String token = UUID.randomUUID().toString();
                createToken(token, userEntity.getId());

                return token;
            }
        }
        return null;
    }

    public String getUserRole(String token) {
        TokenEntity tokenEntity = tokenDao.findTokenByToken(token);
        if (tokenEntity != null) {
            return tokenEntity.getUser().getRole();
        }
        return null;
    }
    public boolean hasPermissionToEdit(String token, int taskId) {
        TokenEntity tokenEntity = tokenDao.findTokenByToken(token);
        if (tokenEntity != null) {
            UserEntity userEntity = tokenEntity.getUser();
            if (userEntity.getRole().equals("sm") || userEntity.getRole().equals("po")) {
                return true;
            }
            for(int i = 0; i < taskDao.findTaskByOwnerId(userEntity.getId()).size(); i++){
                if(taskDao.findTaskByOwnerId(userEntity.getId()).get(i).getId() == taskId){
                    return true;
                }
            }
        }
        return false;
    }
    public UserDto getUserByToken(String token) {
        TokenEntity tokenEntity = tokenDao.findTokenByToken(token);
        if (tokenEntity != null) {
            return UserMapper.convertUserEntityToUserDto(tokenEntity.getUser());
        }
        return null;
    }

    public void logout(String token) {
        TokenEntity tokenEntity = tokenDao.findTokenByToken(token);
        if (tokenEntity != null) {
            em.remove(tokenEntity);
        }
    }

    public TokenStatus isValidUserByToken(String token) {
        TokenEntity tokenEntity = tokenDao.findTokenByToken(token);
        // falta verificar se o já validou o registo e se a expiração do token ainda não passou.
        if(tokenEntity != null && tokenEntity.getUser().getActive()){
            if (tokenEntity.getExpiration().isAfter(LocalDateTime.now())) {
                // Atualiza a expiração do token
                System.out.println("Token still valid, updated expiration" + tokenEntity.getExpiration());
                tokenEntity.setExpiration(LocalDateTime.now().plusMinutes(1));
                System.out.println("New expiration: " + tokenEntity.getExpiration());
                // merge para atualizar a expiração
                tokenDao.merge(tokenEntity);
                return TokenStatus.VALID;
            }else {
                System.out.println("Token expired, cleaned from database");
                tokenDao.remove(tokenEntity);
                return TokenStatus.EXPIRED;
            }
        }
        return TokenStatus.NOT_FOUND;
    }
}
