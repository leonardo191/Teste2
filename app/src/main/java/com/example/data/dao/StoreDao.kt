package com.example.data.dao

import androidx.room.*
import com.example.data.entity.Store
import kotlinx.coroutines.flow.Flow

@Dao
interface StoreDao {
    @Query("SELECT * FROM lojas ORDER BY nomeLoja ASC")
    fun getAllStores(): Flow<List<Store>>

    @Query("SELECT * FROM lojas WHERE id = :id")
    suspend fun getStoreById(id: Int): Store?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStore(store: Store): Long

    @Update
    suspend fun updateStore(store: Store)

    @Delete
    suspend fun deleteStore(store: Store)

    @Query("SELECT COUNT(*) FROM lojas")
    suspend fun getStoreCount(): Int
}
