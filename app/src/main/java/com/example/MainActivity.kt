package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.navigation.Screen
import com.example.ui.MarginViewModelFactory
import com.example.ui.dashboard.DashboardScreen
import com.example.ui.add.AddTransactionScreen
import com.example.ui.recurring.RecurringScreen
import com.example.ui.recurring.RecurringViewModelFactory

import com.example.ui.categories.CategoriesScreen
import com.example.ui.categories.CategoriesViewModelFactory
import com.example.ui.history.HistoryScreen
import com.example.ui.history.HistoryViewModelFactory

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    
    val appContainer = application as MarginApplication
    val factory = MarginViewModelFactory(appContainer.repository)
    val recurringFactory = RecurringViewModelFactory(appContainer.repository)
    val categoriesFactory = CategoriesViewModelFactory(appContainer.repository)
    val historyFactory = HistoryViewModelFactory(appContainer.repository)

    setContent {
      MyApplicationTheme {
        val navController = rememberNavController()
        
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            NavHost(navController = navController, startDestination = Screen.Dashboard.route, modifier = Modifier.padding(innerPadding)) {
                composable(Screen.Dashboard.route) {
                    DashboardScreen(
                        viewModel = viewModel(factory = factory),
                        onAddClick = { navController.navigate(Screen.AddTransaction.route) },
                        onRecurringClick = { navController.navigate(Screen.Recurring.route) },
                        onCategoriesClick = { navController.navigate(Screen.Categories.route) },
                        onHistoryClick = { navController.navigate(Screen.History.route) }
                    )
                }
                composable(Screen.AddTransaction.route) {
                    AddTransactionScreen(
                        viewModel = viewModel(factory = factory),
                        onBack = { navController.popBackStack() }
                    )
                }
                composable(Screen.Recurring.route) {
                    RecurringScreen(
                        viewModel = viewModel(factory = recurringFactory),
                        onBack = { navController.popBackStack() }
                    )
                }
                composable(Screen.Categories.route) {
                    CategoriesScreen(
                        viewModel = viewModel(factory = categoriesFactory),
                        onBack = { navController.popBackStack() }
                    )
                }
                composable(Screen.History.route) {
                    HistoryScreen(
                        viewModel = viewModel(factory = historyFactory),
                        onBack = { navController.popBackStack() }
                    )
                }
            }
        }
      }
    }
  }
}
