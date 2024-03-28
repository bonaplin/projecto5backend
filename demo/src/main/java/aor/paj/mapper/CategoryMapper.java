package aor.paj.mapper;

import aor.paj.dto.CategoryDto;
import aor.paj.entity.CategoryEntity;

public class CategoryMapper {
    public CategoryMapper() {
    }

   public static CategoryDto convertCategoryEntityToCategoryDto(CategoryEntity categoryEntity) {
        CategoryDto categoryDto = new CategoryDto();

        categoryDto.setId(categoryEntity.getId());
        categoryDto.setTitle(categoryEntity.getTitle());
        categoryDto.setDescription(categoryEntity.getDescription());
        categoryDto.setOwner(categoryEntity.getOwner().getUsername());

        return categoryDto;
    }

    public static CategoryEntity convertCategoryDtoToCategoryEntity(CategoryDto categoryDto) {
        CategoryEntity categoryEntity = new CategoryEntity();

        categoryEntity.setId(categoryDto.getId());
        categoryEntity.setTitle(categoryDto.getTitle());
        categoryEntity.setDescription(categoryDto.getDescription());

        return categoryEntity;
    }
}
