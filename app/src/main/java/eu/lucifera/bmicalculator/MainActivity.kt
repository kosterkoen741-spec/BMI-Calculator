package eu.lucifera.bmicalculator

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.core.os.LocaleListCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import eu.lucifera.bmicalculator.data.SettingsRepository
import eu.lucifera.bmicalculator.ui.screens.*
import eu.lucifera.bmicalculator.ui.theme.BMICalculatorTheme
import eu.lucifera.bmicalculator.ui.theme.DarkBlue
import eu.lucifera.bmicalculator.ui.theme.LightBlue
import eu.lucifera.bmicalculator.ui.viewmodel.MainViewModel
import eu.lucifera.bmicalculator.ui.viewmodel.MainViewModelFactory

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        
        val repository = SettingsRepository(applicationContext)
        val factory = MainViewModelFactory(repository)
        val viewModel = ViewModelProvider(this, factory)[MainViewModel::class.java]
        
        splashScreen.setKeepOnScreenCondition {
            viewModel.theme.value == null
        }
        
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            val themeMode by viewModel.theme.collectAsState()
            val language by viewModel.language.collectAsState()

            if (themeMode == null || language == null) return@setContent

            LaunchedEffect(language) {
                val appLocale: LocaleListCompat = LocaleListCompat.forLanguageTags(language)
                AppCompatDelegate.setApplicationLocales(appLocale)
            }

            val darkTheme = when (themeMode) {
                "light" -> false
                "dark" -> true
                else -> isSystemInDarkTheme()
            }

            BMICalculatorTheme(darkTheme = darkTheme) {
                MainScreen(viewModel)
            }
        }
    }
}

sealed class Screen(val route: String, val icon: ImageVector, val labelRes: Int) {
    object Calculator : Screen("calculator", Icons.Default.Calculate, R.string.nav_calculator)
    object Help : Screen("help", Icons.Default.Help, R.string.nav_help)
    object Settings : Screen("settings", Icons.Default.Settings, R.string.nav_settings)
    object Info : Screen("info", Icons.Default.Info, R.string.nav_info)
}

@Composable
fun MainScreen(viewModel: MainViewModel) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    
    val items = listOf(
        Screen.Calculator,
        Screen.Help,
        Screen.Settings,
        Screen.Info
    )

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = DarkBlue,
                contentColor = LightBlue
            ) {
                items.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = null) },
                        label = { Text(stringResource(screen.labelRes)) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = LightBlue,
                            selectedTextColor = LightBlue,
                            unselectedIconColor = LightBlue.copy(alpha = 0.6f),
                            unselectedTextColor = LightBlue.copy(alpha = 0.6f),
                            indicatorColor = DarkBlue.copy(alpha = 0.1f)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController,
            startDestination = Screen.Calculator.route,
            Modifier.padding(innerPadding)
        ) {
            composable(Screen.Calculator.route) { CalculatorScreen() }
            composable(Screen.Help.route) { HelpScreen() }
            composable(Screen.Settings.route) {
                val themeMode by viewModel.theme.collectAsState()
                val language by viewModel.language.collectAsState()
                SettingsScreen(
                    currentTheme = themeMode ?: "system",
                    onThemeChange = { viewModel.setTheme(it) },
                    currentLanguage = language ?: "en",
                    onLanguageChange = { viewModel.setLanguage(it) }
                )
            }
            composable(Screen.Info.route) { InfoScreen() }
        }
    }
}