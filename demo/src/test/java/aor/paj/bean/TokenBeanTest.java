package aor.paj.bean;

import aor.paj.bean.TokenBean;
import aor.paj.dao.TaskDao;
import aor.paj.dao.TokenDao;
import aor.paj.dao.UserDao;
import aor.paj.dto.TokenExpirationUpdateDto;
import aor.paj.entity.TokenEntity;
import aor.paj.entity.UserEntity;
import aor.paj.utils.TokenStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mindrot.jbcrypt.BCrypt;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TokenBeanTest {

    @Mock
    private UserDao userDao;

    @Mock
    private TokenDao tokenDao;

    @Mock
    private TaskDao taskDao;

    @InjectMocks
    private TokenBean tokenBean;

    @Test
    void testCreateToken() {
        // Given
        String token = "testToken";
        String username = "testUser";
        UserEntity userEntity = new UserEntity();
        userEntity.setUsername(username);
        userEntity.setRole("dev");

        // When
        when(userDao.findUserByUsername(username)).thenReturn(userEntity);

        // Then
        TokenEntity result = tokenBean.createToken(token, username);

        assertNotNull(result);
        assertEquals(token, result.getToken());
        assertEquals(userEntity, result.getUser());
    }

    @Test
    void testGetUserRole() {
        // Given
        String token = "testToken";
        TokenEntity tokenEntity = new TokenEntity();
        UserEntity userEntity = new UserEntity();
        userEntity.setRole("testRole");
        tokenEntity.setUser(userEntity);

        // When
        when(tokenDao.findTokenByToken(token)).thenReturn(tokenEntity);

        // Then
        String result = tokenBean.getUserRole(token);

        assertEquals("testRole", result);
    }

    @Test
    void testIsProductOwner() {
        // Given
        String token = "testToken";
        TokenEntity tokenEntity = new TokenEntity();
        UserEntity userEntity = new UserEntity();
        userEntity.setRole("po");
        tokenEntity.setUser(userEntity);

        // When
        when(tokenDao.findTokenByToken(token)).thenReturn(tokenEntity);

        // Then
        boolean result = tokenBean.isProductOwner(token);

        assertTrue(result);
    }

    @Test
    void testIsValidUserByToken() {
        // Given
        String token = "testToken";
        TokenEntity tokenEntity = new TokenEntity();
        UserEntity userEntity = new UserEntity();
        userEntity.setConfirmed(true);
        userEntity.setActive(true);
        userEntity.setRole("dev");
        tokenEntity.setUser(userEntity);
        tokenEntity.setExpiration(Instant.now().plus(Duration.ofMinutes(10))); // Token is valid for 10 minutes

        // When
        when(tokenDao.findTokenByToken(token)).thenReturn(tokenEntity);

        // Then
        TokenStatus result = tokenBean.isValidUserByToken(token);

        assertEquals(TokenStatus.VALID, result);
    }

    @Test
    void testSetDefaultTokenExpiration() {
        // Given
        TokenEntity tokenEntity = new TokenEntity();
        UserEntity userEntity = new UserEntity();
        userEntity.setRole("po");
        tokenEntity.setUser(userEntity);

        // When
        tokenBean.setDefaultTokenExpiration(tokenEntity);

        // Then
        assertNotNull(tokenEntity.getExpiration());
        assertTrue(tokenEntity.getExpiration().isAfter(Instant.now()));
        assertTrue(tokenEntity.getExpiration().isBefore(Instant.now().plus(Duration.ofMinutes(TokenBean.getPoTokenExpirationMinutes() + 1))));
    }

    @Test
    void testChangeTokenExpiration() {
        // Given
        String token = "testToken";
        TokenExpirationUpdateDto tokenExpirationUpdateDto = new TokenExpirationUpdateDto();
        tokenExpirationUpdateDto.setDefaultTokenExpirationMinutes(30);
        tokenExpirationUpdateDto.setPoTokenExpirationMinutes(45);

        TokenEntity tokenEntity = new TokenEntity();
        UserEntity userEntity = new UserEntity();
        userEntity.setConfirmed(true);
        userEntity.setActive(true);
        userEntity.setRole("po");
        tokenEntity.setUser(userEntity);
        tokenEntity.setToken(token);
        tokenEntity.setExpiration(Instant.now().plus(Duration.ofMinutes(10))); // Token is valid for 10 minutes

        // When
        when(tokenDao.findTokenByToken(token)).thenReturn(tokenEntity);

        // Then
        tokenBean.changeTokenExpiration(tokenExpirationUpdateDto, token);

        assertEquals(30, TokenBean.getDefaultTokenExpirationMinutes());
        assertEquals(45, TokenBean.getPoTokenExpirationMinutes());
    }

}