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
    public List<Product> findBySearch(String searchText) {
        String toLowerCaseText = searchText.toLowerCase();
        List<Product> products = productRepository.findAll().stream()
                .filter(product -> product.getName().toLowerCase().contains(searchText) ||
                        product.getCategory().getName().toLowerCase().contains(searchText) ||
                        product.getCategory().getSupercategory().getName().toLowerCase().contains(searchText))
                .collect(Collectors.toList());

        return products.isEmpty() ? productRepository.findAll().stream()
                .filter(product -> isSimilar(product.getName().toLowerCase(), toLowerCaseText) ||
                        isSimilar(product.getCategory().getName().toLowerCase(), toLowerCaseText) ||
                        isSimilar(product.getCategory().getSupercategory().getName().toLowerCase(), toLowerCaseText))
                .collect(Collectors.toList()) : products;
    }

    private boolean isSimilar(String text, String searchText) {
        return calculateLevenshteinDistance(text, searchText) <= 1;
    }

    private int calculateLevenshteinDistance(String s1, String s2) {
        int[][] dp = new int[s1.length() + 1][s2.length() + 1];

        for (int i = 0; i <= s1.length(); i++) {
            for (int j = 0; j <= s2.length(); j++) {
                if (i == 0) {
                    dp[i][j] = j;
                } else if (j == 0) {
                    dp[i][j] = i;
                } else {
                    dp[i][j] = min(dp[i - 1][j - 1] + costOfSubstitution(s1.charAt(i - 1), s2.charAt(j - 1)),
                            dp[i - 1][j] + 1,
                            dp[i][j - 1] + 1);
                }
            }
        }

        return dp[s1.length()][s2.length()];
    }

    private int costOfSubstitution(char a, char b) {
        return a == b ? 0 : 1;
    }

    private int min(int... numbers) {
        return java.util.Arrays.stream(numbers)
                .min().orElse(Integer.MAX_VALUE);
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