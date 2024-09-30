package com.subrecommend.infra.in.web;

import com.subrecommend.biz.dto.CategoryDTO;
import com.subrecommend.biz.usecase.inport.ICategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@Tag(name = "카테고리", description = "카테고리 관련 API")
public class CategoryController {
    private final ICategoryService categoryService;
    private final CategoryControllerHelper controllerHelper;

    public CategoryController(ICategoryService categoryService, CategoryControllerHelper controllerHelper) {
        this.categoryService = categoryService;
        this.controllerHelper = controllerHelper;
    }

    @GetMapping
    @Operation(summary = "모든 카테고리 조회", description = "모든 구독 카테고리를 조회합니다.")
    public ResponseEntity<List<CategoryDTO>> getAllCategories() {
        return ResponseEntity.ok(categoryService.getAllCategories());
    }
}