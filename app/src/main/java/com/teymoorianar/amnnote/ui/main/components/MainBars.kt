package com.teymoorianar.amnnote.ui.main.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.BottomAppBar
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.rounded.Create
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.teymoorianar.amnnote.R
import androidx.compose.material3.MaterialTheme as Material3Theme

/**
 * Renders the branded top bar with a navigation icon.
 */
@Composable
fun MainTopAppBar() {
    TopAppBar(
        modifier = Modifier.statusBarsPadding(),
        title = {
            Text(
                text = stringResource(R.string.app_name),
                fontWeight = FontWeight.SemiBold
            )
        },
        navigationIcon = {
            IconButton(onClick = { /* TODO: hook navigation drawer */ }) {
                Icon(
                    imageVector = Icons.Filled.Menu,
                    contentDescription = stringResource(R.string.menu_button_description)
                )
            }
        },
        backgroundColor = Material3Theme.colorScheme.primary,
        contentColor = Material3Theme.colorScheme.onPrimary,
        elevation = 4.dp
    )
}

/**
 * Displays a curved bottom bar with quick navigation actions.
 */
@Composable
fun CurvedBottomBar(
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    BottomAppBar(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        cutoutShape = CircleShape,
        backgroundColor = Material3Theme.colorScheme.primary,
        contentColor = Color.White,
        elevation = 28.dp
    ) {
        IconButton(onClick = { /* TODO: handle Home click */ }) {
            Icon(
                imageVector = Icons.Filled.Home,
                contentDescription = stringResource(R.string.home_button_description)
            )
        }

        Spacer(modifier = Modifier.weight(0.8f))

        IconButton(onClick = onSettingsClick) {
            Icon(
                imageVector = Icons.Filled.Settings,
                contentDescription = stringResource(R.string.settings_title)
            )
        }
    }
}

/**
 * Floating action button that opens the note editor.
 */
@Composable
fun CenterFab(onAddNote: () -> Unit) {
    FloatingActionButton(
        onClick = onAddNote,
        shape = CircleShape,
        backgroundColor = Material3Theme.colorScheme.primary,
        contentColor = Color.White
    ) {
        Icon(
            imageVector = Icons.Rounded.Create,
            contentDescription = stringResource(R.string.add_note)
        )
    }
}
