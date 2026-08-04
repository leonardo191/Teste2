package com.example.data.dao

import androidx.room.*
import com.example.data.entity.Product
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Query("SELECT * FROM produtos ORDER BY nomeProduto ASC")
    fun getAllProducts(): Flow<List<Product>>

    @Query("SELECT * FROM produtos WHERE id = :id")
    suspend fun getProductById(id: Int): Product?

    @Query("SELECT * FROM produtos WHERE nomeProduto LIKE '%' || :query || '%' OR categoria LIKE '%' || :query || '%' OR codigoBarras = :query ORDER BY nomeProduto ASC")
    fun searchProducts(query: String): Flow<List<Product>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: Product): Long

    @Update
    suspend fun updateProduct(product: Product)

    @Delete
    suspend fun deleteProduct(product: Product)

    @Query("SELECT DISTINCT categoria FROM produtos WHERE categoria IS NOT NULL AND categoria != '' ORDER BY categoria ASC")
    fun getAllCategories(): Flow<List<String>>

    @Query("SELECT COUNT(*) FROM produtos")
    suspend fun getProductCount(): Int
}
