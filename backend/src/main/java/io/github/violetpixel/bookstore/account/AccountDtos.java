package io.github.violetpixel.bookstore.account;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public final class AccountDtos {

    private AccountDtos() { }

    public record AddressRequest(
            @NotBlank(message = "Dê um nome ao endereço.") @Size(max = 40) String label,
            @NotBlank(message = "Informe quem vai receber.") @Size(max = 120) String recipient,
            @NotBlank(message = "Informe o CEP.") @Pattern(regexp = "\\d{5}-?\\d{3}", message = "CEP inválido.") String zipCode,
            @NotBlank(message = "Informe a rua.") @Size(max = 160) String street,
            @NotBlank(message = "Informe o número.") @Size(max = 20) String streetNumber,
            @Size(max = 80) String complement,
            @NotBlank(message = "Informe o bairro.") @Size(max = 80) String district,
            @NotBlank(message = "Informe a cidade.") @Size(max = 80) String city,
            @NotBlank(message = "Informe a UF.") @Pattern(regexp = "[A-Za-z]{2}", message = "UF inválida.") String state,
            boolean isDefault) {
    }

    public record AddressDto(long id, String label, String recipient, String zipCode, String street, String streetNumber,
            String complement, String district, String city, String state, boolean isDefault) {
        public static AddressDto of(Address a) {
            return new AddressDto(a.getId(), a.getLabel(), a.getRecipient(), a.getZipCode(), a.getStreet(), a.getStreetNumber(),
                    a.getComplement(), a.getDistrict(), a.getCity(), a.getState(), a.isDefault());
        }
    }

    public record ProfileRequest(
            @NotBlank(message = "Informe seu nome.") @Size(max = 120) String fullName,
            @Pattern(regexp = "^$|^[0-9()+\\- ]{8,20}$", message = "Telefone inválido.") String phone) {
    }

    public record PasswordRequest(
            @NotBlank(message = "Informe a senha atual.") String currentPassword,
            @NotBlank @Size(min = 8, max = 72, message = "A nova senha precisa ter de 8 a 72 caracteres.") String newPassword) {
    }
}
