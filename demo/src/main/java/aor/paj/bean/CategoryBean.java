package aor.paj.bean;

import aor.paj.dao.CategoryDao;
import aor.paj.dao.TaskDao;
import aor.paj.dao.UserDao;
import aor.paj.dto.CategoryDto;
import aor.paj.dto.TaskDto;
import aor.paj.entity.CategoryEntity;
import aor.paj.entity.TaskEntity;
import aor.paj.entity.UserEntity;
import aor.paj.mapper.CategoryMapper;
import aor.paj.mapper.TaskMapper;
import aor.paj.mapper.UserMapper;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class CategoryBean {

    @EJB
    CategoryDao categoryDao;

    @EJB
    UserDao userDao;

    @EJB
    TaskDao taskDao;

    @Inject
    UserBean userbean;

    @Inject
    Log log;

    //Function that gets all categories from database my sql
    public List<CategoryDto> getAllCategories() {
        List<CategoryEntity> categoryEntities = categoryDao.getAllCategories();
        List<CategoryDto> categoryDtos = new ArrayList<>();
        for (CategoryEntity categoryEntity : categoryEntities) {
            categoryDtos.add(CategoryMapper.convertCategoryEntityToCategoryDto(categoryEntity));
        }
        return categoryDtos;
    }

    public boolean deleteCategory(int id) {
        CategoryEntity categoryEntity = categoryDao.findCategoryById(id);
        List<TaskEntity> taskEntities = taskDao.findTasksByCategory(categoryEntity);
        if (taskEntities.isEmpty()) {
            categoryDao.deleteCategory(categoryEntity);
            return true;
        }
        return false;
    }

//    public boolean updateCategory(CategoryDto categoryDto, String title) {
//        CategoryEntity categoryEntity = categoryDao.findCategoryByTitle(title);
//        if (categoryEntity == null) {
//            return false;
//        }
//        categoryEntity.setTitle(categoryDto.getTitle());
//        categoryEntity.setDescription(categoryDto.getDescription());
//        categoryDao.merge(categoryEntity);
//        log.logUserInfo(token,"Category updated: " + categoryDto.getId(), 1);
//        return true;
//    }
    public boolean updateCategory(CategoryDto categoryDto, int id, String token) {
        CategoryEntity categoryEntity = categoryDao.findCategoryById(id);
        if (categoryEntity == null) {
            return false;
        }
        categoryEntity.setTitle(categoryDto.getTitle());
        categoryEntity.setDescription(categoryDto.getDescription());
        categoryDao.merge(categoryEntity);
        log.logUserInfo(token,"Category updated: " + categoryDto.getId(), 1);
        return true;
    }

    //Function that receives a categorydto, checks if the category exists, and if all fields are not null or empty or over 255 chars.
    public boolean isValidCategory(CategoryDto categoryDto) {
        if (categoryDto.getTitle() == null || categoryDto.getDescription() == null) {
            return false;
        }
        if (categoryDto.getTitle().isEmpty() || categoryDto.getDescription().isEmpty()) {
            return false;
        }
        if (categoryDto.getTitle().length() > 255 || categoryDto.getDescription().length() > 255) {
            return false;
        }
        if(categoryDao.findCategoryByTitle(categoryDto.getTitle()) != null){
            return false;
        }

        return true;
    }

//    public boolean isValidCategoryUpdate(CategoryDto categoryDto, String originalTitle){
//        if (categoryDto.getTitle() == null || categoryDto.getDescription() == null) {
//            return false;
//        }
//        if (categoryDto.getTitle().isEmpty() || categoryDto.getDescription().isEmpty()) {
//            return false;
//        }
//        if (categoryDto.getTitle().length() > 255 || categoryDto.getDescription().length() > 255) {
//            return false;
//        }
//        if(categoryDao.findCategoryByTitle(categoryDto.getTitle()) != null && !categoryDto.getTitle().toLowerCase().equals(originalTitle.toLowerCase())){
//            return false;
//        }
//
//        return true;
//    }
    
    //Function that receives a categorydto, converts it to categoryentity using categorydto.getOwner() to get the userentity and adds the category to the database mysql
    public boolean addCategory(CategoryDto categoryDto, String token) {
        CategoryEntity categoryEntity = new CategoryEntity();
        UserEntity userEntity = userDao.findUserByUsername(categoryDto.getOwner());
        categoryEntity = CategoryMapper.convertCategoryDtoToCategoryEntity(categoryDto);
        categoryEntity.setOwner(userEntity);
//        categoryEntity.setId(generateIdDataBase());
        categoryDao.addCategory(categoryEntity);
        log.logUserInfo(token,"Category added: " + categoryDto.getTitle(), 1);
        return true;
    }

    //Function that receives a category title and returns the number of tasks with that category
    public int getNumberOfTasksByCategory(String title) {
        CategoryEntity categoryEntity = categoryDao.findCategoryByTitle(title);
        List<TaskEntity> taskEntities = taskDao.findTasksByCategory(categoryEntity);
        return taskEntities.size();
    }


    //Function that verifys all categories in the database my sql and generates a unique id for new category
//    public int generateIdDataBase() {
//        int id = 1;
//        boolean idAlreadyExists;
//
//        do {
//            idAlreadyExists = false;
//            CategoryEntity categoryEntity = categoryDao.findCategoryById(id);
//            if (categoryEntity != null) {
//                id++;
//                idAlreadyExists = true;
//            }
//        } while (idAlreadyExists);
//        return id;
//    }

    public void createDefaultCategoryIfNotExistent() {
        if (!categoryDao.findCategoryByName("Backlog")) {
            CategoryDto categoryDto = new CategoryDto();
            categoryDto.setTitle("Backlog");
            categoryDto.setDescription("Backlog category");
            categoryDto.setOwner("admin");
            addCategory(categoryDto,"StartBean");
        }
        if (!categoryDao.findCategoryByName("Java")) {
            CategoryDto categoryDto = new CategoryDto();
            categoryDto.setTitle("Java");
            categoryDto.setDescription("Java category");
            categoryDto.setOwner("admin");
            addCategory(categoryDto,"StartBean");
        }

    }
}
