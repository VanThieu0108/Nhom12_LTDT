package com.example.englishapp.navigation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

class ScreenHostFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val route = requireArguments().getString(ARG_ROUTE).orEmpty()
        val zone = requireArguments().getString(ARG_ZONE).orEmpty()
        val activity = requireActivity() as MainActivityNavigation
        return activity.appNavigation.renderScreen(route, zone)
    }

    companion object {
        const val ARG_ROUTE = "screen_route"
        const val ARG_ZONE = "zone_name"
    }
}

interface MainActivityNavigation {
    val appNavigation: AppNavigation
}
