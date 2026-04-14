package ru.practicum.service.category;

import ru.practicum.dto.category.CategoryDto;
import ru.practicum.dto.category.NewCategoryDto;
import ru.practicum.model.Category;

import java.util.List;

public interface CategoryService {
    CategoryDto addCategory(NewCategoryDto newCategory);

    void removeCategory(Long categoryId);

    CategoryDto updateCategory(Long categoryId, NewCategoryDto updateCategory);

    List<CategoryDto> findAllCategories(Long from, Long size);

    CategoryDto findCategoryById(Long categoryId);

    Category findCategoryEntityById(Long categoryId);
}