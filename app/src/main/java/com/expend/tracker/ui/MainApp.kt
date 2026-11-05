package com.expend.tracker.ui

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.expend.tracker.data.models.TransactionType
import com.expend.tracker.ui.categories.AddEditCategoryScreen
import com.expend.tracker.ui.categories.CategoriesScreen
import com.expend.tracker.ui.home.HomeScreen
import com.expend.tracker.ui.navigation.ExpendBottomNavBar
import com.expend.tracker.ui.navigation.NavItem
import com.expend.tracker.ui.navigation.NavRoutes
import com.expend.tracker.ui.settings.SettingsScreen
import com.expend.tracker.ui.templates.AddEditTemplateScreen
import com.expend.tracker.ui.templates.TemplatesScreen
import com.expend.tracker.ui.transactions.AddEditTransactionScreen
import com.expend.tracker.ui.transactions.TransactionsScreen
import com.expend.tracker.ui.wallets.AddEditWalletScreen
import com.expend.tracker.ui.wallets.WalletsScreen
import com.expend.tracker.viewmodel.ExpendViewModel

@Composable
fun MainApp(
    viewModel: ExpendViewModel = viewModel()
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Show bottom bar only on the 5 top-level tabs
    val isTopLevelDestination = currentRoute in listOf(
        NavRoutes.HOME,
        NavRoutes.TRANSACTIONS,
        NavRoutes.TEMPLATES,
        NavRoutes.WALLETS,
        NavRoutes.CATEGORIES
    )

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            if (isTopLevelDestination) {
                ExpendBottomNavBar(
                    items = NavItem.getBottomItems(),
                    currentRoute = currentRoute,
                    onItemSelected = { item ->
                        if (currentRoute != item.route) {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            NavHost(
                navController = navController,
                startDestination = NavRoutes.HOME,
                enterTransition = { fadeIn(animationSpec = tween(180)) },
                exitTransition = { fadeOut(animationSpec = tween(180)) }
            ) {
                // 1. HOME SCREEN
                composable(NavRoutes.HOME) {
                    HomeScreen(
                        viewModel = viewModel,
                        onNavigateToAddIncome = {
                            navController.navigate(NavRoutes.addTransaction(TransactionType.INCOME))
                        },
                        onNavigateToAddExpense = {
                            navController.navigate(NavRoutes.addTransaction(TransactionType.EXPENSE))
                        },
                        onNavigateToEditTransaction = { id ->
                            navController.navigate(NavRoutes.editTransaction(id))
                        },
                        onNavigateToAllTransactions = {
                            navController.navigate(NavRoutes.TRANSACTIONS) {
                                launchSingleTop = true
                            }
                        },
                        onNavigateToSettings = {
                            navController.navigate(NavRoutes.SETTINGS)
                        }
                    )
                }

                // 2. TRANSACTIONS SCREEN
                composable(NavRoutes.TRANSACTIONS) {
                    TransactionsScreen(
                        viewModel = viewModel,
                        onNavigateToAddTransaction = {
                            navController.navigate(NavRoutes.addTransaction(TransactionType.EXPENSE))
                        },
                        onNavigateToTransfer = {
                            navController.navigate(NavRoutes.addTransaction(TransactionType.TRANSFER))
                        },
                        onNavigateToEditTransaction = { id ->
                            navController.navigate(NavRoutes.editTransaction(id))
                        }
                    )
                }

                // 3. TEMPLATES SCREEN
                composable(NavRoutes.TEMPLATES) {
                    TemplatesScreen(
                        viewModel = viewModel,
                        onNavigateToAddTemplate = { navController.navigate(NavRoutes.ADD_TEMPLATE) },
                        onNavigateToEditTemplate = { id -> navController.navigate(NavRoutes.editTemplate(id)) },
                        onUseTemplate = { templateId -> navController.navigate(NavRoutes.useTemplate(templateId)) }
                    )
                }

                // 4. WALLETS SCREEN
                composable(NavRoutes.WALLETS) {
                    WalletsScreen(
                        viewModel = viewModel,
                        onNavigateToAddWallet = { navController.navigate(NavRoutes.ADD_WALLET) },
                        onNavigateToEditWallet = { walletId -> navController.navigate(NavRoutes.editWallet(walletId)) },
                        onNavigateToTransactionsForWallet = { walletId ->
                            viewModel.setWalletFilter(walletId)
                            navController.navigate(NavRoutes.TRANSACTIONS)
                        }
                    )
                }

                // 5. CATEGORIES SCREEN
                composable(NavRoutes.CATEGORIES) {
                    CategoriesScreen(viewModel = viewModel)
                }

                // ─── SECONDARY ROUTES ────────────────────────────────────────

                // ADD TRANSACTION
                composable(
                    route = NavRoutes.ADD_TRANSACTION,
                    arguments = listOf(
                        navArgument("type") {
                            type = NavType.StringType
                            nullable = true
                            defaultValue = TransactionType.EXPENSE.name
                        }
                    )
                ) { backStackEntry ->
                    val typeString = backStackEntry.arguments?.getString("type") ?: TransactionType.EXPENSE.name
                    val initialType = TransactionType.fromString(typeString)
                    AddEditTransactionScreen(
                        viewModel = viewModel,
                        transactionId = null,
                        templateId = null,
                        initialType = initialType,
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToCreateTemplate = { navController.navigate(NavRoutes.ADD_TEMPLATE) }
                    )
                }

                // EDIT TRANSACTION
                composable(
                    route = NavRoutes.EDIT_TRANSACTION,
                    arguments = listOf(navArgument("transactionId") { type = NavType.LongType })
                ) { backStackEntry ->
                    val transactionId = backStackEntry.arguments?.getLong("transactionId") ?: 0L
                    AddEditTransactionScreen(
                        viewModel = viewModel,
                        transactionId = transactionId,
                        templateId = null,
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToCreateTemplate = { navController.navigate(NavRoutes.ADD_TEMPLATE) }
                    )
                }

                // USE TEMPLATE (Opens Add Transaction with pre-filled fields)
                composable(
                    route = NavRoutes.USE_TEMPLATE,
                    arguments = listOf(navArgument("templateId") { type = NavType.LongType })
                ) { backStackEntry ->
                    val templateId = backStackEntry.arguments?.getLong("templateId") ?: 0L
                    AddEditTransactionScreen(
                        viewModel = viewModel,
                        transactionId = null,
                        templateId = templateId,
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToCreateTemplate = { navController.navigate(NavRoutes.ADD_TEMPLATE) }
                    )
                }

                // ADD CATEGORY (Full-screen)
                composable(
                    route = NavRoutes.ADD_CATEGORY,
                    arguments = listOf(
                        navArgument("type") {
                            type = NavType.StringType
                            nullable = true
                            defaultValue = TransactionType.EXPENSE.name
                        }
                    )
                ) { backStackEntry ->
                    val typeString = backStackEntry.arguments?.getString("type") ?: TransactionType.EXPENSE.name
                    val initialType = TransactionType.fromString(typeString)
                    AddEditCategoryScreen(
                        viewModel = viewModel,
                        categoryId = null,
                        initialType = initialType,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                // EDIT CATEGORY (Full-screen)
                composable(
                    route = NavRoutes.EDIT_CATEGORY,
                    arguments = listOf(navArgument("categoryId") { type = NavType.LongType })
                ) { backStackEntry ->
                    val categoryId = backStackEntry.arguments?.getLong("categoryId") ?: 0L
                    AddEditCategoryScreen(
                        viewModel = viewModel,
                        categoryId = categoryId,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                // ADD TEMPLATE
                composable(NavRoutes.ADD_TEMPLATE) {
                    AddEditTemplateScreen(
                        viewModel = viewModel,
                        templateId = null,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                // EDIT TEMPLATE
                composable(
                    route = NavRoutes.EDIT_TEMPLATE,
                    arguments = listOf(navArgument("templateId") { type = NavType.LongType })
                ) { backStackEntry ->
                    val templateId = backStackEntry.arguments?.getLong("templateId") ?: 0L
                    AddEditTemplateScreen(
                        viewModel = viewModel,
                        templateId = templateId,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                // ADD WALLET (Full-screen)
                composable(NavRoutes.ADD_WALLET) {
                    AddEditWalletScreen(
                        viewModel = viewModel,
                        walletId = null,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                // EDIT WALLET (Full-screen)
                composable(
                    route = NavRoutes.EDIT_WALLET,
                    arguments = listOf(navArgument("walletId") { type = NavType.LongType })
                ) { backStackEntry ->
                    val walletId = backStackEntry.arguments?.getLong("walletId") ?: 0L
                    AddEditWalletScreen(
                        viewModel = viewModel,
                        walletId = walletId,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                // SETTINGS / DATA MANAGEMENT
                composable(NavRoutes.SETTINGS) {
                    SettingsScreen(
                        viewModel = viewModel,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}
