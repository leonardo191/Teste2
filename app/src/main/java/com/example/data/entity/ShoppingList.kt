package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "listas_compras")
data class ShoppingList(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val nomeLista: String,
    val dataCriacao: Long = System.currentTimeMillis(),
    val status: String = STATUS_ABERTA, // ABERTA, COMPRANDO, FINALIZADA
    val lojaId: Int? = null,
    val estrategiaEstimativa: String = ESTRATEGIA_ULTIMO_PRECO // ULTIMO_PRECO or MEDIA
) {
    companion object {
        const val STATUS_ABERTA = "ABERTA"
        const val STATUS_COMPRANDO = "COMPRANDO"
        const val STATUS_FINALIZADA = "FINALIZADA"

        const val ESTRATEGIA_ULTIMO_PRECO = "ULTIMO_PRECO"
        const val ESTRATEGIA_MEDIA = "MEDIA"
    }
}
