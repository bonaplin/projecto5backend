package aor.paj.dto;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.util.List;

@XmlRootElement
public class TaskListsDto {
    private List<TaskDto> todoTasks;
    private List<TaskDto> doingTasks;
    private List<TaskDto> doneTasks;
    @XmlElement
    public List<TaskDto> getTodoTasks() {
        return todoTasks;
    }

    public void setTodoTasks(List<TaskDto> todoTasks) {
        this.todoTasks = todoTasks;
    }
    @XmlElement
    public List<TaskDto> getDoingTasks() {
        return doingTasks;
    }

    public void setDoingTasks(List<TaskDto> doingTasks) {
        this.doingTasks = doingTasks;
    }
    @XmlElement
    public List<TaskDto> getDoneTasks() {
        return doneTasks;
    }

    public void setDoneTasks(List<TaskDto> doneTasks) {
        this.doneTasks = doneTasks;
    }
}
