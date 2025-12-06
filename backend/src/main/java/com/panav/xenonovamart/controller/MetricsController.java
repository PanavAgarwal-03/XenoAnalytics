package com.panav.xenonovamart.controller;

import com.panav.xenonovamart.dto.*;
import com.panav.xenonovamart.service.MetricsService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/api/metrics")
public class MetricsController {

    private final MetricsService metricsService;

    public MetricsController(MetricsService metricsService) {
        this.metricsService = metricsService;
    }

    @GetMapping("/overview")
    public ResponseEntity<MetricsSummaryDto> overview(
            @RequestParam Long storeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        OffsetDateTime fromDt = from.atStartOfDay().atOffset(OffsetDateTime.now().getOffset());
        OffsetDateTime toDt = to.atTime(LocalTime.MAX).atOffset(OffsetDateTime.now().getOffset());

        return ResponseEntity.ok(metricsService.getSummary(storeId, fromDt, toDt));
    }

    @GetMapping("/orders-by-date")
    public ResponseEntity<List<OrdersByDateDto>> ordersByDate(
            @RequestParam Long storeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        return ResponseEntity.ok(metricsService.getOrdersByDate(storeId, from, to));
    }

    @GetMapping("/top-customers")
    public ResponseEntity<List<TopCustomerDto>> topCustomers(
            @RequestParam Long storeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "5") int limit) {

        OffsetDateTime fromDt = from.atStartOfDay().atOffset(OffsetDateTime.now().getOffset());
        OffsetDateTime toDt = to.atTime(LocalTime.MAX).atOffset(OffsetDateTime.now().getOffset());

        return ResponseEntity.ok(metricsService.getTopCustomers(storeId, fromDt, toDt, limit));
    }

    @GetMapping("/top-products")
    public ResponseEntity<List<TopProductDto>> topProducts(
            @RequestParam Long storeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "5") int limit) {

        OffsetDateTime fromDt = from.atStartOfDay().atOffset(OffsetDateTime.now().getOffset());
        OffsetDateTime toDt = to.atTime(LocalTime.MAX).atOffset(OffsetDateTime.now().getOffset());

        return ResponseEntity.ok(metricsService.getTopProducts(storeId, fromDt, toDt, limit));
    }

    @GetMapping("/sync-status")
    public ResponseEntity<SyncStatusDto> syncStatus(@RequestParam Long storeId) {
        return ResponseEntity.ok(metricsService.getSyncStatus(storeId));
    }
}
