package aor.paj.bean;

import aor.paj.bean.UserBean;
import aor.paj.dao.UserDao;
import aor.paj.dto.UserDto;
import aor.paj.dao.TaskDao;
import aor.paj.dto.UserPasswordUpdateDto;
import aor.paj.entity.TaskEntity;
import aor.paj.entity.UserEntity;
import aor.paj.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mindrot.jbcrypt.BCrypt;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserBeanTest {

    @Mock
    private UserDao userDao;

    @Mock
    private TaskDao taskDao; // Mock the TaskDao

    @Mock
    private UserMapper userMapper; // Mock the UserMapper

    @InjectMocks
    private UserBean userBean;

    @Test
    void testIsValidUserByToken() {
        // Given: Define the test inputs
        String token = "testToken";

        UserEntity userEntity = new UserEntity();
        userEntity.setActive(true);

        // When: Define the behavior of the mocks
        // When userDao.findUserByToken is called with the test token, return the test UserEntity
        when(userDao.findUserByToken(token)).thenReturn(userEntity);

        // Then: Assert the expected results and verify the interactions with the mocks
        // Assert that userBean.isValidUserByToken returns true when called with the test token
        assertTrue(userBean.isValidUserByToken(token));

        // Verify that userDao.findUserByToken was called with the test token
        verify(userDao).findUserByToken(token);
    }

    @Test
    void testUserExists() {
        // Given: Define the test inputs
        UserDto userDto = new UserDto();
        userDto.setUsername("testUser");
        userDto.setEmail("testEmail");

        UserEntity userEntity = new UserEntity();
        userEntity.setUsername(userDto.getUsername());
        userEntity.setEmail(userDto.getEmail());

        // When: Define the behavior of the mocks
        // When userDao.findUserByUsername is called with the test username, return the test UserEntity
        when(userDao.findUserByUsername(userDto.getUsername())).thenReturn(userEntity);

        // Then: Assert the expected results and verify the interactions with the mocks
        // Assert that userBean.userExists returns true when called with the test UserDto
        assertTrue(userBean.userExists(userDto));

        // Verify that userDao.findUserByUsername was called with the test username
        verify(userDao).findUserByUsername(userDto.getUsername());

        // Verify that userDao.findUserByEmail was not called
        verify(userDao, never()).findUserByEmail(userDto.getEmail());
    }

    @Test
    void testLogin() {
        // Given: Define the test inputs
        String username = "testUser";
        String password = "testPassword";
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

        UserEntity userEntity = new UserEntity();
        userEntity.setUsername(username);
        userEntity.setPassword(hashedPassword);

        // When: Define the behavior of the mocks
        // When userDao.findUserByUsername is called with the test username, return the test UserEntity
        when(userDao.findUserByUsername(username)).thenReturn(userEntity);

        // Then: Assert the expected results and verify the interactions with the mocks
        // Assert that userBean.login returns a non-null token when called with the test username and password
        assertNotNull(userBean.login(username, password));

        // Verify that userDao.findUserByUsername was called with the test username
        verify(userDao).findUserByUsername(username);

        // Verify that userDao.merge was called with the test UserEntity
        verify(userDao).merge(userEntity);
    }

    @Test
    void testUpdatePassword() {
        // Given: Define the test inputs
        String token = "testToken";
        String oldPassword = "oldPassword";
        String newPassword = "newPassword";
        String hashedOldPassword = BCrypt.hashpw(oldPassword, BCrypt.gensalt());

        UserPasswordUpdateDto userPasswordUpdateDto = new UserPasswordUpdateDto();
        userPasswordUpdateDto.setOldPassword(oldPassword);
        userPasswordUpdateDto.setNewPassword(newPassword);

        UserEntity userEntity = new UserEntity();
        userEntity.setPassword(hashedOldPassword);

        // When: Define the behavior of the mocks
        // When userDao.findUserByToken is called with the test token, return the test UserEntity
        when(userDao.findUserByToken(token)).thenReturn(userEntity);

        // Then: Assert the expected results and verify the interactions with the mocks
        // Assert that userBean.updatePassword returns true when called with the test UserPasswordUpdateDto and token
        assertTrue(userBean.updatePassword(userPasswordUpdateDto, token));

        // Verify that userDao.findUserByToken was called with the test token
        verify(userDao).findUserByToken(token);

        // Verify that userDao.merge was called with the test UserEntity
        verify(userDao).merge(userEntity);
    }

    @Test
    void testChangeTaskOwner() {
        // Given: Define the test inputs
        String oldUsername = "oldUser";
        String newUsername = "newUser";

        UserEntity oldUserEntity = new UserEntity();
        oldUserEntity.setId(1);
        oldUserEntity.setUsername(oldUsername);

        UserEntity newUserEntity = new UserEntity();
        newUserEntity.setId(2);
        newUserEntity.setUsername(newUsername);

        TaskEntity taskEntity = new TaskEntity();
        taskEntity.setId(1);
        taskEntity.setOwner(oldUserEntity);

        // When: Define the behavior of the mocks
        // When userDao.findUserByUsername is called with the old or new username, return the corresponding UserEntity
        when(userDao.findUserByUsername(oldUsername)).thenReturn(oldUserEntity);
        when(userDao.findUserByUsername(newUsername)).thenReturn(newUserEntity);

        // When taskDao.findTaskByOwnerId is called with the old user id, return a list containing the test TaskEntity
        when(taskDao.findTaskByOwnerId(oldUserEntity.getId())).thenReturn(Collections.singletonList(taskEntity));

        // Then: Assert the expected results and verify the interactions with the mocks
        // Assert that userBean.changeTaskOwner returns true when called with the old and new usernames
        assertTrue(userBean.changeTaskOwner(oldUsername, newUsername));

        // Verify that userDao.findUserByUsername was called with the old and new usernames
        verify(userDao).findUserByUsername(oldUsername);
        verify(userDao).findUserByUsername(newUsername);

        // Verify that taskDao.findTaskByOwnerId was called with the old user id
        verify(taskDao).findTaskByOwnerId(oldUserEntity.getId());

        // Verify that taskDao.merge was called with the test TaskEntity
        verify(taskDao).merge(taskEntity);
    }










}