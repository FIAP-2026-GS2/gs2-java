package br.com.sampatech.portal.service;

import br.com.sampatech.portal.dto.request.LoginRequest;
import br.com.sampatech.portal.dto.request.RegisterRequest;
import br.com.sampatech.portal.dto.response.AuthResponse;
import br.com.sampatech.portal.dto.response.UserResponse;
import br.com.sampatech.portal.entity.PlatformUser;
import br.com.sampatech.portal.entity.UserType;
import br.com.sampatech.portal.exception.BusinessException;
import br.com.sampatech.portal.repository.PlatformUserRepository;
import br.com.sampatech.portal.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final PlatformUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Transactional
    public UserResponse register(RegisterRequest req) {
        String email = req.email().trim().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            throw new BusinessException("E-mail já cadastrado");
        }

        PlatformUser user = PlatformUser.builder()
                .name(req.name().trim())
                .email(email)
                .passwordHash(passwordEncoder.encode(req.password()))
                .userType(UserType.CITIZEN)
                .registrationDate(LocalDateTime.now())
                .active(true)
                .build();

        PlatformUser saved = userRepository.save(user);
        // Passo messaging: publicar o evento usuario.cadastrado aqui
        return UserResponse.from(saved);
    }

    public AuthResponse login(LoginRequest req) {
        String email = req.email().trim().toLowerCase();
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, req.password()));

        PlatformUser user = userRepository.findByEmail(email).orElseThrow();
        String token = jwtService.generateToken(user);
        return AuthResponse.bearer(token, UserResponse.from(user));
    }
}