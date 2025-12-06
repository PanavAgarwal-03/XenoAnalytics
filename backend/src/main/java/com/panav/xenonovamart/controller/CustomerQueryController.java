package com.panav.xenonovamart.controller;

import com.panav.xenonovamart.model.CustomerEntity;
import com.panav.xenonovamart.repository.CustomerRepository;
import lombok.Data;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/customers")
public class CustomerQueryController {

    private final CustomerRepository customerRepo;

    public CustomerQueryController(CustomerRepository customerRepo) {
        this.customerRepo = customerRepo;
    }

    @GetMapping("/list")
    public List<CustomerSummaryDto> listCustomers(@RequestParam Long storeId) {
        List<CustomerEntity> all = customerRepo.findByStoreId(storeId);
        return all.stream().map(c -> {
            CustomerSummaryDto dto = new CustomerSummaryDto();
            dto.setId(c.getId());
            dto.setShopifyCustomerId(c.getShopifyCustomerId());
            dto.setEmail(c.getEmail());
            dto.setFirstName(c.getFirstName());
            dto.setLastName(c.getLastName());
            return dto;
        }).collect(Collectors.toList());
    }

    @Data
    public static class CustomerSummaryDto {
        private Long id;
        private Long shopifyCustomerId;
        private String email;
        private String firstName;
        private String lastName;
    }
}
