package com.namita.expense_tracker.service;

import com.namita.expense_tracker.model.Category;
import com.namita.expense_tracker.model.Expense;
import com.namita.expense_tracker.model.User;
import com.namita.expense_tracker.repository.CategoryRepository;
import com.namita.expense_tracker.repository.ExpenseRepository;
import com.namita.expense_tracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public List<Expense> getAllExpenses() {
        return expenseRepository.findByUserIdOrderByDateDesc(
                getCurrentUser().getId());
    }

    public List<Expense> getExpensesByDateRange(LocalDate start, LocalDate end) {
        return expenseRepository.findByUserIdAndDateBetweenOrderByDateDesc(
                getCurrentUser().getId(), start, end);
    }

    public Map<String, Object> addExpense(Map<String, Object> body) {
        User user = getCurrentUser();

        Long categoryId = Long.valueOf(body.get("categoryId").toString());
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        Expense expense = Expense.builder()
                .title(body.get("title").toString())
                .amount(new BigDecimal(body.get("amount").toString()))
                .date(LocalDate.parse(body.get("date").toString()))
                .description(body.getOrDefault("description", "").toString())
                .isRecurring(Boolean.parseBoolean(
                        body.getOrDefault("isRecurring", "false").toString()))
                .category(category)
                .user(user)
                .build();

        expenseRepository.save(expense);

        return Map.of(
                "message", "Expense added successfully",
                "id", expense.getId(),
                "title", expense.getTitle(),
                "amount", expense.getAmount()
        );
    }

    public Map<String, Object> updateExpense(Long id, Map<String, Object> body) {
        User user = getCurrentUser();

        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Expense not found"));

        if (!expense.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized");
        }

        if (body.containsKey("title"))
            expense.setTitle(body.get("title").toString());

        if (body.containsKey("amount"))
            expense.setAmount(new BigDecimal(body.get("amount").toString()));

        if (body.containsKey("date"))
            expense.setDate(LocalDate.parse(body.get("date").toString()));

        if (body.containsKey("description"))
            expense.setDescription(body.get("description").toString());

        expenseRepository.save(expense);

        return Map.of("message", "Expense updated", "id", expense.getId());
    }

    public Map<String, Object> deleteExpense(Long id) {
        User user = getCurrentUser();

        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Expense not found"));

        if (!expense.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized");
        }

        expenseRepository.delete(expense);
        return Map.of("message", "Expense deleted successfully");
    }
}
