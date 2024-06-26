package aor.paj.bean;

import aor.paj.dao.TaskDao;
import aor.paj.dao.UserDao;
import aor.paj.dto.CategoryCountDto;
import aor.paj.dto.RegistrationDataDto;
import aor.paj.dto.TasksStatisticsDto;
import aor.paj.dto.UserStatisticsDto;

import aor.paj.utils.MessageType;
import aor.paj.utils.State;
import aor.paj.websocket.Notifier;
import aor.paj.websocket.bean.HandleWebSockets;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;

import java.util.ArrayList;
import java.util.List;

@Stateless
public class StatisticBean {
    @EJB
    private UserDao userDao;
    @EJB
    private HandleWebSockets handleWebSockets;
    @EJB
    private Notifier notifier;
    @EJB
    private TaskDao taskDao;



    public UserStatisticsDto getStatisticsUsers() {

        int unconfirmedUsers = userDao.getUnconfirmedUserCount();
        int confirmedUsers = userDao.getConfirmedUserCount();
        int countUsers = userDao.getUserCount();
        int activeUsers = userDao.getActiveUserCount();
        int inactiveUsers = userDao.getInactiveUserCount();
        UserStatisticsDto userStatistics = new UserStatisticsDto(countUsers, confirmedUsers, unconfirmedUsers, activeUsers, inactiveUsers);
        return userStatistics;
    }

    public void sendUserStatistics() {
        UserStatisticsDto userStatisticsDto = getStatisticsUsers();
        String json = handleWebSockets.convertToJsonString(userStatisticsDto, MessageType.STATISTIC_USER);
        notifier.sendToAllProductOwnerSessions(json);
    }

    /**
     * Get all statistics from tasks
     * This method is used only go REST request to get all statistics from tasks
     * @return TasksStatisticsDto completed with all statistics from tasks
     */
    public TasksStatisticsDto getAllStatisticsFromTasks() {
        double avg = taskDao.getAvgTaskPerUser();
        int todo = taskDao.getActiveStatusTasks(State.TODO.getValue());
        int doing = taskDao.getActiveStatusTasks(State.DOING.getValue());
        int done = taskDao.getActiveStatusTasks(State.DONE.getValue());
        double avgTime = taskDao.getAverageCompletionTime();

        TasksStatisticsDto tasksStatisticsDto = new TasksStatisticsDto();
        tasksStatisticsDto.setAvgTaskPerUser(avg);
        tasksStatisticsDto.setTodoPerUser(todo);
        tasksStatisticsDto.setDoingPerUser(doing);
        tasksStatisticsDto.setDonePerUser(done);
        tasksStatisticsDto.setAvgTimeToBeDone(avgTime);

        return tasksStatisticsDto;
    }

    private TasksStatisticsDto getAvgTaskPerUser() {
        double avg = taskDao.getAvgTaskPerUser();
        TasksStatisticsDto tasksStatisticsDto = new TasksStatisticsDto();
        tasksStatisticsDto.setAvgTaskPerUser(avg);
        return tasksStatisticsDto;
    }

    public void sendAvgTaskPerUser(MessageType messageType) {
        TasksStatisticsDto avgTaskPerUser = getAvgTaskPerUser();
        String json = handleWebSockets.convertToJsonString(avgTaskPerUser, messageType);
        notifier.sendToAllProductOwnerSessions(json);
    }

    private TasksStatisticsDto getNumberOfTasksPerStatus(){
        TasksStatisticsDto tasksStatisticsDto = new TasksStatisticsDto();

        int todo = taskDao.getActiveStatusTasks(State.TODO.getValue());
        int doing = taskDao.getActiveStatusTasks(State.DOING.getValue());
        int done = taskDao.getActiveStatusTasks(State.DONE.getValue());
        double avg = taskDao.getAverageCompletionTime();

        tasksStatisticsDto.setTodoPerUser(todo);
        tasksStatisticsDto.setDoingPerUser(doing);
        tasksStatisticsDto.setDonePerUser(done);
        tasksStatisticsDto.setAvgTimeToBeDone(avg);

        return tasksStatisticsDto;
    }

