package com.example.projectjarvis

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.projectjarvis.networking.RetrofitClient
import com.example.projectjarvis.presentation.JarvisScreen
import com.example.projectjarvis.repository.*
import com.example.projectjarvis.ui.theme.ProjectJarvisTheme
import com.example.projectjarvis.utils.SpotifyHelper
import com.example.projectjarvis.viewmodel.JarvisViewModel
import com.example.projectjarvis.viewmodel.ViewModelFactory
import com.spotify.sdk.android.auth.AuthorizationClient
import com.spotify.sdk.android.auth.AuthorizationRequest
import com.spotify.sdk.android.auth.AuthorizationResponse

class MainActivity : ComponentActivity() {

    private val brainRepo = BrainRepository(RetrofitClient.brainApiService(BuildConfig.brain_apikey))
    private val generalRepo = GeneralRepository(RetrofitClient.queryApiService(BuildConfig.general_query_apikey))
    private val realtimeRepo = RealtimeRepository(RetrofitClient.queryApiService(BuildConfig.realtime_query_apikey))
    private val automationRepo = AutomationRepository(this)
    private val spotifyHelper by lazy {SpotifyHelper(this)}

    private val authLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val response = AuthorizationClient.getResponse(result.resultCode, result.data)
        when (response.type) {
            AuthorizationResponse.Type.TOKEN -> {
                response.accessToken?.let { token ->
                    spotifyHelper.setAccessToken(token)
                    spotifyHelper.connectSpotifyAppRemote {
                        Log.d("MainActivity", "Spotify Remote connected")
                    }
                }
            }
            AuthorizationResponse.Type.ERROR -> Log.e("SpotifyAuth", "Auth error: ${response.error}")
            else -> Log.d("SpotifyAuth", "Canceled or other type")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        authenticateSpotifyUser()


        val factory = ViewModelFactory(
            context = applicationContext,
            brainRepository = brainRepo,
            generalRepository = generalRepo,
            realtimeRepository = realtimeRepo,
            automationRepository = automationRepo,
            spotifyHelper = spotifyHelper
        )

        setContent {
            ProjectJarvisTheme {
                val jarvisViewModel: JarvisViewModel = viewModel(factory = factory)
                JarvisScreen(jarvisViewModel)
            }
        }
    }

    private fun authenticateSpotifyUser() {
        val request = AuthorizationRequest.Builder(
            "acf77eb726c54f07b6a654c52b82fe35",
            AuthorizationResponse.Type.TOKEN,
            "com.example.projectjarvis://callback"
        ).setScopes(arrayOf("app-remote-control")).build()

        val intent = AuthorizationClient.createLoginActivityIntent(this, request)
        authLauncher.launch(intent)
    }



}
