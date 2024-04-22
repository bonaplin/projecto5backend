package aor.paj.bean;

import aor.paj.dao.CategoryDao;
import aor.paj.dao.TaskDao;
import aor.paj.dao.TokenDao;
import aor.paj.dao.UserDao;
import aor.paj.dto.*;
import aor.paj.entity.CategoryEntity;
import aor.paj.entity.TaskEntity;
import aor.paj.entity.TokenEntity;
import aor.paj.entity.UserEntity;
import aor.paj.gson.InstantAdapter;
import aor.paj.mapper.CategoryMapper;
import aor.paj.mapper.TaskMapper;
import aor.paj.service.LocalDateAdapter;
import aor.paj.utils.JsonUtils;
import aor.paj.utils.MessageType;
import aor.paj.utils.State;
import aor.paj.utils.TokenStatus;
import aor.paj.websocket.Notifier;
import aor.paj.websocket.bean.HandleWebSockets;
import com.fasterxml.jackson.databind.cfg.MapperBuilder;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.websocket.Session;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static aor.paj.utils.MessageType.TASK_CREATE;

//@ApplicationScoped
@Stateless
public class TaskBean {

    @EJB
    UserDao userDao;

    @EJB
    TaskDao taskDao;

    @EJB
    CategoryDao categoryDao;

    @EJB
    TokenDao tokenDao;

    @EJB
    Notifier notifier;
    @EJB
    HandleWebSockets handleWebSockets;

    @Inject
    TokenBean tokenBean;

    @EJB
    StatisticBean statisticBean;

    Gson gson = new GsonBuilder()
            .registerTypeAdapter(Instant.class, new InstantAdapter())
            .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
            .create();

    /**
     * Function that receives a token and a taskDto and adds a new task to the database mysql
     * @param token
     * @param taskDto
     * @return
     */
    public boolean addTask(String token, TaskDto taskDto){
        TokenEntity tokenEntity = tokenDao.findTokenByToken(token);
        if(tokenEntity == null) return false;
        System.out.println("TokenEntity: " + tokenEntity);


        UserEntity userEntity = tokenEntity.getUser();
        System.out.println("UserEntity: " + userEntity);
        CategoryEntity categoryEntity = categoryDao.findCategoryByTitle(taskDto.getCategory());

        System.out.println(categoryEntity);
        TaskEntity taskEntity = TaskMapper.convertTaskDtoToTaskEntity(taskDto);

        taskEntity.setOwner(userEntity);
        taskEntity.setActive(true);
        taskEntity.setStatus(State.TODO.getValue());
        taskEntity.setCategory(categoryEntity);

        if(taskEntity.getInitialDate() == null) {
            taskEntity.setInitialDate(LocalDate.now());
        }

        taskDao.persist(taskEntity);

        sendNewTask(taskEntity);
        sendNumberOfTasksPerStatus();
        sendAvgTaskPerUser();
        return true;
    }

    private void sendNewTask(TaskEntity taskEntity) {
        TaskDto taskDto = TaskMapper.convertTaskEntityToTaskDto(taskEntity);

        JsonObject jsonObject = handleWebSockets.convertStringToJsonObject(gson.toJson(taskDto));
        System.out.println("Sending new task: " + taskDto);
        jsonObject.addProperty("owner", taskEntity.getOwner().getUsername());
        jsonObject.addProperty("type", TASK_CREATE.getValue());
        jsonObject.addProperty("id", taskEntity.getId());
        System.out.println("Sending new task to frontend: " + jsonObject);

        String json = gson.toJson(jsonObject);
        notifier.sendToAllSessions(json);
    }


    //Function that receives a taskdto and checks in database mysql if a task with the same title already exists
    public boolean taskTitleExists(TaskDto taskDto) {
        TaskEntity taskEntity = taskDao.findTaskByTitle(taskDto.getTitle());
        if (taskEntity != null) {
            if(taskDto.getId() == taskEntity.getId()){
                return false;
            }
            return true;
        }
        return false;
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
        sendNumberOfTasksPerStatus();
        sendCompletedTasksOverTime();
    }
    
