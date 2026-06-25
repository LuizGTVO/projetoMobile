# FoodExpress — Aplicativo de Delivery

O **FoodExpress** é um aplicativo mobile nativo desenvolvido para a plataforma Android, projetado para oferecer uma experiência de compra e entrega de comida ágil, moderna e fluida. Ele simula um marketplace completo de alimentação, permitindo aos usuários explorar restaurantes, visualizar menus detalhados, gerenciar um carrinho de compras e personalizar o perfil do usuário.

---

## Integrantes do Projeto e Contribuições

### 1. Guilherme Kikuti
* **Atividades Desenvolvidas:**
  * **Desenvolvimento da Interface Gráfica (UI):** Criação e estilização das telas do aplicativo utilizando o framework moderno **Jetpack Compose**.
  * **Componentização e Telas:** Implementação das telas `HomeScreen` (página principal com carrossel de promoções, categorias e lista de restaurantes), `RestaurantMenuScreen` (cardápio dinâmico do restaurante selecionado), `DetailScreen` (detalhes minuciosos de cada produto) e `ProfileScreen` (carrinho de compras e gerenciamento do perfil do usuário).
  * **Design System e Temas:** Implementação do suporte dinâmico a **Modo Escuro (Dark Mode)** e Modo Claro (Light Mode), utilizando cores personalizadas e transições suaves de tema.
  * **Navegação do App:** Implementação do sistema de navegação baseado em pilha (`navigationStack`), permitindo fluxos intuitivos de avançar e retornar telas.

### 2. Luiz Gustavo Menino
* **Atividades Desenvolvidas:**
  * **Modelagem e Estrutura de Dados:** Criação do arquivo `DataModels.kt`, modelando todas as classes de dados da aplicação (`Category`, `Restaurant`, `Product`, `ProductReview`, `CartItem`, `UserProfile`, `PaymentMethod` e `Order`).
  * **Arquitetura e Lógica de Negócio:** Desenvolvimento do **ViewModel** (`FoodExpressViewModel.kt`), aplicando as melhores práticas do padrão MVVM para controlar o estado da aplicação de forma isolada e testável.
  * **Filtros e Mecanismo de Busca:** Implementação da lógica de filtragem de restaurantes em tempo real baseada em categorias e na query de busca textual inserida pelo usuário.
  * **Gerenciamento do Carrinho de Compras:** Criação da lógica de manipulação de itens no carrinho (adição, remoção, cálculo dinâmico de preços totais, gerenciamento de quantidades de produtos) e finalização de pedidos simulada.

---

## O que o Aplicativo Faz?

O **FoodExpress** simula de ponta a ponta as principais funcionalidades encontradas em grandes plataformas de delivery de comida atuais:

1. **Exploração de Restaurantes (Home Screen):**
   * Exibição de banners dinâmicos com promoções e cupons ativos.
   * Navegação horizontal rápida por categorias (Burgers, Pizzas, Comida Japonesa, Doces, Saudável, Bebidas).
   * Lista completa de restaurantes parceiros contendo nota média de avaliação, tempo estimado de entrega, valor da taxa de entrega e descrição da especialidade gastronômica.

2. **Busca Inteligente:**
   * Uma barra de pesquisa superior permite encontrar restaurantes específicos de forma instantânea conforme o usuário digita nomes, especialidades ou descrições.

3. **Cardápio do Restaurante (Menu Screen):**
   * Ao selecionar um estabelecimento, o usuário visualiza as informações do local e a lista específica de produtos comercializados por ele, com preços individuais e fotos simuladas.

4. **Detalhes do Produto e Avaliações (Product Detail Screen):**
   * Tela detalhada do item escolhido mostrando a descrição detalhada dos ingredientes e preço.
   * Seção de avaliações dos clientes contendo a nota, o comentário opinativo e a data do feedback.
   * Seletor de quantidade numérico para adicionar itens ao carrinho.

5. **Carrinho de Compras e Perfil (Profile Screen):**
   * Lista interativa de produtos selecionados, onde é possível aumentar/diminuir a quantidade ou remover itens com atualização imediata do subtotal.
   * Exibição e edição do endereço de entrega principal e dados do cliente.
   * Histórico de pedidos simulado mostrando o status das últimas entregas realizadas.
   * Configuração instantânea de notificações e alternador de tema (Modo Escuro / Modo Claro).

---

## Demonstração Visual (Telas do App)

O aplicativo conta com duas telas principais que demonstram o fluxo de funcionamento (Tela Principal de Descoberta e Tela de Perfil/Carrinho de Compras).

> **Nota:** Os prints correspondentes às telas em funcionamento já se encontram salvos no diretório de assets/mídia do projeto.

---

## Tecnologias Utilizadas

* **Linguagem:** [Kotlin](https://kotlinlang.org/) — linguagem oficial de desenvolvimento Android.
* **UI Toolkit:** [Jetpack Compose](https://developer.android.com/compose) — toolkit moderno e declarativo para construção de interfaces nativas.
* **Arquitetura:** MVVM (Model-View-ViewModel) para separação clara de responsabilidades entre lógica de dados e visualização.
* **Build System:** Gradle (Kotlin DSL) com Gradle Version Catalog.
* **Componentes adicionais:** Material Design 3 para os elementos visuais de formulários, botões, cards e diálogos de interação.
