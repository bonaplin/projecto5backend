package aor.paj.dao;

import aor.paj.entity.CategoryEntity;
import aor.paj.entity.TaskEntity;
import aor.paj.entity.UserEntity;
import aor.paj.utils.State;
import jakarta.ejb.Stateless;
import jakarta.persistence.NoResultException;

import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Stateless
public class TaskDao extends AbstractDao<TaskEntity>{

    private static final long serialVersionUID = 1L;

    public TaskDao() {
        super(TaskEntity.class);
    }

    public TaskDao(Class<TaskEntity> clazz) {
        super(clazz);
    }

    public TaskEntity findTaskById(int id) {
        try {
            return (TaskEntity) em.createNamedQuery("Task.findTaskById").setParameter("id", id)
                    .getSingleResult();

        } catch (NoResultException e) {
            return null;
        }
    }

    public TaskEntity findTaskByTitle(String title) {
        try {
            return (TaskEntity) em.createNamedQuery("Task.findTaskByTitle").setParameter("title", title)
                    .getSingleResult();

        } catch (NoResultException e) {
            return null;
        }
    }

    public List<TaskEntity> findTaskByOwnerId(int id) {
        try {
            return em.createNamedQuery("Task.findTaskByOwnerId").setParameter("id", id).getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    //Function that returns all tasks that have active == true
    public List<TaskEntity> getActiveTasks() {
        try {
            return em.createNamedQuery("Task.getActiveTasks").getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }
    public List<TaskEntity> getInactiveTasks() {
        try {
            return em.createNamedQuery("Task.getInactiveTasks").getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }
    public int getActiveStatusTasks(int status) {
        try {
            Long result = (Long) em.createNamedQuery("Task.getActiveStatusTasks").setParameter("status", status).getSingleResult();
            int count = result.intValue();
            return count;
        } catch (NoResultException e) {
            return 0;
        }
    }

    public List<TaskEntity> findTasksByCategory(CategoryEntity category){
        try {
            return em.createNamedQuery("Task.findTaskByCategory").setParameter("category", category).getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    public List<TaskEntity> getTasksByCategoryAndOwner(UserEntity owner, CategoryEntity category){
        try {
            return em.createNamedQuery("Task.findTaskByCategoryAndOwner").setParameter("category", category).setParameter("owner", owner).getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }
    public List<TaskEntity> getActiveTasksByCategoryAndOwner(UserEntity owner, CategoryEntity category){
        try {
            return em.createNamedQuery("Task.findActiveTaskByCategoryAndOwner").setParameter("category", category).setParameter("owner", owner).getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

//    public List<TaskEntity> getTasksByStatusAndOwnerAndCategoryAndStatus(Integer status, UserEntity owner, CategoryEntity category, Integer taskStatus){
//        try {
//            return em.createNamedQuery("Task.findTaskByStatusAndOwnerAndCategoryAndStatus", TaskEntity.class)
//                    .setParameter("status", status)
//                    .setParameter("owner", owner)
//                    .setParameter("category", category)
//                    .setParameter("taskStatus", taskStatus)
//                    .getResultList();
//        } catch (NoResultException e) {
//            return new ArrayList<>();
//        }
//    }

    public TaskEntity getPreviousTask(List<TaskEntity> orderedTasks, TaskEntity currentTask) {
        int currentIndex = orderedTasks.indexOf(currentTask);
        if (currentIndex > 0) {
            return orderedTasks.get(currentIndex - 1);
        } else {
            return null;
        }
    }

    public List<TaskEntity> findTaskByOwnerIdAndStatus(int ownerId, int status) {
        try {
            return em.createNamedQuery("Task.findTaskByOwnerIdAndStatus")
                    .setParameter("ownerId", ownerId)
                    .setParameter("status", status)
                    .getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    //Function that returns all the tasks of database mysql
    public List<TaskEntity> getAllTasks() {
        return em.createNamedQuery("Task.getAllTasks").getResultList();
    }

    public List<TaskEntity> getTasksByStatusAndOwnerAndCategory(Integer status, UserEntity owner, CategoryEntity category){
        try {
            return em.createNamedQuery("Task.findTaskByStatusAndOwnerAndCategory", TaskEntity.class)
                    .setParameter("status", status)
                    .setParameter("owner", owner)
                    .setParameter("category", category)
                    .getResultList();
        } catch (NoResultException e) {
            return new ArrayList<>();
        }
    }
    //STATISTICS - STATISTICS - STATISTICS - STATISTICS - STATISTICS - STATISTICS
    public double getAverageTaskCountPerUser() {
        long totalTasks = em.createQuery("SELECT COUNT(t) FROM TaskEntity t", Long.class).getSingleResult();
        long totalUsers = em.createQuery("SELECT COUNT(u) FROM UserEntity u", Long.class).getSingleResult();
        return totalUsers > 0 ? (double) totalTasks / totalUsers : 0;
    }
    public Map<String, Long> getTaskCountByState() {
        List<Object[]> results = em.createQuery("SELECT t.status, COUNT(t) FROM TaskEntity t GROUP BY t.status").getResultList();
        Map<String, Long> taskCountByState = new HashMap<>();
        for (Object[] result : results) {
            taskCountByState.put((String) result[0], (Long) result[1]);
        }
        return taskCountByState;
    }



    public Map<String, Long> getTaskCountByUser() {
        List<Object[]> results = em.createQuery("SELECT t.owner.username, COUNT(t) FROM TaskEntity t GROUP BY t.owner.username").getResultList();
        Map<String, Long> taskCountByUser = new HashMap<>();
        for (Object[] result : results) {
            taskCountByUser.put((String) result[0], (Long) result[1]);
        }
        return taskCountByUser;
    }

    public Map<String, Map<String, Long>> getTaskCountByUserAndState() {
        List<Object[]> results = em.createQuery("SELECT t.owner.username, t.status, COUNT(t) FROM TaskEntity t GROUP BY t.owner.username, t.status").getResultList();
        Map<String, Map<String, Long>> taskCountByUserAndState = new HashMap<>();
        for (Object[] result : results) {
            String username = (String) result[0];
            String state = (String) result[1];
            Long count = (Long) result[2];
            Map<String, Long> userMap = taskCountByUserAndState.getOrDefault(username, new HashMap<>());
            userMap.put(state, count);
            taskCountByUserAndState.put(username, userMap);
        }
        return taskCountByUserAndState;
    }
    public double getAverageCompletionTime() {
        // Find all tasks that are done
        List<TaskEntity> doneTasks = em.createQuery("SELECT t FROM TaskEntity t WHERE t.status = 300", TaskEntity.class).getResultList();
        if (doneTasks.isEmpty()) {
            return 0;
        }
        // Calculate the total duration of done tasks from initial to done date
        long totalDuration = 0;
        for (TaskEntity task : doneTasks) {
            LocalDate doneDate = task.getDoneDate();
            if (doneDate != null) {
                long duration = Duration.between(task.getInitialDate().atStartOfDay(), doneDate.atStartOfDay()).toHours();
                totalDuration += duration;
            } else {
            }
        }
        // Calculate the average duration
        double averageDuration = (double) totalDuration / doneTasks.size();
        return averageDuration;
    }


    public List<LocalDate> getDoneTaskCompletionDates() {
        return em.createQuery("SELECT t.doneDate FROM TaskEntity t WHERE t.active = true AND t.status = 300 ORDER BY t.doneDate", LocalDate.class).getResultList();
    }

    public List<Object[]> getCategoryTaskCount() {
        List<Object[]> results = em.createQuery("SELECT t.category, COUNT(t) FROM TaskEntity t GROUP BY t.category ORDER BY COUNT(t) DESC").getResultList();
        List<Object[]> categoryTaskCount = new ArrayList<>();
        for (Object[] result : results) {
            categoryTaskCount.add(new Object[]{((CategoryEntity) result[0]).getTitle(), result[1]});
        }
        return categoryTaskCount;
    }

    public double getAvgTaskPerUser() {
        List<Object[]> results = em.createQuery("SELECT t.owner.username, COUNT(t) FROM TaskEntity t WHERE t.active = TRUE GROUP BY t.owner.username").getResultList();
        return results.stream().mapToLong(result -> (Long) result[1]).average().orElse(0);
    }

    public List<Object[]> getCompletedTasksByTime() {
        return em.createQuery("SELECT YEAR(t.doneDate), MONTH(t.doneDate), COUNT(t) " +
                        "FROM TaskEntity t " +
                        "WHERE t.status = :status " +
                        "GROUP BY YEAR(t.doneDate), MONTH(t.doneDate) " +
                        "ORDER BY YEAR(t.doneDate), MONTH(t.doneDate)", Object[].class)
                .setParameter("status", State.DONE.getValue())
                .getResultList();
    }

    public List<Object[]> getTasksByCategory() {
        return em.createQuery("SELECT t.category.title, COUNT(t) as task_count FROM TaskEntity t GROUP BY t.category.title ORDER BY task_count DESC ").getResultList();
    }
}
