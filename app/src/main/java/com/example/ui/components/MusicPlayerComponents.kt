package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.Song
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlin.math.sin

// ==========================================
// BACKGROUNDS & DYNAMIC GLOW EFFECTS
// ==========================================

@Composable
fun LovableBackgroundWrapper(
    isDarkPurple: Boolean,
    content: @Composable BoxScope.() -> Unit
) {
    val gradientBrush = remember(isDarkPurple) {
        if (isDarkPurple) {
            Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF0C071C),
                    Color(0xFF0F0827),
                    Color(0xFF1B072E),
                    Color(0xFF07040D)
                )
            )
        } else {
            Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF1F1138),
                    Color(0xFF2C194D),
                    Color(0xFF140B2D),
                    Color(0xFF2D1442)
                )
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradientBrush)
    ) {
        AtmosphericGlowCircles(isDarkPurple)
        FloatingStarParticles(isDarkPurple)

        Box(modifier = Modifier.fillMaxSize()) {
            content()
        }
    }
}

@Composable
fun AtmosphericGlowCircles(isDarkPurple: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "atmosphere")

    val driftOffset by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(40000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "drift"
    )

    val scaleFactor by infiniteTransition.animateFloat(
        initialValue = 0.95f, targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ), label = "pulse"
    )

    Canvas(modifier = Modifier.fillMaxSize().blur(80.dp).graphicsLayer(alpha = 0.8f)) {
        val canvasWidth = size.width
        val canvasHeight = size.height

        // 1. Top-Left Purple Ambient Glow (bg-purple-600/30 blur-80) with subtle drift
        val glow1X = -canvasWidth * 0.05f + (sin(Math.toRadians(driftOffset.toDouble())) * 45).toFloat()
        val glow1Y = -canvasHeight * 0.05f + (sin(Math.toRadians((driftOffset + 45).toDouble())) * 35).toFloat()
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    LovableNeonViolet.copy(alpha = 0.35f),
                    Color.Transparent
                ),
                center = Offset(glow1X, glow1Y),
                radius = 240.dp.toPx() * scaleFactor
            ),
            radius = 240.dp.toPx() * scaleFactor,
            center = Offset(glow1X, glow1Y)
        )

        // 2. Bottom-Right Indigo Ambient Glow (bg-indigo-600/20 blur-80) with subtle drift
        val glow2X = canvasWidth * 1.05f - (sin(Math.toRadians(driftOffset.toDouble())) * 50).toFloat()
        val glow2Y = canvasHeight * 0.9f - (sin(Math.toRadians((driftOffset * 0.5).toDouble())) * 40).toFloat()
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    LovableNeonCyan.copy(alpha = 0.25f),
                    Color.Transparent
                ),
                center = Offset(glow2X, glow2Y),
                radius = 220.dp.toPx() * scaleFactor
            ),
            radius = 220.dp.toPx() * scaleFactor,
            center = Offset(glow2X, glow2Y)
        )

        // 3. Central-Left Pink Ambient Glow (bg-pink-500/10 blur-60) with subtle drift
        val glow3X = canvasWidth * 0.25f + (sin(Math.toRadians((driftOffset + 180).toDouble())) * 60).toFloat()
        val glow3Y = canvasHeight * 0.45f + (sin(Math.toRadians((driftOffset + 120).toDouble())) * 50).toFloat()
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    LovableNeonPink.copy(alpha = 0.15f),
                    Color.Transparent
                ),
                center = Offset(glow3X, glow3Y),
                radius = 160.dp.toPx() * scaleFactor
            ),
            radius = 160.dp.toPx() * scaleFactor,
            center = Offset(glow3X, glow3Y)
        )
    }
}

