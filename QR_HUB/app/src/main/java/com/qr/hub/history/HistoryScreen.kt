package com.qr.hub.history

import androidx.compose.ui.text.withStyle
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material3.*
import com.qr.hub.util.ads.BannerAdView
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import android.content.Intent
import android.widget.Toast
import androidx.lifecycle.viewmodel.compose.viewModel
import com.qr.hub.data.model.HistoryItem
import com.qr.hub.util.*
import com.qr.hub.viewmodel.HistoryViewModel
import com.qr.hub.viewmodel.HistoryUiState
import com.qr.hub.viewmodel.Tab
import java.text.SimpleDateFormat
import java.util.*

// ============================================
// REDESIGNED HISTORY SCREEN COLORS
// ============================================
private val HistoryBg = Ink950
private val HistoryCardBg = Ink800
private val HistoryCardBorder = BorderLine
private val HistoryAccent = AmberPrimary
private val HistoryTextPrimary = TextPrimary
private val HistoryTextSecondary = TextSecondary
private val HistoryTextMuted = TextTertiary
private val SelectionAccent = AmberPrimary

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Suppress("OPT_IN_IS_NOT_ENABLED")
@Composable
fun HistoryScreen(
    onItemClick: (HistoryItem) -> Unit = {},
    onBackClick: () -> Unit = {},
    onPrivacyPolicyClick: () -> Unit = {}
) {
    val viewModel: HistoryViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showSearch by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(HistoryBg)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // ============================================
            // TOP SECTION — Header & Search
            // ============================================
            Spacer(modifier = Modifier.statusBarsPadding())

            if (uiState.selectionMode) {
                SelectionTopBar(
                    selectedCount = uiState.selectedIds.size,
                    totalCount = uiState.items.size,
                    onClearSelection = { viewModel.clearSelection() },
                    onSelectAll = { viewModel.selectAll() },
                    onDelete = { showDeleteConfirm = true }
                )
            } else {
                HistoryHeader(
                    searchQuery = uiState.searchQuery,
                    showSearch = showSearch,
                    onSearchToggle = { showSearch = !showSearch },
                    onSearchChange = { viewModel.setSearchQuery(it) },
                    onBackClick = onBackClick,
                    onClearAll = { showDeleteConfirm = true },
                    onPrivacyPolicyClick = onPrivacyPolicyClick,
                    itemCount = uiState.items.size,
                    allItems = uiState.items
                )
            }

            // ============================================
            // SEGMENTED TABS — Redesigned Amber Underline
            // ============================================
            SegmentedTabRow(
                selectedTab = uiState.selectedTab,
                onTabSelected = { viewModel.setTab(tab = it) }
            )

            // ============================================
            // CATEGORY FILTER CHIPS
            // ============================================
            CategoryFilterRow(
                selectedCategory = uiState.selectedCategory,
                onCategorySelected = { viewModel.setCategory(it) },
                availableCategories = getAvailableCategories(uiState.allItemsForTab)
            )

            // ============================================
            // CONTENT — List or Empty with Smooth Motion
            // ============================================
            Crossfade(
                targetState = Pair(uiState.isLoading, uiState.items.isEmpty()),
                animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
                label = "ContentStateCrossfade"
            ) { (isLoading, isEmpty) ->
                if (isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = HistoryAccent,
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                } else if (isEmpty) {
                    EmptyState(
                        isSearching = uiState.searchQuery.isNotEmpty(),
                        selectedTab = uiState.selectedTab
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(
                            items = uiState.items,
                            key = { it.id }
                        ) { item ->
                            HistoryCard(
                                item = item,
                                isSelected = uiState.selectedIds.contains(item.id),
                                selectionMode = uiState.selectionMode,
                                onClick = {
                                    if (uiState.selectionMode) {
                                        viewModel.toggleSelection(item.id)
                                    } else {
                                        onItemClick(item)
                                    }
                                },
                                onLongClick = {
                                    viewModel.toggleSelection(item.id)
                                },
                                onFavoriteClick = {
                                    viewModel.toggleFavorite(item)
                                },
                                modifier = Modifier.animateItem(
                                    fadeInSpec = tween(durationMillis = 240, easing = FastOutSlowInEasing),
                                    fadeOutSpec = tween(durationMillis = 180, easing = FastOutLinearInEasing),
                                    placementSpec = spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = Spring.DampingRatioNoBouncy)
                                )
                            )
                        }

                        // AdMob Banner Ad
                        item {
                            BannerAdView()
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }
                }
            }
        }
    }

    // ============================================
    // DELETE CONFIRMATION DIALOG
    // ============================================
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            containerColor = Ink850,
            shape = RoundedCornerShape(22.dp),
            icon = {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(DangerRed.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.DeleteForever,
                        contentDescription = null,
                        tint = DangerRed,
                        modifier = Modifier.size(26.dp)
                    )
                }
            },
            title = {
                Text(
                    if (uiState.selectionMode) "Delete Selected?" else "Clear All History?",
                    color = HistoryTextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 19.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Text(
                    if (uiState.selectionMode) {
                        "${uiState.selectedIds.size} items will be permanently deleted."
                    } else {
                        "All history will be permanently deleted. This action cannot be undone."
                    },
                    color = HistoryTextSecondary,
                    fontSize = 13.5.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(DangerRed)
                        .clickable {
                            if (uiState.selectionMode) {
                                viewModel.deleteSelected()
                            } else {
                                viewModel.clearAll()
                            }
                            showDeleteConfirm = false
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Delete",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.5.sp
                    )
                }
            },
            dismissButton = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, BorderLine, RoundedCornerShape(12.dp))
                        .clickable { showDeleteConfirm = false },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Cancel",
                        color = HistoryTextSecondary,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.5.sp
                    )
                }
            }
        )
    }
}

