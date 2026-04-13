package com.aadi.aurajournal.utils

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.*

/**
 * Use this in screens that have a LazyColumn (TimelineScreen, CalendarScreen).
 * Returns a LazyListState to pass to your LazyColumn, and a Boolean for whether
 * the bottom bar should be visible. Call onShowBottomBar whenever the Boolean changes.
 */
@Composable
fun rememberLazyBottomBarState(
    onShowBottomBar: (Boolean) -> Unit
): LazyListState {
    val listState = rememberLazyListState()

    var lastIndex by remember { mutableStateOf(0) }
    var lastOffset by remember { mutableStateOf(0) }

    LaunchedEffect(listState.firstVisibleItemIndex, listState.firstVisibleItemScrollOffset) {
        val index = listState.firstVisibleItemIndex
        val offset = listState.firstVisibleItemScrollOffset

        val show = when {
            index < lastIndex -> true       // scrolled up to a previous item
            index > lastIndex -> false      // scrolled down to a new item
            offset < lastOffset -> true     // scrolled up within same item
            offset > lastOffset -> false    // scrolled down within same item
            else -> return@LaunchedEffect   // no change — don't emit anything
        }

        lastIndex = index
        lastOffset = offset
        onShowBottomBar(show)
    }

    return listState
}

/**
 * Use this in screens that have a Column + verticalScroll (InsightsScreen, ProfileScreen).
 * Returns a ScrollState to pass to your verticalScroll modifier, and drives onShowBottomBar.
 */
@Composable
fun rememberScrollBottomBarState(
    onShowBottomBar: (Boolean) -> Unit
): ScrollState {
    val scrollState = rememberScrollState()
    var lastScrollValue by remember { mutableStateOf(0) }

    LaunchedEffect(scrollState.value) {
        val current = scrollState.value
        val show = current <= lastScrollValue  // scrolling up = show, down = hide
        lastScrollValue = current
        onShowBottomBar(show)
    }

    return scrollState
}