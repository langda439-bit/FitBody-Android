package com.fitbody.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.fitbody.app.feature.camera.FoodCameraScreen
import com.fitbody.app.ui.screens.DietScreen
import com.fitbody.app.ui.screens.HomeScreen
import com.fitbody.app.ui.screens.ProfileScreen
import com.fitbody.app.ui.screens.TrainingScreen
import com.fitbody.app.ui.screens.WeightScreen
import com.fitbody.app.ui.theme.FitBodyTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FitBodyTheme {
                AppRoot()
            }
        }
    }
}

private enum class Dest(val route: String, val label: String, val icon: ImageVector) {
    HOME("home", "首页", Icons.Filled.Home),
    WEIGHT("weight", "体重", Icons.Filled.MonitorWeight),
    DIET("diet", "饮食", Icons.Filled.Restaurant),
    TRAINING("training", "训练", Icons.Filled.FitnessCenter),
    PROFILE("profile", "我的", Icons.Filled.Person)
}

@Composable
private fun AppRoot() {
    val vm: MainViewModel = viewModel()
    val navController = rememberNavController()
    val scaleState by vm.scaleState.collectAsStateWithLifecycle()
    val live by vm.liveMeasurement.collectAsStateWithLifecycle()
    val lastSaved by vm.lastSaved.collectAsStateWithLifecycle()

    val permissions = remember { requiredPermissions() }
    val context = androidx.compose.ui.platform.LocalContext.current
    var allGranted by remember { mutableStateOf(false) }
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        allGranted = permissions.all { result[it] == true }
        if (allGranted) vm.startScale()
    }

    LaunchedEffect(Unit) {
        allGranted = permissions.all {
            context.checkSelfPermission(it) == PackageManager.PERMISSION_GRANTED
        }
        if (allGranted) vm.startScale() else launcher.launch(permissions.toTypedArray())
    }

    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route
    val showBottomBar = currentRoute in Dest.entries.map { it.route }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    Dest.entries.forEach { dest ->
                        NavigationBarItem(
                            selected = currentRoute == dest.route,
                            onClick = {
                                navController.navigate(dest.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(dest.icon, contentDescription = dest.label) },
                            label = { Text(dest.label) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Dest.HOME.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Dest.HOME.route) {
                HomeScreen(
                    scaleState = scaleState,
                    live = live,
                    lastSaved = lastSaved,
                    onStart = { vm.startScale() },
                    onOpenCamera = { navController.navigate("camera") }
                )
            }
            composable(Dest.WEIGHT.route) { WeightScreen() }
            composable(Dest.DIET.route) {
                DietScreen(onOpenCamera = { navController.navigate("camera") })
            }
            composable(Dest.TRAINING.route) { TrainingScreen() }
            composable(Dest.PROFILE.route) { ProfileScreen() }
            composable("camera") {
                FoodCameraScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}

private fun requiredPermissions(): List<String> = buildList {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        add(Manifest.permission.BLUETOOTH_SCAN)
        add(Manifest.permission.BLUETOOTH_CONNECT)
        add(Manifest.permission.POST_NOTIFICATIONS)
    } else {
        add(Manifest.permission.ACCESS_FINE_LOCATION)
    }
    add(Manifest.permission.CAMERA)
}
