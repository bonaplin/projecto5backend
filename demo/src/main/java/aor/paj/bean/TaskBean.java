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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.ThreadContext;

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

    @Inject
    Log log;

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


        UserEntity userEntity = tokenEntity.getUser();
        CategoryEntity categoryEntity = categoryDao.findCategoryByTitle(taskDto.getCategory());

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
        sendCategoryCount();
        log.logUserInfo(token, "Task created with "+taskEntity.getId()+" id", 1);
        return true;
    }

    //websockets
    private void sendNewTask(TaskEntity taskEntity) {
        TaskDto taskDto = TaskMapper.convertTaskEntityToTaskDto(taskEntity);

        JsonObject jsonObject = handleWebSockets.convertStringToJsonObject(gson.toJson(taskDto));

        jsonObject.addProperty("owner", taskEntity.getOwner().getUsername());
        jsonObject.addProperty("type", TASK_CREATE.getValue());
        jsonObject.addProperty("id", taskEntity.getId());


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
    public void updateTaskStatus(int id, int status, String token) {
        TaskEntity taskEntity = taskDao.findTaskById(id);
        if(status == 300){
            taskEntity.setDoneDate(LocalDate.now());
        }
        taskEntity.setStatus(status);
        taskDao.merge(taskEntity);
        sendNumberOfTasksPerStatus();
        sendCompletedTasksOverTime();
        sendCategoryCount();

        log.logUserInfo(token, "Task status updated with "+id+" id", 1);
    }
    
    //Function that receives a task id and sets the task active to false in the database mysql
    public boolean desactivateTask(int id, String token) {
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
        sendCategoryCount();
        log.logUserInfo(token, "Task desactivated with "+id+" id", 1);
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

    public void updateTask(TaskDto taskDto, int id, String token) {

        TokenEntity tokenEntity = tokenDao.findTokenByToken(token);
        if(tokenEntity == null) return;

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

        if(!statusAsChanged(taskDto, lastStatus)) handleTaskEdit(taskEntity);
        else handleTaskEditMove(taskEntity, lastStatus);
        sendCategoryCount();
        sendNumberOfTasksPerStatus();
        log.logUserInfo(token, "Task updated with "+id+" id", 1);
    }

    //websockets
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
    //websockets
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
        if(taskDto.getStatus() != lastStatus){
            sendCategoryCount();
            return true;
        }
//        sendNumberOfTasksPerStatus();
        return false;
    }

    public boolean restoreTask(int id, String token){
        TaskEntity taskEntity = taskDao.findTaskById(id);
        taskEntity.setActive(true);
        taskDao.merge(taskEntity);

        TaskDto taskDto = TaskMapper.convertTaskEntityToTaskDto(taskEntity);
        JsonObject taskDtoJson = gson.toJsonTree(taskDto).getAsJsonObject();
        taskDtoJson.addProperty("type", MessageType.TASK_RESTORE.getValue());

        String taskDtoJsonString = taskDtoJson.toString();
        notifier.sendToAllSessions(taskDtoJsonString);

        sendAvgTaskPerUser();
        sendNumberOfTasksPerStatus();
        sendCategoryCount();
        log.logUserInfo(token, "Task restored with "+id+" id", 1);
        return true;
    }

    public boolean deleteTask(int id, String token) {
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
        sendCategoryCount();
        log.logUserInfo(token, "Task deleted with "+id+" id", 1);
        return true;
    }

    //Function that checks all tasks active = false and sets them to true
    public boolean restoreAllTasks(String token) {
        List<TaskEntity> taskEntities = taskDao.getAllTasks();
        for (TaskEntity taskEntity : taskEntities) {
            if (!taskEntity.getActive()) {
                taskEntity.setActive(true);
                taskDao.merge(taskEntity);
                sendAvgTaskPerUser();
                sendNumberOfTasksPerStatus();
                sendCompletedTasksOverTime();
                sendCategoryCount();
                log.logUserInfo(token, "Task id "+taskEntity.getId()+ " restored.", 1);
            }
            sendMessageActionToAllSocket(MessageType.TASK_RESTORE_ALL);
        }
        return true;
    }

    //Function that deletes all tasks from the database mysql that are active = false, returns true if all tasks were deleted
    public boolean deleteAllTasks(String token) {
        List<TaskEntity> taskEntities = taskDao.getAllTasks();
        for (TaskEntity taskEntity : taskEntities) {
            if (!taskEntity.getActive()) {
                log.logUserInfo(token, "Task id "+taskEntity.getId()+" deleted.", 1);
                taskDao.remove(taskEntity);
                sendAvgTaskPerUser();
                sendNumberOfTasksPerStatus();
                sendCompletedTasksOverTime();
                sendCategoryCount();
            }
        }
        sendMessageActionToAllSocket(MessageType.TASK_DELETE_ALL);
        return true;
    }

    //websockets
    public void sendMessageActionToAllSocket(MessageType messageType) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("type", messageType.getValue());

        String json = gson.toJson(jsonObject);
        notifier.sendToAllSessions(json);
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
        tokenBean.isValidUserByToken(token); //set timeout
        TaskEntity taskEntity = taskDao.findTaskById(taskID);
        if(taskEntity == null) return false;
        int lastStatus = taskEntity.getStatus();
        TokenStatus tokenStatus = tokenBean.isValidUserByToken(token);
        if(tokenStatus != TokenStatus.VALID) return false;

        updateTaskStatus(taskID, status, token);

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

        log.logUserInfo(token,  "Task moved with "+taskID+" id", 1);
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
        statisticBean.sendCompletedTasksOverTime();
    }

    public void sendCategoryCount(){
        statisticBean.sendCategoryCount(MessageType.STATISTIC_CATEGORY_COUNT);
    }





}
