package com.example.noteapp.features.notes.presentation

import android.app.Activity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BookmarkAdded
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.outlined.BookmarkAdd
import androidx.compose.material.icons.outlined.DeleteSweep
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.noteapp.features.core.presentation.MainViewModel
import com.example.noteapp.features.core.presentation.undoDeleted
import com.example.noteapp.features.core.ui.theme.poppinsFontFamily
import com.example.noteapp.features.core.ui.theme.ubuntuFontFamily
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShowNoteScreen(
    noteId: Int,
    navController: NavController,
    onEditClickListener: (Int) -> Unit,
    viewModel: MainViewModel = hiltViewModel()
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    val textStyle = TextStyle(
        fontSize = 18.sp,
        fontFamily = poppinsFontFamily
    )

    LaunchedEffect(key1 = true) {
        if (noteId > 0) {
            viewModel.getNoteById(noteId)
        }
    }

    val scrollState = rememberScrollState()
    val view = LocalView.current
    val window = (view.context as Activity).window

    val surface = MaterialTheme.colorScheme.surface.toArgb()
    val scrolledColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp).toArgb()

    LaunchedEffect(scrollState.value) {
        if (scrollState.canScrollBackward) {
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
            // top bar
            MediumTopAppBar(
                // app icon
                navigationIcon = {
                    IconButton(
                        onClick = { navController.popBackStack() }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                // app name
                title = {
                    Text(
                        text = viewModel.note.title,
                        fontFamily = ubuntuFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1
                    )
                },
                actions = {
                    // favourites button
                    IconToggleButton(
                        checked = viewModel.note.isBookmarked,
                        onCheckedChange = {
                            viewModel.updateNoteBookMarked(it)
                            viewModel.updateNote(
                                viewModel.note.copy(
                                    isBookmarked = it
                                )
                            )
                        }
                    ) {
                        Icon(
                            imageVector = if (viewModel.note.isBookmarked) Icons.Filled.BookmarkAdded
                            else Icons.Outlined.BookmarkAdd,
                            contentDescription = if (viewModel.note.isBookmarked) "Added to favourite"
                            else "Add to favourite"
                        )
                    }
                    // delete icon
                    IconButton(
                        onClick = {
                            viewModel.deleteNote(viewModel.note)
                            undoDeleted(
                                scope = scope,
                                viewModel = viewModel,
                                snackbarHostState = snackbarHostState
                            )
                            scope.launch {
                                delay(5000)
                                navController.popBackStack()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.DeleteSweep,
                            contentDescription = "Delete Note"
                        )
                    }

                    // edit icon
                    IconButton(
                        onClick = { onEditClickListener(viewModel.note.id) }
                    ) {
                        Icon(
                            imageVector = Icons.Default.EditNote,
                            contentDescription = "Edit Note"
                        )
                    }
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
                    .verticalScroll(scrollState)
            ) {
                Text(
                    text = viewModel.note.description ?: "",
                    style = textStyle
                )
            }
        }
    }
}