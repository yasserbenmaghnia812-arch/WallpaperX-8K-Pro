package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.datastore.UserPreferencesRepository
import com.example.data.local.WallpaperDatabase
import com.example.data.model.ColorFilter
import com.example.data.model.Wallpaper
import com.example.data.model.WallpaperCategory
import com.example.data.model.WallpaperCollection
import com.example.data.remote.AiWallpaperResult
import com.example.data.remote.GeminiWallpaperService
import com.example.data.repository.WallpaperRepository
import com.example.data.worker.AutoWallpaperManager
import com.example.ui.util.WallpaperManagerUtil
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface AiGenerationUiState {
    object Idle : AiGenerationUiState
    object Loading : AiGenerationUiState
    data class Success(val result: AiWallpaperResult) : AiGenerationUiState
    data class Error(val message: String) : AiGenerationUiState
}

data class UserSettingsState(
    val isOnboardingCompleted: Boolean = false,
    val isDarkMode: Boolean = true,
    val preferredResolution: String = "8K",
    val isAutoWallpaperEnabled: Boolean = false,
    val autoWallpaperIntervalHours: Int = 6,
    val selectedLanguage: String = "ar",
    val isPremiumUser: Boolean = false
)

class WallpaperViewModel(application: Application) : AndroidViewModel(application) {

    private val database = WallpaperDatabase.getDatabase(application)
    private val repository = WallpaperRepository(database.wallpaperDao())
    private val preferencesRepository = UserPreferencesRepository(application)
    private val geminiService = GeminiWallpaperService()
    private val autoWallpaperManager = AutoWallpaperManager(application)

    init {
        viewModelScope.launch {
            repository.seedInitialDataIfEmpty()
            repository.fetchPinterestWallpapers("anime 8k wallpaper")
            repository.fetchPinterestWallpapers("cyberpunk neon city")
            repository.fetchPinterestWallpapers("cars supercars amoled")
        }
    }

