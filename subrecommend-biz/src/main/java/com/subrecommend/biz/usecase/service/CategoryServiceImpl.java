package com.subrecommend.biz.usecase.service;

import com.subrecommend.biz.dto.CategoryDTO;
import com.subrecommend.biz.usecase.inport.ICategoryService;
import com.subrecommend.biz.usecase.outport.ICategoryProvider;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class CategoryServiceImpl implements ICategoryService {
    private final ICategoryProvider categoryProvider;

    public CategoryServiceImpl(ICategoryProvider categoryProvider) {
        this.categoryProvider = categoryProvider;
    }

    @Override
    public List<CategoryDTO> getAllCategories() {
        return categoryProvider.getCategories();
    }
}