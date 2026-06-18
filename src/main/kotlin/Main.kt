import model.*
import viewmodel.FoodExpressViewModel

// Definição de Cores ANSI para deixar o terminal minimalista e elegante (estilo Slate/Shadcn)
const val ANSI_RESET = "\u001b[0m"
const val ANSI_BOLD = "\u001b[1m"
const val ANSI_GRAY = "\u001b[90m"
const val ANSI_RED = "\u001b[31m"
const val ANSI_GREEN = "\u001b[32m"
const val ANSI_YELLOW = "\u001b[33m"
const val ANSI_CYAN = "\u001b[36m"
const val ANSI_WHITE = "\u001b[37m"

enum class AppScreen {
    HOME,
    RESTAURANT_MENU,
    PRODUCT_DETAIL,
    PROFILE
}

fun main() {
    val viewModel = FoodExpressViewModel()
    var currentScreen = AppScreen.HOME
    var selectedRestaurant: Restaurant? = null
    var activeBannerIndex = 0

    // Variável para armazenar mensagens temporárias de feedback para o usuário
    var feedbackMessage = ""
    var feedbackSuccess = true

    while (true) {
        clearScreen()

        // Cabeçalho Principal (Com cores baseadas nas configurações do usuário)
        val isDarkMode = viewModel.darkModeEnabled
        val themeColor = if (isDarkMode) ANSI_GRAY else ANSI_RESET
        print(themeColor)

        // Exibe feedback se houver
        if (feedbackMessage.isNotEmpty()) {
            val color = if (feedbackSuccess) ANSI_GREEN else ANSI_RED
            println("${color}${ANSI_BOLD}  $feedbackMessage  ${ANSI_RESET}")
            println(ANSI_GRAY + "  ─".repeat(28) + ANSI_RESET)
            feedbackMessage = "" // Limpa após exibir uma vez
        }

        when (currentScreen) {
            AppScreen.HOME -> {
                // Rotaciona banner de promoção a cada visita para simular um carrossel
                val currentBanner = viewModel.banners[activeBannerIndex]
                
                println("${ANSI_BOLD}${ANSI_CYAN}=== FoodExpress ===${ANSI_RESET}")
                println("${ANSI_GRAY}Alimentos frescos entregues rapidamente${ANSI_RESET}\n")

                // Exibição do Banner Promocional em caixa ASCII
                println("┌────────────────────────────────────────────────────────┐")
                println("│ ${ANSI_BOLD}${ANSI_CYAN}PROMOÇÃO EXCLUSIVA${ANSI_RESET}                                     │")
                val paddedBanner = currentBanner.padEnd(52)
                println("│ $paddedBanner │")
                println("└────────────────────────────────────────────────────────┘")
                
                // Opções de pesquisa ativas
                if (viewModel.searchQuery.isNotEmpty()) {
                    println("${ANSI_YELLOW}[Pesquisa ativa: \"${viewModel.searchQuery}\" - Digite P para limpar]${ANSI_RESET}")
                }
                if (viewModel.selectedCategory != null) {
                    println("${ANSI_YELLOW}[Categoria ativa: ${viewModel.selectedCategory?.name} - Digite C0 para limpar]${ANSI_RESET}")
                }
                println()

                // Exibição de Categorias
                println("${ANSI_BOLD}Categorias:${ANSI_RESET}")
                print("  [C0] Todos  ")
                viewModel.categories.forEach { category ->
                    val isCatActive = viewModel.selectedCategory?.id == category.id
                    val format = if (isCatActive) "${ANSI_BOLD}${ANSI_CYAN}" else ""
                    print("  [C${category.id}] $format${category.name}${ANSI_RESET}  ")
                }
                println("\n")

                // Exibição de Restaurantes
                println("${ANSI_BOLD}Restaurantes em Destaque:${ANSI_RESET}")
                if (viewModel.filteredRestaurants.isEmpty()) {
                    println("  ${ANSI_GRAY}Nenhum restaurante encontrado para a busca.${ANSI_RESET}")
                } else {
                    viewModel.filteredRestaurants.forEachIndexed { index, restaurant ->
                        val idx = index + 1
                        println(
                            "  ${ANSI_BOLD}[R$idx] ${restaurant.name}${ANSI_RESET} " +
                            "${ANSI_GRAY}• ${restaurant.category}${ANSI_RESET} " +
                            "${ANSI_YELLOW}★ ${restaurant.rating}${ANSI_RESET} " +
                            "${ANSI_GRAY}(${restaurant.deliveryTime} | Entrega: ${if (restaurant.deliveryFee == 0.0) "Grátis" else viewModel.formatPrice(restaurant.deliveryFee)})${ANSI_RESET}"
                        )
                        println("       ${ANSI_GRAY}${restaurant.description}${ANSI_RESET}")
                    }
                }

                println("\n" + ANSI_GRAY + "─".repeat(58) + ANSI_RESET)
                println(
                    "${ANSI_BOLD}Comandos:${ANSI_RESET}\n" +
                    "  [R <número>] Ver cardápio do restaurante (Ex: R1)\n" +
                    "  [C <número>] Filtrar por Categoria (Ex: C1, C0 para limpar)\n" +
                    "  [P]          Pesquisar restaurante/prato por palavra-chave\n" +
                    "  [M]          Ver Perfil, Configurações e Carrinho\n" +
                    "  [S]          Sair do Aplicativo"
                )
            }

            AppScreen.RESTAURANT_MENU -> {
                val restaurant = selectedRestaurant
                if (restaurant == null) {
                    currentScreen = AppScreen.HOME
                    continue
                }

                println("${ANSI_BOLD}${ANSI_CYAN}=== Cardápio do Restaurante ===${ANSI_RESET}")
                println("${ANSI_BOLD}${restaurant.name}${ANSI_RESET} - ${ANSI_GRAY}${restaurant.category} • ${restaurant.description}${ANSI_RESET}")
                println("${ANSI_YELLOW}★ ${restaurant.rating}${ANSI_RESET} • ${ANSI_GRAY}Tempo de entrega: ${restaurant.deliveryTime}${ANSI_RESET}\n")

                val menuProducts = viewModel.getProductsForRestaurant(restaurant.id)
                println("${ANSI_BOLD}Pratos disponíveis:${ANSI_RESET}")
                menuProducts.forEachIndexed { index, product ->
                    val idx = index + 1
                    println("  ${ANSI_BOLD}[P$idx] ${product.name}${ANSI_RESET} ──── ${ANSI_GREEN}${viewModel.formatPrice(product.price)}${ANSI_RESET}")
                    println("       ${ANSI_GRAY}${product.description}${ANSI_RESET}")
                }

                println("\n" + ANSI_GRAY + "─".repeat(58) + ANSI_RESET)
                println(
                    "${ANSI_BOLD}Comandos:${ANSI_RESET}\n" +
                    "  [P <número>] Ver detalhes do prato e adicionar ao carrinho (Ex: P1)\n" +
                    "  [V]          Voltar para a lista de restaurantes (Home)"
                )
            }

            AppScreen.PRODUCT_DETAIL -> {
                val product = viewModel.selectedProduct
                if (product == null) {
                    currentScreen = AppScreen.HOME
                    continue
                }

                println("${ANSI_BOLD}${ANSI_CYAN}=== Detalhes do Produto ===${ANSI_RESET}\n")

                // Ilustração minimalista ASCII (Iniciais do prato)
                val initials = product.name.split(" ").map { it[0] }.joinToString("").take(2).uppercase().padEnd(2)

                println("        ┌───────────────┐")
                println("        │      $initials       │")
                println("        │  FoodExpress  │")
                println("        └───────────────┘\n")

                println("${ANSI_BOLD}${product.name}${ANSI_RESET}")
                println("Descrição: ${ANSI_GRAY}${product.description}${ANSI_RESET}")
                println("Preço:     ${ANSI_GREEN}${viewModel.formatPrice(product.price)}${ANSI_RESET}")
                println("Avaliação: ${ANSI_YELLOW}★ ${product.rating}${ANSI_RESET} (${product.reviewsCount} avaliações)\n")

                println("${ANSI_BOLD}Avaliações de Clientes:${ANSI_RESET}")
                viewModel.selectedProductReviews.forEach { review ->
                    println("  ${ANSI_BOLD}${review.userName}${ANSI_RESET} ${ANSI_YELLOW}${"★".repeat(review.rating)}${ANSI_RESET} ${ANSI_GRAY}(${review.date})${ANSI_RESET}")
                    println("  \"${review.comment}\"\n")
                }

                println(ANSI_GRAY + "─".repeat(58) + ANSI_RESET)
                println(
                    "${ANSI_BOLD}Comandos:${ANSI_RESET}\n" +
                    "  [A <quantidade>] Adicionar produto ao carrinho (Ex: A1, A3)\n" +
                    "  [V]              Voltar para o menu do restaurante"
                )
            }

            AppScreen.PROFILE -> {
                println("${ANSI_BOLD}${ANSI_CYAN}=== Perfil do Usuário & Carrinho ===${ANSI_RESET}")
                println("${ANSI_BOLD}Cliente:${ANSI_RESET} ${viewModel.profile.name} (${viewModel.profile.email})")
                println("${ANSI_BOLD}Telefone:${ANSI_RESET} ${viewModel.profile.phone}\n")

                // Endereço de Entrega
                println("┌────────────────────────────────────────────────────────┐")
                println("│ ${ANSI_BOLD}Endereço de Entrega${ANSI_RESET}                                    │")
                println("│ ${viewModel.profile.address.padEnd(54)} │")
                println("│ CEP: ${viewModel.profile.zipCode.padEnd(51)} │")
                println("└────────────────────────────────────────────────────────┘\n")

                // Formas de Pagamento
                print("${ANSI_BOLD}Formas de Pagamento salvas:${ANSI_RESET}")
                viewModel.paymentMethods.forEach { method ->
                    val extra = if (method.lastFour.isNotEmpty()) " (${method.cardBrand} final ${method.lastFour})" else ""
                    print(" | ${method.type}$extra")
                }
                println(" |\n")

                // Configurações
                println("${ANSI_BOLD}Configurações:${ANSI_RESET}")
                println("  [N] Notificações Push: ${if (viewModel.notificationsEnabled) "${ANSI_GREEN}LIGADO${ANSI_RESET}" else "${ANSI_RED}DESLIGADO${ANSI_RESET}"}")
                println("  [T] Tema Escuro:       ${if (viewModel.darkModeEnabled) "${ANSI_GREEN}LIGADO (Texto Cinza)${ANSI_RESET}" else "${ANSI_RED}DESLIGADO (Padrão)${ANSI_RESET}"}\n")

                // Carrinho de Compras e Checkout
                println("┌────────────────────────────────────────────────────────┐")
                println("│ ${ANSI_BOLD}Carrinho de Compras${ANSI_RESET}                                     │")
                if (viewModel.cart.isEmpty()) {
                    println("│ Seu carrinho está vazio.                               │")
                    println("└────────────────────────────────────────────────────────┘")
                } else {
                    viewModel.cart.forEach { item ->
                        val itemText = "  - ${item.quantity}x ${item.product.name} (${viewModel.formatPrice(item.product.price * item.quantity)})"
                        println("│ ${itemText.padEnd(54)} │")
                    }
                    println("├────────────────────────────────────────────────────────┤")
                    val totalText = "  Total: ${viewModel.formatPrice(viewModel.getCartTotal())}"
                    println("│ ${ANSI_BOLD}${totalText.padEnd(54)}${ANSI_RESET} │")
                    println("└────────────────────────────────────────────────────────┘")
                    println("  Digite ${ANSI_BOLD}[F]${ANSI_RESET} para finalizar e fechar o pedido.")
                }
                println()

                // Pedidos Anteriores
                println("${ANSI_BOLD}Histórico de Pedidos:${ANSI_RESET}")
                if (viewModel.orders.isEmpty()) {
                    println("  ${ANSI_GRAY}Nenhum pedido anterior.${ANSI_RESET}")
                } else {
                    viewModel.orders.forEach { order ->
                        println(
                            "  [${order.id}] ${order.restaurantName} • ${order.date} • " +
                            "${order.itemsCount} prato(s) • Total: ${ANSI_GREEN}${viewModel.formatPrice(order.total)}${ANSI_RESET} " +
                            "(${ANSI_CYAN}${order.status}${ANSI_RESET})"
                        )
                    }
                }

                println("\n" + ANSI_GRAY + "─".repeat(58) + ANSI_RESET)
                println(
                    "${ANSI_BOLD}Comandos:${ANSI_RESET}\n" +
                    "  [E] Editar Endereço de Entrega\n" +
                    "  [A] Cadastrar Cartão de Pagamento\n" +
                    "  [N] Alternar Notificações\n" +
                    "  [T] Alternar Tema Claro/Escuro\n" +
                    "  [V] Voltar para a Home (Restaurantes)"
                )
            }
        }

        print("\nDigite o comando: ")
        val input = readlnOrNull()?.trim() ?: ""

        if (input.equals("s", ignoreCase = true) && currentScreen == AppScreen.HOME) {
            println("\nObrigado por usar o FoodExpress. Até mais!")
            break
        }

        // Lógica de Processamento de Comandos
        try {
            when (currentScreen) {
                AppScreen.HOME -> {
                    val inputLower = input.lowercase()
                    if (inputLower.startsWith("r")) {
                        // Selecionar restaurante
                        val index = inputLower.substring(1).toIntOrNull()
                        if (index != null && index > 0 && index <= viewModel.filteredRestaurants.size) {
                            selectedRestaurant = viewModel.filteredRestaurants[index - 1]
                            currentScreen = AppScreen.RESTAURANT_MENU
                            // Incrementa o banner para rotacionar
                            activeBannerIndex = (activeBannerIndex + 1) % viewModel.banners.size
                        } else {
                            feedbackMessage = "Número de restaurante inválido!"
                            feedbackSuccess = false
                        }
                    } else if (inputLower.startsWith("c")) {
                        // Selecionar categoria
                        val catId = inputLower.substring(1)
                        if (catId == "0") {
                            viewModel.selectCategory(null)
                            feedbackMessage = "Filtro de categoria removido!"
                            feedbackSuccess = true
                        } else {
                            val category = viewModel.categories.find { it.id == catId }
                            if (category != null) {
                                viewModel.selectCategory(category)
                                feedbackMessage = "Filtro aplicado: ${category.name}"
                                feedbackSuccess = true
                            } else {
                                feedbackMessage = "Categoria inválida!"
                                feedbackSuccess = false
                            }
                        }
                    } else if (inputLower == "p") {
                        // Limpa ou define busca
                        if (viewModel.searchQuery.isNotEmpty()) {
                            viewModel.setSearchQuery("")
                            feedbackMessage = "Busca limpa com sucesso!"
                            feedbackSuccess = true
                        } else {
                            print("Digite o termo de busca: ")
                            val query = readlnOrNull()?.trim() ?: ""
                            viewModel.setSearchQuery(query)
                            feedbackMessage = "Filtro de busca aplicado!"
                            feedbackSuccess = true
                        }
                    } else if (inputLower == "m") {
                        currentScreen = AppScreen.PROFILE
                        activeBannerIndex = (activeBannerIndex + 1) % viewModel.banners.size
                    } else {
                        feedbackMessage = "Comando não reconhecido. Use R <num>, C <num>, P, M ou S."
                        feedbackSuccess = false
                    }
                }

                AppScreen.RESTAURANT_MENU -> {
                    val inputLower = input.lowercase()
                    val restaurant = selectedRestaurant!!
                    val menuProducts = viewModel.getProductsForRestaurant(restaurant.id)

                    if (inputLower == "v") {
                        currentScreen = AppScreen.HOME
                        selectedRestaurant = null
                    } else if (inputLower.startsWith("p")) {
                        val index = inputLower.substring(1).toIntOrNull()
                        if (index != null && index > 0 && index <= menuProducts.size) {
                            val product = menuProducts[index - 1]
                            viewModel.selectProduct(product)
                            currentScreen = AppScreen.PRODUCT_DETAIL
                        } else {
                            feedbackMessage = "Número de prato inválido!"
                            feedbackSuccess = false
                        }
                    } else {
                        feedbackMessage = "Comando inválido. Use P <num> ou V."
                        feedbackSuccess = false
                    }
                }

                AppScreen.PRODUCT_DETAIL -> {
                    val inputLower = input.lowercase()
                    val product = viewModel.selectedProduct!!

                    if (inputLower == "v") {
                        currentScreen = AppScreen.RESTAURANT_MENU
                    } else if (inputLower.startsWith("a")) {
                        val qty = if (inputLower.length > 1) inputLower.substring(1).toIntOrNull() ?: 1 else 1
                        if (qty > 0) {
                            simulateLoading("Adicionando ao carrinho...")
                            viewModel.addToCart(product, qty)
                            feedbackMessage = "✓ $qty x ${product.name} adicionado(s) ao carrinho!"
                            feedbackSuccess = true
                            currentScreen = AppScreen.RESTAURANT_MENU
                        } else {
                            feedbackMessage = "Quantidade deve ser maior que zero!"
                            feedbackSuccess = false
                        }
                    } else {
                        feedbackMessage = "Comando inválido. Use A <qtd> ou V."
                        feedbackSuccess = false
                    }
                }

                AppScreen.PROFILE -> {
                    val inputLower = input.lowercase()
                    when (inputLower) {
                        "v" -> {
                            currentScreen = AppScreen.HOME
                        }
                        "n" -> {
                            viewModel.toggleNotifications()
                            feedbackMessage = "Notificações alteradas!"
                            feedbackSuccess = true
                        }
                        "t" -> {
                            viewModel.toggleDarkMode()
                            feedbackMessage = "Configuração de Tema atualizada!"
                            feedbackSuccess = true
                        }
                        "e" -> {
                            print("Digite o novo endereço completo: ")
                            val newAddr = readlnOrNull()?.trim() ?: ""
                            print("Digite o novo CEP: ")
                            val newZip = readlnOrNull()?.trim() ?: ""
                            if (newAddr.isNotEmpty() && newZip.isNotEmpty()) {
                                viewModel.updateAddress(newAddr, newZip)
                                feedbackMessage = "Endereço atualizado com sucesso!"
                                feedbackSuccess = true
                            } else {
                                feedbackMessage = "Campos não podem ser vazios!"
                                feedbackSuccess = false
                            }
                        }
                        "a" -> {
                            print("Digite os 4 últimos dígitos do cartão: ")
                            val digits = readlnOrNull()?.trim() ?: ""
                            print("Digite a bandeira (ex: Visa, Mastercard): ")
                            val brand = readlnOrNull()?.trim() ?: ""
                            if (digits.length == 4 && digits.toIntOrNull() != null && brand.isNotEmpty()) {
                                viewModel.addPaymentMethod("Cartão de Crédito", digits, brand)
                                feedbackMessage = "Cartão de Crédito adicionado!"
                                feedbackSuccess = true
                            } else {
                                feedbackMessage = "Dados inválidos! Informe exatamente 4 dígitos numéricos."
                                feedbackSuccess = false
                            }
                        }
                        "f" -> {
                            if (viewModel.cart.isNotEmpty()) {
                                simulateLoading("Finalizando seu pedido no restaurante...")
                                val order = viewModel.placeOrder()
                                if (order != null) {
                                    feedbackMessage = "✓ Pedido ${order.id} realizado com sucesso! Total: ${viewModel.formatPrice(order.total)}"
                                    feedbackSuccess = true
                                }
                            } else {
                                feedbackMessage = "Seu carrinho está vazio para checkout!"
                                feedbackSuccess = false
                            }
                        }
                        else -> {
                            feedbackMessage = "Comando inválido. Use E, A, N, T ou V."
                            feedbackSuccess = false
                        }
                    }
                }
            }
        } catch (e: Exception) {
            feedbackMessage = "Ocorreu um erro ao processar o comando: ${e.message}"
            feedbackSuccess = false
        }
    }
}

// Limpa o console para simular a mudança de tela
fun clearScreen() {
    try {
        // Tenta comando ANSI para limpar tela
        print("\u001b[H\u001b[2J")
        System.out.flush()
    } catch (e: Exception) {
        // Fallback: imprime linhas em branco se o terminal não aceitar ANSI
        repeat(50) { println() }
    }
}

// Simula um loading básico para a experiência de uso
fun simulateLoading(message: String) {
    print("  $message ")
    repeat(3) {
        Thread.sleep(300)
        print(".")
    }
    println()
}
