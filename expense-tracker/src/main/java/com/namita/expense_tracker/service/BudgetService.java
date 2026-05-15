package com.namita.expense_tracker.service;

import com.namita.expense_tracker.model.Budget;
import com.namita.expense_tracker.model.Category;
import com.namita.expense_tracker.model.User;
import com.namita.expense_tracker.repository.BudgetRepository;
import com.namita.expense_tracker.repository.CategoryRepository;
import com.namita.expense_tracker.repository.ExpenseRepository;
import com.namita.expense_tracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final ExpenseRepository expenseRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public Map<String, Object> setBudget(Map<String, Object> body) {
        User user = getCurrentUser();

        Long categoryId = Long.valueOf(body.get("categoryId").toString());
        int month = Integer.parseInt(body.get("month").toString());
        int year = Integer.parseInt(body.get("year").toString());
        BigDecimal amount = new BigDecimal(body.get("amount").toString());

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        // update if exists, create if not
        Budget budget = budgetRepository
                .findByUserIdAndCategoryIdAndMonthAndYear(
                        user.getId(), categoryId, month, year)
                .orElse(Budget.builder()
                        .user(user)
                        .category(category)
                        .month(month)
                        .year(year)
                        .build());

        budget.setAmount(amount);
        budgetRepository.save(budget);

        return Map.of(
                "message", "Budget set successfully",
                "category", category.getName(),
                "amount", amount,
                "month", month,
                "year", year
        );
    }

    public List<Map<String, Object>> getBudgetStatus() {
        User user = getCurrentUser();
        LocalDate now = LocalDate.now();

        List<Budget> budgets = budgetRepository
                .findByUserIdAndMonthAndYear(
                        user.getId(), now.getMonthValue(), now.getYear());

        List<Map<String, Object>> result = new ArrayList<>();

        for (Budget budget : budgets) {

            BigDecimal spent = expenseRepository.sumByUserCategoryAndMonth(
                    user.getId(),
                    budget.getCategory().getId(),
                    now.getMonthValue(),
                    now.getYear());

            if (spent == null) spent = BigDecimal.ZERO;

            BigDecimal percentUsed = spent
                    .divide(budget.getAmount(), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(2, RoundingMode.HALF_UP);

            BigDecimal remaining = budget.getAmount().subtract(spent);

            boolean alert = percentUsed.compareTo(BigDecimal.valueOf(80)) >= 0;

            String alertMessage = alert
                    ? "⚠️ Warning: You have used " + percentUsed + "% of your "
                    + budget.getCategory().getName() + " budget!"
                    : "";

            result.add(Map.of(
                    "category",        budget.getCategory().getName(),
                    "budgetAmount",    budget.getAmount(),
                    "spentAmount",     spent,
                    "remainingAmount", remaining,
                    "percentUsed",     percentUsed,
                    "alert",           alert,
                    "alertMessage",    alertMessage
            ));
        }

        return result;
    }
}
