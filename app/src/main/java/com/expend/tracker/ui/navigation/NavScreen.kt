package com.expend.tracker.ui.navigation

import androidx.annotation.DrawableRes
import com.expend.tracker.R
import com.expend.tracker.data.models.TransactionType

sealed class NavItem(
    val route: String,
    val title: String,
    @DrawableRes val iconRes: Int
) {
    data object Home : NavItem(
        route = "home",
        title = "Home",
        iconRes = R.drawable.ic_nav_home
    )

    data object Transactions : NavItem(
        route = "transactions",
        title = "Transactions",
        iconRes = R.drawable.ic_nav_transactions
    )

    data object Templates : NavItem(
        route = "templates",
        title = "Templates",
        iconRes = R.drawable.ic_nav_templates
    )

    data object Wallets : NavItem(
        route = "wallets",
        title = "Wallets",
        iconRes = R.drawable.ic_nav_wallets
    )

    data object Categories : NavItem(
        route = "categories",
        title = "Categories",
        iconRes = R.drawable.ic_nav_categories
    )

    companion object {
        fun getBottomItems(): List<NavItem> = listOf(
            Home,
            Transactions,
            Templates,
            Wallets,
            Categories
        )
    }
}

object NavRoutes {
    const val HOME = "home"
    const val TRANSACTIONS = "transactions"
    const val TEMPLATES = "templates"
    const val WALLETS = "wallets"
    const val CATEGORIES = "categories"
    const val SETTINGS = "settings"
    const val VIEW_ALL_TRANSACTIONS = "view_all_transactions"

    const val ADD_TRANSACTION = "add_transaction?type={type}"
    const val EDIT_TRANSACTION = "edit_transaction/{transactionId}"
    const val USE_TEMPLATE = "use_template/{templateId}"

    const val ADD_WALLET = "add_wallet"
    const val EDIT_WALLET = "edit_wallet/{walletId}"

    const val ADD_CATEGORY = "add_category?type={type}"
    const val EDIT_CATEGORY = "edit_category/{categoryId}"

    const val ADD_TEMPLATE = "add_template"
    const val EDIT_TEMPLATE = "edit_template/{templateId}"

    fun addTransaction(type: TransactionType = TransactionType.EXPENSE) = "add_transaction?type=${type.name}"
    fun editTransaction(id: Long) = "edit_transaction/$id"
    fun editWallet(id: Long) = "edit_wallet/$id"
    fun addCategory(type: TransactionType = TransactionType.EXPENSE) = "add_category?type=${type.name}"
    fun editCategory(id: Long) = "edit_category/$id"
    fun useTemplate(templateId: Long) = "use_template/$templateId"
    fun editTemplate(id: Long) = "edit_template/$id"
}
