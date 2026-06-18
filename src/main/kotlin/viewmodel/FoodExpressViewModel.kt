package viewmodel

import model.*
import java.text.NumberFormat
import java.util.Locale

class FoodExpressViewModel {

    // Banners
    val banners = listOf(
        "Burgers com 30% OFF — Use o cupom BURGER30",
        "Entrega Grátis para pedidos acima de R$ 50,00",
        "Festival da Pizza: Ganhe uma bebida grátis na compra de pizza Grande"
    )

    // Categorias
    val categories = listOf(
        Category("1", "Burgers"),
        Category("2", "Pizzas"),
        Category("3", "Japonesa"),
        Category("4", "Doces"),
        Category("5", "Saudável"),
        Category("6", "Bebidas")
    )

    var selectedCategory: Category? = null
        private set

    // Restaurantes
    private val restaurants = listOf(
        Restaurant("r1", "Burger House", 4.8, "20-30 min", 5.90, "Burgers", "Os melhores hambúrgueres artesanais da cidade."),
        Restaurant("r2", "Pizza Bella", 4.6, "30-45 min", 7.90, "Pizzas", "Pizzaria com forno a lenha e ingredientes importados."),
        Restaurant("r3", "Sushi Master", 4.9, "40-50 min", 12.00, "Japonesa", "Sushis frescos e combinados contemporâneos."),
        Restaurant("r4", "Doce Deleite", 4.7, "15-25 min", 4.90, "Doces", "Doces artesanais, bolos e sobremesas finas."),
        Restaurant("r5", "Green Salad", 4.5, "25-35 min", 6.90, "Saudável", "Saladas orgânicas e wraps saborosos."),
        Restaurant("r6", "Bebidas Expresso", 4.4, "10-20 min", 3.90, "Bebidas", "Bebidas geladas e cafés especiais.")
    )

    var filteredRestaurants = restaurants
        private set

    // Produtos
    private val allProducts = listOf(
        // Burgers
        Product("p1", "Classic Burger", "Pão brioche, blend bovino 150g, queijo cheddar maçaricado, bacon crocante e molho da casa.", 28.90, 4.8, 120, "Burgers", "r1"),
        Product("p2", "Double Cheddar", "Pão australiano, duplo blend de 150g, cebola caramelizada na chapa e o dobro de cheddar cremoso.", 36.90, 4.9, 85, "Burgers", "r1"),
        // Pizzas
        Product("p3", "Pizza Margherita", "Molho de tomate artesanal, muçarela de búfala, rodelas de tomate cereja e manjericão fresco.", 42.00, 4.7, 210, "Pizzas", "r2"),
        Product("p4", "Pizza Calabresa", "Muçarela especial, calabresa artesanal fatiada, cebola roxa fresca, azeitonas pretas e orégano.", 45.00, 4.6, 305, "Pizzas", "r2"),
        // Japonesa
        Product("p5", "Combo Temaki + Uramaki", "1 Temaki de salmão grelhado e cream cheese + 8 unidades de uramaki filadélfia cobertos com gergelim.", 59.90, 4.9, 140, "Japonesa", "r3"),
        Product("p6", "Hot Roll Salmão (10 unid)", "Rolinho frito de salmão com cream cheese, cebolinha picada e molho teriyaki especial.", 32.90, 4.8, 220, "Japonesa", "r3"),
        // Doces
        Product("p7", "Grand Gateau", "Bolo quente de chocolate, picolé cremoso de baunilha mergulhado, calda quente de chocolate belga e morangos.", 24.90, 4.8, 92, "Doces", "r4"),
        // Saudável
        Product("p8", "Wrap de Frango Fit", "Pão folha integral, tiras de frango grelhado, mix de folhas verdes, cenoura ralada e molho iogurte.", 22.00, 4.5, 54, "Saudável", "r5"),
        // Bebidas
        Product("p9", "Suco Natural Laranja 500ml", "Suco de laranja natural e espremido na hora, sem adição de açúcar ou conservantes.", 9.90, 4.7, 180, "Bebidas", "r6")
    )

    // Produto Selecionado para Detalhes
    var selectedProduct: Product? = allProducts.first()
        private set

    var selectedProductReviews = listOf<ProductReview>()
        private set

    // Carrinho
    val cart = mutableListOf<CartItem>()

