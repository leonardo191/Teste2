package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.entity.Product
import com.example.ui.PriceMapperViewModel
import com.example.ui.components.AddEditProductDialog
import com.example.ui.components.ProductStatsBottomSheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductsScreen(
    viewModel: PriceMapperViewModel
) {
    val products by viewModel.products.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val stores by viewModel.stores.collectAsState()

    val selectedProductStats by viewModel.selectedProductStats.collectAsState()
    val selectedProductHistory by viewModel.selectedProductHistory.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var productToEdit by remember { mutableStateOf<Product?>(null) }
    var productForManualPrice by remember { mutableStateOf<Product?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cadastro de Produtos", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddDialog = true },
                icon = { Icon(Icons.Default.Add, contentDescription = "Novo Produto") },
                text = { Text("Novo Produto") },
                modifier = Modifier.testTag("fab_add_product")
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            // Search Input
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                placeholder = { Text("Buscar por nome, categoria ou código...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setSearchQuery("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Limpar")
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .testTag("search_products_input")
            )

            // Category Filter Chips
            if (categories.isNotEmpty()) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = selectedCategory == null,
                            onClick = { viewModel.setSelectedCategory(null) },
                            label = { Text("Todos") },
                            modifier = Modifier.testTag("category_chip_all")
                        )
                    }
                    items(categories) { category ->
                        FilterChip(
                            selected = selectedCategory == category,
                            onClick = { viewModel.setSelectedCategory(category) },
                            label = { Text(category) },
                            modifier = Modifier.testTag("category_chip_$category")
                        )
                    }
                }
            }

            if (products.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Inventory2,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                        )
                        Text("Nenhum produto cadastrado", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "Cadastre produtos para acompanhar o histórico de preços.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Button(onClick = { showAddDialog = true }) {
                            Text("Cadastrar Produto")
                        }
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(products, key = { it.id }) { product ->
                        ProductCard(
                            product = product,
                            onInspectStats = { viewModel.inspectProductStats(product) },
                            onEdit = { productToEdit = product },
                            onAddManualPrice = { productForManualPrice = product },
                            onDelete = { viewModel.deleteProduct(product) }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddEditProductDialog(
            onSave = { nome, cat, cod ->
                viewModel.addProduct(nome, cat, cod)
                showAddDialog = false
            },
            onDismiss = { showAddDialog = false }
        )
    }

    productToEdit?.let { product ->
        AddEditProductDialog(
            initialProduct = product,
            onSave = { nome, cat, cod ->
                viewModel.updateProduct(product.copy(nomeProduto = nome, categoria = cat, codigoBarras = cod))
                productToEdit = null
            },
            onDismiss = { productToEdit = null }
        )
    }

    // Manual Price Registration Dialog
    productForManualPrice?.let { product ->
        ManualPriceDialog(
            product = product,
            stores = stores,
            onSave = { storeId, price ->
                viewModel.addManualPriceHistory(product.id, storeId, price)
                productForManualPrice = null
            },
            onDismiss = { productForManualPrice = null }
        )
    }

    // Product Stats Sheet
    selectedProductStats?.let { stats ->
        ProductStatsBottomSheet(
            productStats = stats,
            priceHistory = selectedProductHistory,
            onDismiss = { viewModel.closeProductStats() },
            onDeleteHistory = { viewModel.deletePriceHistory(it) }
        )
    }
}

@Composable
fun ProductCard(
    product: Product,
    onInspectStats: () -> Unit,
    onEdit: () -> Unit,
    onAddManualPrice: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onInspectStats() }
            .testTag("product_card_${product.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = product.nomeProduto,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        product.categoria?.let { cat ->
                            SuggestionChip(
                                onClick = { },
                                label = { Text(cat, style = MaterialTheme.typography.labelSmall) },
                                colors = SuggestionChipDefaults.suggestionChipColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                                )
                            )
                        }
                        product.codigoBarras?.let { code ->
                            Text(
                                text = "Cód: $code",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Row {
                    IconButton(
                        onClick = onInspectStats,
                        modifier = Modifier.testTag("inspect_stats_button_${product.id}")
                    ) {
                        Icon(
                            Icons.Default.Analytics,
                            contentDescription = "Ver Estatísticas de Preço",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.testTag("edit_product_button_${product.id}")
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar")
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.testTag("delete_product_button_${product.id}")
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Excluir",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = onAddManualPrice,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("add_manual_price_button_${product.id}")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Lançar Preço Avulso no Histórico")
            }
        }
    }
}

@Composable
fun ManualPriceDialog(
    product: Product,
    stores: List<com.example.data.entity.Store>,
    onSave: (storeId: Int, price: Double) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedStoreId by remember { mutableStateOf(stores.firstOrNull()?.id) }
    var priceText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Registrar Preço: ${product.nomeProduto}") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Selecione a loja e digite o valor do cupom/memória:")

                if (stores.isEmpty()) {
                    Text("Nenhuma loja cadastrada. Cadastre uma loja antes.", color = MaterialTheme.colorScheme.error)
                } else {
                    OutlinedTextField(
                        value = priceText,
                        onValueChange = { priceText = it.replace(',', '.') },
                        label = { Text("Preço Pago (R$)") },
                        placeholder = { Text("Ex: 5.90") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("Loja / Mercado:", style = MaterialTheme.typography.labelLarge)
                    stores.forEach { store ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedStoreId = store.id }
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(
                                selected = selectedStoreId == store.id,
                                onClick = { selectedStoreId = store.id }
                            )
                            Text(store.nomeLoja, modifier = Modifier.padding(start = 8.dp))
                        }
                    }
                }
            }
        },
        confirmButton = {
            val price = priceText.toDoubleOrNull() ?: 0.0
            Button(
                onClick = { selectedStoreId?.let { onSave(it, price) } },
                enabled = selectedStoreId != null && price > 0
            ) {
                Text("Salvar Preço")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
