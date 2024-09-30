package com.subrecommend.biz.usecase.outport;

import com.subrecommend.biz.dto.CategoryDTO;
import java.util.List;

public interface ICategoryProvider {
    List<CategoryDTO> getCategories();
}