    // Perfil do Usuário
    val profile = UserProfile(
        name = "Fulano de Tal",
        email = "fulano@exemplo.com",
        address = "Rua das Flores, 123 - Centro",
        zipCode = "01001-000",
        phone = "(11) 98765-4321"
    )

    // Pedidos anteriores
    val orders = mutableListOf(
        Order("o1", "Pizza Bella", "10 Jun 2026", 49.90, 1, "Entregue"),
        Order("o2", "Burger House", "05 Jun 2026", 63.80, 2, "Entregue")
    )

    // Formas de Pagamento
    val paymentMethods = mutableListOf(
        PaymentMethod("pay1", "Pix"),
        PaymentMethod("pay2", "Cartão de Crédito", "4321", "Visa")
    )

    // Configurações
    var notificationsEnabled = true
        private set

    var darkModeEnabled = false
        private set

    var searchQuery = ""
        private set

    // Ações de Filtragem e Busca
    fun setSearchQuery(query: String) {
        searchQuery = query
        applyFilters()
    }

    fun selectCategory(category: Category?) {
        selectedCategory = category
        applyFilters()
    }

    private fun applyFilters() {
        val query = searchQuery.trim().lowercase()
        val cat = selectedCategory

        var list = restaurants

        if (cat != null) {
            list = list.filter { it.category.lowercase() == cat.name.lowercase() }
        }

        if (query.isNotEmpty()) {
            list = list.filter {
                it.name.lowercase().contains(query) ||
                        it.description.lowercase().contains(query) ||
                        it.category.lowercase().contains(query)
            }
        }

        filteredRestaurants = list
    }

    // Ações de Produto
    fun selectProduct(product: Product) {
        selectedProduct = product
        selectedProductReviews = listOf(
            ProductReview("rev1", "Ana Silva", 5, "Hambúrguer simplesmente perfeito! Ponto da carne impecável.", "Hoje"),
            ProductReview("rev2", "Marcos Santos", 4, "Muito gostoso, bem embalado. Chegou bem quente.", "Ontem"),
            ProductReview("rev3", "Carlos Lima", 5, "Excelente qualidade, recomendo demais o ${product.name}!", "Há 3 dias")
        )
    }

    fun getProductsForRestaurant(restaurantId: String): List<Product> {
        return allProducts.filter { it.restaurantId == restaurantId }
    }

    // Ações de Carrinho
    fun addToCart(product: Product, quantity: Int) {
        if (quantity <= 0) return
        val existingItem = cart.find { it.product.id == product.id }
        if (existingItem != null) {
            existingItem.quantity += quantity
        } else {
            cart.add(CartItem(product, quantity))
        }
    }

    fun removeFromCart(product: Product) {
        cart.removeAll { it.product.id == product.id }
    }

    fun updateCartItemQuantity(product: Product, newQuantity: Int) {
        if (newQuantity <= 0) {
            removeFromCart(product)
            return
        }
        val existingItem = cart.find { it.product.id == product.id }
        if (existingItem != null) {
            existingItem.quantity = newQuantity
        }
    }

    fun getCartTotal(): Double {
        return cart.sumOf { it.product.price * it.quantity }
    }

    // Ações de Perfil e Configurações
    fun updateAddress(newAddress: String, zipCode: String) {
        profile.address = newAddress
        profile.zipCode = zipCode
    }

    fun toggleNotifications() {
        notificationsEnabled = !notificationsEnabled
    }

    fun toggleDarkMode() {
        darkModeEnabled = !darkModeEnabled
    }

    fun addPaymentMethod(type: String, lastFour: String, brand: String) {
        val newId = "pay${paymentMethods.size + 1}"
        paymentMethods.add(PaymentMethod(newId, type, lastFour, brand))
    }

    fun placeOrder(): Order? {
        if (cart.isEmpty()) return null

        val cartItems = cart.toList()
        val firstItem = cartItems.first()
        val restaurantName = restaurants.find { it.id == firstItem.product.restaurantId }?.name ?: "Restaurante"

        val total = getCartTotal()
        val totalCount = cartItems.sumOf { it.quantity }

        val newOrder = Order(
            id = "o${orders.size + 1}",
            restaurantName = restaurantName,
            date = "Hoje",
            total = total,
            itemsCount = totalCount,
            status = "Confirmado"
        )

        orders.add(0, newOrder)
        cart.clear()
        return newOrder
    }

    fun formatPrice(value: Double): String {
        return "R$ %.2f".format(Locale.US, value)
    }
}
