package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AdminPanelSettings
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.BookmarkBorder
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.DownloadDone
import androidx.compose.material.icons.rounded.DownloadForOffline
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.BLOG_CATEGORIES
import com.example.data.model.BlogUiItem
import com.example.ui.MainDashboardState
import com.example.ui.components.BlogCoverImage
import com.example.ui.components.OriginBadge
import com.example.ui.theme.LocalSyntheticaColors

private const val FILTER_ALL = "All"
private const val FILTER_OFFLINE = "Saved Offline"
private const val FILTER_BOOKMARKED = "Bookmarked"

@Composable
fun BlogsScreen(
    state: MainDashboardState,
    initialSavedOfflineOnly: Boolean,
    onOpenArticle: (String) -> Unit,
    onToggleBookmark: (BlogUiItem) -> Unit,
    onToggleOfflineSave: (BlogUiItem) -> Unit,
    onOpenAdminManager: () -> Unit
) {
    val synth = LocalSyntheticaColors.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember(initialSavedOfflineOnly) {
        mutableStateOf(if (initialSavedOfflineOnly) FILTER_OFFLINE else FILTER_ALL)
    }
    var sortSection by remember { mutableStateOf("Latest") }

    val filterOptions = remember {
        listOf(FILTER_ALL, FILTER_OFFLINE, FILTER_BOOKMARKED) + BLOG_CATEGORIES
    }

    val visibleBlogs = remember(state.blogs, searchQuery, selectedFilter, sortSection, state.isOnline, state.profile) {
        val base = state.blogs.filter { item ->
            val publishedVisible = item.article.published || item.originScope != "GLOBAL_ADMIN"
            val offlineAccessible = state.isOnline || item.savedOffline || item.article.sampleContent
            publishedVisible && offlineAccessible
        }

        val byCategory = when (selectedFilter) {
            FILTER_ALL -> base
            FILTER_OFFLINE -> base.filter { it.savedOffline }
            FILTER_BOOKMARKED -> base.filter { it.bookmarked }
            else -> base.filter { it.article.category == selectedFilter }
        }

        val q = searchQuery.trim().lowercase()
        val searched = if (q.isEmpty()) byCategory else {
            byCategory.filter {
                it.article.title.lowercase().contains(q) ||
                    it.article.subtitle.lowercase().contains(q) ||
                    it.article.authorName.lowercase().contains(q) ||
                    it.article.category.lowercase().contains(q) ||
                    it.article.tags.any { tag -> tag.lowercase().contains(q) }
            }
        }

        when (sortSection) {
            "Popular" -> searched.sortedByDescending { it.article.popularScore }
            "Recommended" -> searched.sortedByDescending { item ->
                val catBoost = if (state.profile.currentGpa < state.profile.targetGpa &&
                    (item.article.category == "GPA Improvement" || item.article.category == "Study Techniques")
                ) 200L else 0L
                item.article.popularScore + catBoost
            }
            else -> searched
        }
    }

    val featuredArticle = remember(visibleBlogs) {
        visibleBlogs.firstOrNull { it.article.featured } ?: visibleBlogs.firstOrNull()
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        BoxWithConstraints(modifier = Modifier.widthIn(max = 920.dp).fillMaxSize()) {
            val isWide = maxWidth >= 620.dp
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header + Admin Blog Studio button
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "ACADEMIC KNOWLEDGE HUB",
                                style = MaterialTheme.typography.labelSmall,
                                color = synth.royalBlue
                            )
                            Text(
                                text = "Editorial Insights",
                                style = MaterialTheme.typography.headlineMedium
                            )
                        }
                        if (state.isAdminActive) {
                            Button(
                                onClick = onOpenAdminManager,
                                shape = RoundedCornerShape(999.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = synth.executiveDark),
                                modifier = Modifier.testTag("blogs_open_admin_button")
                            ) {
                                Icon(
                                    Icons.Rounded.AdminPanelSettings,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text("Manage Blogs")
                            }
                        }
                    }
                }

                // Search Bar
                item {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = "Search articles") },
                        placeholder = { Text("Search GPA guides, study techniques, authors...") },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("blogs_search_input")
                    )
                }

                // Category & Saved Offline Chips
                item {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filterOptions) { option ->
                            FilterChip(
                                selected = selectedFilter == option,
                                onClick = { selectedFilter = option },
                                label = {
                                    Text(
                                        text = if (option == FILTER_OFFLINE) {
                                            "Saved Offline (${state.offlineArticlesCount})"
                                        } else option
                                    )
                                }
                            )
                        }
                    }
                }

                // Editorial Sort Tabs: Latest / Popular / Recommended
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Latest", "Popular", "Recommended").forEach { tab ->
                            val selected = sortSection == tab
                            Surface(
                                shape = RoundedCornerShape(999.dp),
                                color = if (selected) synth.executiveDark else MaterialTheme.colorScheme.surface,
                                shadowElevation = if (selected) 3.dp else 1.dp,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(999.dp))
                                    .clickable { sortSection = tab }
                            ) {
                                Text(
                                    text = tab,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }
                }

                // Featured Article Hero Card (shown when viewing All without search query)
                if (featuredArticle != null && searchQuery.isBlank() && selectedFilter == FILTER_ALL) {
                    item {
                        FeaturedBlogHeroCard(
                            item = featuredArticle,
                            onClick = { onOpenArticle(featuredArticle.article.id) },
                            onToggleBookmark = { onToggleBookmark(featuredArticle) },
                            onToggleOffline = { onToggleOfflineSave(featuredArticle) }
                        )
                    }
                }

                // Empty State (especially for Saved Offline or strict filter)
                if (visibleBlogs.isEmpty()) {
                    item {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                            color = MaterialTheme.colorScheme.surface
                        ) {
                            Column(
                                modifier = Modifier.padding(28.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = if (selectedFilter == FILTER_OFFLINE) Icons.Rounded.DownloadForOffline else Icons.Rounded.CloudOff,
                                    contentDescription = null,
                                    tint = synth.royalBlue,
                                    modifier = Modifier.size(38.dp)
                                )
                                Text(
                                    text = if (selectedFilter == FILTER_OFFLINE) {
                                        "No Articles Downloaded Offline Yet"
                                    } else {
                                        "No Matching Articles"
                                    },
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = if (selectedFilter == FILTER_OFFLINE) {
                                        "Tap the download icon on any editorial article to read it anytime without internet."
                                    } else {
                                        "Try clearing your search query or switching to All categories."
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = synth.mutedText
                                )
                            }
                        }
                    }
                } else {
                    val gridRows = if (isWide) visibleBlogs.chunked(2) else visibleBlogs.map { listOf(it) }
                    items(gridRows, key = { row -> row.joinToString("_") { it.article.id } }) { rowItems ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            rowItems.forEach { item ->
                                BlogArticleCard(
                                    modifier = Modifier.weight(1f),
                                    item = item,
                                    onClick = { onOpenArticle(item.article.id) },
                                    onToggleBookmark = { onToggleBookmark(item) },
                                    onToggleOffline = { onToggleOfflineSave(item) }
                                )
                            }
                            if (isWide && rowItems.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FeaturedBlogHeroCard(
    item: BlogUiItem,
    onClick: () -> Unit,
    onToggleBookmark: () -> Unit,
    onToggleOffline: () -> Unit
) {
    val synth = LocalSyntheticaColors.current
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .clickable { onClick() }
            .testTag("featured_blog_hero"),
        shape = RoundedCornerShape(24.dp),
        color = synth.executiveDark,
        shadowElevation = 8.dp
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(190.dp)
            ) {
                BlogCoverImage(
                    coverKey = item.article.coverKey,
                    contentDescription = item.article.title,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, synth.executiveDark.copy(alpha = 0.92f))
                            )
                        )
                )
                Row(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = synth.royalBlue
                    ) {
                        Text(
                            text = "FEATURED • ${item.article.category}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                    OriginBadge(
                        sampleContent = item.article.sampleContent,
                        published = item.article.published,
                        originScope = item.originScope
                    )
                }
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                ) {
                    Text(
                        text = item.article.title,
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White
                    )
                }
            }

            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = item.article.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFCBD5E1)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = item.article.authorName,
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White
                        )
                        Text(
                            text = "${item.article.publishedDateLabel} • ${item.article.readMinutes} min read",
                            style = MaterialTheme.typography.bodySmall,
                            color = synth.electricBlue
                        )
                    }
                    Row {
                        IconButton(onClick = onToggleOffline) {
                            Icon(
                                imageVector = if (item.savedOffline) Icons.Rounded.DownloadDone else Icons.Rounded.DownloadForOffline,
                                contentDescription = if (item.savedOffline) "Saved offline" else "Download for offline",
                                tint = if (item.savedOffline) synth.teal else Color.White
                            )
                        }
                        IconButton(onClick = onToggleBookmark) {
                            Icon(
                                imageVector = if (item.bookmarked) Icons.Rounded.Bookmark else Icons.Rounded.BookmarkBorder,
                                contentDescription = "Bookmark article",
                                tint = if (item.bookmarked) synth.electricBlue else Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BlogArticleCard(
    modifier: Modifier,
    item: BlogUiItem,
    onClick: () -> Unit,
    onToggleBookmark: () -> Unit,
    onToggleOffline: () -> Unit
) {
    val synth = LocalSyntheticaColors.current
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.98f else 1f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 400f),
        label = "blogCardScale"
    )

    Surface(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(22.dp))
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        pressed = true
                        tryAwaitRelease()
                        pressed = false
                    },
                    onTap = { onClick() }
                )
            }
            .testTag("blog_card_${item.article.id}"),
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 4.dp
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(136.dp)
            ) {
                BlogCoverImage(
                    coverKey = item.article.coverKey,
                    contentDescription = item.article.title,
                    modifier = Modifier.fillMaxSize()
                )
                Row(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = synth.executiveDark.copy(alpha = 0.86f)
                    ) {
                        Text(
                            text = item.article.category,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
                if (item.savedOffline) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(10.dp),
                        shape = RoundedCornerShape(999.dp),
                        color = synth.teal
                    ) {
                        Text(
                            text = "Offline ✓",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OriginBadge(
                    sampleContent = item.article.sampleContent,
                    published = item.article.published,
                    originScope = item.originScope
                )
                Text(
                    text = item.article.title,
                    style = MaterialTheme.typography.headlineSmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = item.article.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = synth.mutedText,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.article.authorName,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${item.article.publishedDateLabel} • ${item.article.readMinutes} min",
                            style = MaterialTheme.typography.bodySmall,
                            color = synth.mutedText
                        )
                    }
                    Row {
                        IconButton(onClick = onToggleOffline) {
                            Icon(
                                imageVector = if (item.savedOffline) Icons.Rounded.DownloadDone else Icons.Rounded.DownloadForOffline,
                                contentDescription = if (item.savedOffline) "Remove offline copy" else "Save offline",
                                tint = if (item.savedOffline) synth.teal else synth.mutedText
                            )
                        }
                        IconButton(onClick = onToggleBookmark) {
                            Icon(
                                imageVector = if (item.bookmarked) Icons.Rounded.Bookmark else Icons.Rounded.BookmarkBorder,
                                contentDescription = "Bookmark",
                                tint = if (item.bookmarked) synth.royalBlue else synth.mutedText
                            )
                        }
                    }
                }
            }
        }
    }
}
