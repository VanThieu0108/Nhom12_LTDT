package com.example.englishapp.viewmodel

import androidx.lifecycle.ViewModel
import com.example.englishapp.model.GameScore
import com.example.englishapp.model.Pet
import com.example.englishapp.model.User
import com.example.englishapp.model.Word

class MainViewModel : ViewModel() {
    var user = User("Smart Explorer")
        private set
    var selectedAnswer: String? = null
        private set
    var selectedLetters = ""
        private set
    var selectedLetterIndices: List<Int> = emptyList()
        private set
    var spellingCorrect = false
        private set
    var matchSelection: String? = null
        private set
    var matchedWords = mutableSetOf<String>()
        private set
    var score = 0
        private set
    var hearts = 3
        private set
    var runnerLane = 1
        private set
    var runnerDistance = 0
        private set
    var runnerBestDistance = 0
        private set
    var meteorInput = ""
        private set
    var activeWordIndex = 0
        private set
    var feedback = ""
        private set
    var lastGameScore: GameScore? = null
        private set
    val currentWord get() = words[activeWordIndex]
    val meteorTarget = "gardening"

    val words = listOf(
        Word("Apple", "Quả táo", listOf("Con mèo", "Mặt trời", "Con chó"), listOf('P', 'A', 'L', 'P', 'E')),
        Word("Cat", "Con mèo", listOf("Quả táo", "Mặt trời", "Con chó"), listOf('T', 'C', 'A')),
        Word("Dog", "Con chó", listOf("Quả táo", "Con mèo", "Mặt trời"), listOf('G', 'O', 'D')),
        Word("Sun", "Mặt trời", listOf("Quả táo", "Con mèo", "Con chó"), listOf('N', 'U', 'S'))
    )

    var pets: List<Pet> = listOf(
        Pet("bunny", "Bunny", "🐰", "Common", true),
        Pet("fox", "Golden Fox", "🦊", "Rare", true),
        Pet("cat", "Golden Cat", "🐱", "Legendary", false),
        Pet("dragon", "Green Dragon", "🐲", "Epic", false),
        Pet("panda", "Panda", "🐼", "Rare", false),
        Pet("unicorn", "Unicorn", "🦄", "Legendary", false)
    )
        private set

    fun updateExplorerName(name: String) {
        if (name.isNotBlank()) user = user.copy(explorerName = name)
    }

    fun selectAnswer(answer: String): Boolean {
        if (selectedAnswer != null) return selectedAnswer == answer
        selectedAnswer = answer
        return recordAnswer(answer == currentWord.vietnamese)
    }

    fun selectListeningAnswer(answer: String): Boolean {
        if (selectedAnswer != null) return selectedAnswer == answer
        selectedAnswer = answer
        return recordAnswer(answer == currentWord.english)
    }

    private fun recordAnswer(correct: Boolean): Boolean {
        if (correct) {
            score += 10
            user = user.copy(coins = user.coins + 5, wordsLearned = user.wordsLearned + 1)
            feedback = "Correct! +5 coins ⭐"
        } else {
            hearts = (hearts - 1).coerceAtLeast(0)
            feedback = "Almost! Try the next word."
        }
        return correct
    }

    fun nextQuestion() {
        activeWordIndex = (activeWordIndex + 1) % words.size
        selectedAnswer = null
        selectedLetters = ""
        selectedLetterIndices = emptyList()
        feedback = ""
        spellingCorrect = false
        if (hearts == 0) hearts = 3
    }

    fun markSpeakingSuccess() {
        selectedAnswer = null
        score += 10
        user = user.copy(coins = user.coins + 5)
        feedback = "Perfect pronunciation!"
    }

    fun selectLetter(letter: Char) {
        val index = currentWord.scrambledLetters.indices.firstOrNull {
            it !in selectedLetterIndices && currentWord.scrambledLetters[it].equals(letter, ignoreCase = true)
        } ?: return
        selectLetterAt(index)
    }

    fun selectLetterAt(index: Int) {
        if (index !in currentWord.scrambledLetters.indices ||
            index in selectedLetterIndices ||
            selectedLetters.length >= currentWord.english.length
        ) return
        selectedLetterIndices = selectedLetterIndices + index
        selectedLetters = selectedLetterIndices.map { currentWord.scrambledLetters[it] }.joinToString("")
        spellingCorrect = false
        feedback = ""
    }

