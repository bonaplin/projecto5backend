package aor.paj.bean;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import aor.paj.dao.CategoryDao;
import aor.paj.dao.TaskDao;
import aor.paj.dao.UserDao;
import aor.paj.dto.UserDto;
import aor.paj.dto.UserPartialDto;
import aor.paj.dto.UserPasswordUpdateDto;
import aor.paj.dto.UserUpdateDto;
import aor.paj.entity.CategoryEntity;
import aor.paj.entity.TaskEntity;
import aor.paj.entity.UserEntity;
import aor.paj.mapper.UserMapper;
import aor.paj.utils.JsonUtils;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.mindrot.jbcrypt.BCrypt;

@ApplicationScoped
public class UserBean {
    private ArrayList<UserDto> userDtos;

    @EJB
    UserDao userDao;

    @EJB
    TaskDao taskDao;

    @EJB
    CategoryDao categoryDao;


    //Function that generates a unique id for new user checking in database mysql if the id already exists
    public int generateIdDataBase() {
        int id = 1;
        boolean idAlreadyExists;

        do {
            idAlreadyExists = false;
            UserEntity userEntity = userDao.findUserById(id);
            if (userEntity != null) {
                id++;
                idAlreadyExists = true;
            }
        } while (idAlreadyExists);
        return id;
    }

    //Add a user to the database mysql, encrypting the password, role to "dev" and generating a id
    public boolean addUser(UserDto user) {

            UserEntity userEntity = UserMapper.convertUserDtoToUserEntity(user);
            //Encrypt the password
            userEntity.setPassword(BCrypt.hashpw(userEntity.getPassword(), BCrypt.gensalt()));
            userEntity.setId(generateIdDataBase());
            if(userEntity.getUsername().equals("admin")){
                userEntity.setRole("po");
            }else {
                userEntity.setRole("dev");
            }
            userEntity.setActive(true);
            userDao.persist(userEntity);

            return true;
    }
    public boolean addUserPO(UserDto user, String role) {
        UserEntity userEntity = UserMapper.convertUserDtoToUserEntity(user);
        //Encrypt the password
        userEntity.setPassword(BCrypt.hashpw(userEntity.getPassword(), BCrypt.gensalt()));
        userEntity.setId(generateIdDataBase());
        userEntity.setRole("dev");
        userEntity.setActive(true);
        userEntity.setRole(role);
        userDao.persist(userEntity);

        return true;
    }

    //Function that validates a user in database by token
    public boolean isValidUserByToken(String token) {
        UserEntity userEntity = userDao.findUserByToken(token);
        if(userEntity != null && userEntity.getActive()){
            return true;
        }
        return false;
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

    //Function that receives the username and password and checks in database mysql if the user exists and if the password is correct, then if so returns the token generated
    public String login(String username, String password) {
        UserEntity userEntity = userDao.findUserByUsername(username);
        if (userEntity != null) {
            if (BCrypt.checkpw(password, userEntity.getPassword())) {
                String token = generateNewToken();
                userEntity.setToken(token);
                userDao.merge(userEntity);
                userDao.flush();
                return token;
            }
        }
        return null;
    }

    //Fubction that receives username, retrieves the user from the database and returns the userDto object
    public UserDto getUserByUsername(String username) {
        UserEntity userEntity = userDao.findUserByUsername(username);
        if (userEntity != null) {
            return UserMapper.convertUserEntityToUserDto(userEntity);
        }
        return null;
    }
    //Function that receives the token and retrieves the user from the database and returns the userDto object
    public UserDto getUserByToken(String token) {
        UserEntity userEntity = userDao.findUserByToken(token);
        if (userEntity != null) {
            return UserMapper.convertUserEntityToUserDto(userEntity);
        }
        return null;
    }

    //Function that receives the token and sets it to null, logging out the user
    public void logout(String token) {
        UserEntity userEntity = userDao.findUserByToken(token);
        if (userEntity != null) {
            userEntity.setToken(null);
            userDao.merge(userEntity);
        }
    }

    //Function that generates a new token
    private String generateNewToken() {
        return UUID.randomUUID().toString();
    }

    //Function that receives a token and a task id and checks if the user has permission to access the task, to edit he must be role sm or po, or the be owner of the task
    public boolean hasPermissionToEdit(String token, int taskId) {
        UserEntity userEntity = userDao.findUserByToken(token);
        if (userEntity != null) {
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

        UserEntity userEntity = userDao.findUserByToken(token);
        if (userEntity != null) {
            if (BCrypt.checkpw(userPasswordUpdateDto.getOldPassword(), userEntity.getPassword())) {
                String encryptedPassword = BCrypt.hashpw(userPasswordUpdateDto.getNewPassword(), BCrypt.gensalt());
                userEntity.setPassword(encryptedPassword);
                userDao.merge(userEntity);
                return true;
            }
        }
        return false;
    }

    //Function that receives a token and returns the user role
    public String getUserRole(String token) {
        UserEntity userEntity = userDao.findUserByToken(token);
        if (userEntity != null) {
            return userEntity.getRole();
        }
        return null;
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
            addUser(userDto);
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
            addUser(userDto);
            changeStatus("deleted",false);
        }

    }
}
