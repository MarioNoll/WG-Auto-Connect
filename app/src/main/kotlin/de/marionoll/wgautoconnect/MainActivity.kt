package de.marionoll.wgautoconnect

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.hilt.android.AndroidEntryPoint
import de.marionoll.wgautoconnect.home.HomeScreen
import de.marionoll.wgautoconnect.home.HomeViewModel
import de.marionoll.wgautoconnect.home.HomeViewState
import de.marionoll.wgautoconnect.theme.AppTheme
import kotlinx.coroutines.Dispatchers

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val homeViewModel: HomeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
            navigationBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT),
        )
        super.onCreate(savedInstanceState)

        setContent {
            val homeViewState by remember { homeViewModel.viewState() }
                .collectAsStateWithLifecycle(
                    initialValue = HomeViewState.Loading,
                    context = Dispatchers.Main.immediate,
                )

            AppTheme {
                Scaffold { contentPadding ->
                    val modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                0.2f to MaterialTheme.colorScheme.secondaryContainer,
                                1f to MaterialTheme.colorScheme.background
                            )
                        )
                        .padding(contentPadding)


                    when (val viewState = homeViewState) {
                        is HomeViewState.Content -> {
                            HomeScreen(
                                modifier = modifier,
                                viewState = viewState,
                                onEvent = homeViewModel::onEvent,
                            )
                        }

                        HomeViewState.Loading -> {
                            Box(
                                modifier = modifier,
                                contentAlignment = Alignment.Center,
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                    }
                }
            }
        }
    }
}