package aor.paj.bean;

import aor.paj.dao.TaskDao;
import aor.paj.dao.UserDao;
import aor.paj.dto.TasksStatisticsDto;
import aor.paj.dto.UserStatisticsDto;

import aor.paj.utils.MessageType;
import aor.paj.utils.State;
import aor.paj.websocket.Notifier;
import aor.paj.websocket.bean.HandleWebSockets;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;

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
        UserStatisticsDto userStatistics = new UserStatisticsDto(countUsers, confirmedUsers, unconfirmedUsers);
        return userStatistics;
    }

    public void sendUserStatistics(MessageType messageType) {
        UserStatisticsDto userStatisticsDto = getStatisticsUsers();
        String json = handleWebSockets.convertToJsonString(userStatisticsDto, MessageType.STATISTIC_USER);
        notifier.sendToAllProductOwnerSessions(json);
        System.out.println(json);
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

        TasksStatisticsDto tasksStatisticsDto = new TasksStatisticsDto();
        tasksStatisticsDto.setAvgTaskPerUser(avg);
        tasksStatisticsDto.setTodoPerUser(todo);
        tasksStatisticsDto.setDoingPerUser(doing);
        tasksStatisticsDto.setDonePerUser(done);
        System.out.println(tasksStatisticsDto.toString());
        return tasksStatisticsDto;
    }

    public TasksStatisticsDto getAvgTaskPerUser() {
        double avg = taskDao.getAvgTaskPerUser();
        TasksStatisticsDto tasksStatisticsDto = new TasksStatisticsDto();
        tasksStatisticsDto.setAvgTaskPerUser(avg);
        return tasksStatisticsDto;
    }

    public void sendAvgTaskPerUser(MessageType messageType) {
        TasksStatisticsDto avgTaskPerUser = getAvgTaskPerUser();
        String json = handleWebSockets.convertToJsonString(avgTaskPerUser, MessageType.STATISTIC_TASK);
        notifier.sendToAllProductOwnerSessions(json);
        System.out.println(json);
    }

    public TasksStatisticsDto getNumberOfTasksPerStatus(){
        TasksStatisticsDto tasksStatisticsDto = new TasksStatisticsDto();

        int todo = taskDao.getActiveStatusTasks(State.TODO.getValue());
        int doing = taskDao.getActiveStatusTasks(State.DOING.getValue());
        int done = taskDao.getActiveStatusTasks(State.DONE.getValue());

        tasksStatisticsDto.setTodoPerUser(todo);
        tasksStatisticsDto.setDoingPerUser(doing);
        tasksStatisticsDto.setDonePerUser(done);

        return tasksStatisticsDto;
    }

    public void sendNumberOfTasksPerStatus(MessageType messageType){
        TasksStatisticsDto tasksStatisticsDto = getNumberOfTasksPerStatus();
        String json = handleWebSockets.convertToJsonString(tasksStatisticsDto, MessageType.STATISTIC_TASK_PER_STATUS);
        notifier.sendToAllProductOwnerSessions(json);
        System.out.println(json);
    }


}
