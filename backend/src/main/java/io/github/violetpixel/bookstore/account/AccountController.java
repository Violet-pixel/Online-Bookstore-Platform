package io.github.violetpixel.bookstore.account;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.github.violetpixel.bookstore.account.AccountDtos.AddressDto;
import io.github.violetpixel.bookstore.account.AccountDtos.AddressRequest;
import io.github.violetpixel.bookstore.account.AccountDtos.PasswordRequest;
import io.github.violetpixel.bookstore.account.AccountDtos.ProfileRequest;
import io.github.violetpixel.bookstore.auth.AuthDtos.UserDto;
import io.github.violetpixel.bookstore.auth.CurrentUser;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/account")
@Tag(name = "Minha conta")
public class AccountController {

    private final AccountService account;

    public AccountController(AccountService account) {
        this.account = account;
    }

    @PutMapping("/profile")
    public UserDto profile(Authentication auth, @Valid @RequestBody ProfileRequest req) {
        return account.updateProfile(CurrentUser.id(auth), req);
    }

    @PostMapping("/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void password(Authentication auth, @Valid @RequestBody PasswordRequest req) {
        account.changePassword(CurrentUser.id(auth), req);
    }

    @GetMapping("/addresses")
    public List<AddressDto> addresses(Authentication auth) {
        return account.addresses(CurrentUser.id(auth));
    }

    @PostMapping("/addresses")
    @ResponseStatus(HttpStatus.CREATED)
    public AddressDto create(Authentication auth, @Valid @RequestBody AddressRequest req) {
        return account.create(CurrentUser.id(auth), req);
    }

    @PutMapping("/addresses/{id}")
    public AddressDto update(Authentication auth, @PathVariable long id, @Valid @RequestBody AddressRequest req) {
        return account.update(CurrentUser.id(auth), id, req);
    }

    @DeleteMapping("/addresses/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(Authentication auth, @PathVariable long id) {
        account.delete(CurrentUser.id(auth), id);
    }
}
