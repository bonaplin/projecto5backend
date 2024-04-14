package aor.paj.bean;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import aor.paj.controller.EmailSender;
import aor.paj.dao.CategoryDao;
import aor.paj.dao.TaskDao;
import aor.paj.dao.TokenDao;
import aor.paj.dao.UserDao;
import aor.paj.dto.*;
import aor.paj.entity.CategoryEntity;
import aor.paj.entity.TaskEntity;
import aor.paj.entity.TokenEntity;
import aor.paj.entity.UserEntity;
import aor.paj.mapper.UserMapper;
import aor.paj.utils.ResetPasswordStatus;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.enterprise.context.ApplicationScoped;
import org.apache.logging.log4j.LogManager;
import org.mindrot.jbcrypt.BCrypt;

@Stateless
public class UserBean {
//    private ArrayList<UserDto> userDtos;

    private static final org.apache.logging.log4j.Logger logger = LogManager.getLogger(UserBean.class);

    @EJB
    UserDao userDao;

    @EJB
    TaskDao taskDao;

    @EJB
    CategoryDao categoryDao;

    @EJB
    TokenDao tokenDao;

    // FOR POPULATE USING
    public boolean addUserFromPopulator(UserDto user) {

            UserEntity userEntity = UserMapper.convertUserDtoToUserEntity(user);
            //Encrypt the password
            userEntity.setPassword(BCrypt.hashpw(userEntity.getPassword(), BCrypt.gensalt()));

            if(userEntity.getUsername().equals("admin")){
                userEntity.setRole("po");
            }else {
                userEntity.setRole("dev");
            }
            userEntity.setActive(true);
            userEntity.setConfirmed(true);
            userEntity.setCreated(Instant.now());
            //System.out.println("user a ser adicionado: " + userEntity);
            userDao.persist(userEntity);
            logger.info("user a ser adicionado: " + userEntity.getUsername());
            return true;
    }
    public boolean addUserPO(UserDto user, String role) {

        String password = user.getPassword();
        if(password==null || password.equals("") || password.isBlank()){
            user.setPassword(UUID.randomUUID().toString());
        }

        UserEntity userEntity = UserMapper.convertUserDtoToUserEntity(user);


        userEntity.setPassword(BCrypt.hashpw(userEntity.getPassword(), BCrypt.gensalt()));

        if(userEntity.getUsername().equals("admin")){
            userEntity.setRole("po");
        }

        if (role.equals("po") || role.equals("sm") || role.equals("dev")) {
            userEntity.setRole(role);
        } else {
            userEntity.setRole("dev");
        }

        userEntity.setActive(true);

        userEntity.setCreated(Instant.now());

        generateNewToken(userEntity, 60);
        userDao.persist(userEntity);


        String verificationLink = "http://localhost:3000/confirm-account/" + userEntity.getToken_verification();
        EmailSender.sendVerificationEmail(userEntity.getEmail(), userEntity.getUsername(), verificationLink);

        logger.info("User adicionado: " + userEntity.getUsername()+" & email de verificação enviado para: " + userEntity.getEmail());

        return true;
    }

    //Function that receives a UserDto and checks in database mysql if the username and email already exists
    public boolean userExists(UserDto user) {
        UserEntity userEntity = userDao.findUserByUsername(user.getUsername());
        if (userEntity != null) {
            return true;
        }
        userEntity = userDao.findUserByEmail(user.getEmail());
        if (userEntity != null) {
            return true;
        }
        return false;
    }


    //Fubction that receives username, retrieves the user from the database and returns the userDto object
    public UserDto getUserByUsername(String username) {
        UserEntity userEntity = userDao.findUserByUsername(username);
        if (userEntity != null) {
            return UserMapper.convertUserEntityToUserDto(userEntity);
        }
        return null;
    }
    public UserProfileDto getUserProfileByUsername(String username){
        UserEntity userEntity = userDao.findUserByUsername(username);
        if(userEntity != null){
            UserProfileDto userProfileDto = new UserProfileDto();
            userProfileDto.setUsername(userEntity.getUsername());
            userProfileDto.setEmail(userEntity.getEmail());
            userProfileDto.setFirstname(userEntity.getFirstname());
            userProfileDto.setLastname(userEntity.getLastname());
            userProfileDto.setPhotoURL(userEntity.getPhotoURL());
            userProfileDto.setTaskcount(taskDao.findTaskByOwnerId(userEntity.getId()).size());
            userProfileDto.setTodocount(taskDao.findTaskByOwnerIdAndStatus(userEntity.getId(),100).size());
            userProfileDto.setDoingcount(taskDao.findTaskByOwnerIdAndStatus(userEntity.getId(),200).size());
            userProfileDto.setDonecount(taskDao.findTaskByOwnerIdAndStatus(userEntity.getId(),300).size());
            userProfileDto.setTaskcount(userProfileDto.getTaskcount());
            return userProfileDto;
        }
        return new UserProfileDto();
    }

