package com.example.noteapp.features.notes.presentation

import android.app.Activity
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.StickyNote2
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.noteapp.features.core.presentation.EmptyScreenList
import com.example.noteapp.features.core.presentation.MainViewModel
import com.example.noteapp.features.core.presentation.undoDeleted
import com.example.noteapp.features.core.ui.theme.ubuntuFontFamily
import com.example.noteapp.features.notes.presentation.components.LoadingAndErrorScreen
import com.example.noteapp.features.notes.presentation.components.NoteList
import com.example.noteapp.util.Response

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteScreen(
    onShowNoteClickListener: (Int) -> Unit,
    onEditClickListener: (Int) -> Unit,
    onAddClickListener: (Int) -> Unit,
    onSearchClickListener: () -> Unit,
    viewModel: MainViewModel = hiltViewModel()
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    val response by viewModel.response.collectAsStateWithLifecycle()

    val gridScrollState = rememberLazyStaggeredGridState()
    val view = LocalView.current
    val window = (view.context as Activity).window

    val surface = MaterialTheme.colorScheme.surface.toArgb()
    val scrolledColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp).toArgb()

    LaunchedEffect(gridScrollState.canScrollBackward) {
        if (gridScrollState.canScrollBackward) {
            window.statusBarColor = scrolledColor
        } else {
            window.statusBarColor = surface
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        // top app bar
        topBar = {
            CenterAlignedTopAppBar(
                // app icon
                navigationIcon = {
                    IconButton(
                        onClick = {},
                        enabled = false,
                        colors = IconButtonColors(
                            containerColor = Color.Unspecified,
                            contentColor = Color.Unspecified,
                            disabledContentColor = MaterialTheme.colorScheme.onSurface,
                            disabledContainerColor = Color.Unspecified
                        )
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.StickyNote2,
                            contentDescription = "Notes App"
                        )
                    }
                },
                // app name
                title = {
                    Text(
                        text = "Notes",
                        fontFamily = ubuntuFontFamily,
                    )
                },
                // search button
                actions = {
                    IconButton(
                        onClick = { onSearchClickListener() } // navigating to search screen
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "search"
                        )
                    }
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.topAppBarColors().copy(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        // add new note floating icon
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onAddClickListener(-1) } // navigating to add screen
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Note"
                )
            }
        },
    ) { innerPadding ->
        // screen animation
        AnimatedContent(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            targetState = response,
            label = "Animated content",
            transitionSpec = {
                fadeIn(
                    animationSpec = tween(
                        durationMillis = 300,
                        easing = LinearEasing
                    )
                ) togetherWith fadeOut(
                    animationSpec = tween(
                        durationMillis = 300,
                        easing = LinearEasing
                    )
                )
            }
        ) { result ->
            when (result) {
                is Response.Loading -> { // displaying loading msg
                    LoadingAndErrorScreen(label = "Loading...")
                }

                is Response.Success -> {
                    val notes = result.data // fetching notes
                    if (notes.isEmpty()) { // displaying empty screen list
                        EmptyScreenList()
                    } else { // displaying notes
                        NoteList(
                            notes = notes,
                            onEditClickListener = onEditClickListener,
                            onUndoClickListener = { // showing snack bar to undo deleted note
                                undoDeleted(
                                    scope = scope,
                                    viewModel = viewModel,
                                    snackbarHostState = snackbarHostState
                                )
                            },
                            onShowNoteClickListener = onShowNoteClickListener,
                            state = gridScrollState
                        )
                    }
                }

                is Response.Error -> { // displaying error msg
                    val error = result.error.message ?: "Something went wrong"
                    LoadingAndErrorScreen(label = error)
                }

                else -> Unit
            }
        }
    }
}