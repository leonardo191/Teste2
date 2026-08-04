package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "produtos")
data class Product(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val nomeProduto: String,
    val codigoBarras: String? = null,
    val categoria: String? = null
)
