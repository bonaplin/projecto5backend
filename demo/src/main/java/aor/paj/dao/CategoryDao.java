package aor.paj.dao;

import aor.paj.entity.CategoryEntity;
import aor.paj.entity.UserEntity;
import jakarta.ejb.Stateless;

import java.util.List;

@Stateless
public class CategoryDao extends AbstractDao<CategoryEntity> {

    private static final long serialVersionUID = 1L;

    public CategoryDao() {
        super(CategoryEntity.class);
    }

    public CategoryEntity findCategoryByTitle(String title) {
        try {
            return (CategoryEntity) em.createNamedQuery("Category.findCategoryByTitle").setParameter("title", title)
                    .getSingleResult();

        } catch (Exception e) {
            return null;
        }
    }

    //Function that gets all categories from database my sql
    public List<CategoryEntity> getAllCategories() {
        try {
            return em.createNamedQuery("Category.getAllCategories").getResultList();
        } catch (Exception e) {
            return null;
        }
    }

    public void deleteCategory(CategoryEntity categoryEntity) {
        try {
            em.createQuery("DELETE FROM CategoryEntity c WHERE c.id = :id")
                    .setParameter("id", categoryEntity.getId())
                    .executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addCategory(CategoryEntity categoryEntity) {
        try {
            em.persist(categoryEntity);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public CategoryEntity findCategoryById(int id) {
        try {
            return (CategoryEntity) em.createNamedQuery("Category.findCategoryById").setParameter("id", id)
                    .getSingleResult();

        } catch (Exception e) {
            return null;
        }
    }

    public boolean findCategoryByName(String aDefault) {
        try {
            return em.createNamedQuery("Category.findCategoryByTitle").setParameter("title", aDefault)
                    .getSingleResult() != null;
        } catch (Exception e) {
            return false;
        }
    }

    public List<CategoryEntity> findCategoryByOwnerID(int id) {
        try {
            return em.createNamedQuery("Category.findCategoryByOwnerID").setParameter("id", id)
                    .getResultList();
        } catch (Exception e) {
            return null;
        }
    }
}
