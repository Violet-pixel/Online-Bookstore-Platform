<p align="center">
  <img src="docs/images/logo.png" width="160" alt="Prelo">
</p>

<h1 align="center">Prelo<br><sub><sup><sup><sub>Copyright (©) 2026 Clara Portes</sub></sup></sup></sub></h1>

<p align="center">
  Livraria online com vitrine, catálogos e categorias, busca sem acentos, carrinho, checkout com frete por CEP,
  pagamento com cartão, Pix ou boleto, acompanhamento do envio e um painel para a livraria.<br>
  O mesmo código roda em PostgreSQL, MySQL, SQL Server ou Oracle.
</p>

<p align="center">
  Java 21, Spring Boot 4.1, Spring Security com JWT, JPA e Hibernate 7, Flyway, Redis 7, React 19, Vite,
  Docker Compose e JUnit. Versão 1.1.0.
</p>

<p align="center">
  <img src="docs/images/tela-vitrine.png" alt="Vitrine do Prelo com destaques, benefícios e catálogos">
</p>

## Documentação

| Arquivo | O que tem |
|---|---|
| [`Manual-Prelo.pdf`](Manual-Prelo.pdf) ([cópia em `docs/`](docs/Manual-Prelo.pdf)) | Instalação, cada tela da loja e da administração, janela de pagamento, envio, arquitetura, segurança, API, configuração, testes e solução de problemas |
| [`Banco-de-Dados-Prelo.pdf`](Banco-de-Dados-Prelo.pdf) ([cópia em `docs/`](docs/Banco-de-Dados-Prelo.pdf)) | Diagrama de relacionamentos, todas as tabelas com campos, tipos e chaves, os tipos equivalentes nos quatro bancos, máquinas de estado, regras do esquema e como preparar cada banco |

Os dois manuais são da versão 1.1.0. O HTML de onde eles saem fica em `.design/docs-src/`; para gerar de novo,
rode `python3 .design/docs-src/render.py` (precisa de Python com Playwright e pypdf).

## O que o Prelo faz

- Vitrines curadas com ordem própria (Mais vendidos, Lançamentos, Clássicos essenciais e outras) e oito
  categorias com contagem de livros. Os catálogos aparecem em trilhos que deslizam com o mouse ou o dedo.
- Busca por título, subtítulo, autor, editora e ISBN, sem diferenciar acentos nem maiúsculas. Filtros por
  categoria, formato, faixa de preço e estoque, seis ordenações e paginação.
- Página do livro com capa gerada pela própria interface, sinopse, ficha técnica, estoque, cálculo de frete
  pelo CEP e sugestões da mesma estante.
- Carrinho que funciona sem login (fica no navegador) e é somado ao da conta quando você entra. Tem gaveta
  lateral, barra de frete grátis e cupons.
- Checkout em três passos: endereço preenchido pelo CEP, forma de envio com preço e prazo por região e
  revisão com cupom. O estoque fica reservado por 60 minutos enquanto você paga.
- Janela de pagamento com cartão em até 6x sem juros, Pix com QR Code, copia e cola e contagem regressiva, ou
  boleto com linha digitável. O número do cartão vira um token de uso único e nunca é gravado.
- Minhas compras com filtros por status e, em cada pedido, barra de progresso, código de rastreio, previsão
  de entrega e linha do tempo.
- Administração com painel (receita, ticket médio, pedidos por status, estoque baixo e mais vendidos),
  pedidos e envios, livros e estoque, categorias e catálogos.
- Tema claro e escuro e layout para celular.

<p align="center">
  <img src="docs/images/tela-livro.png" alt="Página do livro com preço, estoque, cálculo de frete e ficha técnica">
</p>

### Novidades da 1.1.0

A 1.1.0 muda a aparência da loja, do painel e dos manuais. Rotas, API, banco e regras de negócio continuam
iguais aos da 1.0.0.

- Visual novo inspirado em oficina tipográfica: papel-algodão, tinta preta e as três tintas de processo
  (magenta para ações, ciano para links, amarelo para destaques), com filetes finos no lugar de caixas.
- Tipografia nova: Gloock nos títulos e Libre Franklin no texto, servidas pelo próprio projeto.
- Alguns rótulos mudaram: Ver carrinho e fechar pedido na gaveta, Fechar pedido no carrinho, Da mesma
  estante na página do livro e Catálogos na página de categorias.
