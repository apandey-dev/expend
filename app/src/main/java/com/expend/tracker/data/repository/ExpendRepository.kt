package com.expend.tracker.data.repository

import androidx.room.withTransaction
import com.expend.tracker.data.database.ExpendDatabase
import com.expend.tracker.data.entities.CategoryEntity
import com.expend.tracker.data.entities.TemplateEntity
import com.expend.tracker.data.entities.TransactionEntity
import com.expend.tracker.data.entities.WalletEntity
import com.expend.tracker.data.models.FinancialSummary
import com.expend.tracker.data.models.TransactionItem
import com.expend.tracker.data.models.TransactionType
import com.expend.tracker.data.models.WalletWithBalance
import com.expend.tracker.util.DateUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class ExpendRepository(private val database: ExpendDatabase) {

    private val walletDao = database.walletDao()
    private val transactionDao = database.transactionDao()
    private val templateDao = database.templateDao()
    private val categoryDao = database.categoryDao()

    // ─── WALLET STREAMS & CALCULATIONS ───────────────────────────────────────

    val allWalletsFlow: Flow<List<WalletEntity>> = walletDao.getAllWalletsFlow()
    val activeWalletsFlow: Flow<List<WalletEntity>> = walletDao.getActiveWalletsFlow()

    val allTransactionsFlow: Flow<List<TransactionEntity>> = transactionDao.getAllTransactionsFlow()
    val recentTransactionsFlow: Flow<List<TransactionEntity>> = transactionDao.getRecentTransactionsFlow(5)
    val distinctPaymentModesFlow: Flow<List<String>> = transactionDao.getDistinctPaymentModesFlow()
    val allTemplatesFlow: Flow<List<TemplateEntity>> = templateDao.getAllTemplatesFlow()

    // ─── CATEGORY STREAMS ───────────────────────────────────────────────────

    val allCategoriesFlow: Flow<List<CategoryEntity>> = categoryDao.getAllCategoriesFlow()
    val expenseCategoriesFlow: Flow<List<CategoryEntity>> = categoryDao.getCategoriesByTypeFlow("EXPENSE")
    val incomeCategoriesFlow: Flow<List<CategoryEntity>> = categoryDao.getCategoriesByTypeFlow("INCOME")
    val activeExpenseCategoriesFlow: Flow<List<CategoryEntity>> = categoryDao.getActiveCategoriesByTypeFlow("EXPENSE")
    val activeIncomeCategoriesFlow: Flow<List<CategoryEntity>> = categoryDao.getActiveCategoriesByTypeFlow("INCOME")

    /**
     * Single Source of Truth for Wallets with their live mathematically accurate balances.
     * Balance = Initial + Total Inflows (Income + Inbound Transfers) - Total Outflows (Expense + Outbound Transfers)
     */
    val walletsWithBalancesFlow: Flow<List<WalletWithBalance>> = combine(
        allWalletsFlow,
        allTransactionsFlow
    ) { wallets, transactions ->
        wallets.map { wallet ->
            var income = 0L
            var expense = 0L
            var count = 0

            for (tx in transactions) {
                when (tx.type) {
                    TransactionType.INCOME.name -> {
                        if (tx.walletId == wallet.id) {
                            income += tx.amountPaise
                            count++
                        }
                    }
                    TransactionType.EXPENSE.name -> {
                        if (tx.walletId == wallet.id) {
                            expense += tx.amountPaise
                            count++
                        }
                    }
                    TransactionType.TRANSFER.name -> {
                        if (tx.walletId == wallet.id) {
                            // Source wallet: money left
                            expense += tx.amountPaise
                            count++
                        }
                        if (tx.destinationWalletId == wallet.id) {
                            // Destination wallet: money entered
                            income += tx.amountPaise
                            count++
                        }
                    }
                }
            }

            WalletWithBalance(
                wallet = wallet,
                incomePaise = income,
                expensePaise = expense,
                transactionCount = count
            )
        }
    }

    /**
     * Single Source of Truth for Overall Financial Summary.
     * Total Balance across active wallets.
     * Transfers do NOT inflate monthly income or monthly expenses.
     */
    val financialSummaryFlow: Flow<FinancialSummary> = combine(
        walletsWithBalancesFlow,
        allTransactionsFlow
    ) { walletsWithBalances, transactions ->
        val activeWallets = walletsWithBalances.filter { !it.wallet.isArchived }
        val totalBalance = activeWallets.sumOf { it.currentBalancePaise }

        var monthlyIncome = 0L
        var monthlyExpense = 0L

        // Include initial balances for wallets that opted into income reporting in their creation month
        for (wb in activeWallets) {
            val w = wb.wallet
            if (!w.isCreditCard && w.includeInIncome && w.initialBalancePaise > 0L && DateUtils.isCurrentMonth(w.createdAt)) {
                monthlyIncome += w.initialBalancePaise
            }
        }

        for (tx in transactions) {
            if (DateUtils.isCurrentMonth(tx.dateMillis)) {
                if (tx.type == TransactionType.INCOME.name) {
                    monthlyIncome += tx.amountPaise
                } else if (tx.type == TransactionType.EXPENSE.name) {
                    monthlyExpense += tx.amountPaise
                }
                // Transfer is explicitly NOT added to income or expense
            }
        }

        FinancialSummary(
            totalBalancePaise = totalBalance,
            monthlyIncomePaise = monthlyIncome,
            monthlyExpensePaise = monthlyExpense,
            activeWalletsCount = activeWallets.size
        )
    }

    /**
     * Resolves transaction items with appropriate wallet name (or "Source → Destination" for transfer)
     */
    val recentTransactionItemsFlow: Flow<List<TransactionItem>> = combine(
        recentTransactionsFlow,
        allWalletsFlow
    ) { transactions, wallets ->
        val walletMap = wallets.associate { it.id to it.name }
        transactions.map { tx ->
            val displayName = if (tx.type == TransactionType.TRANSFER.name) {
                val fromName = walletMap[tx.walletId] ?: "Unknown"
                val toName = walletMap[tx.destinationWalletId] ?: "Unknown"
                "$fromName → $toName"
            } else {
                walletMap[tx.walletId] ?: "Unknown"
            }
            TransactionItem(
                transaction = tx,
                walletName = displayName
            )
        }
    }

    val allTransactionItemsFlow: Flow<List<TransactionItem>> = combine(
        allTransactionsFlow,
        allWalletsFlow
    ) { transactions, wallets ->
        val walletMap = wallets.associate { it.id to it.name }
        transactions.map { tx ->
            val displayName = if (tx.type == TransactionType.TRANSFER.name) {
                val fromName = walletMap[tx.walletId] ?: "Unknown"
                val toName = walletMap[tx.destinationWalletId] ?: "Unknown"
                "$fromName → $toName"
            } else {
                walletMap[tx.walletId] ?: "Unknown"
            }
            TransactionItem(
                transaction = tx,
                walletName = displayName
            )
        }
    }

    // ─── TRANSACTION OPERATIONS ──────────────────────────────────────────────

    suspend fun getTransactionById(id: Long): TransactionEntity? {
        return transactionDao.getTransactionById(id)
    }

    suspend fun saveTransaction(transaction: TransactionEntity): Long {
        return database.withTransaction {
            if (transaction.id == 0L) {
                transactionDao.insertTransaction(transaction)
            } else {
                transactionDao.updateTransaction(
                    transaction.copy(updatedAt = System.currentTimeMillis())
                )
                transaction.id
            }
        }
    }

    suspend fun deleteTransaction(transactionId: Long) {
        database.withTransaction {
            transactionDao.deleteTransactionById(transactionId)
        }
    }

    // ─── WALLET OPERATIONS ───────────────────────────────────────────────────

    suspend fun getWalletById(id: Long): WalletEntity? {
        return walletDao.getWalletById(id)
    }

    suspend fun saveWallet(wallet: WalletEntity): Long {
        return if (wallet.id == 0L) {
            walletDao.insertWallet(wallet)
        } else {
            val existing = walletDao.getWalletById(wallet.id)
            walletDao.updateWallet(
                wallet.copy(
                    createdAt = existing?.createdAt ?: wallet.createdAt,
                    updatedAt = System.currentTimeMillis()
                )
            )
            wallet.id
        }
    }

    suspend fun archiveWallet(walletId: Long) {
        walletDao.archiveWallet(walletId)
    }

    suspend fun unarchiveWallet(walletId: Long) {
        walletDao.unarchiveWallet(walletId)
    }

    suspend fun getTransactionCountForWallet(walletId: Long): Int {
        return transactionDao.getTransactionCountForWallet(walletId)
    }

    suspend fun deleteEmptyWallet(wallet: WalletEntity) {
        val count = transactionDao.getTransactionCountForWallet(wallet.id)
        if (count > 0) {
            throw IllegalStateException("Cannot delete wallet with existing transactions. Archive it or reassign transactions first.")
        }
        walletDao.deleteWallet(wallet)
    }

    suspend fun reassignAndDeleteWallet(oldWalletId: Long, newWalletId: Long) {
        database.withTransaction {
            transactionDao.reassignSourceWalletTransactions(oldWalletId, newWalletId)
            transactionDao.reassignDestinationWalletTransactions(oldWalletId, newWalletId)
            walletDao.deleteWalletById(oldWalletId)
        }
    }

    // ─── TEMPLATE OPERATIONS ─────────────────────────────────────────────────

    suspend fun getTemplateById(id: Long): TemplateEntity? {
        return templateDao.getTemplateById(id)
    }

    suspend fun saveTemplate(template: TemplateEntity): Long {
        return if (template.id == 0L) {
            templateDao.insertTemplate(template)
        } else {
            templateDao.updateTemplate(template)
            template.id
        }
    }

    suspend fun deleteTemplate(templateId: Long) {
        templateDao.deleteTemplateById(templateId)
    }

    // ─── CATEGORY OPERATIONS ─────────────────────────────────────────────────

    suspend fun getCategoryById(id: Long): CategoryEntity? {
        return categoryDao.getCategoryById(id)
    }

    suspend fun saveCategory(name: String, type: String, iconId: String = "ic_cat_general", id: Long = 0L): Long {
        val trimmed = name.trim()
        val existing = categoryDao.getCategoryByNameAndType(trimmed, type)
        if (existing != null && existing.id != id) {
            throw IllegalArgumentException("A category named '$trimmed' already exists in $type.")
        }

        return if (id == 0L) {
            categoryDao.insertCategory(CategoryEntity(name = trimmed, type = type, iconId = iconId))
        } else {
            val current = categoryDao.getCategoryById(id)
            val oldName = current?.name
            val updated = CategoryEntity(
                id = id,
                name = trimmed,
                type = type,
                iconId = iconId,
                isArchived = current?.isArchived ?: false,
                createdAt = current?.createdAt ?: System.currentTimeMillis()
            )
            database.withTransaction {
                categoryDao.updateCategory(updated)
                if (oldName != null && oldName != trimmed) {
                    transactionDao.reassignCategoryTransactions(oldName, trimmed)
                }
            }
            id
        }
    }

    suspend fun archiveCategory(id: Long) {
        categoryDao.archiveCategory(id)
    }

    suspend fun unarchiveCategory(id: Long) {
        categoryDao.unarchiveCategory(id)
    }

    suspend fun getTransactionCountForCategory(categoryName: String): Int {
        return transactionDao.getTransactionCountForCategory(categoryName)
    }

    suspend fun deleteCategorySafely(category: CategoryEntity) {
        val count = transactionDao.getTransactionCountForCategory(category.name)
        if (count > 0) {
            throw IllegalStateException("Cannot delete category in use. Archive it or reassign its transactions first.")
        }
        categoryDao.deleteCategory(category)
    }

    suspend fun reassignAndDeleteCategory(oldCategory: CategoryEntity, newCategoryName: String) {
        database.withTransaction {
            transactionDao.reassignCategoryTransactions(oldCategory.name, newCategoryName)
            categoryDao.deleteCategory(oldCategory)
        }
    }
}
