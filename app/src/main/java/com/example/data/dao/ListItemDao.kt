package com.example.data.dao

import androidx.room.*
import com.example.data.entity.ListItem
import kotlinx.coroutines.flow.Flow

@Dao
interface ListItemDao {
    @Query("SELECT * FROM itens_lista WHERE listaId = :listId")
    fun getItemsForList(listId: Int): Flow<List<ListItem>>

    @Query("SELECT * FROM itens_lista WHERE listaId = :listId")
    suspend fun getItemsForListSync(listId: Int): List<ListItem>

    @Query("SELECT * FROM itens_lista WHERE id = :itemId")
    suspend fun getItemById(itemId: Int): ListItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertListItem(listItem: ListItem): Long

    @Update
    suspend fun updateListItem(listItem: ListItem)

    @Delete
    suspend fun deleteListItem(listItem: ListItem)

    @Query("UPDATE itens_lista SET comprado = :comprado, precoRealUnidade = :precoReal WHERE id = :itemId")
    suspend fun updateItemPurchasedState(itemId: Int, comprado: Boolean, precoReal: Double?)

    @Query("DELETE FROM itens_lista WHERE listaId = :listId")
    suspend fun deleteItemsForList(listId: Int)

    @Query("SELECT * FROM itens_lista")
    suspend fun getAllRawListItems(): List<ListItem>
}