// ============================================
// HEADER — Redesigned with count badge
// ============================================
@Composable
private fun HistoryHeader(
    searchQuery: String,
    showSearch: Boolean,
    onSearchToggle: () -> Unit,
    onSearchChange: (String) -> Unit,
    onBackClick: () -> Unit,
    onClearAll: () -> Unit,
    onPrivacyPolicyClick: () -> Unit,
    itemCount: Int,
    allItems: List<HistoryItem>
) {
    var showMenu by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Title & Count Badge
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(9.dp)
            ) {
                Text(
                    "History",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = HistoryTextPrimary
                )
                if (itemCount > 0) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(AmberDim)
                            .padding(horizontal = 9.dp, vertical = 2.dp)
                    ) {
                        Text(
                            "$itemCount",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = AmberSoft
                        )
                    }
                }
            }

            // Search toggle
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (showSearch) AmberDim2 else Ink800)
                    .border(1.dp, BorderLine, RoundedCornerShape(12.dp))
                    .clickable(onClick = onSearchToggle),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (showSearch) Icons.Default.Close else Icons.Default.Search,
                    contentDescription = "Search",
                    tint = if (showSearch) AmberSoft else HistoryTextPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // More menu
            Box {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Ink800)
                        .border(1.dp, BorderLine, RoundedCornerShape(12.dp))
                        .clickable { showMenu = true },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.MoreVert,
                        contentDescription = "More",
                        tint = HistoryTextPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    shape = RoundedCornerShape(20.dp),
                    containerColor = Ink850,
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderLineStrong),
                    shadowElevation = 12.dp,
                    modifier = Modifier.width(250.dp)
                ) {
                    // Export all history
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(
                                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                                indication = androidx.compose.material3.ripple(color = AmberDim2)
                            ) {
                                showMenu = false
                                exportAllHistory(context, allItems)
                            }
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Ink750),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.FileDownload,
                                contentDescription = null,
                                tint = AmberSoft,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Text(
                            "Export all history",
                            color = HistoryTextPrimary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    HorizontalDivider(color = BorderLine, thickness = 0.8.dp)

                    // Clear all history
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(
                                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                                indication = androidx.compose.material3.ripple(color = AmberDim2)
                            ) {
                                showMenu = false
                                onClearAll()
                            }
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Ink750),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.DeleteOutline,
                                contentDescription = null,
                                tint = DangerRed,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Text(
                            "Clear all history",
                            color = DangerRed,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    HorizontalDivider(color = BorderLine, thickness = 0.8.dp)

                    // About & Legal
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(
                                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                                indication = androidx.compose.material3.ripple(color = AmberDim2)
                            ) {
                                showMenu = false
                                onPrivacyPolicyClick()
                            }
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Ink750),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = null,
                                tint = AmberSoft,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Text(
                            "About & Legal",
                            color = HistoryTextPrimary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    HorizontalDivider(color = BorderLine, thickness = 0.8.dp)

                    // Premium Brand Footer
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp, bottom = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            // Gold accent divider line
                            Box(
                                modifier = Modifier
                                    .width(80.dp)
                                    .height(1.5.dp)
                                    .background(
                                        brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                                            colors = listOf(
                                                androidx.compose.ui.graphics.Color.Transparent,
                                                AmberDim2,
                                                AmberSoft,
                                                AmberDim2,
                                                androidx.compose.ui.graphics.Color.Transparent
                                            )
                                        ),
                                        shape = RoundedCornerShape(1.dp)
                                    )
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = androidx.compose.ui.text.buildAnnotatedString {
                                    withStyle(
                                        style = androidx.compose.ui.text.SpanStyle(
                                            color = HistoryTextMuted,
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 9.5.sp,
                                            fontWeight = FontWeight.Normal,
                                            letterSpacing = 2.sp
                                        )
                                    ) { append("BUILT BY ") }
                                    withStyle(
                                        style = androidx.compose.ui.text.SpanStyle(
                                            color = AmberSoft,
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 10.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 2.5.sp
                                        )
                                    ) { append("KRISHNA") }
                                },
                            )
                        }
                    }
                }
            }
        }

        // Search Bar — Animated
        AnimatedVisibility(
            visible = showSearch,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                placeholder = {
                    Text(
                        "Search history...",
                        color = HistoryTextMuted,
                        fontSize = 13.5.sp
                    )
                },
                leadingIcon = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = null,
                        tint = HistoryTextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchChange("") }) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Clear",
                                tint = HistoryTextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AmberPrimary,
                    unfocusedBorderColor = BorderLine,
                    cursorColor = AmberPrimary,
                    focusedContainerColor = Ink800,
                    unfocusedContainerColor = Ink800,
                    focusedTextColor = HistoryTextPrimary,
                    unfocusedTextColor = HistoryTextPrimary
                ),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.5.sp)
            )
        }
    }
}

