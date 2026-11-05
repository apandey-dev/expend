package com.expend.tracker.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.expend.tracker.data.backup.BackupManager
import com.expend.tracker.data.database.ExpendDatabase
import com.expend.tracker.data.entities.CategoryEntity
import com.expend.tracker.data.entities.TemplateEntity
import com.expend.tracker.data.entities.TransactionEntity
import com.expend.tracker.data.entities.WalletEntity
import com.expend.tracker.data.models.FinancialSummary
import com.expend.tracker.data.models.TransactionItem
import com.expend.tracker.data.models.TransactionType
import com.expend.tracker.data.models.WalletWithBalance
import com.expend.tracker.data.preferences.ThemeMode
import com.expend.tracker.data.preferences.ThemePreferences
import com.expend.tracker.data.repository.ExpendRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ExpendViewModel(application: Application) : AndroidViewModel(application) {

    private val database = ExpendDatabase.getInstance(application)
    private val repository = ExpendRepository(database)
    val backupManager = BackupManager(application, database)
    private val themePreferences = ThemePreferences(application)

    val themeMode: StateFlow<ThemeMode> = themePreferences.themeModeFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = ThemeMode.DEVICE
        )

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            themePreferences.setThemeMode(mode)
        }
    }

    // ─── STATE STREAMS ─────────────────────────────────────────────────────────

    val financialSummary: StateFlow<FinancialSummary> = repository.financialSummaryFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = FinancialSummary()
        )

    val walletsWithBalances: StateFlow<List<WalletWithBalance>> = repository.walletsWithBalancesFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val activeWallets: StateFlow<List<WalletEntity>> = repository.activeWalletsFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val recentTransactions: StateFlow<List<TransactionItem>> = repository.recentTransactionItemsFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allTransactionItems: StateFlow<List<TransactionItem>> = repository.allTransactionItemsFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val templates: StateFlow<List<TemplateEntity>> = repository.allTemplatesFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val userPaymentModes: StateFlow<List<String>> = repository.distinctPaymentModesFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val expenseCategories: StateFlow<List<CategoryEntity>> = repository.expenseCategoriesFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val incomeCategories: StateFlow<List<CategoryEntity>> = repository.incomeCategoriesFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Reactive State for Nested Full-Screen Creation Auto-Selection
    val newlyCreatedWalletId = MutableStateFlow<Long?>(null)
    fun clearNewlyCreatedWalletId() {
        newlyCreatedWalletId.value = null
    }

    val newlyCreatedCategoryName = MutableStateFlow<String?>(null)
    fun clearNewlyCreatedCategoryName() {
        newlyCreatedCategoryName.value = null
    }

    val activeExpenseCategories: StateFlow<List<CategoryEntity>> = repository.activeExpenseCategoriesFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val activeIncomeCategories: StateFlow<List<CategoryEntity>> = repository.activeIncomeCategoriesFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // ─── TRANSACTIONS SCREEN FILTERING ─────────────────────────────────────────

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _selectedFilterType = MutableStateFlow<TransactionType?>(null) // null = ALL
    val selectedFilterType = _selectedFilterType.asStateFlow()

    private val _selectedWalletFilterId = MutableStateFlow<Long?>(null) // null = ALL
    val selectedWalletFilterId = _selectedWalletFilterId.asStateFlow()

    val filteredTransactions: StateFlow<List<TransactionItem>> = combine(
        repository.allTransactionItemsFlow,
        _searchQuery,
        _selectedFilterType,
        _selectedWalletFilterId
    ) { allItems, query, filterType, walletId ->
        allItems.filter { item ->
            val matchesQuery = query.isEmpty() ||
                    item.transaction.title.contains(query, ignoreCase = true) ||
                    item.transaction.category.contains(query, ignoreCase = true) ||
                    item.transaction.note.contains(query, ignoreCase = true) ||
                    item.walletName.contains(query, ignoreCase = true)

            val matchesType = filterType == null || item.transaction.type == filterType.name
            val matchesWallet = walletId == null ||
                    item.transaction.walletId == walletId ||
                    (item.transaction.type == TransactionType.TRANSFER.name && item.transaction.destinationWalletId == walletId)

            matchesQuery && matchesType && matchesWallet
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setFilterType(type: TransactionType?) {
        _selectedFilterType.value = type
    }

    fun setWalletFilter(walletId: Long?) {
        _selectedWalletFilterId.value = walletId
    }

    // ─── TRANSACTION ACTIONS ─────────────────────────────────────────────────

    fun saveTransaction(
        id: Long = 0L,
        title: String,
        amountPaise: Long,
        type: TransactionType,
        category: String,
        walletId: Long,
        destinationWalletId: Long? = null,
        paymentMode: String = "",
        dateMillis: Long,
        note: String = "",
        onComplete: (() -> Unit)? = null
    ) {
        viewModelScope.launch {
            val entity = TransactionEntity(
                id = id,
                title = title.trim(),
                amountPaise = amountPaise,
                type = type.name,
                category = if (type == TransactionType.TRANSFER) "Transfer" else category.trim(),
                walletId = walletId,
                destinationWalletId = if (type == TransactionType.TRANSFER) destinationWalletId else null,
                paymentMode = paymentMode.trim(),
                dateMillis = dateMillis,
                note = note.trim()
            )
            repository.saveTransaction(entity)
            onComplete?.invoke()
        }
    }

    suspend fun getTransaction(id: Long): TransactionEntity? {
        return repository.getTransactionById(id)
    }

    fun deleteTransaction(id: Long, onComplete: (() -> Unit)? = null) {
        viewModelScope.launch {
            repository.deleteTransaction(id)
            onComplete?.invoke()
        }
    }

    // ─── WALLET ACTIONS ──────────────────────────────────────────────────────

    fun saveWallet(
        id: Long = 0L,
        name: String,
        initialBalancePaise: Long,
        walletType: String,
        description: String = "",
        onComplete: ((Long) -> Unit)? = null
    ) {
        viewModelScope.launch {
            val entity = WalletEntity(
                id = id,
                name = name.trim(),
                initialBalancePaise = initialBalancePaise,
                walletType = walletType,
                description = description.trim()
            )
            val savedId = repository.saveWallet(entity)
            newlyCreatedWalletId.value = savedId
            onComplete?.invoke(savedId)
        }
    }

    suspend fun getWallet(id: Long): WalletEntity? {
        return repository.getWalletById(id)
    }

    fun archiveWallet(walletId: Long) {
        viewModelScope.launch {
            repository.archiveWallet(walletId)
        }
    }

    fun unarchiveWallet(walletId: Long) {
        viewModelScope.launch {
            repository.unarchiveWallet(walletId)
        }
    }

    suspend fun getTransactionCountForWallet(walletId: Long): Int {
        return repository.getTransactionCountForWallet(walletId)
    }

    fun deleteEmptyWallet(wallet: WalletEntity, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                repository.deleteEmptyWallet(wallet)
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Failed to delete wallet")
            }
        }
    }

    fun reassignAndDeleteWallet(oldWalletId: Long, newWalletId: Long, onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.reassignAndDeleteWallet(oldWalletId, newWalletId)
            onComplete()
        }
    }

    // ─── TEMPLATE ACTIONS ────────────────────────────────────────────────────

    fun saveTemplate(
        id: Long = 0L,
        name: String,
        type: TransactionType,
        defaultAmountPaise: Long?,
        title: String,
        category: String,
        defaultWalletId: Long?,
        note: String = "",
        onComplete: (() -> Unit)? = null
    ) {
        viewModelScope.launch {
            val entity = TemplateEntity(
                id = id,
                name = name.trim(),
                type = type.name,
                defaultAmountPaise = defaultAmountPaise,
                title = title.trim(),
                category = category.trim(),
                defaultWalletId = defaultWalletId,
                note = note.trim()
            )
            repository.saveTemplate(entity)
            onComplete?.invoke()
        }
    }

    suspend fun getTemplate(id: Long): TemplateEntity? {
        return repository.getTemplateById(id)
    }

    fun deleteTemplate(id: Long, onComplete: (() -> Unit)? = null) {
        viewModelScope.launch {
            repository.deleteTemplate(id)
            onComplete?.invoke()
        }
    }

    // ─── CATEGORY ACTIONS ───────────────────────────────────────────────────

    fun saveCategory(
        name: String,
        type: String,
        iconId: String = "ic_cat_general",
        id: Long = 0L,
        onComplete: ((String) -> Unit)? = null,
        onError: ((String) -> Unit)? = null
    ) {
        viewModelScope.launch {
            try {
                repository.saveCategory(name, type, iconId, id)
                val trimmedName = name.trim()
                newlyCreatedCategoryName.value = trimmedName
                onComplete?.invoke(trimmedName)
            } catch (e: Exception) {
                onError?.invoke(e.message ?: "Failed to save category")
            }
        }
    }

    fun archiveCategory(id: Long) {
        viewModelScope.launch {
            repository.archiveCategory(id)
        }
    }

    fun unarchiveCategory(id: Long) {
        viewModelScope.launch {
            repository.unarchiveCategory(id)
        }
    }

    suspend fun getCategory(id: Long): CategoryEntity? {
        return repository.getCategoryById(id)
    }

    suspend fun getTransactionCountForCategory(categoryName: String): Int {
        return repository.getTransactionCountForCategory(categoryName)
    }

    fun deleteCategorySafely(
        category: CategoryEntity,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                repository.deleteCategorySafely(category)
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Failed to delete category")
            }
        }
    }

    fun reassignAndDeleteCategory(
        oldCategory: CategoryEntity,
        newCategoryName: String,
        onComplete: () -> Unit
    ) {
        viewModelScope.launch {
            repository.reassignAndDeleteCategory(oldCategory, newCategoryName)
            onComplete()
        }
    }

    // ─── JSON BACKUP & RESTORE ───────────────────────────────────────────────

    fun exportBackup(uri: Uri, onResult: (Result<Unit>) -> Unit) {
        viewModelScope.launch {
            val result = backupManager.exportToFile(uri)
            onResult(result)
        }
    }

    fun restoreBackup(uri: Uri, onResult: (Result<String>) -> Unit) {
        viewModelScope.launch {
            try {
                val json = backupManager.readJsonFromUri(uri)
                val result = backupManager.restoreFromJson(json)
                onResult(result)
            } catch (e: Exception) {
                onResult(Result.failure(e))
            }
        }
    }
}
