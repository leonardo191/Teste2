package com.example.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "itens_lista",
    foreignKeys = [
        ForeignKey(
            entity = ShoppingList::class,
            parentColumns = ["id"],
            childColumns = ["listaId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Product::class,
            parentColumns = ["id"],
            childColumns = ["produtoId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("listaId"), Index("produtoId")]
)
data class ListItem(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val listaId: Int,
    val produtoId: Int,
    val quantidadeDesejada: Double = 1.0,
    val precoEstimadoUnidade: Double = 0.0,
    val precoRealUnidade: Double? = null,
    val comprado: Boolean = false
)
