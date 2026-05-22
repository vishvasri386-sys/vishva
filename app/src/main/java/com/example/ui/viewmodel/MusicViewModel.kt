package com.example.ui.viewmodel

import android.app.Application
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.example.data.database.AppDatabase
import com.example.data.model.Song
import com.example.data.repository.MusicRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.random.Random

class MusicViewModel(application: Application) : AndroidViewModel(application) {

    private val database: AppDatabase = Room.databaseBuilder(
        application,
        AppDatabase::class.java, "lovable-music-db"
    )
    .fallbackToDestructiveMigration()
    .build()

    private val repository = MusicRepository(database.musicDao())

    // All songs defined in repo
    val allSongs: List<Song> = repository.getSongs()

    // Reactive states
    val favoritesState: StateFlow<List<Song>> = repository.getFavorites()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentlyPlayedState: StateFlow<List<Song>> = repository.getRecentlyPlayed()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Tracks navigation screens/tabs
    private val _currentTab = MutableStateFlow("Home")
    val currentTab: StateFlow<String> = _currentTab.asStateFlow()

    // Search query state
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Filtered songs for search tab
    val filteredSongs: StateFlow<List<Song>> = combine(_searchQuery, flowOf(allSongs)) { query, songs ->
        if (query.isBlank()) songs
        else songs.filter {
            it.title.contains(query, ignoreCase = true) ||
            it.artist.contains(query, ignoreCase = true) ||
            it.album.contains(query, ignoreCase = true)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), allSongs)

    // Player engine state
    private var mediaPlayer: MediaPlayer? = null
    
    private val _currentSong = MutableStateFlow<Song?>(null)
    val currentSong: StateFlow<Song?> = _currentSong.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentTimeMs = MutableStateFlow(0L)
    val currentTimeMs: StateFlow<Long> = _currentTimeMs.asStateFlow()

    private val _volume = MutableStateFlow(0.8f) // default 80% volume
    val volume: StateFlow<Float> = _volume.asStateFlow()

    // Multi-color Theme mode toggle (True: Deep Dark Purple, False: Premium Soft Violet Neon)
    private val _isDarkPurple = MutableStateFlow(true)
    val isDarkPurple: StateFlow<Boolean> = _isDarkPurple.asStateFlow()

    // Notification banners
    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    // Animated visualizer frequencies amplitudes (8 bars for music display)
    private val _visualizerAmplitudes = MutableStateFlow(List(12) { 0.1f })
    val visualizerAmplitudes: StateFlow<List<Float>> = _visualizerAmplitudes.asStateFlow()

    private var progressJob: Job? = null
    private var visualizerJob: Job? = null

    init {
        initMediaPlayer()
    }

