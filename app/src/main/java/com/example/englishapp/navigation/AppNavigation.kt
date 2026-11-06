package com.example.englishapp.navigation

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.NavController
import androidx.navigation.NavGraph
import androidx.navigation.NavGraphNavigator
import androidx.navigation.NavOptions
import androidx.navigation.fragment.FragmentNavigator
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import androidx.lifecycle.ViewModelProvider
import com.example.englishapp.R
import com.example.englishapp.viewmodel.AuthViewModel
import com.example.englishapp.viewmodel.MainViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView

class AppNavigation(private val activity: AppCompatActivity) {
    val mainViewModel = ViewModelProvider(activity)[MainViewModel::class.java]
    private val authViewModel = ViewModelProvider(activity)[AuthViewModel::class.java]
    private lateinit var navController: NavController
    private lateinit var bottomNavigation: BottomNavigationView

    fun start() {
        val root = activity.findViewById<View>(R.id.main)
        ViewCompat.setOnApplyWindowInsetsListener(root) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }
        bottomNavigation = activity.findViewById(R.id.bottomNavigationView)
        bottomNavigation.itemIconTintList = null
        val navHost = activity.supportFragmentManager
            .findFragmentById(R.id.nav_host) as NavHostFragment
        navController = navHost.navController
        navController.graph = createGraph()
        NavigationUI.setupWithNavController(bottomNavigation, navController)
        mainViewModel.initializeMatchProgress(activity)
        mainViewModel.loadVocabulary()
        navController.addOnDestinationChangedListener { _, destination, _ ->
            updateBottomNavigation(destination.id)
        }
        navController.currentDestination?.id?.let(::updateBottomNavigation)
    }

    fun navigateTo(route: String, zone: String? = null) {
        if (route == ROUTE_LOGIN) authViewModel.logout(activity)
        val routeId = when {
            route == ROUTE_HOME -> R.id.nav_islands
            route == ROUTE_PETS -> R.id.nav_pets
            route == ROUTE_TROPHIES -> R.id.nav_trophies
            route == ROUTE_PROFILE -> R.id.nav_profile
            route == ROUTE_MAP -> R.id.destination_map
            route == ROUTE_LOGIN -> R.id.destination_login
            route == ROUTE_REGISTER -> R.id.destination_register
            route == ROUTE_LISTENING -> R.id.destination_listening
            route == ROUTE_READING -> R.id.destination_reading
            route == ROUTE_SPEAKING -> R.id.destination_speaking
            route == ROUTE_WRITING -> R.id.destination_writing
            route == ROUTE_ARCADE -> R.id.destination_arcade
            route == ROUTE_MATCH -> R.id.destination_match
            route == ROUTE_RUNNER -> R.id.destination_runner
            route == ROUTE_METEOR -> R.id.destination_meteor
            route == ROUTE_BATTLE -> R.id.destination_battle
            route.startsWith("$ROUTE_MAP:") -> R.id.destination_map
            else -> R.id.nav_islands
        }

        val arguments = if (route == ROUTE_MAP || route.startsWith("$ROUTE_MAP:")) {
            Bundle().apply {
                putString(
                    ScreenHostFragment.ARG_ZONE,
                    zone ?: route.substringAfter(':', "Listening")
                )
            }
        } else {
            null
        }
        val currentId = navController.currentDestination?.id
        if (routeId == R.id.nav_islands && currentId != routeId) {
            if (!navController.popBackStack(routeId, false)) {
                navController.navigate(routeId, arguments, NavOptions.Builder()
                    .setPopUpTo(navController.currentDestination?.id ?: R.id.destination_login, true)
                    .setLaunchSingleTop(true)
                    .build())
            }
        } else if (currentId == routeId && routeId !in MAIN_DESTINATIONS) {
            navController.popBackStack(routeId, true)
            navController.navigate(routeId, arguments)
        } else if (currentId != routeId || arguments != null) {
            navController.navigate(routeId, arguments, NavOptions.Builder().setLaunchSingleTop(true).build())
        }
    }

    private fun updateBottomNavigation(destinationId: Int) {
        bottomNavigation.visibility = if (destinationId in MAIN_DESTINATIONS) View.VISIBLE else View.GONE
    }

    private fun createGraph(): NavGraph {
        val provider = navController.navigatorProvider
        val graph = NavGraph(provider.getNavigator(NavGraphNavigator::class.java)).apply {
            id = R.id.nav_graph
            setStartDestination(if (authViewModel.savedExplorerName(activity) == null) {
                R.id.destination_login
            } else {
                mainViewModel.updateExplorerName(authViewModel.savedExplorerName(activity).orEmpty())
                R.id.nav_islands
            })
        }
        val fragmentNavigator = provider.getNavigator(FragmentNavigator::class.java)
        val destinations = listOf(
            R.id.destination_login to ROUTE_LOGIN,
            R.id.destination_register to ROUTE_REGISTER,
            R.id.nav_islands to ROUTE_HOME,
            R.id.destination_listening to ROUTE_LISTENING,
            R.id.destination_reading to ROUTE_READING,
            R.id.destination_speaking to ROUTE_SPEAKING,
            R.id.destination_writing to ROUTE_WRITING,
            R.id.destination_arcade to ROUTE_ARCADE,
            R.id.destination_match to ROUTE_MATCH,
            R.id.destination_runner to ROUTE_RUNNER,
            R.id.destination_meteor to ROUTE_METEOR,
            R.id.nav_pets to ROUTE_PETS,
            R.id.nav_trophies to ROUTE_TROPHIES,
            R.id.nav_profile to ROUTE_PROFILE,
            R.id.destination_battle to ROUTE_BATTLE,
            R.id.destination_map to ROUTE_MAP
        )
        destinations.forEach { (id, route) ->
            val destination = fragmentNavigator.createDestination().apply {
                this.id = id
                setClassName(ScreenHostFragment::class.java.name)
                label = route
                addArgument(
                    ScreenHostFragment.ARG_ROUTE,
                    androidx.navigation.NavArgument.Builder().setDefaultValue(route).build()
                )
                if (route == ROUTE_MAP) {
                    addArgument(
                        ScreenHostFragment.ARG_ZONE,
                        androidx.navigation.NavArgument.Builder().setDefaultValue("Listening").build()
                    )
                }
            }
            graph.addDestination(destination)
        }
        return graph
    }

    fun renderScreen(route: String, zone: String): View {
        val navigate: (String) -> Unit = { requestedRoute ->
            if (requestedRoute.startsWith("$ROUTE_MAP:")) {
                navigateTo(ROUTE_MAP, requestedRoute.substringAfter(':'))
            } else {
                navigateTo(requestedRoute)
            }
        }
        return when (route) {
            ROUTE_LOGIN -> com.example.englishapp.ui.screens.LoginScreen(activity, { name, pin ->
                val error = authViewModel.login(activity, name, pin)
                if (error == null) mainViewModel.updateExplorerName(name)
                error
            }, navigate)
            ROUTE_REGISTER -> com.example.englishapp.ui.screens.RegisterScreen(activity, { name, pin, confirm ->
                val error = authViewModel.register(activity, name, pin, confirm)
                if (error == null) mainViewModel.updateExplorerName(name)
                error
            }, navigate)
            ROUTE_LISTENING -> com.example.englishapp.ui.screens.ListeningGameScreen(activity, mainViewModel, navigate)
            ROUTE_HOME -> com.example.englishapp.ui.screens.HomeScreen(activity, mainViewModel, navigate)
            ROUTE_READING -> com.example.englishapp.ui.screens.ReadingGameScreen(activity, mainViewModel, navigate)
            ROUTE_SPEAKING -> com.example.englishapp.ui.screens.SpeakingGameScreen(activity, mainViewModel, navigate)
            ROUTE_WRITING -> com.example.englishapp.ui.screens.WritingGameScreen(activity, mainViewModel, navigate)
            ROUTE_ARCADE -> com.example.englishapp.ui.screens.ArcadeScreen(activity, navigate)
            ROUTE_MATCH -> com.example.englishapp.ui.screens.MinigameMatchScreen(activity, mainViewModel, navigate)
            ROUTE_RUNNER -> com.example.englishapp.ui.screens.MinigameRunnerScreen(activity, mainViewModel, navigate)
            ROUTE_METEOR -> com.example.englishapp.ui.screens.MinigameMeteorScreen(activity, mainViewModel, navigate)
            ROUTE_PETS -> com.example.englishapp.ui.screens.PetCollectionScreen(activity, mainViewModel, navigate)
            ROUTE_TROPHIES -> com.example.englishapp.ui.screens.TrophiesScreen(activity, mainViewModel, navigate)
            ROUTE_PROFILE -> com.example.englishapp.ui.screens.ProfileScreen(activity, mainViewModel, navigate)
            ROUTE_BATTLE -> com.example.englishapp.ui.screens.BattleScreen(activity, mainViewModel, navigate)
            else -> com.example.englishapp.ui.screens.ZoneMapScreen(activity, mainViewModel, zone, navigate)
        }
    }

    companion object {
        const val ROUTE_LOGIN = "login"
        const val ROUTE_REGISTER = "register"
        const val ROUTE_LISTENING = "listening"
        const val ROUTE_HOME = "home"
        const val ROUTE_READING = "reading"
        const val ROUTE_SPEAKING = "speaking"
        const val ROUTE_WRITING = "writing"
        const val ROUTE_ARCADE = "arcade"
        const val ROUTE_MATCH = "match"
        const val ROUTE_RUNNER = "runner"
        const val ROUTE_METEOR = "meteor"
        const val ROUTE_PETS = "pets"
        const val ROUTE_TROPHIES = "trophies"
        const val ROUTE_PROFILE = "profile"
        const val ROUTE_BATTLE = "battle"
        const val ROUTE_MAP = "map"
        private val MAIN_DESTINATIONS = setOf(
            R.id.nav_islands,
            R.id.nav_pets,
            R.id.nav_trophies,
            R.id.nav_profile
        )
    }
}
