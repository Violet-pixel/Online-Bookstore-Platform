package io.github.violetpixel.bookstore.auth;

import java.time.Instant;
import java.util.Locale;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.github.violetpixel.bookstore.auth.AuthDtos.LoginRequest;
import io.github.violetpixel.bookstore.auth.AuthDtos.RegisterRequest;
import io.github.violetpixel.bookstore.common.ApiException;

@Service
public class AuthService {

    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final LoginThrottle throttle;
    /** Hash descartável: quando o e-mail não existe, a conferência gasta o mesmo tempo. */
    private final String dummyHash;

    public AuthService(UserRepository users, PasswordEncoder encoder, LoginThrottle throttle) {
        this.users = users;
        this.encoder = encoder;
        this.throttle = throttle;
        this.dummyHash = encoder.encode("conta-inexistente");
    }

    @Transactional
    public AppUser register(RegisterRequest req) {
        String email = req.email().trim().toLowerCase(Locale.ROOT);
        if (users.existsByEmailIgnoreCase(email)) {
            throw ApiException.conflict("email_em_uso", "Já existe uma conta com este e-mail.");
        }
        AppUser user = new AppUser(email, encoder.encode(req.password()), req.fullName().trim(), UserRole.CUSTOMER);
        user.setLastLoginAt(Instant.now());
        return users.save(user);
    }

    @Transactional
    public AppUser login(LoginRequest req) {
        String email = req.email().trim().toLowerCase(Locale.ROOT);
        throttle.check(email);
        AppUser user = users.findByEmailIgnoreCase(email).orElse(null);
        boolean ok = encoder.matches(req.password(), user != null ? user.getPasswordHash() : dummyHash);
        if (user == null || !ok || !user.isActive()) {
            throttle.failed(email);
            throw new ApiException(HttpStatus.UNAUTHORIZED, "credenciais_invalidas", "E-mail ou senha incorretos.");
        }
        throttle.succeeded(email);
        user.setLastLoginAt(Instant.now());
        return user;
    }

    @Transactional(readOnly = true)
    public AppUser get(long id) {
        return users.findById(id).filter(AppUser::isActive)
            .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "sessao_invalida", "Sua sessão expirou. Entre de novo."));
    }
}