@Composable
fun FloatingStarParticles(isDarkPurple: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "particles")

    val particles = remember {
        List(22) {
            val rx = (0..100).random().toFloat() / 100f
            val ry = (0..100).random().toFloat() / 100f
            val rSize = (3..8).random().dp
            val rMaxAlpha = (0.2f..0.7f).random().coerceIn(0.1f, 0.8f)
            val rPeriod = (5000..9000).random()
            Triple(rx, ry, Pair(rSize, Pair(rMaxAlpha, rPeriod)))
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        particles.forEach { (rx, ry, details) ->
            val particleSize = details.first
            val maxAlpha = details.second.first
            val period = details.second.second

            val alphaAnim by infiniteTransition.animateFloat(
                initialValue = 0.05f,
                targetValue = maxAlpha,
                animationSpec = infiniteRepeatable(
                    animation = tween(period, easing = EaseInOutQuad),
                    repeatMode = RepeatMode.Reverse
                ), label = "star_alpha"
            )

            val driftAnim by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = -120f,
                animationSpec = infiniteRepeatable(
                    animation = tween(period * 3, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                ), label = "star_drift"
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(alpha = alphaAnim)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val canvasWidth = this.size.width
                    val canvasHeight = this.size.height
                    
                    if (canvasWidth > 0f && canvasHeight > 0f) {
                        val px = rx * canvasWidth
                        val py = ((ry * canvasHeight + driftAnim) % canvasHeight + canvasHeight) % canvasHeight
                        
                        drawCircle(
                            color = if (isDarkPurple) LovableTextPrimary else LovableNeonCyan,
                            radius = particleSize.toPx() / 2f,
                            center = Offset(px, py)
                        )
                    }
                }
            }
        }
    }
}

private fun ClosedRange<Float>.random() =
    kotlin.random.Random.nextDouble(start.toDouble(), endInclusive.toDouble()).toFloat()

// ==========================================
// COMPOSABLE GLASS SURFACES
// ==========================================

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    borderGlowColor: Color = LovableGlassBorder,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val clickableModifier = if (onClick != null) {
        Modifier.clickable(onClick = onClick)
    } else {
        Modifier
    }

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(LovableGlassCard)
            .border(
                width = 1.dp,
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        LovableGlassBorder,
                        borderGlowColor.copy(alpha = 0.25f),
                        Color.Transparent,
                        LovableGlassBorder.copy(alpha = 0.1f)
                    )
                ),
                shape = RoundedCornerShape(20.dp)
            )
            .then(clickableModifier)
            .padding(18.dp)
    ) {
        content()
    }
}

// ==========================================
// EXPONENTIAL BOUNCING LOGO ("LV" HEART)
// ==========================================

@Composable
fun LovableLogo(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isHovered by remember { mutableStateOf(false) }
    
    val transition = updateTransition(targetState = isHovered, label = "logo_bounce")
    
    val scaleFactor by transition.animateFloat(
        transitionSpec = {
            if (targetState) {
                spring(dampingRatio = 0.45f, stiffness = Spring.StiffnessMedium)
            } else {
                spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessLow)
            }
        }, label = "scale"
    ) { hovered ->
        if (hovered) 1.22f else 1.0f
    }

    Box(
        modifier = modifier
            .testTag("app_logo")
            .scale(scaleFactor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .graphicsLayer(alpha = if (isHovered) 0.61f else 0.2f)
                .blur(8.dp)
                .background(LovableNeonViolet, CircleShape)
        )

        Box(
            modifier = Modifier
                .size(44.dp)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(LovableNeonPink, LovableNeonViolet)
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
                .border(2.dp, LovableTextPrimary.copy(alpha = 0.8f), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "LV",
                color = LovableTextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                style = MaterialTheme.typography.titleMedium.copy(
                    shadow = Shadow(
                        color = Color.Black.copy(alpha = 0.7f),
                        offset = Offset(2f, 2f),
                        blurRadius = 4f
                    )
                )
            )
        }
    }

    LaunchedEffect(Unit) {
        delay(1200)
        isHovered = true
        delay(600)
        isHovered = false
    }
}

// ==========================================
// BOUNCING NEON SOUND VISUALIZER
// ==========================================

@Composable
fun BouncingVisualizer(
    amplitudes: List<Float>,
    isPlaying: Boolean,
    isDarkPurple: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .height(38.dp)
            .padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        val barsCount = amplitudes.size
        
        for (i in 0 until barsCount) {
            val ampHeight by animateFloatAsState(
                targetValue = if (isPlaying) amplitudes[i] else 0.15f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessVeryLow
                ), label = "visualizer_bar_$i"
            )

            val barColor = remember(i, isDarkPurple) {
                when {
                    i % 3 == 0 -> LovableNeonViolet
                    i % 3 == 1 -> LovableNeonPink
                    else -> LovableNeonCyan
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(ampHeight.coerceIn(0.12f, 1.0f))
                    .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                    .background(barColor)
                    .border(
                        width = 0.5.dp,
                        color = LovableTextPrimary.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
                    )
            )
        }
    }
}

// ==========================================
// MUSIC CARDS & GRID GRAPHICS
// ==========================================

