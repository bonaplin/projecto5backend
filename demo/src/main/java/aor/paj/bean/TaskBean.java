package aor.paj.bean;

import aor.paj.dao.CategoryDao;
import aor.paj.dao.TaskDao;
import aor.paj.dao.UserDao;
import aor.paj.dto.TaskDto;
import aor.paj.dto.UserDto;
import aor.paj.entity.CategoryEntity;
import aor.paj.entity.TaskEntity;
import aor.paj.entity.UserEntity;
import aor.paj.mapper.TaskMapper;
import aor.paj.utils.JsonUtils;
import aor.paj.utils.State;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class TaskBean {

    @EJB
    UserDao userDao;

    @EJB
    TaskDao taskDao;

    @EJB
    CategoryDao categoryDao;


   //Function that receives a token and a taskdto and creates a task with the user token as owner and adds the task to the database mysql
    public boolean addTask(String token, TaskDto taskDto) {
        UserEntity userEntity = userDao.findUserByToken(token);
        CategoryEntity categoryEntity = categoryDao.findCategoryByTitle(taskDto.getCategory());
        TaskEntity taskEntity = TaskMapper.convertTaskDtoToTaskEntity(taskDto);

        taskEntity.setOwner(userEntity);
        taskEntity.setActive(true);
        taskEntity.setId(generateTaskId());
        taskEntity.setStatus(State.TODO.getValue());
        taskEntity.setCategory(categoryEntity);
        if(taskEntity.getInitialDate() == null) {
            taskEntity.setInitialDate(LocalDate.now());
        }
        taskDao.persist(taskEntity);
        return true;
    }

    //Function that receives a taskdto and checks in database mysql if a task with the same title already exists
    public boolean taskTitleExists(TaskDto taskDto) {
        TaskEntity taskEntity = taskDao.findTaskByTitle(taskDto.getTitle());
        if (taskEntity != null) {
            return true;
        }
        return false;
    }

    //Function that generates a unique id for new task checking in database mysql if the id already exists
    public int generateTaskId() {
        int id = 1;
        boolean idAlreadyExists;

        do {
            idAlreadyExists = false;
            TaskEntity taskEntity = taskDao.findTaskById(id);
            if (taskEntity != null) {
                id++;
                idAlreadyExists = true;
            }
        } while (idAlreadyExists);

        return id;
    }

    //Function that returns all tasks from the database mysql
    public List<TaskDto> getAllTasks() {
        List<TaskEntity> taskEntities = taskDao.getAllTasks();
        ArrayList<TaskDto> taskDtos = new ArrayList<>();
        for (TaskEntity taskEntity : taskEntities) {
            taskDtos.add(TaskMapper.convertTaskEntityToTaskDto(taskEntity));
        }
        return taskDtos;
    }
    public List<TaskDto> getActiveTasks() {
        List<TaskEntity> taskEntities = taskDao.getActiveTasks();
        ArrayList<TaskDto> taskDtos = new ArrayList<>();
        for (TaskEntity taskEntity : taskEntities) {
            taskDtos.add(TaskMapper.convertTaskEntityToTaskDto(taskEntity));
        }
        return taskDtos;
    }
    public List<TaskDto> getInactiveTasks(){
        List<TaskEntity> taskEntities = taskDao.getInactiveTasks();
        ArrayList<TaskDto> taskDtos = new ArrayList<>();
        for (TaskEntity taskEntity : taskEntities) {
            taskDtos.add(TaskMapper.convertTaskEntityToTaskDto(taskEntity));
        }
        return taskDtos;
    }

    //Function that receives a task id and a new task status and updates the task status in the database mysql
    public void updateTaskStatus(int id, int status) {
        TaskEntity taskEntity = taskDao.findTaskById(id);
        taskEntity.setStatus(status);
        taskDao.merge(taskEntity);
    }
    
    //Function that receives a task id and sets the task active to false in the database mysql
    public boolean desactivateTask(int id) {
        TaskEntity taskEntity = taskDao.findTaskById(id);
        taskEntity.setActive(false);
        taskDao.merge(taskEntity);
        return true;
    }
    
    //Function that receives a task id and a token and checks if the user its the owner of task with that id
    public boolean taskBelongsToUser(String token, int id) {
        UserEntity userEntity = userDao.findUserByToken(token);
        TaskEntity taskEntity = taskDao.findTaskById(id);
        if (taskEntity.getOwner().getId() == userEntity.getId()) {
            return true;
        }
        return false;
    }

    //Function that receives a task id and returns the task from the database mysql
    public TaskDto getTaskById(int id) {
        TaskEntity taskEntity = taskDao.findTaskById(id);
        return TaskMapper.convertTaskEntityToTaskDto(taskEntity);
    }

    public void updateTask(TaskDto taskDto, int id) {
        TaskEntity taskEntity = taskDao.findTaskById(id);
        taskEntity.setTitle(taskDto.getTitle());
        taskEntity.setDescription(taskDto.getDescription());
        taskEntity.setInitialDate(taskDto.getInitialDate());
        taskEntity.setFinalDate(taskDto.getFinalDate());
        taskEntity.setStatus(taskDto.getStatus());
        taskEntity.setPriority(taskDto.getPriority());
        taskEntity.setCategory(categoryDao.findCategoryByTitle(taskDto.getCategory()));
        taskDao.merge(taskEntity);
    }

    //Function that receives a task name and sets the task active to true in the database mysql
    public boolean restoreTask(String title) {
        TaskEntity taskEntity = taskDao.findTaskByTitle(title);
        taskEntity.setActive(true);
        taskDao.merge(taskEntity);
        return true;
    }
    public boolean restoreTask(int id) {
        TaskEntity taskEntity = taskDao.findTaskById(id);
        taskEntity.setActive(true);
        taskDao.merge(taskEntity);
        return true;
    }

    //Function that receives a task name and deletes the task from the database mysql
    public boolean deleteTask(String title) {
        TaskEntity taskEntity = taskDao.findTaskByTitle(title);
        taskDao.remove(taskEntity);
        return true;
    }
    public boolean deleteTask(int id) {
        TaskEntity taskEntity = taskDao.findTaskById(id);
        taskDao.remove(taskEntity);
        return true;
    }

    //Function that checks all tasks active = false and sets them to true
    public boolean restoreAllTasks() {
        List<TaskEntity> taskEntities = taskDao.getAllTasks();
        for (TaskEntity taskEntity : taskEntities) {
            if (!taskEntity.getActive()) {
                taskEntity.setActive(true);
                taskDao.merge(taskEntity);
            }
        }
        return true;
    }

    //Function that deletes all tasks from the database mysql that are active = false, returns true if all tasks were deleted
    public boolean deleteAllTasks() {
        List<TaskEntity> taskEntities = taskDao.getAllTasks();
        for (TaskEntity taskEntity : taskEntities) {
            if (!taskEntity.getActive()) {
                taskDao.remove(taskEntity);
            }
        }
        return true;
    }

//    //Function that returns list of tasks filtered by category and owner from the database mysql
    public List<TaskDto> getTasksByCategoryAndOwner(String category, String owner){
        UserEntity userEntity = userDao.findUserByUsername(owner);
        CategoryEntity categoryEntity = categoryDao.findCategoryByTitle(category);
        List<TaskEntity> taskEntities = taskDao.getTasksByCategoryAndOwner(userEntity, categoryEntity);
        ArrayList<TaskDto> taskDtos = new ArrayList<>();
        for (TaskEntity taskEntity : taskEntities) {
            taskDtos.add(TaskMapper.convertTaskEntityToTaskDto(taskEntity));
        }
        return taskDtos;
    }
//public List<TaskDto> getActiveTasksByCategoryAndOwnerAndStatus(String category, String owner, Integer status){
//    UserEntity userEntity = userDao.findUserByUsername(owner);
//    CategoryEntity categoryEntity = categoryDao.findCategoryByTitle(category);
//    List<TaskEntity> taskEntities = taskDao.getTasksByStatusAndOwnerAndCategory(status, userEntity, categoryEntity);
//    ArrayList<TaskDto> taskDtos = new ArrayList<>();
//    for (TaskEntity taskEntity : taskEntities) {
//        taskDtos.add(TaskMapper.convertTaskEntityToTaskDto(taskEntity));
//    }
//    return taskDtos;
//}


    //Function that returns list of tasks filtered by category from the database mysql
    public List<TaskDto> getTasksByCategory(String category){
        CategoryEntity categoryEntity = categoryDao.findCategoryByTitle(category);
        List<TaskEntity> taskEntities = taskDao.findTasksByCategory(categoryEntity);
        ArrayList<TaskDto> taskDtos = new ArrayList<>();
        for (TaskEntity taskEntity : taskEntities) {
            taskDtos.add(TaskMapper.convertTaskEntityToTaskDto(taskEntity));
        }
        return taskDtos;
    }

    //Function that returns list of tasks filtered by owner from the database mysql
    public List<TaskDto> getTasksByOwner(String owner){
        UserEntity userEntity = userDao.findUserByUsername(owner);
        List<TaskEntity> taskEntities = taskDao.findTaskByOwnerId(userEntity.getId());
        ArrayList<TaskDto> taskDtos = new ArrayList<>();
        for (TaskEntity taskEntity : taskEntities) {
            taskDtos.add(TaskMapper.convertTaskEntityToTaskDto(taskEntity));
        }
        return taskDtos;
    }

    public List<TaskDto> getActiveStatusTasks(int status) {
        List<TaskEntity> taskEntities = taskDao.getActiveStatusTasks(status);
        ArrayList<TaskDto> taskDtos = new ArrayList<>();
        for (TaskEntity taskEntity : taskEntities) {
            taskDtos.add(TaskMapper.convertTaskEntityToTaskDto(taskEntity));
        }
        return taskDtos;
    }
}
