package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.entity.ShoppingList
import com.example.data.model.ShoppingListSummary
import com.example.ui.PriceMapperViewModel
import com.example.ui.components.CreateShoppingListDialog
import com.example.ui.components.formatCurrency
import com.example.ui.components.formatDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingListsScreen(
    viewModel: PriceMapperViewModel,
    onOpenListDetail: (Int) -> Unit
) {
    val lists by viewModel.shoppingLists.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Listas de Compras",
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showCreateDialog = true },
                icon = { Icon(Icons.Default.Add, contentDescription = "Nova Lista") },
                text = { Text("Nova Lista Preditiva") },
                modifier = Modifier.testTag("fab_create_list")
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            if (lists.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ShoppingCart,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                        )
                        Text(
                            text = "Nenhuma lista criada ainda",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Crie sua primeira lista para calcular estimativas e acompanhar preços!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Button(
                            onClick = { showCreateDialog = true },
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            Text("Criar Lista Preditiva")
                        }
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    items(lists, key = { it.shoppingList.id }) { summary ->
                        ShoppingListCard(
                            summary = summary,
                            onClick = {
                                viewModel.selectList(summary.shoppingList.id)
                                onOpenListDetail(summary.shoppingList.id)
                            },
                            onDelete = { viewModel.deleteShoppingList(summary) }
                        )
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        CreateShoppingListDialog(
            onSave = { nome, estrategia ->
                viewModel.createShoppingList(nome, estrategia)
                showCreateDialog = false
                viewModel.selectedListId.value?.let { id -> onOpenListDetail(id) }
            },
            onDismiss = { showCreateDialog = false }
        )
    }
}

@Composable
fun ShoppingListCard(
    summary: ShoppingListSummary,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val isComprando = summary.shoppingList.status == ShoppingList.STATUS_COMPRANDO
    val isFinalizada = summary.shoppingList.status == ShoppingList.STATUS_FINALIZADA

    val cardContainerColor = when {
        isComprando -> MaterialTheme.colorScheme.primaryContainer
        isFinalizada -> MaterialTheme.colorScheme.surfaceVariant
        else -> MaterialTheme.colorScheme.surface
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = cardContainerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isComprando) 4.dp else 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("list_card_${summary.shoppingList.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = summary.shoppingList.nomeLista,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Criada em ${formatDate(summary.shoppingList.dataCriacao)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Status Badge
                AssistChip(
                    onClick = { onClick() },
                    label = {
                        Text(
                            when (summary.shoppingList.status) {
                                ShoppingList.STATUS_COMPRANDO -> "EM COMPRA"
                                ShoppingList.STATUS_FINALIZADA -> "FINALIZADA"
                                else -> "ABERTA"
                            },
                            fontWeight = FontWeight.Bold
                        )
                    },
                    leadingIcon = {
                        if (isComprando) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = when {
                            isComprando -> MaterialTheme.colorScheme.primary
                            isFinalizada -> MaterialTheme.colorScheme.outlineVariant
                            else -> MaterialTheme.colorScheme.secondaryContainer
                        },
                        labelColor = when {
                            isComprando -> MaterialTheme.colorScheme.onPrimary
                            isFinalizada -> MaterialTheme.colorScheme.onSurfaceVariant
                            else -> MaterialTheme.colorScheme.onSecondaryContainer
                        }
                    )
                )
            }

            summary.nomeLoja?.let { storeName ->
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Storefront,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = storeName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "${summary.itemCount} itens (${summary.itemsCompradosCount} no carrinho)",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = if (isFinalizada || summary.totalReal > 0)
                            "Gasto Real: ${formatCurrency(summary.totalReal)}"
                        else
                            "Estimado: ${formatCurrency(summary.totalEstimado)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.testTag("delete_list_button_${summary.shoppingList.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Excluir Lista",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}