- Capturas de tela e manuais em PDF refeitos.

## Ver rodando

Você só precisa do Docker com Docker Compose.

No Windows, dê dois cliques em `iniciar.cmd`. No Linux ou no macOS:

```bash
./iniciar.sh
```

Na primeira vez, o script cria o `.env` com uma senha aleatória para o banco e uma chave aleatória para o
JWT, sobe os contêineres (banco escolhido, Redis, API e interface) e abre http://localhost:8080.

| Endereço | O que é |
|---|---|
| http://localhost:8080 | Loja e administração |
| http://localhost:8080/api/docs | Documentação da API (Swagger) |
| http://localhost:8080/api/health | Saúde da API, do banco e do Redis |

### Contas de demonstração

As duas usam a senha `Prelo@2026` e existem só para testes, para qualquer pessoa entrar logo depois de baixar
o projeto. Nomes, e-mails, endereços, editoras, preços e ISBNs (prefixo 978-65-99999) são fictícios; os
clássicos citados estão em domínio público.

| Papel | E-mail | O que pode fazer |
|---|---|---|
| Cliente | `leitora@prelo.dev` | Comprar, pagar, acompanhar os pedidos e gerenciar endereços |
| Administração | `admin@prelo.dev` | Tudo, incluindo painel, pedidos, envios, livros, estoque, categorias e catálogos |

Cupons de demonstração: `BEMVINDO10` (10%), `LEIA20` (R$ 20 acima de R$ 150) e `CLASSICOS15` (15% acima
de R$ 100).

## Instalar

### Com Docker, sem o script

```bash
cp .env.example .env              # preencha DB_PASSWORD e JWT_SECRET
docker compose up -d --build
docker compose logs -f api        # acompanhar a API
```

### Escolher o banco

O banco é escolhido no `.env`. Os dois valores precisam ser iguais (os scripts corrigem sozinhos):

```bash
DB_VENDOR=oracle
COMPOSE_PROFILES=oracle
```

| `DB_VENDOR` | Versões | Migrações | Observações |
|---|---|---|---|
| `postgresql` (padrão) | 14 ou mais (CI no 16) | `db/migration/postgresql` | `TIMESTAMP WITH TIME ZONE`, `NUMERIC(12,2)` |
| `mysql` | 8.0 ou mais (CI no 8.4) | `db/migration/mysql` | InnoDB, `utf8mb4_0900_ai_ci`, `DATETIME(6)` em UTC |
| `sqlserver` | 2019 ou mais (CI no 2022) | `db/migration/sqlserver` | `NVARCHAR`, `DATETIMEOFFSET(6)` |
| `oracle` | 19c ou mais (CI no 23ai Free) | `db/migration/oracle` | `VARCHAR2(n CHAR)`, `NUMBER`, identidade `GENERATED BY DEFAULT ON NULL` |

As entidades JPA são as mesmas para os quatro. Cada banco tem a sua pasta de migrações Flyway, com tabelas,
colunas e nomes de restrições idênticos. Cada banco guarda os dados no próprio volume: trocar `DB_VENDOR`
começa uma loja nova, e voltar ao banco anterior traz os dados dele de volta.

### Usar com dados reais

