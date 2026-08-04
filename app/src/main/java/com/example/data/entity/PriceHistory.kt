package com.example.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "historico_precos",
    foreignKeys = [
        ForeignKey(
            entity = Product::class,
            parentColumns = ["id"],
            childColumns = ["produtoId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Store::class,
            parentColumns = ["id"],
            childColumns = ["lojaId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("produtoId"), Index("lojaId")]
)
data class PriceHistory(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val produtoId: Int,
    val lojaId: Int,
    val precoPago: Double,
    val dataCompra: Long = System.currentTimeMillis()
)
