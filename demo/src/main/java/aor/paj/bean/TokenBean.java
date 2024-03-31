package aor.paj.bean;

import aor.paj.dao.TaskDao;
import aor.paj.dao.TokenDao;
import aor.paj.dao.UserDao;
import aor.paj.dto.UserDto;
import aor.paj.entity.TokenEntity;
import aor.paj.entity.UserEntity;
import aor.paj.mapper.UserMapper;
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
        System.out.println("createToken");
        TokenEntity tokenEntity = new TokenEntity();
        System.out.println("userId: " + userId);
        tokenEntity.setToken(token);
        System.out.println("token: " + token);
        tokenEntity.setUser(userDao.findUserById(userId));
        tokenEntity.setExpiration(LocalDateTime.now().plusHours(1));
        System.out.println("antes de persist: user: " + userDao.findUserById(userId));

        tokenDao.persist(tokenEntity);
        System.out.println("depois de persist: user: " + userDao.findUserById(userId));
        return tokenEntity;
    }


    public String login(String username, String password) {
        System.out.println(username + " & " + password);
        UserEntity userEntity = userDao.findUserByUsername(username);
        if (userEntity != null) {
            System.out.println("userentity: "+ userEntity.getPassword());
            if (BCrypt.checkpw(password, userEntity.getPassword())) {
                String token = UUID.randomUUID().toString();
                createToken(token, userEntity.getId());
                System.out.println("token: " + token);
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

    public boolean isValidUserByToken(String token) {
        TokenEntity tokenEntity = tokenDao.findTokenByToken(token);
        // falta verificar se o já validou o registo e se a expiração do token ainda não passou.
        if(tokenEntity != null && tokenEntity.getUser().getActive()){
            return true;
        }
        return false;
    }
}
