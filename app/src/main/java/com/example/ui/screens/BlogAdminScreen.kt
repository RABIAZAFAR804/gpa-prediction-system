package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Publish
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Unpublished
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.model.BLOG_CATEGORIES
import com.example.data.model.BlogUiItem
import com.example.ui.MainDashboardState
import com.example.ui.components.OriginBadge
import com.example.ui.theme.LocalSyntheticaColors

@Composable
fun BlogAdminScreen(
    state: MainDashboardState,
    onBack: () -> Unit,
    onSavePost: (
        existingId: String?,
        originScope: String,
        title: String,
        subtitle: String,
        authorName: String,
        authorRole: String,
        category: String,
        tagsRaw: String,
        coverKey: String,
        readMinutes: Long,
        contentMarkdown: String,
        published: Boolean,
        featured: Boolean,
        onComplete: (Boolean) -> Unit
    ) -> Unit,
    onTogglePublish: (BlogUiItem) -> Unit,
    onDeletePost: (BlogUiItem) -> Unit
) {
    val synth = LocalSyntheticaColors.current
    val editablePosts = remember(state.blogs) {
        state.blogs.filter { it.originScope != "SAMPLE" }
    }
    var editorTarget by remember { mutableStateOf<BlogUiItem?>(null) }
    var showEditor by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .widthIn(max = 820.dp)
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                        }
                        Column {
                            Text(
                                text = if (state.isBootstrapAdmin) "GLOBAL ADMIN BLOG CMS" else "ADMIN EDITORIAL STUDIO",
                                style = MaterialTheme.typography.labelSmall,
                                color = synth.teal
                            )
                            Text("Manage Articles", style = MaterialTheme.typography.headlineMedium)
                        }
                    }
                    Button(
                        onClick = {
                            editorTarget = null
                            showEditor = true
                        },
                        enabled = state.isOnline,
                        shape = RoundedCornerShape(999.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = synth.executiveDark),
                        modifier = Modifier.testTag("admin_new_post_button")
                    ) {
                        Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("New Post")
                    }
                }
            }

            if (editablePosts.isEmpty()) {
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("No Admin-Published Posts Yet", style = MaterialTheme.typography.titleMedium)
                            Text(
                                text = "Tap New Post to author an editorial guide with custom categories, tags, cover artwork, featured placement, and publish controls. Built-in sample articles remain clearly labeled separately.",
                                style = MaterialTheme.typography.bodySmall,
                                color = synth.mutedText
                            )
                        }
                    }
                }
            } else {
                items(editablePosts, key = { it.article.id }) { item ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surface,
                        shadowElevation = 3.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OriginBadge(
                                    sampleContent = false,
                                    published = item.article.published,
                                    originScope = item.originScope
                                )
                                if (item.article.featured) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Rounded.Star,
                                            contentDescription = "Featured",
                                            tint = synth.riskModerate,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(Modifier.width(3.dp))
                                        Text("Featured", style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                            }
                            Text(item.article.title, style = MaterialTheme.typography.titleMedium)
                            Text(
                                text = "${item.article.category} • By ${item.article.authorName} • ${item.article.readMinutes} min",
                                style = MaterialTheme.typography.bodySmall,
                                color = synth.mutedText
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                IconButton(
                                    onClick = { onTogglePublish(item) },
                                    enabled = state.isOnline
                                ) {
                                    Icon(
                                        imageVector = if (item.article.published) Icons.Rounded.Unpublished else Icons.Rounded.Publish,
                                        contentDescription = if (item.article.published) "Unpublish" else "Publish",
                                        tint = if (item.article.published) synth.riskModerate else synth.teal
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        editorTarget = item
                                        showEditor = true
                                    },
                                    enabled = state.isOnline
                                ) {
                                    Icon(Icons.Rounded.Edit, contentDescription = "Edit post", tint = synth.royalBlue)
                                }
                                IconButton(
                                    onClick = { onDeletePost(item) },
                                    enabled = state.isOnline
                                ) {
                                    Icon(Icons.Rounded.DeleteOutline, contentDescription = "Delete post", tint = synth.riskHigh)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showEditor) {
        BlogEditorDialog(
            initialItem = editorTarget,
            defaultAuthor = state.profile.displayName,
            onDismiss = { showEditor = false },
            onSave = { title, subtitle, authorName, authorRole, category, tagsRaw, coverKey, readMin, body, published, featured ->
                onSavePost(
                    editorTarget?.article?.id,
                    editorTarget?.originScope ?: if (state.isBootstrapAdmin) "GLOBAL_ADMIN" else "LOCAL_WORKSPACE",
                    title,
                    subtitle,
                    authorName,
                    authorRole,
                    category,
                    tagsRaw,
                    coverKey,
                    readMin,
                    body,
                    published,
                    featured
                ) { ok ->
                    if (ok) showEditor = false
                }
            }
        )
    }
}

@Composable
private fun BlogEditorDialog(
    initialItem: BlogUiItem?,
    defaultAuthor: String,
    onDismiss: () -> Unit,
    onSave: (
        title: String,
        subtitle: String,
        authorName: String,
        authorRole: String,
        category: String,
        tagsRaw: String,
        coverKey: String,
        readMinutes: Long,
        body: String,
        published: Boolean,
        featured: Boolean
    ) -> Unit
) {
    val art = initialItem?.article
    var title by remember { mutableStateOf(art?.title ?: "") }
    var subtitle by remember { mutableStateOf(art?.subtitle ?: "") }
    var authorName by remember { mutableStateOf(art?.authorName ?: defaultAuthor) }
    var authorRole by remember { mutableStateOf(art?.authorRole ?: "Academic Strategy Lead") }
    var category by remember { mutableStateOf(art?.category ?: BLOG_CATEGORIES.first()) }
    var tagsRaw by remember { mutableStateOf(art?.tags?.joinToString(", ") ?: "gpa, study") }
    var coverKey by remember { mutableStateOf(art?.coverKey ?: "img_blog_ai_education") }
    var readMinutes by remember { mutableStateOf((art?.readMinutes ?: 5L).toString()) }
    var body by remember {
        mutableStateOf(
            art?.contentMarkdown ?: "## Key Academic Insight\nWrite your structured academic advice here.\n\n## Action Steps\n- First actionable takeaway"
        )
    }
    var published by remember { mutableStateOf(art?.published ?: true) }
    var featured by remember { mutableStateOf(art?.featured ?: false) }

    val coverPresets = listOf(
        "img_blog_ai_education" to "AI Neural",
        "img_blog_gpa_strategy" to "GPA Steps",
        "img_blog_wellness" to "Wellness"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (initialItem == null) "Create Blog Article" else "Edit Blog Article")
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Article Title (4+ chars)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("editor_title_input")
                )
                OutlinedTextField(
                    value = subtitle,
                    onValueChange = { subtitle = it },
                    label = { Text("Subtitle / Summary") },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = authorName,
                        onValueChange = { authorName = it },
                        label = { Text("Author") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = readMinutes,
                        onValueChange = { readMinutes = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Min") },
                        singleLine = true,
                        modifier = Modifier.width(76.dp)
                    )
                }
                OutlinedTextField(
                    value = authorRole,
                    onValueChange = { authorRole = it },
                    label = { Text("Author Role") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Category", style = MaterialTheme.typography.labelMedium)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(BLOG_CATEGORIES) { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat) }
                        )
                    }
                }

                Text("Cover Artwork Preset or Image URL", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    coverPresets.forEach { (key, label) ->
                        FilterChip(
                            selected = coverKey == key,
                            onClick = { coverKey = key },
                            label = { Text(label) }
                        )
                    }
                }
                OutlinedTextField(
                    value = coverKey,
                    onValueChange = { coverKey = it },
                    label = { Text("Cover Key or HTTPS Image URL") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = tagsRaw,
                    onValueChange = { tagsRaw = it },
                    label = { Text("Tags (comma separated)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = body,
                    onValueChange = { body = it },
                    label = { Text("Article Content (use ## for TOC headings)") },
                    minLines = 5,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Published", style = MaterialTheme.typography.bodyMedium)
                    Switch(checked = published, onCheckedChange = { published = it })
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Featured Article", style = MaterialTheme.typography.bodyMedium)
                    Switch(checked = featured, onCheckedChange = { featured = it })
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        title,
                        subtitle,
                        authorName,
                        authorRole,
                        category,
                        tagsRaw,
                        coverKey,
                        readMinutes.toLongOrNull()?.coerceIn(1L, 60L) ?: 5L,
                        body,
                        published,
                        featured
                    )
                },
                shape = RoundedCornerShape(999.dp),
                modifier = Modifier.testTag("editor_save_button")
            ) {
                Text("Save Article")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
