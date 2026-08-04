package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.example.ui.PriceMapperViewModel
import com.example.ui.screens.*
import com.example.ui.theme.PriceMapperTheme

sealed class Screen(val route: String, val title: String) {
    object Listas : Screen("listas", "Listas")
    object Produtos : Screen("produtos", "Produtos")
    object Lojas : Screen("lojas", "Lojas")
    object Historico : Screen("historico", "Histórico")
    object ListaDetail : Screen("lista_detail", "Detalhes da Lista")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PriceMapperTheme {
                MainAppScreen()
            }
        }
    }
}

@Composable
fun MainAppScreen(
    viewModel: PriceMapperViewModel = viewModel()
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val snackbarHostState = remember { SnackbarHostState() }
    val userMessage by viewModel.userMessage.collectAsState()

    LaunchedEffect(userMessage) {
        userMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearUserMessage()
        }
    }

    val bottomNavScreens = listOf(
        Screen.Listas,
        Screen.Produtos,
        Screen.Lojas,
        Screen.Historico
    )

    val showBottomBar = currentRoute in bottomNavScreens.map { it.route }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.navigationBars)
                ) {
                    bottomNavScreens.forEach { screen ->
                        val selected = currentRoute == screen.route
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                val icon = when (screen) {
                                    Screen.Listas -> if (selected) Icons.Filled.ShoppingCart else Icons.Outlined.ShoppingCart
                                    Screen.Produtos -> if (selected) Icons.Filled.LocalOffer else Icons.Outlined.LocalOffer
                                    Screen.Lojas -> if (selected) Icons.Filled.Storefront else Icons.Outlined.Storefront
                                    Screen.Historico -> if (selected) Icons.Filled.History else Icons.Outlined.History
                                    else -> Icons.Filled.Home
                                }
                                Icon(icon, contentDescription = screen.title)
                            },
                            label = { Text(screen.title) },
                            modifier = Modifier.testTag("nav_item_${screen.route}")
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Listas.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Listas.route) {
                ShoppingListsScreen(
                    viewModel = viewModel,
                    onOpenListDetail = { listId ->
                        navController.navigate(Screen.ListaDetail.route)
                    }
                )
            }

            composable(Screen.ListaDetail.route) {
                ListDetailScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Produtos.route) {
                ProductsScreen(viewModel = viewModel)
            }

            composable(Screen.Lojas.route) {
                StoresScreen(viewModel = viewModel)
            }

            composable(Screen.Historico.route) {
                BackupHistoryScreen(viewModel = viewModel)
            }
        }
    }
}
