package aor.paj.dto;

import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class TasksStatisticsDto {
    private double avgTaskPerUser;
    private double todoPerUser;
    private double doingPerUser;
    private double donePerUser;
    private double avgTimeToBeDone;

    public TasksStatisticsDto() {
    }

//    public TasksStatisticsDto(double avgTaskPerUser) {
//        this.avgTaskPerUser = avgTaskPerUser;
//    }

    public double getAvgTaskPerUser() {
        return avgTaskPerUser;
    }

    public void setAvgTaskPerUser(double avgTaskPerUser) {
        this.avgTaskPerUser = avgTaskPerUser;
    }

    public double getTodoPerUser() {
        return todoPerUser;
    }

    public void setTodoPerUser(double todoPerUser) {
        this.todoPerUser = todoPerUser;
    }

    public double getDoingPerUser() {
        return doingPerUser;
    }

    public void setDoingPerUser(double doingPerUser) {
        this.doingPerUser = doingPerUser;
    }

    public double getDonePerUser() {
        return donePerUser;
    }

    public void setDonePerUser(double donePerUser) {
        this.donePerUser = donePerUser;
    }

    public double getAvgTimeToBeDone() {
        return avgTimeToBeDone;
    }

    public void setAvgTimeToBeDone(double avgTimeToBeDone) {
        this.avgTimeToBeDone = avgTimeToBeDone;
    }
}
