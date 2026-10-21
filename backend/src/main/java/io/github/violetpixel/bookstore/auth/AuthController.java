package io.github.violetpixel.bookstore.auth;

import java.time.Duration;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.github.violetpixel.bookstore.auth.AuthDtos.LoginRequest;
import io.github.violetpixel.bookstore.auth.AuthDtos.RegisterRequest;
import io.github.violetpixel.bookstore.auth.AuthDtos.SessionDto;
import io.github.violetpixel.bookstore.auth.AuthDtos.UserDto;
import io.github.violetpixel.bookstore.common.ApiException;
import io.github.violetpixel.bookstore.config.AppProperties;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Conta e sessão")
public class AuthController {

    static final String COOKIE = "livraria_refresh";

    private final AuthService auth;
    private final TokenService tokens;
    private final RefreshTokenStore refreshTokens;
    private final AppProperties props;

    public AuthController(AuthService auth, TokenService tokens, RefreshTokenStore refreshTokens, AppProperties props) {
        this.auth = auth;
        this.tokens = tokens;
        this.refreshTokens = refreshTokens;
        this.props = props;
    }

    @PostMapping("/register")
    public ResponseEntity<SessionDto> register(@Valid @RequestBody RegisterRequest req) {
        return session(auth.register(req), HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<SessionDto> login(@Valid @RequestBody LoginRequest req) {
        return session(auth.login(req), HttpStatus.OK);
    }

    @PostMapping("/refresh")
    public ResponseEntity<SessionDto> refresh(@CookieValue(name = COOKIE, required = false) String token) {
        long userId = refreshTokens.consume(token)
            .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "sessao_expirada", "Sua sessão expirou. Entre de novo."));
        return session(auth.get(userId), HttpStatus.OK);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@CookieValue(name = COOKIE, required = false) String token) {
        refreshTokens.revoke(token);
        return ResponseEntity.noContent().header(HttpHeaders.SET_COOKIE, cookie("", Duration.ZERO).toString()).build();
    }

    @GetMapping("/me")
    public UserDto me(Authentication authentication) {
        return UserDto.of(auth.get(CurrentUser.id(authentication)));
    }

    private ResponseEntity<SessionDto> session(AppUser user, HttpStatus status) {
        String refresh = refreshTokens.issue(user.getId());
        SessionDto body = new SessionDto(tokens.accessToken(user), tokens.accessTtlSeconds(), UserDto.of(user));
        return ResponseEntity.status(status)
            .header(HttpHeaders.SET_COOKIE, cookie(refresh, Duration.ofSeconds(refreshTokens.ttlSeconds())).toString())
            .body(body);
    }

    private ResponseCookie cookie(String value, Duration maxAge) {
        return ResponseCookie.from(COOKIE, value)
            .httpOnly(true)
            .secure(props.https())
            .sameSite("Strict")
            .path("/api/auth")
            .maxAge(maxAge)
            .build();
    }
}
