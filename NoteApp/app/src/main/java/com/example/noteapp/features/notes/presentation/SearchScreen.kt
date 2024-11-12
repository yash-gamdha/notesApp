package com.example.noteapp.features.notes.presentation

import android.app.Activity
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.noteapp.features.core.presentation.MainViewModel
import com.example.noteapp.features.core.presentation.toastMsg
import com.example.noteapp.features.core.presentation.undoDeleted
import com.example.noteapp.features.core.ui.theme.ubuntuFontFamily
import com.example.noteapp.features.notes.presentation.components.LoadingAndErrorScreen
import com.example.noteapp.features.notes.presentation.components.NoteList
import com.example.noteapp.util.Response

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onShowNoteClickListener: (Int) -> Unit,
    onEditClickListener: (Int) -> Unit,
    navController: NavController,
    viewModel: MainViewModel = hiltViewModel()
) {
    var search by remember {
        mutableStateOf("")
    }

    var showClearButton by remember {
        mutableFloatStateOf(0f)
    }

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    val context = LocalContext.current

    val searchResponse by viewModel.searchResponse.collectAsStateWithLifecycle()

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

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
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(
                        onClick = { navController.popBackStack() }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.ArrowBack,
                            contentDescription = "back"
                        )
                    }
                },
                title = {
                    Text(
                        text = "Search Page",
                        fontFamily = ubuntuFontFamily
                    )
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(4.dp)
        ) {
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = search,
                onValueChange = {
                    search = it
                    showClearButton = 1f
                },
                placeholder = {
                    Text("Search")
                },
                trailingIcon = {
                    IconButton(
                        modifier = Modifier
                            .alpha(showClearButton),
                        onClick = {
                            search = ""
                            showClearButton = 0f
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "clear"
                        )
                    }
                },
                shape = RoundedCornerShape(4.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        if (search.isBlank()) {
                            toastMsg(
                                context = context,
                                message = "Search field can't be empty"
                            )
                        } else {
                            viewModel.search("%${search}%")
                        }
                    }
                )
            )
            Spacer(modifier = Modifier.height(5.dp))
            AnimatedContent(
                modifier = Modifier
                    .fillMaxSize(),
                targetState = searchResponse,
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
                    is Response.Loading -> {
                        LoadingAndErrorScreen("Search Notes")
                    }

                    is Response.Success -> {
                        val notes = result.data
                        if (notes.isEmpty()) {
                            LoadingAndErrorScreen("No notes found with the given title")
                        } else {
                            NoteList(
                                notes = notes,
                                onEditClickListener = onEditClickListener,
                                onShowNoteClickListener = onShowNoteClickListener,
                                onUndoClickListener = {
                                    undoDeleted(
                                        scope = scope,
                                        viewModel = viewModel,
                                        snackbarHostState = snackbarHostState
                                    )
                                },
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
}