    //Return the list of users in the json file
    public List<UserDto> getAllUsersDB() {
        List<UserEntity> userEntities = userDao.findAllUsers();
        //cria um arraylist de userentity para devolver
        List<UserDto> userDtos = new ArrayList<>();
        //adiciona os users à lista
        for(UserEntity ue : userEntities){
            userDtos.add(UserMapper.convertUserEntityToUserDto(ue));
        }
        return userDtos;
    }

    //Function that receives a UserUpdateDto and updates the corresponding user
    public void updateUser(UserUpdateDto userUpdateDto) {
        UserEntity userEntity = userDao.findUserByUsername(userUpdateDto.getUsername());

        if (userEntity != null) {
            userEntity.setFirstname(userUpdateDto.getFirstname());
            userEntity.setLastname(userUpdateDto.getLastname());
            userEntity.setEmail(userUpdateDto.getEmail());
            userEntity.setPhone(userUpdateDto.getPhone());
            userEntity.setPhotoURL(userUpdateDto.getPhotoURL());
            userEntity.setRole(userUpdateDto.getRole());

            userDao.merge(userEntity);
        }
    }

    //Function that receives a UserPasswordUpdateDto and updates the corresponding user
    public boolean updatePassword(UserPasswordUpdateDto userPasswordUpdateDto, String token) {
        TokenEntity tokenEntity = tokenDao.findTokenByToken(token);
        if (tokenEntity != null) {
            UserEntity userEntity = tokenEntity.getUser();
            if (BCrypt.checkpw(userPasswordUpdateDto.getOldPassword(), userEntity.getPassword())) {
                String encryptedPassword = BCrypt.hashpw(userPasswordUpdateDto.getNewPassword(), BCrypt.gensalt());
                userEntity.setPassword(encryptedPassword);
                userDao.merge(userEntity);
                return true;
            }
        }
        return false;
    }

    //Function that returns a list of users that own tasks
    public List<UserDto> getUsersOwners() {
        List<UserEntity> userEntities = userDao.findAllUsers();
        List<UserDto> userDtos = new ArrayList<>();
        for (UserEntity userEntity : userEntities) {
            List<TaskEntity> tasks = taskDao.findTaskByOwnerId(userEntity.getId());
            boolean hasActiveTask = false;
            for (TaskEntity task : tasks) {
                if (task.getActive()) {
                    hasActiveTask = true;
                    break;
                }
            }
            if (hasActiveTask) {
                userDtos.add(UserMapper.convertUserEntityToUserDto(userEntity));
            }
        }
        return userDtos;
    }
    public boolean changeStatus(String username, boolean status){
        if(username.equals("admin")){
            return false;
        }
        UserEntity userEntity = userDao.findUserByUsername(username);
        if(userEntity != null){
            userEntity.setActive(status);
            userDao.merge(userEntity);
            return true;
        }
        return false;
    }

    //Function that receives a UserDto and converts it to a UserPartialDto
    public UserPartialDto mapUserToUserPartialDTO(UserDto userDto) {
        return new UserPartialDto(userDto.getFirstname(), userDto.getPhotoURL());
    }

    public boolean deleteUser(String username) {
        if(username.equals("admin") || username.equals("deleted")){
            return false;
        }
        UserEntity userEntity = userDao.findUserByUsername(username);
        System.out.println("user a ser apagado: " + userEntity.getUsername());
        if (userEntity != null) {
            changeTaskOwner(username,"deleted");
            System.out.println("tasks alteradas");
            changeCategoryOwner(username,"deleted");
            System.out.println("categorias alteradas");
            userDao.remove(userEntity);
            System.out.println("user removido");

            return true;
            }
        return false;
    }

    public boolean changeCategoryOwner(String oldUsername, String newUsername){
        UserEntity oldUserEntity = userDao.findUserByUsername(oldUsername);
        UserEntity newUserEntity = userDao.findUserByUsername(newUsername);
        if(oldUserEntity != null && newUserEntity != null) {
            List<CategoryEntity> categories = categoryDao.findCategoryByOwnerID(oldUserEntity.getId());
            for (CategoryEntity category : categories) {
                category.setOwner(newUserEntity);
                categoryDao.merge(category);
                return true;
            }
        }
        return false;
    }

