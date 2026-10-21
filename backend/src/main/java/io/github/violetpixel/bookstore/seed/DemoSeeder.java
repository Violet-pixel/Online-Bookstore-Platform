package io.github.violetpixel.bookstore.seed;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import io.github.violetpixel.bookstore.account.Address;
import io.github.violetpixel.bookstore.account.AddressRepository;
import io.github.violetpixel.bookstore.auth.AppUser;
import io.github.violetpixel.bookstore.auth.UserRepository;
import io.github.violetpixel.bookstore.auth.UserRole;
import io.github.violetpixel.bookstore.catalog.Author;
import io.github.violetpixel.bookstore.catalog.AuthorRepository;
import io.github.violetpixel.bookstore.catalog.Book;
import io.github.violetpixel.bookstore.catalog.BookFormat;
import io.github.violetpixel.bookstore.catalog.BookRepository;
import io.github.violetpixel.bookstore.catalog.CatalogCache;
import io.github.violetpixel.bookstore.catalog.Category;
import io.github.violetpixel.bookstore.catalog.CategoryRepository;
import io.github.violetpixel.bookstore.catalog.CuratedCatalog;
import io.github.violetpixel.bookstore.catalog.CuratedCatalogRepository;
import io.github.violetpixel.bookstore.catalog.Publisher;
import io.github.violetpixel.bookstore.catalog.PublisherRepository;
import io.github.violetpixel.bookstore.common.Money;
import io.github.violetpixel.bookstore.common.RedisLock;
import io.github.violetpixel.bookstore.common.Text;
import io.github.violetpixel.bookstore.config.AppProperties;
import io.github.violetpixel.bookstore.order.Coupon;
import io.github.violetpixel.bookstore.order.CouponRepository;
import io.github.violetpixel.bookstore.order.OrderItem;
import io.github.violetpixel.bookstore.order.OrderRepository;
import io.github.violetpixel.bookstore.order.OrderStatus;
import io.github.violetpixel.bookstore.order.PurchaseOrder;
import io.github.violetpixel.bookstore.order.ShippingAddress;
import io.github.violetpixel.bookstore.payment.Payment;
import io.github.violetpixel.bookstore.payment.PaymentGateway;
import io.github.violetpixel.bookstore.payment.PaymentMethod;
import io.github.violetpixel.bookstore.payment.PaymentRepository;
import io.github.violetpixel.bookstore.payment.PaymentStatus;
import io.github.violetpixel.bookstore.seed.DemoBooks.B;
import io.github.violetpixel.bookstore.shipping.Shipment;
import io.github.violetpixel.bookstore.shipping.ShipmentRepository;
import io.github.violetpixel.bookstore.shipping.ShipmentStatus;
import io.github.violetpixel.bookstore.shipping.ShippingMethod;
import io.github.violetpixel.bookstore.shipping.ShippingMethodRepository;

/**
 * Prepara o banco na primeira subida: formas de envio sempre; acervo, contas, cupons e pedidos de exemplo
 * só com APP_SEED_DEMO=true e banco vazio. Todos os nomes, endereços e e-mails são fictícios.
 */
