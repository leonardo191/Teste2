package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.entity.*
import com.example.data.model.*
import com.example.data.repository.PriceMapperRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class PriceMapperViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: PriceMapperRepository

    init {
        val database = AppDatabase.getDatabase(application, viewModelScope)
        repository = PriceMapperRepository(
            storeDao = database.storeDao(),
            productDao = database.productDao(),
            priceHistoryDao = database.priceHistoryDao(),
            shoppingListDao = database.shoppingListDao(),
            listItemDao = database.listItemDao()
        )
    }

    // --- Stores State ---
    val stores: StateFlow<List<Store>> = repository.allStores.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // --- Products Search & Categories State ---
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val products: StateFlow<List<Product>> = combine(_searchQuery, _selectedCategory) { query, category ->
        Pair(query, category)
    }.flatMapLatest { (query, category) ->
        repository.searchProducts(query).map { list ->
            if (category.isNull_or_blank()) list
            else list.filter { it.categoria?.equals(category, ignoreCase = true) == true }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val categories: StateFlow<List<String>> = repository.allCategories.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // --- Price History State ---
    val priceHistory: StateFlow<List<PriceHistoryItem>> = repository.allPriceHistory.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // --- Shopping Lists State ---
    val shoppingLists: StateFlow<List<ShoppingListSummary>> = repository.allShoppingLists.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // --- Active Selected List Detail State ---
    private val _selectedListId = MutableStateFlow<Int?>(null)
    val selectedListId: StateFlow<Int?> = _selectedListId.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val selectedList: StateFlow<ShoppingList?> = _selectedListId.flatMapLatest { id ->
        if (id == null) flowOf(null)
        else repository.getShoppingListFlow(id)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    val selectedListItems: StateFlow<List<ListItemWithProduct>> = _selectedListId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList())
        else repository.getListItemsWithProducts(id)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // --- Active Check Item Dialog (Data Entry Flow: "O preço se manteve R$ X?") ---
    private val _activeCheckItem = MutableStateFlow<ListItemWithProduct?>(null)
    val activeCheckItem: StateFlow<ListItemWithProduct?> = _activeCheckItem.asStateFlow()

    // --- Selected Product for History / Stats Sheet ---
    private val _selectedProductStats = MutableStateFlow<ProductStats?>(null)
    val selectedProductStats: StateFlow<ProductStats?> = _selectedProductStats.asStateFlow()

    private val _selectedProductHistory = MutableStateFlow<List<PriceHistoryItem>>(emptyList())
    val selectedProductHistory: StateFlow<List<PriceHistoryItem>> = _selectedProductHistory.asStateFlow()

    // --- User Feedback Messages ---
    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    fun clearUserMessage() {
        _userMessage.value = null
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedCategory(category: String?) {
        _selectedCategory.value = if (_selectedCategory.value == category) null else category
    }

    fun selectList(listId: Int?) {
        _selectedListId.value = listId
    }

    // --- Store Actions ---
    fun addStore(nome: String, localizacao: String?) {
        if (nome.isBlank()) return
        viewModelScope.launch {
            repository.insertStore(Store(nomeLoja = nome.trim(), localizacao = localizacao?.trim().takeIf { !it.isNull_or_blank() }))
            _userMessage.value = "Loja '${nome.trim()}' cadastrada com sucesso!"
        }
    }

    fun updateStore(store: Store) {
        viewModelScope.launch {
            repository.updateStore(store)
            _userMessage.value = "Loja atualizada!"
        }
    }

    fun deleteStore(store: Store) {
        viewModelScope.launch {
            repository.deleteStore(store)
            _userMessage.value = "Loja removida!"
        }
    }

    // --- Product Actions ---
    fun addProduct(nome: String, categoria: String?, codigoBarras: String?, onCreated: ((Int) -> Unit)? = null) {
        if (nome.isBlank()) return
        viewModelScope.launch {
            val id = repository.insertProduct(
                Product(
                    nomeProduto = nome.trim(),
                    categoria = categoria?.trim().takeIf { !it.isNull_or_blank() },
                    codigoBarras = codigoBarras?.trim().takeIf { !it.isNull_or_blank() }
                )
            ).toInt()
            _userMessage.value = "Produto '$nome' cadastrado!"
            onCreated?.invoke(id)
        }
    }

    fun updateProduct(product: Product) {
        viewModelScope.launch {
            repository.updateProduct(product)
            _userMessage.value = "Produto atualizado!"
        }
    }

    fun deleteProduct(product: Product) {
        viewModelScope.launch {
            repository.deleteProduct(product)
            _userMessage.value = "Produto removido!"
        }
    }

    fun inspectProductStats(product: Product) {
        viewModelScope.launch {
            val stats = repository.getProductStats(product)
            _selectedProductStats.value = stats
            repository.getHistoryForProduct(product.id).collect { history ->
                _selectedProductHistory.value = history
            }
        }
    }

    fun closeProductStats() {
        _selectedProductStats.value = null
        _selectedProductHistory.value = emptyList()
    }

    // --- Manual Price Entry ---
    fun addManualPriceHistory(productId: Int, storeId: Int, preco: Double) {
        viewModelScope.launch {
            repository.insertPriceHistory(
                PriceHistory(
                    produtoId = productId,
                    lojaId = storeId,
                    precoPago = preco
                )
            )
            _userMessage.value = "Preço registrado no histórico!"
            // Refresh stats if opened
            _selectedProductStats.value?.product?.let { inspectProductStats(it) }
        }
    }

    fun deletePriceHistory(historyItem: PriceHistoryItem) {
        viewModelScope.launch {
            repository.deletePriceHistory(
                PriceHistory(
                    id = historyItem.id,
                    produtoId = historyItem.produtoId,
                    lojaId = historyItem.lojaId,
                    precoPago = historyItem.precoPago,
                    dataCompra = historyItem.dataCompra
                )
            )
            _userMessage.value = "Registro de preço removido!"
            _selectedProductStats.value?.product?.let { inspectProductStats(it) }
        }
    }

    // --- Shopping List Actions ---
    fun createShoppingList(nome: String, estrategia: String) {
        if (nome.isBlank()) return
        viewModelScope.launch {
            val listId = repository.insertShoppingList(
                ShoppingList(
                    nomeLista = nome.trim(),
                    estrategiaEstimativa = estrategia
                )
            ).toInt()
            _selectedListId.value = listId
            _userMessage.value = "Lista '$nome' criada com sucesso!"
        }
    }

    fun updateListEstimationStrategy(listId: Int, novaEstrategia: String) {
        viewModelScope.launch {
            val current = repository.getShoppingListById(listId) ?: return@launch
            val updated = current.copy(estrategiaEstimativa = novaEstrategia)
            repository.updateShoppingList(updated)
            repository.recalculateListEstimates(listId, novaEstrategia)
            _userMessage.value = if (novaEstrategia == ShoppingList.ESTRATEGIA_MEDIA)
                "Estimativas recalculadas com base na média de preços!"
            else
                "Estimativas recalculadas com base no último preço pago!"
        }
    }

    fun deleteShoppingList(listSummary: ShoppingListSummary) {
        viewModelScope.launch {
            repository.deleteShoppingList(listSummary.shoppingList)
            if (_selectedListId.value == listSummary.shoppingList.id) {
                _selectedListId.value = null
            }
            _userMessage.value = "Lista removida!"
        }
    }

    // --- List Item Actions ---
    fun addProductToActiveList(productId: Int, quantidade: Double = 1.0) {
        val listId = _selectedListId.value ?: return
        val currentList = selectedList.value ?: return
        viewModelScope.launch {
            repository.addProductToList(
                listId = listId,
                productId = productId,
                quantidade = quantidade,
                estrategia = currentList.estrategiaEstimativa
            )
            _userMessage.value = "Item adicionado à lista!"
        }
    }

    fun updateListItemQuantity(listItem: ListItem, novaQuantidade: Double) {
        if (novaQuantidade <= 0) {
            deleteListItem(listItem)
            return
        }
        viewModelScope.launch {
            repository.updateListItem(listItem.copy(quantidadeDesejada = novaQuantidade))
        }
    }

    fun deleteListItem(listItem: ListItem) {
        viewModelScope.launch {
            repository.deleteListItem(listItem)
            _userMessage.value = "Item removido da lista."
        }
    }

    // --- Data Entry Flow in In-Store Mode ("Comprando") ---
    fun openCheckItemDialog(itemWithProd: ListItemWithProduct) {
        _activeCheckItem.value = itemWithProd
    }

    fun dismissCheckItemDialog() {
        _activeCheckItem.value = null
    }

    fun confirmItemPurchased(itemWithProd: ListItemWithProduct, finalPriceUnit: Double) {
        viewModelScope.launch {
            repository.updateItemPurchasedState(
                itemId = itemWithProd.listItem.id,
                comprado = true,
                precoReal = finalPriceUnit
            )
            // If item previously had no estimated price, update estimated price snapshot as well
            if (itemWithProd.listItem.precoEstimadoUnidade <= 0.0) {
                repository.updateListItem(
                    itemWithProd.listItem.copy(
                        comprado = true,
                        precoRealUnidade = finalPriceUnit,
                        precoEstimadoUnidade = finalPriceUnit
                    )
                )
            }
            _activeCheckItem.value = null
        }
    }

    fun uncheckItem(itemWithProd: ListItemWithProduct) {
        viewModelScope.launch {
            repository.updateItemPurchasedState(
                itemId = itemWithProd.listItem.id,
                comprado = false,
                precoReal = null
            )
        }
    }

    // --- Start / Finish Shopping ---
    fun startShopping(listId: Int, storeId: Int) {
        viewModelScope.launch {
            repository.startShopping(listId, storeId)
            _userMessage.value = "Modo 'Comprando' ativado! Atualize os preços no carrinho."
        }
    }

    fun finishShopping(listId: Int) {
        viewModelScope.launch {
            val newEntriesCount = repository.finishShopping(listId)
            _userMessage.value = "Compra finalizada! $newEntriesCount novos registros salvos no Histórico de Preços."
        }
    }

    // --- JSON Backup Export / Import ---
    fun exportBackupJson(onExportReady: (String) -> Unit) {
        viewModelScope.launch {
            val jsonStr = repository.exportDataToJson()
            onExportReady(jsonStr)
        }
    }

    fun importBackupJson(jsonStr: String) {
        viewModelScope.launch {
            val success = repository.importDataFromJson(jsonStr)
            if (success) {
                _userMessage.value = "Backup restaurado com sucesso!"
            } else {
                _userMessage.value = "Falha ao importar JSON de backup. Verifique o arquivo."
            }
        }
    }
}

private fun String?.isNull_or_blank(): Boolean {
    return this == null || this.isBlank()
}
