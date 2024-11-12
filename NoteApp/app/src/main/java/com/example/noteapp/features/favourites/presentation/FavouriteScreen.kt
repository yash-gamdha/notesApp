package com.example.noteapp.features.favourites.presentation

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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.noteapp.features.core.presentation.EmptyScreenList
import com.example.noteapp.features.core.presentation.MainViewModel
import com.example.noteapp.features.core.presentation.undoDeleted
import com.example.noteapp.features.core.ui.theme.ubuntuFontFamily
import com.example.noteapp.features.notes.presentation.components.LoadingAndErrorScreen
import com.example.noteapp.features.notes.presentation.components.NoteList
import com.example.noteapp.util.Response

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavouriteScreen(
    onShowNoteClickListener:(Int) -> Unit,
    onEditClickListener: (Int) -> Unit,
    navController: NavController,
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
            TopAppBar(
                // back arrow
                navigationIcon = {
                    IconButton(
                        onClick = { navController.popBackStack() }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Notes App"
                        )
                    }
                },
                title = {
                    Text(
                        text = "Favourite",
                        fontFamily = ubuntuFontFamily,
                    )
                },
                scrollBehavior = scrollBehavior
            )
        }
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
            when (result) { // displaying loading msg
                is Response.Loading -> {
                    LoadingAndErrorScreen(label = "Loading...")
                }

                is Response.Success -> {
                    val favourites = result.data.filter { it.isBookmarked } // fetching favourite notes
                    if (favourites.isEmpty()) { // displaying empty message
                        EmptyScreenList()
                    } else { // displaying notes
                        NoteList(
                            notes = favourites,
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