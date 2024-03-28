package aor.paj.entity;

import jakarta.persistence.*;

import java.io.Serializable;

@Entity
@Table(name="category")
@NamedQuery(name = "Category.findCategoryById", query = "SELECT c FROM CategoryEntity c WHERE c.id = :id")
@NamedQuery(name = "Category.findCategoryByOwner", query = "SELECT c FROM CategoryEntity c WHERE c.owner = :owner")
@NamedQuery(name = "Category.findCategoryByTitle", query = "SELECT c FROM CategoryEntity c WHERE c.title = :title")
@NamedQuery(name = "Category.getAllCategories", query = "SELECT c FROM CategoryEntity c")
@NamedQuery(name = "Category.findCategoryByOwnerID", query = "SELECT c FROM CategoryEntity c WHERE c.owner.id = :id")
public class CategoryEntity implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Column(name="id", nullable = false, unique = true, updatable = false)
    private int id;

    @Column(name="title", nullable = false, unique = true, updatable = true)
    private String title;

    @Column(name="description", nullable = false, unique = false, updatable = true)
    private String description;

    @ManyToOne
    @JoinColumn(name="owner_id", nullable = false)
    private UserEntity owner;

    public CategoryEntity() {
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

    public UserEntity getOwner() {
        return owner;
    }

    public void setOwner(UserEntity owner) {
        this.owner = owner;
    }
}