    public boolean changeTaskOwner(String oldUsername, String newUsername){
        UserEntity oldUserEntity = userDao.findUserByUsername(oldUsername);
        UserEntity newUserEntity = userDao.findUserByUsername(newUsername);
        if(oldUserEntity != null && newUserEntity != null){
            List<TaskEntity> tasks = taskDao.findTaskByOwnerId(oldUserEntity.getId());
            for(TaskEntity task : tasks){
                System.out.println("task a ser alterada: " + task.getTitle());
                task.setOwner(newUserEntity);

                taskDao.merge(task);
                System.out.println("task alterada: " + task.getTitle());
            }
            return true;
        }
        return false;
    }

    public boolean deleteTasks(String username) {
        UserEntity userEntity = userDao.findUserByUsername(username);
        if (userEntity != null) {
            List<TaskEntity> tasks = taskDao.findTaskByOwnerId(userEntity.getId());
            for(TaskEntity task : tasks){
                task.setActive(false);
                taskDao.merge(task);
            }
            return true;
        }
        return false;
    }

    public void createDefaultUsersIfNotExistent() {
        if (userDao.findUserByUsername("admin") == null) {
            UserDto userDto = new UserDto();
            userDto.setUsername("admin");
            userDto.setPassword("admin");
            userDto.setFirstname("Admin");
            userDto.setLastname("Admin");
            userDto.setEmail("admin@admin");
            userDto.setPhone("000000000");
            userDto.setPhotoURL("https://t4.ftcdn.net/jpg/04/75/00/99/360_F_475009987_zwsk4c77x3cTpcI3W1C1LU4pOSyPKaqi.jpg");
            userDto.setConfirmed(true);
            addUserPO(userDto,"po");
        }

        if (userDao.findUserByUsername("deleted") == null) {
            UserDto userDto = new UserDto();
            userDto.setUsername("deleted");
            userDto.setPassword("deleted");
            userDto.setFirstname("User deleted");
            userDto.setLastname("User deleted");
            userDto.setEmail("userdeleted@deleted");
            userDto.setPhone("000000000");
            userDto.setPhotoURL("https://www.shutterstock.com/image-vector/trash-can-icon-symbol-delete-600nw-1454137346.jpg");
            userDto.setConfirmed(true);
            addUserPO(userDto,"dev");
            changeStatus("deleted",false);
        }

    }
    public boolean isSameUserEmail(String email, int userId) {
        UserEntity userEntity = userDao.findUserByEmail(email);
        return userEntity != null && userEntity.getId() == userId;
    }

    public boolean confirmUser(String token) {
        UserEntity userEntity = userDao.findUserByToken(token);
        if (userEntity != null) {
            System.out.println("user encontrado");
            return true;
        }else{
            System.out.println("user nao encontrado");
            return false;
        }
    }

    public boolean userConfirmed(String token){
        UserEntity userEntity = tokenDao.findUserByTokenString(token);
           if(userEntity != null){
                return userEntity.getConfirmed();
            }
        return false;
    }

    //Function that generates a new token and expire time
    private boolean generateNewToken(UserEntity userEntity, int minutes) {
        String token = UUID.randomUUID().toString();
        userEntity.setToken_verification(token);
        userEntity.setToken_expiration(Instant.now().plus(Duration.ofMinutes(minutes)));
        return true;
    }

    public boolean sendPasswordResetEmail(String email) {
        UserEntity userEntity = userDao.findUserByEmail(email);
        if (userEntity != null) {
            generateNewToken(userEntity, 60);
            userDao.merge(userEntity); //update the user with the new token in DB
            String resetLink = "http://localhost:3000/reset-password/" + userEntity.getToken_verification();
            EmailSender.sendPasswordResetEmail(email, userEntity.getUsername(), resetLink);
            return true;
        }
        return false;
    }

    public ResetPasswordStatus resetPassword(String token, String password){
        UserEntity userEntity = userDao.findUserByToken(token);
        System.out.println("reset password");

        if(userEntity == null){
            return ResetPasswordStatus.USER_NOT_FOUND;
        }

        if(!userEntity.getConfirmed()){
            userEntity.setConfirmed(true);
            System.out.println("user confirmado" + userEntity.getConfirmed());
            userEntity.setCreated(Instant.now());
        }

        if(userEntity.getToken_expiration().isBefore(Instant.now())){
            userEntity.setToken_expiration(null);
            userEntity.setToken_verification(null);
            return ResetPasswordStatus.TOKEN_EXPIRED;
        }

        userEntity.setToken_expiration(null);
        userEntity.setToken_verification(null);

        userEntity.setPassword(BCrypt.hashpw(password, BCrypt.gensalt()));
        System.out.println(userEntity.getConfirmed());
        userDao.merge(userEntity);
        return ResetPasswordStatus.SUCCESS;
    }
}
