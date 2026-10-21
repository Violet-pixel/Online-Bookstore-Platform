package io.github.violetpixel.bookstore.seed;

import java.util.List;

/**
 * Acervo de demonstração. Os clássicos estão em domínio público; os demais títulos, autores contemporâneos,
 * editoras, preços e ISBNs (prefixo fictício 978-65-99999) foram inventados para a demonstração.
 */
final class DemoBooks {

    private DemoBooks() { }

    record Cat(String slug, String name, String accent, String description) {
    }

    record B(String title, String subtitle, String authors, String publisher, String category, int year, int pages,
            String format, String price, String listPrice, int stock, int weight, double rating, int ratings, int sold,
            String synopsis) {
    }

    static final List<Cat> CATEGORIES = List.of(
        new Cat("literatura-brasileira", "Literatura brasileira", "#B86B84", "De Machado de Assis a Lima Barreto: a prosa que formou o nosso jeito de contar histórias."),
        new Cat("classicos-do-mundo", "Clássicos do mundo", "#E0B94F", "Romances que atravessaram séculos e continuam conversando com a gente."),
        new Cat("poesia", "Poesia", "#FFDF7F", "Versos para ler devagar, em voz alta, e voltar sempre."),
        new Cat("fantasia-e-ficcao", "Fantasia e ficção", "#212121", "Mundos inventados, faróis, cidades de vidro e bibliotecas proibidas."),
        new Cat("tecnologia", "Tecnologia", "#B86B84", "Programação, bancos de dados e arquitetura para quem constrói software."),
        new Cat("negocios-e-carreira", "Negócios e carreira", "#E0B94F", "Decisões, foco e pequenos negócios, sem fórmulas mágicas."),
        new Cat("infantojuvenil", "Infantojuvenil", "#FFDF7F", "Histórias para ler junto, rir junto e pedir de novo."),
        new Cat("historia-e-ensaio", "História e ensaio", "#DADADA", "Ensaios e reportagens que ajudam a entender de onde viemos."));

