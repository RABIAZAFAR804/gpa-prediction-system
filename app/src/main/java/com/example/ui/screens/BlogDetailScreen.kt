package com.example.ui.screens

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.BookmarkBorder
import androidx.compose.material.icons.rounded.DownloadDone
import androidx.compose.material.icons.rounded.DownloadForOffline
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Toc
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.BlogUiItem
import com.example.ui.components.BlogCoverImage
import com.example.ui.components.OriginBadge
import com.example.ui.theme.LocalSyntheticaColors
import kotlinx.coroutines.launch

private data class ArticleSection(
    val heading: String,
    val paragraphs: List<String>
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BlogDetailScreen(
    item: BlogUiItem?,
    relatedArticles: List<BlogUiItem>,
    onBack: () -> Unit,
    onToggleBookmark: (BlogUiItem) -> Unit,
    onToggleOfflineSave: (BlogUiItem) -> Unit,
    onOpenRelated: (String) -> Unit
) {
    val context = LocalContext.current
    val synth = LocalSyntheticaColors.current
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    if (item == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Article not available", style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(12.dp))
                Button(onClick = onBack) { Text("Back to Knowledge Hub") }
            }
        }
        return
    }

    val sections = remember(item.article.contentMarkdown) {
        parseMarkdownSections(item.article.contentMarkdown)
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .widthIn(max = 760.dp)
                .fillMaxSize(),
            contentPadding = PaddingValues(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Top Cover & Back Bar
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                ) {
                    BlogCoverImage(
                        coverKey = item.article.coverKey,
                        contentDescription = item.article.title,
                        modifier = Modifier.fillMaxSize()
                    )
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = synth.executiveDark.copy(alpha = 0.85f),
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(16.dp)
                            .testTag("blog_detail_back_button")
                    ) {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                    }
                }
            }

            // Editorial Header Metadata + Actions
            item {
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(999.dp),
                            color = synth.royalBlue.copy(alpha = 0.14f)
                        ) {
                            Text(
                                text = item.article.category,
                                style = MaterialTheme.typography.labelSmall,
                                color = synth.royalBlue,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                        OriginBadge(
                            sampleContent = item.article.sampleContent,
                            published = item.article.published,
                            originScope = item.originScope
                        )
                    }

                    Text(
                        text = item.article.title,
                        style = MaterialTheme.typography.headlineLarge
                    )
                    Text(
                        text = item.article.subtitle,
                        style = MaterialTheme.typography.bodyLarge,
                        color = synth.mutedText
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = item.article.authorName,
                                style = MaterialTheme.typography.titleSmall
                            )
                            Text(
                                text = "${item.article.authorRole} • ${item.article.publishedDateLabel} • ${item.article.readMinutes} min read",
                                style = MaterialTheme.typography.bodySmall,
                                color = synth.mutedText
                            )
                        }
                    }

                    if (item.article.tags.isNotEmpty()) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            item.article.tags.forEach { tag ->
                                Surface(
                                    shape = RoundedCornerShape(999.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    Text(
                                        text = "#$tag",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = synth.mutedText,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Bookmark, Share, Download for Offline Reading
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { onToggleOfflineSave(item) },
                            shape = RoundedCornerShape(999.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (item.savedOffline) synth.teal else synth.executiveDark
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("blog_detail_offline_button")
                        ) {
                            Icon(
                                imageVector = if (item.savedOffline) Icons.Rounded.DownloadDone else Icons.Rounded.DownloadForOffline,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(if (item.savedOffline) "Saved Offline" else "Download Offline")
                        }

                        FilledTonalButton(
                            onClick = { onToggleBookmark(item) },
                            shape = RoundedCornerShape(999.dp),
                            modifier = Modifier.testTag("blog_detail_bookmark_button")
                        ) {
                            Icon(
                                imageVector = if (item.bookmarked) Icons.Rounded.Bookmark else Icons.Rounded.BookmarkBorder,
                                contentDescription = "Bookmark"
                            )
                        }

                        FilledTonalButton(
                            onClick = {
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_SUBJECT, item.article.title)
                                    putExtra(
                                        Intent.EXTRA_TEXT,
                                        "${item.article.title}\n\n${item.article.subtitle}\nBy ${item.article.authorName} • Synthetica GPA Predictor"
                                    )
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Share Article"))
                            },
                            shape = RoundedCornerShape(999.dp),
                            modifier = Modifier.testTag("blog_detail_share_button")
                        ) {
                            Icon(Icons.Rounded.Share, contentDescription = "Share article")
                        }
                    }
                }
            }

            // Table of Contents
            if (sections.size >= 2) {
                item {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surface,
                        shadowElevation = 2.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Rounded.Toc, contentDescription = null, tint = synth.royalBlue)
                                Text("Table of Contents", style = MaterialTheme.typography.titleSmall)
                            }
                            sections.forEachIndexed { idx, sec ->
                                Text(
                                    text = "${idx + 1}. ${sec.heading}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = synth.royalBlue,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable {
                                            scope.launch {
                                                // Scroll directly to the section item (header + metadata + TOC = index 3 + idx)
                                                listState.animateScrollToItem(3 + idx)
                                            }
                                        }
                                        .padding(vertical = 4.dp, horizontal = 4.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Formatted Sections
            itemsIndexed(sections) { _, sec ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 2.dp
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (sec.heading.isNotBlank()) {
                            Text(
                                text = sec.heading,
                                style = MaterialTheme.typography.headlineSmall
                            )
                        }
                        sec.paragraphs.forEach { para ->
                            val isBullet = para.startsWith("- ") || para.startsWith("• ")
                            if (isBullet) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(
                                            synth.royalBlue.copy(alpha = 0.06f),
                                            RoundedCornerShape(10.dp)
                                        )
                                        .padding(10.dp)
                                ) {
                                    Text("•", color = synth.royalBlue, fontWeight = FontWeight.Bold)
                                    Text(
                                        text = para.removePrefix("- ").removePrefix("• "),
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            } else {
                                Text(
                                    text = para,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }
                    }
                }
            }

            // Related Articles
            if (relatedArticles.isNotEmpty()) {
                item {
                    Column(
                        modifier = Modifier.padding(horizontal = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("Related Articles", style = MaterialTheme.typography.headlineSmall)
                        relatedArticles.forEach { rel ->
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable { onOpenRelated(rel.article.id) },
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.surface,
                                shadowElevation = 2.dp
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    BlogCoverImage(
                                        coverKey = rel.article.coverKey,
                                        contentDescription = rel.article.title,
                                        modifier = Modifier
                                            .size(64.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = rel.article.category,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = synth.royalBlue
                                        )
                                        Text(
                                            text = rel.article.title,
                                            style = MaterialTheme.typography.titleSmall
                                        )
                                        Text(
                                            text = "${rel.article.readMinutes} min read",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = synth.mutedText
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
}

private fun parseMarkdownSections(markdown: String): List<ArticleSection> {
    val lines = markdown.lines()
    val result = mutableListOf<ArticleSection>()
    var currentHeading = ""
    var currentParagraphs = mutableListOf<String>()

    for (rawLine in lines) {
        val line = rawLine.trim()
        if (line.isEmpty()) continue
        if (line.startsWith("## ")) {
            if (currentHeading.isNotBlank() || currentParagraphs.isNotEmpty()) {
                result.add(ArticleSection(currentHeading, currentParagraphs))
            }
            currentHeading = line.removePrefix("## ").trim()
            currentParagraphs = mutableListOf()
        } else {
            currentParagraphs.add(line)
        }
    }
    if (currentHeading.isNotBlank() || currentParagraphs.isNotEmpty()) {
        result.add(ArticleSection(currentHeading, currentParagraphs))
    }
    return result
}
