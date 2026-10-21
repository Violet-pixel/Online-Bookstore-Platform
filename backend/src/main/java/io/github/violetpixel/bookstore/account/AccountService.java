package io.github.violetpixel.bookstore.account;

import java.util.List;
import java.util.Locale;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.github.violetpixel.bookstore.account.AccountDtos.AddressDto;
import io.github.violetpixel.bookstore.account.AccountDtos.AddressRequest;
import io.github.violetpixel.bookstore.account.AccountDtos.PasswordRequest;
import io.github.violetpixel.bookstore.account.AccountDtos.ProfileRequest;
import io.github.violetpixel.bookstore.auth.AppUser;
import io.github.violetpixel.bookstore.auth.AuthDtos.UserDto;
import io.github.violetpixel.bookstore.auth.UserRepository;
import io.github.violetpixel.bookstore.common.ApiException;
import io.github.violetpixel.bookstore.common.Text;

@Service
public class AccountService {

    private static final int MAX_ADDRESSES = 10;

    private final UserRepository users;
    private final AddressRepository addresses;
    private final PasswordEncoder encoder;

    public AccountService(UserRepository users, AddressRepository addresses, PasswordEncoder encoder) {
        this.users = users;
        this.addresses = addresses;
        this.encoder = encoder;
    }

    @Transactional
    public UserDto updateProfile(long userId, ProfileRequest req) {
        AppUser user = users.findById(userId).orElseThrow(() -> ApiException.notFound("Usuário"));
        user.setFullName(req.fullName().trim());
        user.setPhone(req.phone() == null || req.phone().isBlank() ? null : req.phone().trim());
        return UserDto.of(user);
    }

    @Transactional
    public void changePassword(long userId, PasswordRequest req) {
        AppUser user = users.findById(userId).orElseThrow(() -> ApiException.notFound("Usuário"));
        if (!encoder.matches(req.currentPassword(), user.getPasswordHash())) {
            throw ApiException.badRequest("senha_incorreta", "A senha atual não confere.");
        }
        user.setPasswordHash(encoder.encode(req.newPassword()));
    }

    @Transactional(readOnly = true)
    public List<AddressDto> addresses(long userId) {
        return addresses.findByUserIdOrderByIdAsc(userId).stream().map(AddressDto::of).toList();
    }

    @Transactional(readOnly = true)
    public Address address(long userId, long addressId) {
        return addresses.findByIdAndUserId(addressId, userId).orElseThrow(() -> ApiException.notFound("Endereço"));
    }

    @Transactional
    public AddressDto create(long userId, AddressRequest req) {
        if (addresses.countByUserId(userId) >= MAX_ADDRESSES) {
            throw ApiException.unprocessable("limite_enderecos", "Você pode guardar até 10 endereços.");
        }
        Address address = new Address(userId);
        apply(address, req);
        boolean first = addresses.countByUserId(userId) == 0;
        address = addresses.save(address);
        if (req.isDefault() || first) {
            makeDefault(userId, address);
        }
        return AddressDto.of(address);
    }

    @Transactional
    public AddressDto update(long userId, long addressId, AddressRequest req) {
        Address address = address(userId, addressId);
        apply(address, req);
        if (req.isDefault()) {
            makeDefault(userId, address);
        }
        return AddressDto.of(address);
    }

    @Transactional
    public void delete(long userId, long addressId) {
        Address address = address(userId, addressId);
        addresses.delete(address);
        if (address.isDefault()) {
            addresses.findByUserIdOrderByIdAsc(userId).stream().filter(a -> !a.getId().equals(addressId)).findFirst()
                .ifPresent(a -> a.setDefault(true));
        }
    }

    private void makeDefault(long userId, Address chosen) {
        for (Address a : addresses.findByUserIdOrderByIdAsc(userId)) {
            a.setDefault(a.getId().equals(chosen.getId()));
        }
    }

    private static void apply(Address a, AddressRequest req) {
        String zip = Text.digits(req.zipCode());
        a.setLabel(req.label().trim());
        a.setRecipient(req.recipient().trim());
        a.setZipCode(zip.substring(0, 5) + "-" + zip.substring(5));
        a.setStreet(req.street().trim());
        a.setStreetNumber(req.streetNumber().trim());
        a.setComplement(req.complement() == null || req.complement().isBlank() ? null : req.complement().trim());
        a.setDistrict(req.district().trim());
        a.setCity(req.city().trim());
        a.setState(req.state().trim().toUpperCase(Locale.ROOT));
    }
}
