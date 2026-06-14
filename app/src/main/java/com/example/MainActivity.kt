package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.BackHandler
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val mainViewModel: MainViewModel = viewModel()
            val isLightTheme by mainViewModel.isLightTheme.collectAsState()
            val currentTab by mainViewModel.currentTab.collectAsState()
            val currentUser by mainViewModel.currentUser.collectAsState()

            MyApplicationTheme(darkTheme = !isLightTheme) {
                BackHandler(enabled = currentTab != "Dashboard" && currentUser != null) {
                    mainViewModel.navigateTo("Dashboard")
                }

                val accentColor = if (isLightTheme) Color(0xFF4F46E5) else NeonPurple
                var showGlobalSearch by remember { mutableStateOf(false) }

                if (currentUser == null) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .drawBehind {
                                // Draw base background dependent on theme mode
                                val baseColor = if (isLightTheme) Color(0xFFF3F4F6) else Color(0xFF050508)
                                drawRect(color = baseColor)
                                
                                // Draw Indigo radial glow (top-right area)
                                val glowColorIndigo = if (isLightTheme) Color(0x2E6366F1) else Color(0x244F46E5)
                                drawCircle(
                                    brush = Brush.radialGradient(
                                        colors = listOf(glowColorIndigo, Color.Transparent),
                                        center = androidx.compose.ui.geometry.Offset(size.width * 0.85f, size.height * 0.15f),
                                        radius = size.width * 0.8f
                                    ),
                                    center = androidx.compose.ui.geometry.Offset(size.width * 0.85f, size.height * 0.15f),
                                    radius = size.width * 0.8f
                                )
                                
                                // Draw Purple/Pink radial glow (middle-left area)
                                val glowColorPurple = if (isLightTheme) Color(0x25A855F7) else Color(0x1E8B5CF6)
                                drawCircle(
                                    brush = Brush.radialGradient(
                                        colors = listOf(glowColorPurple, Color.Transparent),
                                        center = androidx.compose.ui.geometry.Offset(size.width * 0.15f, size.height * 0.5f),
                                        radius = size.width * 0.9f
                                    ),
                                    center = androidx.compose.ui.geometry.Offset(size.width * 0.15f, size.height * 0.5f),
                                    radius = size.width * 0.9f
                                )
                            }
                    ) {
                        AuthScreen(viewModel = mainViewModel)
                    }
                } else {
                    val textTitleColor = if (isLightTheme) Color(0xFF111827) else Color.White
                    val textSubTitleColor = if (isLightTheme) Color(0xFF4B5563) else TextSecondary

                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        topBar = {
                            TopAppBar(
                                title = {
                                    Column {
                                        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                                            Text(
                                                text = "DeepScholar",
                                                fontSize = 20.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = textTitleColor
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Box(
                                                modifier = Modifier
                                                    .background(
                                                        color = accentColor.copy(alpha = 0.15f),
                                                        shape = RoundedCornerShape(4.dp)
                                                    )
                                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = "PRO",
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Black,
                                                    color = accentColor,
                                                    letterSpacing = 1.sp
                                                )
                                            }
                                        }
                                        Text(
                                            text = "Welcome back, ${currentUser?.username ?: "Explorer"}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = textSubTitleColor
                                        )
                                    }
                                },
                                actions = {
                                    IconButton(
                                        onClick = { mainViewModel.toggleTheme() },
                                        modifier = Modifier.testTag("theme_toggle_button")
                                    ) {
                                        Icon(
                                            imageVector = if (isLightTheme) Icons.Default.DarkMode else Icons.Default.LightMode,
                                            contentDescription = "Toggle Theme",
                                            tint = accentColor
                                        )
                                    }
                                    IconButton(
                                        onClick = { showGlobalSearch = true },
                                        modifier = Modifier.testTag("global_search_trigger_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Search,
                                            contentDescription = "Search",
                                            tint = accentColor
                                        )
                                    }
                                },
                                colors = TopAppBarDefaults.topAppBarColors(
                                    containerColor = Color.Transparent,
                                    titleContentColor = TextPrimary
                                ),
                                modifier = Modifier.statusBarsPadding()
                            )
                        },
                        bottomBar = {
                            Box(
                                modifier = Modifier
                                    .navigationBarsPadding()
                                    .padding(horizontal = 16.dp, vertical = 12.dp)
                                    .background(
                                        color = if (isLightTheme) Color(0xF2FFFFFF) else Color(0xE6121218),
                                        shape = RoundedCornerShape(24.dp)
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = if (isLightTheme) Color(0x1B000000) else Color(0x1BFFFFFF),
                                        shape = RoundedCornerShape(24.dp)
                                    )
                            ) {
                                NavigationBar(
                                    containerColor = Color.Transparent,
                                    tonalElevation = 0.dp,
                                    modifier = Modifier.height(72.dp)
                                ) {
                                    val items = listOf(
                                        NavigationItem("Dashboard", Icons.Default.Dashboard, Icons.Outlined.Dashboard, "dashboard_tab"),
                                        NavigationItem("Research", Icons.Default.Science, Icons.Outlined.Science, "research_tab"),
                                        NavigationItem("Notes", Icons.Default.Article, Icons.Outlined.Article, "notes_tab"),
                                        NavigationItem("Planner", Icons.Default.CalendarToday, Icons.Outlined.CalendarToday, "planner_tab"),
                                        NavigationItem("Chatbot", Icons.Default.Forum, Icons.Outlined.Forum, "chatbot_tab"),
                                        NavigationItem("Profile", Icons.Default.Person, Icons.Outlined.Person, "profile_tab")
                                    )

                                    items.forEach { item ->
                                        val isSelected = currentTab == item.label
                                        NavigationBarItem(
                                            selected = isSelected,
                                            onClick = { mainViewModel.navigateTo(item.label) },
                                            icon = {
                                                Icon(
                                                    imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                                    contentDescription = item.label
                                                )
                                            },
                                            label = { Text(item.label, fontSize = 8.sp) },
                                            colors = NavigationBarItemDefaults.colors(
                                                selectedIconColor = accentColor,
                                                unselectedIconColor = if (isLightTheme) Color(0xFF6B7280) else TextMuted,
                                                selectedTextColor = accentColor,
                                                unselectedTextColor = if (isLightTheme) Color(0xFF6B7280) else TextMuted,
                                                indicatorColor = if (isLightTheme) accentColor.copy(alpha = 0.12f) else Color(0x1AFFFFFF)
                                            ),
                                            modifier = Modifier.testTag(item.testTag)
                                        )
                                    }
                                }
                            }
                        }
                    ) { innerPadding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .drawBehind {
                                    // Draw base background dependent on theme mode
                                    val baseColor = if (isLightTheme) Color(0xFFF3F4F6) else Color(0xFF050508)
                                    drawRect(color = baseColor)
                                    
                                    // Draw Indigo radial glow (top-right area)
                                    val glowColorIndigo = if (isLightTheme) Color(0x2E6366F1) else Color(0x244F46E5)
                                    drawCircle(
                                        brush = Brush.radialGradient(
                                            colors = listOf(glowColorIndigo, Color.Transparent),
                                            center = androidx.compose.ui.geometry.Offset(size.width * 0.85f, size.height * 0.15f),
                                            radius = size.width * 0.8f
                                        ),
                                        center = androidx.compose.ui.geometry.Offset(size.width * 0.85f, size.height * 0.15f),
                                        radius = size.width * 0.8f
                                    )
                                    
                                    // Draw Purple/Pink radial glow (middle-left area)
                                    val glowColorPurple = if (isLightTheme) Color(0x25A855F7) else Color(0x1E8B5CF6)
                                    drawCircle(
                                        brush = Brush.radialGradient(
                                            colors = listOf(glowColorPurple, Color.Transparent),
                                            center = androidx.compose.ui.geometry.Offset(size.width * 0.15f, size.height * 0.5f),
                                            radius = size.width * 0.9f
                                        ),
                                        center = androidx.compose.ui.geometry.Offset(size.width * 0.15f, size.height * 0.5f),
                                        radius = size.width * 0.9f
                                    )

                                    // Draw Blue radial glow (bottom-right area)
                                    val glowColorBlue = if (isLightTheme) Color(0x203B82F6) else Color(0x143B82F6)
                                    drawCircle(
                                        brush = Brush.radialGradient(
                                            colors = listOf(glowColorBlue, Color.Transparent),
                                            center = androidx.compose.ui.geometry.Offset(size.width * 0.75f, size.height * 0.85f),
                                            radius = size.width * 0.7f
                                        ),
                                        center = androidx.compose.ui.geometry.Offset(size.width * 0.75f, size.height * 0.85f),
                                        radius = size.width * 0.7f
                                    )
                                }
                                .padding(innerPadding)
                        ) {
                            AnimatedContent(
                                targetState = currentTab,
                                transitionSpec = {
                                    fadeIn() togetherWith fadeOut()
                                },
                                label = "ScreenNavigation"
                            ) { tab ->
                                when (tab) {
                                    "Dashboard" -> DashboardScreen(mainViewModel)
                                    "Research" -> ResearchScreen(mainViewModel)
                                    "Notes" -> NotesScreen(mainViewModel)
                                    "Planner" -> PlannerScreen(mainViewModel)
                                    "Chatbot" -> ChatbotScreen(mainViewModel)
                                    "Profile" -> ProfileScreen(mainViewModel)
                                    else -> DashboardScreen(mainViewModel)
                                }
                            }
                        }
                    }
                }

                // Global Interactive Search Overlay Screen
                if (showGlobalSearch) {
                    GlobalSearchOverlay(
                        viewModel = mainViewModel,
                        onDismiss = { showGlobalSearch = false }
                    )
                }
            }
        }
    }
}

data class NavigationItem(
    val label: String,
    val selectedIcon: androidx.compose.ui.graphics.vector.ImageVector,
    val unselectedIcon: androidx.compose.ui.graphics.vector.ImageVector,
    val testTag: String
)
