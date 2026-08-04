package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.entity.Product

@Composable
fun AddEditProductDialog(
    initialProduct: Product? = null,
    onSave: (nome: String, categoria: String?, codigoBarras: String?) -> Unit,
    onDismiss: () -> Unit
) {
    var nome by remember { mutableStateOf(initialProduct?.nomeProduto ?: "") }
    var categoria by remember { mutableStateOf(initialProduct?.categoria ?: "") }
    var codigoBarras by remember { mutableStateOf(initialProduct?.codigoBarras ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (initialProduct == null) "Novo Produto" else "Editar Produto")
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = nome,
                    onValueChange = { nome = it },
                    label = { Text("Nome do Produto *") },
                    placeholder = { Text("Ex: Leite Integral 1L") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_product_name")
                )

                OutlinedTextField(
                    value = categoria,
                    onValueChange = { categoria = it },
                    label = { Text("Categoria (Opcional)") },
                    placeholder = { Text("Ex: Laticínios, Mercearia") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_product_category")
                )

                OutlinedTextField(
                    value = codigoBarras,
                    onValueChange = { codigoBarras = it },
                    label = { Text("Código de Barras (Opcional)") },
                    placeholder = { Text("Ex: 7891000123456") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_product_barcode")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(nome, categoria, codigoBarras) },
                enabled = nome.isNotBlank(),
                modifier = Modifier.testTag("save_product_button")
            ) {
                Text("Salvar")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("cancel_product_button")
            ) {
                Text("Cancelar")
            }
        }
    )
}
