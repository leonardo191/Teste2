package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.entity.Store

@Composable
fun SelectStoreForShoppingDialog(
    stores: List<Store>,
    onSelectStore: (Int) -> Unit,
    onQuickAddStore: (nome: String) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedStoreId by remember { mutableStateOf<Int?>(stores.firstOrNull()?.id) }
    var showQuickAdd by remember { mutableStateOf(false) }
    var newStoreName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Storefront,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Onde você está comprando?")
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Selecione o mercado para vincular os preços aos registros de histórico:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (showQuickAdd) {
                    OutlinedTextField(
                        value = newStoreName,
                        onValueChange = { newStoreName = it },
                        label = { Text("Nome da Nova Loja") },
                        placeholder = { Text("Ex: Supermercado Carrefour") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Button(
                        onClick = {
                            if (newStoreName.isNotBlank()) {
                                onQuickAddStore(newStoreName)
                                showQuickAdd = false
                                newStoreName = ""
                            }
                        },
                        enabled = newStoreName.isNotBlank(),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Cadastrar Loja")
                    }
                } else {
                    if (stores.isEmpty()) {
                        Text("Nenhuma loja cadastrada ainda.", style = MaterialTheme.typography.bodyMedium)
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 200.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            items(stores, key = { it.id }) { store ->
                                Surface(
                                    shape = MaterialTheme.shapes.small,
                                    color = if (selectedStoreId == store.id) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { selectedStoreId = store.id }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        RadioButton(
                                            selected = (selectedStoreId == store.id),
                                            onClick = { selectedStoreId = store.id }
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = store.nomeLoja,
                                                style = MaterialTheme.typography.bodyLarge,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            store.localizacao?.let {
                                                Text(
                                                    text = it,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    TextButton(
                        onClick = { showQuickAdd = true },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Cadastrar Nova Loja")
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { selectedStoreId?.let { onSelectStore(it) } },
                enabled = selectedStoreId != null,
                modifier = Modifier.testTag("start_shopping_confirm_button")
            ) {
                Text("Iniciar Compra")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("cancel_start_shopping_button")
            ) {
                Text("Cancelar")
            }
        }
    )
}
