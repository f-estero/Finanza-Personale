package com.example.ui.dashboard

import android.content.Intent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.TransactionType
import com.example.data.TransactionWithCategory
import com.example.ui.DashboardState
import com.example.ui.DashboardViewModel
import com.example.util.CsvExporter
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import androidx.compose.material.icons.filled.Settings

import com.example.ui.TrendPoint

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(viewModel: DashboardViewModel, onAddClick: () -> Unit, onRecurringClick: () -> Unit = {}, onCategoriesClick: () -> Unit = {}, onHistoryClick: () -> Unit = {}) {
    val state by viewModel.dashboardState.collectAsStateWithLifecycle()
    val monthLabel by viewModel.selectedMonthLabel.collectAsStateWithLifecycle()
    val isCurrentMonth by viewModel.isCurrentMonth.collectAsStateWithLifecycle()
    val trendData by viewModel.trendData.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dashboard") },
                actions = {
                    IconButton(onClick = onHistoryClick) {
                        Icon(Icons.Default.FilterList, contentDescription = "Storico Movimenti")
                    }
                    IconButton(onClick = {
                        val uri = CsvExporter.exportToCsv(context, state.recentTransactions)
                        val sendIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/csv"
                            putExtra(Intent.EXTRA_STREAM, uri)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "Esporta CSV"))
                    }) {
                        Icon(Icons.Default.Share, contentDescription = "Esporta CSV")
                    }
                    IconButton(onClick = onRecurringClick) {
                        Icon(Icons.Default.Refresh, contentDescription = "Ricorrenti")
                    }
                    IconButton(onClick = onCategoriesClick) {
                        Icon(Icons.Default.Settings, contentDescription = "Categorie")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                icon = { Icon(Icons.Default.Add, contentDescription = "Aggiungi Movimento") },
                text = { Text("Aggiungi", fontWeight = FontWeight.Bold) }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item { Spacer(modifier = Modifier.height(16.dp)) }
            
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { viewModel.goToPreviousMonth() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Precedente")
                    }
                    Text(monthLabel, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    IconButton(onClick = { viewModel.goToNextMonth() }, enabled = !isCurrentMonth) {
                        Icon(Icons.Default.ArrowForward, contentDescription = "Successivo", tint = if (isCurrentMonth) MaterialTheme.colorScheme.onSurface.copy(alpha=0.3f) else MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
            
            item {
                MainBalanceCard(state)
            }
            
            item {
                Text("Struttura Budget", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold))
                Spacer(modifier = Modifier.height(16.dp))
                BudgetStructureCards(state)
            }

            item {
                Text("Movimenti del mese", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold))
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (state.recentTransactions.isEmpty()) {
                item {
                    Text("Nessun movimento nel mese.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                items(state.recentTransactions, key = { it.transaction.id }) { tx ->
                    val dismissState = rememberSwipeToDismissBoxState(
                        confirmValueChange = {
                            if (it == SwipeToDismissBoxValue.EndToStart || it == SwipeToDismissBoxValue.StartToEnd) {
                                viewModel.deleteTransaction(tx.transaction)
                                true
                            } else false
                        }
                    )
                    SwipeToDismissBox(
                        state = dismissState,
                        backgroundContent = {
                            val color = MaterialTheme.colorScheme.error
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(color, RoundedCornerShape(16.dp))
                                    .padding(vertical = 12.dp, horizontal = 20.dp),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Elimina", tint = Color.White)
                            }
                        }
                    ) {
                        Surface(color = MaterialTheme.colorScheme.background) {
                            TransactionRow(tx, state.recentTransactions)
                        }
                    }
                }
            }

            if (trendData.isNotEmpty()) {
                item { Spacer(modifier = Modifier.height(16.dp)) }
                item {
                    Text("Trend Ultimi 6 Mesi", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold))
                    Spacer(modifier = Modifier.height(8.dp))
                    TrendCard(trendData)
                }
            }

            item { Spacer(modifier = Modifier.height(100.dp)) }
        }
    }
}

