package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.rounded.AdminPanelSettings
import androidx.compose.material.icons.rounded.Analytics
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.DownloadDone
import androidx.compose.material.icons.rounded.GetApp
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.data.ai.GpaAiPredictor
import com.example.data.firebase.configureAppCheck
import com.example.data.local.AppDatabase
import com.example.data.local.UserPreferencesRepository
import com.example.data.network.ConnectivityObserver
import com.example.data.repository.AcademicRepository
import com.example.ui.MainViewModel
import com.example.ui.auth.AuthScreen
import com.example.ui.auth.signOut
import com.example.ui.components.InstallCompanionDialog
import com.example.ui.components.OfflineNotificationBanner
import com.example.ui.components.SyntheticaTopBar
import com.example.ui.screens.AnalyticsScreen
import com.example.ui.screens.BlogAdminScreen
import com.example.ui.screens.BlogDetailScreen
import com.example.ui.screens.BlogsScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.PredictScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.theme.LocalSyntheticaColors
import com.example.ui.theme.SyntheticaTheme
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Mandatory Step 1: Configure Firebase App Check with launch intent before UI/ViewModels
        configureAppCheck(this, intent)

        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current
            val prefs = remember { UserPreferencesRepository(context.applicationContext) }
            val darkTheme by prefs.darkThemeEnabled.collectAsStateWithLifecycle(initialValue = false)

            SyntheticaTheme(darkTheme = darkTheme) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    GpaPredictorRoot()
                }
            }
        }
    }
}

private fun FirebaseAuth.authStateFlow(): Flow<FirebaseUser?> = callbackFlow {
    val listener = FirebaseAuth.AuthStateListener { auth ->
        trySend(auth.currentUser)
    }
    addAuthStateListener(listener)
    awaitClose { removeAuthStateListener(listener) }
}

