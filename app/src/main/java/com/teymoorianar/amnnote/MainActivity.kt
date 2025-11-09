package com.teymoorianar.amnnote

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.BottomAppBar
import androidx.compose.material.FabPosition
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.rounded.Create
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.teymoorianar.amnnote.ui.note.NoteActivity
import com.teymoorianar.amnnote.ui.theme.AmnNoteTheme
import androidx.compose.material3.MaterialTheme as Material3Theme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AmnNoteTheme {
                Screen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Screen() {
    val backgroundColor = Material3Theme.colorScheme.background

    Scaffold(
        contentWindowInsets = WindowInsets.systemBars,
        bottomBar = { CurvedBottomBar() },
        floatingActionButton = { CenterFab() },
        floatingActionButtonPosition = FabPosition.Center,
        isFloatingActionButtonDocked = true,
        backgroundColor = backgroundColor,
        topBar = { MainTopAppBar() }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "Main content")
        }
    }
}

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
            IconButton(onClick = { /* TODO: handle navigation click */ }) {
                Icon(
                    imageVector = Icons.Filled.Menu,
                    contentDescription = "Menu"
                )
            }
        },
        backgroundColor = Material3Theme.colorScheme.primary,
        contentColor = Material3Theme.colorScheme.onPrimary,
        elevation = 4.dp
    )
}

@Composable
fun CurvedBottomBar() {
    BottomAppBar(
        modifier = Modifier.navigationBarsPadding(),
        cutoutShape = CircleShape,
        backgroundColor = Material3Theme.colorScheme.primary,
        contentColor = Color.White,
        elevation = 28.dp
    ) {
        IconButton(onClick = { /* TODO: handle Home click */ }) {
            Icon(
                imageVector = Icons.Filled.Home,
                contentDescription = "Home"
            )
        }

        // Spacer to keep the center area free for the FAB
        Spacer(modifier = Modifier.weight(0.8f))

        IconButton(onClick = { /* TODO: handle Settings click */ }) {
            Icon(
                imageVector = Icons.Filled.Settings,
                contentDescription = "Settings"
            )
        }
    }
}

@Composable
fun CenterFab() {
    var expanded by remember { mutableStateOf(false) }
    val context = LocalContext.current
    FloatingActionButton(
        onClick = {
            context.startActivity(NoteActivity.newIntent(context))
        },
        shape = CircleShape,
        backgroundColor = Material3Theme.colorScheme.primary,
        contentColor = Color.White,
    ) {
        Icon(
            imageVector = Icons.Rounded.Create,
            contentDescription = "Add",
            modifier = Modifier.size(30.dp, 30.dp),
        )

    }
}

@Preview
@Composable
fun preview() {
    Screen()
}