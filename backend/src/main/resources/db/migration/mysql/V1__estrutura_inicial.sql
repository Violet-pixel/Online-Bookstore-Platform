-- Prelo · estrutura inicial do banco (MySQL 8.0+)
-- Gerado a partir da mesma definição usada nos outros três bancos: tabelas, colunas e nomes de
-- restrições são idênticos em PostgreSQL, MySQL, SQL Server e Oracle.
-- Booleanos ficam como 0/1 numérico; datas e horas são gravadas em UTC.

-- Contas de clientes e da equipe
CREATE TABLE app_user (
    id BIGINT AUTO_INCREMENT NOT NULL,
    email VARCHAR(160) NOT NULL,
    password_hash VARCHAR(100) NOT NULL,
    full_name VARCHAR(120) NOT NULL,
    phone VARCHAR(20),
    user_role VARCHAR(20) NOT NULL,
    active SMALLINT NOT NULL,
    created_at DATETIME(6) NOT NULL,
    last_login_at DATETIME(6),
    CONSTRAINT pk_app_user PRIMARY KEY (id),
    CONSTRAINT uk_app_user_email UNIQUE (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Endereços de entrega de cada conta
CREATE TABLE address (
    id BIGINT AUTO_INCREMENT NOT NULL,
    user_id BIGINT NOT NULL,
    label VARCHAR(40) NOT NULL,
    recipient VARCHAR(120) NOT NULL,
    zip_code VARCHAR(9) NOT NULL,
    street VARCHAR(160) NOT NULL,
    street_number VARCHAR(20) NOT NULL,
    complement VARCHAR(80),
    district VARCHAR(80) NOT NULL,
    city VARCHAR(80) NOT NULL,
    state VARCHAR(2) NOT NULL,
    is_default SMALLINT NOT NULL,
    created_at DATETIME(6) NOT NULL,
    CONSTRAINT pk_address PRIMARY KEY (id),
    CONSTRAINT fk_address_user FOREIGN KEY (user_id) REFERENCES app_user (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE INDEX ix_address_user_id ON address (user_id);

-- Categorias do catálogo
CREATE TABLE category (
    id BIGINT AUTO_INCREMENT NOT NULL,
    name VARCHAR(80) NOT NULL,
    slug VARCHAR(90) NOT NULL,
    description VARCHAR(400),
    accent VARCHAR(7) NOT NULL,
    sort_order INT NOT NULL,
    active SMALLINT NOT NULL,
    CONSTRAINT pk_category PRIMARY KEY (id),
    CONSTRAINT uk_category_name UNIQUE (name),
    CONSTRAINT uk_category_slug UNIQUE (slug)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Autores
CREATE TABLE author (
    id BIGINT AUTO_INCREMENT NOT NULL,
    name VARCHAR(120) NOT NULL,
    slug VARCHAR(130) NOT NULL,
    bio VARCHAR(1000),
    CONSTRAINT pk_author PRIMARY KEY (id),
    CONSTRAINT uk_author_slug UNIQUE (slug)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Editoras
CREATE TABLE publisher (
    id BIGINT AUTO_INCREMENT NOT NULL,
    name VARCHAR(120) NOT NULL,
    slug VARCHAR(130) NOT NULL,
    CONSTRAINT pk_publisher PRIMARY KEY (id),
    CONSTRAINT uk_publisher_name UNIQUE (name),
    CONSTRAINT uk_publisher_slug UNIQUE (slug)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Livros à venda
CREATE TABLE book (
    id BIGINT AUTO_INCREMENT NOT NULL,
    isbn VARCHAR(17) NOT NULL,
    title VARCHAR(200) NOT NULL,
    subtitle VARCHAR(200),
    slug VARCHAR(220) NOT NULL,
    synopsis VARCHAR(4000),
    category_id BIGINT NOT NULL,
    publisher_id BIGINT NOT NULL,
    language_code VARCHAR(5) NOT NULL,
    page_count INT,
    published_year INT,
    book_format VARCHAR(20) NOT NULL,
    price DECIMAL(12,2) NOT NULL,
    list_price DECIMAL(12,2) NOT NULL,
    stock_quantity INT NOT NULL,
    weight_grams INT NOT NULL,
    cover_style INT NOT NULL,
    rating_average DECIMAL(3,2) NOT NULL,
    rating_count INT NOT NULL,
    sold_count INT NOT NULL,
    active SMALLINT NOT NULL,
    search_text VARCHAR(1000) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    CONSTRAINT pk_book PRIMARY KEY (id),
    CONSTRAINT uk_book_isbn UNIQUE (isbn),
    CONSTRAINT uk_book_slug UNIQUE (slug),
    CONSTRAINT fk_book_category FOREIGN KEY (category_id) REFERENCES category (id),
    CONSTRAINT fk_book_publisher FOREIGN KEY (publisher_id) REFERENCES publisher (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE INDEX ix_book_category_id ON book (category_id);
CREATE INDEX ix_book_publisher_id ON book (publisher_id);
CREATE INDEX ix_book_active_sold_count ON book (active, sold_count);
CREATE INDEX ix_book_created_at ON book (created_at);
CREATE INDEX ix_book_price ON book (price);

-- Autores de cada livro, em ordem
CREATE TABLE book_author (
    book_id BIGINT NOT NULL,
    author_id BIGINT NOT NULL,
    author_order INT NOT NULL,
    CONSTRAINT pk_book_author PRIMARY KEY (book_id, author_order),
    CONSTRAINT fk_book_author_book FOREIGN KEY (book_id) REFERENCES book (id) ON DELETE CASCADE,
    CONSTRAINT fk_book_author_author FOREIGN KEY (author_id) REFERENCES author (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE INDEX ix_book_author_author_id ON book_author (author_id);

-- Catálogos curados (vitrines)
CREATE TABLE curated_catalog (
    id BIGINT AUTO_INCREMENT NOT NULL,
    name VARCHAR(80) NOT NULL,
    slug VARCHAR(90) NOT NULL,
    description VARCHAR(400),
    highlight VARCHAR(160),
    sort_order INT NOT NULL,
    active SMALLINT NOT NULL,
    CONSTRAINT pk_catalog PRIMARY KEY (id),
    CONSTRAINT uk_catalog_slug UNIQUE (slug)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Livros de cada catálogo, em ordem
CREATE TABLE curated_catalog_book (
    catalog_id BIGINT NOT NULL,
    book_id BIGINT NOT NULL,
    item_position INT NOT NULL,
    CONSTRAINT pk_catalog_book PRIMARY KEY (catalog_id, item_position),
    CONSTRAINT fk_catalog_book_catalog FOREIGN KEY (catalog_id) REFERENCES curated_catalog (id) ON DELETE CASCADE,
    CONSTRAINT fk_catalog_book_book FOREIGN KEY (book_id) REFERENCES book (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE INDEX ix_catalog_book_book_id ON curated_catalog_book (book_id);

-- Carrinho de cada conta
CREATE TABLE cart (
    id BIGINT AUTO_INCREMENT NOT NULL,
    user_id BIGINT NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    CONSTRAINT pk_cart PRIMARY KEY (id),
    CONSTRAINT uk_cart_user_id UNIQUE (user_id),
    CONSTRAINT fk_cart_user FOREIGN KEY (user_id) REFERENCES app_user (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Itens do carrinho
CREATE TABLE cart_item (
    id BIGINT AUTO_INCREMENT NOT NULL,
    cart_id BIGINT NOT NULL,
    book_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    added_at DATETIME(6) NOT NULL,
    CONSTRAINT pk_cart_item PRIMARY KEY (id),
    CONSTRAINT uk_cart_item_cart_id_book_id UNIQUE (cart_id, book_id),
    CONSTRAINT fk_cart_item_cart FOREIGN KEY (cart_id) REFERENCES cart (id) ON DELETE CASCADE,
    CONSTRAINT fk_cart_item_book FOREIGN KEY (book_id) REFERENCES book (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE INDEX ix_cart_item_book_id ON cart_item (book_id);

-- Formas de envio e tabela de frete
CREATE TABLE shipping_method (
    id BIGINT AUTO_INCREMENT NOT NULL,
    code VARCHAR(20) NOT NULL,
    name VARCHAR(60) NOT NULL,
    carrier VARCHAR(60) NOT NULL,
    description VARCHAR(200),
    base_fee DECIMAL(12,2) NOT NULL,
    fee_per_kg DECIMAL(12,2) NOT NULL,
    free_above DECIMAL(12,2),
    min_days INT NOT NULL,
    max_days INT NOT NULL,
    sort_order INT NOT NULL,
    active SMALLINT NOT NULL,
    CONSTRAINT pk_shipping_method PRIMARY KEY (id),
    CONSTRAINT uk_shipping_method_code UNIQUE (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Cupons de desconto
CREATE TABLE coupon (
    id BIGINT AUTO_INCREMENT NOT NULL,
    code VARCHAR(30) NOT NULL,
    description VARCHAR(160) NOT NULL,
    discount_type VARCHAR(10) NOT NULL,
    discount_value DECIMAL(12,2) NOT NULL,
    min_subtotal DECIMAL(12,2) NOT NULL,
    valid_until DATETIME(6),
    active SMALLINT NOT NULL,
    CONSTRAINT pk_coupon PRIMARY KEY (id),
    CONSTRAINT uk_coupon_code UNIQUE (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Pedidos (com cópia do endereço e do frete)
CREATE TABLE purchase_order (
    id BIGINT AUTO_INCREMENT NOT NULL,
    code VARCHAR(20) NOT NULL,
    user_id BIGINT NOT NULL,
    order_status VARCHAR(20) NOT NULL,
    subtotal DECIMAL(12,2) NOT NULL,
    discount DECIMAL(12,2) NOT NULL,
    shipping_fee DECIMAL(12,2) NOT NULL,
    total DECIMAL(12,2) NOT NULL,
    coupon_code VARCHAR(30),
    item_count INT NOT NULL,
    ship_recipient VARCHAR(120) NOT NULL,
    ship_zip_code VARCHAR(9) NOT NULL,
    ship_street VARCHAR(160) NOT NULL,
    ship_number VARCHAR(20) NOT NULL,
    ship_complement VARCHAR(80),
    ship_district VARCHAR(80) NOT NULL,
    ship_city VARCHAR(80) NOT NULL,
    ship_state VARCHAR(2) NOT NULL,
    shipping_method_code VARCHAR(20) NOT NULL,
    shipping_method_name VARCHAR(60) NOT NULL,
    shipping_carrier VARCHAR(60) NOT NULL,
    shipping_min_days INT NOT NULL,
    shipping_max_days INT NOT NULL,
    placed_at DATETIME(6) NOT NULL,
    payment_due_at DATETIME(6) NOT NULL,
    paid_at DATETIME(6),
    canceled_at DATETIME(6),
    cancel_reason VARCHAR(200),
    version BIGINT NOT NULL,
    CONSTRAINT pk_order PRIMARY KEY (id),
    CONSTRAINT uk_order_code UNIQUE (code),
    CONSTRAINT fk_order_user FOREIGN KEY (user_id) REFERENCES app_user (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE INDEX ix_order_user_id_placed_at ON purchase_order (user_id, placed_at);
CREATE INDEX ix_order_order_status_payment_due_at ON purchase_order (order_status, payment_due_at);
CREATE INDEX ix_order_placed_at ON purchase_order (placed_at);
CREATE INDEX ix_order_paid_at ON purchase_order (paid_at);

-- Itens do pedido (cópia do livro e do preço)
CREATE TABLE order_item (
    id BIGINT AUTO_INCREMENT NOT NULL,
    order_id BIGINT NOT NULL,
    book_id BIGINT NOT NULL,
    book_slug VARCHAR(220) NOT NULL,
    book_title VARCHAR(200) NOT NULL,
    book_authors VARCHAR(300),
    book_isbn VARCHAR(17) NOT NULL,
    cover_style INT NOT NULL,
    unit_price DECIMAL(12,2) NOT NULL,
    quantity INT NOT NULL,
    line_total DECIMAL(12,2) NOT NULL,
    CONSTRAINT pk_order_item PRIMARY KEY (id),
    CONSTRAINT fk_order_item_order FOREIGN KEY (order_id) REFERENCES purchase_order (id) ON DELETE CASCADE,
    CONSTRAINT fk_order_item_book FOREIGN KEY (book_id) REFERENCES book (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE INDEX ix_order_item_order_id ON order_item (order_id);
CREATE INDEX ix_order_item_book_id ON order_item (book_id);

-- Tentativas de pagamento (sem número de cartão)
CREATE TABLE payment (
    id BIGINT AUTO_INCREMENT NOT NULL,
    order_id BIGINT NOT NULL,
    payment_method VARCHAR(20) NOT NULL,
    payment_status VARCHAR(20) NOT NULL,
    amount DECIMAL(12,2) NOT NULL,
    installments INT NOT NULL,
    card_brand VARCHAR(20),
    card_last4 VARCHAR(4),
    pix_payload VARCHAR(400),
    boleto_line VARCHAR(60),
    gateway_reference VARCHAR(40) NOT NULL,
    idempotency_key VARCHAR(80) NOT NULL,
    failure_reason VARCHAR(200),
    expires_at DATETIME(6),
    created_at DATETIME(6) NOT NULL,
    confirmed_at DATETIME(6),
    CONSTRAINT pk_payment PRIMARY KEY (id),
    CONSTRAINT uk_payment_gateway_reference UNIQUE (gateway_reference),
    CONSTRAINT uk_payment_idempotency_key UNIQUE (idempotency_key),
    CONSTRAINT fk_payment_order FOREIGN KEY (order_id) REFERENCES purchase_order (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE INDEX ix_payment_order_id_payment_status ON payment (order_id, payment_status);

-- Envio de cada pedido pago
CREATE TABLE shipment (
    id BIGINT AUTO_INCREMENT NOT NULL,
    order_id BIGINT NOT NULL,
    carrier VARCHAR(60) NOT NULL,
    service_name VARCHAR(60) NOT NULL,
    tracking_code VARCHAR(30),
    shipment_status VARCHAR(20) NOT NULL,
    estimated_delivery DATE,
    shipped_at DATETIME(6),
    delivered_at DATETIME(6),
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    CONSTRAINT pk_shipment PRIMARY KEY (id),
    CONSTRAINT uk_shipment_order_id UNIQUE (order_id),
    CONSTRAINT fk_shipment_order FOREIGN KEY (order_id) REFERENCES purchase_order (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE INDEX ix_shipment_shipment_status ON shipment (shipment_status);
CREATE INDEX ix_shipment_tracking_code ON shipment (tracking_code);

-- Linha do tempo do rastreio
CREATE TABLE shipment_event (
    id BIGINT AUTO_INCREMENT NOT NULL,
    shipment_id BIGINT NOT NULL,
    shipment_status VARCHAR(20) NOT NULL,
    title VARCHAR(120) NOT NULL,
    detail VARCHAR(300),
    location VARCHAR(120),
    occurred_at DATETIME(6) NOT NULL,
    CONSTRAINT pk_shp_event PRIMARY KEY (id),
    CONSTRAINT fk_shp_event_shipment FOREIGN KEY (shipment_id) REFERENCES shipment (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE INDEX ix_shp_event_shipment_id_occurred_at ON shipment_event (shipment_id, occurred_at);
