package system.pos.spring.service;

import system.pos.spring.model.AddedProduct;

import java.util.List;

public interface AddedProductService {
    void saveProducts(List<AddedProduct> products);

    void delete(List<AddedProduct> addedProducts);

    void updateProduct(AddedProduct existingProduct, AddedProduct newProduct);

    void saveProduct(AddedProduct newProduct);

    AddedProduct updateLocalList(AddedProduct addProduct, int quantity, String description);

    void deleteAddedProduct(AddedProduct addedProduct);
}
