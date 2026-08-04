package com.example.data.model

import com.example.data.entity.ListItem
import com.example.data.entity.Product
import com.example.data.entity.ShoppingList

data class ListItemWithProduct(
    val listItem: ListItem,
    val product: Product,
    val lastPrice: Double?,
    val avgPrice: Double?
) {
    val totalEstimado: Double
        get() = listItem.quantidadeDesejada * listItem.precoEstimadoUnidade

    val totalReal: Double?
        get() = listItem.precoRealUnidade?.let { it * listItem.quantidadeDesejada }

    val isPendente: Boolean
        get() = listItem.precoEstimadoUnidade <= 0.0 && (listItem.precoRealUnidade == null || listItem.precoRealUnidade <= 0.0)
}

data class ProductStats(
    val product: Product,
    val lastPrice: Double?,
    val avgPrice: Double?,
    val minPrice: Double?,
    val minPriceStoreName: String?,
    val maxPrice: Double?,
    val maxPriceStoreName: String?,
    val totalRecords: Int
)

data class PriceHistoryItem(
    val id: Int,
    val produtoId: Int,
    val nomeProduto: String,
    val categoria: String?,
    val lojaId: Int,
    val nomeLoja: String,
    val precoPago: Double,
    val dataCompra: Long
)

data class ShoppingListSummary(
    val shoppingList: ShoppingList,
    val nomeLoja: String?,
    val itemCount: Int,
    val itemsCompradosCount: Int,
    val totalEstimado: Double,
    val totalReal: Double
)
