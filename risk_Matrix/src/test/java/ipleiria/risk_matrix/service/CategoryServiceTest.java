package ipleiria.risk_matrix.service;

import ipleiria.risk_matrix.exceptions.exception.ConflictException;
import ipleiria.risk_matrix.exceptions.exception.NotFoundException;
import ipleiria.risk_matrix.models.category.Category;
import ipleiria.risk_matrix.repository.CategoryRepository;
import ipleiria.risk_matrix.repository.QuestionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private QuestionRepository questionRepository;

    @InjectMocks
    private CategoryService categoryService;

    private Category category;

    @BeforeEach
    void setUp() {
        category = new Category();
        category.setId(1L);
        category.setName("Network Security");
    }

    @Test
    void getAllCategories_returnsList() {
        when(categoryRepository.findAll()).thenReturn(List.of(category));

        List<Category> result = categoryService.getAllCategories();

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getName()).isEqualTo("Network Security");
        verify(categoryRepository).findAll();
    }

    @Test
    void getCategoryById_existingId_returnsCategory() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        Category result = categoryService.getCategoryById(1L);

        assertThat(result.getName()).isEqualTo("Network Security");
    }

    @Test
    void getCategoryById_nonExistingId_throwsNotFoundException() {
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.getCategoryById(99L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Category not found");
    }

    @Test
    void createCategory_validInput_savesAndReturns() {
        Category input = new Category();
        input.setName("Data Protection");

        when(categoryRepository.existsByNameIgnoreCase("Data Protection")).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenAnswer(inv -> {
            Category saved = inv.getArgument(0);
            saved.setId(2L);
            return saved;
        });

        Category result = categoryService.createCategory(input);

        assertThat(result.getName()).isEqualTo("Data Protection");
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void createCategory_duplicateName_throwsConflictException() {
        Category input = new Category();
        input.setName("Network Security");

        when(categoryRepository.existsByNameIgnoreCase("Network Security")).thenReturn(true);

        assertThatThrownBy(() -> categoryService.createCategory(input))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    void createCategory_nullName_throwsIllegalArgument() {
        Category input = new Category();
        input.setName(null);

        assertThatThrownBy(() -> categoryService.createCategory(input))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void createCategory_blankName_throwsIllegalArgument() {
        Category input = new Category();
        input.setName("   ");

        assertThatThrownBy(() -> categoryService.createCategory(input))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void updateCategory_validInput_updatesAndReturns() {
        Category details = new Category();
        details.setName("Updated Name");

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(categoryRepository.existsByNameIgnoreCase("Updated Name")).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenAnswer(inv -> inv.getArgument(0));

        Category result = categoryService.updateCategory(1L, details);

        assertThat(result.getName()).isEqualTo("Updated Name");
    }

    @Test
    void updateCategory_sameName_noConflict() {
        Category details = new Category();
        details.setName("Network Security");

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(categoryRepository.save(any(Category.class))).thenAnswer(inv -> inv.getArgument(0));

        Category result = categoryService.updateCategory(1L, details);

        assertThat(result.getName()).isEqualTo("Network Security");
    }

    @Test
    void updateCategory_duplicateName_throwsConflictException() {
        Category details = new Category();
        details.setName("Existing Name");

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(categoryRepository.existsByNameIgnoreCase("Existing Name")).thenReturn(true);

        assertThatThrownBy(() -> categoryService.updateCategory(1L, details))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void deleteCategory_notInUse_deletes() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(questionRepository.existsByCategoryId(1L)).thenReturn(false);

        categoryService.deleteCategory(1L);

        verify(categoryRepository).delete(category);
    }

    @Test
    void deleteCategory_inUse_throwsConflictException() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(questionRepository.existsByCategoryId(1L)).thenReturn(true);

        assertThatThrownBy(() -> categoryService.deleteCategory(1L))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Cannot delete category in use");
    }

    @Test
    void deleteCategory_nonExisting_throwsNotFoundException() {
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.deleteCategory(99L))
                .isInstanceOf(NotFoundException.class);
    }
}