// ============================================
// SEGMENTED TABS — Smooth Sliding Underline Indicator
// ============================================
@Composable
private fun SegmentedTabRow(
    selectedTab: Tab,
    onTabSelected: (Tab) -> Unit
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .drawBehind {
                drawLine(
                    color = BorderLine,
                    start = Offset(0f, size.height),
                    end = Offset(size.width, size.height),
                    strokeWidth = 1.dp.toPx()
                )
            }
            .padding(horizontal = 8.dp)
    ) {
        val totalWidth = maxWidth
        val tabCount = Tab.entries.size
        val tabWidth = totalWidth / tabCount
        val indicatorWidth = 36.dp

        // Animate underline indicator offset smoothly between tabs
        val targetOffset = (tabWidth * selectedTab.ordinal) + ((tabWidth - indicatorWidth) / 2)
        val animatedIndicatorOffset by animateDpAsState(
            targetValue = targetOffset,
            animationSpec = tween(
                durationMillis = 260,
                easing = FastOutSlowInEasing
            ),
            label = "TabIndicatorOffset"
        )

        // Tab Items Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Tab.entries.forEach { tab ->
                val isSelected = selectedTab == tab
                val label = when (tab) {
                    Tab.ALL -> "All"
                    Tab.SCANNED -> "Scanned"
                    Tab.GENERATED -> "Created"
                    Tab.FAVORITES -> "Favorites"
                }

                val animatedTextColor by animateColorAsState(
                    targetValue = if (isSelected) HistoryTextPrimary else HistoryTextSecondary,
                    animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
                    label = "TabTextColor"
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(
                            interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                            indication = null
                        ) { onTabSelected(tab) }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        fontSize = 13.5.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                        color = animatedTextColor,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // Active Underline Indicator — physically glides across tabs
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = animatedIndicatorOffset)
                .width(indicatorWidth)
                .height(2.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(AmberPrimary)
        )
    }
}

