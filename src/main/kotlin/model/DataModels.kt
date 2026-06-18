package model

data class Category(
    val id: String,
    val name: String
)

data class Restaurant(
    val id: String,
    val name: String,
    val rating: Double,
    val deliveryTime: String,
    val deliveryFee: Double,
    val category: String,
    val description: String = ""
)

data class Product(
    val id: String,
    val name: String,
    val description: String,
    val price: Double,
    val rating: Double,
    val reviewsCount: Int,
    val category: String,
    val restaurantId: String
)

data class ProductReview(
    val id: String,
    val userName: String,
    val rating: Int,
    val comment: String,
    val date: String
)

data class Order(
    val id: String,
    val restaurantName: String,
    val date: String,
    val total: Double,
    val itemsCount: Int,
    val status: String
)

data class CartItem(
    val product: Product,
    var quantity: Int
)

data class UserProfile(
    var name: String,
    var email: String,
    var address: String,
    var zipCode: String,
    var phone: String
)

data class PaymentMethod(
    val id: String,
    val type: String, // ex: "Pix", "Cartão de Crédito"
    val lastFour: String = "",
    val cardBrand: String = ""
)
