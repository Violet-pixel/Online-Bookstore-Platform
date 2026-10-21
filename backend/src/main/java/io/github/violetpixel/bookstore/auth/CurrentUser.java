package io.github.violetpixel.bookstore.auth;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;

import io.github.violetpixel.bookstore.common.ApiException;

/** Lê o id da pessoa logada a partir do token JWT. */
public final class CurrentUser {

    private CurrentUser() { }

    public static long id(Authentication auth) {
        if (auth != null && auth.getPrincipal() instanceof Jwt jwt) {
            return Long.parseLong(jwt.getSubject());
        }
        throw new ApiException(org.springframework.http.HttpStatus.UNAUTHORIZED, "nao_autenticado", "Entre na sua conta para continuar.");
    }

    public static boolean isAdmin(Authentication auth) {
        return auth != null && auth.getAuthorities().stream().anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
    }
}
