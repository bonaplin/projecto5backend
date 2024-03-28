package aor.paj.mapper;

import aor.paj.dao.CategoryDao;
import aor.paj.dto.TaskDto;
import aor.paj.entity.TaskEntity;

public class TaskMapper {

    public TaskMapper() {
    }

    public static TaskEntity convertTaskDtoToTaskEntity(TaskDto taskDto) {

        CategoryDao categoryDao = new CategoryDao();

        TaskEntity taskEntity = new TaskEntity();

        taskEntity.setTitle(taskDto.getTitle());
        taskEntity.setDescription(taskDto.getDescription());
        taskEntity.setInitialDate(taskDto.getInitialDate());
        taskEntity.setFinalDate(taskDto.getFinalDate());
        taskEntity.setStatus(taskDto.getStatus());
        taskEntity.setPriority(taskDto.getPriority());

        return taskEntity;
    }

    public static TaskDto convertTaskEntityToTaskDto(TaskEntity taskEntity) {
        TaskDto taskDto = new TaskDto();

        taskDto.setId(taskEntity.getId());
        taskDto.setTitle(taskEntity.getTitle());
        taskDto.setDescription(taskEntity.getDescription());
        taskDto.setInitialDate(taskEntity.getInitialDate());
        taskDto.setFinalDate(taskEntity.getFinalDate());
        taskDto.setStatus(taskEntity.getStatus());
        taskDto.setPriority(taskEntity.getPriority());
        if(taskEntity.getActive() != null) {
            taskDto.setActive(taskEntity.getActive());
        }
        if(taskEntity.getCategory() != null) {
            taskDto.setCategory(taskEntity.getCategory().getTitle());
        }
        if(taskEntity.getOwner() != null) {
            taskDto.setOwner(taskEntity.getOwner().getUsername());
        }

        return taskDto;
    }
}
