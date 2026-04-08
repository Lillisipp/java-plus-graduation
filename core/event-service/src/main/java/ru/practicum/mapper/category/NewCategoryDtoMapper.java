package ru.practicum.mapper.category;

import lombok.experimental.UtilityClass;
import ru.practicum.dto.category.NewCategoryDto;
import ru.practicum.model.Category;

@UtilityClass
public class NewCategoryDtoMapper {
    public static Category toModel(NewCategoryDto newCategory) {
        return Category.builder()
                .name(newCategory.getName())
                .build();
    }
}