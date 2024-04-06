package system.pos.spring.service.impl;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import system.pos.spring.enumm.ProductType;
import system.pos.spring.model.Category;
import system.pos.spring.model.Product;
import system.pos.spring.repository.CategoryRepository;
import system.pos.spring.repository.ProductRepository;
import system.pos.spring.service.ProductService;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public ProductServiceImpl(ProductRepository productRepository, CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    @Override
    public Product findByCode(Long code) {
        return productRepository.findById(code).orElse(null);
    }

    public List<Category> getSubcategoriesForCategory(Long categoryId) {
        return categoryRepository.findSubcategoriesBySupercategoryId(categoryId);
    }

    @Override
    public List<Category> getTopCategories() {
        return categoryRepository.findByLevel(1);
    }

    @Override
    public List<Product> findAll() {
        return productRepository.findAll();
    }

    @Override
    public List<Product> findBySerach(String serachText) {
        return productRepository.findAll().stream().filter(product -> product.getName().toLowerCase().contains(serachText) || product.getCategory().getName().toLowerCase().contains(serachText)).collect(Collectors.toList());
    }

    @Override
    public List<Category> getSecondLevelCategories() {
        return categoryRepository.findByLevel(2);
    }

    @Override
    public Category isValidCategory(String name, String supercategory) {
        if(categoryRepository.findByName(name) != null) {
            return null;
        }
        return new Category(name,categoryRepository.findByName(supercategory),2);
    }

    @Override
    public void addCategory(Category category) {
        categoryRepository.save(category);
    }

    @Override
    @Transactional
    public void deleteCategory(Category category) {
        categoryRepository.delete(category);
    }

    @Override
    public void changeProductsCategory(List<Product> products) {
        Category category = categoryRepository.findById(9999L).orElse(null);
        if(category != null) {
            products.forEach(pr -> pr.setCategory(category));
            productRepository.saveAll(products);
        }
    }

    @Override
    public Category findCategoryByName(String supCat) {
        return categoryRepository.findByName(supCat);
    }

    @Override
    @Transactional
    public Product isValidName(String name, int price, ProductType type, String category, byte[] image) {
        if(!productRepository.existsByName(name)) {
            Category categoryEntity = categoryRepository.findByName(category);
            if (categoryEntity != null) {
                return productRepository.save(new Product(name, price, type,image, categoryRepository.findByName(category)));
            }
        }
        return null;
    }

    @Override
    @Transactional
    public void delete(Product product) {
        productRepository.delete(product);
    }

    @Override
    public void addProduct(Product product) {
        productRepository.save(product);
    }

    @Override
    public boolean findByProductName(String name) {
        return productRepository.existsByName(name);
    }
}