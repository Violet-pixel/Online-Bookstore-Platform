package io.github.violetpixel.bookstore.order;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "order_item")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private PurchaseOrder order;

    @Column(nullable = false)
    private Long bookId;

    @Column(nullable = false, length = 220)
    private String bookSlug;

    @Column(nullable = false, length = 200)
    private String bookTitle;

    @Column(length = 300)
    private String bookAuthors;

    @Column(nullable = false, length = 17)
    private String bookIsbn;

    @Column(nullable = false)
    private Integer coverStyle;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal lineTotal;

    protected OrderItem() { }

    public OrderItem(PurchaseOrder order, Long bookId, String bookSlug, String bookTitle, String bookAuthors, String bookIsbn,
            int coverStyle, BigDecimal unitPrice, int quantity, BigDecimal lineTotal) {
        this.order = order;
        this.bookId = bookId;
        this.bookSlug = bookSlug;
        this.bookTitle = bookTitle;
        this.bookAuthors = bookAuthors;
        this.bookIsbn = bookIsbn;
        this.coverStyle = coverStyle;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
        this.lineTotal = lineTotal;
    }

    public Long getId() { return id; }
    public PurchaseOrder getOrder() { return order; }
    public Long getBookId() { return bookId; }
    public String getBookSlug() { return bookSlug; }
    public String getBookTitle() { return bookTitle; }
    public String getBookAuthors() { return bookAuthors; }
    public String getBookIsbn() { return bookIsbn; }
    public int getCoverStyle() { return coverStyle; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public int getQuantity() { return quantity; }
    public BigDecimal getLineTotal() { return lineTotal; }
}
