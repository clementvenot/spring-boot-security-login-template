package com.template.service;

import com.template.entity.User;
import com.template.repository.UserRepository;

import jakarta.validation.Validator;

import com.template.dto.RegisterRequestDTO;
import com.template.dto.LoginRequestDTO;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

    private final UserRepository repo;
    private final PasswordEncoder encoder;
    private final Validator validator;

    public AuthService(UserRepository repo, PasswordEncoder encoder, Validator validator) {
        this.repo = repo;
        this.encoder = encoder;
        this.validator = validator;
    }

    @Transactional
    public User register(RegisterRequestDTO dto) {
    	
        var pwdViolations = validator.validateProperty(dto, "password");    
        if (!pwdViolations.isEmpty()) {        
        	String msg = pwdViolations.stream()                
        			.map(v -> v.getMessage())                
        			.reduce((a, b) -> a + "; " + b)                
        			.orElse("Invalid password");        
        	throw new IllegalArgumentException(msg);    
        	}
        if (dto.email() == null || dto.email().isBlank()) {            
        	throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already used");
        	}
    	
    	var email = normalizeEmail(dto.email());
    	
        if (repo.existsByEmail(email)) {
            throw new RuntimeException("Email already used");
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
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Incorrect credentials"));

        if (!encoder.matches(dto.password(), user.getPassword())) {
            throw new RuntimeException("Incorrect credentials");
        }
        return user;
    }

	private String normalizeEmail(String email) {
	    return email == null ? null : email.trim().toLowerCase();
	}
}



