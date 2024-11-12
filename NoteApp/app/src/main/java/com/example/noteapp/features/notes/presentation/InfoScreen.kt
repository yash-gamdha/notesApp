package com.example.noteapp.features.notes.presentation

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.noteapp.app_info.AppInfo
import com.example.noteapp.features.core.ui.theme.poppinsFontFamily
import com.example.noteapp.features.core.ui.theme.ubuntuFontFamily

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InfoScreen(
    navController: NavController
) {
    val context = LocalContext.current
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    val scrollState = rememberScrollState()
    val view = LocalView.current
    val window = (view.context as Activity).window

    val surface = MaterialTheme.colorScheme.surface.toArgb()
    val scrolledColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp).toArgb()

    LaunchedEffect(scrollState.value) {
        if (scrollState.value > 0) {
            window.statusBarColor = scrolledColor
        } else {
            window.statusBarColor = surface
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
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
                        text = "About",
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
                .padding(8.dp)
                .verticalScroll(scrollState)
        ) {
            Text(
                text = "Features",
                fontFamily = poppinsFontFamily,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 16.dp, bottom = 16.dp)
            )

            AppInfo.features.forEach {  feature ->
                Text(
                    text = "● $feature",
                    fontFamily = ubuntuFontFamily,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            HorizontalDivider(
                modifier = Modifier
                    .padding(8.dp),
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.secondary
            )

            Text(
                text = "Developer",
                fontFamily = poppinsFontFamily,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 16.dp, bottom = 16.dp)
            )

            Text(
                text = buildAnnotatedString {
                    withStyle(
                        style = SpanStyle(
                            fontWeight = FontWeight.SemiBold
                        )
                    ) {
                        append("Created by : ")
                    }
                    append("Yash Gamdha")
                },
                fontFamily = poppinsFontFamily,
                fontSize = 18.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            HorizontalDivider(
                modifier = Modifier
                    .padding(8.dp),
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.secondary
            )

            Text(
                text = "Want to see more apps created by me?",
                fontFamily = poppinsFontFamily,
                fontSize = 16.sp,
                fontWeight = FontWeight.Light,
                modifier = Modifier.padding(top = 16.dp, bottom = 16.dp)
            )

            TextButton (
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/yash-gamdha"))
                    context.startActivity(intent)
                },
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Text(
                    text = "GitHub",
                    fontSize = 15.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}