@Composable
fun TrendCard(trends: List<TrendPoint>) {
    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.ITALY)
    val primaryColor = MaterialTheme.colorScheme.primary
    val trackColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
    val onSurface = MaterialTheme.colorScheme.onSurface
    
    Card(
        modifier = Modifier.fillMaxWidth().height(200.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(24.dp)
    ) {
        if (trends.size < 2) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Dati insufficienti.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            Canvas(modifier = Modifier.fillMaxSize().padding(24.dp)) {
                val maxMargin = trends.map { it.margin }.maxOrNull()?.coerceAtLeast(0.0) ?: 0.0
                val minMargin = trends.map { it.margin }.minOrNull()?.coerceAtMost(0.0) ?: 0.0
                val range = (maxMargin - minMargin).takeIf { it > 0.0 } ?: 1.0
                
                val w = size.width
                val h = size.height
                val stepX = w / (trends.size - 1).coerceAtLeast(1)
                
                val points = trends.mapIndexed { index, point ->
                    val x = index * stepX
                    val y = h - ((point.margin - minMargin) / range * h).toFloat()
                    Offset(x, y)
                }
                
                // Draw 0 line if it's within range
                if (minMargin < 0 && maxMargin > 0) {
                    val yZero = h - ((0.0 - minMargin) / range * h).toFloat()
                    drawLine(color = trackColor, start = Offset(0f, yZero), end = Offset(w, yZero), strokeWidth = 2f)
                }
                
                // Draw line connecting points
                for (i in 0 until points.size - 1) {
                    drawLine(
                        color = primaryColor,
                        start = points[i],
                        end = points[i+1],
                        strokeWidth = 6f,
                        cap = StrokeCap.Round
                    )
                }
                
                // Draw points and labels
                points.forEachIndexed { i, pt ->
                    val pointData = trends[i]
                    val color = if (pointData.isCurrentMonth) primaryColor else onSurface
                    drawCircle(color = color, radius = if(pointData.isCurrentMonth) 12f else 8f, center = pt)
                }
            }
        }
    }
}

@Composable
fun MainBalanceCard(state: DashboardState) {
    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.ITALY)
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        shape = RoundedCornerShape(28.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp).fillMaxWidth()) {
            Text("Margine Reale Residuo", color = MaterialTheme.colorScheme.onPrimaryContainer, style = MaterialTheme.typography.labelLarge)
            Spacer(modifier = Modifier.height(8.dp))
            Text(currencyFormatter.format(state.margineReale), color = MaterialTheme.colorScheme.onPrimaryContainer, style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Bold))
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Budget Resp.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
                    Text(currencyFormatter.format(state.budgetDisponibile), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold), color = MaterialTheme.colorScheme.onPrimaryContainer)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Spese Variabili", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
                    Text(currencyFormatter.format(state.speseVariabili), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold), color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

@Composable
fun BudgetStructureCards(state: DashboardState) {
    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.ITALY)
    
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        Card(
            modifier = Modifier.weight(1f).aspectRatio(1.1f),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp).fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
                Text("Entrate\nTotali", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.titleMedium, lineHeight = 20.sp)
                Text(currencyFormatter.format(state.entrate), color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
            }
        }

        Card(
            modifier = Modifier.weight(1f).aspectRatio(1.1f),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp).fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
                Text("Fisse +\nInvest.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.titleMedium, lineHeight = 20.sp)
                Text(currencyFormatter.format(state.speseFisse + state.investimenti), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
            }
        }

        Card(
            modifier = Modifier.weight(1f).aspectRatio(1.1f),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
            shape = RoundedCornerShape(24.dp)
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize().padding(16.dp)) {
                val primaryColor = MaterialTheme.colorScheme.error
                val trackColor = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.2f)
                val ratio = (state.percentualSuperflua / 100f).coerceIn(0.0, 1.0).toFloat()
                
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawArc(color = trackColor, startAngle = 0f, sweepAngle = 360f, useCenter = false, style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round))
                    drawArc(color = primaryColor, startAngle = -90f, sweepAngle = 360f * ratio, useCenter = false, style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round))
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("${state.percentualSuperflua.toInt()}%", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onErrorContainer))
                    Text("Superfluo", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onErrorContainer)
                }
            }
        }
    }
}

@Composable
fun TransactionRow(tx: TransactionWithCategory, allMonthTransactions: List<TransactionWithCategory>) {
    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.ITALY)
    val dateFormat = SimpleDateFormat("dd MMM, HH:mm", Locale.ITALY)
    
    val isPositive = tx.category.type == TransactionType.ENTRATA
    
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(android.graphics.Color.parseColor(tx.category.colorHex))),
                    contentAlignment = Alignment.Center
                ) {
                    Text(tx.category.name.take(1).uppercase(), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                }
                
                Column {
                    Text(tx.category.name, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold))
                    val desc = listOfNotNull(
                        tx.transaction.note.takeIf { it?.isNotBlank() == true }, 
                        dateFormat.format(Date(tx.transaction.dateMillis))
                    ).joinToString(" • ")
                    Text(desc, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            
            val color = if (isPositive) MaterialTheme.colorScheme.primary else if (tx.transaction.isSuperfluous) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
            val sign = if (isPositive) "+" else "-"
            
            Text(
                "$sign${currencyFormatter.format(tx.transaction.amount)}",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = color
            )
        }

        if (tx.category.monthlyBudget != null && tx.category.monthlyBudget > 0 && !isPositive) {
            val totalConsumed = allMonthTransactions.filter { it.category.id == tx.category.id }.sumOf { it.transaction.amount }
            val progress = (totalConsumed / tx.category.monthlyBudget).toFloat().coerceIn(0f, 1f)
            val barColor = if (progress >= 1f) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
            
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.weight(1f).height(6.dp).clip(RoundedCornerShape(3.dp)),
                    color = barColor,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "${currencyFormatter.format(totalConsumed)} / ${currencyFormatter.format(tx.category.monthlyBudget)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
