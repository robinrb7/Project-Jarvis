package com.example.projectjarvis.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.projectjarvis.repository.*
import com.example.projectjarvis.utils.SpotifyHelper

class ViewModelFactory(
    private val context: Context,
    private val brainRepository: BrainRepository,
    private val generalRepository: GeneralRepository,
    private val realtimeRepository: RealtimeRepository,
    private val automationRepository: AutomationRepository,
    private val spotifyHelper: SpotifyHelper
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(JarvisViewModel::class.java)) {
            return JarvisViewModel(
                context,
                brainRepository,
                generalRepository,
                realtimeRepository,
                automationRepository,
                spotifyHelper
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
