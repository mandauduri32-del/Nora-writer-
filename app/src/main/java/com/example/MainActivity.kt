package com.example

import android.graphics.Color as AndroidColor
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ui.components.AppBackgroundContainer
import com.example.ui.components.AppSideDrawer
import com.example.ui.components.GlobalSearchDialog
import com.example.ui.navigation.Screen
import com.example.ui.screens.bookdetail.BookDetailScreen
import com.example.ui.screens.editor.EditorScreen
import com.example.ui.screens.files.FilesScreen
import com.example.ui.screens.library.LibraryScreen
import com.example.ui.screens.lock.PinLockScreen
import com.example.ui.screens.notes.NotesScreen
import com.example.ui.screens.settings.SettingsScreen
import com.example.ui.screens.statistics.StatisticsScreen
import com.example.ui.screens.trash.TrashScreen
import com.example.ui.theme.NeonPink
import com.example.ui.theme.NovelDeskTheme
import com.example.viewmodel.EditorViewModel
import com.example.viewmodel.MainViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val mainViewModel: MainViewModel by viewModels()
    private val editorViewModel: EditorViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val preferences by mainViewModel.preferences.collectAsStateWithLifecycle()
            val isUnlocked by mainViewModel.isUnlocked.collectAsStateWithLifecycle()
            val isSystemDark = isSystemInDarkTheme()
            val isDarkTheme = when (preferences.themeMode.uppercase()) {
                "LIGHT", "SEPIA" -> false
                "SYSTEM" -> isSystemDark
                else -> true
            }

            DisposableEffect(isDarkTheme) {
                enableEdgeToEdge(
                    statusBarStyle = if (isDarkTheme) {
                        SystemBarStyle.dark(AndroidColor.TRANSPARENT)
                    } else {
                        SystemBarStyle.light(AndroidColor.TRANSPARENT, AndroidColor.TRANSPARENT)
                    },
                    navigationBarStyle = if (isDarkTheme) {
                        SystemBarStyle.dark(AndroidColor.TRANSPARENT)
                    } else {
                        SystemBarStyle.light(AndroidColor.TRANSPARENT, AndroidColor.TRANSPARENT)
                    }
                )
                onDispose {}
            }

            NovelDeskTheme(themeMode = preferences.themeMode) {
                AppBackgroundContainer(preferences = preferences) {
                    val hasPin = preferences.securityPin.isNotBlank()
                    if (hasPin && !isUnlocked) {
                        PinLockScreen(
                            correctPin = preferences.securityPin,
                            onSuccess = { mainViewModel.unlockApp() }
                        )
                    } else {
                        NovelDeskApp(
                            mainViewModel = mainViewModel,
                            editorViewModel = editorViewModel
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NovelDeskApp(
    mainViewModel: MainViewModel,
    editorViewModel: EditorViewModel
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    var showGlobalSearch by remember { mutableStateOf(false) }

    // 4 Bottom Navigation Bar Items matching Screenshots (Buku, Berkas, Catatan, Statistik)
    val bottomNavItems = listOf(
        NavigationItem(Screen.Library.route, "Buku", Icons.Default.MenuBook),
        NavigationItem(Screen.Dashboard.route, "Berkas", Icons.Default.Folder),
        NavigationItem(Screen.Notes.route, "Catatan", Icons.Default.Layers),
        NavigationItem(Screen.Statistics.route, "Statistik", Icons.Default.TrendingUp)
    )

    val shouldShowBottomBar = currentRoute in listOf(
        Screen.Library.route,
        Screen.Dashboard.route,
        Screen.Notes.route,
        Screen.Statistics.route
    )

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AppSideDrawer(
                viewModel = mainViewModel,
                onNavigateToTrash = {
                    navController.navigate(Screen.Trash.route)
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                },
                onCloseDrawer = {
                    coroutineScope.launch { drawerState.close() }
                }
            )
        }
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                if (shouldShowBottomBar) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.98f),
                        tonalElevation = 6.dp,
                        modifier = Modifier.testTag("main_bottom_nav")
                    ) {
                        bottomNavItems.forEach { item ->
                            val selected = currentRoute == item.route
                            NavigationBarItem(
                                selected = selected,
                                onClick = {
                                    if (currentRoute != item.route) {
                                        navController.navigate(item.route) {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                },
                                icon = {
                                    Icon(
                                        imageVector = item.icon,
                                        contentDescription = item.label
                                    )
                                },
                                label = {
                                    Text(
                                        text = item.label,
                                        fontSize = 11.sp,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.onSurface,
                                    selectedTextColor = MaterialTheme.colorScheme.onSurface,
                                    indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                modifier = Modifier.testTag("nav_item_${item.route}")
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = Screen.Library.route, // Default screen "Buku" (Screenshot 2)
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .consumeWindowInsets(innerPadding)
            ) {
                // Tab 1: "Buku" (Screenshot 2)
                composable(Screen.Library.route) {
                    LibraryScreen(
                        viewModel = mainViewModel,
                        onNavigateToBookDetail = { bookId ->
                            navController.navigate(Screen.BookDetail.createRoute(bookId))
                        },
                        onNavigateToEditor = { type, id ->
                            navController.navigate(Screen.Editor.createRoute(type, id))
                        },
                        onOpenDrawer = {
                            coroutineScope.launch { drawerState.open() }
                        },
                        onOpenSearch = { showGlobalSearch = true }
                    )
                }

                // Tab 2: "Berkas" (Screenshot 1)
                composable(Screen.Dashboard.route) {
                    FilesScreen(
                        viewModel = mainViewModel,
                        onNavigateToBookDetail = { bookId ->
                            navController.navigate(Screen.BookDetail.createRoute(bookId))
                        },
                        onNavigateToEditor = { type, id ->
                            navController.navigate(Screen.Editor.createRoute(type, id))
                        },
                        onOpenDrawer = {
                            coroutineScope.launch { drawerState.open() }
                        },
                        onOpenSearch = { showGlobalSearch = true }
                    )
                }

                // Tab 3: "Catatan" (Screenshot 3)
                composable(Screen.Notes.route) {
                    NotesScreen(
                        viewModel = mainViewModel,
                        onNavigateToEditor = { type, id ->
                            navController.navigate(Screen.Editor.createRoute(type, id))
                        },
                        onOpenDrawer = {
                            coroutineScope.launch { drawerState.open() }
                        },
                        onOpenSearch = { showGlobalSearch = true }
                    )
                }

                // Tab 4: "Statistik" (Screenshot 5)
                composable(Screen.Statistics.route) {
                    StatisticsScreen(
                        viewModel = mainViewModel,
                        onOpenDrawer = {
                            coroutineScope.launch { drawerState.open() }
                        }
                    )
                }

                // Sub-screen: Pengaturan (Accessible via Drawer or BookDetail)
                composable(Screen.Settings.route) {
                    SettingsScreen(
                        viewModel = mainViewModel,
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToTrash = {
                            navController.navigate(Screen.Trash.route)
                        }
                    )
                }

                // Sub-screen: Tempat Sampah (Accessible via Drawer or Settings)
                composable(Screen.Trash.route) {
                    TrashScreen(
                        viewModel = mainViewModel,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                // Sub-screen: Detail Buku & Bab
                composable(
                    route = Screen.BookDetail.route,
                    arguments = listOf(navArgument("bookId") { type = NavType.LongType })
                ) { backStackEntry ->
                    val bookId = backStackEntry.arguments?.getLong("bookId") ?: 0L
                    BookDetailScreen(
                        bookId = bookId,
                        viewModel = mainViewModel,
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToEditor = { type, id ->
                            navController.navigate(Screen.Editor.createRoute(type, id))
                        }
                    )
                }

                // Sub-screen: Editor Penulisan
                composable(
                    route = Screen.Editor.route,
                    arguments = listOf(
                        navArgument("docType") { type = NavType.StringType },
                        navArgument("docId") { type = NavType.LongType }
                    )
                ) { backStackEntry ->
                    val docType = backStackEntry.arguments?.getString("docType") ?: "CHAPTER"
                    val docId = backStackEntry.arguments?.getLong("docId") ?: 0L
                    EditorScreen(
                        docType = docType,
                        docId = docId,
                        viewModel = editorViewModel,
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToChapter = { nextChapterId ->
                            navController.navigate(Screen.Editor.createRoute("CHAPTER", nextChapterId)) {
                                popUpTo(Screen.Editor.route) { inclusive = true }
                            }
                        }
                    )
                }
            }
        }
    }

    // Global Search Modal
    if (showGlobalSearch) {
        GlobalSearchDialog(
            viewModel = mainViewModel,
            onNavigateToBook = { bookId ->
                navController.navigate(Screen.BookDetail.createRoute(bookId))
            },
            onNavigateToEditor = { type, id ->
                navController.navigate(Screen.Editor.createRoute(type, id))
            },
            onDismiss = { showGlobalSearch = false }
        )
    }
}

private data class NavigationItem(
    val route: String,
    val label: String,
    val icon: ImageVector
)
