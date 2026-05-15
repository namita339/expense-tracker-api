package com.namita.expense_tracker.repository;

import com.namita.expense_tracker.model.Budget;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BudgetRepository extends JpaRepository<Budget, Long> {

    Optional<Budget> findByUserIdAndCategoryIdAndMonthAndYear(
            Long userId, Long categoryId, int month, int year);

    List<Budget> findByUserIdAndMonthAndYear(
            Long userId, int month, int year);
}
