package com.example.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Dashboard : Screen("dashboard", "Dashboard", Icons.Default.Home)
    object Analytics : Screen("analytics", "Analytics", Icons.Default.PieChart)
    object AddTransaction : Screen("add_transaction", "Add", Icons.Default.Add)
    object Categories : Screen("categories", "Categorie", Icons.Default.Settings)
    object Recurring : Screen("recurring", "Ricorrenti", Icons.Default.Refresh)
    object History : Screen("history", "Storico", Icons.Default.FilterList)
}
