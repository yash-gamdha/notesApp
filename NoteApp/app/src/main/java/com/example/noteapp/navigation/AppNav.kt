package com.example.noteapp.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.noteapp.features.core.ui.theme.ubuntuFontFamily
import com.example.noteapp.navigation.nav_graph.about
import com.example.noteapp.navigation.nav_graph.favourites
import com.example.noteapp.navigation.nav_graph.notes

@Composable
fun AppNav() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()

    val isBottomNavVisible = rememberSaveable(navBackStackEntry) {
        navBackStackEntry?.destination?.route == Screen.NoteScreen.route ||
                navBackStackEntry?.destination?.route == Screen.FavouriteScreen.route ||
                navBackStackEntry?.destination?.route == Screen.InfoScreen.route
    }

    Scaffold(
        bottomBar = {
            if (isBottomNavVisible) {
                BottomNavBar(navController)
            }
        }
    ) { innerPadding ->
        NavHost(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            navController = navController,
            startDestination = Tab.Notes.route
        ) {
            notes(navController = navController)
            favourites(navController = navController)
            about(navController = navController)
        }
    }
}

@Composable
fun BottomNavBar(navController: NavController) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)
    ) {
        val navBackStackEntry by
                navController.currentBackStackEntryAsState()

        val currentDestination = navBackStackEntry?.destination

        navList.forEach { tab ->
            val selected = currentDestination?.hierarchy?.any { it.route == tab.route } == true

            NavigationBarItem(
                selected = selected,
                onClick = {
                    navController.navigate(
                        route = tab.route
                    ) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = {
                    Icon(
                        imageVector = if (selected) tab.selectedIcon else tab.icon,
                        contentDescription = null
                    )
                },
                label = {
                    Text(
                        text = tab.label,
                        fontFamily = ubuntuFontFamily,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontWeight = if (selected) FontWeight.Bold else null
                    )
                }
            )
        }
    }
}