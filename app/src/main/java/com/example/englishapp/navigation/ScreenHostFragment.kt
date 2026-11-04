package com.example.englishapp.navigation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.Fragment

class ScreenHostFragment : Fragment() {
    private var screenContainer: FrameLayout? = null
    private var vocabularyListener: (() -> Unit)? = null
    private var renderedVersion = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return FrameLayout(requireContext()).also { screenContainer = it }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        renderScreen()
    }

    override fun onResume() {
        super.onResume()
        val activity = requireActivity() as MainActivityNavigation
        val listener = {
            if (renderedVersion != activity.appNavigation.mainViewModel.vocabularyVersion &&
                requireArguments().getString(ARG_ROUTE).orEmpty() !in setOf("login", "register")
            ) {
                renderScreen()
            }
        }
        vocabularyListener = listener
        activity.appNavigation.mainViewModel.onVocabularyChanged = listener
    }

    override fun onPause() {
        val activity = requireActivity() as? MainActivityNavigation
        activity?.appNavigation?.mainViewModel?.let { viewModel ->
            if (viewModel.onVocabularyChanged === vocabularyListener) {
                viewModel.onVocabularyChanged = null
            }
        }
        vocabularyListener = null
        super.onPause()
    }

    override fun onDestroyView() {
        screenContainer = null
        super.onDestroyView()
    }

    private fun renderScreen() {
        val route = requireArguments().getString(ARG_ROUTE).orEmpty()
        val zone = requireArguments().getString(ARG_ZONE).orEmpty()
        val activity = requireActivity() as MainActivityNavigation
        val container = screenContainer ?: return
        renderedVersion = activity.appNavigation.mainViewModel.vocabularyVersion
        container.removeAllViews()
        container.addView(
            activity.appNavigation.renderScreen(route, zone),
            FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        )
    }

    companion object {
        const val ARG_ROUTE = "screen_route"
        const val ARG_ZONE = "zone_name"
    }
}

interface MainActivityNavigation {
    val appNavigation: AppNavigation
}
