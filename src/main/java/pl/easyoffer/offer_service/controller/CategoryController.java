package pl.easyoffer.offer_service.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.easyoffer.offer_service.exception.ErrorResponse;
import pl.easyoffer.offer_service.model.to.CategoryTO;
import pl.easyoffer.offer_service.service.CategoryService;

import java.util.List;

import static pl.easyoffer.offer_service.config.SwaggerDescription.*;

@RestController
@RequestMapping("/v1.0/categories")
@RequiredArgsConstructor
@Tag(name = CATEGORIES_TAG, description = CATEGORIES_TAG_DESCRIPTION)
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    @Operation(summary = LIST_CATEGORIES_SUMMARY)
    @ApiResponses({
            @ApiResponse(responseCode = RESPONSE_OK, description = CATEGORIES_RETURNED),
            @ApiResponse(responseCode = RESPONSE_INTERNAL_SERVER_ERROR, description = UNEXPECTED_SERVER_ERROR,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public List<CategoryTO> getCategories() {
        return categoryService.getCategories();
    }

}
