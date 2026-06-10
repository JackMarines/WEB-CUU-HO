package com.emergency.controller;

import com.emergency.dto.response.UserResponse;
import com.emergency.service.UserAdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/users")
public class UserController {

    private final UserAdminService userAdminService;

    public UserController(UserAdminService userAdminService) {
        this.userAdminService = userAdminService;
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> getAll() {
        return ResponseEntity.ok(userAdminService.getAll());
    }

    @PostMapping
    public ResponseEntity<UserResponse> createAdmin(@RequestBody Map<String, String> body) {
        return ResponseEntity.ok(userAdminService.createAdmin(
            body.get("name"), body.get("email"), body.get("password")));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id, Authentication auth) {
        userAdminService.deleteUser(id, auth);
        return ResponseEntity.noContent().build();
    }
}
