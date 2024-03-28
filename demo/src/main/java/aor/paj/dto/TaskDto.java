package aor.paj.dto;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.time.format.DateTimeParseException;

import java.time.LocalDate;

@XmlRootElement
public class TaskDto {

    private int id;
    private String title;
    private String description;
    private LocalDate initialDate;
    private LocalDate finalDate;
    private Integer status;
    private Integer priority;
    private String category;
    private String owner;
    private boolean active;

    public TaskDto() {}

    public TaskDto(String title, String description, LocalDate initialDate, LocalDate finalDate, int priority) {
        this.title = title;
        this.description = description;
        this.initialDate = initialDate;
        this.finalDate = finalDate;
        this.priority = priority;
    }

    @XmlElement
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @XmlElement
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @XmlElement
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @XmlElement
    public LocalDate getInitialDate() {
        return initialDate;
    }

    public void setInitialDate(String initialDate) {
        try {
            this.initialDate = LocalDate.parse(initialDate);
        } catch (DateTimeParseException e) {
            // If the string is not in the correct format, set initialDate to null
            this.initialDate = null;
        }
    }

    public void setInitialDate(LocalDate initialDate) {
        this.initialDate = initialDate;
    }

    @XmlElement
    public LocalDate getFinalDate() {
        return finalDate;
    }

    public void setFinalDate(String finalDate) {
        try {
            this.finalDate = LocalDate.parse(finalDate);
        } catch (DateTimeParseException e) {
            // If the string is not in the correct format, set initialDate to null
            this.finalDate = null;
        }
    }

    public void setFinalDate(LocalDate finalDate) {
        this.finalDate = finalDate;
    }

    @XmlElement
    public Integer getStatus() {
        return status;
    }


    public void setStatus(Integer status) {
        if (status == null) {
            this.status = 0; // or any default value
        } else {
            this.status = status;
        }
    }

    public void setStatus(String status) {
        try {
            this.status = Integer.parseInt(status);
        } catch (NumberFormatException e) {
            this.status = 0; // or any default value
        }
    }

    @XmlElement
    public Integer getPriority() {
        return priority;
    }


    public void setPriority(Integer priority) {
        if (priority == null) {
            this.priority = 0;
        } else {
            this.priority = priority;
        }
    }

    public void setPriority(String priority) {
        try {
            this.priority = Integer.parseInt(priority);
        } catch (NumberFormatException e) {
            this.priority = 0;
        }
    }

    @XmlElement
    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    @XmlElement
    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    @XmlElement
    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public String toString() {
        return "TaskDto{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", initialDate=" + initialDate +
                ", finalDate=" + finalDate +
                ", status=" + status +
                ", priority=" + priority +
                ", category='" + category + '\'' +
                ", owner='" + owner + '\'' +
                ", active=" + active +
                '}';
    }
}

