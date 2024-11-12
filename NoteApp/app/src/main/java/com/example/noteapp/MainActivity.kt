package com.example.noteapp

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.noteapp.features.core.ui.theme.NoteAppYTTheme
import com.example.noteapp.navigation.AppNav
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NoteAppYTTheme {
                AppNav()
            }
        }
    }
}