@Composable
fun SongGridCard(
    song: Song,
    isPlayingOnDevice: Boolean,
    isFavorited: Boolean,
    onPlayClick: () -> Unit,
    onFavClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scaleFactor by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1.0f,
        animationSpec = spring(dampingRatio = 0.55f), label = "press_scale"
    )

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        modifier = modifier
            .testTag("song_card_${song.id}")
            .scale(scaleFactor)
            .clickable(
                interactionSource = interactionSource,
                indication = LocalIndication.current
            ) {
                onPlayClick()
            }
    ) {
        Column(
            modifier = Modifier
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.08f),
                            Color.White.copy(alpha = 0.03f)
                        )
                    ),
                    shape = RoundedCornerShape(24.dp)
                )
                .border(
                    width = 1.dp,
                    color = if (isPlayingOnDevice) LovableNeonViolet.copy(alpha = 0.7f) else LovableGlassBorder,
                    shape = RoundedCornerShape(24.dp)
                )
                .padding(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(18.dp))
            ) {
                AsyncImage(
                    model = song.coverUrl,
                    contentDescription = "Album image mapping: ${song.album}",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                if (isPlayingOnDevice) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.45f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .background(LovableNeonVioletGlow, CircleShape)
                                .border(1.dp, LovableNeonGreen, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.VolumeUp,
                                contentDescription = "Active Playing",
                                tint = LovableNeonGreen,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.15f))
                    )
                }

                IconButton(
                    onClick = onFavClick,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .background(Color.Black.copy(alpha = 0.45f), CircleShape)
                        .size(34.dp)
                ) {
                    Icon(
                        imageVector = if (isFavorited) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Toggle favorite from item",
                        tint = if (isFavorited) LovableNeonPink else LovableTextPrimary,
                        modifier = Modifier.size(17.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                        .background(Color.Black.copy(alpha = 0.75f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = song.durationText,
                        color = LovableTextPrimary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = song.title,
                color = LovableTextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = song.artist,
                color = LovableTextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun SongRowItem(
    song: Song,
    isPlayingOnDevice: Boolean,
    isFavorited: Boolean,
    onPlayClick: () -> Unit,
    onFavClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .testTag("song_row_${song.id}")
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(if (isPlayingOnDevice) LovableGlassCardPurple else Color.White.copy(alpha = 0.04f))
            .border(
                width = 1.dp,
                color = if (isPlayingOnDevice) LovableNeonViolet.copy(alpha = 0.4f) else Color.Transparent,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { onPlayClick() }
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(10.dp))
        ) {
            AsyncImage(
                model = song.coverUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            if (isPlayingOnDevice) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.PlayArrow,
                        contentDescription = "Playing Now",
                        tint = LovableNeonCyan,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = song.title,
                color = if (isPlayingOnDevice) LovableNeonCyan else LovableTextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "${song.artist} • ${song.album}",
                color = LovableTextSecondary,
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Text(
            text = song.durationText,
            color = LovableTextSecondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        IconButton(
            onClick = onFavClick,
            modifier = Modifier.testTag("heart_favorite_${song.id}")
        ) {
            Icon(
                imageVector = if (isFavorited) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                contentDescription = "Toggle favorite state",
                tint = if (isFavorited) LovableNeonPink else LovableTextSecondary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// ==========================================
// TOAST BANNER / TRANSIENT POPUP GADGETS
// ==========================================

@Composable
fun LovableToastNotification(
    message: String?,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = message != null,
        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 40.dp, start = 20.dp, end = 20.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = LovableNeonViolet.copy(alpha = 0.8f),
                    shape = RoundedCornerShape(16.dp)
                )
        ) {
            Row(
                modifier = Modifier
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF160931).copy(alpha = 0.95f),
                                Color(0xFF2A0B42).copy(alpha = 0.95f)
                            )
                        )
                    )
                    .padding(horizontal = 16.dp, vertical = 14.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(LovableNeonVioletGlow, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.MusicNote,
                        contentDescription = null,
                        tint = LovableNeonCyan,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = message ?: "",
                    color = LovableTextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

// ==========================================
// CUSTOM SCROLLBAR UTILITY FOR JETPACK COMPOSE
// ==========================================

@Composable
fun CustomGlassScrollbar(
    scrollProgress: Float,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .width(5.dp)
            .fillMaxHeight()
            .background(Color.White.copy(alpha = 0.05f), CircleShape)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.2f)
                .graphicsLayer {
                    translationY = scrollProgress * (size.height - size.height * 0.2f)
                }
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(LovableNeonViolet, LovableNeonPink)
                    ),
                    shape = CircleShape
                )
        )
    }
}
