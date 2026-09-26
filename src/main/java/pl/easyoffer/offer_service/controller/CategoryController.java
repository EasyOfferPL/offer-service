package pl.easyoffer.offer_service.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.easyoffer.offer_service.model.to.CategoryTO;
import pl.easyoffer.offer_service.service.CategoryService;

import java.util.List;

@RestController
@RequestMapping("/v1.0/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public List<CategoryTO> getCategories() {
        return categoryService.getCategories();
    }

}