    public void sendNumberOfTasksPerStatus(MessageType messageType){
        TasksStatisticsDto tasksStatisticsDto = getNumberOfTasksPerStatus();
        String json = handleWebSockets.convertToJsonString(tasksStatisticsDto, messageType);
        notifier.sendToAllProductOwnerSessions(json);

    }

    public List<RegistrationDataDto> getRegisteredUserOverTime(){
        List<Object[]> results = userDao.getRegistrationByTime();
        List<RegistrationDataDto> registrationDataDtos = new ArrayList<>();
        for(Object[] result : results){
            RegistrationDataDto registrationDataDto = new RegistrationDataDto();
            int year = (int) result[0];
            int month = (int) result[1];
            int count = ((Long) result[2]).intValue();

            registrationDataDto.setYear(year);
            registrationDataDto.setMonth(month);
            registrationDataDto.setCount(count);

            registrationDataDtos.add(registrationDataDto);
        }
        return registrationDataDtos;
    }

    public void sendRegisteredUserOverTime(){
        List<RegistrationDataDto> registrationList = getRegisteredUserOverTime();
        if(registrationList == null || registrationList.isEmpty()){
            return;
        }
        try{

            String json = handleWebSockets.convertListToJsonString(registrationList, MessageType.STATISTIC_REGISTRATION);
            notifier.sendToAllProductOwnerSessions(json);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public List<RegistrationDataDto> getCompletedTasksOverTime() {
        List<Object[]> results = taskDao.getCompletedTasksByTime();
        List<RegistrationDataDto> taskCompletionDataList = new ArrayList<>();
        int cumulativeCount = 0;

        for(Object[] result : results){
            if (result[0] != null && result[1] != null && result[2] != null) {
                RegistrationDataDto taskCompletionData = new RegistrationDataDto();
                int year = ((Integer) result[0]).intValue();
                int month = ((Integer) result[1]).intValue();
                int count = ((Long) result[2]).intValue();

                cumulativeCount += count;

                taskCompletionData.setYear(year);
                taskCompletionData.setMonth(month);
                taskCompletionData.setCount(cumulativeCount);

                taskCompletionDataList.add(taskCompletionData);
            }
        }
        return taskCompletionDataList;
    }

    public void sendCompletedTasksOverTime() {
        List<RegistrationDataDto> taskCompletionDataList = getCompletedTasksOverTime();
        if(taskCompletionDataList == null || taskCompletionDataList.isEmpty()){
            return;
        }
        try{
            String json = handleWebSockets.convertListToJsonString(taskCompletionDataList, MessageType.STATISTIC_TASK_COMULATIVE);
            notifier.sendToAllProductOwnerSessions(json);

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void sendCategoryCount(MessageType messageType) {
        List<CategoryCountDto> categoryCountList = getTasksPerCategory();
        String json = handleWebSockets.convertListToJsonString(categoryCountList, messageType);
        notifier.sendToAllProductOwnerSessions(json);
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

    public int getActiveUsers() {
        return userDao.getActiveUserCount();
    }

    public int getInactiveUsers() {
        return userDao.getInactiveUserCount();
    }

    public void sendActiveUsers() {
        int activeUsers = getActiveUsers();
        int inactiveUsers = getInactiveUsers();
        UserStatisticsDto userStatisticsDto = new UserStatisticsDto();
        userStatisticsDto.setActiveUsers(activeUsers);
        userStatisticsDto.setInactiveUsers(inactiveUsers);
        String json = handleWebSockets.convertToJsonString(userStatisticsDto, MessageType.STATISTIC_ACTIVE_USERS);
        notifier.sendToAllProductOwnerSessions(json);
    }



}
