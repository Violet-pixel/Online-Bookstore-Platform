-- Prelo · estrutura inicial do banco (SQL Server 2019+)
-- Gerado a partir da mesma definição usada nos outros três bancos: tabelas, colunas e nomes de
-- restrições são idênticos em PostgreSQL, MySQL, SQL Server e Oracle.
-- Booleanos ficam como 0/1 numérico; datas e horas são gravadas em UTC.

-- Contas de clientes e da equipe
CREATE TABLE app_user (
    id BIGINT IDENTITY(1,1) NOT NULL,
    email NVARCHAR(160) NOT NULL,
    password_hash NVARCHAR(100) NOT NULL,
    full_name NVARCHAR(120) NOT NULL,
    phone NVARCHAR(20),
    user_role NVARCHAR(20) NOT NULL,
    active SMALLINT NOT NULL,
    created_at DATETIMEOFFSET(6) NOT NULL,
    last_login_at DATETIMEOFFSET(6),
    CONSTRAINT pk_app_user PRIMARY KEY (id),
    CONSTRAINT uk_app_user_email UNIQUE (email)
);

-- Endereços de entrega de cada conta
CREATE TABLE address (
    id BIGINT IDENTITY(1,1) NOT NULL,
    user_id BIGINT NOT NULL,
    label NVARCHAR(40) NOT NULL,
    recipient NVARCHAR(120) NOT NULL,
    zip_code NVARCHAR(9) NOT NULL,
    street NVARCHAR(160) NOT NULL,
    street_number NVARCHAR(20) NOT NULL,
    complement NVARCHAR(80),
    district NVARCHAR(80) NOT NULL,
    city NVARCHAR(80) NOT NULL,
    state NVARCHAR(2) NOT NULL,
    is_default SMALLINT NOT NULL,
    created_at DATETIMEOFFSET(6) NOT NULL,
    CONSTRAINT pk_address PRIMARY KEY (id),
    CONSTRAINT fk_address_user FOREIGN KEY (user_id) REFERENCES app_user (id)
);
CREATE INDEX ix_address_user_id ON address (user_id);

-- Categorias do catálogo
CREATE TABLE category (
    id BIGINT IDENTITY(1,1) NOT NULL,
    name NVARCHAR(80) NOT NULL,
    slug NVARCHAR(90) NOT NULL,
    description NVARCHAR(400),
    accent NVARCHAR(7) NOT NULL,
    sort_order INT NOT NULL,
    active SMALLINT NOT NULL,
    CONSTRAINT pk_category PRIMARY KEY (id),
    CONSTRAINT uk_category_name UNIQUE (name),
    CONSTRAINT uk_category_slug UNIQUE (slug)
);

-- Autores
CREATE TABLE author (
    id BIGINT IDENTITY(1,1) NOT NULL,
    name NVARCHAR(120) NOT NULL,
    slug NVARCHAR(130) NOT NULL,
    bio NVARCHAR(1000),
    CONSTRAINT pk_author PRIMARY KEY (id),
    CONSTRAINT uk_author_slug UNIQUE (slug)
);

-- Editoras
CREATE TABLE publisher (
    id BIGINT IDENTITY(1,1) NOT NULL,
    name NVARCHAR(120) NOT NULL,
    slug NVARCHAR(130) NOT NULL,
    CONSTRAINT pk_publisher PRIMARY KEY (id),
    CONSTRAINT uk_publisher_name UNIQUE (name),
    CONSTRAINT uk_publisher_slug UNIQUE (slug)
);