    fun removeLastLetter() {
        if (selectedLetterIndices.isNotEmpty()) {
            selectedLetterIndices = selectedLetterIndices.dropLast(1)
            selectedLetters = selectedLetterIndices.map { currentWord.scrambledLetters[it] }.joinToString("")
            spellingCorrect = false
        }
    }

    fun checkSpelling(): Boolean {
        val correct = selectedLetters.equals(currentWord.english, ignoreCase = true)
        spellingCorrect = correct
        if (correct) {
            score += 15
            user = user.copy(coins = user.coins + 5, wordsLearned = user.wordsLearned + 1)
            feedback = "Amazing spelling! +5 coins"
        } else {
            feedback = "Not quite. Tap a letter to try again."
        }
        return correct
    }

    fun clearSpelling() {
        selectedLetters = ""
        selectedLetterIndices = emptyList()
        feedback = ""
        spellingCorrect = false
    }

    fun chooseMatch(word: String): Boolean {
        if (matchedWords.contains(word)) return false
        if (matchSelection == null) {
            matchSelection = word
            return false
        }
        val first = matchSelection
        matchSelection = null
        val english = listOf("Apple", "Cat", "Dog", "Sun")
        val vietnamese = listOf("Quả táo", "Con mèo", "Con chó", "Mặt trời")
        val firstIndex = english.indexOf(first).takeIf { it >= 0 } ?: vietnamese.indexOf(first)
        val secondIndex = english.indexOf(word).takeIf { it >= 0 } ?: vietnamese.indexOf(word)
        val paired = firstIndex >= 0 && secondIndex >= 0 && firstIndex == secondIndex &&
            (english.indexOf(first) >= 0) != (english.indexOf(word) >= 0)
        if (paired) {
            matchedWords.add(english[firstIndex])
            score += 10
            user = user.copy(coins = user.coins + 3)
            feedback = "Perfect match! +3 coins"
        } else {
            feedback = "Those don't match. Try again!"
        }
        return paired
    }

    fun resetMatch() {
        matchedWords = mutableSetOf()
        matchSelection = null
        feedback = ""
        if (lastGameScore?.gameName == "Word Match") lastGameScore = null
    }

    fun moveRunner(lane: Int) {
        runnerLane = lane.coerceIn(0, 2)
        runnerDistance += 25
        runnerBestDistance = maxOf(runnerBestDistance, runnerDistance)
        if (runnerLane == 1) {
            score += 10
            user = user.copy(coins = user.coins + 2)
            feedback = "Correct gate! +10 points"
        } else {
            hearts = (hearts - 1).coerceAtLeast(0)
            feedback = "Missed the word gate!"
        }
    }

    fun submitMeteorLetter(letter: Char): Boolean {
        meteorInput += letter.lowercaseChar()
        val correct = meteorTarget.startsWith(meteorInput)
        if (!correct) {
            meteorInput = ""
            hearts = (hearts - 1).coerceAtLeast(0)
            feedback = "Meteor hit! Try again."
            return false
        }
        if (meteorInput == meteorTarget) {
            score += 20
            user = user.copy(coins = user.coins + 10)
            feedback = "Meteor destroyed! +10 coins"
            meteorInput = ""
            return true
        }
        feedback = "Nice shot!"
        return false
    }

    fun clearFeedback() {
        feedback = ""
    }

    fun completeGame(name: String) {
        lastGameScore?.let {
            if (it.gameName == name && it.completed) return
        }
        lastGameScore = GameScore(name, score, true)
        user = user.copy(experience = user.experience + 20)
    }

    fun hatchEgg(): Pet? {
        val lockedPet = pets.firstOrNull { !it.unlocked } ?: return null
        pets = pets.map { if (it.id == lockedPet.id) it.copy(unlocked = true) else it }
        user = user.copy(coins = (user.coins + 5))
        feedback = "${lockedPet.name} hatched!"
        return lockedPet
    }

    fun awardBattleCoins() {
        user = user.copy(coins = user.coins + 10)
        score += 10
    }
}
