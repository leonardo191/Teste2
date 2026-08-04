package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ListItemWithProduct

@Composable
fun CheckItemPriceDialog(
    itemWithProduct: ListItemWithProduct,
    onConfirm: (Double) -> Unit,
    onDismiss: () -> Unit
) {
    val estimatedPrice = itemWithProduct.listItem.precoEstimadoUnidade
    val initialPrice = itemWithProduct.listItem.precoRealUnidade
        ?: if (estimatedPrice > 0) estimatedPrice else 0.0

    var priceInput by remember { mutableStateOf(if (initialPrice > 0) String.format("%.2f", initialPrice).replace(',', '.') else "") }
    var isEditing by remember { mutableStateOf(estimatedPrice <= 0) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ShoppingCart,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = itemWithProduct.product.nomeProduto,
                    style = MaterialTheme.typography.titleLarge
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Quantidade: ${if (itemWithProduct.listItem.quantidadeDesejada % 1.0 == 0.0) itemWithProduct.listItem.quantidadeDesejada.toInt() else itemWithProduct.listItem.quantidadeDesejada} un.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (estimatedPrice > 0 && !isEditing) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "O preço se manteve R$ ${String.format("%.2f", estimatedPrice)}?",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Subtotal: ${formatCurrency(itemWithProduct.listItem.quantidadeDesejada * estimatedPrice)}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = { isEditing = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("button_edit_price"),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("O preço mudou? Digitar novo valor")
                    }
                } else {
                    OutlinedTextField(
                        value = priceInput,
                        onValueChange = { priceInput = it.replace(',', '.') },
                        label = { Text("Preço Unitário Pago (R$)") },
                        placeholder = { Text("Ex: 5.50") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_unit_price"),
                        shape = MaterialTheme.shapes.medium
                    )

                    val parsedPrice = priceInput.toDoubleOrNull() ?: 0.0
                    if (parsedPrice > 0) {
                        Text(
                            text = "Subtotal calculado: ${formatCurrency(itemWithProduct.listItem.quantidadeDesejada * parsedPrice)}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        },
        confirmButton = {
            if (estimatedPrice > 0 && !isEditing) {
                Button(
                    onClick = { onConfirm(estimatedPrice) },
                    modifier = Modifier.testTag("confirm_same_price_button")
                ) {
                    Icon(imageVector = Icons.Default.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Sim, confirmar ${formatCurrency(estimatedPrice)}")
                }
            } else {
                val parsedPrice = priceInput.toDoubleOrNull() ?: 0.0
                Button(
                    onClick = { onConfirm(parsedPrice) },
                    enabled = parsedPrice > 0,
                    modifier = Modifier.testTag("confirm_custom_price_button")
                ) {
                    Text("Confirmar ${formatCurrency(parsedPrice)}")
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("cancel_check_button")
            ) {
                Text("Cancelar")
            }
        }
    )
}
