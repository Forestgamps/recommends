package ru.mirea.recom.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {
//    @GetMapping("/me")
//    public ResponseEntity<String> getCurrentUser(Authentication authentication) {
//        String username = authentication.getName();
//        return ResponseEntity.ok("Hello, " + username);
//    }

    @GetMapping("/me")
    public ResponseEntity<String> testMeEndpoint() {
        return ResponseEntity.ok("Hello, testUser");
    }
}