    private fun initMediaPlayer() {
        mediaPlayer = MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build()
            )
            setOnPreparedListener {
                start()
                _isPlaying.value = true
                startProgressTracker()
                startVisualizerTracker()
                _currentSong.value?.let { song ->
                    viewModelScope.launch {
                        repository.addToRecentlyPlayed(song.id)
                    }
                }
            }
            setOnCompletionListener {
                playNext()
            }
            setOnErrorListener { _, what, extra ->
                Log.e("LOVABLE_PLAYER", "MediaPlayer Error: what=$what, extra=$extra")
                showNotification("Could not stream music track. Connecting...")
                // Auto skip on stream issues
                playNext()
                true
            }
        }
    }

    fun selectTab(tab: String) {
        _currentTab.value = tab
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleTheme() {
        _isDarkPurple.value = !_isDarkPurple.value
        showNotification(if (_isDarkPurple.value) "Dark Purple Neo theme set!" else "Hyper Light Violet theme set!")
    }

    fun playSong(song: Song) {
        try {
            stopPlaybackTrack()
            _currentSong.value = song
            _currentTimeMs.value = 0L

            mediaPlayer?.apply {
                reset()
                setDataSource(song.audioUrl)
                setVolume(_volume.value, _volume.value)
                prepareAsync() // Avoid UI thread blocking for URL preparings
            }
            showNotification("Streaming: ${song.title}")
        } catch (e: Exception) {
            Log.e("LOVABLE_PLAYER", "Error playing song", e)
            showNotification("Streaming error: Check connection.")
        }
    }

    fun togglePlayPause() {
        val player = mediaPlayer ?: return
        val current = _currentSong.value

        if (current == null) {
            // Pick first song available
            if (allSongs.isNotEmpty()) {
                playSong(allSongs.first())
            }
            return
        }

        if (player.isPlaying) {
            player.pause()
            _isPlaying.value = false
            stopProgressTracker()
            stopVisualizerTracker()
            showNotification("Paused: ${current.title}")
        } else {
            player.start()
            _isPlaying.value = true
            startProgressTracker()
            startVisualizerTracker()
            showNotification("Playing: ${current.title}")
        }
    }

    fun playNext() {
        val songsList = getActivePlaylistContext()
        if (songsList.isEmpty()) return

        val current = _currentSong.value
        val currentIndex = songsList.indexOfFirst { it.id == current?.id }
        val nextIndex = if (currentIndex == -1 || currentIndex == songsList.lastIndex) 0 else currentIndex + 1

        playSong(songsList[nextIndex])
    }

    fun playPrevious() {
        val songsList = getActivePlaylistContext()
        if (songsList.isEmpty()) return

        val current = _currentSong.value
        val currentIndex = songsList.indexOfFirst { it.id == current?.id }
        val prevIndex = if (currentIndex <= 0) songsList.lastIndex else currentIndex - 1

        playSong(songsList[prevIndex])
    }

    fun seekToFraction(fraction: Float) {
        val player = mediaPlayer ?: return
        val current = _currentSong.value ?: return
        val targetMs = (fraction * current.durationMs).toInt()
        player.seekTo(targetMs)
        _currentTimeMs.value = targetMs.toLong()
    }

    fun updateVolume(newVolume: Float) {
        val volClamp = newVolume.coerceIn(0f, 1f)
        _volume.value = volClamp
        mediaPlayer?.setVolume(volClamp, volClamp)
    }

    fun toggleFavorite(song: Song) {
        viewModelScope.launch {
            val isFav = favoritesState.value.any { it.id == song.id }
            if (isFav) {
                repository.removeFavorite(song.id)
                showNotification("Removed of favorites: ${song.title}")
            } else {
                repository.addFavorite(song.id)
                showNotification("Added of favorites: ${song.title} ❤️")
            }
        }
    }

    private fun getActivePlaylistContext(): List<Song> {
        return when (_currentTab.value) {
            "Favorites" -> favoritesState.value
            "Trending" -> allSongs.filter { it.isTrending }
            "Search" -> filteredSongs.value
            else -> allSongs
        }
    }

    private fun startProgressTracker() {
        progressJob?.cancel()
        progressJob = viewModelScope.launch {
            while (_isPlaying.value) {
                mediaPlayer?.let { player ->
                    if (player.isPlaying) {
                        _currentTimeMs.value = player.currentPosition.toLong()
                    }
                }
                delay(250)
            }
        }
    }

    private fun stopProgressTracker() {
        progressJob?.cancel()
    }

    private fun startVisualizerTracker() {
        visualizerJob?.cancel()
        visualizerJob = viewModelScope.launch {
            while (_isPlaying.value) {
                // Generate dynamic realistic retro waves
                _visualizerAmplitudes.value = List(12) {
                    Random.nextFloat().coerceIn(0.1f, 1.0f)
                }
                delay(120)
            }
            // Transition back smoothly to idle bars
            _visualizerAmplitudes.value = List(12) { 0.1f }
        }
    }

    private fun stopVisualizerTracker() {
        visualizerJob?.cancel()
        _visualizerAmplitudes.value = List(12) { 0.15f }
    }

    private fun stopPlaybackTrack() {
        stopProgressTracker()
        stopVisualizerTracker()
        _isPlaying.value = false
    }

    private fun showNotification(message: String) {
        _toastMessage.value = message
        viewModelScope.launch {
            delay(2500)
            if (_toastMessage.value == message) {
                _toastMessage.value = null
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopPlaybackTrack()
        mediaPlayer?.let { player ->
            try {
                if (player.isPlaying) {
                    player.stop()
                }
                player.release()
            } catch (e: Exception) {
                Log.e("LOVABLE_PLAYER", "Error releasing MediaPlayer", e)
            }
        }
        mediaPlayer = null
    }
}
