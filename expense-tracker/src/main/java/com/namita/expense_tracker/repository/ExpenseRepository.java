package com.namita.expense_tracker.repository;

import com.namita.expense_tracker.model.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    List<Expense> findByUserIdOrderByDateDesc(Long userId);

    List<Expense> findByUserIdAndDateBetweenOrderByDateDesc(
            Long userId, LocalDate start, LocalDate end);

    List<Expense> findByUserIdAndCategoryIdOrderByDateDesc(
            Long userId, Long categoryId);

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e " +
            "WHERE e.user.id = :userId AND e.category.id = :categoryId " +
            "AND MONTH(e.date) = :month AND YEAR(e.date) = :year")
    BigDecimal sumByUserCategoryAndMonth(Long userId, Long categoryId,
                                         int month, int year);
}
