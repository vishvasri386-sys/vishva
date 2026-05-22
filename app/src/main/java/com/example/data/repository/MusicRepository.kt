package com.example.data.repository

import com.example.data.database.FavoriteEntity
import com.example.data.database.MusicDao
import com.example.data.database.RecentlyPlayedEntity
import com.example.data.model.Song
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class MusicRepository(private val musicDao: MusicDao) {

    // Curated high-fidelity futuristic synthwave and lo-fi tracks matching the LOVABLE theme
    private val staticSongs = listOf(
        Song(
            id = "lv-01",
            title = "Cyber Heartbeat",
            artist = "L V R S",
            album = "Neon Pulse Vol. 1",
            durationText = "6:12",
            durationMs = 372000,
            audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3",
            coverUrl = "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?q=85&w=400",
            isTrending = true,
            playlistCategory = "Liquid Neon"
        ),
        Song(
            id = "lv-02",
            title = "Neon Love Engine",
            artist = "VaporGlow",
            album = "Atmospheric Glitch",
            durationText = "7:05",
            durationMs = 425000,
            audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3",
            coverUrl = "https://images.unsplash.com/photo-1578894381163-e72c17f2d45f?q=85&w=400",
            isTrending = true,
            playlistCategory = "Liquid Neon"
        ),
        Song(
            id = "lv-03",
            title = "Violet Horizon",
            artist = "Arcade Dreamer",
            album = "Outrun 2099",
            durationText = "5:44",
            durationMs = 344000,
            audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3",
            coverUrl = "https://images.unsplash.com/photo-1563089145-599997674d42?q=85&w=400",
            isTrending = false,
            playlistCategory = "Vapor Nostalgia"
        ),
        Song(
            id = "lv-04",
            title = "Starlight Overdrive",
            artist = "Quantum Echoes",
            album = "Deep Galactic Space",
            durationText = "5:02",
            durationMs = 302000,
            audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-4.mp3",
            coverUrl = "https://images.unsplash.com/photo-1462331940025-496dfbfc7564?q=85&w=400",
            isTrending = true,
            playlistCategory = "Cyber Resonance"
        ),
        Song(
            id = "lv-05",
            title = "Retro Eclipse",
            artist = "Tokyo 198X",
            album = "Analog Sunset",
            durationText = "6:03",
            durationMs = 363000,
            audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-5.mp3",
            coverUrl = "https://images.unsplash.com/photo-1509198397868-475647b2a1e5?q=85&w=400",
            isTrending = false,
            playlistCategory = "Vapor Nostalgia"
        ),
        Song(
            id = "lv-06",
            title = "Lovable Waves",
            artist = "L V R S",
            album = "Neon Pulse Vol. 1",
            durationText = "5:38",
            durationMs = 338000,
            audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-6.mp3",
            coverUrl = "https://images.unsplash.com/photo-1550684848-fac1c5b4e853?q=85&w=400",
            isTrending = true,
            playlistCategory = "Liquid Neon"
        ),
        Song(
            id = "lv-07",
            title = "Future Nostalgia",
            artist = "VaporGlow",
            album = "Atmospheric Glitch",
            durationText = "6:44",
            durationMs = 404000,
            audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-7.mp3",
            coverUrl = "https://images.unsplash.com/photo-1542838132-92c53300491e?q=85&w=400",
            isTrending = false,
            playlistCategory = "Cyber Resonance"
        ),
        Song(
            id = "lv-08",
            title = "Midnight Odyssey",
            artist = "Arcade Dreamer",
            album = "Outrun 2099",
            durationText = "4:55",
            durationMs = 295000,
            audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-8.mp3",
            coverUrl = "https://images.unsplash.com/photo-1508739773434-c26b3d09e071?q=85&w=400",
            isTrending = false,
            playlistCategory = "Vapor Nostalgia"
        )
    )

    fun getSongs(): List<Song> = staticSongs

    fun getSongById(id: String): Song? = staticSongs.find { it.id == id }

    fun getFavorites(): Flow<List<Song>> {
        return musicDao.getFavorites().map { favoriteEntities ->
            val favIds = favoriteEntities.map { it.songId }.toSet()
            staticSongs.filter { it.id in favIds }
        }
    }

    fun getRecentlyPlayed(): Flow<List<Song>> {
        return musicDao.getRecentlyPlayed().map { recentEntities ->
            // Order recently played songs according to the saved recents (up to 20 unique songs)
            val recentIds = recentEntities.sortedByDescending { it.timestamp }.map { it.songId }
            recentIds.mapNotNull { id -> staticSongs.find { it.id == id } }.distinct()
        }
    }

    suspend fun toggleFavorite(songId: String) {
        val currentFavorites = musicDao.getFavorites().map { list -> list.map { it.songId } }
        // Note: For simplicity and thread safety inside background repository,
        // we write direct database insertions based on ViewModel evaluations or direct lookups
    }

    suspend fun addFavorite(songId: String) {
        musicDao.insertFavorite(FavoriteEntity(songId = songId))
    }

    suspend fun removeFavorite(songId: String) {
        musicDao.deleteFavoriteById(songId)
    }

    suspend fun addToRecentlyPlayed(songId: String) {
        musicDao.insertRecentlyPlayed(RecentlyPlayedEntity(songId = songId, timestamp = System.currentTimeMillis()))
    }
}
