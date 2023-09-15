package com.example.roomtodolist.ui.screens.profile

import android.net.Uri
import androidx.lifecycle.ViewModel
import com.example.roomtodolist.domain.main_activity.MainViewModel

class ProfileViewModel(
    private val mainViewModel: MainViewModel
) : ViewModel() {

    fun getProfilePicture() = mainViewModel.getProfilePicture()

    fun getUsername() = mainViewModel.getUsername()

    fun isDarkMode() = mainViewModel.uiState.isDarkTheme

    fun setUsername(newUsername: String?) {
        mainViewModel.setUsername(newUsername)
    }

    fun setLightDarkMode(isDark: Boolean) {
        mainViewModel.setIsDarkMode(isDark)
    }

    fun setProfilePicture(newProfilePicture: Uri?) {
        mainViewModel.setProfilePicture(uri = newProfilePicture)
    }

}