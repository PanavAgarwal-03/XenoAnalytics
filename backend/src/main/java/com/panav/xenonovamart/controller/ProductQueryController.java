package com.panav.xenonovamart.controller;

import com.panav.xenonovamart.model.ProductEntity;
import com.panav.xenonovamart.repository.ProductRepository;
import lombok.Data;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/products")
public class ProductQueryController {

    private final ProductRepository productRepo;

    public ProductQueryController(ProductRepository productRepo) {
        this.productRepo = productRepo;
    }

    @GetMapping("/list")
    public List<ProductSummaryDto> listProducts(@RequestParam Long storeId) {
        List<ProductEntity> all = productRepo.findByStoreIdOrderByTitleAsc(storeId);
        return all.stream().map(p -> {
            ProductSummaryDto dto = new ProductSummaryDto();
            dto.setId(p.getId());
            dto.setShopifyProductId(p.getShopifyProductId());
            dto.setTitle(p.getTitle());
            dto.setProductType(p.getProductType());
            dto.setVendor(p.getVendor());
            dto.setStatus(p.getStatus());
            dto.setMinPrice(p.getMinPrice());
            dto.setMaxPrice(p.getMaxPrice());
            dto.setTotalVariants(p.getTotalVariants());
            return dto;
        }).collect(Collectors.toList());
    }

    @Data
    public static class ProductSummaryDto {
        private Long id;
        private Long shopifyProductId;
        private String title;
        private String productType;
        private String vendor;
        private String status;
        private java.math.BigDecimal minPrice;
        private java.math.BigDecimal maxPrice;
        private Integer totalVariants;
    }
}