@Component
@Order(1)
public class DemoSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DemoSeeder.class);
    private static final String HUB = "Centro de distribuição · Rio de Janeiro, RJ";

    private final AppProperties props;
    private final TransactionTemplate tx;
    private final RedisLock lock;
    private final PasswordEncoder encoder;
    private final ShippingMethodRepository shippingMethods;
    private final UserRepository users;
    private final AddressRepository addresses;
    private final CategoryRepository categories;
    private final PublisherRepository publishers;
    private final AuthorRepository authors;
    private final BookRepository books;
    private final CuratedCatalogRepository catalogs;
    private final CouponRepository coupons;
    private final OrderRepository orders;
    private final PaymentRepository payments;
    private final ShipmentRepository shipments;
    private final PaymentGateway gateway;
    private final CatalogCache cache;

    public DemoSeeder(AppProperties props, PlatformTransactionManager txManager, RedisLock lock, PasswordEncoder encoder,
            ShippingMethodRepository shippingMethods, UserRepository users, AddressRepository addresses,
            CategoryRepository categories, PublisherRepository publishers, AuthorRepository authors, BookRepository books,
            CuratedCatalogRepository catalogs, CouponRepository coupons, OrderRepository orders, PaymentRepository payments,
            ShipmentRepository shipments, PaymentGateway gateway, CatalogCache cache) {
        this.props = props;
        this.tx = new TransactionTemplate(txManager);
        this.lock = lock;
        this.encoder = encoder;
        this.shippingMethods = shippingMethods;
        this.users = users;
        this.addresses = addresses;
        this.categories = categories;
        this.publishers = publishers;
        this.authors = authors;
        this.books = books;
        this.catalogs = catalogs;
        this.coupons = coupons;
        this.orders = orders;
        this.payments = payments;
        this.shipments = shipments;
        this.gateway = gateway;
        this.cache = cache;
    }

    @Override
    public void run(ApplicationArguments args) {
        lock.runExclusive("seed", Duration.ofMinutes(5), () -> tx.executeWithoutResult(status -> {
            seedShipping();
            if (props.demo().enabled() && users.count() == 0 && books.count() == 0) {
                seedDemo();
                cache.evictAll();
            }
        }));
    }

    private void seedShipping() {
        if (shippingMethods.count() > 0) {
            return;
        }
        shippingMethods.save(new ShippingMethod("ECONOMICA", "Econômica", "Rota Postal", "Entrega padrão, mais em conta",
                Money.of("14.90"), Money.of("4.50"), Money.of("199.00"), 5, 9, 1));
        shippingMethods.save(new ShippingMethod("EXPRESSA", "Expressa", "Voo Rápido Logística", "Prioridade na separação e no transporte",
                Money.of("24.90"), Money.of("7.90"), null, 1, 3, 2));
        shippingMethods.save(new ShippingMethod("RETIRADA", "Retirada em ponto parceiro", "Rede Ponto Livro", "Você retira num ponto perto de casa",
                Money.of("7.90"), Money.ZERO, Money.of("99.00"), 3, 6, 3));
    }

    private void seedDemo() {
        String domain = props.demo().domain();
        String hash = encoder.encode(props.demo().password());

        Map<String, Category> cats = new HashMap<>();
        int order = 0;
        for (DemoBooks.Cat c : DemoBooks.CATEGORIES) {
            cats.put(c.slug(), categories.save(new Category(c.name(), c.slug(), c.description(), c.accent(), order++)));
        }

        Map<String, Publisher> pubs = new HashMap<>();
        Map<String, Author> auths = new HashMap<>();
        Map<String, Book> byTitle = new HashMap<>();
        Instant now = Instant.now();
        int i = 0;
        for (B b : DemoBooks.BOOKS) {
            Publisher pub = pubs.computeIfAbsent(b.publisher(), n -> publishers.save(new Publisher(n, Text.slug(n))));
            Book book = new Book(isbn(i), b.title(), Text.slug(b.title()), cats.get(b.category()), pub);
            book.setSubtitle(b.subtitle());
            book.setSynopsis(b.synopsis());
            book.setPublishedYear(b.year());
            book.setPageCount(b.pages());
            book.setFormat(BookFormat.valueOf(b.format()));
            book.setPrice(Money.of(b.price()));
            book.setListPrice(Money.of(b.listPrice()));
            book.setStockQuantity(b.stock());
            book.setWeightGrams(b.weight());
            book.setCoverStyle(i % 12);
            book.setRatingAverage(BigDecimal.valueOf(b.rating()).setScale(2));
            book.setRatingCount(b.ratings());
            book.setSoldCount(b.sold());
            book.setCreatedAt(now.minus(DemoBooks.BOOKS.size() - i, ChronoUnit.DAYS).minus(b.year() >= 2025 ? 0 : 60, ChronoUnit.DAYS));
            for (String name : b.authors().split(";")) {
                String n = name.trim();
                book.getAuthors().add(auths.computeIfAbsent(n, k -> authors.save(new Author(k, Text.slug(k), null))));
            }
            book.refreshSearchText();
            byTitle.put(b.title(), books.save(book));
            i++;
        }

        int sort = 0;
        for (DemoBooks.CatalogSeed c : DemoBooks.CATALOGS) {
            CuratedCatalog catalog = new CuratedCatalog(c.name(), c.slug(), c.description(), c.highlight(), sort++);
            for (String title : c.titles()) {
                Book b = byTitle.get(title);
                if (b != null) {
                    catalog.getBooks().add(b);
                }
            }
            catalogs.save(catalog);
        }

        coupons.save(new Coupon("BEMVINDO10", "10% de desconto na sua compra", Coupon.Type.PERCENT, Money.of("10.00"), Money.ZERO, null));
        coupons.save(new Coupon("LEIA20", "R$ 20 de desconto acima de R$ 150", Coupon.Type.FIXED, Money.of("20.00"), Money.of("150.00"), null));
        coupons.save(new Coupon("CLASSICOS15", "15% de desconto acima de R$ 100", Coupon.Type.PERCENT, Money.of("15.00"), Money.of("100.00"), null));

        users.save(new AppUser("admin@" + domain, hash, "Equipe Prelo", UserRole.ADMIN));
        AppUser marina = users.save(new AppUser("leitora@" + domain, hash, "Marina Duarte", UserRole.CUSTOMER));
        AppUser rafael = users.save(new AppUser("rafael@" + domain, hash, "Rafael Nunes", UserRole.CUSTOMER));
        AppUser beatriz = users.save(new AppUser("beatriz@" + domain, hash, "Beatriz Lima", UserRole.CUSTOMER));

        Address casa = address(marina, "Casa", "Marina Duarte", "20040-000", "Rua dos Livreiros", "42", "Apto 301", "Centro", "Rio de Janeiro", "RJ", true);
        address(marina, "Trabalho", "Marina Duarte", "22250-040", "Avenida das Letras", "1500", "Sala 801", "Botafogo", "Rio de Janeiro", "RJ", false);
        Address rafaelCasa = address(rafael, "Casa", "Rafael Nunes", "01310-000", "Rua do Prelo", "88", null, "Bela Vista", "São Paulo", "SP", true);
        Address biaCasa = address(beatriz, "Casa", "Beatriz Lima", "30130-000", "Rua das Páginas", "315", "Casa 2", "Centro", "Belo Horizonte", "MG", true);

        Instant base = now.truncatedTo(ChronoUnit.MINUTES);
        demoOrder(marina, casa, "PED-DEMO1-ENTRG", base.minus(18, ChronoUnit.DAYS), ShipmentStatus.DELIVERED, PaymentMethod.PIX,
                List.of(byTitle.get("Dom Casmurro"), byTitle.get("O Alienista")), List.of(1, 1), "ECONOMICA");
        demoOrder(marina, casa, "PED-DEMO2-TRANS", base.minus(3, ChronoUnit.DAYS), ShipmentStatus.IN_TRANSIT, PaymentMethod.CREDIT_CARD,
                List.of(byTitle.get("Java Moderno na Prática"), byTitle.get("Redis em Produção")), List.of(1, 1), "EXPRESSA");
        demoOrder(marina, casa, "PED-DEMO3-BOLET", base.minus(2, ChronoUnit.HOURS), null, PaymentMethod.BOLETO,
                List.of(byTitle.get("Orgulho e Preconceito")), List.of(1), "RETIRADA");
        demoOrder(rafael, rafaelCasa, "PED-DEMO4-SEPAR", base.minus(1, ChronoUnit.DAYS), ShipmentStatus.PREPARING, PaymentMethod.CREDIT_CARD,
                List.of(byTitle.get("O Último Bibliotecário"), byTitle.get("As Cartas do Farol"), byTitle.get("O Mapa das Marés")),
                List.of(1, 1, 1), "ECONOMICA");
        demoOrder(beatriz, biaCasa, "PED-DEMO5-ENTRG", base.minus(9, ChronoUnit.DAYS), ShipmentStatus.DELIVERED, PaymentMethod.CREDIT_CARD,
                List.of(byTitle.get("A Baleia que Queria Voar"), byTitle.get("Alice no País das Maravilhas")), List.of(2, 1), "EXPRESSA");
        demoOrder(beatriz, biaCasa, "PED-DEMO6-SAIUE", base.minus(5, ChronoUnit.DAYS), ShipmentStatus.OUT_FOR_DELIVERY, PaymentMethod.PIX,
                List.of(byTitle.get("Crime e Castigo")), List.of(1), "ECONOMICA");
        log.info("Dados de demonstração criados: {} livros, {} catálogos, 4 contas (domínio {}).", DemoBooks.BOOKS.size(),
                DemoBooks.CATALOGS.size(), domain);
    }

    private Address address(AppUser user, String label, String recipient, String zip, String street, String number,
            String complement, String district, String city, String state, boolean isDefault) {
        Address a = new Address(user.getId());
        a.setLabel(label);
        a.setRecipient(recipient);
        a.setZipCode(zip);
        a.setStreet(street);
        a.setStreetNumber(number);
        a.setComplement(complement);
        a.setDistrict(district);
        a.setCity(city);
        a.setState(state);
        a.setDefault(isDefault);
        return addresses.save(a);
    }

    private void demoOrder(AppUser user, Address address, String code, Instant placedAt, ShipmentStatus shipmentStatus,
            PaymentMethod method, List<Book> items, List<Integer> quantities, String shippingCode) {
        ShippingMethod sm = shippingMethods.findByCode(shippingCode).orElseThrow();
        PurchaseOrder o = new PurchaseOrder(code, user.getId(), ShippingAddress.of(address), placedAt,
                placedAt.plus(method == PaymentMethod.BOLETO ? Duration.ofDays(4) : Duration.ofHours(1)));
        BigDecimal subtotal = Money.ZERO;
        int count = 0;
        for (int k = 0; k < items.size(); k++) {
            Book b = items.get(k);
            int q = quantities.get(k);
            BigDecimal line = Money.scale(b.getPrice().multiply(BigDecimal.valueOf(q)));
            subtotal = subtotal.add(line);
            count += q;
            o.getItems().add(new OrderItem(o, b.getId(), b.getSlug(), b.getTitle(),
                    String.join(", ", b.getAuthors().stream().map(Author::getName).toList()), b.getIsbn(), b.getCoverStyle(),
                    b.getPrice(), q, line));
        }
        BigDecimal fee = sm.getFreeAbove() != null && subtotal.compareTo(sm.getFreeAbove()) >= 0 ? Money.ZERO
                : Money.scale(sm.getBaseFee().add(sm.getFeePerKg()));
        o.setSubtotal(Money.scale(subtotal));
        o.setDiscount(Money.ZERO);
        o.setShippingFee(fee);
        o.setTotal(Money.scale(subtotal.add(fee)));
        o.setItemCount(count);
        o.setShipping(sm.getCode(), sm.getName(), sm.getCarrier(), sm.getMinDays(), sm.getMaxDays());
        orders.save(o);

        Payment p = new Payment(o, method, o.getTotal(), gateway.newReference(), "demo:" + code);
        p.setCreatedAt(placedAt.plusSeconds(40));
        if (method == PaymentMethod.CREDIT_CARD) {
            p.setCardBrand("VISA");
            p.setCardLast4("1111");
            p.setInstallments(o.getTotal().compareTo(new BigDecimal("150")) > 0 ? 3 : 1);
        } else if (method == PaymentMethod.PIX) {
            p.setExpiresAt(placedAt.plus(Duration.ofMinutes(30)));
            p.setPixPayload(gateway.pixPayload(p.getGatewayReference(), p.getAmount(), p.getExpiresAt()));
        } else {
            p.setExpiresAt(placedAt.plus(Duration.ofDays(4)));
            p.setBoletoLine(gateway.boletoLine(p.getGatewayReference(), p.getAmount(),
                    placedAt.plus(Duration.ofDays(3)).atZone(java.time.ZoneId.of("America/Sao_Paulo")).toLocalDate()));
        }
        if (shipmentStatus == null) {
            payments.save(p);
            return;
        }
        Instant paidAt = placedAt.plus(Duration.ofMinutes(method == PaymentMethod.PIX ? 3 : 1));
        p.setStatus(PaymentStatus.APPROVED);
        p.setConfirmedAt(paidAt);
        payments.save(p);
        o.setStatus(OrderStatus.PAID);
        o.setPaidAt(paidAt);

        Shipment s = new Shipment(o, sm.getCarrier(), sm.getName(),
                paidAt.plus(sm.getMaxDays() + 1L, ChronoUnit.DAYS).atZone(java.time.ZoneId.of("America/Sao_Paulo")).toLocalDate());
        s.setCreatedAt(paidAt);
        s.addEvent(ShipmentStatus.PREPARING, "Pagamento confirmado", "Recebemos o pagamento do pedido " + code + ".", null, paidAt);
        s.addEvent(ShipmentStatus.PREPARING, "Pedido em separação", "Os livros estão sendo separados e embalados.", HUB,
                paidAt.plus(2, ChronoUnit.HOURS));
        String city = address.getCity() + ", " + address.getState();
        if (shipmentStatus.ordinal() >= ShipmentStatus.IN_TRANSIT.ordinal()) {
            Instant shipped = paidAt.plus(20, ChronoUnit.HOURS);
            String tracking = "LV" + String.format("%09d", Math.abs(code.hashCode()) % 1_000_000_000) + "BR";
            s.setTrackingCode(tracking);
            s.setShippedAt(shipped);
            o.setStatus(OrderStatus.SHIPPED);
            s.addEvent(ShipmentStatus.IN_TRANSIT, "Pedido enviado", "Objeto postado com o código " + tracking + ".", HUB, shipped);
            s.addEvent(ShipmentStatus.IN_TRANSIT, "Em trânsito", "A caminho da unidade de distribuição da sua cidade.",
                    "Unidade de tratamento · " + city, shipped.plus(18, ChronoUnit.HOURS));
        }
        if (shipmentStatus.ordinal() >= ShipmentStatus.OUT_FOR_DELIVERY.ordinal()) {
            Instant out = paidAt.plus(3, ChronoUnit.DAYS);
            s.addEvent(ShipmentStatus.OUT_FOR_DELIVERY, "Saiu para entrega", "O entregador está a caminho do seu endereço.", city, out);
        }
        if (shipmentStatus == ShipmentStatus.DELIVERED) {
            Instant delivered = paidAt.plus(3, ChronoUnit.DAYS).plus(5, ChronoUnit.HOURS);
            s.setDeliveredAt(delivered);
            o.setStatus(OrderStatus.DELIVERED);
            s.addEvent(ShipmentStatus.DELIVERED, "Pedido entregue", "Entregue para " + address.getRecipient() + ". Boa leitura!", city,
                    delivered);
        }
        shipments.save(s);
    }

    /** ISBN-13 fictício com dígito verificador correto (prefixo 978-65-99999). */
    static String isbn(int index) {
        String body = "9786599999" + String.format("%02d", index);
        int sum = 0;
        for (int k = 0; k < 12; k++) {
            sum += (body.charAt(k) - '0') * (k % 2 == 0 ? 1 : 3);
        }
        int check = (10 - (sum % 10)) % 10;
        return "978-65-99999-" + body.substring(10) + "-" + check;
    }
}
