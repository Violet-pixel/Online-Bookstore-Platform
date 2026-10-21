package io.github.violetpixel.bookstore.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class AuthDtos {

    private AuthDtos() { }

    public record RegisterRequest(
            @NotBlank(message = "Informe seu nome.") @Size(max = 120) String fullName,
            @NotBlank(message = "Informe seu e-mail.") @Email(message = "E-mail inválido.") @Size(max = 160) String email,
            @NotBlank(message = "Crie uma senha.") @Size(min = 8, max = 72, message = "A senha precisa ter de 8 a 72 caracteres.") String password) {
    }

    public record LoginRequest(
            @NotBlank(message = "Informe seu e-mail.") String email,
            @NotBlank(message = "Informe sua senha.") String password) {
    }

    public record UserDto(long id, String fullName, String email, String phone, String role) {
        public static UserDto of(AppUser u) {
            return new UserDto(u.getId(), u.getFullName(), u.getEmail(), u.getPhone(), u.getRole().name());
        }
    }

    public record SessionDto(String accessToken, long expiresIn, UserDto user) {
    }
}