    static final List<B> BOOKS = List.of(
        new B("Dom Casmurro", null, "Machado de Assis", "Editora Alvorada", "literatura-brasileira", 1899, 256, "PAPERBACK", "34.90", "44.90", 42, 320, 4.8, 1284, 820,
            "Bentinho revisita a juventude ao lado de Capitu e tenta provar, para si mesmo, aquilo que nunca soube ao certo. Um dos romances mais discutidos da língua portuguesa, narrado com ironia, memória e dúvida do começo ao fim."),
        new B("Memórias Póstumas de Brás Cubas", null, "Machado de Assis", "Editora Alvorada", "literatura-brasileira", 1881, 288, "PAPERBACK", "36.90", "42.90", 30, 340, 4.7, 962, 640,
            "Um defunto autor resolve contar a própria vida, capítulo a capítulo, sem nenhuma pressa e com muito deboche. O livro que virou a prosa brasileira de cabeça para baixo."),
        new B("Quincas Borba", null, "Machado de Assis", "Editora Alvorada", "literatura-brasileira", 1891, 320, "PAPERBACK", "39.90", "39.90", 18, 360, 4.5, 402, 210,
            "Rubião herda a fortuna e o cachorro do filósofo Quincas Borba e desembarca no Rio de Janeiro, onde aprende, do jeito mais caro, que ao vencedor ficam as batatas."),
        new B("O Alienista", null, "Machado de Assis", "Selo Bambu", "literatura-brasileira", 1882, 96, "POCKET", "19.90", "24.90", 64, 140, 4.6, 733, 905,
            "Em Itaguaí, o doutor Simão Bacamarte funda a Casa Verde para estudar a loucura e começa a internar a cidade inteira. Uma novela curta, afiada e engraçadíssima."),
        new B("O Cortiço", null, "Aluísio Azevedo", "Edições Pilar", "literatura-brasileira", 1890, 304, "PAPERBACK", "32.90", "38.90", 25, 350, 4.5, 588, 470,
            "Ao redor do cortiço de João Romão, uma multidão de personagens trabalha, briga, ama e sobrevive. Retrato vivo e barulhento do Rio do século XIX."),
        new B("Iracema", "Lenda do Ceará", "José de Alencar", "Quintal Editorial", "literatura-brasileira", 1865, 160, "PAPERBACK", "24.90", "29.90", 3, 220, 4.2, 311, 260,
            "A história de Iracema, a virgem dos lábios de mel, e do guerreiro português Martim, contada numa prosa que soa como poema."),
        new B("Triste Fim de Policarpo Quaresma", null, "Lima Barreto", "Casa Mirante", "literatura-brasileira", 1915, 272, "PAPERBACK", "34.90", "34.90", 22, 330, 4.6, 420, 300,
            "O major Quaresma ama o Brasil com tanta sinceridade que propõe o tupi como língua oficial. Uma sátira terna e dolorida sobre patriotismo e desilusão."),
        new B("Senhora", null, "José de Alencar", "Quintal Editorial", "literatura-brasileira", 1875, 240, "PAPERBACK", "29.90", "34.90", 0, 300, 4.3, 290, 180,
            "Aurélia, que um dia foi trocada por um dote, enriquece e decide comprar o próprio marido. Um romance sobre amor, orgulho e dinheiro."),

        new B("Orgulho e Preconceito", null, "Jane Austen", "Casa Mirante", "classicos-do-mundo", 1813, 424, "HARDCOVER", "59.90", "79.90", 35, 620, 4.8, 1530, 980,
            "Elizabeth Bennet e o senhor Darcy se detestam à primeira vista, e talvez à segunda também. Uma comédia de costumes elegante, divertida e muito atual."),
        new B("Frankenstein", "Ou o Prometeu moderno", "Mary Shelley", "Vento Sul Livros", "classicos-do-mundo", 1818, 280, "PAPERBACK", "39.90", "49.90", 28, 330, 4.6, 870, 540,
            "Victor Frankenstein dá vida a uma criatura e foge dela. A obra que inaugurou a ficção científica e ainda pergunta o que nos torna humanos."),
        new B("Crime e Castigo", null, "Fiódor Dostoiévski", "Edições Pilar", "classicos-do-mundo", 1866, 592, "HARDCOVER", "74.90", "89.90", 14, 820, 4.9, 1102, 610,
            "Raskólnikov comete um crime para provar uma teoria e passa o resto do livro fugindo da própria consciência pelas ruas de São Petersburgo."),
        new B("A Metamorfose", null, "Franz Kafka", "Selo Bambu", "classicos-do-mundo", 1915, 112, "POCKET", "19.90", "22.90", 80, 150, 4.6, 1420, 1210,
            "Gregor Samsa acorda transformado em um inseto monstruoso e, ainda assim, sua maior preocupação é perder o trem para o trabalho."),
        new B("O Retrato de Dorian Gray", null, "Oscar Wilde", "Vento Sul Livros", "classicos-do-mundo", 1890, 304, "PAPERBACK", "42.90", "49.90", 4, 350, 4.7, 990, 700,
            "Dorian permanece jovem e belo enquanto seu retrato envelhece e apodrece no sótão. Um romance sobre vaidade, arte e o preço dos desejos."),
        new B("Os Maias", "Episódios da vida romântica", "Eça de Queirós", "Editora Alvorada", "classicos-do-mundo", 1888, 720, "HARDCOVER", "79.90", "89.90", 9, 980, 4.6, 380, 160,
            "Três gerações de uma família lisboeta e um amor impossível. O grande romance de Eça, irônico e trágico na mesma medida."),
        new B("Dom Quixote", "O engenhoso fidalgo de La Mancha", "Miguel de Cervantes", "Casa Mirante", "classicos-do-mundo", 1605, 1104, "HARDCOVER", "119.90", "149.90", 7, 1400, 4.7, 512, 140,
            "De tanto ler romances de cavalaria, um fidalgo resolve virar cavaleiro andante, convoca Sancho Pança e sai pelo mundo enfrentando moinhos de vento."),

        new B("Espumas Flutuantes", null, "Castro Alves", "Quintal Editorial", "poesia", 1870, 176, "PAPERBACK", "27.90", "32.90", 20, 230, 4.5, 210, 120,
            "O único livro publicado em vida pelo poeta dos escravos: amor, natureza e indignação em versos que pedem leitura em voz alta."),
        new B("Eu", null, "Augusto dos Anjos", "Selo Bambu", "poesia", 1912, 160, "PAPERBACK", "29.90", "29.90", 16, 210, 4.7, 344, 190,
            "Poemas que misturam ciência, morte e melancolia num vocabulário que ninguém mais ousou usar. Estranho, único e inesquecível."),
        new B("Broquéis", null, "Cruz e Sousa", "Selo Bambu", "poesia", 1893, 128, "POCKET", "22.90", "26.90", 12, 160, 4.4, 130, 75,
            "O livro que inaugurou o simbolismo no Brasil, com sonetos de brancura, música e mistério."),
        new B("Folhas de Relva", null, "Walt Whitman", "Vento Sul Livros", "poesia", 1855, 512, "HARDCOVER", "69.90", "84.90", 10, 720, 4.6, 260, 95,
            "Whitman canta a si mesmo, as cidades, os corpos e a democracia, num verso livre que mudou a poesia moderna."),

        new B("O Último Bibliotecário", null, "Sofia Carvalho", "Vento Sul Livros", "fantasia-e-ficcao", 2024, 384, "PAPERBACK", "54.90", "64.90", 40, 460, 4.8, 612, 760,
            "Numa cidade onde os livros são racionados, Ana guarda um catálogo secreto de obras que não deveriam existir. Até que alguém começa a pedir empréstimos."),
        new B("As Cartas do Farol", null, "Iara Monteiro", "Casa Mirante", "fantasia-e-ficcao", 2025, 312, "PAPERBACK", "49.90", "49.90", 33, 380, 4.6, 288, 410,
            "Toda noite o farol da ilha acende uma mensagem diferente. Uma faroleira aposentada decide descobrir quem está escrevendo para o mar."),
        new B("Cidade de Vidro", null, "Rafael Lins", "Edições Pilar", "fantasia-e-ficcao", 2023, 448, "PAPERBACK", "59.90", "69.90", 21, 520, 4.4, 199, 230,
            "Em Lúmen, tudo é transparente, inclusive o que as pessoas pensam. Menos para Teo, que nasceu opaco e precisa esconder isso de todo mundo."),
        new B("O Mapa das Marés", null, "Iara Monteiro", "Casa Mirante", "fantasia-e-ficcao", 2026, 296, "PAPERBACK", "52.90", "52.90", 50, 360, 4.7, 96, 180,
            "A continuação de As Cartas do Farol leva a faroleira para alto-mar atrás de um mapa que muda a cada maré."),

        new B("Java Moderno na Prática", "Records, virtual threads e Spring Boot do zero à produção", "Renata Albuquerque", "Edições Pilar", "tecnologia", 2026, 512, "PAPERBACK", "119.90", "139.90", 26, 820, 4.8, 340, 390,
            "Um guia direto do Java 21 em diante: records, pattern matching, virtual threads, testes e uma API completa com Spring Boot, JPA e Flyway."),
        new B("SQL para Quem Constrói Produtos", "Modelagem, consultas e desempenho em quatro bancos", "Helena Duarte", "Edições Pilar", "tecnologia", 2025, 368, "PAPERBACK", "89.90", "99.90", 19, 600, 4.7, 275, 300,
            "Modelagem, índices, transações e planos de execução explicados com exemplos lado a lado em PostgreSQL, MySQL, SQL Server e Oracle."),
        new B("Redis em Produção", "Cache, filas e travas distribuídas sem sustos", "Marcos Tavares", "Quintal Editorial", "tecnologia", 2025, 280, "PAPERBACK", "79.90", "89.90", 2, 420, 4.6, 151, 170,
            "Estruturas de dados, expiração, persistência, Streams e locks distribuídos, com os erros mais comuns de quem coloca Redis no ar pela primeira vez."),
        new B("React: Interfaces que Respiram", "Componentes, estado e animação com propósito", "Lívia Campos", "Vento Sul Livros", "tecnologia", 2026, 336, "PAPERBACK", "94.90", "104.90", 31, 540, 4.8, 188, 220,
            "Como construir interfaces fluidas e acessíveis em React: composição, dados remotos, transições e microinterações que ajudam em vez de distrair."),
        new B("Segurança de APIs, Sem Pânico", "Autenticação, tokens e o OWASP Top 10 na prática", "Otávio Ramos", "Quintal Editorial", "tecnologia", 2024, 304, "PAPERBACK", "84.90", "84.90", 15, 480, 4.5, 122, 140,
            "JWT, sessões, CORS, limites de tentativa e validação de entrada, com exemplos de ataques reais e de como fechar cada porta."),
        new B("Sistemas Distribuídos sem Mistério", null, "Caio Menezes", "Edições Pilar", "tecnologia", 2023, 420, "HARDCOVER", "129.90", "149.90", 8, 760, 4.7, 210, 160,
            "Consenso, filas, idempotência e consistência eventual explicados com diagramas simples e casos de produção."),

        new B("A Arte de Decidir Devagar", null, "Tomás Rezende", "Casa Mirante", "negocios-e-carreira", 2024, 256, "PAPERBACK", "49.90", "59.90", 27, 320, 4.4, 233, 260,
            "Por que as melhores decisões quase nunca são as mais rápidas, e como criar espaço para pensar no meio da pressa do trabalho."),
        new B("Pequenos Negócios, Grandes Ideias", null, "Bianca Farias", "Quintal Editorial", "negocios-e-carreira", 2025, 224, "PAPERBACK", "44.90", "49.90", 24, 290, 4.3, 140, 150,
            "Histórias de cafés, livrarias e oficinas que cresceram sem perder a alma, com lições práticas sobre caixa, clientes e equipe."),
        new B("O Hábito do Foco", null, "Tomás Rezende", "Casa Mirante", "negocios-e-carreira", 2026, 208, "PAPERBACK", "46.90", "46.90", 38, 270, 4.5, 64, 90,
            "Um método gentil para proteger o tempo de trabalho profundo, sem aplicativos milagrosos nem rotinas impossíveis."),

        new B("Alice no País das Maravilhas", null, "Lewis Carroll", "Vento Sul Livros", "infantojuvenil", 1865, 192, "HARDCOVER", "54.90", "64.90", 22, 480, 4.8, 680, 520,
            "Alice segue um coelho branco apressado, cai na toca e encontra o chapeleiro, a lagarta e uma rainha que só sabe gritar cortem-lhe a cabeça."),
        new B("A Baleia que Queria Voar", null, "Nina Prado", "Selo Bambu", "infantojuvenil", 2025, 48, "HARDCOVER", "49.90", "54.90", 30, 380, 4.9, 210, 330,
            "Lua é uma baleia que sonha em voar com as gaivotas. Um livro ilustrado sobre sonhos grandes, amigos pequenos e o tamanho do céu."),
        new B("Pipoca e o Dragão de Papel", null, "Léo Martins", "Selo Bambu", "infantojuvenil", 2024, 64, "HARDCOVER", "44.90", "44.90", 26, 360, 4.7, 150, 240,
            "Pipoca dobra um dragão de papel que ganha vida e só tem medo de uma coisa: chuva. Uma aventura para ler antes de dormir."),

        new B("Os Sertões", "Campanha de Canudos", "Euclides da Cunha", "Edições Pilar", "historia-e-ensaio", 1902, 656, "HARDCOVER", "89.90", "99.90", 11, 900, 4.5, 190, 90,
            "A terra, o homem e a luta: o relato de Euclides sobre a Guerra de Canudos, entre a reportagem, a ciência e a literatura."),
        new B("Uma Breve História do Livro", "Do papiro à tela", "Marta Lobo", "Casa Mirante", "historia-e-ensaio", 2025, 272, "PAPERBACK", "64.90", "74.90", 17, 400, 4.6, 98, 120,
            "Como o livro mudou de forma, de dono e de preço ao longo de cinco mil anos, e por que ele continua aqui."));