// ============================================
// CATEGORY FILTER CHIPS — Smooth Interactive Transition
// ============================================
@Composable
private fun CategoryFilterRow(
    selectedCategory: String?,
    onCategorySelected: (String?) -> Unit,
    availableCategories: List<String>
) {
    if (availableCategories.size <= 1) return

    val categories = listOf("All" to null) + availableCategories.map { it to it }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        categories.forEach { (label, value) ->
            val isSelected = selectedCategory == value ||
                    (value == null && selectedCategory == null)

            val chipBg by animateColorAsState(
                targetValue = if (isSelected) AmberDim else Ink800,
                animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing),
                label = "ChipBg"
            )
            val chipBorder by animateColorAsState(
                targetValue = if (isSelected) AmberDim2 else BorderLine,
                animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing),
                label = "ChipBorder"
            )
            val chipTextColor by animateColorAsState(
                targetValue = if (isSelected) HistoryTextPrimary else HistoryTextSecondary,
                animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing),
                label = "ChipTextColor"
            )

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(chipBg)
                    .border(
                        width = 1.dp,
                        color = chipBorder,
                        shape = RoundedCornerShape(20.dp)
                    )
                    .clickable(
                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                        indication = null
                    ) { onCategorySelected(value) }
                    .padding(horizontal = 13.dp, vertical = 6.dp)
            ) {
                Text(
                    text = label,
                    fontSize = 12.sp,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                    color = chipTextColor
                )
            }
        }
    }
}

