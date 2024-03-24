package system.pos.spring.service;

import system.pos.spring.enumm.ProductType;
import system.pos.spring.model.Category;
import system.pos.spring.model.Product;

import java.util.List;

public interface ProductService {
    Product findByCode(Long code);

    List<Category> getSubcategoriesForCategory(Long id);

    List<Category> getTopCategories();

    List<Product> findAll();

    List<Product> findBySerach(String serachText);

    List<Category> getSecondLevelCategories();

    Category isValidCategory(String name, String supercategory);

    void addCategory(Category category);

    void deleteCategory(Category category);

    void changeProductsCategory(List<Product> products);

    Category findCategoryByName(String supCat);

    Product isValidName(String name, int price, ProductType type, String category, byte[] image);

    void delete(Product product);

    void addProduct(Product product);

    boolean findByProductName(String name);
}
