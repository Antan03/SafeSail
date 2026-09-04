package no.uio.ifi.in2000.sofiaalo.team44.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Icecream
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController

@Composable
fun MenuBar(
    navController: NavHostController,
    onNavigateToFavorites: () -> Unit
){
    NavigationBar(
        modifier = Modifier
            .fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,    ) {
        NavigationBarItem(
            selected = true,
            onClick = {navController.navigate("home")},
            icon = {
                Icon(Icons.Default.Home, contentDescription = "Home")
            },
            label = {Text("Hjem")}
        )

        NavigationBarItem(
            selected = true,
            onClick = onNavigateToFavorites,
            icon = {
                Icon(Icons.Default.Icecream, contentDescription = "Lagrede simuleringer")
            },
            label = {Text("Simuleringer")}
        )

        NavigationBarItem(
            selected = true,
            onClick = {navController.navigate("Instillinger")},
            icon = {
                Icon(Icons.Default.Settings, contentDescription = "Settings")
            },
            label = {Text("Instillinger")}
        )
    }
}