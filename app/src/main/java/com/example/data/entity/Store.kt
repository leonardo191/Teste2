package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "lojas")
data class Store(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val nomeLoja: String,
    val localizacao: String? = null
)
