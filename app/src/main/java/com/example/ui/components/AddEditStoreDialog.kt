package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.entity.Store

@Composable
fun AddEditStoreDialog(
    initialStore: Store? = null,
    onSave: (nome: String, localizacao: String?) -> Unit,
    onDismiss: () -> Unit
) {
    var nome by remember { mutableStateOf(initialStore?.nomeLoja ?: "") }
    var localizacao by remember { mutableStateOf(initialStore?.localizacao ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (initialStore == null) "Nova Loja / Mercado" else "Editar Loja")
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = nome,
                    onValueChange = { nome = it },
                    label = { Text("Nome da Loja / Mercado *") },
                    placeholder = { Text("Ex: Supermercado Extra") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_store_name")
                )

                OutlinedTextField(
                    value = localizacao,
                    onValueChange = { localizacao = it },
                    label = { Text("Bairro / Cidade / Identificador (Opcional)") },
                    placeholder = { Text("Ex: Centro, Zona Sul") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_store_location")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(nome, localizacao) },
                enabled = nome.isNotBlank(),
                modifier = Modifier.testTag("save_store_button")
            ) {
                Text("Salvar")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("cancel_store_button")
            ) {
                Text("Cancelar")
            }
        }
    )
}