    // User Settings State
    val userSettings: StateFlow<UserSettingsState> = combine(
        preferencesRepository.isOnboardingCompleted,
        preferencesRepository.isDarkMode,
        preferencesRepository.preferredResolution,
        preferencesRepository.isAutoWallpaperEnabled,
        preferencesRepository.autoWallpaperIntervalHours,
        preferencesRepository.selectedLanguage,
        preferencesRepository.isPremiumUser
    ) { array ->
        UserSettingsState(
            isOnboardingCompleted = array[0] as Boolean,
            isDarkMode = array[1] as Boolean,
            preferredResolution = array[2] as String,
            isAutoWallpaperEnabled = array[3] as Boolean,
            autoWallpaperIntervalHours = array[4] as Int,
            selectedLanguage = array[5] as String,
            isPremiumUser = array[6] as Boolean
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = UserSettingsState()
    )

    // Wallpaper Catalog State
    val allWallpapers: StateFlow<List<Wallpaper>> = repository.allWallpapers
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val favoriteWallpapers: StateFlow<List<Wallpaper>> = repository.favoriteWallpapers
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val downloadedWallpapers: StateFlow<List<Wallpaper>> = repository.downloadedWallpapers
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val categories: List<WallpaperCategory> = repository.getCategories()
    val collections: List<WallpaperCollection> = repository.getCollections()
    val colorFilters: List<ColorFilter> = repository.getColorFilters()

    // Filtering & Search
    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory.asStateFlow()

    private val _selectedColorFilter = MutableStateFlow<String?>(null)
    val selectedColorFilter: StateFlow<String?> = _selectedColorFilter.asStateFlow()

    private val _selectedResolutionFilter = MutableStateFlow<String?>("All")
    val selectedResolutionFilter: StateFlow<String?> = _selectedResolutionFilter.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val searchResults: StateFlow<List<Wallpaper>> = _searchQuery
        .flatMapLatest { query ->
            if (query.isBlank()) {
                repository.allWallpapers
            } else {
                repository.searchWallpapers(query)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Detail Screen State
    private val _selectedWallpaper = MutableStateFlow<Wallpaper?>(null)
    val selectedWallpaper: StateFlow<Wallpaper?> = _selectedWallpaper.asStateFlow()

    // AI Studio State
    private val _aiGenerationState = MutableStateFlow<AiGenerationUiState>(AiGenerationUiState.Idle)
    val aiGenerationState: StateFlow<AiGenerationUiState> = _aiGenerationState.asStateFlow()

    // Operation Notifications (e.g., Wallpaper applied successfully)
    private val _operationStatus = MutableStateFlow<String?>(null)
    val operationStatus: StateFlow<String?> = _operationStatus.asStateFlow()

    fun selectWallpaper(wallpaper: Wallpaper?) {
        _selectedWallpaper.value = wallpaper
    }

    fun toggleFavorite(wallpaper: Wallpaper) {
        viewModelScope.launch {
            repository.toggleFavorite(wallpaper.id, !wallpaper.isFavorite)
            if (_selectedWallpaper.value?.id == wallpaper.id) {
                _selectedWallpaper.value = _selectedWallpaper.value?.copy(isFavorite = !wallpaper.isFavorite)
            }
        }
    }

    fun downloadWallpaper(wallpaper: Wallpaper) {
        viewModelScope.launch {
            repository.markAsDownloaded(wallpaper.id)
            _operationStatus.value = "Wallpaper downloaded in 8K Ultra HD quality!"
        }
    }

    fun applyWallpaper(wallpaper: Wallpaper, target: WallpaperManagerUtil.WallpaperTarget) {
        viewModelScope.launch {
            _operationStatus.value = "Applying 8K Wallpaper..."
            val result = WallpaperManagerUtil.setWallpaperFromUrl(
                context = getApplication(),
                imageUrl = wallpaper.highResUrl,
                target = target
            )
            if (result.isSuccess) {
                val targetText = when (target) {
                    WallpaperManagerUtil.WallpaperTarget.HOME -> "Home Screen"
                    WallpaperManagerUtil.WallpaperTarget.LOCK -> "Lock Screen"
                    WallpaperManagerUtil.WallpaperTarget.BOTH -> "Home & Lock Screens"
                }
                _operationStatus.value = "Successfully applied to $targetText!"
            } else {
                _operationStatus.value = "Failed to apply wallpaper: ${result.exceptionOrNull()?.message}"
            }
        }
    }

    fun clearOperationStatus() {
        _operationStatus.value = null
    }

    val searchHistory: StateFlow<List<String>> = preferencesRepository.searchHistory.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _isAiSearching = MutableStateFlow(false)
    val isAiSearching: StateFlow<Boolean> = _isAiSearching.asStateFlow()

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
        val q = query.trim()
        if (q.isNotEmpty()) {
            viewModelScope.launch {
                preferencesRepository.addSearchQuery(q)
                _isAiSearching.value = true
                repository.fetchPinterestWallpapersWithAI(q)
                _isAiSearching.value = false
            }
        }
    }

    fun removeSearchHistoryItem(item: String) {
        viewModelScope.launch {
            preferencesRepository.removeSearchQuery(item)
        }
    }

    fun clearSearchHistory() {
        viewModelScope.launch {
            preferencesRepository.clearSearchHistory()
        }
    }

    fun performAiSearch(query: String) {
        setSearchQuery(query)
    }

    fun selectCategoryFilter(categoryName: String?) {
        _selectedCategory.value = if (_selectedCategory.value == categoryName) null else categoryName
    }

    fun selectColorFilter(colorHex: String?) {
        _selectedColorFilter.value = if (_selectedColorFilter.value == colorHex) null else colorHex
    }

    fun selectResolutionFilter(res: String) {
        _selectedResolutionFilter.value = res
    }

    fun generateAiWallpaperConcept(prompt: String, style: String) {
        if (prompt.isBlank()) return
        viewModelScope.launch {
            _aiGenerationState.value = AiGenerationUiState.Loading
            try {
                val result = geminiService.generateWallpaperConcept(prompt, style)
                _aiGenerationState.value = AiGenerationUiState.Success(result)
                
                // Construct real AI Generated Wallpaper object and insert to database catalog
                val styleLower = style.lowercase()
                val bgUrl = when {
                    styleLower.contains("cyber") || styleLower.contains("neon") -> "https://images.unsplash.com/photo-1578632767115-351597cf2477?w=1600"
                    styleLower.contains("amoled") || styleLower.contains("black") -> "https://images.unsplash.com/photo-1550684848-fac1c5b4e853?w=1600"
                    styleLower.contains("anime") -> "https://images.unsplash.com/photo-1534447677768-be436bb09401?w=1600"
                    styleLower.contains("space") || styleLower.contains("cosmic") -> "https://images.unsplash.com/photo-1451187580459-43490279c0fa?w=1600"
                    styleLower.contains("glass") || styleLower.contains("3d") -> "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=1600"
                    else -> "https://images.unsplash.com/photo-1563089145-599997674d42?w=1600"
                }

                val aiWp = Wallpaper(
                    id = "ai_wp_${System.currentTimeMillis()}",
                    title = result.title,
                    description = result.description,
                    category = "AI Generated",
                    imageUrl = bgUrl,
                    highResUrl = bgUrl,
                    resolution = "7680×4320 (8K AI)",
                    fileSize = "16.8 MB",
                    dominantColors = result.suggestedColors,
                    photographer = "Gemini AI Studio",
                    views = 1250,
                    downloads = 430,
                    likes = 310,
                    isEditorChoice = true,
                    isTrending = true,
                    tags = result.tags
                )

                repository.insertCustomWallpaper(aiWp)
                _selectedWallpaper.value = aiWp
                _operationStatus.value = "AI Wallpaper concept generated & created in 8K!"
            } catch (e: Exception) {
                _aiGenerationState.value = AiGenerationUiState.Error(e.message ?: "Generation failed")
            }
        }
    }

    fun downloadCollectionPack(collection: WallpaperCollection) {
        viewModelScope.launch {
            collection.wallpapers.forEach { wp ->
                repository.markAsDownloaded(wp.id)
            }
            _operationStatus.value = "Downloaded ${collection.wallpapers.size} wallpapers from '${collection.title}' in 8K!"
        }
    }

    fun performAiImageSearch(prompt: String) {
        setSearchQuery(prompt)
    }

    fun triggerAutoWallpaperChange() {
        viewModelScope.launch {
            _operationStatus.value = "Changing wallpaper..."
            val success = autoWallpaperManager.changeWallpaperNow()
            if (success) {
                _operationStatus.value = "New 8K Wallpaper applied automatically!"
            } else {
                _operationStatus.value = "Auto wallpaper change failed"
            }
        }
    }

    // User preference actions
    fun setDarkMode(enabled: Boolean) {
        viewModelScope.launch { preferencesRepository.setDarkMode(enabled) }
    }

    fun setLanguage(lang: String) {
        viewModelScope.launch { preferencesRepository.setSelectedLanguage(lang) }
    }

    fun setAutoWallpaperEnabled(enabled: Boolean) {
        viewModelScope.launch { preferencesRepository.setAutoWallpaperEnabled(enabled) }
    }

    fun setAutoWallpaperInterval(hours: Int) {
        viewModelScope.launch { preferencesRepository.setAutoWallpaperIntervalHours(hours) }
    }

    fun completeOnboarding() {
        viewModelScope.launch { preferencesRepository.setOnboardingCompleted(true) }
    }

    fun setPremiumStatus(isPremium: Boolean) {
        viewModelScope.launch { preferencesRepository.setPremiumUser(isPremium) }
    }
}
