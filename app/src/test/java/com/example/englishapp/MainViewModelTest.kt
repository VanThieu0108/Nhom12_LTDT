package com.example.englishapp

import com.example.englishapp.viewmodel.MainViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MainViewModelTest {
    @Test
    fun readingAnswerRewardsCoinsAndKeepsLives() {
        val viewModel = MainViewModel()

        assertTrue(viewModel.selectAnswer("Quả táo"))

        assertEquals(10, viewModel.score)
        assertEquals(155, viewModel.user.coins)
        assertEquals(3, viewModel.hearts)
    }

    @Test
    fun incorrectReadingAnswerConsumesOneHeart() {
        val viewModel = MainViewModel()

        assertFalse(viewModel.selectAnswer("Con mèo"))

        assertEquals(2, viewModel.hearts)
        assertEquals(0, viewModel.score)
    }

    @Test
    fun listeningAnswerUsesTheEnglishWordAndAwardsPoints() {
        val viewModel = MainViewModel()

        assertTrue(viewModel.selectListeningAnswer("Apple"))

        assertEquals(10, viewModel.score)
        assertEquals(155, viewModel.user.coins)
        assertEquals(3, viewModel.hearts)
    }

    @Test
    fun correctWordPairDisappearsFromMatchSet() {
        val viewModel = MainViewModel()

        assertFalse(viewModel.chooseMatch("Apple"))
        assertTrue(viewModel.chooseMatch("Quả táo"))

        assertTrue(viewModel.matchedWords.contains("Apple"))
        assertEquals(10, viewModel.score)
    }

    @Test
    fun spellingAcceptsScrambledLettersInCorrectOrder() {
        val viewModel = MainViewModel()
        "APPLE".forEach(viewModel::selectLetter)

        assertTrue(viewModel.checkSpelling())
        assertTrue(viewModel.spellingCorrect)
        assertEquals(15, viewModel.score)
    }

    @Test
    fun meteorIsDestroyedAfterCorrectSpelling() {
        val viewModel = MainViewModel()
        var destroyed = false
        viewModel.meteorTarget.forEach { letter ->
            destroyed = viewModel.submitMeteorLetter(letter)
        }

        assertTrue(destroyed)
        assertEquals(20, viewModel.score)
        assertEquals(3, viewModel.hearts)
    }

    @Test
    fun hatchingUnlocksNextPet() {
        val viewModel = MainViewModel()

        val pet = viewModel.hatchEgg()

        assertEquals("cat", pet?.id)
        assertTrue(viewModel.pets.first { it.id == "cat" }.unlocked)
    }
}
