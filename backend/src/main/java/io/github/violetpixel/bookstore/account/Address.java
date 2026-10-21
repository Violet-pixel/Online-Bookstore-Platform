package io.github.violetpixel.bookstore.account;

import java.time.Instant;

import io.github.violetpixel.bookstore.common.Flag;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "address")
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false, length = 40)
    private String label;

    @Column(nullable = false, length = 120)
    private String recipient;

    @Column(nullable = false, length = 9)
    private String zipCode;

    @Column(nullable = false, length = 160)
    private String street;

    @Column(nullable = false, length = 20)
    private String streetNumber;

    @Column(length = 80)
    private String complement;

    @Column(nullable = false, length = 80)
    private String district;

    @Column(nullable = false, length = 80)
    private String city;

    @Column(nullable = false, length = 2)
    private String state;

    @Convert(converter = Flag.class)
    @Column(nullable = false)
    private Boolean isDefault = Boolean.FALSE;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    protected Address() { }

    public Address(Long userId) {
        this.userId = userId;
    }

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }
    public String getRecipient() { return recipient; }
    public void setRecipient(String recipient) { this.recipient = recipient; }
    public String getZipCode() { return zipCode; }
    public void setZipCode(String zipCode) { this.zipCode = zipCode; }
    public String getStreet() { return street; }
    public void setStreet(String street) { this.street = street; }
    public String getStreetNumber() { return streetNumber; }
    public void setStreetNumber(String streetNumber) { this.streetNumber = streetNumber; }
    public String getComplement() { return complement; }
    public void setComplement(String complement) { this.complement = complement; }
    public String getDistrict() { return district; }
    public void setDistrict(String district) { this.district = district; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
    public boolean isDefault() { return Boolean.TRUE.equals(isDefault); }
    public void setDefault(boolean value) { this.isDefault = value; }
    public Instant getCreatedAt() { return createdAt; }
}