1. Suba com `APP_SEED_DEMO=false` num banco vazio.
2. Se não for testar pagamentos simulados, defina `PAYMENTS_SANDBOX=false` e implemente um provedor real
   (veja [Pagamentos](#pagamentos)).
3. Crie a sua conta pela tela Criar conta e promova-a a administração direto no banco:

   ```sql
   UPDATE app_user SET user_role = 'ADMIN' WHERE email = 'voce@sualivraria.com.br';
   ```

4. Saia e entre de novo para o novo papel valer no token.

O manual traz o checklist completo para produção (HTTPS, segredos, Swagger e backup).

### Sem Docker

Para desenvolver, com Java 21, Maven 3.9, Node 22, Redis 7 e um dos quatro bancos:

```bash
# API na porta 8081 (PostgreSQL local por padrão; troque com DB_VENDOR)
cd backend
DB_PASSWORD=prelo JWT_SECRET=troque-por-uma-chave-com-32-caracteres mvn spring-boot:run

# interface em outro terminal: http://localhost:5173, com /api repassado para a porta 8081
cd frontend
npm install
npm run dev
```

## Usar

### Comandos do dia a dia

| Para | Comando |
|---|---|
| Ver os logs da API | `docker compose logs -f api` |
| Parar (os dados ficam nos volumes) | `docker compose down` |
| Atualizar depois de baixar uma versão nova | `docker compose up -d --build` |
| Abrir o terminal do banco (PostgreSQL) | `docker compose exec postgresql psql -U prelo` |
| Abrir o terminal do Redis | `docker compose exec redis redis-cli` |
| Apagar tudo, inclusive os dados | `docker compose down -v` |

### Pagamentos

O projeto vem com um sandbox: nada sai da aplicação e nenhum código gerado paga nada (o Pix usa um
identificador de sandbox e o boleto, um banco fictício).

| Cartão de teste | Resultado |
|---|---|
| `4111 1111 1111 1111`, `5555 5555 5555 4444` | Aprovado |
| `4000 0000 0000 0002` | Recusado pelo emissor |
| `4000 0000 0000 9995` | Limite insuficiente |

Qualquer nome, validade futura e CVV de 3 dígitos (4 no Amex) funcionam. Pix e boleto ganham o botão Simular
pagamento na janela de pagamento. Para um provedor real, implemente a interface `PaymentGateway`, registre-a
no lugar do `SandboxGateway` e desligue o sandbox com `PAYMENTS_SANDBOX=false`.

<p align="center">
  <img src="docs/images/tela-checkout.png" width="49%" alt="Checkout com endereço, forma de envio e resumo com cupom">
  <img src="docs/images/tela-pagamento-pix.png" width="49%" alt="Janela de pagamento com QR Code Pix e contagem regressiva">
</p>

<p align="center">
  <img src="docs/images/tela-envio.png" width="49%" alt="Detalhes do envio com barra de progresso, rastreio e linha do tempo">
  <img src="docs/images/tela-admin-painel.png" width="49%" alt="Painel da administração com receita, pedidos por status, estoque baixo e mais vendidos">
</p>

## Por dentro

```mermaid
flowchart LR
    UI["Navegador<br/>React 19 + Vite"] -- "HTTPS" --> WEB["web<br/>nginx"]
    WEB -- "/api" --> API["api<br/>Spring Boot 4.1, Java 21"]
    API -- "JPA / Hibernate" --> DB[("PostgreSQL, MySQL,<br/>SQL Server ou Oracle")]
    API -- "Flyway<br/>db/migration/{banco}" --> DB
    API -- "cache, tokens, travas" --> RS[("Redis 7")]
    API -- "PaymentGateway" --> PAY["Sandbox de pagamentos<br/>(troque por um provedor real)"]
```

| Serviço | Tecnologia | Responsabilidade |
|---|---|---|
| `web` | React 19, React Router 7, Vite, nginx | Loja, checkout, janela de pagamento, minhas compras, conta e administração. O nginx entrega a interface e repassa `/api` |
| `api` | Java 21, Spring Boot 4.1, Spring Security, Spring Data JPA, Hibernate 7, Flyway | API REST, autenticação com JWT, regras de catálogo, carrinho, checkout, pagamentos, envio e administração |
| banco | PostgreSQL 16, MySQL 8.4, SQL Server 2022 ou Oracle 23ai | Fonte da verdade: contas, endereços, catálogo, carrinhos, pedidos, pagamentos e envios |
| `redis` | Redis 7 | Cache do catálogo, tokens de renovação de sessão, tokens de cartão, limite de tentativas de login e trava das tarefas agendadas |

### Como uma compra acontece

1. O carrinho é conferido item a item e o estoque é reservado com um `UPDATE ... WHERE stock_quantity >= :qtd`
   atômico, em ordem de id. Dois clientes nunca levam o mesmo último exemplar.
2. O pedido nasce em Aguardando pagamento, com uma cópia do endereço, dos preços e do frete, e tem 60 minutos
   para ser pago.
3. Na janela de pagamento, o cartão vira um token de 10 minutos no Redis (só bandeira e 4 últimos dígitos), e
   o pagamento vai com uma `Idempotency-Key`. Clicar duas vezes não cobra duas vezes.
4. Com o pagamento aprovado, o pedido vira Pago, o envio nasce em separação e os outros códigos Pix ou boleto
   pendentes deixam de valer.
5. A livraria posta o pedido, marca a saída para entrega e a entrega. Cada etapa entra na linha do tempo do
   cliente. A cada minuto, os pedidos não pagos no prazo são cancelados e o estoque volta.

### Segurança

- Senhas com BCrypt (custo 11); login com e-mail inexistente leva o mesmo tempo que um com senha errada.
- Token de acesso JWT HS256 de 15 minutos, só na memória do navegador. A renovação usa um cookie HttpOnly,
  SameSite Strict, e o Redis guarda só o hash do token, que gira a cada uso.
- 5 senhas erradas por e-mail bloqueiam o login por 15 minutos.
- `/api/admin/**` só para `ADMIN`; pedidos e pagamentos de outra conta respondem 404.
- CSP, X-Frame-Options, nosniff e Referrer-Policy na API e no nginx.

### Estrutura

```text
backend/                  API Spring Boot (Java 21)
  src/main/java/.../      auth, account, catalog, cart, order, payment, shipping, admin, seed
  src/main/resources/
    db/migration/         uma pasta por banco: postgresql, mysql, sqlserver, oracle
  src/test/               testes de integração (JUnit + MockMvc)
frontend/                 interface React + Vite, servida pelo nginx no Docker
  src/pages/              vitrine, catálogo, livro, carrinho, checkout, compras, conta, administração
  src/components/         capas geradas, janela de pagamento, linha do tempo do envio, gaveta do carrinho
docs/                     manuais em PDF e imagens do README
.design/docs-src/         HTML, estilo e script que geram os manuais
.github/workflows/        integração contínua (testes nos quatro bancos)
```

<p align="center">
  <img src="docs/images/tela-compras.png" width="49%" alt="Minhas compras com filtros por status">
  <img src="docs/images/tela-escuro.png" width="49%" alt="Vitrine no tema escuro">
</p>

## Referência

### API

Entre com `POST /api/auth/login` e envie o `accessToken` no cabeçalho `Authorization: Bearer <token>`. O
token vale 15 minutos; quando ele expira, `POST /api/auth/refresh` usa o cookie HttpOnly para gerar outro.

```bash
# entrar
TOKEN=$(curl -s -H "Content-Type: application/json" \
  -d '{"email":"leitora@prelo.dev","password":"Prelo@2026"}' \
  http://localhost:8080/api/auth/login | jq -r .accessToken)

# buscar sem acentos
curl "http://localhost:8080/api/books?q=memorias%20postumas&sort=price_asc"

# carrinho, checkout e pagamento com Pix
curl -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
     -d '{"bookId":1,"quantity":1}' http://localhost:8080/api/cart/items
curl -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
     -d '{"addressId":1,"shippingMethod":"EXPRESSA","couponCode":"BEMVINDO10"}' http://localhost:8080/api/orders
curl -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" -H "Idempotency-Key: $(uuidgen)" \
     -d '{"method":"PIX"}' http://localhost:8080/api/orders/<codigo>/payments
```

| Rota | O que faz |
|---|---|
| `GET /api/home`, `/api/categories`, `/api/catalogs/{slug}` | Vitrine, categorias e catálogos curados |
| `GET /api/books`, `/api/books/{slug}` | Busca com filtros (`q`, `category`, `catalog`, `format`, `minPrice`, `maxPrice`, `inStock`, `sort`, `page`) e detalhe |
| `POST /api/shipping/quote` | Cotação de frete por CEP, peso e subtotal |
| `POST /api/auth/register`, `/login`, `/refresh`, `/logout` | Conta e sessão |
| `GET/POST/PATCH/DELETE /api/cart/...` | Carrinho, inclusive `POST /api/cart/merge` |
| `POST /api/checkout/quote`, `POST /api/orders` | Simulação com cupom e frete, e fechamento do pedido |
| `GET /api/orders`, `/api/orders/{codigo}`, `POST .../cancel` | Minhas compras, detalhes do envio e cancelamento |
| `POST /api/payments/card-tokens`, `POST /api/orders/{codigo}/payments` | Token do cartão e pagamento (cartão, Pix ou boleto) |
| `GET /api/account/addresses`, `PUT /api/account/profile` | Endereços e dados da conta |
| `/api/admin/...` | Painel, pedidos, envios, livros, estoque, categorias e catálogos |

Todos os erros seguem o formato Problem Details (RFC 9457), com um campo `code` estável, por exemplo
`estoque_insuficiente` ou `cupom_invalido`. A lista completa de rotas e códigos está no manual e em
`/api/docs`.

### Configuração

As principais variáveis do `.env` (a lista completa está no manual):

| Variável | Padrão | O que faz |
|---|---|---|
| `APP_PORT` | `8080` | Porta no navegador |
| `DB_VENDOR` / `COMPOSE_PROFILES` | `postgresql` | Banco usado: `postgresql`, `mysql`, `sqlserver` ou `oracle` |
| `DB_NAME` / `DB_USER` / `DB_PASSWORD` | `prelo` / `prelo` / sem padrão | Banco de dados. A senha é gerada pelos scripts |
| `JWT_SECRET` | sem padrão | Chave do JWT (32 caracteres ou mais). Gerada pelos scripts; nunca publique a sua |
| `APP_SEED_DEMO` | `true` | Contas, livros e pedidos de exemplo num banco vazio |
| `PAYMENTS_SANDBOX` | `true` | Pagamentos simulados com cartões de teste |
| `APP_HTTPS` | `false` | Use `true` atrás de um proxy com HTTPS (cookie de sessão só por HTTPS) |
| `API_DOCS_ENABLED` | `true` | Liga ou desliga o Swagger |

### Testes

Os testes da API sobem a aplicação inteira contra um banco e um Redis reais (o Redis de teste usa o banco 15):

```bash
cd backend
DB_PASSWORD=prelo mvn verify                                    # PostgreSQL
DB_VENDOR=mysql DB_HOST=127.0.0.1 DB_PASSWORD=prelo mvn verify     # MySQL (o mesmo vale para sqlserver e oracle)

cd frontend && npm test
```

São 16 testes de integração na API (cadastro, sessão e limite de tentativas, busca sem acentos, filtros,
frete, carrinho, checkout com cupom, cartão aprovado e recusado, Pix e boleto, idempotência, cancelamento com
estorno, expiração de pedidos, estoque concorrente, permissões e validação do esquema pelo Hibernate) e testes
de componentes na interface. O GitHub Actions roda a API nos quatro bancos, testa e compila a interface e
constrói as imagens Docker a cada push.

### Componentes de terceiros

O Prelo depende de componentes com licenças próprias, que continuam valendo para cada um deles:

| Componente | Uso no projeto | Licença |
|---|---|---|
| [Spring Boot](https://spring.io/projects/spring-boot) e [Spring Security](https://spring.io/projects/spring-security) | API, segurança e JWT | Apache-2.0 |
| [Hibernate ORM](https://hibernate.org/orm/) | JPA | Apache-2.0 |
| [Flyway](https://github.com/flyway/flyway) | Migrações do banco | Apache-2.0 |
| [springdoc-openapi](https://springdoc.org/) | Documentação da API | Apache-2.0 |
| [React](https://react.dev/) e [React Router](https://reactrouter.com/) | Interface | MIT |
| [Vite](https://vite.dev/) | Build da interface | MIT |
| [Lucide](https://lucide.dev/) | Ícones da interface | ISC |
| [node-qrcode](https://github.com/soldair/node-qrcode) | QR Code do Pix | MIT |
| [Gloock](https://github.com/duartp/gloock) e [Libre Franklin](https://github.com/impallari/Libre-Franklin) | Fontes da interface e dos manuais | SIL Open Font License 1.1 |
| [IBM Plex Mono](https://github.com/IBM/plex) | Fonte do código nos manuais | SIL Open Font License 1.1 |
| Drivers JDBC do [PostgreSQL](https://jdbc.postgresql.org/), [MySQL](https://dev.mysql.com/downloads/connector/j/), [SQL Server](https://github.com/microsoft/mssql-jdbc) e [Oracle](https://www.oracle.com/database/technologies/appdev/jdbc.html) | Acesso aos bancos | BSD-2-Clause, GPLv2 com exceção FOSS, MIT e Oracle FUTC |

PostgreSQL, MySQL, SQL Server, Oracle e Redis rodam como serviços externos via Docker e não são
redistribuídos neste repositório; cada um segue a licença da sua própria imagem (a do SQL Server roda na
edição Developer, e a do Oracle, na edição Free).

## Licença

Copyright (©) 2026 Clara Portes

Este projeto é distribuído sob a licença [MIT](LICENSE). Você pode usar, copiar, modificar e redistribuir o
código, inclusive para fins comerciais, desde que mantenha o aviso de copyright e o texto da licença.
