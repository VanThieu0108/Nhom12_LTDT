package com.example.englishapp.viewmodel

import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.ViewModel
import com.example.englishapp.data.VocabularyRepository
import com.example.englishapp.model.GameScore
import com.example.englishapp.model.Pet
import com.example.englishapp.model.User
import com.example.englishapp.model.Word
import kotlin.random.Random

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
    val meteorTarget get() = currentWord.english.lowercase()
    var availableVocabularyLevels = listOf(1)
        private set
    var selectedVocabularyLevel = 1
        private set
    var isVocabularyLoading = false
        private set
    var vocabularyMessage = "Using sample vocabulary until the backend connects."
        private set
    var vocabularyVersion = 0
        private set
    var onVocabularyChanged: (() -> Unit)? = null
    private var vocabularyRequestId = 0
    var matchGameLevel = 1
        private set
    var matchPairs: List<Word> = emptyList()
        private set
    var matchRightOrder: List<Int> = emptyList()
        private set
    var matchWrongSelections: Set<String> = emptySet()
        private set
    var isMatchLoading = false
        private set
    var matchStagePassed = false
        private set
    private var matchStageLoadedFor: Int? = null
    private var matchStageRequestId = 0

    private val offlineMatchWords = listOf(
        Word("Apple", "Quả táo", emptyList(), emptyList()),
        Word("Cat", "Con mèo", emptyList(), emptyList()),
        Word("Dog", "Con chó", emptyList(), emptyList()),
        Word("Sun", "Mặt trời", emptyList(), emptyList()),
        Word("Book", "Quyển sách", emptyList(), emptyList()),
        Word("Fish", "Con cá", emptyList(), emptyList()),
        Word("Milk", "Sữa", emptyList(), emptyList()),
        Word("Star", "Ngôi sao", emptyList(), emptyList())
    )

    var words = listOf(
        Word("Apple", "Quả táo", listOf("Con mèo", "Mặt trời", "Con chó"), listOf('P', 'A', 'L', 'P', 'E')),
        Word("Cat", "Con mèo", listOf("Quả táo", "Mặt trời", "Con chó"), listOf('T', 'C', 'A')),
        Word("Dog", "Con chó", listOf("Quả táo", "Con mèo", "Mặt trời"), listOf('G', 'O', 'D')),
        Word("Sun", "Mặt trời", listOf("Quả táo", "Con mèo", "Con chó"), listOf('N', 'U', 'S'))
    )
        private set

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

    fun loadVocabulary(level: Int = selectedVocabularyLevel) {
        selectedVocabularyLevel = level
        val requestId = ++vocabularyRequestId
        isVocabularyLoading = true
        vocabularyMessage = "Loading level $level vocabulary..."
        notifyVocabularyChanged()
        VocabularyRepository.loadLevel(level) { result ->
            Handler(Looper.getMainLooper()).post {
                if (requestId != vocabularyRequestId) return@post
                isVocabularyLoading = false
                result.onSuccess { (levels, remoteWords) ->
                    availableVocabularyLevels = levels
                    selectedVocabularyLevel = level
                    words = remoteWords.mapIndexed { index, item ->
                        val distractors = remoteWords.asSequence()
                            .filter { it.wordEn != item.wordEn && it.meaningVi != item.meaningVi }
                            .map { it.meaningVi }
                            .distinct()
                            .take(3)
                            .toList()
                        Word(
                            english = item.wordEn,
                            vietnamese = item.meaningVi,
                            distractors = distractors,
                            scrambledLetters = item.wordEn.toList().shuffled(Random(index + level))
                        )
                    }
                    activeWordIndex = 0
                    selectedAnswer = null
                    selectedLetters = ""
                    selectedLetterIndices = emptyList()
                    spellingCorrect = false
                    matchedWords = mutableSetOf()
                    vocabularyMessage = "Connected · ${words.size} words · Level $level"
                }.onFailure { error ->
                    vocabularyMessage = "Offline sample words · ${error.message ?: "API unavailable"}"
                }
                notifyVocabularyChanged()
            }
        }
    }

    private fun notifyVocabularyChanged() {
        vocabularyVersion += 1
        onVocabularyChanged?.invoke()
    }

    fun initializeMatchProgress(context: Context) {
        matchGameLevel = context.getSharedPreferences(MATCH_PREFERENCES, Context.MODE_PRIVATE)
            .getInt(KEY_MATCH_LEVEL, 1)
            .coerceIn(1, MATCH_LEVEL_COUNT)
    }

    fun ensureMatchStageLoaded() {
        if (matchPairs.size == MATCH_PAIR_COUNT || isMatchLoading) return
        if (matchStageLoadedFor == matchGameLevel) return
        loadMatchStage()
    }

    fun loadMatchStage() {
        val requestId = ++matchStageRequestId
        val gameLevel = matchGameLevel.coerceIn(1, MATCH_LEVEL_COUNT)
        matchStageLoadedFor = null
        isMatchLoading = true
        matchStagePassed = false
        matchSelection = null
        matchWrongSelections = emptySet()
        matchedWords = mutableSetOf()

        val dictionaryLevel = matchDictionaryLevel(gameLevel)
        val levelOffset = matchLevelOffset(gameLevel)
        VocabularyRepository.loadMatchStage(dictionaryLevel, levelOffset) { result ->
            Handler(Looper.getMainLooper()).post {
                if (requestId != matchStageRequestId) return@post
                isMatchLoading = false
                val stageWords = result.fold(
                    onSuccess = { vocabulary ->
                        vocabulary.map { word ->
                            Word(word.wordEn, word.meaningVi, emptyList(), emptyList())
                        }
                    },
                    onFailure = {
                        if (gameLevel == 1) offlineMatchWords else matchPairs.ifEmpty { offlineMatchWords }
                    }
                )
                matchPairs = stageWords.take(MATCH_PAIR_COUNT).let { selected ->
                    if (selected.size == MATCH_PAIR_COUNT) selected else offlineMatchWords
                }
                matchRightOrder = (0 until MATCH_PAIR_COUNT)
                    .shuffled(Random(gameLevel * 97 + matchPairs.first().english.hashCode()))
                matchStageLoadedFor = gameLevel
                vocabularyMessage = if (result.isSuccess) {
                    "8 pairs · difficulty $dictionaryLevel"
                } else {
                    "Offline sample · ${result.exceptionOrNull()?.message ?: "API unavailable"}"
                }
                notifyVocabularyChanged()
            }
        }
    }

    fun selectMatchCard(pairIndex: Int, isEnglish: Boolean): Boolean {
        if (isMatchLoading || matchStagePassed || pairIndex !in matchPairs.indices) return false
        val english = matchPairs[pairIndex].english
        if (english in matchedWords || matchWrongSelections.isNotEmpty()) return false
        val key = matchCardKey(pairIndex, isEnglish)
        val current = matchSelection
        if (current == null) {
            matchSelection = key
            notifyVocabularyChanged()
            return false
        }
        if (current == key) {
            matchSelection = null
            notifyVocabularyChanged()
            return false
        }

        val selected = parseMatchCardKey(current) ?: run {
            matchSelection = null
            notifyVocabularyChanged()
            return false
        }
        val isCorrect = selected.first == pairIndex && selected.second != isEnglish
        matchSelection = null
        if (isCorrect) {
            matchedWords.add(english)
            score += 10
            user = user.copy(coins = user.coins + 3, wordsLearned = user.wordsLearned + 1)
            feedback = "Perfect match! +3 coins ⭐"
            matchStagePassed = matchedWords.size == MATCH_PAIR_COUNT
            notifyVocabularyChanged()
            return true
        }

        matchWrongSelections = setOf(
            matchCardKey(selected.first, selected.second),
            key
        )
        feedback = "Not quite! Try another pair."
        val requestId = matchStageRequestId
        Handler(Looper.getMainLooper()).postDelayed({
            if (requestId != matchStageRequestId || matchWrongSelections.isEmpty()) return@postDelayed
            matchWrongSelections = emptySet()
            feedback = ""
            notifyVocabularyChanged()
        }, MATCH_WRONG_FLASH_MILLIS)
        return false
    }

    fun advanceMatchLevel(context: Context) {
        if (!matchStagePassed || matchGameLevel >= MATCH_LEVEL_COUNT) return
        matchGameLevel += 1
        context.getSharedPreferences(MATCH_PREFERENCES, Context.MODE_PRIVATE)
            .edit()
            .putInt(KEY_MATCH_LEVEL, matchGameLevel)
            .apply()
        loadMatchStage()
    }

    fun replayMatchStage() {
        if (matchStagePassed) loadMatchStage()
    }

    fun matchMatchedIndices(): Set<Int> =
        matchPairs.indices.filterTo(mutableSetOf()) { index ->
            matchPairs[index].english in matchedWords
        }

    fun matchCardKeyForUi(index: Int, isEnglish: Boolean) = matchCardKey(index, isEnglish)

    private fun matchCardKey(index: Int, isEnglish: Boolean) =
        "$index:${if (isEnglish) "en" else "vi"}"

    private fun parseMatchCardKey(key: String): Pair<Int, Boolean>? {
        val parts = key.split(':', limit = 2)
        val index = parts.firstOrNull()?.toIntOrNull() ?: return null
        val language = parts.getOrNull(1) ?: return null
        if (language != "en" && language != "vi") return null
        return index to (language == "en")
    }

    private fun matchDictionaryLevel(level: Int): Int = when (level) {
        in 1..17 -> 1
        in 18..34 -> 2
        else -> 3
    }

    private fun matchLevelOffset(level: Int): Int {
        val levelInTier = when (level) {
            in 1..17 -> level - 1
            in 18..34 -> level - 18
            else -> level - 35
        }
        return levelInTier * MATCH_PAIR_COUNT
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
        val firstIndex = words.indexOfFirst { it.english == first || it.vietnamese == first }
        val secondIndex = words.indexOfFirst { it.english == word || it.vietnamese == word }
        val firstIsEnglish = firstIndex >= 0 && words[firstIndex].english == first
        val secondIsEnglish = secondIndex >= 0 && words[secondIndex].english == word
        val paired = firstIndex >= 0 && secondIndex >= 0 && firstIndex == secondIndex &&
            firstIsEnglish != secondIsEnglish
        if (paired) {
            matchedWords.add(words[firstIndex].english)
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
            activeWordIndex = (activeWordIndex + 1) % words.size
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
            activeWordIndex = (activeWordIndex + 1) % words.size
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

    companion object {
        const val MATCH_LEVEL_COUNT = 50
        const val MATCH_PAIR_COUNT = 8
        const val MATCH_WRONG_FLASH_MILLIS = 650L
        private const val MATCH_PREFERENCES = "kids_english_match_progress"
        private const val KEY_MATCH_LEVEL = "match_game_level"
    }
}