-- Livros à venda
CREATE TABLE book (
    id BIGINT IDENTITY(1,1) NOT NULL,
    isbn NVARCHAR(17) NOT NULL,
    title NVARCHAR(200) NOT NULL,
    subtitle NVARCHAR(200),
    slug NVARCHAR(220) NOT NULL,
    synopsis NVARCHAR(4000),
    category_id BIGINT NOT NULL,
    publisher_id BIGINT NOT NULL,
    language_code NVARCHAR(5) NOT NULL,
    page_count INT,
    published_year INT,
    book_format NVARCHAR(20) NOT NULL,
    price DECIMAL(12,2) NOT NULL,
    list_price DECIMAL(12,2) NOT NULL,
    stock_quantity INT NOT NULL,
    weight_grams INT NOT NULL,
    cover_style INT NOT NULL,
    rating_average DECIMAL(3,2) NOT NULL,
    rating_count INT NOT NULL,
    sold_count INT NOT NULL,
    active SMALLINT NOT NULL,
    search_text NVARCHAR(1000) NOT NULL,
    created_at DATETIMEOFFSET(6) NOT NULL,
    updated_at DATETIMEOFFSET(6) NOT NULL,
    CONSTRAINT pk_book PRIMARY KEY (id),
    CONSTRAINT uk_book_isbn UNIQUE (isbn),
    CONSTRAINT uk_book_slug UNIQUE (slug),
    CONSTRAINT fk_book_category FOREIGN KEY (category_id) REFERENCES category (id),
    CONSTRAINT fk_book_publisher FOREIGN KEY (publisher_id) REFERENCES publisher (id)
);
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
);
CREATE INDEX ix_book_author_author_id ON book_author (author_id);

-- Catálogos curados (vitrines)
CREATE TABLE curated_catalog (
    id BIGINT IDENTITY(1,1) NOT NULL,
    name NVARCHAR(80) NOT NULL,
    slug NVARCHAR(90) NOT NULL,
    description NVARCHAR(400),
    highlight NVARCHAR(160),
    sort_order INT NOT NULL,
    active SMALLINT NOT NULL,
    CONSTRAINT pk_catalog PRIMARY KEY (id),
    CONSTRAINT uk_catalog_slug UNIQUE (slug)
);

-- Livros de cada catálogo, em ordem
CREATE TABLE curated_catalog_book (
    catalog_id BIGINT NOT NULL,
    book_id BIGINT NOT NULL,
    item_position INT NOT NULL,
    CONSTRAINT pk_catalog_book PRIMARY KEY (catalog_id, item_position),
    CONSTRAINT fk_catalog_book_catalog FOREIGN KEY (catalog_id) REFERENCES curated_catalog (id) ON DELETE CASCADE,
    CONSTRAINT fk_catalog_book_book FOREIGN KEY (book_id) REFERENCES book (id)
);
CREATE INDEX ix_catalog_book_book_id ON curated_catalog_book (book_id);

-- Carrinho de cada conta
CREATE TABLE cart (
    id BIGINT IDENTITY(1,1) NOT NULL,
    user_id BIGINT NOT NULL,
    updated_at DATETIMEOFFSET(6) NOT NULL,
    CONSTRAINT pk_cart PRIMARY KEY (id),
    CONSTRAINT uk_cart_user_id UNIQUE (user_id),
    CONSTRAINT fk_cart_user FOREIGN KEY (user_id) REFERENCES app_user (id)
);

-- Itens do carrinho
CREATE TABLE cart_item (
    id BIGINT IDENTITY(1,1) NOT NULL,
    cart_id BIGINT NOT NULL,
    book_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    added_at DATETIMEOFFSET(6) NOT NULL,
    CONSTRAINT pk_cart_item PRIMARY KEY (id),
    CONSTRAINT uk_cart_item_cart_id_book_id UNIQUE (cart_id, book_id),
    CONSTRAINT fk_cart_item_cart FOREIGN KEY (cart_id) REFERENCES cart (id) ON DELETE CASCADE,
    CONSTRAINT fk_cart_item_book FOREIGN KEY (book_id) REFERENCES book (id)
);
CREATE INDEX ix_cart_item_book_id ON cart_item (book_id);

