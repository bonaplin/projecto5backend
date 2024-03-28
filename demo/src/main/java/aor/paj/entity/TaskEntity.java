package aor.paj.entity;

import aor.paj.dto.UserDto;
import jakarta.persistence.*;
import jdk.jfr.Category;

import java.io.Serializable;
import java.time.LocalDate;

@Entity
@Table(name="task")
@NamedQuery(name = "Task.findTaskById", query = "SELECT t FROM TaskEntity t WHERE t.id = :id")
@NamedQuery(name = "Task.findTaskByOwner", query = "SELECT t FROM TaskEntity t WHERE t.owner = :owner")
@NamedQuery(name = "Task.findTaskByTitle", query = "SELECT t FROM TaskEntity t WHERE t.title = :title")
@NamedQuery(name = "Task.getAllTasks", query = "SELECT t FROM TaskEntity t")
@NamedQuery(name = "Task.getActiveTasks", query = "SELECT t FROM TaskEntity t WHERE t.active = true ORDER BY t.priority DESC, t.initialDate, COALESCE(t.finalDate, '9999-12-31')")
@NamedQuery(name = "Task.getActiveStatusTasks", query = "SELECT t FROM TaskEntity t WHERE t.active = true AND t.status = :status ORDER BY t.priority DESC, t.initialDate, COALESCE(t.finalDate, '9999-12-31')")
@NamedQuery(name = "Task.getInactiveTasks", query = "SELECT t FROM TaskEntity t WHERE t.active = false")
@NamedQuery(name = "Task.findTaskByOwnerId", query = "SELECT t FROM TaskEntity t WHERE t.owner.id = :id AND t.active = true ORDER BY t.priority DESC, t.initialDate, COALESCE(t.finalDate, '9999-12-31')")
@NamedQuery(name = "Task.findTaskByCategory", query = "SELECT t FROM TaskEntity t WHERE t.category = :category AND t.active = true ORDER BY t.priority DESC, t.initialDate, COALESCE(t.finalDate, '9999-12-31')")
@NamedQuery(name = "Task.findTaskByCategoryAndOwner", query = "SELECT t FROM TaskEntity t WHERE t.category = :category AND t.owner = :owner AND t.active = true ORDER BY t.priority DESC, t.initialDate, COALESCE(t.finalDate, '9999-12-31')")
@NamedQuery(name = "Task.findActiveTaskByCategoryAndOwner", query = "SELECT t FROM TaskEntity t WHERE t.category = :category AND t.owner = :owner AND t.active = true ORDER BY t.priority DESC, t.initialDate, COALESCE(t.finalDate, '9999-12-31')")
@NamedQuery(name = "Task.findTaskByStatusAndOwnerAndCategory", query = "SELECT t FROM TaskEntity t WHERE t.status = :status AND t.owner = :owner AND t.category = :category AND t.active = true ORDER BY t.priority DESC, t.initialDate, COALESCE(t.finalDate, '9999-12-31')")
@NamedQuery(name = "Task.findTaskByNameAndStatus",
        query = "SELECT t FROM TaskEntity t WHERE t.title = :title AND t.status = :status AND t.active = true ORDER BY t.priority DESC, t.initialDate, COALESCE(t.finalDate, '9999-12-31')")
@NamedQuery(name = "Task.findTaskByCategoryAndStatus",
        query = "SELECT t FROM TaskEntity t WHERE t.category = :category AND t.status = :status AND t.active = true ORDER BY t.priority DESC, t.initialDate, COALESCE(t.finalDate, '9999-12-31')")
public class TaskEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "id", nullable = false, unique = true, updatable = false)
    private int id;

    @Column(name = "title", nullable = false, unique = true, updatable = true)
    private String title;

    @Column(name = "description", nullable = false, unique = false, updatable = true)
    private String description;

    @Column(name = "initialDate", nullable = false, unique = false, updatable = true)
    private LocalDate initialDate;

    @Column(name = "finalDate", nullable = true, unique = false, updatable = true)
    private LocalDate finalDate;

    @Column(name = "status", nullable = false, unique = false, updatable = true)
    private Integer status;

    @Column(name = "priority", nullable = false, unique = false, updatable = true)
    private Integer priority;

    @Column(name = "active", nullable = false, unique = false, updatable = true)
    private Boolean active;

    @ManyToOne
    @JoinColumn(name = "owner_id", nullable = false)
    private UserEntity owner;

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private CategoryEntity category;

    public TaskEntity() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getInitialDate() {
        return initialDate;
    }

    public void setInitialDate(LocalDate initialDate) {
        this.initialDate = initialDate;
    }

    public LocalDate getFinalDate() {
        return finalDate;
    }

    public void setFinalDate(LocalDate finalDate) {
        this.finalDate = finalDate;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public UserEntity getOwner() {
        return owner;
    }

    public void setOwner(UserEntity owner) {
        this.owner = owner;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public CategoryEntity getCategory() {
        return category;
    }

    public void setCategory(CategoryEntity category) {
        this.category = category;
    }

    @Override
    public String toString() {
        return "TaskEntity{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", initialDate=" + initialDate +
                ", finalDate=" + finalDate +
                ", status=" + status +
                ", priority=" + priority +
                ", active=" + active +
                ", owner=" + owner +
                ", category=" + category +
                '}';
    }
}

