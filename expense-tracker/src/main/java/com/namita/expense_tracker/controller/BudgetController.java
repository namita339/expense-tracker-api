package com.namita.expense_tracker.controller;

import com.namita.expense_tracker.service.BudgetService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/budgets")
@RequiredArgsConstructor
public class BudgetController {

    private final BudgetService budgetService;

    @PostMapping
    public ResponseEntity<?> setBudget(@RequestBody Map<String, Object> body) {
        return ResponseEntity.ok(budgetService.setBudget(body));
    }

    @GetMapping("/status")
    public ResponseEntity<?> getStatus() {
        return ResponseEntity.ok(budgetService.getBudgetStatus());
    }
}
