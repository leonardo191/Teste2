package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.*
import com.example.data.entity.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        Store::class,
        Product::class,
        PriceHistory::class,
        ShoppingList::class,
        ListItem::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun storeDao(): StoreDao
    abstract fun productDao(): ProductDao
    abstract fun priceHistoryDao(): PriceHistoryDao
    abstract fun shoppingListDao(): ShoppingListDao
    abstract fun listItemDao(): ListItemDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "mapeador_precos_database"
                )
                .addCallback(DatabaseCallback(scope))
                .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateDatabase(database)
                    }
                }
            }
        }

        suspend fun populateDatabase(db: AppDatabase) {
            val storeDao = db.storeDao()
            val productDao = db.productDao()
            val historyDao = db.priceHistoryDao()
            val listDao = db.shoppingListDao()
            val itemDao = db.listItemDao()

            if (storeDao.getStoreCount() > 0) return

            // Seed Stores
            val store1Id = storeDao.insertStore(Store(nomeLoja = "Supermercado Extra", localizacao = "Centro")).toInt()
            val store2Id = storeDao.insertStore(Store(nomeLoja = "Atacadão", localizacao = "Zona Sul")).toInt()
            val store3Id = storeDao.insertStore(Store(nomeLoja = "Mercado do Bairro", localizacao = "Vila Nova")).toInt()

            // Seed Products
            val p1 = productDao.insertProduct(Product(nomeProduto = "Leite Integral 1L", categoria = "Laticínios", codigoBarras = "7891000100111")).toInt()
            val p2 = productDao.insertProduct(Product(nomeProduto = "Café Torrado 500g", categoria = "Mercearia", codigoBarras = "7891000100222")).toInt()
            val p3 = productDao.insertProduct(Product(nomeProduto = "Arroz Tipo 1 5kg", categoria = "Mercearia", codigoBarras = "7891000100333")).toInt()
            val p4 = productDao.insertProduct(Product(nomeProduto = "Feijão Carioca 1kg", categoria = "Mercearia", codigoBarras = "7891000100444")).toInt()
            val p5 = productDao.insertProduct(Product(nomeProduto = "Açúcar Refinado 1kg", categoria = "Mercearia", codigoBarras = "7891000100555")).toInt()
            val p6 = productDao.insertProduct(Product(nomeProduto = "Óleo de Soja 900ml", categoria = "Mercearia", codigoBarras = "7891000100666")).toInt()
            val p7 = productDao.insertProduct(Product(nomeProduto = "Sabão em Pó 1kg", categoria = "Limpeza", codigoBarras = "7891000100777")).toInt()

            val now = System.currentTimeMillis()
            val oneDay = 86400000L

            // Seed Price Histories
            // Leite
            historyDao.insertPriceHistory(PriceHistory(produtoId = p1, lojaId = store1Id, precoPago = 4.89, dataCompra = now - 14 * oneDay))
            historyDao.insertPriceHistory(PriceHistory(produtoId = p1, lojaId = store2Id, precoPago = 4.50, dataCompra = now - 7 * oneDay))
            historyDao.insertPriceHistory(PriceHistory(produtoId = p1, lojaId = store1Id, precoPago = 5.20, dataCompra = now - 2 * oneDay))

            // Café
            historyDao.insertPriceHistory(PriceHistory(produtoId = p2, lojaId = store1Id, precoPago = 16.90, dataCompra = now - 14 * oneDay))
            historyDao.insertPriceHistory(PriceHistory(produtoId = p2, lojaId = store2Id, precoPago = 14.50, dataCompra = now - 5 * oneDay))

            // Arroz
            historyDao.insertPriceHistory(PriceHistory(produtoId = p3, lojaId = store2Id, precoPago = 24.90, dataCompra = now - 10 * oneDay))
            historyDao.insertPriceHistory(PriceHistory(produtoId = p3, lojaId = store3Id, precoPago = 27.50, dataCompra = now - 3 * oneDay))

            // Feijão
            historyDao.insertPriceHistory(PriceHistory(produtoId = p4, lojaId = store1Id, precoPago = 7.90, dataCompra = now - 12 * oneDay))
            historyDao.insertPriceHistory(PriceHistory(produtoId = p4, lojaId = store2Id, precoPago = 6.80, dataCompra = now - 4 * oneDay))

            // Açúcar
            historyDao.insertPriceHistory(PriceHistory(produtoId = p5, lojaId = store1Id, precoPago = 4.20, dataCompra = now - 10 * oneDay))

            // Seed Sample Shopping List
            val listId = listDao.insertShoppingList(
                ShoppingList(
                    nomeLista = "Compra Semanal Exemplo",
                    dataCriacao = now - oneDay,
                    status = ShoppingList.STATUS_ABERTA,
                    estrategiaEstimativa = ShoppingList.ESTRATEGIA_ULTIMO_PRECO
                )
            ).toInt()

            itemDao.insertListItem(ListItem(listaId = listId, produtoId = p1, quantidadeDesejada = 6.0, precoEstimadoUnidade = 5.20))
            itemDao.insertListItem(ListItem(listaId = listId, produtoId = p2, quantidadeDesejada = 2.0, precoEstimadoUnidade = 14.50))
            itemDao.insertListItem(ListItem(listaId = listId, produtoId = p3, quantidadeDesejada = 1.0, precoEstimadoUnidade = 27.50))
            itemDao.insertListItem(ListItem(listaId = listId, produtoId = p7, quantidadeDesejada = 1.0, precoEstimadoUnidade = 0.0)) // Pendente!
        }
    }
}
