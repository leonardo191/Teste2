package com.example.data.repository

import com.example.data.dao.*
import com.example.data.entity.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

class PriceMapperRepository(
    private val storeDao: StoreDao,
    private val productDao: ProductDao,
    private val priceHistoryDao: PriceHistoryDao,
    private val shoppingListDao: ShoppingListDao,
    private val listItemDao: ListItemDao
) {
    // --- Stores ---
    val allStores: Flow<List<Store>> = storeDao.getAllStores()

    suspend fun insertStore(store: Store): Long = storeDao.insertStore(store)
    suspend fun updateStore(store: Store) = storeDao.updateStore(store)
    suspend fun deleteStore(store: Store) = storeDao.deleteStore(store)

    // --- Products ---
    val allProducts: Flow<List<Product>> = productDao.getAllProducts()
    val allCategories: Flow<List<String>> = productDao.getAllCategories()

    fun searchProducts(query: String): Flow<List<Product>> {
        return if (query.isBlank()) allProducts else productDao.searchProducts(query)
    }

    suspend fun getProductById(id: Int): Product? = productDao.getProductById(id)
    suspend fun insertProduct(product: Product): Long = productDao.insertProduct(product)
    suspend fun updateProduct(product: Product) = productDao.updateProduct(product)
    suspend fun deleteProduct(product: Product) = productDao.deleteProduct(product)

    suspend fun getProductStats(product: Product): ProductStats {
        val lastPrice = priceHistoryDao.getLastPriceForProduct(product.id)
        val avgPrice = priceHistoryDao.getAveragePriceForProduct(product.id)
        val minPrice = priceHistoryDao.getMinPriceForProduct(product.id)
        val minStore = priceHistoryDao.getMinPriceStoreName(product.id)
        val maxPrice = priceHistoryDao.getMaxPriceForProduct(product.id)
        val maxStore = priceHistoryDao.getMaxPriceStoreName(product.id)
        val totalRecords = priceHistoryDao.getHistoryCountForProduct(product.id)

        return ProductStats(
            product = product,
            lastPrice = lastPrice,
            avgPrice = avgPrice,
            minPrice = minPrice,
            minPriceStoreName = minStore,
            maxPrice = maxPrice,
            maxPriceStoreName = maxStore,
            totalRecords = totalRecords
        )
    }

    // --- Price History ---
    val allPriceHistory: Flow<List<PriceHistoryItem>> = priceHistoryDao.getAllPriceHistory()

    fun getHistoryForProduct(productId: Int): Flow<List<PriceHistoryItem>> =
        priceHistoryDao.getHistoryForProduct(productId)

    suspend fun insertPriceHistory(priceHistory: PriceHistory): Long =
        priceHistoryDao.insertPriceHistory(priceHistory)

    suspend fun deletePriceHistory(priceHistory: PriceHistory) =
        priceHistoryDao.deletePriceHistory(priceHistory)

    // --- Shopping Lists ---
    val allShoppingLists: Flow<List<ShoppingListSummary>> = combine(
        shoppingListDao.getAllShoppingLists(),
        storeDao.getAllStores()
    ) { lists, stores ->
        val storeMap = stores.associateBy { it.id }
        lists.map { list ->
            val items = listItemDao.getItemsForListSync(list.id)
            val totalEst = items.sumOf { it.quantidadeDesejada * it.precoEstimadoUnidade }
            val totalR = items.sumOf { (it.precoRealUnidade ?: 0.0) * it.quantidadeDesejada }
            val compradosCount = items.count { it.comprado }

            ShoppingListSummary(
                shoppingList = list,
                nomeLoja = list.lojaId?.let { storeMap[it]?.nomeLoja },
                itemCount = items.size,
                itemsCompradosCount = compradosCount,
                totalEstimado = totalEst,
                totalReal = totalR
            )
        }
    }

    fun getShoppingListFlow(listId: Int): Flow<ShoppingList?> =
        shoppingListDao.getShoppingListByIdFlow(listId)

    suspend fun getShoppingListById(listId: Int): ShoppingList? =
        shoppingListDao.getShoppingListById(listId)

    suspend fun insertShoppingList(shoppingList: ShoppingList): Long =
        shoppingListDao.insertShoppingList(shoppingList)

    suspend fun updateShoppingList(shoppingList: ShoppingList) =
        shoppingListDao.updateShoppingList(shoppingList)

    suspend fun deleteShoppingList(shoppingList: ShoppingList) =
        shoppingListDao.deleteShoppingList(shoppingList)

    // --- List Items ---
    fun getListItemsWithProducts(listId: Int): Flow<List<ListItemWithProduct>> {
        return combine(
            listItemDao.getItemsForList(listId),
            productDao.getAllProducts()
        ) { items, products ->
            val productMap = products.associateBy { it.id }
            items.mapNotNull { item ->
                val prod = productMap[item.produtoId] ?: return@mapNotNull null
                val lastP = priceHistoryDao.getLastPriceForProduct(prod.id)
                val avgP = priceHistoryDao.getAveragePriceForProduct(prod.id)
                ListItemWithProduct(
                    listItem = item,
                    product = prod,
                    lastPrice = lastP,
                    avgPrice = avgP
                )
            }
        }
    }

    suspend fun addProductToList(
        listId: Int,
        productId: Int,
        quantidade: Double,
        estrategia: String
    ) {
        val estimatedUnit = when (estrategia) {
            ShoppingList.ESTRATEGIA_MEDIA -> priceHistoryDao.getAveragePriceForProduct(productId) ?: 0.0
            else -> priceHistoryDao.getLastPriceForProduct(productId) ?: 0.0
        }

        val item = ListItem(
            listaId = listId,
            produtoId = productId,
            quantidadeDesejada = quantidade,
            precoEstimadoUnidade = estimatedUnit,
            precoRealUnidade = if (estimatedUnit > 0) estimatedUnit else null,
            comprado = false
        )
        listItemDao.insertListItem(item)
    }

    suspend fun recalculateListEstimates(listId: Int, estrategia: String) {
        val items = listItemDao.getItemsForListSync(listId)
        items.forEach { item ->
            val estimatedUnit = when (estrategia) {
                ShoppingList.ESTRATEGIA_MEDIA -> priceHistoryDao.getAveragePriceForProduct(item.produtoId) ?: 0.0
                else -> priceHistoryDao.getLastPriceForProduct(item.produtoId) ?: 0.0
            }
            listItemDao.updateListItem(
                item.copy(
                    precoEstimadoUnidade = estimatedUnit,
                    precoRealUnidade = if (item.precoRealUnidade == null && estimatedUnit > 0) estimatedUnit else item.precoRealUnidade
                )
            )
        }
    }

    suspend fun updateListItem(listItem: ListItem) = listItemDao.updateListItem(listItem)
    suspend fun deleteListItem(listItem: ListItem) = listItemDao.deleteListItem(listItem)

    suspend fun updateItemPurchasedState(itemId: Int, comprado: Boolean, precoReal: Double?) {
        listItemDao.updateItemPurchasedState(itemId, comprado, precoReal)
    }

    // --- In-Store Mode & Completion ---
    suspend fun startShopping(listId: Int, storeId: Int) {
        shoppingListDao.updateListStatusAndStore(
            listId = listId,
            status = ShoppingList.STATUS_COMPRANDO,
            lojaId = storeId
        )
    }

    /**
     * Finalize shopping:
     * 1. Update shopping list status to FINALIZADA
     * 2. Automatically generate new PriceHistory records for all checked/purchased items
     *    linked to the chosen store and current date!
     */
    suspend fun finishShopping(listId: Int): Int {
        val list = shoppingListDao.getShoppingListById(listId) ?: return 0
        val storeId = list.lojaId ?: return 0
        val items = listItemDao.getItemsForListSync(listId)

        val purchasedItems = items.filter { it.comprado }
        val now = System.currentTimeMillis()

        val historyEntries = purchasedItems.mapNotNull { item ->
            val pricePaid = item.precoRealUnidade ?: item.precoEstimadoUnidade
            if (pricePaid > 0) {
                PriceHistory(
                    produtoId = item.produtoId,
                    lojaId = storeId,
                    precoPago = pricePaid,
                    dataCompra = now
                )
            } else null
        }

        if (historyEntries.isNotEmpty()) {
            priceHistoryDao.insertAll(historyEntries)
        }

        shoppingListDao.updateListStatusAndStore(
            listId = listId,
            status = ShoppingList.STATUS_FINALIZADA,
            lojaId = storeId
        )

        return historyEntries.size
    }

    // --- Data Export & Import (JSON Backup) ---
    suspend fun exportDataToJson(): String {
        val root = JSONObject()

        // Lojas
        val storesArray = JSONArray()
        allStores.first().forEach { store ->
            val sObj = JSONObject()
            sObj.put("id", store.id)
            sObj.put("nomeLoja", store.nomeLoja)
            sObj.put("localizacao", store.localizacao ?: "")
            storesArray.put(sObj)
        }
        root.put("lojas", storesArray)

        // Produtos
        val productsArray = JSONArray()
        allProducts.first().forEach { prod ->
            val pObj = JSONObject()
            pObj.put("id", prod.id)
            pObj.put("nomeProduto", prod.nomeProduto)
            pObj.put("codigoBarras", prod.codigoBarras ?: "")
            pObj.put("categoria", prod.categoria ?: "")
            productsArray.put(pObj)
        }
        root.put("produtos", productsArray)

        // Historico Precos
        val historyArray = JSONArray()
        priceHistoryDao.getAllRawHistory().forEach { h ->
            val hObj = JSONObject()
            hObj.put("id", h.id)
            hObj.put("produtoId", h.produtoId)
            hObj.put("lojaId", h.lojaId)
            hObj.put("precoPago", h.precoPago)
            hObj.put("dataCompra", h.dataCompra)
            historyArray.put(hObj)
        }
        root.put("historico_precos", historyArray)

        // Listas Compras
        val listsArray = JSONArray()
        shoppingListDao.getAllRawLists().forEach { l ->
            val lObj = JSONObject()
            lObj.put("id", l.id)
            lObj.put("nomeLista", l.nomeLista)
            lObj.put("dataCriacao", l.dataCriacao)
            lObj.put("status", l.status)
            lObj.put("lojaId", l.lojaId ?: JSONObject.NULL)
            lObj.put("estrategiaEstimativa", l.estrategiaEstimativa)
            listsArray.put(lObj)
        }
        root.put("listas_compras", listsArray)

        // Itens Lista
        val itemsArray = JSONArray()
        listItemDao.getAllRawListItems().forEach { item ->
            val iObj = JSONObject()
            iObj.put("id", item.id)
            iObj.put("listaId", item.listaId)
            iObj.put("produtoId", item.produtoId)
            iObj.put("quantidadeDesejada", item.quantidadeDesejada)
            iObj.put("precoEstimadoUnidade", item.precoEstimadoUnidade)
            iObj.put("precoRealUnidade", item.precoRealUnidade ?: JSONObject.NULL)
            iObj.put("comprado", item.comprado)
            itemsArray.put(iObj)
        }
        root.put("itens_lista", itemsArray)

        return root.toString(2)
    }

    suspend fun importDataFromJson(jsonStr: String): Boolean {
        return try {
            val root = JSONObject(jsonStr)

            if (root.has("lojas")) {
                val arr = root.getJSONArray("lojas")
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    storeDao.insertStore(
                        Store(
                            id = obj.optInt("id", 0),
                            nomeLoja = obj.getString("nomeLoja"),
                            localizacao = obj.optString("localizacao").ifBlank { null }
                        )
                    )
                }
            }

            if (root.has("produtos")) {
                val arr = root.getJSONArray("produtos")
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    productDao.insertProduct(
                        Product(
                            id = obj.optInt("id", 0),
                            nomeProduto = obj.getString("nomeProduto"),
                            codigoBarras = obj.optString("codigoBarras").ifBlank { null },
                            categoria = obj.optString("categoria").ifBlank { null }
                        )
                    )
                }
            }

            if (root.has("historico_precos")) {
                val arr = root.getJSONArray("historico_precos")
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    priceHistoryDao.insertPriceHistory(
                        PriceHistory(
                            id = obj.optInt("id", 0),
                            produtoId = obj.getInt("produtoId"),
                            lojaId = obj.getInt("lojaId"),
                            precoPago = obj.getDouble("precoPago"),
                            dataCompra = obj.optLong("dataCompra", System.currentTimeMillis())
                        )
                    )
                }
            }

            if (root.has("listas_compras")) {
                val arr = root.getJSONArray("listas_compras")
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    shoppingListDao.insertShoppingList(
                        ShoppingList(
                            id = obj.optInt("id", 0),
                            nomeLista = obj.getString("nomeLista"),
                            dataCriacao = obj.optLong("dataCriacao", System.currentTimeMillis()),
                            status = obj.optString("status", ShoppingList.STATUS_ABERTA),
                            lojaId = if (obj.isNull("lojaId")) null else obj.optInt("lojaId"),
                            estrategiaEstimativa = obj.optString("estrategiaEstimativa", ShoppingList.ESTRATEGIA_ULTIMO_PRECO)
                        )
                    )
                }
            }

            if (root.has("itens_lista")) {
                val arr = root.getJSONArray("itens_lista")
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    listItemDao.insertListItem(
                        ListItem(
                            id = obj.optInt("id", 0),
                            listaId = obj.getInt("listaId"),
                            produtoId = obj.getInt("produtoId"),
                            quantidadeDesejada = obj.optDouble("quantidadeDesejada", 1.0),
                            precoEstimadoUnidade = obj.optDouble("precoEstimadoUnidade", 0.0),
                            precoRealUnidade = if (obj.isNull("precoRealUnidade")) null else obj.optDouble("precoRealUnidade"),
                            comprado = obj.optBoolean("comprado", false)
                        )
                    )
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