    //Function that receives a task id and sets the task active to false in the database mysql
    public boolean desactivateTask(int id) {
        TaskEntity taskEntity = taskDao.findTaskById(id);
        if(taskEntity == null) return false;
        taskEntity.setActive(false);

        TaskDto taskDto = TaskMapper.convertTaskEntityToTaskDto(taskEntity);

        JsonObject taskDtoJson = gson.toJsonTree(taskDto).getAsJsonObject();
        taskDtoJson.addProperty("type", MessageType.TASK_DESACTIVATE.getValue());

        taskDao.merge(taskEntity);

        String taskDtoJsonString = taskDtoJson.toString();
        notifier.sendToAllSessions(taskDtoJsonString);
        sendNumberOfTasksPerStatus();
        sendCompletedTasksOverTime();
        return true;
    }
    
    //Function that receives a task id and a token and checks if the user its the owner of task with that id
    public boolean taskBelongsToUser(String token, int id) {
        TokenEntity tokenEntity = tokenDao.findTokenByToken(token);
        if (tokenEntity != null) {
            UserEntity userEntity = tokenEntity.getUser();
            TaskEntity taskEntity = taskDao.findTaskById(id);
            if (taskEntity.getOwner().getId() == userEntity.getId()) {
                return true;
            }
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
        int lastStatus = taskEntity.getStatus();
        taskEntity.setTitle(taskDto.getTitle());
        taskEntity.setDescription(taskDto.getDescription());
        taskEntity.setInitialDate(taskDto.getInitialDate());
        taskEntity.setFinalDate(taskDto.getFinalDate());
        taskEntity.setStatus(taskDto.getStatus());
        taskEntity.setPriority(taskDto.getPriority());
        taskEntity.setCategory(categoryDao.findCategoryByTitle(taskDto.getCategory()));
        taskDao.merge(taskEntity);
        sendNumberOfTasksPerStatus();
        if(!statusAsChanged(taskDto, lastStatus)) handleTaskEdit(taskEntity);
        else handleTaskEditMove(taskEntity, lastStatus);
    }

    private void handleTaskEdit(TaskEntity taskEntity) {
        TaskDto taskDto = TaskMapper.convertTaskEntityToTaskDto(taskEntity);

        // Convert taskDto to JsonObject
        JsonObject taskDtoJson = gson.toJsonTree(taskDto).getAsJsonObject();

        // Add "type" & "lastStatus property to taskDtoJson
        taskDtoJson.addProperty("type", MessageType.TASK_EDIT.getValue());

        // Convert taskDtoJson back to string
        String taskDtoJsonString = taskDtoJson.toString();

        // Send taskDtoJsonString to all logged in users
//        sendNumberOfTasksPerStatus();
        notifier.sendToAllSessions(taskDtoJsonString);
    }

    private void handleTaskEditMove(TaskEntity taskEntity, int lastStatus){
        TaskDto taskDto = TaskMapper.convertTaskEntityToTaskDto(taskEntity);

        // Convert taskDto to JsonObject
        JsonObject taskDtoJson = gson.toJsonTree(taskDto).getAsJsonObject();

        // Add "type" & "lastStatus property to taskDtoJson
        taskDtoJson.addProperty("type", MessageType.TASK_EDIT_AND_MOVE.getValue());
        taskDtoJson.addProperty("lastStatus", lastStatus);

        // Convert taskDtoJson back to string
        String taskDtoJsonString = taskDtoJson.toString();
//        sendNumberOfTasksPerStatus();
        // Send taskDtoJsonString to all logged in users
        notifier.sendToAllSessions(taskDtoJsonString);
    }

    private boolean statusAsChanged(TaskDto taskDto, int lastStatus) {
//        sendNumberOfTasksPerStatus();
        return taskDto.getStatus() != lastStatus;
    }

    public boolean restoreTask(int id) {
        TaskEntity taskEntity = taskDao.findTaskById(id);
        taskEntity.setActive(true);
        taskDao.merge(taskEntity);
        sendAvgTaskPerUser();
        sendNumberOfTasksPerStatus();
        return true;
    }

    public boolean deleteTask(int id) {
        TaskEntity taskEntity = taskDao.findTaskById(id);
        if(taskEntity == null) return false;

        TaskDto taskDto = TaskMapper.convertTaskEntityToTaskDto(taskEntity);

        JsonObject taskDtoJson = gson.toJsonTree(taskDto).getAsJsonObject();
        taskDtoJson.addProperty("type", MessageType.TASK_DELETE.getValue());

        taskDao.remove(taskEntity);

        String taskDtoJsonString = taskDtoJson.toString();
        notifier.sendToAllSessions(taskDtoJsonString);
        sendAvgTaskPerUser();
        sendNumberOfTasksPerStatus();
        return true;
    }

    //Function that checks all tasks active = false and sets them to true
    public boolean restoreAllTasks() {
        List<TaskEntity> taskEntities = taskDao.getAllTasks();
        for (TaskEntity taskEntity : taskEntities) {
            if (!taskEntity.getActive()) {
                taskEntity.setActive(true);
                taskDao.merge(taskEntity);
                sendAvgTaskPerUser();
                sendNumberOfTasksPerStatus();
                sendCompletedTasksOverTime();
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
                sendAvgTaskPerUser();
                sendNumberOfTasksPerStatus();
                sendCompletedTasksOverTime();
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

    public boolean handleTaskMove(Session session, JsonObject jsonObject) {
        int taskID = jsonObject.get("id").getAsInt();
        int status = jsonObject.get("status").getAsInt();

        String token = session.getPathParameters().get("token");

        TaskEntity taskEntity = taskDao.findTaskById(taskID);
        if(taskEntity == null) return false;
        int lastStatus = taskEntity.getStatus();
        TokenStatus tokenStatus = tokenBean.isValidUserByToken(token);
        if(tokenStatus != TokenStatus.VALID) return false;

        updateTaskStatus(taskID, status);

        TaskEntity taskEntityUpdated = taskDao.findTaskById(taskID);
        TaskDto taskDto = TaskMapper.convertTaskEntityToTaskDto(taskEntityUpdated);

        if(statusAsChanged(taskDto, lastStatus) && status == 300) {
            taskEntityUpdated.setDoneDate(LocalDate.now());
            taskDao.merge(taskEntityUpdated);
        }
        // Convert taskDto to JsonObject
        JsonObject taskDtoJson = gson.toJsonTree(taskDto).getAsJsonObject();

        // Add "type" & "lastStatus property to taskDtoJson
        taskDtoJson.addProperty("type", MessageType.TASK_MOVE.getValue());
        taskDtoJson.addProperty("lastStatus", lastStatus);
        taskDtoJson.addProperty("index", jsonObject.get("index").getAsInt());

        // Convert taskDtoJson back to string
        String taskDtoJsonString = taskDtoJson.toString();

        // Send taskDtoJsonString to all logged in users
        notifier.sendToAllSessions(taskDtoJsonString);
        //ab33
        sendNumberOfTasksPerStatus();
        return true;
    }


    // Para devolver as tarefas já organizadas por estado ou como um tod0 caso sejam para ver em lista. ***
    public Object getTasksBasedOnQueryParams(String category, String username, Boolean active) {
        List<TaskDto> allTasks;
        if (category != null && !category.isEmpty() && username != null && !username.isEmpty()) {
            allTasks = getTasksByCategoryAndOwner(category, username);
        } else if (category != null && !category.isEmpty()) {
            allTasks = getTasksByCategory(category);
        } else if (username != null && !username.isEmpty()) {
            allTasks = getTasksByOwner(username);
        } else if (active != null) {
            allTasks = active ? getActiveTasks() : getInactiveTasks();
            return allTasks; //***
        } else {
            allTasks = getActiveTasks();
        }

        TaskListsDto taskLists = new TaskListsDto();
        taskLists.setTodoTasks(allTasks.stream().filter(task -> task.getStatus() == 100).collect(Collectors.toList()));
        taskLists.setDoingTasks(allTasks.stream().filter(task -> task.getStatus() == 200).collect(Collectors.toList()));
        taskLists.setDoneTasks(allTasks.stream().filter(task -> task.getStatus() == 300).collect(Collectors.toList()));

        return taskLists;
    }

    public void sendAvgTaskPerUser(){
        statisticBean.sendAvgTaskPerUser(MessageType.STATISTIC_TASK);
    }

    public void sendNumberOfTasksPerStatus(){
        statisticBean.sendNumberOfTasksPerStatus(MessageType.STATISTIC_TASK_PER_STATUS);
    }
    public void sendCompletedTasksOverTime(){
        //ab33
        statisticBean.sendCompletedTasksOverTime();
    }

    public List<CategoryCountDto> getTasksPerCategory(){
        List<Object[]> results = taskDao.getTasksByCategory();
        List<CategoryCountDto> categoryData = new ArrayList<>();
        for (Object[] result : results) {
            String name = (String) result[0];
            Long count = (Long) result[1];
            categoryData.add(new CategoryCountDto(name, count));
        }
        return categoryData;
    }


}
