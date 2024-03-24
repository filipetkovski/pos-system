package system.pos.spring.service.impl;

import org.springframework.stereotype.Service;
import system.pos.spring.model.AddedProduct;
import system.pos.spring.repository.AddedProductRepository;
import system.pos.spring.service.AddedProductService;

import java.util.List;

@Service
public class AddedProductServiceImpl implements AddedProductService {
    private final AddedProductRepository addedProductRepository;

    public AddedProductServiceImpl(AddedProductRepository addedProductRepository) {
        this.addedProductRepository = addedProductRepository;
    }

    @Override
    public void saveProducts(List<AddedProduct> products) {
        addedProductRepository.saveAll(products);
    }

    @Override
    public void delete(List<AddedProduct> addedProducts) {
        addedProductRepository.deleteAll(addedProducts);
    }

    @Override
    public void updateProduct(AddedProduct existingProduct, AddedProduct newProduct) {
        existingProduct.setQuantity(existingProduct.getQuantity() + newProduct.getQuantity());

        if(existingProduct.getDescription().isBlank() && !newProduct.getDescription().isBlank()) {
            existingProduct.setDescription(newProduct.getDescription());
        } else if(!newProduct.getDescription().isBlank()) {
            existingProduct.setDescription(existingProduct.getDescription() + "," + newProduct.getDescription());
        }

        addedProductRepository.save(existingProduct);
    }

    @Override
    public void saveProduct(AddedProduct newProduct) {
        addedProductRepository.save(newProduct);
    }

    @Override
    public AddedProduct updateLocalList(AddedProduct addProduct, int quantity, String description) {
        addProduct.setQuantity(addProduct.getQuantity()+quantity);
        if(addProduct.getDescription().isBlank() && !description.isBlank()) {
            addProduct.setDescription(description);
        } else if(!description.isBlank()) {
            addProduct.setDescription(addProduct.getDescription() + ", " + description);
        }
        return addProduct;
    }

    @Override
    public void deleteAddedProduct(AddedProduct addedProduct) {
        addedProductRepository.delete(addedProduct);
    }
}
