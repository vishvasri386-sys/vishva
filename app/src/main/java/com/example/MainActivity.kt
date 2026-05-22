package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.data.model.Song
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.MusicViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val viewModel: MusicViewModel = viewModel()
            val isDarkPurple by viewModel.isDarkPurple.collectAsStateWithLifecycle()

            MyApplicationTheme(isDarkPurple = isDarkPurple) {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    contentWindowInsets = WindowInsets.safeDrawing
                ) { innerPadding ->
                    LovableMusicApp(
                        viewModel = viewModel,
                        isDarkPurple = isDarkPurple,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun LovableMusicApp(
    viewModel: MusicViewModel,
    isDarkPurple: Boolean,
    modifier: Modifier = Modifier
) {
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val currentSong by viewModel.currentSong.collectAsStateWithLifecycle()
    val isPlaying by viewModel.isPlaying.collectAsStateWithLifecycle()
    val favorites by viewModel.favoritesState.collectAsStateWithLifecycle()
    val recents by viewModel.recentlyPlayedState.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val filteredSongs by viewModel.filteredSongs.collectAsStateWithLifecycle()
    val amplitudes by viewModel.visualizerAmplitudes.collectAsStateWithLifecycle()
    val mediaVolume by viewModel.volume.collectAsStateWithLifecycle()
    val toastMsg by viewModel.toastMessage.collectAsStateWithLifecycle()

    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    Box(
        modifier = modifier
            .fillMaxSize()
            .focusRequester(focusRequester)
            .focusable()
            .onKeyEvent { keyEvent ->
                if (keyEvent.type == KeyEventType.KeyDown) {
                    when (keyEvent.key) {
                        Key.Spacebar -> {
                            viewModel.togglePlayPause()
                            true
                        }
                        Key.DirectionRight -> {
                            viewModel.playNext()
                            true
                        }
                        Key.DirectionLeft -> {
                            viewModel.playPrevious()
                            true
                        }
                        Key.VolumeUp -> {
                            viewModel.updateVolume(mediaVolume + 0.1f)
                            true
                        }
                        Key.VolumeDown -> {
                            viewModel.updateVolume(mediaVolume - 0.1f)
                            true
                        }
                        else -> false
                    }
                } else false
            }
    ) {
        LovableBackgroundWrapper(isDarkPurple = isDarkPurple) {
            
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val isWideScreen = maxWidth >= 680.dp

                Row(modifier = Modifier.fillMaxSize()) {
                    if (isWideScreen) {
                        FloatingSidebarNavigation(
                            activeTab = currentTab,
                            onTabSelected = { viewModel.selectTab(it) },
                            isDarkPurple = isDarkPurple,
                            onThemeToggle = { viewModel.toggleTheme() },
                            onLogoClick = { viewModel.selectTab("Home") },
                            modifier = Modifier
                                .width(230.dp)
                                .fillMaxHeight()
                                .padding(16.dp)
                        )
                    }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    ) {
                        StickyTopHeader(
                            activeTab = currentTab,
                            onTabSelected = { viewModel.selectTab(it) },
                            showNavLinks = !isWideScreen,
                            isDarkPurple = isDarkPurple,
                            onThemeToggle = { viewModel.toggleTheme() },
                            onLogoClick = { viewModel.selectTab("Home") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                        )

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                        ) {
                            AnimatedContent(
                                targetState = currentTab,
                                transitionSpec = {
                                    slideInHorizontally { width -> width / 3 } + fadeIn() with
                                            slideOutHorizontally { width -> -width / 3 } + fadeOut()
                                }, label = "screen_transition"
                            ) { activeTab ->
                                when (activeTab) {
                                    "Home" -> HomeScreen(
                                        viewModel = viewModel,
                                        isDarkPurple = isDarkPurple,
                                        favorites = favorites,
                                        recents = recents,
                                        onPlaySong = { viewModel.playSong(it) },
                                        onToggleFav = { viewModel.toggleFavorite(it) }
                                    )
                                    "Trending" -> TrendingScreen(
                                        viewModel = viewModel,
                                        favorites = favorites,
                                        onPlaySong = { viewModel.playSong(it) },
                                        onToggleFav = { viewModel.toggleFavorite(it) }
                                    )
                                    "Playlists" -> PlaylistsScreen(
                                        viewModel = viewModel,
                                        favorites = favorites,
                                        onPlaySong = { viewModel.playSong(it) },
                                        onToggleFav = { viewModel.toggleFavorite(it) }
                                    )
                                    "Favorites" -> FavoritesScreen(
                                        favorites = favorites,
                                        currentSong = currentSong,
                                        onPlaySong = { viewModel.playSong(it) },
                                        onToggleFav = { viewModel.toggleFavorite(it) }
                                    )
                                    "Search" -> SearchScreen(
                                        searchQuery = searchQuery,
                                        filteredSongs = filteredSongs,
                                        favorites = favorites,
                                        currentSong = currentSong,
                                        onSearchChanged = { viewModel.updateSearchQuery(it) },
                                        onPlaySong = { viewModel.playSong(it) },
                                        onToggleFav = { viewModel.toggleFavorite(it) }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(115.dp))
                    }
                }

                GlobalPlayerDock(
                    viewModel = viewModel,
                    currentSong = currentSong,
                    isPlaying = isPlaying,
                    favorites = favorites,
                    amplitudes = amplitudes,
                    mediaVolume = mediaVolume,
                    isDarkPurple = isDarkPurple,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                        .navigationBarsPadding()
                )
            }

            LovableToastNotification(
                message = toastMsg,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}

// ==========================================
// RESPONSIVE SIDEBAR (FOR EXPANDED DEVICES)
// ==========================================

@Composable
fun FloatingSidebarNavigation(
    activeTab: String,
    onTabSelected: (String) -> Unit,
    isDarkPurple: Boolean,
    onThemeToggle: () -> Unit,
    onLogoClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    GlassCard(
        modifier = modifier,
        borderGlowColor = LovableNeonViolet
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LovableLogo(onClick = onLogoClick)

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "LOVABLE",
                fontSize = 20.sp,
                letterSpacing = 2.sp,
                style = androidx.compose.ui.text.TextStyle(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.White,
                            Color.White.copy(alpha = 0.6f)
                        )
                    ),
                    fontWeight = FontWeight.Black,
                    shadow = Shadow(
                        color = LovableNeonViolet,
                        blurRadius = 8f
                    )
                )
            )

            Spacer(modifier = Modifier.height(30.dp))

            val navItems = listOf(
                "Home" to Icons.Default.Home,
                "Trending" to Icons.Default.TrendingUp,
                "Playlists" to Icons.Default.QueueMusic,
                "Favorites" to Icons.Default.Favorite,
                "Search" to Icons.Default.Search
            )

            navItems.forEach { (tabName, icon) ->
                val isActive = tabName == activeTab
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isActive) LovableNeonVioletGlow else Color.Transparent)
                        .border(
                            width = 1.dp,
                            color = if (isActive) LovableNeonViolet.copy(alpha = 0.5f) else Color.Transparent,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable { onTabSelected(tabName) }
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = "$tabName tab link",
                        tint = if (isActive) LovableNeonCyan else LovableTextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Text(
                        text = tabName,
                        color = if (isActive) LovableTextPrimary else LovableTextSecondary,
                        fontSize = 14.sp,
                        fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.White.copy(alpha = 0.05f))
                    .clickable { onThemeToggle() }
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = if (isDarkPurple) Icons.Default.NightsStay else Icons.Default.LightMode,
                    contentDescription = "Switch Theme style",
                    tint = LovableNeonCyan,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isDarkPurple) "Abyss Dark" else "Nebula Soft",
                    color = LovableTextPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// ==========================================
// STICKY TOP NAVIGATION BAR
// ==========================================

@Composable
fun StickyTopHeader(
    activeTab: String,
    onTabSelected: (String) -> Unit,
    showNavLinks: Boolean,
    isDarkPurple: Boolean,
    onThemeToggle: () -> Unit,
    onLogoClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (!showNavLinks) {
            Text(
                text = "Premium Live Audio Streams",
                color = LovableTextSecondary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { onLogoClick() }
            ) {
                LovableLogo(onClick = onLogoClick, modifier = Modifier.size(38.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "LOVABLE",
                    fontSize = 18.sp,
                    letterSpacing = 1.sp,
                    style = androidx.compose.ui.text.TextStyle(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color.White,
                                Color.White.copy(alpha = 0.6f)
                            )
                        ),
                        fontWeight = FontWeight.Black
                    )
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        if (showNavLinks) {
            val shortNavItems = listOf("Home", "Trending", "Favs" to "Favorites", "Search")
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .background(Color.White.copy(alpha = 0.05f), CircleShape)
                    .padding(4.dp)
            ) {
                shortNavItems.forEach { item ->
                    val displayName = if (item is Pair<*,*>) item.first as String else item as String
                    val actualTabName = if (item is Pair<*,*>) item.second as String else item as String
                    val isActive = actualTabName == activeTab

                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(if (isActive) LovableNeonViolet else Color.Transparent)
                            .clickable { onTabSelected(actualTabName) }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = displayName,
                            color = if (isActive) LovableTextPrimary else LovableTextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.width(10.dp))

        IconButton(
            onClick = onThemeToggle,
            modifier = Modifier
                .background(Color.White.copy(alpha = 0.06f), CircleShape)
                .size(38.dp)
        ) {
            Icon(
                imageVector = if (isDarkPurple) Icons.Default.NightsStay else Icons.Default.LightMode,
                contentDescription = "Theme style shift button",
                tint = LovableNeonCyan,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}


// ==========================================
// 1. HOME SCREEN SECTION
// ==========================================

@Composable
fun HomeScreen(
    viewModel: MusicViewModel,
    isDarkPurple: Boolean,
    favorites: List<Song>,
    recents: List<Song>,
    onPlaySong: (Song) -> Unit,
    onToggleFav: (Song) -> Unit
) {
    val allSongs = viewModel.allSongs
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        WelcomeHeroBanner(isDarkPurple = isDarkPurple)

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Discover Our Hot Tracks",
            color = LovableTextPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.ExtraBold,
            style = MaterialTheme.typography.titleMedium.copy(
                shadow = Shadow(color = LovableNeonViolet, blurRadius = 4f)
            ),
            modifier = Modifier.padding(bottom = 12.dp)
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 12.dp)
        ) {
            items(allSongs.take(4)) { song ->
                SongGridCard(
                    song = song,
                    isPlayingOnDevice = viewModel.currentSong.collectAsStateWithLifecycle().value?.id == song.id,
                    isFavorited = favorites.any { it.id == song.id },
                    onPlayClick = { onPlaySong(song) },
                    onFavClick = { onToggleFav(song) },
                    modifier = Modifier.width(170.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Recently Listened",
            color = LovableTextPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        if (recents.isEmpty()) {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                borderGlowColor = LovableGlassBorder
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        tint = LovableTextSecondary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "History empty. Play a stream to sync stats!",
                        color = LovableTextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                recents.forEach { song ->
                    SongRowItem(
                        song = song,
                        isPlayingOnDevice = viewModel.currentSong.collectAsStateWithLifecycle().value?.id == song.id,
                        isFavorited = favorites.any { it.id == song.id },
                        onPlayClick = { onPlaySong(song) },
                        onFavClick = { onToggleFav(song) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Top Global Producers",
            color = LovableTextPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        val artists = listOf(
            "L V R S" to "https://images.unsplash.com/photo-1534528741775-53994a69daeb?q=85&w=200",
            "VaporGlow" to "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?q=85&w=200",
            "Arcade Dreamer" to "https://images.unsplash.com/photo-1492562080023-ab3db95bfbce?q=85&w=200",
            "Quantum Echoes" to "https://images.unsplash.com/photo-1487412720507-e7ab37603c6f?q=85&w=200"
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(artists) { (name, imageUrl) ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.width(85.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .border(2.dp, LovableNeonViolet, CircleShape)
                            .padding(3.dp)
                    ) {
                        AsyncImage(
                            model = imageUrl,
                            contentDescription = name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = name,
                        color = LovableTextPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
fun WelcomeHeroBanner(isDarkPurple: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "banner")

    val musicNoteDriftY by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = -25f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ), label = "noteDrift"
    )

    val gradientBack = remember(isDarkPurple) {
        Brush.linearGradient(
            colors = listOf(
                Color(0x33581C87), // purple-900/20 backdrop
                Color(0x33312E81), // indigo-900/20 backdrop
                Color(0x0F0F0118)  // deep base backdrop
            )
        )
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        shape = RoundedCornerShape(40.dp), // rounded-[2.5rem]
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.1f), // white/10
                shape = RoundedCornerShape(40.dp)
            )
    ) {
        Box(
            modifier = Modifier
                .background(gradientBack)
                .drawBehind {
                    // Absolute decorative -right-4 -bottom-4 w-32 h-32 border-[12px] border-white/10 rounded-full
                    with(this) {
                        val diameterPx = 128.dp.toPx()
                        val strokePx = 12.dp.toPx()
                        val centerOffset = Offset(
                            this.size.width + 16.dp.toPx() - (diameterPx / 2),
                            this.size.height + 16.dp.toPx() - (diameterPx / 2)
                        )
                        drawCircle(
                            color = Color.White.copy(alpha = 0.1f),
                            radius = (diameterPx / 2),
                            center = centerOffset,
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokePx)
                        )
                    }
                }
                .padding(24.dp)
                .fillMaxWidth()
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "FEATURED EXPERIENCE",
                        color = Color(0xFFA78BFA), // text-purple-400
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )

                    Icon(
                        imageVector = Icons.Filled.MusicNote,
                        contentDescription = "Floating notation",
                        tint = LovableNeonPink,
                        modifier = Modifier
                            .size(24.dp)
                            .graphicsLayer(translationY = musicNoteDriftY)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = buildAnnotatedString {
                        withStyle(
                            style = SpanStyle(
                                fontWeight = FontWeight.Light,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                fontSize = 30.sp,
                                color = Color.White
                            )
                        ) {
                            append("Feel the Music with \n")
                        }
                        withStyle(
                            style = SpanStyle(
                                fontWeight = FontWeight.Black,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Normal,
                                fontSize = 30.sp,
                                color = Color.White
                            )
                        ) {
                            append("LOVABLE")
                        }
                    },
                    lineHeight = 36.sp,
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Discover futuristic listening experiences curated for you.",
                    color = Color.White.copy(alpha = 0.5f), // text-white/50
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 16.sp,
                    modifier = Modifier.widthIn(max = 220.dp)
                )

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.height(32.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    val relativeHeights = listOf(0.4f, 0.7f, 1.0f, 0.5f, 0.8f)
                    val colors = listOf(
                        Color(0xFFA855F7), // purple-500
                        Color.White,
                        Color(0xFFC084FC), // purple-400
                        Color.White.copy(alpha = 0.3f), // white/30
                        Color(0xFF6366F1)  // indigo-500
                    )
                    relativeHeights.forEachIndexed { idx, heightFactor ->
                        Box(
                            modifier = Modifier
                                .width(4.dp)
                                .fillMaxHeight(heightFactor)
                                .background(colors[idx], CircleShape)
                        )
                    }
                }
            }
        }
    }
}


// ==========================================
// 2. TRENDING SCREEN SECTION
// ==========================================

@Composable
fun TrendingScreen(
    viewModel: MusicViewModel,
    favorites: List<Song>,
    onPlaySong: (Song) -> Unit,
    onToggleFav: (Song) -> Unit
) {
    val trendingSongs = viewModel.allSongs.filter { it.isTrending }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Text(
            text = "Trending Now 🔥",
            color = LovableTextPrimary,
            fontSize = 24.sp,
            fontWeight = FontWeight.Black,
            style = MaterialTheme.typography.headlineMedium.copy(
                shadow = Shadow(color = LovableNeonPink, blurRadius = 8f)
            ),
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Text(
            text = "The absolute hottest tracks moving the cyberspace grid.",
            color = LovableTextSecondary,
            fontSize = 13.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 20.dp)
        ) {
            items(trendingSongs) { song ->
                SongRowItem(
                    song = song,
                    isPlayingOnDevice = viewModel.currentSong.collectAsStateWithLifecycle().value?.id == song.id,
                    isFavorited = favorites.any { it.id == song.id },
                    onPlayClick = { onPlaySong(song) },
                    onFavClick = { onToggleFav(song) }
                )
            }
        }
    }
}


// ==========================================
// 3. PLAYLISTS SCREEN SECTION
// ==========================================

@Composable
fun PlaylistsScreen(
    viewModel: MusicViewModel,
    favorites: List<Song>,
    onPlaySong: (Song) -> Unit,
    onToggleFav: (Song) -> Unit
) {
    val allSongs = viewModel.allSongs
    
    val playlistNames = listOf("Liquid Neon", "Vapor Nostalgia", "Cyber Resonance")
    var selectedPlaylist by remember { mutableStateOf("Liquid Neon") }

    val currentPlaylistSongs = allSongs.filter { it.playlistCategory == selectedPlaylist }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Text(
            text = "Techno Playlists",
            color = LovableTextPrimary,
            fontSize = 24.sp,
            fontWeight = FontWeight.Black,
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 14.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            playlistNames.forEach { name ->
                val isSelected = name == selectedPlaylist
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (isSelected) LovableNeonViolet else Color.White.copy(alpha = 0.05f))
                        .border(
                            width = 1.dp,
                            color = if (isSelected) LovableNeonCyan else Color.Transparent,
                            shape = RoundedCornerShape(14.dp)
                        )
                        .clickable { selectedPlaylist = name }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = name,
                        color = if (isSelected) LovableTextPrimary else LovableTextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.PlaylistPlay,
                contentDescription = null,
                tint = LovableNeonCyan,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "$selectedPlaylist • ${currentPlaylistSongs.size} Streams",
                color = LovableTextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 20.dp)
        ) {
            items(currentPlaylistSongs) { song ->
                SongRowItem(
                    song = song,
                    isPlayingOnDevice = viewModel.currentSong.collectAsStateWithLifecycle().value?.id == song.id,
                    isFavorited = favorites.any { it.id == song.id },
                    onPlayClick = { onPlaySong(song) },
                    onFavClick = { onToggleFav(song) }
                )
            }
        }
    }
}


// ==========================================
// 4. FAVORITES SCREEN SECTION
// ==========================================

@Composable
fun FavoritesScreen(
    favorites: List<Song>,
    currentSong: Song?,
    onPlaySong: (Song) -> Unit,
    onToggleFav: (Song) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 4.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Favorite,
                contentDescription = null,
                tint = LovableNeonPink,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "My Favorites",
                color = LovableTextPrimary,
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                style = MaterialTheme.typography.headlineMedium.copy(
                    shadow = Shadow(color = LovableNeonPink, blurRadius = 10f)
                )
            )
        }

        Text(
            text = "Your personalized collection of futuristic beats.",
            color = LovableTextSecondary,
            fontSize = 13.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (favorites.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    borderGlowColor = LovableNeonPink
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth().padding(16.dp)
                    ) {
                        val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                        val heartScale by infiniteTransition.animateFloat(
                            initialValue = 1.0f,
                            targetValue = 1.25f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(1200, easing = EaseInOutSine),
                                repeatMode = RepeatMode.Reverse
                            ), label = "visual_wobble"
                        )

                        Icon(
                            imageVector = Icons.Filled.Favorite,
                            contentDescription = "Pulsing favorite logo indicator",
                            tint = LovableNeonPink,
                            modifier = Modifier
                                .size(64.dp)
                                .scale(heartScale)
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Text(
                            text = "Your heart is empty",
                            color = LovableTextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Save tracks into your favorites by clicking the heart button on cards.",
                            color = LovableTextSecondary,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 20.dp)
            ) {
                items(favorites) { song ->
                    SongRowItem(
                        song = song,
                        isPlayingOnDevice = currentSong?.id == song.id,
                        isFavorited = true,
                        onPlayClick = { onPlaySong(song) },
                        onFavClick = { onToggleFav(song) }
                    )
                }
            }
        }
    }
}


// ==========================================
// 5. SEARCH SCREEN SECTION
// ==========================================

@Composable
fun SearchScreen(
    searchQuery: String,
    filteredSongs: List<Song>,
    favorites: List<Song>,
    currentSong: Song?,
    onSearchChanged: (String) -> Unit,
    onPlaySong: (Song) -> Unit,
    onToggleFav: (Song) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Text(
            text = "Secure Filter Library",
            color = LovableTextPrimary,
            fontSize = 24.sp,
            fontWeight = FontWeight.Black,
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White.copy(alpha = 0.05f))
                .border(
                    width = 1.dp,
                    color = Color.White.copy(alpha = 0.1f), // border border-white/10
                    shape = RoundedCornerShape(16.dp)
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search icon label",
                    tint = LovableNeonCyan,
                    modifier = Modifier.size(22.dp)
                )

                Spacer(modifier = Modifier.width(10.dp))

                TextField(
                    value = searchQuery,
                    onValueChange = onSearchChanged,
                    placeholder = {
                        Text(
                            text = "Search your favorite songs...",
                            color = LovableTextMuted,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    textStyle = LocalTextStyle.current.copy(
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        cursorColor = LovableNeonCyan
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("search_field_input")
                )

                if (searchQuery.isNotEmpty()) {
                    IconButton(
                        onClick = { onSearchChanged("") },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear search",
                            tint = LovableTextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        if (filteredSongs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.RunningWithErrors,
                        contentDescription = null,
                        tint = LovableTextMuted,
                        modifier = Modifier.size(52.dp)
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "No matches inside cyber logs",
                        color = LovableTextSecondary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 20.dp)
            ) {
                items(filteredSongs) { song ->
                    SongRowItem(
                        song = song,
                        isPlayingOnDevice = currentSong?.id == song.id,
                        isFavorited = favorites.any { it.id == song.id },
                        onPlayClick = { onPlaySong(song) },
                        onFavClick = { onToggleFav(song) }
                    )
                }
            }
        }
    }
}


// ==========================================
// PERSISTENT BOTTOM GLOBAL PLAYER PANEL
// ==========================================

@Composable
fun GlobalPlayerDock(
    viewModel: MusicViewModel,
    currentSong: Song?,
    isPlaying: Boolean,
    favorites: List<Song>,
    amplitudes: List<Float>,
    mediaVolume: Float,
    isDarkPurple: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_aura")
    
    val pulseBrightness by infiniteTransition.animateFloat(
        initialValue = if (isPlaying) 0.1f else 0.02f,
        targetValue = if (isPlaying) 0.35f else 0.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ), label = "aurapulse"
    )

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        shape = RoundedCornerShape(24.dp),
        modifier = modifier
            .testTag("global_player_dock")
            .fillMaxWidth()
            .heightIn(min = 90.dp)
            .graphicsLayer {
                shadowElevation = 12.dp.toPx()
            }
    ) {
        Box(
            modifier = Modifier
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFA11092F),
                            Color(0xFA1D0A3D)
                        )
                    )
                )
                .border(
                    width = 1.dp,
                    color = if (isPlaying) LovableNeonViolet.copy(alpha = 0.5f) else LovableGlassBorder,
                    shape = RoundedCornerShape(24.dp)
                )
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .graphicsLayer(alpha = pulseBrightness)
                    .blur(20.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(LovableNeonViolet, Color.Transparent)
                        )
                    )
            )

            if (currentSong == null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MusicVideo,
                        contentDescription = null,
                        tint = LovableNeonCyan,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "LOVABLE is ready. Pick a stream to feel the beat!",
                        color = LovableTextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                val currentTimeMs by viewModel.currentTimeMs.collectAsStateWithLifecycle()
                val progressFraction = if (currentSong.durationMs > 0) {
                    currentTimeMs.toFloat() / currentSong.durationMs
                } else {
                    0f
                }

                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1.2f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(RoundedCornerShape(8.dp))
                            ) {
                                AsyncImage(
                                    model = currentSong.coverUrl,
                                    contentDescription = "Active cover thumbnail",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = currentSong.title,
                                    color = LovableTextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = currentSong.artist,
                                    color = LovableTextSecondary,
                                    fontSize = 11.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.weight(1.5f),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = { viewModel.playPrevious() },
                                modifier = Modifier.size(34.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SkipPrevious,
                                    contentDescription = "Skip Previous key",
                                    tint = LovableTextPrimary
                                )
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            IconButton(
                                onClick = { viewModel.togglePlayPause() },
                                modifier = Modifier
                                    .size(46.dp)
                                    .background(LovableNeonViolet, CircleShape)
                                    .border(1.dp, LovableNeonCyan, CircleShape)
                            ) {
                                Icon(
                                    imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                    contentDescription = "Main toggle shortcut space",
                                    tint = LovableTextPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            IconButton(
                                onClick = { viewModel.playNext() },
                                modifier = Modifier.size(34.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SkipNext,
                                    contentDescription = "Skip Next dynamic",
                                    tint = LovableTextPrimary
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.weight(1.1f),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = { viewModel.toggleFavorite(currentSong) }
                            ) {
                                Icon(
                                    imageVector = if (favorites.any { it.id == currentSong.id }) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                    contentDescription = "Player favored toggles",
                                    tint = if (favorites.any { it.id == currentSong.id }) LovableNeonPink else LovableTextSecondary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            Icon(
                                imageVector = if (mediaVolume == 0f) Icons.Default.VolumeMute else Icons.Default.VolumeUp,
                                contentDescription = "Volume speaker tracker",
                                tint = LovableTextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Slider(
                                value = mediaVolume,
                                onValueChange = { viewModel.updateVolume(it) },
                                modifier = Modifier.width(54.dp),
                                colors = SliderDefaults.colors(
                                    thumbColor = LovableNeonCyan,
                                    activeTrackColor = LovableNeonViolet,
                                    inactiveTrackColor = Color.White.copy(alpha = 0.2f)
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = formatDuration(currentTimeMs),
                            color = LovableTextSecondary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.width(36.dp),
                            textAlign = TextAlign.Start
                        )

                        Slider(
                            value = progressFraction,
                            onValueChange = { viewModel.seekToFraction(it) },
                            modifier = Modifier.weight(1f),
                            colors = SliderDefaults.colors(
                                thumbColor = LovableNeonCyan,
                                activeTrackColor = LovableNeonPink,
                                inactiveTrackColor = Color.White.copy(alpha = 0.15f)
                            )
                        )

                        Text(
                            text = currentSong.durationText,
                            color = LovableTextSecondary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.width(36.dp),
                            textAlign = TextAlign.End
                        )
                    }

                    BouncingVisualizer(
                        amplitudes = amplitudes,
                        isPlaying = isPlaying,
                        isDarkPurple = isDarkPurple,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp)
                    )
                }
            }
        }
    }
}

// Visual duration helper
fun formatDuration(ms: Long): String {
    val totalSecs = ms / 1000
    val mins = totalSecs / 60
    val secs = totalSecs % 60
    return String.format("%02d:%02d", mins, secs)
}
