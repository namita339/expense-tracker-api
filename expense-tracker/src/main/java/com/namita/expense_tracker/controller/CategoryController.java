package com.namita.expense_tracker.controller;

import com.namita.expense_tracker.model.Category;
import com.namita.expense_tracker.model.User;
import com.namita.expense_tracker.repository.CategoryRepository;
import com.namita.expense_tracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @GetMapping
    public ResponseEntity<List<Category>> getAll() {
        return ResponseEntity.ok(
                categoryRepository.findByUserId(getCurrentUser().getId())
        );
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Map<String, String> body) {
        User user = getCurrentUser();

        Category category = Category.builder()
                .name(body.get("name"))
                .colorCode(body.getOrDefault("colorCode", "#000000"))
                .user(user)
                .build();

        categoryRepository.save(category);

        return ResponseEntity.ok(Map.of(
                "message", "Category created",
                "id", category.getId(),
                "name", category.getName()
        ));
    }
}
