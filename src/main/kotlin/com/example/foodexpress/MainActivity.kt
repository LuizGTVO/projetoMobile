package com.example.foodexpress

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import model.*
import viewmodel.FoodExpressViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val viewModel = FoodExpressViewModel()

        setContent {
            var darkMode by remember { mutableStateOf(viewModel.darkModeEnabled) }
            
            // Force toggle dark mode initially for AI dark theme look
            LaunchedEffect(Unit) {
                if (!viewModel.darkModeEnabled) {
                    viewModel.toggleDarkMode()
                    darkMode = true
                }
            }

            FoodExpressTheme(darkMode = darkMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    FoodExpressApp(
                        viewModel = viewModel,
                        darkMode = darkMode,
                        onThemeToggle = {
                            viewModel.toggleDarkMode()
                            darkMode = viewModel.darkModeEnabled
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun FoodExpressTheme(
    darkMode: Boolean,
    content: @Composable () -> Unit
) {
    val colors = if (darkMode) {
        darkColorScheme(
            primary = Color(0xFF06B6D4), // Cyan / AI Accent
            background = Color(0xFF0A0A0C),
            surface = Color(0xFF121215),
            onBackground = Color(0xFFF4F4F5),
            onSurface = Color(0xFFF4F4F5),
            outline = Color(0xFF27272A)
        )
    } else {
        lightColorScheme(
            primary = Color(0xFF0891B2),
            background = Color(0xFFF9F9FB),
            surface = Color(0xFFFFFFFF),
            onBackground = Color(0xFF18181B),
            onSurface = Color(0xFF18181B),
            outline = Color(0xFFE4E4E7)
        )
    }
    MaterialTheme(
        colorScheme = colors,
        content = content
    )
}

enum class Screen {
    HOME,
    MENU,
    DETAIL,
    PROFILE
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodExpressApp(
    viewModel: FoodExpressViewModel,
    darkMode: Boolean,
    onThemeToggle: () -> Unit
) {
    var currentScreen by remember { mutableStateOf(Screen.HOME) }
    var selectedRestaurant by remember { mutableStateOf<Restaurant?>(null) }
    val navigationStack = remember { mutableStateListOf(Screen.HOME) }

    fun navigateTo(screen: Screen) {
        if (navigationStack.isEmpty() || navigationStack.last() != screen) {
            navigationStack.add(screen)
        }
        currentScreen = screen
    }

    fun navigateBack() {
        if (navigationStack.size > 1) {
            navigationStack.removeAt(navigationStack.size - 1)
            currentScreen = navigationStack.last()
        } else {
            currentScreen = Screen.HOME
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when (currentScreen) {
                            Screen.HOME -> "FoodExpress"
                            Screen.MENU -> selectedRestaurant?.name ?: "Cardápio"
                            Screen.DETAIL -> viewModel.selectedProduct?.name ?: "Detalhes"
                            Screen.PROFILE -> "Perfil e Carrinho"
                        },
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                navigationIcon = {
                    if (currentScreen != Screen.HOME) {
                        Button(
                            onClick = { navigateBack() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surface,
                                contentColor = MaterialTheme.colorScheme.onSurface
                            ),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.padding(horizontal = 8.dp)
                        ) {
                            Text("Voltar", fontSize = 12.sp)
                        }
                    }
                },
                actions = {
                    if (currentScreen != Screen.PROFILE) {
                        Button(
                            onClick = { navigateTo(Screen.PROFILE) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surface,
                                contentColor = MaterialTheme.colorScheme.onSurface
                            ),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text("Carrinho", fontSize = 12.sp)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (currentScreen) {
                Screen.HOME -> HomeScreen(
                    viewModel = viewModel,
                    onRestaurantSelect = { restaurant ->
                        selectedRestaurant = restaurant
                        navigateTo(Screen.MENU)
                    }
                )
                Screen.MENU -> RestaurantMenuScreen(
                    viewModel = viewModel,
                    restaurant = selectedRestaurant,
                    onProductSelect = { product ->
                        viewModel.selectProduct(product)
                        navigateTo(Screen.DETAIL)
                    }
                )
                Screen.DETAIL -> ProductDetailScreen(
                    viewModel = viewModel,
                    onAddedToCart = { navigateBack() }
                )
                Screen.PROFILE -> ProfileScreen(
                    viewModel = viewModel,
                    darkMode = darkMode,
                    onThemeToggle = onThemeToggle
                )
            }
        }
    }
}

@Composable
fun HomeScreen(
    viewModel: FoodExpressViewModel,
    onRestaurantSelect: (Restaurant) -> Unit
) {
    var searchQuery by remember { mutableStateOf(viewModel.searchQuery) }
    var selectedCategory by remember { mutableStateOf(viewModel.selectedCategory) }
    var activeBannerIndex by remember { mutableIntStateOf(0) }
    
    // Refresh triggered internally when state changes
    val restaurantsList = remember(searchQuery, selectedCategory) {
        viewModel.filteredRestaurants
    }

    // Auto banner rotator
    LaunchedEffect(Unit) {
        while (true) {
            delay(5000)
            activeBannerIndex = (activeBannerIndex + 1) % viewModel.banners.size
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Banner promo card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp)),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = viewModel.banners[activeBannerIndex],
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Search field
        OutlinedTextField(
            value = searchQuery,
            onValueChange = {
                searchQuery = it
                viewModel.setSearchQuery(it)
            },
            label = { Text("Pesquisar restaurante ou prato", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            ),
            shape = RoundedCornerShape(8.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Categories selector scrollable
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val allActive = selectedCategory == null
            Button(
                onClick = {
                    selectedCategory = null
                    viewModel.selectCategory(null)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (allActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                    contentColor = if (allActive) Color.Black else MaterialTheme.colorScheme.onSurface
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Todas", fontSize = 12.sp)
            }

            viewModel.categories.forEach { category ->
                val isActive = selectedCategory?.id == category.id
                Button(
                    onClick = {
                        selectedCategory = category
                        viewModel.selectCategory(category)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                        contentColor = if (isActive) Color.Black else MaterialTheme.colorScheme.onSurface
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(category.name, fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Featured Title
        Text(
            text = "Restaurantes em Destaque",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Restaurants List
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (restaurantsList.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Nenhum restaurante encontrado.",
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                        )
                    }
                }
            } else {
                items(restaurantsList) { restaurant ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                            .clickable { onRestaurantSelect(restaurant) },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = restaurant.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = restaurant.description,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = restaurant.category,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "Nota: ${restaurant.rating} / 5",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = restaurant.deliveryTime,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                                Text(
                                    text = if (restaurant.deliveryFee == 0.0) "Grátis" else viewModel.formatPrice(restaurant.deliveryFee),
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RestaurantMenuScreen(
    viewModel: FoodExpressViewModel,
    restaurant: Restaurant?,
    onProductSelect: (Product) -> Unit
) {
    if (restaurant == null) return

    val products = remember(restaurant.id) {
        viewModel.getProductsForRestaurant(restaurant.id)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Restaurant Details Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp)),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = restaurant.name,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = restaurant.description,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Categoria: ${restaurant.category}  |  Nota: ${restaurant.rating} / 5  |  Entrega: ${restaurant.deliveryTime}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Pratos Disponíveis",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Product Cards List
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(products) { product ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                        .clickable { onProductSelect(product) },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = product.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = product.description,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = viewModel.formatPrice(product.price),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Button(
                            onClick = { onProductSelect(product) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Ver", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProductDetailScreen(
    viewModel: FoodExpressViewModel,
    onAddedToCart: () -> Unit
) {
    val product = viewModel.selectedProduct ?: return
    val context = LocalContext.current
    var quantity by remember { mutableIntStateOf(1) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp)),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = product.name,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = product.description,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Preço: " + viewModel.formatPrice(product.price),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Avaliação: ${product.rating} / 5 (${product.reviewsCount} avaliações)",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Quantity selector
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("Qtd:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                    ) {
                        Button(
                            onClick = { if (quantity > 1) quantity-- },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = MaterialTheme.colorScheme.onSurface),
                            contentPadding = PaddingValues(0.dp),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Text("-", fontSize = 16.sp)
                        }
                        Text(
                            text = quantity.toString(),
                            modifier = Modifier.padding(horizontal = 12.dp),
                            fontWeight = FontWeight.Bold
                        )
                        Button(
                            onClick = { quantity++ },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = MaterialTheme.colorScheme.onSurface),
                            contentPadding = PaddingValues(0.dp),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Text("+", fontSize = 16.sp)
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    Button(
                        onClick = {
                            viewModel.addToCart(product, quantity)
                            Toast.makeText(context, "$quantity x ${product.name} adicionado!", Toast.LENGTH_SHORT).show()
                            onAddedToCart()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Adicionar", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Avaliações de Clientes",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(10.dp))

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            viewModel.selectedProductReviews.forEach { review ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "${review.userName}  -  Nota: ${review.rating} / 5 (${review.date})",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = review.comment,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileScreen(
    viewModel: FoodExpressViewModel,
    darkMode: Boolean,
    onThemeToggle: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Meu Perfil", fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Carrinho", fontWeight = FontWeight.Bold) }
            )
        }

        if (selectedTab == 0) {
            ProfileDetailsTab(viewModel = viewModel, darkMode = darkMode, onThemeToggle = onThemeToggle)
        } else {
            CartTab(viewModel = viewModel)
        }
    }
}

@Composable
fun ProfileDetailsTab(
    viewModel: FoodExpressViewModel,
    darkMode: Boolean,
    onThemeToggle: () -> Unit
) {
    var address by remember { mutableStateOf(viewModel.profile.address) }
    var zipCode by remember { mutableStateOf(viewModel.profile.zipCode) }
    var cardDigits by remember { mutableStateOf("") }
    var cardBrand by remember { mutableStateOf("") }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Personal Details Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp)),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Perfil", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Cliente: ${viewModel.profile.name}", color = MaterialTheme.colorScheme.onSurface)
                Text("E-mail: ${viewModel.profile.email}", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                Text("Telefone: ${viewModel.profile.phone}", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            }
        }

        // Toggles Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp)),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Configurações", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Notificações Push", color = MaterialTheme.colorScheme.onSurface)
                    Switch(
                        checked = viewModel.notificationsEnabled,
                        onCheckedChange = { viewModel.toggleNotifications() }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Tema Escuro", color = MaterialTheme.colorScheme.onSurface)
                    Switch(
                        checked = darkMode,
                        onCheckedChange = { onThemeToggle() }
                    )
                }
            }
        }

        // Address Editor Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp)),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Endereço de Entrega", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Endereço") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = zipCode,
                    onValueChange = { zipCode = it },
                    label = { Text("CEP") },
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = {
                        if (address.isNotEmpty() && zipCode.isNotEmpty()) {
                            viewModel.updateAddress(address, zipCode)
                            Toast.makeText(context, "Endereço atualizado!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Os campos não podem ser vazios.", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.align(Alignment.End),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = Color.Black),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Salvar Endereço")
                }
            }
        }

        // Saved Cards & Add Form Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp)),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Formas de Pagamento", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                
                viewModel.paymentMethods.forEach { method ->
                    val brandText = if (method.lastFour.isNotEmpty()) " (${method.cardBrand} final ${method.lastFour})" else ""
                    Text("- ${method.type}$brandText", color = MaterialTheme.colorScheme.onSurface)
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text("Cadastrar Novo Cartão:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = cardDigits,
                        onValueChange = { cardDigits = it },
                        label = { Text("4 digitos") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = cardBrand,
                        onValueChange = { cardBrand = it },
                        label = { Text("Bandeira") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Button(
                    onClick = {
                        if (cardDigits.length == 4 && cardDigits.toIntOrNull() != null && cardBrand.isNotEmpty()) {
                            viewModel.addPaymentMethod("Cartão de Crédito", cardDigits, cardBrand)
                            Toast.makeText(context, "Cartão cadastrado!", Toast.LENGTH_SHORT).show()
                            cardDigits = ""
                            cardBrand = ""
                        } else {
                            Toast.makeText(context, "Informe 4 dígitos e a bandeira.", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.align(Alignment.End),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = Color.Black),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Adicionar Cartão")
                }
            }
        }

        // Order History Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp)),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Histórico de Pedidos", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(8.dp))
                
                if (viewModel.orders.isEmpty()) {
                    Text("Nenhum pedido anterior.", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                } else {
                    viewModel.orders.forEach { order ->
                        Text(
                            text = "[${order.id}] ${order.restaurantName} - Total: ${viewModel.formatPrice(order.total)} (${order.status})",
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CartTab(viewModel: FoodExpressViewModel) {
    val cartList = remember { viewModel.cart }
    val context = LocalContext.current
    var triggerRebuild by remember { mutableStateOf(false) }

    // Dummy usage to trigger recomposition when cart changes
    val cartTotal = remember(cartList.size, triggerRebuild) { viewModel.getCartTotal() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        if (cartList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text("Seu carrinho está vazio.", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(cartList) { item ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp)),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.product.name, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                Text("Preço un: ${viewModel.formatPrice(item.product.price)}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(6.dp))
                                ) {
                                    Button(
                                        onClick = {
                                            if (item.quantity > 1) {
                                                viewModel.updateCartItemQuantity(item.product, item.quantity - 1)
                                                triggerRebuild = !triggerRebuild
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = MaterialTheme.colorScheme.onSurface),
                                        contentPadding = PaddingValues(0.dp),
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Text("-", fontSize = 14.sp)
                                    }
                                    Text(
                                        text = item.quantity.toString(),
                                        modifier = Modifier.padding(horizontal = 8.dp),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                    Button(
                                        onClick = {
                                            viewModel.updateCartItemQuantity(item.product, item.quantity + 1)
                                            triggerRebuild = !triggerRebuild
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = MaterialTheme.colorScheme.onSurface),
                                        contentPadding = PaddingValues(0.dp),
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Text("+", fontSize = 14.sp)
                                    }
                                }

                                Button(
                                    onClick = {
                                        viewModel.removeFromCart(item.product)
                                        triggerRebuild = !triggerRebuild
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error, contentColor = MaterialTheme.colorScheme.onError),
                                    shape = RoundedCornerShape(6.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    Text("Remover", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Checkout Footer
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outline), RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Total: " + viewModel.formatPrice(cartTotal),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Button(
                    onClick = {
                        val order = viewModel.placeOrder()
                        if (order != null) {
                            Toast.makeText(context, "Pedido realizado! Total: ${viewModel.formatPrice(order.total)}", Toast.LENGTH_LONG).show()
                            triggerRebuild = !triggerRebuild
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Finalizar Pedido", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
