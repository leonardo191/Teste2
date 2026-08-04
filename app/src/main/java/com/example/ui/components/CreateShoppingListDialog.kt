package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.example.data.entity.ShoppingList

@Composable
fun CreateShoppingListDialog(
    onSave: (nome: String, estrategia: String) -> Unit,
    onDismiss: () -> Unit
) {
    var nome by remember { mutableStateOf("") }
    var estrategia by remember { mutableStateOf(ShoppingList.ESTRATEGIA_ULTIMO_PRECO) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Nova Lista de Compras")
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = nome,
                    onValueChange = { nome = it },
                    label = { Text("Nome da Lista *") },
                    placeholder = { Text("Ex: Compra da Semana, Feira de Sábado") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_list_name")
                )

                Text(
                    text = "Base de Cálculo de Estimativa:",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Column(modifier = Modifier.selectableGroup()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = (estrategia == ShoppingList.ESTRATEGIA_ULTIMO_PRECO),
                                onClick = { estrategia = ShoppingList.ESTRATEGIA_ULTIMO_PRECO },
                                role = Role.RadioButton
                            )
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (estrategia == ShoppingList.ESTRATEGIA_ULTIMO_PRECO),
                            onClick = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("Último Preço Pago (Padrão)", style = MaterialTheme.typography.bodyMedium)
                            Text("Utiliza o valor mais recente do histórico", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = (estrategia == ShoppingList.ESTRATEGIA_MEDIA),
                                onClick = { estrategia = ShoppingList.ESTRATEGIA_MEDIA },
                                role = Role.RadioButton
                            )
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (estrategia == ShoppingList.ESTRATEGIA_MEDIA),
                            onClick = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("Média de Preços", style = MaterialTheme.typography.bodyMedium)
                            Text("Calcula a média de todas as compras já feitas", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(nome, estrategia) },
                enabled = nome.isNotBlank(),
                modifier = Modifier.testTag("save_list_button")
            ) {
                Text("Criar Lista")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("cancel_list_button")
            ) {
                Text("Cancelar")
            }
        }
    )
}
