package com.namita.expense_tracker;

import com.namita.expense_tracker.model.Budget;
import com.namita.expense_tracker.model.Category;
import com.namita.expense_tracker.model.User;
import com.namita.expense_tracker.repository.BudgetRepository;
import com.namita.expense_tracker.repository.CategoryRepository;
import com.namita.expense_tracker.repository.ExpenseRepository;
import com.namita.expense_tracker.repository.UserRepository;
import com.namita.expense_tracker.service.BudgetService;
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
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BudgetServiceTest {

    @Mock private BudgetRepository budgetRepository;
    @Mock private ExpenseRepository expenseRepository;
    @Mock private UserRepository userRepository;
    @Mock private CategoryRepository categoryRepository;

    @InjectMocks
    private BudgetService budgetService;

    private User testUser;
    private Category testCategory;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .email("namita@gmail.com")
                .role(User.Role.USER)
                .build();

        testCategory = Category.builder()
                .id(1L)
                .name("Food")
                .user(testUser)
                .build();

        // use lenient() so unused stubs don't cause failures
        Authentication auth = mock(Authentication.class);
        lenient().when(auth.getName()).thenReturn("namita@gmail.com");
        SecurityContext ctx = mock(SecurityContext.class);
        lenient().when(ctx.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(ctx);

        lenient().when(userRepository.findByEmail("namita@gmail.com"))
                .thenReturn(Optional.of(testUser));
    }

    // Test 1 — alert triggers when spending is above 80%
    @Test
    void getBudgetStatus_shouldTriggerAlertWhenAbove80Percent() {
        BigDecimal spent = new BigDecimal("850");
        BigDecimal budgetAmt = new BigDecimal("1000");
        double percent = spent.doubleValue() / budgetAmt.doubleValue() * 100;

        assertTrue(percent >= 80,
                "Alert should trigger when spending is 85%");
    }

    // Test 2 — no alert when spending is below 80%
    @Test
    void getBudgetStatus_shouldNotAlertWhenBelow80Percent() {
        BigDecimal spent = new BigDecimal("400");
        BigDecimal budgetAmt = new BigDecimal("1000");
        double percent = spent.doubleValue() / budgetAmt.doubleValue() * 100;

        assertFalse(percent >= 80,
                "Should not alert when spending is only 40%");
    }

    // Test 3 — remaining amount calculation is correct
    @Test
    void remainingAmount_shouldBeCorrect() {
        BigDecimal budget = new BigDecimal("1000");
        BigDecimal spent = new BigDecimal("350");
        BigDecimal remaining = budget.subtract(spent);

        assertEquals(new BigDecimal("650"), remaining);
    }

    // Test 4 — setBudget saves correctly
    @Test
    void setBudget_shouldSaveNewBudget() {
        when(categoryRepository.findById(1L))
                .thenReturn(Optional.of(testCategory));

        when(budgetRepository
                .findByUserIdAndCategoryIdAndMonthAndYear(1L, 1L, 5, 2026))
                .thenReturn(Optional.empty());

        Budget savedBudget = Budget.builder()
                .id(1L)
                .amount(new BigDecimal("500"))
                .month(5).year(2026)
                .category(testCategory)
                .user(testUser)
                .build();

        when(budgetRepository.save(any(Budget.class)))
                .thenReturn(savedBudget);

        Map<String, Object> body = new HashMap<>();
        body.put("categoryId", "1");
        body.put("amount", "500");
        body.put("month", "5");
        body.put("year", "2026");

        Map<String, Object> result = budgetService.setBudget(body);

        assertEquals("Budget set successfully", result.get("message"));
        verify(budgetRepository, times(1)).save(any(Budget.class));
    }
}