-- Formas de envio e tabela de frete
CREATE TABLE shipping_method (
    id BIGINT IDENTITY(1,1) NOT NULL,
    code NVARCHAR(20) NOT NULL,
    name NVARCHAR(60) NOT NULL,
    carrier NVARCHAR(60) NOT NULL,
    description NVARCHAR(200),
    base_fee DECIMAL(12,2) NOT NULL,
    fee_per_kg DECIMAL(12,2) NOT NULL,
    free_above DECIMAL(12,2),
    min_days INT NOT NULL,
    max_days INT NOT NULL,
    sort_order INT NOT NULL,
    active SMALLINT NOT NULL,
    CONSTRAINT pk_shipping_method PRIMARY KEY (id),
    CONSTRAINT uk_shipping_method_code UNIQUE (code)
);

-- Cupons de desconto
CREATE TABLE coupon (
    id BIGINT IDENTITY(1,1) NOT NULL,
    code NVARCHAR(30) NOT NULL,
    description NVARCHAR(160) NOT NULL,
    discount_type NVARCHAR(10) NOT NULL,
    discount_value DECIMAL(12,2) NOT NULL,
    min_subtotal DECIMAL(12,2) NOT NULL,
    valid_until DATETIMEOFFSET(6),
    active SMALLINT NOT NULL,
    CONSTRAINT pk_coupon PRIMARY KEY (id),
    CONSTRAINT uk_coupon_code UNIQUE (code)
);

-- Pedidos (com cópia do endereço e do frete)
CREATE TABLE purchase_order (
    id BIGINT IDENTITY(1,1) NOT NULL,
    code NVARCHAR(20) NOT NULL,
    user_id BIGINT NOT NULL,
    order_status NVARCHAR(20) NOT NULL,
    subtotal DECIMAL(12,2) NOT NULL,
    discount DECIMAL(12,2) NOT NULL,
    shipping_fee DECIMAL(12,2) NOT NULL,
    total DECIMAL(12,2) NOT NULL,
    coupon_code NVARCHAR(30),
    item_count INT NOT NULL,
    ship_recipient NVARCHAR(120) NOT NULL,
    ship_zip_code NVARCHAR(9) NOT NULL,
    ship_street NVARCHAR(160) NOT NULL,
    ship_number NVARCHAR(20) NOT NULL,
    ship_complement NVARCHAR(80),
    ship_district NVARCHAR(80) NOT NULL,
    ship_city NVARCHAR(80) NOT NULL,
    ship_state NVARCHAR(2) NOT NULL,
    shipping_method_code NVARCHAR(20) NOT NULL,
    shipping_method_name NVARCHAR(60) NOT NULL,
    shipping_carrier NVARCHAR(60) NOT NULL,
    shipping_min_days INT NOT NULL,
    shipping_max_days INT NOT NULL,
    placed_at DATETIMEOFFSET(6) NOT NULL,
    payment_due_at DATETIMEOFFSET(6) NOT NULL,
    paid_at DATETIMEOFFSET(6),
    canceled_at DATETIMEOFFSET(6),
    cancel_reason NVARCHAR(200),
    version BIGINT NOT NULL,
    CONSTRAINT pk_order PRIMARY KEY (id),
    CONSTRAINT uk_order_code UNIQUE (code),
    CONSTRAINT fk_order_user FOREIGN KEY (user_id) REFERENCES app_user (id)
);
CREATE INDEX ix_order_user_id_placed_at ON purchase_order (user_id, placed_at);
CREATE INDEX ix_order_order_status_payment_due_at ON purchase_order (order_status, payment_due_at);
CREATE INDEX ix_order_placed_at ON purchase_order (placed_at);
CREATE INDEX ix_order_paid_at ON purchase_order (paid_at);

