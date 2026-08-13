package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.ScreenTab
import com.example.ui.components.WallpaperXBottomBar
import com.example.ui.screens.AiGeneratorScreen
import com.example.ui.screens.CollectionsScreen
import com.example.ui.screens.DetailScreen
import com.example.ui.screens.ExploreScreen
import com.example.ui.screens.FavoritesScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.SearchScreen
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.WallpaperXTheme
import com.example.viewmodel.WallpaperViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: WallpaperViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val userSettings by viewModel.userSettings.collectAsStateWithLifecycle()
            val allWallpapers by viewModel.allWallpapers.collectAsStateWithLifecycle()
            val favoriteWallpapers by viewModel.favoriteWallpapers.collectAsStateWithLifecycle()
            val downloadedWallpapers by viewModel.downloadedWallpapers.collectAsStateWithLifecycle()
            val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
            val selectedColorHex by viewModel.selectedColorFilter.collectAsStateWithLifecycle()
            val selectedResolution by viewModel.selectedResolutionFilter.collectAsStateWithLifecycle()
            val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
            val searchResults by viewModel.searchResults.collectAsStateWithLifecycle()
            val isAiSearching by viewModel.isAiSearching.collectAsStateWithLifecycle()
            val searchHistory by viewModel.searchHistory.collectAsStateWithLifecycle()
            val selectedWallpaper by viewModel.selectedWallpaper.collectAsStateWithLifecycle()
            val aiState by viewModel.aiGenerationState.collectAsStateWithLifecycle()
            val operationStatus by viewModel.operationStatus.collectAsStateWithLifecycle()

            WallpaperXTheme(darkTheme = userSettings.isDarkMode) {
                val snackbarHostState = remember { SnackbarHostState() }
                val isArabic = userSettings.selectedLanguage == "ar"

                // Show operation status toast/snackbar
                LaunchedEffect(operationStatus) {
                    operationStatus?.let { msg ->
                        snackbarHostState.showSnackbar(msg)
                        viewModel.clearOperationStatus()
                    }
                }

                if (!userSettings.isOnboardingCompleted) {
                    OnboardingScreen(
                        onFinishOnboarding = { viewModel.completeOnboarding() },
                        isArabic = isArabic
                    )
                } else if (selectedWallpaper != null) {
                    DetailScreen(
                        wallpaper = selectedWallpaper!!,
                        onBackClick = { viewModel.selectWallpaper(null) },
                        onFavoriteClick = { viewModel.toggleFavorite(it) },
                        onDownloadClick = { viewModel.downloadWallpaper(it) },
                        onApplyClick = { wp, target -> viewModel.applyWallpaper(wp, target) },
                        onColorSelect = {
                            viewModel.selectColorFilter(it)
                            viewModel.selectWallpaper(null)
                        },
                        isArabic = isArabic
                    )
                } else {
                    var currentTab by remember { mutableStateOf(ScreenTab.HOME) }
                    var isSearchActive by remember { mutableStateOf(false) }

                    Scaffold(
                        snackbarHost = { SnackbarHost(snackbarHostState) },
                        bottomBar = {
                            if (!isSearchActive) {
                                WallpaperXBottomBar(
                                    currentTab = currentTab,
                                    onTabSelected = { currentTab = it },
                                    isArabic = isArabic
                                )
                            }
                        },
                        containerColor = DarkBackground
                    ) { innerPadding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                            if (isSearchActive) {
                                SearchScreen(
                                    searchQuery = searchQuery,
                                    searchResults = searchResults,
                                    onQueryChange = { viewModel.setSearchQuery(it) },
                                    onBackClick = { isSearchActive = false },
                                    onWallpaperClick = { viewModel.selectWallpaper(it) },
                                    onFavoriteClick = { viewModel.toggleFavorite(it) },
                                    searchHistory = searchHistory,
                                    onRemoveHistoryItem = { viewModel.removeSearchHistoryItem(it) },
                                    onClearHistory = { viewModel.clearSearchHistory() },
                                    isAiSearching = isAiSearching,
                                    isArabic = isArabic
                                )
                            } else {
                                when (currentTab) {
                                    ScreenTab.HOME -> HomeScreen(
                                        wallpapers = allWallpapers,
                                        categories = viewModel.categories,
                                        colorFilters = viewModel.colorFilters,
                                        selectedCategory = selectedCategory,
                                        selectedColorHex = selectedColorHex,
                                        onWallpaperClick = { viewModel.selectWallpaper(it) },
                                        onFavoriteClick = { viewModel.toggleFavorite(it) },
                                        onCategorySelect = { viewModel.selectCategoryFilter(it) },
                                        onColorSelect = { viewModel.selectColorFilter(it) },
                                        onSearchClick = { isSearchActive = true },
                                        onAutoWallpaperTrigger = { viewModel.triggerAutoWallpaperChange() },
                                        isArabic = isArabic
                                    )

                                    ScreenTab.EXPLORE -> ExploreScreen(
                                        categories = viewModel.categories,
                                        selectedCategory = selectedCategory,
                                        selectedResolution = selectedResolution,
                                        onCategoryClick = {
                                            viewModel.selectCategoryFilter(it)
                                            currentTab = ScreenTab.HOME
                                        },
                                        onResolutionSelect = { viewModel.selectResolutionFilter(it) },
                                        onSearchClick = { isSearchActive = true },
                                        isArabic = isArabic
                                    )

                                    ScreenTab.COLLECTIONS -> CollectionsScreen(
                                        collections = viewModel.collections,
                                        onWallpaperClick = { viewModel.selectWallpaper(it) },
                                        onFavoriteClick = { viewModel.toggleFavorite(it) },
                                        onDownloadPackClick = { viewModel.downloadCollectionPack(it) },
                                        isArabic = isArabic
                                    )

                                    ScreenTab.FAVORITES -> FavoritesScreen(
                                        favoriteWallpapers = favoriteWallpapers,
                                        downloadedWallpapers = downloadedWallpapers,
                                        onWallpaperClick = { viewModel.selectWallpaper(it) },
                                        onFavoriteClick = { viewModel.toggleFavorite(it) },
                                        isArabic = isArabic
                                    )

                                    ScreenTab.PROFILE -> ProfileScreen(
                                        userSettings = userSettings,
                                        onToggleDarkMode = { viewModel.setDarkMode(it) },
                                        onSetLanguage = { viewModel.setLanguage(it) },
                                        onToggleAutoWallpaper = { viewModel.setAutoWallpaperEnabled(it) },
                                        onAutoWallpaperIntervalSelect = { viewModel.setAutoWallpaperInterval(it) },
                                        onTriggerAutoWallpaperNow = { viewModel.triggerAutoWallpaperChange() },
                                        onOpenAiStudio = {
                                            // Open AI Studio view directly
                                            viewModel.generateAiWallpaperConcept("Cyberpunk 8K Neon Prism", "Cyberpunk")
                                        },
                                        onUnlockPremium = { viewModel.setPremiumStatus(true) },
                                        isArabic = isArabic
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
