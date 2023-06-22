package ru.practicum.category.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.category.service.CategoryService;
import ru.practicum.category.model.dto.CategoryDto;
import ru.practicum.category.model.dto.NewCategoryDto;

import javax.validation.Valid;

@Slf4j
@Validated
@RestController
@RequestMapping
@AllArgsConstructor
public class CategoryControllerAdmin {

    private final CategoryService categoryService;

    @PostMapping("/admin/categories")
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryDto createCategory(@Valid @RequestBody NewCategoryDto newCategoryDto) {
        log.info("POST at '/admin/categories' to create category with body={}", newCategoryDto.toString());
        return categoryService.createCategory(newCategoryDto);
    }

    @DeleteMapping("/admin/categories/{catId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCategory(@PathVariable Long catId) {
        log.info("DELETE at '/admin/categories/{}' to delete category with id={}", catId, catId);
        categoryService.deleteCategory(catId);
    }

    @PatchMapping("/admin/categories/{catId}")
    @ResponseStatus(HttpStatus.OK)
    public CategoryDto updateCategory(@PathVariable Long catId,
                                      @Valid @RequestBody CategoryDto categoryDto) {
        log.info("PATCH at '/admin/categories/{}' to update category with body={}", catId, categoryDto.toString());
        return categoryService.updateCategory(catId, categoryDto);
    }
}