-- Itens do pedido (cópia do livro e do preço)
CREATE TABLE order_item (
    id BIGINT IDENTITY(1,1) NOT NULL,
    order_id BIGINT NOT NULL,
    book_id BIGINT NOT NULL,
    book_slug NVARCHAR(220) NOT NULL,
    book_title NVARCHAR(200) NOT NULL,
    book_authors NVARCHAR(300),
    book_isbn NVARCHAR(17) NOT NULL,
    cover_style INT NOT NULL,
    unit_price DECIMAL(12,2) NOT NULL,
    quantity INT NOT NULL,
    line_total DECIMAL(12,2) NOT NULL,
    CONSTRAINT pk_order_item PRIMARY KEY (id),
    CONSTRAINT fk_order_item_order FOREIGN KEY (order_id) REFERENCES purchase_order (id) ON DELETE CASCADE,
    CONSTRAINT fk_order_item_book FOREIGN KEY (book_id) REFERENCES book (id)
);
CREATE INDEX ix_order_item_order_id ON order_item (order_id);
CREATE INDEX ix_order_item_book_id ON order_item (book_id);

-- Tentativas de pagamento (sem número de cartão)
CREATE TABLE payment (
    id BIGINT IDENTITY(1,1) NOT NULL,
    order_id BIGINT NOT NULL,
    payment_method NVARCHAR(20) NOT NULL,
    payment_status NVARCHAR(20) NOT NULL,
    amount DECIMAL(12,2) NOT NULL,
    installments INT NOT NULL,
    card_brand NVARCHAR(20),
    card_last4 NVARCHAR(4),
    pix_payload NVARCHAR(400),
    boleto_line NVARCHAR(60),
    gateway_reference NVARCHAR(40) NOT NULL,
    idempotency_key NVARCHAR(80) NOT NULL,
    failure_reason NVARCHAR(200),
    expires_at DATETIMEOFFSET(6),
    created_at DATETIMEOFFSET(6) NOT NULL,
    confirmed_at DATETIMEOFFSET(6),
    CONSTRAINT pk_payment PRIMARY KEY (id),
    CONSTRAINT uk_payment_gateway_reference UNIQUE (gateway_reference),
    CONSTRAINT uk_payment_idempotency_key UNIQUE (idempotency_key),
    CONSTRAINT fk_payment_order FOREIGN KEY (order_id) REFERENCES purchase_order (id)
);
CREATE INDEX ix_payment_order_id_payment_status ON payment (order_id, payment_status);

-- Envio de cada pedido pago
CREATE TABLE shipment (
    id BIGINT IDENTITY(1,1) NOT NULL,
    order_id BIGINT NOT NULL,
    carrier NVARCHAR(60) NOT NULL,
    service_name NVARCHAR(60) NOT NULL,
    tracking_code NVARCHAR(30),
    shipment_status NVARCHAR(20) NOT NULL,
    estimated_delivery DATE,
    shipped_at DATETIMEOFFSET(6),
    delivered_at DATETIMEOFFSET(6),
    created_at DATETIMEOFFSET(6) NOT NULL,
    updated_at DATETIMEOFFSET(6) NOT NULL,
    CONSTRAINT pk_shipment PRIMARY KEY (id),
    CONSTRAINT uk_shipment_order_id UNIQUE (order_id),
    CONSTRAINT fk_shipment_order FOREIGN KEY (order_id) REFERENCES purchase_order (id)
);
CREATE INDEX ix_shipment_shipment_status ON shipment (shipment_status);
CREATE INDEX ix_shipment_tracking_code ON shipment (tracking_code);

-- Linha do tempo do rastreio
CREATE TABLE shipment_event (
    id BIGINT IDENTITY(1,1) NOT NULL,
    shipment_id BIGINT NOT NULL,
    shipment_status NVARCHAR(20) NOT NULL,
    title NVARCHAR(120) NOT NULL,
    detail NVARCHAR(300),
    location NVARCHAR(120),
    occurred_at DATETIMEOFFSET(6) NOT NULL,
    CONSTRAINT pk_shp_event PRIMARY KEY (id),
    CONSTRAINT fk_shp_event_shipment FOREIGN KEY (shipment_id) REFERENCES shipment (id) ON DELETE CASCADE
);
CREATE INDEX ix_shp_event_shipment_id_occurred_at ON shipment_event (shipment_id, occurred_at);
