package io.github.violetpixel.bookstore.order;

import io.github.violetpixel.bookstore.account.Address;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/** Cópia do endereço no momento da compra: mudar o cadastro depois não altera pedidos antigos. */
@Embeddable
public class ShippingAddress {

    @Column(name = "ship_recipient", nullable = false, length = 120)
    private String recipient;

    @Column(name = "ship_zip_code", nullable = false, length = 9)
    private String zipCode;

    @Column(name = "ship_street", nullable = false, length = 160)
    private String street;

    @Column(name = "ship_number", nullable = false, length = 20)
    private String number;

    @Column(name = "ship_complement", length = 80)
    private String complement;

    @Column(name = "ship_district", nullable = false, length = 80)
    private String district;

    @Column(name = "ship_city", nullable = false, length = 80)
    private String city;

    @Column(name = "ship_state", nullable = false, length = 2)
    private String state;

    protected ShippingAddress() { }

    public static ShippingAddress of(Address a) {
        ShippingAddress s = new ShippingAddress();
        s.recipient = a.getRecipient();
        s.zipCode = a.getZipCode();
        s.street = a.getStreet();
        s.number = a.getStreetNumber();
        s.complement = a.getComplement();
        s.district = a.getDistrict();
        s.city = a.getCity();
        s.state = a.getState();
        return s;
    }

    public String getRecipient() { return recipient; }
    public String getZipCode() { return zipCode; }
    public String getStreet() { return street; }
    public String getNumber() { return number; }
    public String getComplement() { return complement; }
    public String getDistrict() { return district; }
    public String getCity() { return city; }
    public String getState() { return state; }
}
