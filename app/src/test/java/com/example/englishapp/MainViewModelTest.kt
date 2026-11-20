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

    @Test
    fun listeningProgressStartsAtLevelOneAndRejectsLockedLevels() {
        val viewModel = MainViewModel()

        assertEquals(1, viewModel.unlockedListeningLevel)
        assertTrue(viewModel.isListeningLevelUnlocked(1))
        assertFalse(viewModel.isListeningLevelUnlocked(2))
        assertFalse(viewModel.selectListeningLevel(2))
        assertEquals(1, viewModel.selectedListeningLevel)
    }

    @Test
    fun listeningLevelOneStartsWithThreeDistractors() {
        val viewModel = MainViewModel()

        assertEquals(3, viewModel.listeningOptionDistractorCount)
    }

    @Test
    fun speakingProgressStartsAtLevelOneAndRejectsLockedLevels() {
        val viewModel = MainViewModel()

        assertEquals(1, viewModel.unlockedSpeakingLevel)
        assertTrue(viewModel.isSpeakingLevelUnlocked(1))
        assertFalse(viewModel.isSpeakingLevelUnlocked(2))
        assertFalse(viewModel.selectSpeakingLevel(2))
        assertEquals(1, viewModel.selectedSpeakingLevel)
    }

    @Test
    fun speakingPromptCannotBeMarkedCompleteBeforeLessonContentLoads() {
        val viewModel = MainViewModel()

        viewModel.markSpeakingSuccess()

        assertFalse(viewModel.isSpeakingQuestionAnswered)
        assertEquals(0, viewModel.speakingCorrectCount)
        assertEquals(0, viewModel.score)
    }

    @Test
    fun readingProgressStartsAtLevelOneAndRejectsLockedLevels() {
        val viewModel = MainViewModel()

        assertEquals(1, viewModel.unlockedReadingLevel)
        assertTrue(viewModel.isReadingLevelUnlocked(1))
        assertFalse(viewModel.isReadingLevelUnlocked(2))
        assertFalse(viewModel.selectReadingLevel(2))
        assertFalse(viewModel.startReadingLesson(2))
        assertEquals(1, viewModel.selectedReadingLevel)
    }

    @Test
    fun readingAnswersAreNotCountedOutsideAnActiveReadingLesson() {
        val viewModel = MainViewModel()

        assertFalse(viewModel.selectReadingAnswer("Quả táo"))

        assertEquals(0, viewModel.readingQuestionCount)
        assertEquals(0, viewModel.readingCorrectCount)
    }

    @Test
    fun writingProgressStartsAtLevelOneAndRejectsLockedLevels() {
        val viewModel = MainViewModel()

        assertEquals(1, viewModel.unlockedWritingLevel)
        assertTrue(viewModel.isWritingLevelUnlocked(1))
        assertFalse(viewModel.isWritingLevelUnlocked(2))
        assertFalse(viewModel.selectWritingLevel(2))
        assertFalse(viewModel.startWritingLesson(2))
        assertEquals(1, viewModel.selectedWritingLevel)
    }

    @Test
    fun writingLessonCannotBeAdvancedBeforeAWritingQuestionIsChecked() {
        val viewModel = MainViewModel()

        viewModel.continueWritingLesson()

        assertEquals(0, viewModel.writingQuestionCount)
        assertEquals(0, viewModel.writingCorrectCount)
        assertFalse(viewModel.isWritingLessonComplete)
    }
}
