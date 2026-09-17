package com.example.cpen321application

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import com.example.cpen321application.network.NetworkClient
import com.example.cpen321application.network.PixelWebSocketClient
import com.example.cpen321application.repository.ApiRepository
import com.example.cpen321application.ui.screens.Button1ViewModel
import com.example.cpen321application.ui.screens.Button2ViewModel
import com.example.cpen321application.ui.screens.Button3ViewModel
import com.example.cpen321application.ui.screens.MainHomeScreen
import com.example.cpen321application.ui.screens.PixelArtScreen
import com.example.cpen321application.ui.screens.ServerInfoScreen
import com.example.cpen321application.ui.screens.TimerTriviaScreen
import com.example.cpen321application.ui.theme.CPEN321ApplicationTheme

enum class AppScreen {
    HOME,
    SERVER_INFO,
    PIXEL_ART,
    TIMER_TRIVIA
}

class MainActivity : ComponentActivity() {
    private lateinit var button1ViewModel: Button1ViewModel
    private lateinit var button2ViewModel: Button2ViewModel
    private lateinit var button3ViewModel: Button3ViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val baseUrl = BuildConfig.API_BASE_URL
        val apiService = NetworkClient.create(baseUrl)
        val repository = ApiRepository(apiService)

        val factory1 = Button1ViewModelFactory(repository)
        button1ViewModel = ViewModelProvider(this, factory1)[Button1ViewModel::class.java]

        val webSocketClient = PixelWebSocketClient(baseUrl)
        val factory2 = Button2ViewModelFactory(webSocketClient)
        button2ViewModel = ViewModelProvider(this, factory2)[Button2ViewModel::class.java]

        val factory3 = Button3ViewModelFactory(repository)
        button3ViewModel = ViewModelProvider(this, factory3)[Button3ViewModel::class.java]

        setContent {
            CPEN321ApplicationTheme {
                var currentScreen by remember { mutableStateOf(AppScreen.HOME) }
                val button1State by button1ViewModel.state.collectAsState()
                val button2State by button2ViewModel.state.collectAsState()
                val button3State by button3ViewModel.state.collectAsState()

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    when (currentScreen) {
                        AppScreen.HOME -> {
                            MainHomeScreen(
                                button1State = button1State,
                                onLoginSuccess = { userName, userEmail, givenName, familyName, idToken ->
                                    button1ViewModel.setLoggedIn(userName, userEmail, givenName, familyName, idToken)
                                    currentScreen = AppScreen.SERVER_INFO
                                },
                                onNavigateToServerInfo = { currentScreen = AppScreen.SERVER_INFO },
                                onNavigateToPixelArt = { currentScreen = AppScreen.PIXEL_ART },
                                onNavigateToTimerTrivia = { currentScreen = AppScreen.TIMER_TRIVIA },
                                onLogout = { button1ViewModel.logout() },
                                modifier = Modifier.padding(innerPadding)
                            )
                        }

                        AppScreen.SERVER_INFO -> {
                            ServerInfoScreen(
                                state = button1State,
                                onNavigateHome = { currentScreen = AppScreen.HOME },
                                onLogout = {
                                    button1ViewModel.logout()
                                    currentScreen = AppScreen.HOME
                                },
                                modifier = Modifier.padding(innerPadding)
                            )
                        }

                        AppScreen.PIXEL_ART -> {
                            PixelArtScreen(
                                state = button2State,
                                onNavigateHome = { currentScreen = AppScreen.HOME },
                                onScreenOpened = { button2ViewModel.onScreenOpened() },
                                onScreenClosed = { button2ViewModel.onScreenClosed() },
                                onRetryConnection = { button2ViewModel.retryConnection() },
                                onClearCanvas = { button2ViewModel.clearCanvas() },
                                modifier = Modifier.padding(innerPadding)
                            )
                        }

                        AppScreen.TIMER_TRIVIA -> {
                            TimerTriviaScreen(
                                state = button3State,
                                onNavigateHome = { currentScreen = AppScreen.HOME },
                                onMinutesChanged = { button3ViewModel.setMinutesInput(it) },
                                onSecondsChanged = { button3ViewModel.setSecondsInput(it) },
                                onStartTimer = { button3ViewModel.startTimer() },
                                onPauseTimer = { button3ViewModel.pauseTimer() },
                                onResetTimer = { button3ViewModel.resetTimer() },
                                onSelectAnswer = { button3ViewModel.selectAnswer(it) },
                                onNextQuestion = { button3ViewModel.fetchTriviaQuestion() },
                                modifier = Modifier.padding(innerPadding)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Factory for creating Button1ViewModel with dependency injection
 */
class Button1ViewModelFactory(private val repository: ApiRepository) :
    ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        return Button1ViewModel(repository) as T
    }
}

/**
 * Factory for creating Button2ViewModel with dependency injection
 */
class Button2ViewModelFactory(private val webSocketClient: PixelWebSocketClient) :
    ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        return Button2ViewModel(webSocketClient) as T
    }
}

/**
 * Factory for creating Button3ViewModel with dependency injection
 */
class Button3ViewModelFactory(private val repository: ApiRepository) :
    ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        return Button3ViewModel(repository) as T
    }
}
