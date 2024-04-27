package aor.paj.bean;

import aor.paj.dao.TaskDao;
import aor.paj.dao.UserDao;
import aor.paj.dto.RegistrationDataDto;
import aor.paj.dto.UserStatisticsDto;
import aor.paj.websocket.Notifier;
import aor.paj.websocket.bean.HandleWebSockets;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StatisticBeanTest {

    @Mock
    private UserDao userDao;

    @Mock
    private TaskDao taskDao;

    @Mock
    private HandleWebSockets handleWebSockets;

    @Mock
    private Notifier notifier;

    @InjectMocks
    private StatisticBean statisticBean;

    @Test
    void testGetStatisticsUsers() {
        // Given
        when(userDao.getUnconfirmedUserCount()).thenReturn(10);
        when(userDao.getConfirmedUserCount()).thenReturn(20);
        when(userDao.getUserCount()).thenReturn(30);
        when(userDao.getActiveUserCount()).thenReturn(40);
        when(userDao.getInactiveUserCount()).thenReturn(50);

        // When
        UserStatisticsDto result = statisticBean.getStatisticsUsers();

        // Then
        assertEquals(10, result.getUnconfirmedUsers());
        assertEquals(20, result.getConfirmedUsers());
        assertEquals(30, result.getCountUsers());
        assertEquals(40, result.getActiveUsers());
        assertEquals(50, result.getInactiveUsers());
    }

    @Test
    void testGetCompletedTasksOverTime() {
        // Given
        List<Object[]> mockResults = new ArrayList<>();
        mockResults.add(new Object[]{2022, 1, 10L});
        mockResults.add(new Object[]{2022, 2, 20L});
        mockResults.add(new Object[]{2022, 3, 30L});

        when(taskDao.getCompletedTasksByTime()).thenReturn(mockResults);

        // When
        List<RegistrationDataDto> result = statisticBean.getCompletedTasksOverTime();

        // Then
        assertEquals(3, result.size());

        assertEquals(2022, result.get(0).getYear());
        assertEquals(1, result.get(0).getMonth());
        assertEquals(10, result.get(0).getCount());

        assertEquals(2022, result.get(1).getYear());
        assertEquals(2, result.get(1).getMonth());
        assertEquals(30, result.get(1).getCount()); // 10 (previous) + 20

        assertEquals(2022, result.get(2).getYear());
        assertEquals(3, result.get(2).getMonth());
        assertEquals(60, result.get(2).getCount()); // 30 (previous) + 30
    }

}