    record CatalogSeed(String slug, String name, String highlight, String description, List<String> titles) {
    }

    static final List<CatalogSeed> CATALOGS = List.of(
        new CatalogSeed("mais-vendidos", "Mais vendidos", "O que mais saiu da livraria neste mês",
            "Os títulos mais pedidos pelos leitores nos últimos 30 dias.",
            List.of("A Metamorfose", "Orgulho e Preconceito", "O Alienista", "Dom Casmurro", "O Último Bibliotecário",
                "O Retrato de Dorian Gray", "Memórias Póstumas de Brás Cubas", "Crime e Castigo", "Frankenstein", "Alice no País das Maravilhas")),
        new CatalogSeed("lancamentos", "Lançamentos", "Acabaram de chegar",
            "Novidades de 2025 e 2026, recém-saídas do prelo.",
            List.of("O Mapa das Marés", "Java Moderno na Prática", "React: Interfaces que Respiram", "O Hábito do Foco",
                "As Cartas do Farol", "A Baleia que Queria Voar", "SQL para Quem Constrói Produtos", "Redis em Produção",
                "Uma Breve História do Livro", "Pequenos Negócios, Grandes Ideias")),
        new CatalogSeed("classicos-essenciais", "Clássicos essenciais", "Para ler pelo menos uma vez na vida",
            "Uma estante com os clássicos que todo mundo cita, e que valem cada página.",
            List.of("Dom Casmurro", "Crime e Castigo", "Orgulho e Preconceito", "Dom Quixote", "Memórias Póstumas de Brás Cubas",
                "Os Maias", "O Cortiço", "Frankenstein", "Triste Fim de Policarpo Quaresma", "Os Sertões")),
        new CatalogSeed("para-programar-melhor", "Para programar melhor", "Da primeira API ao sistema distribuído",
            "Livros técnicos escolhidos a dedo para quem escreve código todos os dias.",
            List.of("Java Moderno na Prática", "SQL para Quem Constrói Produtos", "Redis em Produção", "React: Interfaces que Respiram",
                "Segurança de APIs, Sem Pânico", "Sistemas Distribuídos sem Mistério")),
        new CatalogSeed("leituras-de-fim-de-semana", "Leituras de fim de semana", "Comece no sábado, termine no domingo",
            "Livros curtos e intensos, perfeitos para uma tarde.",
            List.of("A Metamorfose", "O Alienista", "Iracema", "Eu", "Broquéis", "Espumas Flutuantes", "A Baleia que Queria Voar",
                "Pipoca e o Dragão de Papel")));
}
