package com.template.service;

import com.template.entity.User;
import com.template.repository.UserRepository;
import com.template.dto.RegisterRequestDTO;
import com.template.dto.LoginRequestDTO;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository repo;
    private final PasswordEncoder encoder;

    public AuthService(UserRepository repo, PasswordEncoder encoder) {
        this.repo = repo;
        this.encoder = encoder;
    }

    public User register(RegisterRequestDTO dto) {

        if (repo.existsByEmail(dto.email())) {
            throw new RuntimeException("Email déjà utilisé");
        }

        User user = new User();
        user.setEmail(dto.email());
        user.setPassword(encoder.encode(dto.password()));
        user.setFirstName(dto.firstName());
        user.setLastName(dto.lastName());
        user.getRoles().add("USER");

        return repo.save(user);
    }

    public User login(LoginRequestDTO dto) {

        User user = repo.findByEmail(dto.email())
                .orElseThrow(() -> new RuntimeException("Identifiants incorrects"));

        if (!encoder.matches(dto.password(), user.getPassword())) {
            throw new RuntimeException("Identifiants incorrects");
        }

        return user;
    }
}