// ============================================
// HISTORY CARD — Redesigned Ink & Amber with Micro-interactions
// ============================================
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun HistoryCard(
    item: HistoryItem,
    isSelected: Boolean,
    selectionMode: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isScanned = item.isScanned
    val icon = getTypeIcon(item.type)

    val iconBg = if (isScanned) CyanDim else AmberDim
    val iconColor = if (isScanned) CyanAccent else AmberSoft
    val statusBg = if (isScanned) CyanDim else AmberDim
    val statusColor = if (isScanned) CyanAccent else AmberSoft
    val statusLabel = if (isScanned) "SCANNED" else "CREATED"

    val cardBgColor by animateColorAsState(
        targetValue = if (isSelected) AmberDim.copy(alpha = 0.2f) else Ink800,
        animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
        label = "CardBgColor"
    )
    val cardBorderColor by animateColorAsState(
        targetValue = if (isSelected) AmberDim2 else BorderLine,
        animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
        label = "CardBorderColor"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBgColor),
        border = androidx.compose.foundation.BorderStroke(1.dp, cardBorderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(13.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Left Icon Wrap
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(18.dp)
                )
            }

            // Body
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = getDisplayTitle(item),
                    color = HistoryTextPrimary,
                    fontSize = 14.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                val subtitle = getSubtitle(item)
                if (subtitle.isNotEmpty()) {
                    Text(
                        text = subtitle,
                        color = HistoryTextMuted,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(3.dp))

                // Meta: Time + Status Pill
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Default.AccessTime,
                            contentDescription = null,
                            tint = HistoryTextSecondary,
                            modifier = Modifier.size(11.dp)
                        )
                        Text(
                            text = item.getFormattedDate(),
                            color = HistoryTextSecondary,
                            fontSize = 11.sp
                        )
                    }

                    // Status pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(statusBg)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = statusLabel,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = statusColor,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }

            // Right: Heart Favorite Toggle with micro-scale & color pop animation
            var favJustTapped by remember { mutableStateOf(false) }
            val heartScale by animateFloatAsState(
                targetValue = if (favJustTapped) 1.28f else 1.0f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                ),
                finishedListener = { favJustTapped = false },
                label = "HeartScale"
            )
            val heartColor by animateColorAsState(
                targetValue = if (item.isFavorite) DangerRed else HistoryTextMuted,
                animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
                label = "HeartColor"
            )

            IconButton(
                onClick = {
                    favJustTapped = true
                    onFavoriteClick()
                },
                modifier = Modifier
                    .size(32.dp)
                    .scale(heartScale)
            ) {
                Icon(
                    imageVector = if (item.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = heartColor,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

// ============================================
// SELECTION TOP BAR — Premium
// ============================================
@Composable
private fun SelectionTopBar(
    selectedCount: Int,
    totalCount: Int,
    onClearSelection: () -> Unit,
    onSelectAll: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Close button
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(HistoryCardBg)
                .clickable(onClick = onClearSelection),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Close,
                contentDescription = "Cancel",
                tint = HistoryTextPrimary,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        // Selection count
        Column(modifier = Modifier.weight(1f)) {
            Text(
                "$selectedCount selected",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = HistoryTextPrimary
            )
            Text(
                "of $totalCount items",
                fontSize = 12.sp,
                color = HistoryTextSecondary
            )
        }

        // Select All button
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(SelectionAccent.copy(alpha = 0.12f))
                .clickable(onClick = onSelectAll)
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Text(
                "Select All",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = SelectionAccent
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Delete button
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(ErrorRed.copy(alpha = 0.12f))
                .clickable(onClick = onDelete),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.DeleteOutline,
                contentDescription = "Delete",
                tint = ErrorRed,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// ============================================
// EMPTY STATE — Premium with animation
// ============================================
@Composable
private fun EmptyState(
    isSearching: Boolean,
    selectedTab: Tab
) {
    val icon: ImageVector
    val title: String
    val subtitle: String

    when {
        isSearching -> {
            icon = Icons.Default.SearchOff
            title = "No results found"
            subtitle = "Try a different search term"
        }
        selectedTab == Tab.FAVORITES -> {
            icon = Icons.Outlined.FavoriteBorder
            title = "No favorites yet"
            subtitle = "Tap the heart icon on any item\nto add it to your favorites"
        }
        selectedTab == Tab.GENERATED -> {
            icon = Icons.Default.QrCode
            title = "No created QR codes"
            subtitle = "Go to Generate tab to create\nyour first QR code"
        }
        selectedTab == Tab.SCANNED -> {
            icon = Icons.Default.QrCodeScanner
            title = "No scanned QR codes"
            subtitle = "Start scanning to see\nyour history here"
        }
        else -> {
            icon = Icons.Outlined.History
            title = "No history yet"
            subtitle = "Scan or create QR codes\nto see them here"
        }
    }

    // Floating animation
    val infiniteTransition = rememberInfiniteTransition(label = "empty")
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float"
    )

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(48.dp)
        ) {
            // Animated icon container
            Box(
                modifier = Modifier
                    .offset(y = floatOffset.dp)
                    .size(88.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(
                                AmberDim,
                                Ink800
                            )
                        )
                    )
                    .border(
                        1.dp,
                        BorderLine,
                        RoundedCornerShape(24.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = HistoryTextMuted,
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = title,
                color = HistoryTextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = subtitle,
                color = HistoryTextMuted,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )
        }
    }
}

// ============================================
// HELPER FUNCTIONS
// ============================================
private fun getAvailableCategories(items: List<HistoryItem>): List<String> {
    return items.map { it.category }.distinct().sorted()
}

private fun getDisplayTitle(item: HistoryItem): String {
    return when {
        item.title.isNotEmpty() -> item.title
        item.type == "TEXT" || item.type == "URL" -> {
            val text = item.rawValue
            if (text.length > 40) text.take(40) + "..." else text
        }
        else -> "${item.type} QR Code"
    }.toString()
}

private fun getSubtitle(item: HistoryItem): String {
    // Show a relevant preview of the raw value
    val raw = item.rawValue
    return when (item.type.uppercase()) {
        "URL", "QRURL" -> {
            raw.removePrefix("https://").removePrefix("http://").take(50)
        }
        "UPI" -> {
            val pa = Regex("pa=([^&]*)").find(raw)?.groupValues?.getOrNull(1) ?: ""
            if (pa.isNotEmpty()) pa else raw.take(50)
        }
        "PHONE" -> raw.removePrefix("tel:").take(20)
        "SMS" -> raw.removePrefix("smsto:").removePrefix("sms:").substringBefore("?").take(20)
        "EMAIL", "QREMAIL" -> raw.removePrefix("mailto:").substringBefore("?").take(40)
        "WIFI" -> {
            val ssid = Regex("S:([^;]*)").find(raw)?.groupValues?.getOrNull(1) ?: ""
            if (ssid.isNotEmpty()) "Network: $ssid" else raw.take(40)
        }
        "WHATSAPP" -> {
            if (raw.contains("wa.me/")) raw.substringAfter("wa.me/").take(20)
            else raw.take(40)
        }
        "CONTACT", "VCARD" -> {
            val fn = Regex("FN:([^\n]*)").find(raw)?.groupValues?.getOrNull(1) ?: ""
            if (fn.isNotEmpty()) fn.take(40) else raw.take(40)
        }
        "TEXT" -> "" // Title already shows the text
        else -> {
            if (raw.length > 50) raw.take(50) + "..." else raw
        }
    }
}

private fun getTypeIcon(type: String) = when (type.uppercase()) {
    "URL", "QRURL" -> Icons.Default.Link
    "PHONE" -> Icons.Default.Phone
    "SMS" -> Icons.Default.Sms
    "EMAIL", "QREMAIL" -> Icons.Default.Email
    "WIFI" -> Icons.Default.Wifi
    "WHATSAPP" -> Icons.AutoMirrored.Filled.Chat
    "TEXT" -> Icons.Default.TextFields
    "LOCATION", "PLUS_CODE", "GOOGLE_MAPS" -> Icons.Default.LocationOn
    "UPI" -> Icons.Default.Payment
    "CONTACT", "VCARD" -> Icons.Default.Person
    "EVENT", "VEVENT" -> Icons.Default.Event
    else -> Icons.Default.QrCode
}

private fun getTypeColor(type: String): Color = when (type.uppercase()) {
    "URL", "QRURL" -> Color(0xFF4E9EFF)
    "PHONE" -> Color(0xFF4CAF50)
    "SMS" -> Color(0xFF66BB6A)
    "EMAIL", "QREMAIL" -> Color(0xFF42A5F5)
    "WIFI" -> Color(0xFFAB47BC)
    "WHATSAPP" -> Color(0xFF25D366)
    "TEXT" -> Color(0xFFFFA726)
    "LOCATION", "PLUS_CODE", "GOOGLE_MAPS" -> Color(0xFFEF5350)
    "UPI" -> Color(0xFF7C4DFF)
    "CONTACT", "VCARD" -> Color(0xFF5C6BC0)
    "EVENT", "VEVENT" -> Color(0xFFF9A825)
    else -> Color(0xFF26C6DA)
}

// ============================================
// EXPORT ALL HISTORY — Share as text file
// ============================================
private fun exportAllHistory(context: android.content.Context, items: List<HistoryItem>) {
    if (items.isEmpty()) {
        Toast.makeText(context, "No items to export", Toast.LENGTH_SHORT).show()
        return
    }
    
    val content = buildString {
        appendLine("QR Hub Export")
        appendLine("============")
        appendLine()
        items.forEachIndexed { index, item ->
            appendLine("${index + 1}. [${item.type}] ${item.getFormattedDate()}")
            appendLine("Content: ${item.rawValue.take(200)}")
            appendLine("---")
        }
    }
    
    try {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, content)
            putExtra(Intent.EXTRA_SUBJECT, "QR Hub History Export")
        }
        context.startActivity(Intent.createChooser(intent, "Export History"))
    } catch (e: Exception) {
        Toast.makeText(context, "Export failed", Toast.LENGTH_SHORT).show()
    }
}
