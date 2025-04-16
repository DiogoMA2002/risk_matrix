package ipleiria.risk_matrix.service;

import ipleiria.risk_matrix.exceptions.exception.ConflictException;
import ipleiria.risk_matrix.exceptions.exception.NotFoundException;
import ipleiria.risk_matrix.models.category.Category;
import ipleiria.risk_matrix.repository.CategoryRepository;
import ipleiria.risk_matrix.repository.QuestionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final QuestionRepository questionRepository;


    @Autowired
    public CategoryService(CategoryRepository categoryRepository, QuestionRepository questionRepository) {
        this.categoryRepository = categoryRepository;
        this.questionRepository = questionRepository;
    }

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    public Category getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Category not found with id: " + id));
    }

    public Category createCategory(Category category) {
        validateCategoryInput(category);

        String name = category.getName().trim();
        if (categoryRepository.existsByNameIgnoreCase(name)) {
            throw new ConflictException("Category with name '" + name + "' already exists.");
        }

        category.setName(name);
        return categoryRepository.save(category);
    }

    public Category updateCategory(Long id, Category categoryDetails) {
        validateCategoryInput(categoryDetails);

        Category existing = getCategoryById(id);
        String newName = categoryDetails.getName().trim();

        if (!existing.getName().equalsIgnoreCase(newName) &&
                categoryRepository.existsByNameIgnoreCase(newName)) {
            throw new ConflictException("Another category with the name '" + newName + "' already exists.");
        }

        existing.setName(newName);
        return categoryRepository.save(existing);
    }

    public void deleteCategory(Long id) {
        Category category = getCategoryById(id);

        //Optional: validate if category is in use (uncomment if FK relationship exists)
         if (questionRepository.existsByCategoryId(id)) {
             throw new ConflictException("Cannot delete category in use by questions.");
         }

        categoryRepository.delete(category);
    }

    private void validateCategoryInput(Category category) {
        if (category == null || category.getName() == null || category.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Category name must not be null or empty.");
        }
    }
}
