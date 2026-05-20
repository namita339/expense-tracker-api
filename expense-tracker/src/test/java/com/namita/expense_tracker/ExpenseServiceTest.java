package com.namita.expense_tracker;

import com.namita.expense_tracker.model.Category;
import com.namita.expense_tracker.model.Expense;
import com.namita.expense_tracker.model.User;
import com.namita.expense_tracker.repository.CategoryRepository;
import com.namita.expense_tracker.repository.ExpenseRepository;
import com.namita.expense_tracker.repository.UserRepository;
import com.namita.expense_tracker.service.ExpenseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExpenseServiceTest {

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private ExpenseService expenseService;

    private User testUser;
    private Category testCategory;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .name("Namita")
                .email("namita@gmail.com")
                .password("encoded_password")
                .role(User.Role.USER)
                .build();

        testCategory = Category.builder()
                .id(1L)
                .name("Food")
                .colorCode("#FF5733")
                .user(testUser)
                .build();

        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("namita@gmail.com");
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);

        when(userRepository.findByEmail("namita@gmail.com"))
                .thenReturn(Optional.of(testUser));
    }

    @Test
    void getAllExpenses_shouldReturnListForCurrentUser() {
        Expense expense = Expense.builder()
                .id(1L)
                .title("Lunch")
                .amount(new BigDecimal("150.00"))
                .date(LocalDate.now())
                .category(testCategory)
                .user(testUser)
                .build();

        when(expenseRepository.findByUserIdOrderByDateDesc(1L))
                .thenReturn(List.of(expense));

        List<Expense> result = expenseService.getAllExpenses();

        assertEquals(1, result.size());
        assertEquals("Lunch", result.get(0).getTitle());
        assertEquals(new BigDecimal("150.00"), result.get(0).getAmount());
    }

    @Test
    void addExpense_shouldSaveAndReturnSuccessMessage() {
        when(categoryRepository.findById(1L))
                .thenReturn(Optional.of(testCategory));

        Map<String, Object> body = new HashMap<>();
        body.put("title", "Dinner");
        body.put("amount", "200.00");
        body.put("date", "2026-05-16");
        body.put("categoryId", "1");
        body.put("description", "Dinner at home");
        body.put("isRecurring", "false");

        when(expenseRepository.save(any(Expense.class)))
                .thenAnswer(invocation -> {
                    Expense e = invocation.getArgument(0);
                    return Expense.builder()
                            .id(2L)
                            .title(e.getTitle())
                            .amount(e.getAmount())
                            .date(e.getDate())
                            .description(e.getDescription())
                            .category(e.getCategory())
                            .user(e.getUser())
                            .build();
                });

        Map<String, Object> result = expenseService.addExpense(body);

        assertEquals("Expense added successfully", result.get("message"));
        verify(expenseRepository, times(1)).save(any(Expense.class));
    }

    @Test
    void deleteExpense_shouldDeleteWhenUserIsOwner() {
        Expense expense = Expense.builder()
                .id(1L)
                .title("Lunch")
                .amount(new BigDecimal("150.00"))
                .date(LocalDate.now())
                .category(testCategory)
                .user(testUser)
                .build();

        when(expenseRepository.findById(1L))
                .thenReturn(Optional.of(expense));

        Map<String, Object> result = expenseService.deleteExpense(1L);

        assertEquals("Expense deleted successfully", result.get("message"));
        verify(expenseRepository, times(1)).delete(expense);
    }

    @Test
    void deleteExpense_shouldThrowWhenUserIsNotOwner() {
        User anotherUser = User.builder()
                .id(99L)
                .email("other@gmail.com")
                .build();

        Expense expense = Expense.builder()
                .id(1L)
                .title("Lunch")
                .amount(new BigDecimal("150.00"))
                .date(LocalDate.now())
                .category(testCategory)
                .user(anotherUser)
                .build();

        when(expenseRepository.findById(1L))
                .thenReturn(Optional.of(expense));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> expenseService.deleteExpense(1L));

        assertEquals("Unauthorized", ex.getMessage());
        verify(expenseRepository, never()).delete(any());
    }

    @Test
    void getExpensesByDateRange_shouldReturnFilteredList() {
        LocalDate start = LocalDate.of(2026, 5, 1);
        LocalDate end = LocalDate.of(2026, 5, 31);

        Expense expense = Expense.builder()
                .id(1L)
                .title("Groceries")
                .amount(new BigDecimal("300.00"))
                .date(LocalDate.of(2026, 5, 10))
                .category(testCategory)
                .user(testUser)
                .build();

        when(expenseRepository.findByUserIdAndDateBetweenOrderByDateDesc(
                1L, start, end))
                .thenReturn(List.of(expense));

        List<Expense> result = expenseService.getExpensesByDateRange(start, end);

        assertEquals(1, result.size());
        assertEquals("Groceries", result.get(0).getTitle());
    }
}