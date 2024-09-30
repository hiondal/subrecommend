package com.subrecommend.biz.usecase.inport;

import com.subrecommend.biz.dto.CategoryDTO;
import java.util.List;

public interface ICategoryService {
    List<CategoryDTO> getAllCategories();
}