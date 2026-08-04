package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.entity.Product
import com.example.data.entity.ShoppingList
import com.example.data.model.ListItemWithProduct
import com.example.ui.PriceMapperViewModel
import com.example.ui.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListDetailScreen(
    viewModel: PriceMapperViewModel,
    onBack: () -> Unit
) {
    val shoppingList by viewModel.selectedList.collectAsState()
    val items by viewModel.selectedListItems.collectAsState()
    val stores by viewModel.stores.collectAsState()
    val availableProducts by viewModel.products.collectAsState()
    val activeCheckItem by viewModel.activeCheckItem.collectAsState()

    var showAddProductSheet by remember { mutableStateOf(false) }
    var showSelectStoreDialog by remember { mutableStateOf(false) }
    var showFinishConfirmDialog by remember { mutableStateOf(false) }

    val list = shoppingList ?: return

    val totalEstimado = items.sumOf { it.totalEstimado }
    val totalRealCarrinho = items.filter { it.listItem.comprado }.sumOf { it.totalReal ?: it.totalEstimado }
    val compradosCount = items.count { it.listItem.comprado }

    val isComprando = list.status == ShoppingList.STATUS_COMPRANDO
    val isFinalizada = list.status == ShoppingList.STATUS_FINALIZADA

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(list.nomeLista, fontWeight = FontWeight.Bold)
                        Text(
                            text = when (list.status) {
                                ShoppingList.STATUS_COMPRANDO -> "Modo Comprando no Mercado"
                                ShoppingList.STATUS_FINALIZADA -> "Lista Finalizada"
                                else -> "Lista Aberta (Preditiva)"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
                actions = {
                    // Estimation mode switch menu
                    var showMenu by remember { mutableStateOf(false) }
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Opções")
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Usar Último Preço Pago") },
                            onClick = {
                                viewModel.updateListEstimationStrategy(list.id, ShoppingList.ESTRATEGIA_ULTIMO_PRECO)
                                showMenu = false
                            },
                            leadingIcon = {
                                if (list.estrategiaEstimativa == ShoppingList.ESTRATEGIA_ULTIMO_PRECO) {
                                    Icon(Icons.Default.Check, contentDescription = null)
                                }
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Usar Média de Preços") },
                            onClick = {
                                viewModel.updateListEstimationStrategy(list.id, ShoppingList.ESTRATEGIA_MEDIA)
                                showMenu = false
                            },
                            leadingIcon = {
                                if (list.estrategiaEstimativa == ShoppingList.ESTRATEGIA_MEDIA) {
                                    Icon(Icons.Default.Check, contentDescription = null)
                                }
                            }
                        )
                    }
                }
            )
        },
        bottomBar = {
            // Sticky Bottom Total Card & Primary Action
            Surface(
                tonalElevation = 8.dp,
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (isComprando) "Total no Carrinho" else "Total Estimado da Compra",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = if (isComprando) formatCurrency(totalRealCarrinho) else formatCurrency(totalEstimado),
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            if (isComprando) {
                                Text(
                                    text = "Estimado inicial: ${formatCurrency(totalEstimado)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        if (!isFinalizada) {
                            if (isComprando) {
                                Button(
                                    onClick = { showFinishConfirmDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                    modifier = Modifier.testTag("finish_shopping_button")
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Finalizar Compra")
                                }
                            } else {
                                Button(
                                    onClick = { showSelectStoreDialog = true },
                                    enabled = items.isNotEmpty(),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                                    modifier = Modifier.testTag("start_shopping_button")
                                ) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Iniciar Compra")
                                }
                            }
                        }
                    }
                }
            }
        },
        floatingActionButton = {
            if (!isFinalizada) {
                FloatingActionButton(
                    onClick = { showAddProductSheet = true },
                    modifier = Modifier.testTag("fab_add_item_to_list")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Adicionar Produto")
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            // Progress / Status Banner
            if (isComprando) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Progresso: $compradosCount de ${items.size} itens",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "${if (items.isNotEmpty()) (compradosCount * 100 / items.size) else 0}%",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { if (items.isNotEmpty()) compradosCount.toFloat() / items.size else 0f },
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Toque em um item para colocar no carrinho e atualizar o preço!",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }
            } else if (!isFinalizada) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Calculate,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Estimativa baseada em: ${if (list.estrategiaEstimativa == ShoppingList.ESTRATEGIA_MEDIA) "Média de Preços" else "Último Preço Pago"}",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Text(
                                text = "O valor total é recalculado automaticamente a cada item inserido.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }

            if (items.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlaylistAdd,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                        )
                        Text("Sua lista está vazia", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "Toque no botão '+' para adicionar produtos.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(items, key = { it.listItem.id }) { itemWithProduct ->
                        ListItemCard(
                            itemWithProduct = itemWithProduct,
                            isComprando = isComprando,
                            isFinalizada = isFinalizada,
                            onToggleComprado = {
                                if (itemWithProduct.listItem.comprado) {
                                    viewModel.uncheckItem(itemWithProduct)
                                } else {
                                    viewModel.openCheckItemDialog(itemWithProduct)
                                }
                            },
                            onQuantityChange = { delta ->
                                val newQty = itemWithProduct.listItem.quantidadeDesejada + delta
                                viewModel.updateListItemQuantity(itemWithProduct.listItem, newQty)
                            },
                            onDelete = { viewModel.deleteListItem(itemWithProduct.listItem) }
                        )
                    }
                }
            }
        }
    }

    // Modal Sheet to Add Product to List
    if (showAddProductSheet) {
        AddProductToListSheet(
            products = availableProducts,
            onSelectProduct = { product, cantidad ->
                viewModel.addProductToActiveList(product.id, cantidad)
                showAddProductSheet = false
            },
            onCreateAndSelectProduct = { nome, cat, cod ->
                viewModel.addProduct(nome, cat, cod) { newProdId ->
                    viewModel.addProductToActiveList(newProdId, 1.0)
                    showAddProductSheet = false
                }
            },
            onDismiss = { showAddProductSheet = false }
        )
    }

    // Select Store Dialog for Starting Shopping
    if (showSelectStoreDialog) {
        SelectStoreForShoppingDialog(
            stores = stores,
            onSelectStore = { storeId ->
                viewModel.startShopping(list.id, storeId)
                showSelectStoreDialog = false
            },
            onQuickAddStore = { nome ->
                viewModel.addStore(nome, null)
            },
            onDismiss = { showSelectStoreDialog = false }
        )
    }

    // Confirm Finish Shopping Dialog
    if (showFinishConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showFinishConfirmDialog = false },
            title = { Text("Finalizar Compra?") },
            text = {
                Text("Ao finalizar, todos os itens marcados no carrinho gerarão novos registros no Histórico de Preços vinculados a esta loja e data!")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.finishShopping(list.id)
                        showFinishConfirmDialog = false
                    },
                    modifier = Modifier.testTag("confirm_finish_shopping_dialog_button")
                ) {
                    Text("Confirmar e Gravar Histórico")
                }
            },
            dismissButton = {
                TextButton(onClick = { showFinishConfirmDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Data Entry Flow Check Item Dialog ("O preço se manteve R$ X?")
    activeCheckItem?.let { checkItem ->
        CheckItemPriceDialog(
            itemWithProduct = checkItem,
            onConfirm = { finalPriceUnit ->
                viewModel.confirmItemPurchased(checkItem, finalPriceUnit)
            },
            onDismiss = { viewModel.dismissCheckItemDialog() }
        )
    }
}

@Composable
fun ListItemCard(
    itemWithProduct: ListItemWithProduct,
    isComprando: Boolean,
    isFinalizada: Boolean,
    onToggleComprado: () -> Unit,
    onQuantityChange: (Double) -> Unit,
    onDelete: () -> Unit
) {
    val isComprado = itemWithProduct.listItem.comprado
    val isPendente = itemWithProduct.isPendente

    Card(
        colors = CardDefaults.cardColors(
            containerColor = when {
                isComprado -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                isPendente -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.4f)
                else -> MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = isComprando) { onToggleComprado() }
            .testTag("list_item_card_${itemWithProduct.listItem.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Checkbox for In-Store mode
            if (isComprando) {
                Checkbox(
                    checked = isComprado,
                    onCheckedChange = { onToggleComprado() },
                    modifier = Modifier.testTag("item_checkbox_${itemWithProduct.listItem.id}")
                )
                Spacer(modifier = Modifier.width(8.dp))
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = itemWithProduct.product.nomeProduto,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                itemWithProduct.product.categoria?.let { cat ->
                    Text(
                        text = cat,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Price display badge
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isPendente) {
                        SuggestionChip(
                            onClick = {},
                            label = { Text("Preço Pendente (R$ 0,00)", style = MaterialTheme.typography.labelSmall) },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                labelColor = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        )
                    } else {
                        val unitPrice = if (isComprado && itemWithProduct.listItem.precoRealUnidade != null)
                            itemWithProduct.listItem.precoRealUnidade
                        else itemWithProduct.listItem.precoEstimadoUnidade

                        Text(
                            text = "${formatCurrency(unitPrice)} / un",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Total: ${formatCurrency((unitPrice ?: 0.0) * itemWithProduct.listItem.quantidadeDesejada)}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Quantity controls
            if (!isFinalizada && !isComprando) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { onQuantityChange(-1.0) },
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("decrease_qty_${itemWithProduct.listItem.id}")
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "Diminuir")
                    }

                    Text(
                        text = "${if (itemWithProduct.listItem.quantidadeDesejada % 1.0 == 0.0) itemWithProduct.listItem.quantidadeDesejada.toInt() else itemWithProduct.listItem.quantidadeDesejada}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp)
                    )

                    IconButton(
                        onClick = { onQuantityChange(1.0) },
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("increase_qty_${itemWithProduct.listItem.id}")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Aumentar")
                    }

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("delete_item_${itemWithProduct.listItem.id}")
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Remover",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductToListSheet(
    products: List<Product>,
    onSelectProduct: (Product, Double) -> Unit,
    onCreateAndSelectProduct: (nome: String, categoria: String?, codigoBarras: String?) -> Unit,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var showCreateProductDialog by remember { mutableStateOf(false) }

    val filteredProducts = products.filter {
        it.nomeProduto.contains(searchQuery, ignoreCase = true) ||
                (it.categoria?.contains(searchQuery, ignoreCase = true) == true)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Adicionar à Lista",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                TextButton(
                    onClick = { showCreateProductDialog = true },
                    modifier = Modifier.testTag("button_quick_create_product")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Novo Produto")
                }
            }

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Buscar produto cadastrado...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .testTag("search_product_input")
            )

            if (filteredProducts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Nenhum produto encontrado.", style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { showCreateProductDialog = true }) {
                            Text("Cadastrar '$searchQuery'")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 350.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(filteredProducts, key = { it.id }) { product ->
                        Surface(
                            shape = MaterialTheme.shapes.medium,
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelectProduct(product, 1.0) }
                                .testTag("select_product_item_${product.id}")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = product.nomeProduto,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    product.categoria?.let { cat ->
                                        Text(
                                            text = cat,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Adicionar",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCreateProductDialog) {
        AddEditProductDialog(
            initialProduct = if (searchQuery.isNotBlank()) Product(nomeProduto = searchQuery) else null,
            onSave = { nome, cat, cod ->
                onCreateAndSelectProduct(nome, cat, cod)
                showCreateProductDialog = false
            },
            onDismiss = { showCreateProductDialog = false }
        )
    }
}
