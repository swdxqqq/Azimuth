package com.example.azimuth_mobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.azimuth_mobile.presentation.navigation.AzimuthNavigation
import com.example.azimuth_mobile.presentation.theme.AzimuthmobileTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AzimuthmobileTheme(darkTheme = false, dynamicColor = false) {
                AzimuthNavigation()
            }
        }
    }
}
