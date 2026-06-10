package com.emergency.service;

import com.emergency.dto.response.UserResponse;
import com.emergency.exception.ResourceNotFoundException;
import com.emergency.exception.UnauthorizedException;
import com.emergency.model.User;
import com.emergency.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserAdminService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserAdminService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<UserResponse> getAll() {
        return userRepository.findAllByOrderByCreatedAtDesc().stream()
            .map(UserResponse::fromEntity)
            .toList();
    }

    @Transactional
    public UserResponse createAdmin(String name, String email, String password) {
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already registered");
        }
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(User.Role.admin);
        return UserResponse.fromEntity(userRepository.save(user));
    }

    @Transactional
    public void deleteUser(Long id, Authentication auth) {
        User currentUser = getCurrentUser(auth);
        if (currentUser.getId().equals(id)) {
            throw new UnauthorizedException("Không thể xóa chính mình");
        }
        User target = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (target.getRole() == User.Role.superadmin) {
            throw new UnauthorizedException("Không thể xóa superadmin");
        }
        if (currentUser.getRole() == User.Role.admin && target.getRole() != User.Role.user) {
            throw new UnauthorizedException("Admin chỉ có thể xóa người dùng thông thường");
        }
        userRepository.deleteById(id);
    }

    private User getCurrentUser(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Current user not found"));
    }
}
