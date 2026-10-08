package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.PsycotimexproLogoBadge
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppTab
import com.example.ui.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val currentCoach by viewModel.currentCoach.collectAsState()
                if (currentCoach == null) {
                    LoginScreen(viewModel = viewModel)
                } else if (viewModel.isCoachSubscriptionExpired(currentCoach)) {
                    SubscriptionExpiredLockScreen(
                        coach = currentCoach!!,
                        onLogout = { viewModel.logout() }
                    )
                } else {
                    MainAppScreen(viewModel = viewModel)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(viewModel: MainViewModel) {
    val currentTab by viewModel.currentTab.collectAsState()
    val currentCoach by viewModel.currentCoach.collectAsState()

    // BackHandler: If user is on a secondary tab, pressing Back returns them to DASHBOARD (front screen)
    BackHandler(enabled = currentTab != AppTab.DASHBOARD) {
        viewModel.selectTab(AppTab.DASHBOARD)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = DarkBackground,
        topBar = {
            if (currentTab != AppTab.TIMING_CAM) {
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            PsycotimexproLogoBadge(size = 34.dp)
                            Column {
                                Text(
                                    text = "PSYCO TIME X PRO",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = currentCoach?.name ?: "Pusat Latihan",
                                    color = TextSecondary,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    },
                    actions = {
                        val unreadCount by viewModel.unreadInboxCount.collectAsState()
                        IconButton(onClick = { viewModel.selectTab(AppTab.COACH_PROFILE) }) {
                            BadgedBox(
                                badge = {
                                    if (unreadCount > 0) {
                                        Badge(containerColor = RacingRed) {
                                            Text("$unreadCount", color = Color.White, fontSize = 8.sp)
                                        }
                                    }
                                }
                            ) {
                                Icon(Icons.Default.AccountCircle, contentDescription = "Profil Coach", tint = if (currentTab == AppTab.COACH_PROFILE) RacingRed else Color.White)
                            }
                        }
                        IconButton(onClick = { viewModel.logout() }) {
                            Icon(Icons.Default.Logout, contentDescription = "Log Keluar", tint = TextMuted)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceDark)
                )
            }
        },
        bottomBar = {
            ScrollableNavigationBar(
                selectedTab = currentTab,
                onTabSelected = { viewModel.selectTab(it) }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                AppTab.DASHBOARD -> CoachDashboardScreen(viewModel = viewModel)
                AppTab.TIMING_CAM -> TimingCamScreen(viewModel = viewModel)
                AppTab.RANKING -> RankingScreen(viewModel = viewModel)
                AppTab.ATHLETES -> AthletesScreen(viewModel = viewModel)
                AppTab.SUB_COACHES -> SubCoachScreen(viewModel = viewModel)
                AppTab.SUBSCRIPTION -> SubscriptionScreen(viewModel = viewModel)
                AppTab.FEES -> FeesScreen(viewModel = viewModel)
                AppTab.ATTENDANCE -> AttendanceScreen(viewModel = viewModel)
                AppTab.AI_ANALYSIS -> AiRunnerAnalysisScreen(viewModel = viewModel)
                AppTab.AI_ROUTINE -> AiRoutineScreen(viewModel = viewModel)
                AppTab.WEB_PORTAL -> WebPortalScreen(viewModel = viewModel)
                AppTab.COACH_PROFILE -> CoachProfileScreen(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun ScrollableNavigationBar(
    selectedTab: AppTab,
    onTabSelected: (AppTab) -> Unit
) {
    NavigationBar(
        containerColor = SurfaceDark,
        tonalElevation = 8.dp,
        modifier = Modifier.testTag("bottom_nav_bar")
    ) {
        val navItems = listOf(
            Triple(AppTab.DASHBOARD, Icons.Default.Dashboard, "Papan Pemuka"),
            Triple(AppTab.TIMING_CAM, Icons.Default.Videocam, "Kamera ET"),
            Triple(AppTab.RANKING, Icons.Default.EmojiEvents, "Ranking Atlit"),
            Triple(AppTab.ATHLETES, Icons.Default.People, "Atlit"),
            Triple(AppTab.SUB_COACHES, Icons.Default.GroupAdd, "Sub-Coach"),
            Triple(AppTab.SUBSCRIPTION, Icons.Default.CardMembership, "Langganan"),
            Triple(AppTab.FEES, Icons.Default.Payment, "Yuran Atlit"),
            Triple(AppTab.WEB_PORTAL, Icons.Default.Language, "Portal Web")
        )

        navItems.forEach { (tab, icon, label) ->
            val isSelected = selectedTab == tab
            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabSelected(tab) },
                icon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        modifier = Modifier.size(19.dp)
                    )
                },
                label = {
                    Text(
                        text = label,
                        fontSize = 9.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        maxLines = 1
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.White,
                    selectedTextColor = RacingRed,
                    indicatorColor = RacingRed,
                    unselectedIconColor = TextSecondary,
                    unselectedTextColor = TextMuted
                ),
                modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
            )
        }
    }
}
