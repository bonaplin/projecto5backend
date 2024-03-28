package aor.paj.bean;

import aor.paj.bean.TaskBean;
import aor.paj.dao.TaskDao;
import aor.paj.dao.UserDao;
import aor.paj.dto.TaskDto;
import aor.paj.entity.TaskEntity;
import aor.paj.entity.UserEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class TaskBeanTest {

    @Mock
    private TaskDao taskDao;

    @InjectMocks
    private TaskBean taskBean;

    // Mock the UserDao
    @Mock
    private UserDao userDao;


    @Test // Annotates the method as a test method for JUnit
    void testTaskTitleExists() {
        // Given
        TaskDto taskDto = new TaskDto(); // Create a new TaskDto object
        taskDto.setTitle("Test Task"); // Set the title of the TaskDto object

        TaskEntity taskEntity = new TaskEntity(); // Create a new TaskEntity object
        taskEntity.setTitle("Test Task"); // Set the title of the TaskEntity object

        // When
        when(taskDao.findTaskByTitle(taskDto.getTitle())).thenReturn(taskEntity);
        // Define the behavior of the mock object taskDao. When the method findTaskByTitle is called with the title of the taskDto, it returns the taskEntity

        // Then
        assertTrue(taskBean.taskTitleExists(taskDto));
        // Assert that the method taskTitleExists of taskBean returns true when called with the taskDto

        verify(taskDao).findTaskByTitle(taskDto.getTitle());
        // Verify that the method findTaskByTitle of the mock object taskDao was called with the title of the taskDto
    }

    @Test // Annotates the method as a test method for JUnit
    void testGetAllTasks() {
        // Given
        List<TaskEntity> taskEntities = new ArrayList<>(); // Create a list of TaskEntity objects
        TaskEntity taskEntity = new TaskEntity(); // Create a new TaskEntity object
        taskEntities.add(taskEntity); // Add the TaskEntity object to the list

        when(taskDao.getAllTasks()).thenReturn(taskEntities); // Define the behavior of the mock object taskDao. When the method getAllTasks is called, it returns the list of TaskEntity objects

        // When
        List<TaskDto> taskDtos = taskBean.getAllTasks(); // Call the method getAllTasks and store the returned list of TaskDto objects

        // Then
        assertFalse(taskDtos.isEmpty()); // Assert that the returned list is not empty
        assertEquals(taskEntities.size(), taskDtos.size()); // Assert that the size of the returned list is equal to the size of the list of TaskEntity objects
        verify(taskDao).getAllTasks(); // Verify that the method getAllTasks of the mock object taskDao was called
    }

    @Test // Annotates the method as a test method for JUnit
    void testDesactivateTask() {
        // Given
        int id = 1; // Define an id for the task
        TaskEntity taskEntity = new TaskEntity(); // Create a new TaskEntity object
        taskEntity.setId(id); // Set the id of the TaskEntity object
        taskEntity.setActive(true); // Set the active status of the TaskEntity object to true
        // When
        when(taskDao.findTaskById(id)).thenReturn(taskEntity);
        // Define the behavior of the mock object taskDao. When the method findTaskById is called with the id, it returns the taskEntity

        doNothing().when(taskDao).merge(taskEntity);
        // Define the behavior of the mock object taskDao. When the method merge is called with the taskEntity, it does nothing

        // Then
        assertTrue(taskBean.desactivateTask(id));
        // Assert that the method desactivateTask of taskBean returns true when called with the id

        assertFalse(taskEntity.getActive());
        // Assert that the active status of the taskEntity is false

        verify(taskDao).findTaskById(id);
        // Verify that the method findTaskById of the mock object taskDao was called with the id

        verify(taskDao).merge(taskEntity);
        // Verify that the method merge of the mock object taskDao was called with the taskEntity
    }

    // Define a test method
    @Test
    void testTaskBelongsToUser() {
        // Given: Define the test inputs
        String token = "testToken";
        int id = 1;

        // Create a UserEntity and set its id
        UserEntity userEntity = new UserEntity();
        userEntity.setId(id);

        // Create a TaskEntity and set its owner
        TaskEntity taskEntity = new TaskEntity();
        taskEntity.setOwner(userEntity);

        // When: Define the behavior of the mocks
        // When userDao.findUserByToken is called with the test token, return the test UserEntity
        when(userDao.findUserByToken(token)).thenReturn(userEntity);
        // When taskDao.findTaskById is called with the test id, return the test TaskEntity
        when(taskDao.findTaskById(id)).thenReturn(taskEntity);

        // Then: Assert the expected results and verify the interactions with the mocks
        // Assert that taskBean.taskBelongsToUser returns true when called with the test token and id
        assertTrue(taskBean.taskBelongsToUser(token, id));

        // Verify that userDao.findUserByToken was called with the test token
        verify(userDao).findUserByToken(token);
        // Verify that taskDao.findTaskById was called with the test id
        verify(taskDao).findTaskById(id);
    }

    @Test
    void testDeleteTask() {
        // Given: Define the test inputs
        String title = "testTitle";

        TaskEntity taskEntity = new TaskEntity(); // Create a new TaskEntity object
        taskEntity.setTitle(title); // Set the title of the TaskEntity object

        // When: Define the behavior of the mocks
        // When taskDao.findTaskByTitle is called with the test title, return the test TaskEntity
        when(taskDao.findTaskByTitle(title)).thenReturn(taskEntity);

        // Then: Assert the expected results and verify the interactions with the mocks
        // Assert that taskBean.deleteTask returns true when called with the test title
        assertTrue(taskBean.deleteTask(title));

        // Verify that taskDao.findTaskByTitle was called with the test title
        verify(taskDao).findTaskByTitle(title);

        // Verify that taskDao.remove was called with the test TaskEntity
        verify(taskDao).remove(taskEntity);
    }

}
