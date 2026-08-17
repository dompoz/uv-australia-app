package com.uvaustralia.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.uvaustralia.app.ui.main.MainScreen
import com.uvaustralia.app.ui.main.MainViewModel
import com.uvaustralia.app.ui.theme.UvAustraliaTheme
import com.uvaustralia.app.widget.UvWidgetWorker
import android.Manifest

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    @OptIn(ExperimentalPermissionsApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        UvWidgetWorker.schedule(this)

        setContent {
            UvAustraliaTheme {
                val locationPermission = rememberPermissionState(
                    permission = Manifest.permission.ACCESS_COARSE_LOCATION,
                    onPermissionResult = { granted ->
                        if (granted) viewModel.onLocationPermissionGranted()
                        else viewModel.onLocationPermissionDenied()
                    }
                )

                val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                if (uiState.locationPermissionNeeded && !locationPermission.status.isGranted) {
                    androidx.compose.runtime.LaunchedEffect(Unit) {
                        locationPermission.launchPermissionRequest()
                    }
                }

                MainScreen(viewModel = viewModel)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val hasPermission = checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) ==
                android.content.pm.PackageManager.PERMISSION_GRANTED
        viewModel.onAppResumed(hasPermission)
    }
}
