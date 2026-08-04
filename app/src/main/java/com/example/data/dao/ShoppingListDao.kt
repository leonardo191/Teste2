package com.example.data.dao

import androidx.room.*
import com.example.data.entity.ShoppingList
import kotlinx.coroutines.flow.Flow

@Dao
interface ShoppingListDao {
    @Query("SELECT * FROM listas_compras ORDER BY dataCriacao DESC")
    fun getAllShoppingLists(): Flow<List<ShoppingList>>

    @Query("SELECT * FROM listas_compras WHERE id = :id")
    fun getShoppingListByIdFlow(id: Int): Flow<ShoppingList?>

    @Query("SELECT * FROM listas_compras WHERE id = :id")
    suspend fun getShoppingListById(id: Int): ShoppingList?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShoppingList(shoppingList: ShoppingList): Long

    @Update
    suspend fun updateShoppingList(shoppingList: ShoppingList)

    @Delete
    suspend fun deleteShoppingList(shoppingList: ShoppingList)

    @Query("UPDATE listas_compras SET status = :status, lojaId = :lojaId WHERE id = :listId")
    suspend fun updateListStatusAndStore(listId: Int, status: String, lojaId: Int?)

    @Query("SELECT * FROM listas_compras")
    suspend fun getAllRawLists(): List<ShoppingList>
}
