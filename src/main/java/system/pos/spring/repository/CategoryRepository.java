package system.pos.spring.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import system.pos.spring.model.Category;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    @Query("SELECT c FROM Category c WHERE c.supercategory.id = :categoryId")
    List<Category> findSubcategoriesBySupercategoryId(Long categoryId);

    List<Category> findByLevel(int i);

    Category findByName(String supercategory);
}
