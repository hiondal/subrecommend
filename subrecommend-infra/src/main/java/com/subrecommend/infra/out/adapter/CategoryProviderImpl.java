package com.subrecommend.infra.out.adapter;

import com.subrecommend.biz.dto.CategoryDTO;
import com.subrecommend.biz.usecase.outport.ICategoryProvider;
import com.subrecommend.infra.out.entity.CategoryEntity;
import com.subrecommend.infra.out.repo.ICategoryRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class CategoryProviderImpl implements ICategoryProvider {
    private final ICategoryRepository categoryRepository;

    public CategoryProviderImpl(ICategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public List<CategoryDTO> getCategories() {
        return categoryRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private CategoryDTO mapToDTO(CategoryEntity entity) {
        CategoryDTO dto = new CategoryDTO();
        dto.setName(entity.getName());
        dto.setImage(entity.getImage());
        return dto;
    }
}