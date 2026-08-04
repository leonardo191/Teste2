package com.example.data.dao

import androidx.room.*
import com.example.data.entity.PriceHistory
import com.example.data.model.PriceHistoryItem
import kotlinx.coroutines.flow.Flow

@Dao
interface PriceHistoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPriceHistory(priceHistory: PriceHistory): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(priceHistories: List<PriceHistory>)

    @Query("""
        SELECT h.id, h.produtoId, p.nomeProduto, p.categoria, h.lojaId, l.nomeLoja, h.precoPago, h.dataCompra
        FROM historico_precos h
        INNER JOIN produtos p ON h.produtoId = p.id
        INNER JOIN lojas l ON h.lojaId = l.id
        ORDER BY h.dataCompra DESC
    """)
    fun getAllPriceHistory(): Flow<List<PriceHistoryItem>>

    @Query("""
        SELECT h.id, h.produtoId, p.nomeProduto, p.categoria, h.lojaId, l.nomeLoja, h.precoPago, h.dataCompra
        FROM historico_precos h
        INNER JOIN produtos p ON h.produtoId = p.id
        INNER JOIN lojas l ON h.lojaId = l.id
        WHERE h.produtoId = :productId
        ORDER BY h.dataCompra DESC
    """)
    fun getHistoryForProduct(productId: Int): Flow<List<PriceHistoryItem>>

    @Query("SELECT precoPago FROM historico_precos WHERE produtoId = :productId ORDER BY dataCompra DESC LIMIT 1")
    suspend fun getLastPriceForProduct(productId: Int): Double?

    @Query("SELECT AVG(precoPago) FROM historico_precos WHERE produtoId = :productId")
    suspend fun getAveragePriceForProduct(productId: Int): Double?

    @Query("""
        SELECT h.precoPago 
        FROM historico_precos h 
        WHERE h.produtoId = :productId 
        ORDER BY h.precoPago ASC LIMIT 1
    """)
    suspend fun getMinPriceForProduct(productId: Int): Double?

    @Query("""
        SELECT l.nomeLoja 
        FROM historico_precos h 
        INNER JOIN lojas l ON h.lojaId = l.id
        WHERE h.produtoId = :productId AND h.precoPago = (SELECT MIN(precoPago) FROM historico_precos WHERE produtoId = :productId)
        LIMIT 1
    """)
    suspend fun getMinPriceStoreName(productId: Int): String?

    @Query("""
        SELECT h.precoPago 
        FROM historico_precos h 
        WHERE h.produtoId = :productId 
        ORDER BY h.precoPago DESC LIMIT 1
    """)
    suspend fun getMaxPriceForProduct(productId: Int): Double?

    @Query("""
        SELECT l.nomeLoja 
        FROM historico_precos h 
        INNER JOIN lojas l ON h.lojaId = l.id
        WHERE h.produtoId = :productId AND h.precoPago = (SELECT MAX(precoPago) FROM historico_precos WHERE produtoId = :productId)
        LIMIT 1
    """)
    suspend fun getMaxPriceStoreName(productId: Int): String?

    @Query("SELECT COUNT(*) FROM historico_precos WHERE produtoId = :productId")
    suspend fun getHistoryCountForProduct(productId: Int): Int

    @Delete
    suspend fun deletePriceHistory(priceHistory: PriceHistory)

    @Query("SELECT * FROM historico_precos")
    suspend fun getAllRawHistory(): List<PriceHistory>
}
