package com.namita.expense_tracker.controller;

import com.namita.expense_tracker.model.Expense;
import com.namita.expense_tracker.repository.BudgetRepository;
import com.namita.expense_tracker.repository.ExpenseRepository;
import com.namita.expense_tracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final ExpenseRepository expenseRepository;
    private final UserRepository userRepository;
    private final BudgetRepository budgetRepository;

    private Long getCurrentUserId() {
        String email = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"))
                .getId();
    }

    // GET /api/v1/analytics/monthly?month=2026-05
    @GetMapping("/monthly")
    public ResponseEntity<?> monthlySummary(
            @RequestParam(required = false) String month) {

        Long userId = getCurrentUserId();

        // default to current month if not provided
        LocalDate now = LocalDate.now();
        LocalDate start;

        if (month != null && !month.isEmpty()) {
            start = LocalDate.parse(month + "-01",
                    DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        } else {
            start = now.withDayOfMonth(1);
        }

        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());

        List<Expense> expenses = expenseRepository
                .findByUserIdAndDateBetweenOrderByDateDesc(userId, start, end);

        if (expenses.isEmpty()) {
            return ResponseEntity.ok(Map.of(
                    "month", start.getMonth().toString(),
                    "year", start.getYear(),
                    "message", "No expenses found for this month",
                    "totalSpent", 0
            ));
        }

        // total spent
        BigDecimal totalSpent = expenses.stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // spending by category
        Map<String, BigDecimal> byCategory = expenses.stream()
                .collect(Collectors.groupingBy(
                        e -> e.getCategory().getName(),
                        Collectors.reducing(
                                BigDecimal.ZERO,
                                Expense::getAmount,
                                BigDecimal::add)
                ));

        // top category (highest spending)
        String topCategory = byCategory.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("N/A");

        // day with highest spending
        Map<LocalDate, BigDecimal> byDay = expenses.stream()
                .collect(Collectors.groupingBy(
                        Expense::getDate,
                        Collectors.reducing(
                                BigDecimal.ZERO,
                                Expense::getAmount,
                                BigDecimal::add)
                ));

        LocalDate highestDay = byDay.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);

        // average daily spending
        long daysInMonth = start.lengthOfMonth();
        BigDecimal avgDaily = totalSpent
                .divide(BigDecimal.valueOf(daysInMonth), 2,
                        java.math.RoundingMode.HALF_UP);

        // category breakdown as list for clean JSON
        List<Map<String, Object>> categoryBreakdown = byCategory.entrySet()
                .stream()
                .sorted(Map.Entry.<String, BigDecimal>comparingByValue()
                        .reversed())
                .map(e -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("category", e.getKey());
                    item.put("amount", e.getValue());
                    item.put("percentage",
                            e.getValue()
                                    .divide(totalSpent, 4,
                                            java.math.RoundingMode.HALF_UP)
                                    .multiply(BigDecimal.valueOf(100))
                                    .setScale(2, java.math.RoundingMode.HALF_UP));
                    return item;
                })
                .collect(Collectors.toList());

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("month", start.getMonth().toString());
        response.put("year", start.getYear());
        response.put("totalSpent", totalSpent);
        response.put("transactionCount", expenses.size());
        response.put("topCategory", topCategory);
        response.put("highestSpendingDay", highestDay);
        response.put("averageDailySpending", avgDaily);
        response.put("categoryBreakdown", categoryBreakdown);

        return ResponseEntity.ok(response);
    }

    // GET /api/v1/analytics/trends
    @GetMapping("/trends")
    public ResponseEntity<?> spendingTrends() {
        Long userId = getCurrentUserId();
        LocalDate now = LocalDate.now();

        List<Map<String, Object>> trends = new ArrayList<>();

        // last 6 months
        for (int i = 5; i >= 0; i--) {
            LocalDate monthStart = now.minusMonths(i).withDayOfMonth(1);
            LocalDate monthEnd = monthStart.withDayOfMonth(
                    monthStart.lengthOfMonth());

            List<Expense> expenses = expenseRepository
                    .findByUserIdAndDateBetweenOrderByDateDesc(
                            userId, monthStart, monthEnd);

            BigDecimal total = expenses.stream()
                    .map(Expense::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            Map<String, Object> monthData = new LinkedHashMap<>();
            monthData.put("month", monthStart.getMonth().toString());
            monthData.put("year", monthStart.getYear());
            monthData.put("totalSpent", total);
            monthData.put("transactionCount", expenses.size());
            trends.add(monthData);
        }

        return ResponseEntity.ok(Map.of(
                "last6Months", trends,
                "generatedAt", now.toString()
        ));
    }
}