private data class NavTabItem(
    val route: String,
    val baseRoute: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

@Composable
private fun GpaPredictorRoot() {
    val auth = Firebase.auth
    val currentUser by auth.authStateFlow().collectAsStateWithLifecycle(initialValue = auth.currentUser)
    val user = currentUser

    if (user == null) {
        AuthScreen(onAuthSuccess = {})
    } else {
        key(user.uid) {
            AuthenticatedApp(
                currentUserId = user.uid,
                userDisplayName = user.displayName ?: "Academic Scholar",
                userEmail = user.email ?: ""
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AuthenticatedApp(
    currentUserId: String,
    userDisplayName: String,
    userEmail: String
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }

    val viewModel: MainViewModel = viewModel(
        key = currentUserId,
        factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY])
                val databaseId = app.getString(R.string.firestore_database_id)
                val db = FirebaseFirestore.getInstance(databaseId)
                val offlineDao = AppDatabase.getInstance(app).offlineDao()
                val prefs = UserPreferencesRepository(app)
                val connectivity = ConnectivityObserver(app)
                val repo = AcademicRepository(
                    db = db,
                    offlineDao = offlineDao,
                    aiPredictor = GpaAiPredictor(),
                    prefs = prefs,
                    auth = Firebase.auth
                )
                MainViewModel(
                    repository = repo,
                    prefs = prefs,
                    connectivityObserver = connectivity,
                    currentUserId = currentUserId,
                    userDisplayName = userDisplayName,
                    userEmail = userEmail
                )
            }
        }
    )

    val state by viewModel.dashboardState.collectAsStateWithLifecycle()
    val predictionRunState by viewModel.predictionRunState.collectAsStateWithLifecycle()
    val bannerMessage by viewModel.bannerMessage.collectAsStateWithLifecycle()

    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: "home"
    val snackbarHostState = remember { SnackbarHostState() }

    var showInstallDialog by remember { mutableStateOf(false) }
    var showMoreSheet by remember { mutableStateOf(false) }

    LaunchedEffect(bannerMessage) {
        val msg = bannerMessage
        if (!msg.isNullOrBlank()) {
            snackbarHostState.showSnackbar(msg)
            viewModel.clearBannerMessage()
        }
    }

    val allSixTabs = remember {
        listOf(
            NavTabItem("home", "home", "Home", Icons.Rounded.Home, Icons.Outlined.Home),
            NavTabItem("predict", "predict", "Predict", Icons.Rounded.AutoAwesome, Icons.Outlined.AutoAwesome),
            NavTabItem("analytics", "analytics", "Analytics", Icons.Rounded.Analytics, Icons.Outlined.Analytics),
            NavTabItem("blogs?offlineOnly=false", "blogs", "Blogs", Icons.Rounded.MenuBook, Icons.Outlined.MenuBook),
            NavTabItem("history", "history", "History", Icons.Rounded.History, Icons.Outlined.History),
            NavTabItem("profile", "profile", "Profile", Icons.Rounded.Person, Icons.Outlined.Person)
        )
    }

    val compactBottomTabs = remember(allSixTabs) { allSixTabs.take(4) }

    val screenTitle = when {
        currentRoute.startsWith("home") -> "GPA Cockpit"
        currentRoute.startsWith("predict") -> "AI Predictor"
        currentRoute.startsWith("analytics") -> "Analytics"
        currentRoute.startsWith("blogs") -> "Knowledge Hub"
        currentRoute.startsWith("blog_detail") -> "Editorial Reader"
        currentRoute.startsWith("blog_admin") -> "Blog Admin CMS"
        currentRoute.startsWith("history") -> "History"
        currentRoute.startsWith("profile") -> "Profile"
        else -> "GPA Predictor"
    }

    val hideTopBar = currentRoute.startsWith("blog_detail")

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useRail = maxWidth >= 600.dp

        Row(modifier = Modifier.fillMaxSize()) {
            if (useRail) {
                NavigationRail(
                    modifier = Modifier.fillMaxHeight()
                ) {
                    Spacer(Modifier.height(12.dp))
                    allSixTabs.forEach { tab ->
                        val selected = currentRoute.startsWith(tab.baseRoute)
                        NavigationRailItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(tab.route) {
                                    popUpTo("home") { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                    contentDescription = tab.label
                                )
                            },
                            label = { Text(tab.label) }
                        )
                    }
                }
            }

            Scaffold(
                modifier = Modifier.weight(1f),
                contentWindowInsets = WindowInsets.safeDrawing,
                snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                topBar = {
                    if (!hideTopBar) {
                        Column {
                            SyntheticaTopBar(
                                title = screenTitle,
                                subtitle = "Synthetica Academic AI",
                                isOnline = state.isOnline,
                                isSyncing = state.syncStatus.isSyncing,
                                pendingCount = state.pendingPredictions.size,
                                companionInstalled = state.companionInstalled,
                                onRetrySync = { viewModel.syncPendingQueue(silentIfEmpty = false) },
                                onInstallClick = { showInstallDialog = true }
                            )
                            OfflineNotificationBanner(
                                visible = !state.isOnline,
                                pendingCount = state.pendingPredictions.size,
                                onRetryClick = { viewModel.syncPendingQueue(silentIfEmpty = false) }
                            )
                        }
                    }
                },
                bottomBar = {
                    if (!useRail && !hideTopBar) {
                        NavigationBar {
                            compactBottomTabs.forEach { tab ->
                                val selected = currentRoute.startsWith(tab.baseRoute)
                                NavigationBarItem(
                                    selected = selected,
                                    onClick = {
                                        navController.navigate(tab.route) {
                                            popUpTo("home") { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    },
                                    icon = {
                                        Icon(
                                            imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                            contentDescription = tab.label
                                        )
                                    },
                                    label = { Text(tab.label) },
                                    modifier = Modifier.testTag("nav_${tab.baseRoute}")
                                )
                            }
                            val moreSelected = currentRoute.startsWith("history") ||
                                currentRoute.startsWith("profile") ||
                                currentRoute.startsWith("blog_admin")
                            NavigationBarItem(
                                selected = moreSelected,
                                onClick = { showMoreSheet = true },
                                icon = {
                                    Icon(
                                        imageVector = if (moreSelected) Icons.Rounded.MoreHoriz else Icons.Outlined.MoreHoriz,
                                        contentDescription = "More"
                                    )
                                },
                                label = { Text("More") },
                                modifier = Modifier.testTag("nav_more")
                            )
                        }
                    }
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    NavHost(
                        navController = navController,
                        startDestination = "home"
                    ) {
                        composable("home") {
                            HomeScreen(
                                state = state,
                                onNavigateToPredict = { navController.navigate("predict") },
                                onNavigateToAnalytics = { navController.navigate("analytics") },
                                onNavigateToBlogs = { offlineOnly ->
                                    navController.navigate("blogs?offlineOnly=$offlineOnly")
                                },
                                onNavigateToHistory = { navController.navigate("history") },
                                onNavigateToBlogAdmin = { navController.navigate("blog_admin") },
                                onOpenBlogDetail = { articleId ->
                                    navController.navigate("blog_detail/$articleId")
                                },
                                onInstallClick = { showInstallDialog = true },
                                onDismissInstallBanner = { viewModel.dismissInstallBanner() },
                                onSyncNow = { viewModel.syncPendingQueue(silentIfEmpty = false) }
                            )
                        }

                        composable("predict") {
                            PredictScreen(
                                state = state,
                                runState = predictionRunState,
                                onRunPrediction = { input -> viewModel.runPrediction(input) },
                                onSyncPending = { viewModel.syncPendingQueue(silentIfEmpty = false) },
                                onDeletePending = { id -> viewModel.deletePendingPrediction(id) }
                            )
                        }

                        composable("analytics") {
                            AnalyticsScreen(
                                state = state,
                                onAddCourse = { code, title, cr, score, att, target, diff ->
                                    viewModel.addCourse(code, title, cr, score, att, target, diff)
                                },
                                onLoadDemoCourses = { viewModel.loadDemoSemesterCourses() },
                                onDeleteCourse = { id -> viewModel.deleteCourse(id) }
                            )
                        }

                        composable(
                            route = "blogs?offlineOnly={offlineOnly}",
                            arguments = listOf(
                                navArgument("offlineOnly") {
                                    type = NavType.BoolType
                                    defaultValue = false
                                }
                            )
                        ) { entry ->
                            val offlineOnly = entry.arguments?.getBoolean("offlineOnly") ?: false
                            BlogsScreen(
                                state = state,
                                initialSavedOfflineOnly = offlineOnly,
                                onOpenArticle = { id -> navController.navigate("blog_detail/$id") },
                                onToggleBookmark = { item -> viewModel.toggleBookmark(item) },
                                onToggleOfflineSave = { item -> viewModel.toggleOfflineSave(item) },
                                onOpenAdminManager = { navController.navigate("blog_admin") }
                            )
                        }

                        composable(
                            route = "blog_detail/{articleId}",
                            arguments = listOf(navArgument("articleId") { type = NavType.StringType })
                        ) { entry ->
                            val articleId = entry.arguments?.getString("articleId") ?: ""
                            val currentItem = state.blogs.firstOrNull { it.article.id == articleId }
                            val related = state.blogs
                                .filter { it.article.id != articleId && it.article.published }
                                .sortedByDescending { it.article.category == currentItem?.article?.category }
                                .take(3)
                            BlogDetailScreen(
                                item = currentItem,
                                relatedArticles = related,
                                onBack = { navController.popBackStack() },
                                onToggleBookmark = { item -> viewModel.toggleBookmark(item) },
                                onToggleOfflineSave = { item -> viewModel.toggleOfflineSave(item) },
                                onOpenRelated = { relId ->
                                    navController.navigate("blog_detail/$relId")
                                }
                            )
                        }

                        composable("blog_admin") {
                            BlogAdminScreen(
                                state = state,
                                onBack = { navController.popBackStack() },
                                onSavePost = { existingId, scopeKey, title, subtitle, author, role, cat, tags, cover, min, body, pub, feat, cb ->
                                    viewModel.saveAdminBlogPost(
                                        existingId = existingId,
                                        originScope = scopeKey,
                                        title = title,
                                        subtitle = subtitle,
                                        authorName = author,
                                        authorRole = role,
                                        category = cat,
                                        tagsRaw = tags,
                                        coverKey = cover,
                                        readMinutes = min,
                                        contentMarkdown = body,
                                        published = pub,
                                        featured = feat,
                                        onComplete = cb
                                    )
                                },
                                onTogglePublish = { item -> viewModel.togglePublishBlog(item) },
                                onDeletePost = { item -> viewModel.deleteBlogPost(item) }
                            )
                        }

                        composable("history") {
                            HistoryScreen(
                                state = state,
                                onSyncPending = { viewModel.syncPendingQueue(silentIfEmpty = false) },
                                onDeletePending = { id -> viewModel.deletePendingPrediction(id) },
                                onDeletePrediction = { id -> viewModel.deletePrediction(id) }
                            )
                        }

                        composable("profile") {
                            ProfileScreen(
                                state = state,
                                onSaveProfile = { name, major, uni, sem, cur, tgt, cr, goal ->
                                    viewModel.updateProfile(name, major, uni, sem, cur, tgt, cr, goal)
                                },
                                onToggleDarkTheme = { viewModel.setDarkTheme(it) },
                                onToggleOfflineSimulation = { viewModel.setOfflineSimulation(it) },
                                onToggleAdminWorkspace = { viewModel.setAdminWorkspace(it) },
                                onOpenBlogAdmin = { navController.navigate("blog_admin") },
                                onSyncNow = { viewModel.syncPendingQueue(silentIfEmpty = false) },
                                onInstallAppClick = { showInstallDialog = true },
                                onSignOut = { signOut(credentialManager, scope) }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showMoreSheet) {
        val synth = LocalSyntheticaColors.current
        ModalBottomSheet(onDismissRequest = { showMoreSheet = false }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "MORE WORKSPACES",
                    style = MaterialTheme.typography.labelSmall,
                    color = synth.royalBlue,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
                ListItem(
                    headlineContent = { Text("Prediction History") },
                    supportingContent = { Text("${state.predictions.size} saved • ${state.pendingPredictions.size} pending sync") },
                    leadingContent = { Icon(Icons.Rounded.History, contentDescription = null) },
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            showMoreSheet = false
                            navController.navigate("history")
                        }
                        .testTag("more_history_item")
                )
                ListItem(
                    headlineContent = { Text("Saved Offline Articles") },
                    supportingContent = { Text("${state.offlineArticlesCount} downloaded for offline reading") },
                    leadingContent = { Icon(Icons.Rounded.DownloadDone, contentDescription = null, tint = synth.teal) },
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            showMoreSheet = false
                            navController.navigate("blogs?offlineOnly=true")
                        }
                        .testTag("more_offline_blogs_item")
                )
                ListItem(
                    headlineContent = { Text("Student Profile & Offline Settings") },
                    supportingContent = { Text("Role access, dark theme & sync diagnostics") },
                    leadingContent = { Icon(Icons.Rounded.Person, contentDescription = null) },
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            showMoreSheet = false
                            navController.navigate("profile")
                        }
                        .testTag("more_profile_item")
                )
                if (state.isAdminActive) {
                    ListItem(
                        headlineContent = { Text("Admin Blog Management") },
                        supportingContent = { Text("Create, edit, publish & feature editorial posts") },
                        leadingContent = { Icon(Icons.Rounded.AdminPanelSettings, contentDescription = null, tint = synth.purple) },
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                showMoreSheet = false
                                navController.navigate("blog_admin")
                            }
                            .testTag("more_admin_blogs_item")
                    )
                }
                ListItem(
                    headlineContent = { Text(if (state.companionInstalled) "App Installed ✓" else "Install App") },
                    supportingContent = { Text("Pin home screen shortcut & Chrome PWA instructions") },
                    leadingContent = { Icon(Icons.Rounded.GetApp, contentDescription = null, tint = synth.royalBlue) },
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            showMoreSheet = false
                            showInstallDialog = true
                        }
                        .testTag("more_install_app_item")
                )
                Spacer(Modifier.height(12.dp))
            }
        }
    }

    if (showInstallDialog) {
        InstallCompanionDialog(
            alreadyInstalled = state.companionInstalled,
            onConfirmInstall = { viewModel.markCompanionInstalled() },
            onDismiss = { showInstallDialog = false }
        )
    }
}
