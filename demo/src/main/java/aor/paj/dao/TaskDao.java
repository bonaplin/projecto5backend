package aor.paj.dao;

import aor.paj.entity.CategoryEntity;
import aor.paj.entity.TaskEntity;
import aor.paj.entity.UserEntity;
import jakarta.ejb.Stateless;
import jakarta.persistence.NoResultException;

import java.util.ArrayList;
import java.util.List;

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
    public List<TaskEntity> getActiveStatusTasks(int status) {
        try {
            return em.createNamedQuery("Task.getActiveStatusTasks").setParameter("status", status).getResultList();
        } catch (NoResultException e) {
            return null;
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
}
