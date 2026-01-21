package com.template.service;

import com.template.entity.User;
import com.template.repository.UserRepository;
import com.template.dto.RegisterRequestDTO;
import com.template.dto.LoginRequestDTO;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository repo;
    private final PasswordEncoder encoder;

    public AuthService(UserRepository repo, PasswordEncoder encoder) {
        this.repo = repo;
        this.encoder = encoder;
    }

    @Transactional
    public User register(RegisterRequestDTO dto) {
    	
    	var email = normalizeEmail(dto.email());
    	
        if (repo.existsByEmail(email)) {
            throw new RuntimeException("Email déjà utilisé");
        }

        User user = new User();
        user.setEmail(email);
        user.setPassword(encoder.encode(dto.password()));
        user.setFirstName(dto.firstName());
        user.setLastName(dto.lastName());
        user.getRoles().add("USER");

        return repo.save(user);
    }

    @Transactional(readOnly = true)
    public User login(LoginRequestDTO dto) {

        User user = repo.findByEmail(dto.email())
                .orElseThrow(() -> new RuntimeException("Identifiants incorrects"));

        if (!encoder.matches(dto.password(), user.getPassword())) {
            throw new RuntimeException("Identifiants incorrects");
        }
        return user;
    }
    

	private String normalizeEmail(String email) {
	    return email == null ? null : email.trim().toLowerCase();
	}

}



