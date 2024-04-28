package aor.paj.bean;

import aor.paj.dao.MessageDao;
import aor.paj.dao.TokenDao;
import aor.paj.dao.UserDao;
import aor.paj.dto.MessageDto;
import aor.paj.entity.MessageEntity;
import aor.paj.entity.NotificationEntity;
import aor.paj.entity.UserEntity;
import aor.paj.websocket.Notifier;
import aor.paj.websocket.bean.HandleWebSockets;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MessageBeanTest {

    @Mock
    private UserDao userDao;

    @Mock
    private MessageDao messageDao;

    @Mock
    private TokenDao tokenDao;

    @Mock
    private HandleWebSockets handleWebSockets;

    @Mock
    private Notifier notifier;

    @InjectMocks
    private MessageBean messageBean;

    @Test
    void testGetMessagesBetweenUsers() {
        // Given
        String usernameX = "userX";
        String usernameY = "userY";
        String token = "token";

        UserEntity userEntity = new UserEntity();
        userEntity.setUsername(usernameX);

        MessageEntity messageEntity = new MessageEntity();
        messageEntity.setSender_id(userEntity);
        messageEntity.setReceiver_id(userEntity);
        messageEntity.setMessage("Hello");
        messageEntity.setIsRead(true);

        when(tokenDao.findUserByTokenString(token)).thenReturn(userEntity);
        when(messageDao.findMessagesBySenderAndReceiver(usernameX, usernameY)).thenReturn(Arrays.asList(messageEntity));
        when(messageDao.findMessagesBySenderAndReceiver(usernameY, usernameX)).thenReturn(Arrays.asList(messageEntity));
        // When
        List<MessageDto> result = messageBean.getMessagesBetweenUsers(usernameX, usernameY, token);

        // Then
        assertEquals(2, result.size());
        assertEquals("Hello", result.get(0).getMessage());
        assertEquals("Hello", result.get(1).getMessage());
    }

    @Test
    void testConvertMessageEntityToMessageDto() {
        // Given
        UserEntity userEntity = new UserEntity();
        userEntity.setUsername("user");

        MessageEntity messageEntity = new MessageEntity();
        messageEntity.setSender_id(userEntity);
        messageEntity.setReceiver_id(userEntity);
        messageEntity.setMessage("Hello");
        messageEntity.setIsRead(true);

        // When
        MessageDto result = messageBean.convertMessageEntityToMessageDto(messageEntity);

        // Then
        assertEquals("Hello", result.getMessage());
        assertEquals("user", result.getSender());
        assertEquals("user", result.getReceiver());
        assertEquals(true, result.isRead());
    }

    @Test
    void testConvertMessageDtoToMessageEntity() {
        // Given
        UserEntity userEntity = new UserEntity();
        userEntity.setUsername("user");

        MessageDto messageDto = new MessageDto();
        messageDto.setSender("user");
        messageDto.setReceiver("user");
        messageDto.setMessage("Hello");
        messageDto.setRead(true);

        when(userDao.findUserByUsername("user")).thenReturn(userEntity);

        // When
        MessageEntity result = messageBean.convertMessageDtoToMessageEntity(messageDto);

        // Then
        assertEquals("Hello", result.getMessage());
        assertEquals(userEntity, result.getSender_id());
        assertEquals(userEntity, result.getReceiver_id());
        assertEquals(true, result.isRead());
    }
}