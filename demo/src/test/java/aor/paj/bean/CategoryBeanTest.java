package aor.paj.bean;

import aor.paj.bean.CategoryBean;
import aor.paj.dao.CategoryDao;
import aor.paj.dao.TaskDao;
import aor.paj.dao.UserDao;
import aor.paj.dto.CategoryDto;
import aor.paj.entity.CategoryEntity;
import aor.paj.entity.TaskEntity;
import aor.paj.entity.UserEntity;
import aor.paj.mapper.CategoryMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryBeanTest {

    @Mock
    private CategoryDao categoryDao; // Mock the CategoryDao

    @Mock
    private TaskDao taskDao; // Mock the TaskDao

    @Mock
    private UserDao userDao; // Mock the UserDao

    @InjectMocks
    private CategoryBean categoryBean; // Inject the mocks into CategoryBean



    @Test
    void testGetAllCategories() {
        // Given: Define the test inputs
        CategoryEntity categoryEntity = new CategoryEntity();
        categoryEntity.setTitle("testTitle");

        UserEntity userEntity = new UserEntity(); // Create a new UserEntity
        userEntity.setUsername("testUser"); // Set the username of the UserEntity
        categoryEntity.setOwner(userEntity); // Set the owner of the CategoryEntity

        // When: Define the behavior of the mocks
        // When categoryDao.getAllCategories is called, return a list containing the test CategoryEntity
        when(categoryDao.getAllCategories()).thenReturn(Collections.singletonList(categoryEntity));

        // Then: Assert the expected results and verify the interactions with the mocks
        // Call the method under test
        List<CategoryDto> result = categoryBean.getAllCategories();

        // Assert that the result is not null and has size 1
        assertNotNull(result);
        assertEquals(1, result.size());

        // Assert that the first element in the result list has the same title as the test CategoryEntity
        assertEquals(categoryEntity.getTitle(), result.get(0).getTitle());

        // Verify that categoryDao.getAllCategories was called
        verify(categoryDao).getAllCategories();
    }

    @Test
    void testDeleteCategory() {
        // Given: Define the test inputs
        String title = "testTitle";

        CategoryEntity categoryEntity = new CategoryEntity(); // Create a new CategoryEntity object
        categoryEntity.setTitle(title); // Set the title of the CategoryEntity object

        // When: Define the behavior of the mocks
        // When categoryDao.findCategoryByTitle is called with the test title, return the test CategoryEntity
        when(categoryDao.findCategoryByTitle(title)).thenReturn(categoryEntity);

        // When taskDao.findTasksByCategory is called with the test CategoryEntity, return an empty list
        when(taskDao.findTasksByCategory(categoryEntity)).thenReturn(Collections.emptyList());

        // Then: Assert the expected results and verify the interactions with the mocks
        // Assert that categoryBean.deleteCategory returns true when called with the test title
        assertTrue(categoryBean.deleteCategory(title));

        // Verify that categoryDao.findCategoryByTitle was called with the test title
        verify(categoryDao).findCategoryByTitle(title);

        // Verify that taskDao.findTasksByCategory was called with the test CategoryEntity
        verify(taskDao).findTasksByCategory(categoryEntity);

        // Verify that categoryDao.deleteCategory was called with the test CategoryEntity
        verify(categoryDao).deleteCategory(categoryEntity);
    }

    @Test
    void testUpdateCategory() {
        // Given: Define the test inputs
        String title = "testTitle";
        CategoryDto categoryDto = new CategoryDto();
        categoryDto.setTitle("newTitle");
        categoryDto.setDescription("newDescription");

        CategoryEntity categoryEntity = new CategoryEntity(); // Create a new CategoryEntity object
        categoryEntity.setTitle(title); // Set the title of the CategoryEntity object

        // When: Define the behavior of the mocks
        // When categoryDao.findCategoryByTitle is called with the test title, return the test CategoryEntity
        when(categoryDao.findCategoryByTitle(title)).thenReturn(categoryEntity);

        // Then: Assert the expected results and verify the interactions with the mocks
        // Assert that categoryBean.updateCategory returns true when called with the test CategoryDto and title
        assertTrue(categoryBean.updateCategory(categoryDto, title));

        // Verify that categoryDao.findCategoryByTitle was called with the test title
        verify(categoryDao).findCategoryByTitle(title);

        // Verify that categoryDao.merge was called with the test CategoryEntity
        verify(categoryDao).merge(categoryEntity);

        // Assert that the title and description of the CategoryEntity were updated
        assertEquals(categoryDto.getTitle(), categoryEntity.getTitle());
        assertEquals(categoryDto.getDescription(), categoryEntity.getDescription());
    }

    @Test
    void testIsValidCategory() {
        // Given: Define the test inputs
        CategoryDto categoryDto = new CategoryDto();
        categoryDto.setTitle("testTitle");
        categoryDto.setDescription("testDescription");

        // When: Define the behavior of the mocks
        // When categoryDao.findCategoryByTitle is called with the test title, return null
        when(categoryDao.findCategoryByTitle(categoryDto.getTitle())).thenReturn(null);

        // Then: Assert the expected results and verify the interactions with the mocks
        // Assert that categoryBean.isValidCategory returns true when called with the test CategoryDto
        assertTrue(categoryBean.isValidCategory(categoryDto));

        // Verify that categoryDao.findCategoryByTitle was called with the test title
        verify(categoryDao).findCategoryByTitle(categoryDto.getTitle());
    }

    @Test
    public void testGetNumberOfTasksByCategory() {
        // Given: Define the test inputs
        String title = "testTitle";
        CategoryEntity categoryEntity = new CategoryEntity(); // Create a new CategoryEntity
        TaskEntity taskEntity = new TaskEntity(); // Create a new TaskEntity

        // When: Define the behavior of the mocks
        // When categoryDao.findCategoryByTitle is called with the test title, return the test CategoryEntity
        when(categoryDao.findCategoryByTitle(title)).thenReturn(categoryEntity);

        // When taskDao.findTasksByCategory is called with the test CategoryEntity, return a list with the test TaskEntity
        when(taskDao.findTasksByCategory(categoryEntity)).thenReturn(Collections.singletonList(taskEntity));

        // Then: Assert the expected results
        // Assert that categoryBean.getNumberOfTasksByCategory returns 1 when called with the test title
        assertEquals(1, categoryBean.getNumberOfTasksByCategory(title));
    }
    
}