package aor.paj.bean;

import aor.paj.dao.TaskDao;
import aor.paj.dao.TokenDao;
import aor.paj.dao.UserDao;
import aor.paj.dto.TokenExpirationUpdateDto;
import aor.paj.dto.UserDto;
import aor.paj.entity.TokenEntity;
import aor.paj.entity.UserEntity;
import aor.paj.mapper.UserMapper;
import aor.paj.utils.TokenStatus;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.mindrot.jbcrypt.BCrypt;

import java.time.Duration;
import java.time.Instant;


import java.util.UUID;

@ApplicationScoped
public class TokenBean  {

    @EJB
    private UserDao userDao;
    @EJB
    private TokenDao tokenDao;
    @EJB
    private TaskDao taskDao;

    @Inject
    Log log;

    private static int DEFAULT_TOKEN_EXPIRATION_MINUTES = 5;
    private static int PO_TOKEN_EXPIRATION_MINUTES = 60;
    private static final int SHORT_TOKEN_EXPIRATION_SECONDS = 10;

    private void setDefaultTokenExpirationMinutes(int minutes, String token){
        isValidUserByToken(token);
        if(minutes <= 0) return;
        else if(minutes >60) return;
        DEFAULT_TOKEN_EXPIRATION_MINUTES = minutes;
    }

    private void setPoTokenExpirationMinutes(int minutes, String token){
        isValidUserByToken(token);
        if(minutes <= 0) return;
        else if(minutes >60) return;
        PO_TOKEN_EXPIRATION_MINUTES = minutes;
    }

    public static int getDefaultTokenExpirationMinutes() {
        return DEFAULT_TOKEN_EXPIRATION_MINUTES;
    }

    public static int getPoTokenExpirationMinutes() {
        return PO_TOKEN_EXPIRATION_MINUTES;
    }

    @Transactional
    public TokenEntity createToken(String token, String username) {

        TokenEntity tokenEntity = new TokenEntity();
        UserEntity userEntity = userDao.findUserByUsername(username);

        tokenEntity.setUser(userEntity);
        tokenEntity.setToken(token);

        setDefaultTokenExpiration(tokenEntity);

        tokenDao.persist(tokenEntity);

        return tokenEntity;
    }

    public String login(String username, String password) {

        UserEntity userEntity = userDao.findUserByUsername(username);
        if (userEntity != null) {

            if (BCrypt.checkpw(password, userEntity.getPassword())) {
                String token = UUID.randomUUID().toString();
                createToken(token, userEntity.getUsername());
                log.logUserInfo(token, "login user.", 1);
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
    public boolean isProductOwner(String token){
        TokenEntity tokenEntity = tokenDao.findTokenByToken(token);
        if (tokenEntity != null) {
            UserEntity userEntity = tokenEntity.getUser();
            if (userEntity.getRole().equals("po")) {
                return true;
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
            log.logUserInfo(token, "logout user.", 1);
            tokenDao.remove(tokenEntity);
        }
    }

    public TokenStatus isValidUserByToken(String token) {
        TokenEntity tokenEntity = tokenDao.findTokenByToken(token);

        if(tokenEntity == null){
            return TokenStatus.NOT_FOUND;
        }

        if(!tokenEntity.getUser().getConfirmed()){
            return TokenStatus.NOT_CONFIRMED;
        }

        if(tokenEntity.getUser().getActive()){
            if (tokenEntity.getExpiration().isAfter(Instant.now())) {
                // Atualiza a expiração do token

                setDefaultTokenExpiration(tokenEntity);

//                System.out.println("EXPIRAÇÃO: "+tokenEntity.getExpiration());
//                System.out.println("AGORA: "+Instant.now());

                // merge para atualizar a expiração
                tokenDao.merge(tokenEntity);
                return TokenStatus.VALID;
            }else {
                tokenDao.remove(tokenEntity);
                return TokenStatus.EXPIRED;
            }
        }
        return TokenStatus.NOT_FOUND;
    }

    /**
     * Method to set the time of the token expiration based on the user role.
     *
     * @param tokenEntity
     */
    public void setDefaultTokenExpiration (TokenEntity tokenEntity){
        UserEntity userEntity = tokenEntity.getUser();

        if(userEntity.getRole().equals("po")){
            tokenEntity.setExpiration(Instant.now().plus(Duration.ofMinutes(PO_TOKEN_EXPIRATION_MINUTES)));
        }else{
            tokenEntity.setExpiration(Instant.now().plus(Duration.ofMinutes(DEFAULT_TOKEN_EXPIRATION_MINUTES)));
        }
    }

    /**
     * Method to use the tokenExpirationUpdateDto to change the token expiration.
     * It will be called by the endpoint
     *
     * @param tokenExpirationUpdateDto
     * @param token
     */
    public void changeTokenExpiration(TokenExpirationUpdateDto tokenExpirationUpdateDto, String token) {
        if(tokenExpirationUpdateDto.getDefaultTokenExpirationMinutes()>0 &&
            tokenExpirationUpdateDto.getDefaultTokenExpirationMinutes() <= 60){
            setDefaultTokenExpirationMinutes(tokenExpirationUpdateDto.getDefaultTokenExpirationMinutes(), token);

        }
        if(tokenExpirationUpdateDto.getPoTokenExpirationMinutes()>0 &&
            tokenExpirationUpdateDto.getPoTokenExpirationMinutes() <= 60){
            setPoTokenExpirationMinutes(tokenExpirationUpdateDto.getPoTokenExpirationMinutes(), token);

        }

    }



}
