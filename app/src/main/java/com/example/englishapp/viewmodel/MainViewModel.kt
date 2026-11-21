package com.example.englishapp.viewmodel

import android.content.Context
import android.content.SharedPreferences
import android.os.Handler
import android.os.Looper
import android.util.Log
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
    var runnerReachedMilestones: Set<Int> = emptySet()
        private set
    var activeRunnerIndex = 0
        private set
    var runnerSecondsRemaining = 10
        private set
    var runnerRoundResolved = false
        private set
    var runnerLastAnswerCorrect: Boolean? = null
        private set
    private val runnerFallbackPool = listOf(
        Word("Apple", "Quả táo", emptyList(), emptyList()),
        Word("Cat", "Con mèo", emptyList(), emptyList()),
        Word("Dog", "Con chó", emptyList(), emptyList()),
        Word("Sun", "Mặt trời", emptyList(), emptyList()),
        Word("Book", "Quyển sách", emptyList(), emptyList()),
        Word("Fish", "Con cá", emptyList(), emptyList()),
        Word("Milk", "Sữa", emptyList(), emptyList()),
        Word("Star", "Ngôi sao", emptyList(), emptyList()),
        Word("Bird", "Con chim", emptyList(), emptyList()),
        Word("Tree", "Cây", emptyList(), emptyList()),
        Word("Water", "Nước", emptyList(), emptyList()),
        Word("Chair", "Cái ghế", emptyList(), emptyList()),
        Word("Car", "Xe hơi", emptyList(), emptyList()),
        Word("Flower", "Bông hoa", emptyList(), emptyList()),
        Word("School", "Trường học", emptyList(), emptyList()),
        Word("Pencil", "Cây bút chì", emptyList(), emptyList())
    )
    private var runnerPool: List<Word> = runnerFallbackPool
    private var runnerRoundOptions: List<String> = listOf("Cat", "Dog", "Apple")
    private var runnerRoundId = 0
    val runnerCurrentWord: Word
        get() = runnerPool.getOrNull(activeRunnerIndex) ?: runnerFallbackPool.first()
    val runnerOptions: List<String> get() = runnerRoundOptions
    val runnerCorrectLane: Int
        get() = runnerOptions.indexOf(runnerCurrentWord.english)
    val isRunnerGameOver: Boolean
        get() = runnerDistance >= RUNNER_FINISH_DISTANCE || hearts <= 0
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
    var selectedListeningLevel = 1
        private set
    var unlockedListeningLevel = 1
        private set
    var completedListeningLevels: Set<Int> = emptySet()
        private set
    var listeningQuestionCount = 0
        private set
    var listeningCorrectCount = 0
        private set
    val listeningOptionDistractorCount: Int
        get() = listeningDistractorCount(selectedListeningLevel)
    var isListeningLessonReady = false
        private set
    var isListeningLessonComplete = false
        private set
    var wasListeningAttemptPassed = false
        private set
    var selectedSpeakingLevel = 1
        private set
    var selectedReadingLevel = 1
        private set
    var unlockedReadingLevel = 1
        private set
    var completedReadingLevels: Set<Int> = emptySet()
        private set
    var selectedWritingLevel = 1
        private set
    var writingAttemptLevel = 1
        private set
    var unlockedWritingLevel = 1
        private set
    var completedWritingLevels: Set<Int> = emptySet()
        private set
    var writingQuestionCount = 0
        private set
    var writingCorrectCount = 0
        private set
    var isWritingLessonReady = false
        private set
    var isWritingLessonComplete = false
        private set
    var wasWritingAttemptPassed = false
        private set
    var isWritingQuestionAnswered = false
        private set
    var readingQuestionCount = 0
        private set
    var readingCorrectCount = 0
        private set
    var isReadingLessonReady = false
        private set
    var isReadingLessonComplete = false
        private set
    var wasReadingAttemptPassed = false
        private set
    var unlockedSpeakingLevel = 1
        private set
    var completedSpeakingLevels: Set<Int> = emptySet()
        private set
    var speakingQuestionCount = 0
        private set
    var speakingCorrectCount = 0
        private set
    var isSpeakingLessonReady = false
        private set
    var isSpeakingLessonComplete = false
        private set
    var wasSpeakingAttemptPassed = false
        private set
    var isSpeakingQuestionAnswered = false
        private set
    val speakingDifficultyDescription: String
        get() = when (selectedSpeakingLevel) {
            in 1..10 -> "Short, familiar words and clear sounds"
            in 11..20 -> "Longer words and careful syllable practice"
            in 21..30 -> "More challenging sounds and word stress"
            in 31..40 -> "Short phrases spoken smoothly"
            else -> "Longer sentence prompts and clear pronunciation"
        }
    var isVocabularyLoading = false
        private set
    var vocabularyMessage = "Using sample vocabulary until the backend connects."
        private set
    var vocabularyVersion = 0
        private set
    var onVocabularyChanged: (() -> Unit)? = null
    private var vocabularyRequestId = 0
    private var listeningPreferences: SharedPreferences? = null
    private var speakingPreferences: SharedPreferences? = null
    private var readingPreferences: SharedPreferences? = null
    private var writingPreferences: SharedPreferences? = null
    var matchGameLevel = 1
        private set
    var unlockedMatchLevel = 1
        private set
    var completedMatchLevels: Set<Int> = emptySet()
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
    private var matchPreferences: SharedPreferences? = null

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

    fun initializeListeningProgress(context: Context) {
        listeningPreferences = context.applicationContext.getSharedPreferences(
            LISTENING_PREFERENCES,
            Context.MODE_PRIVATE
        )
        val saved = listeningPreferences ?: return
        completedListeningLevels = saved.getString(KEY_LISTENING_COMPLETED, "")
            .orEmpty()
            .split(',')
            .mapNotNull(String::toIntOrNull)
            .filter { it in 1..LISTENING_LEVEL_COUNT }
            .toSet()
        unlockedListeningLevel = nextSequentialListeningLevel(completedListeningLevels)
        selectedListeningLevel = unlockedListeningLevel
    }

    fun initializeSpeakingProgress(context: Context) {
        speakingPreferences = context.applicationContext.getSharedPreferences(
            SPEAKING_PREFERENCES,
            Context.MODE_PRIVATE
        )
        val saved = speakingPreferences ?: return
        completedSpeakingLevels = saved.getString(KEY_SPEAKING_COMPLETED, "")
            .orEmpty()
            .split(',')
            .mapNotNull(String::toIntOrNull)
            .filter { it in 1..SPEAKING_LEVEL_COUNT }
            .toSet()
        unlockedSpeakingLevel = nextSequentialSpeakingLevel(completedSpeakingLevels)
        selectedSpeakingLevel = unlockedSpeakingLevel
    }

    fun isSpeakingLevelUnlocked(level: Int): Boolean = level in 1..unlockedSpeakingLevel

    fun isSpeakingLevelCompleted(level: Int): Boolean = level in completedSpeakingLevels

    fun selectSpeakingLevel(level: Int): Boolean {
        if (!isSpeakingLevelUnlocked(level)) return false
        selectedSpeakingLevel = level
        notifyVocabularyChanged()
        return true
    }

    fun startSpeakingLesson(level: Int = selectedSpeakingLevel): Boolean {
        if (!isSpeakingLevelUnlocked(level)) return false
        selectedSpeakingLevel = level
        val requestId = ++vocabularyRequestId
        isVocabularyLoading = true
        isSpeakingLessonReady = false
        isSpeakingLessonComplete = false
        wasSpeakingAttemptPassed = false
        isSpeakingQuestionAnswered = false
        speakingQuestionCount = 0
        speakingCorrectCount = 0
        activeWordIndex = 0
        feedback = ""
        vocabularyMessage = "Loading Speaking level $level..."
        notifyVocabularyChanged()

        VocabularyRepository.loadSpeakingLesson(level) { result ->
            Handler(Looper.getMainLooper()).post {
                if (requestId != vocabularyRequestId) return@post
                isVocabularyLoading = false
                result.onSuccess { vocabulary ->
                    words = vocabulary.mapIndexed { index, item ->
                        val prompt = speakingPrompt(level, item.wordEn)
                        Word(
                            english = prompt.first,
                            vietnamese = prompt.second(item.meaningVi),
                            distractors = emptyList(),
                            scrambledLetters = item.wordEn.toList().shuffled(Random(index + level)),
                            phonetic = item.phonetic?.trim()?.takeIf(String::isNotEmpty),
                            pronunciationText = item.wordEn
                        )
                    }
                    isSpeakingLessonReady = words.size == SPEAKING_QUESTIONS_PER_LEVEL
                    vocabularyMessage = if (isSpeakingLessonReady) {
                        "Speaking level $level · ${words.size} prompts"
                    } else {
                        "Level $level needs 10 unique speaking prompts."
                    }
                    activeWordIndex = 0
                }.onFailure { error ->
                    Log.w(TAG, "Failed to load Speaking level $level", error)
                    isSpeakingLessonReady = false
                    vocabularyMessage = "Couldn't load Speaking level $level. Check the backend and retry."
                }
                notifyVocabularyChanged()
            }
        }
        return true
    }

    private fun speakingPrompt(level: Int, vocabulary: String): Pair<String, (String) -> String> =
        when (level) {
            in 1..30 -> vocabulary to { meaning -> meaning }
            in 31..40 -> "Could you say $vocabulary slowly and clearly?" to
                { meaning -> "Bạn có thể nói “$meaning” chậm và rõ ràng không?" }
            else -> "Today, I am practicing how to pronounce $vocabulary clearly." to
                { meaning -> "Hôm nay, tôi đang luyện cách phát âm “$meaning” thật rõ ràng." }
        }

    fun continueSpeakingLesson() {
        if (!isSpeakingLessonReady || !isSpeakingQuestionAnswered || isSpeakingLessonComplete) return
        if (activeWordIndex < words.lastIndex) {
            activeWordIndex += 1
            isSpeakingQuestionAnswered = false
            feedback = ""
            notifyVocabularyChanged()
            return
        }

        isSpeakingLessonComplete = true
        val passed = speakingCorrectCount >= SPEAKING_REQUIRED_CORRECT
        wasSpeakingAttemptPassed = passed
        if (passed) {
            completedSpeakingLevels = completedSpeakingLevels + selectedSpeakingLevel
            unlockedSpeakingLevel = nextSequentialSpeakingLevel(completedSpeakingLevels)
            speakingPreferences?.edit()
                ?.putString(KEY_SPEAKING_COMPLETED, completedSpeakingLevels.sorted().joinToString(","))
                ?.apply()
            feedback = "Level complete! $speakingCorrectCount/$SPEAKING_QUESTIONS_PER_LEVEL pronounced ⭐"
        } else {
            feedback = "$speakingCorrectCount/$SPEAKING_QUESTIONS_PER_LEVEL completed. Try this level again!"
        }
        notifyVocabularyChanged()
    }

    private fun nextSequentialSpeakingLevel(completed: Set<Int>): Int {
        var next = 1
        while (next < SPEAKING_LEVEL_COUNT && next in completed) next += 1
        return next
    }

    fun initializeReadingProgress(context: Context) {
        readingPreferences = context.applicationContext.getSharedPreferences(
            READING_PREFERENCES,
            Context.MODE_PRIVATE
        )
        val saved = readingPreferences ?: return
        completedReadingLevels = saved.getString(KEY_READING_COMPLETED, "")
            .orEmpty()
            .split(',')
            .mapNotNull(String::toIntOrNull)
            .filter { it in 1..READING_LEVEL_COUNT }
            .toSet()
        unlockedReadingLevel = nextSequentialReadingLevel(completedReadingLevels)
        selectedReadingLevel = unlockedReadingLevel
    }

    fun isReadingLevelUnlocked(level: Int): Boolean = level in 1..unlockedReadingLevel

    fun isReadingLevelCompleted(level: Int): Boolean = level in completedReadingLevels

    fun selectReadingLevel(level: Int): Boolean {
        if (!isReadingLevelUnlocked(level)) return false
        selectedReadingLevel = level
        notifyVocabularyChanged()
        return true
    }

    fun startReadingLesson(level: Int = selectedReadingLevel): Boolean {
        if (!isReadingLevelUnlocked(level)) return false
        selectedReadingLevel = level
        val requestId = ++vocabularyRequestId
        isVocabularyLoading = true
        isReadingLessonReady = false
        isReadingLessonComplete = false
        wasReadingAttemptPassed = false
        readingQuestionCount = 0
        readingCorrectCount = 0
        activeWordIndex = 0
        selectedAnswer = null
        feedback = ""
        vocabularyMessage = "Loading Reading level $level..."
        notifyVocabularyChanged()

        VocabularyRepository.loadReadingLesson(level) { result ->
            Handler(Looper.getMainLooper()).post {
                if (requestId != vocabularyRequestId) return@post
                isVocabularyLoading = false
                result.onSuccess { vocabulary ->
                    words = vocabulary.mapIndexed { index, item ->
                        val distractors = vocabulary.asSequence()
                            .filter { it.wordEn != item.wordEn && it.meaningVi != item.meaningVi }
                            .map { it.meaningVi }
                            .distinct()
                            .take(READING_MAX_DISTRACTORS)
                            .toList()
                        val passage = readingPassage(level, item.wordEn)
                        Word(
                            english = item.wordEn,
                            vietnamese = item.meaningVi,
                            distractors = distractors,
                            scrambledLetters = item.wordEn.toList().shuffled(Random(index + level)),
                            phonetic = item.phonetic?.trim()?.takeIf(String::isNotEmpty),
                            pronunciationText = item.wordEn,
                            readingPassage = passage
                        )
                    }
                    isReadingLessonReady = words.size == READING_QUESTIONS_PER_LEVEL &&
                        words.all { it.distractors.size >= READING_MIN_DISTRACTORS }
                    vocabularyMessage = if (isReadingLessonReady) {
                        "Reading level $level · ${words.size} questions"
                    } else {
                        "Reading level $level needs 10 words and enough unique meanings for answer choices."
                    }
                }.onFailure { error ->
                    Log.w(TAG, "Failed to load Reading level $level", error)
                    isReadingLessonReady = false
                    vocabularyMessage = "Couldn't load Reading level $level. Check the backend and retry."
                }
                notifyVocabularyChanged()
            }
        }
        return true
    }

    private fun readingPassage(level: Int, vocabulary: String): String? = when (level) {
        in 1..15 -> null
        in 16..30 -> "Read the phrase carefully: “$vocabulary”."
        in 31..40 -> "Mia found the word “$vocabulary” in her book. She read the whole line slowly and looked for clues in the sentence."
        else -> "During the island's reading challenge, Mia came across the word “$vocabulary”. She reread the paragraph, noticed how the word was used, and then chose the meaning that best matched it."
    }

    fun continueReadingLesson() {
        if (!isReadingLessonReady || selectedAnswer == null || isReadingLessonComplete) return
        if (activeWordIndex < words.lastIndex) {
            activeWordIndex += 1
            selectedAnswer = null
            feedback = ""
            notifyVocabularyChanged()
            return
        }

        isReadingLessonComplete = true
        val passed = readingCorrectCount >= READING_REQUIRED_CORRECT
        wasReadingAttemptPassed = passed
        if (passed) {
            completedReadingLevels = completedReadingLevels + selectedReadingLevel
            unlockedReadingLevel = nextSequentialReadingLevel(completedReadingLevels)
            readingPreferences?.edit()
                ?.putString(KEY_READING_COMPLETED, completedReadingLevels.sorted().joinToString(","))
                ?.apply()
            feedback = "Level complete! $readingCorrectCount/$READING_QUESTIONS_PER_LEVEL correct ⭐"
        } else {
            feedback = "You got $readingCorrectCount/$READING_QUESTIONS_PER_LEVEL. Try this level again!"
        }
        notifyVocabularyChanged()
    }

    private fun nextSequentialReadingLevel(completed: Set<Int>): Int {
        var next = 1
        while (next < READING_LEVEL_COUNT && next in completed) next += 1
        return next
    }

    fun initializeWritingProgress(context: Context) {
        writingPreferences = context.applicationContext.getSharedPreferences(
            WRITING_PREFERENCES,
            Context.MODE_PRIVATE
        )
        val saved = writingPreferences ?: return
        completedWritingLevels = saved.getString(KEY_WRITING_COMPLETED, "")
            .orEmpty()
            .split(',')
            .mapNotNull(String::toIntOrNull)
            .filter { it in 1..WRITING_LEVEL_COUNT }
            .toSet()
        unlockedWritingLevel = nextSequentialWritingLevel(completedWritingLevels)
        selectedWritingLevel = unlockedWritingLevel
    }

    fun isWritingLevelUnlocked(level: Int): Boolean = level in 1..unlockedWritingLevel

    fun isWritingLevelCompleted(level: Int): Boolean = level in completedWritingLevels

    fun selectWritingLevel(level: Int): Boolean {
        if (!isWritingLevelUnlocked(level)) return false
        selectedWritingLevel = level
        writingAttemptLevel = level
        notifyVocabularyChanged()
        return true
    }

    fun startWritingLesson(level: Int = selectedWritingLevel): Boolean {
        if (!isWritingLevelUnlocked(level)) return false
        selectedWritingLevel = level
        writingAttemptLevel = level
        val requestId = ++vocabularyRequestId
        isVocabularyLoading = true
        isWritingLessonReady = false
        isWritingLessonComplete = false
        wasWritingAttemptPassed = false
        isWritingQuestionAnswered = false
        writingQuestionCount = 0
        writingCorrectCount = 0
        activeWordIndex = 0
        selectedAnswer = null
        selectedLetters = ""
        selectedLetterIndices = emptyList()
        spellingCorrect = false
        feedback = ""
        vocabularyMessage = "Loading Writing level $level..."
        notifyVocabularyChanged()

        VocabularyRepository.loadWritingLesson(level) { result ->
            Handler(Looper.getMainLooper()).post {
                if (requestId != vocabularyRequestId) return@post
                isVocabularyLoading = false
                result.onSuccess { vocabulary ->
                    words = vocabulary.mapIndexed { index, item ->
                        val baseWord = item.wordEn.substringAfterLast(' ').substringAfterLast('-')
                        val target = if (level >= 41) "I can read $baseWord" else item.wordEn
                        val hint = if (level >= 41) {
                            "Tôi có thể đọc ${item.meaningVi}"
                        } else {
                            item.meaningVi
                        }
                        Word(
                            english = target,
                            vietnamese = hint,
                            distractors = emptyList(),
                            scrambledLetters = target.filter(Char::isLetter)
                                .toList()
                                .shuffled(Random(index + level)),
                            phonetic = item.phonetic?.trim()?.takeIf(String::isNotEmpty),
                            pronunciationText = target
                        )
                    }
                    isWritingLessonReady = words.size == WRITING_QUESTIONS_PER_LEVEL
                    vocabularyMessage = if (isWritingLessonReady) {
                        "Writing level $level · ${words.size} spelling challenges"
                    } else {
                        "Writing level $level needs 10 unique words or phrases."
                    }
                }.onFailure { error ->
                    Log.w(TAG, "Failed to load Writing level $level", error)
                    isWritingLessonReady = false
                    vocabularyMessage = "Couldn't load Writing level $level. Check the backend and retry."
                }
                notifyVocabularyChanged()
            }
        }
        return true
    }

    fun continueWritingLesson() {
        if (!isWritingLessonReady || !isWritingQuestionAnswered || isWritingLessonComplete) return
        if (activeWordIndex < words.lastIndex) {
            activeWordIndex += 1
            selectedLetters = ""
            selectedLetterIndices = emptyList()
            spellingCorrect = false
            isWritingQuestionAnswered = false
            feedback = ""
            notifyVocabularyChanged()
            return
        }

        isWritingLessonComplete = true
        val passed = writingCorrectCount >= WRITING_REQUIRED_CORRECT
        wasWritingAttemptPassed = passed
        if (passed) {
            completedWritingLevels = completedWritingLevels + selectedWritingLevel
            unlockedWritingLevel = nextSequentialWritingLevel(completedWritingLevels)
            selectedWritingLevel = unlockedWritingLevel
            writingPreferences?.edit()
                ?.putString(KEY_WRITING_COMPLETED, completedWritingLevels.sorted().joinToString(","))
                ?.apply()
            feedback = "Level complete! $writingCorrectCount/$WRITING_QUESTIONS_PER_LEVEL correct ⭐"
        } else {
            feedback = "You got $writingCorrectCount/$WRITING_QUESTIONS_PER_LEVEL. Try this level again!"
        }
        notifyVocabularyChanged()
    }

    private fun nextSequentialWritingLevel(completed: Set<Int>): Int {
        var next = 1
        while (next < WRITING_LEVEL_COUNT && next in completed) next += 1
        return next
    }

    fun isListeningLevelUnlocked(level: Int): Boolean = level in 1..unlockedListeningLevel

    fun isListeningLevelCompleted(level: Int): Boolean = level in completedListeningLevels

    fun selectListeningLevel(level: Int): Boolean {
        if (!isListeningLevelUnlocked(level)) return false
        selectedListeningLevel = level
        notifyVocabularyChanged()
        return true
    }

    fun startListeningLesson(level: Int = selectedListeningLevel): Boolean {
        if (!isListeningLevelUnlocked(level)) return false
        selectedListeningLevel = level
        val requestId = ++vocabularyRequestId
        isVocabularyLoading = true
        isListeningLessonReady = false
        isListeningLessonComplete = false
        wasListeningAttemptPassed = false
        listeningQuestionCount = 0
        listeningCorrectCount = 0
        activeWordIndex = 0
        selectedAnswer = null
        feedback = ""
        vocabularyMessage = "Loading Listening level $level..."
        notifyVocabularyChanged()

        VocabularyRepository.loadListeningLesson(level) { result ->
            Handler(Looper.getMainLooper()).post {
                if (requestId != vocabularyRequestId) return@post
                isVocabularyLoading = false
                result.onSuccess { vocabulary ->
                    words = vocabulary.mapIndexed { index, item ->
                        Word(
                            english = item.wordEn,
                            vietnamese = item.meaningVi,
                            distractors = vocabulary.asSequence()
                                .filter { it.wordEn != item.wordEn }
                                .map { it.wordEn }
                                .distinct()
                                .take(listeningDistractorCount(level))
                                .toList(),
                            scrambledLetters = item.wordEn.toList().shuffled(Random(index + level))
                        )
                    }
                    isListeningLessonReady = words.size == LISTENING_QUESTIONS_PER_LEVEL
                    vocabularyMessage = if (isListeningLessonReady) {
                        "Listening level $level · ${words.size} questions"
                    } else {
                        "Level $level needs 10 unique listening questions."
                    }
                    selectedAnswer = null
                    activeWordIndex = 0
                }.onFailure { error ->
                    Log.w(TAG, "Failed to load Listening level $level", error)
                    isListeningLessonReady = false
                    vocabularyMessage = "Couldn't load Listening level $level. Check the backend and retry."
                }
                notifyVocabularyChanged()
            }
        }
        return true
    }

    fun continueListeningLesson() {
        if (!isListeningLessonReady || selectedAnswer == null || isListeningLessonComplete) return
        if (activeWordIndex < words.lastIndex) {
            activeWordIndex += 1
            selectedAnswer = null
            feedback = ""
            notifyVocabularyChanged()
            return
        }

        isListeningLessonComplete = true
        val passed = listeningCorrectCount >= LISTENING_REQUIRED_CORRECT
        wasListeningAttemptPassed = passed
        if (passed) {
            completedListeningLevels = completedListeningLevels + selectedListeningLevel
            unlockedListeningLevel = nextSequentialListeningLevel(completedListeningLevels)
            listeningPreferences?.edit()
                ?.putString(KEY_LISTENING_COMPLETED, completedListeningLevels.sorted().joinToString(","))
                ?.apply()
            feedback = "Level complete! ${listeningCorrectCount}/${LISTENING_QUESTIONS_PER_LEVEL} correct ⭐"
        } else {
            feedback = "You got ${listeningCorrectCount}/${LISTENING_QUESTIONS_PER_LEVEL}. Try this level again!"
        }
        notifyVocabularyChanged()
    }

    private fun nextSequentialListeningLevel(completed: Set<Int>): Int {
        var next = 1
        while (next < LISTENING_LEVEL_COUNT && next in completed) next += 1
        return next
    }

    private fun listeningDistractorCount(level: Int): Int =
        (3 + (level - 1) / 10).coerceIn(3, LISTENING_MAX_DISTRACTORS)

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
                    runnerPool = (words + runnerFallbackPool)
                        .distinctBy { it.english.lowercase() }
                    activeRunnerIndex = Random.nextInt(runnerPool.size)
                    prepareRunnerRound()
                    activeWordIndex = 0
                    selectedAnswer = null
                    selectedLetters = ""
                    selectedLetterIndices = emptyList()
                    spellingCorrect = false
                    matchedWords = mutableSetOf()
                    vocabularyMessage = "Connected · ${words.size} words · Level $level"
                }.onFailure { error ->
                    Log.w(TAG, "Failed to load vocabulary for level $level", error)
                    vocabularyMessage = "Offline sample words · backend unavailable"
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
        matchPreferences = context.applicationContext.getSharedPreferences(MATCH_PREFERENCES, Context.MODE_PRIVATE)
        val saved = matchPreferences ?: return
        val legacyLevel = saved.getInt(KEY_MATCH_LEVEL, 1).coerceIn(1, MATCH_LEVEL_COUNT)
        completedMatchLevels = saved.getString(KEY_COMPLETED_MATCH_LEVELS, "")
            .orEmpty()
            .split(',')
            .mapNotNull(String::toIntOrNull)
            .filter { it in 1..MATCH_LEVEL_COUNT }
            .toSet()
            .ifEmpty {
                if (legacyLevel > 1) (1 until legacyLevel).toSet() else emptySet()
            }
        unlockedMatchLevel = (completedMatchLevels.maxOrNull()?.plus(1) ?: 1)
            .coerceIn(1, MATCH_LEVEL_COUNT)
        matchGameLevel = unlockedMatchLevel
    }

    fun isMatchLevelUnlocked(level: Int): Boolean = level in 1..unlockedMatchLevel

    fun isMatchLevelCompleted(level: Int): Boolean = level in completedMatchLevels

    fun selectMatchLevel(context: Context, level: Int): Boolean {
        if (!isMatchLevelUnlocked(level)) return false
        matchGameLevel = level
        matchPairs = emptyList()
        matchStageLoadedFor = null
        loadMatchStage()
        return true
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
        matchPairs = emptyList()

        val levelOffset = (gameLevel - 1) * MATCH_PAIR_COUNT
        VocabularyRepository.loadMatchStage(levelOffset) { result ->
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
                        Log.w(TAG, "Failed to load Match level $gameLevel", it)
                        if (gameLevel == 1) offlineMatchWords else emptyList()
                    }
                )
                matchPairs = stageWords.take(MATCH_PAIR_COUNT)
                matchRightOrder = (0 until MATCH_PAIR_COUNT)
                    .shuffled(Random(gameLevel * 97 + 31))
                matchStageLoadedFor = gameLevel
                vocabularyMessage = if (result.isSuccess) {
                    "8 pairs · level $gameLevel"
                } else {
                    if (gameLevel == 1) {
                        "Offline sample · backend unavailable"
                    } else {
                        "Vocabulary unavailable · check your connection"
                    }
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
            if (matchStagePassed) {
                completedMatchLevels = completedMatchLevels + matchGameLevel
                unlockedMatchLevel = (completedMatchLevels.maxOrNull()?.plus(1) ?: 1)
                    .coerceAtMost(MATCH_LEVEL_COUNT)
                persistMatchProgress()
            }
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

    private fun persistMatchProgress() {
        matchPreferences?.edit()
            ?.putString(KEY_COMPLETED_MATCH_LEVELS, completedMatchLevels.sorted().joinToString(","))
            ?.putInt(KEY_MATCH_LEVEL, unlockedMatchLevel)
            ?.apply()
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

    fun selectAnswer(answer: String): Boolean {
        if (selectedAnswer != null) return selectedAnswer == answer
        selectedAnswer = answer
        return recordAnswer(answer == currentWord.vietnamese)
    }

    fun selectReadingAnswer(answer: String): Boolean {
        if (!isReadingLessonReady || isReadingLessonComplete || selectedAnswer != null) return false
        selectedAnswer = answer
        return recordAnswer(answer == currentWord.vietnamese, countReadingQuestion = true)
    }

    fun selectListeningAnswer(answer: String): Boolean {
        if (selectedAnswer != null) return selectedAnswer == answer
        selectedAnswer = answer
        return recordAnswer(answer == currentWord.english, countListeningQuestion = true)
    }

    private fun recordAnswer(
        correct: Boolean,
        countListeningQuestion: Boolean = false,
        countReadingQuestion: Boolean = false
    ): Boolean {
        if (correct) {
            score += 10
            user = user.copy(coins = user.coins + 5, wordsLearned = user.wordsLearned + 1)
            feedback = "Correct! +5 coins ⭐"
        } else {
            hearts = (hearts - 1).coerceAtLeast(0)
            feedback = "Almost! Try the next word."
        }
        if (countListeningQuestion && isListeningLessonReady && !isListeningLessonComplete) {
            listeningQuestionCount += 1
            if (correct) listeningCorrectCount += 1
        }
        if (countReadingQuestion && isReadingLessonReady && !isReadingLessonComplete) {
            readingQuestionCount += 1
            if (correct) readingCorrectCount += 1
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
        if (!isSpeakingLessonReady || isSpeakingLessonComplete || isSpeakingQuestionAnswered) return
        isSpeakingQuestionAnswered = true
        speakingQuestionCount += 1
        speakingCorrectCount += 1
        selectedAnswer = null
        score += 10
        user = user.copy(coins = user.coins + 5)
        feedback = "Speaking practice recorded!"
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
            (isWritingLessonReady && (isWritingQuestionAnswered || isWritingLessonComplete)) ||
            selectedLetters.length >= currentWord.english.count(Char::isLetter)
        ) return
        selectedLetterIndices = selectedLetterIndices + index
        selectedLetters = selectedLetterIndices.map { currentWord.scrambledLetters[it] }.joinToString("")
        spellingCorrect = false
        feedback = ""
    }

    fun removeLastLetter() {
        if (selectedLetterIndices.isNotEmpty() &&
            (!isWritingLessonReady || (!isWritingQuestionAnswered && !isWritingLessonComplete))
        ) {
            selectedLetterIndices = selectedLetterIndices.dropLast(1)
            selectedLetters = selectedLetterIndices.map { currentWord.scrambledLetters[it] }.joinToString("")
            spellingCorrect = false
        }
    }

    fun checkSpelling(): Boolean {
        if (isWritingLessonReady) {
            if (isWritingQuestionAnswered || isWritingLessonComplete ||
                selectedLetters.length != currentWord.english.count(Char::isLetter)
            ) return false
            val correct = selectedLetters.equals(
                currentWord.english.filter(Char::isLetter),
                ignoreCase = true
            )
            spellingCorrect = correct
            isWritingQuestionAnswered = true
            writingQuestionCount += 1
            if (correct) {
                writingCorrectCount += 1
                score += 15
                user = user.copy(coins = user.coins + 5, wordsLearned = user.wordsLearned + 1)
                feedback = "Amazing spelling! +5 coins"
            } else {
                feedback = "Not quite. Keep practicing!"
            }
            return correct
        }

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
        if (isWritingLessonReady && (isWritingQuestionAnswered || isWritingLessonComplete)) return
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

    private fun prepareRunnerRound() {
        runnerRoundId += 1
        val roundId = runnerRoundId
        runnerRoundResolved = false
        runnerLastAnswerCorrect = null
        runnerSecondsRemaining = RUNNER_ANSWER_SECONDS
        runnerLane = 1

        val answer = runnerCurrentWord.english
        val distractors = runnerPool.asSequence()
            .map { it.english }
            .filter { it != answer }
            .distinct()
            .take(2)
            .toList()
        runnerRoundOptions = (distractors + answer)
            .shuffled(Random(activeRunnerIndex * 31 + runnerDistance + 7))
        notifyVocabularyChanged()
        prepareRunnerCountdown(roundId)
    }

    private fun prepareRunnerCountdown(roundId: Int) {
        Handler(Looper.getMainLooper()).postDelayed({
            if (roundId != runnerRoundId || runnerRoundResolved || isRunnerGameOver) return@postDelayed
            runnerSecondsRemaining -= 1
            if (runnerSecondsRemaining <= 0) {
                runnerRoundId += 1
                runnerRoundResolved = true
                runnerLastAnswerCorrect = false
                hearts = (hearts - 1).coerceAtLeast(0)
                runnerSecondsRemaining = 0
                feedback = "Hết 10 giây! Không trả lời được — mất 1 mạng."
                if (isRunnerGameOver) completeGame("Vocabulary Run")
                notifyVocabularyChanged()
            } else {
                notifyVocabularyChanged()
                prepareRunnerCountdown(roundId)
            }
        }, 1_000)
    }

    fun moveRunner(lane: Int) {
        if (runnerRoundResolved || isRunnerGameOver || lane !in 0..2) return
        runnerRoundId += 1
        runnerSecondsRemaining = 0
        runnerLane = lane.coerceIn(0, 2)
        runnerDistance = (runnerDistance + 25).coerceAtMost(RUNNER_FINISH_DISTANCE)
        runnerBestDistance = maxOf(runnerBestDistance, runnerDistance)
        val correct = runnerLane == runnerCorrectLane
        runnerLastAnswerCorrect = correct
        runnerRoundResolved = true
        if (correct) {
            score += 10
            user = user.copy(coins = user.coins + 2)
            feedback = "Correct gate! +10 points"
            val milestone = listOf(100, 200, 300).firstOrNull {
                runnerDistance >= it && it !in runnerReachedMilestones
            }
            if (milestone != null) {
                runnerReachedMilestones = runnerReachedMilestones + milestone
                val reward = when (milestone) {
                    100 -> 5
                    200 -> 10
                    else -> 20
                }
                user = user.copy(coins = user.coins + reward)
                feedback = "Milestone $milestone m! +$reward coins 🎉"
            }
        } else {
            hearts = (hearts - 1).coerceAtLeast(0)
            feedback = "Wrong lane! The answer was ${runnerOptions[runnerCorrectLane]}."
        }
        if (isRunnerGameOver) completeGame("Vocabulary Run")
    }

    fun continueRunner() {
        if (!runnerRoundResolved || isRunnerGameOver) return
        var nextIndex = Random.nextInt(runnerPool.size)
        if (runnerPool.size > 1) {
            while (nextIndex == activeRunnerIndex) {
                nextIndex = Random.nextInt(runnerPool.size)
            }
        }
        activeRunnerIndex = nextIndex
        runnerRoundResolved = false
        runnerLastAnswerCorrect = null
        feedback = ""
        prepareRunnerRound()
    }

    fun resetRunnerGame() {
        runnerDistance = 0
        runnerLane = 1
        runnerReachedMilestones = emptySet()
        runnerPool = (runnerFallbackPool + words).distinctBy { it.english.lowercase() }
        activeRunnerIndex = Random.nextInt(runnerPool.size)
        prepareRunnerRound()
        runnerRoundResolved = false
        runnerLastAnswerCorrect = null
        activeWordIndex = 0
        hearts = 3
        feedback = ""
        if (lastGameScore?.gameName == "Vocabulary Run") lastGameScore = null
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
        private const val TAG = "MainViewModel"
        const val READING_LEVEL_COUNT = 50
        const val READING_QUESTIONS_PER_LEVEL = 10
        const val READING_REQUIRED_CORRECT = 8
        private const val READING_MIN_DISTRACTORS = 3
        private const val READING_MAX_DISTRACTORS = 7
        private const val READING_PREFERENCES = "kids_english_reading_progress"
        private const val KEY_READING_COMPLETED = "completed_levels"
        const val WRITING_LEVEL_COUNT = 50
        const val WRITING_QUESTIONS_PER_LEVEL = 10
        const val WRITING_REQUIRED_CORRECT = 8
        private const val WRITING_PREFERENCES = "kids_english_writing_progress"
        private const val KEY_WRITING_COMPLETED = "completed_levels"
        const val SPEAKING_LEVEL_COUNT = 50
        const val SPEAKING_QUESTIONS_PER_LEVEL = 10
        const val SPEAKING_REQUIRED_CORRECT = 8
        private const val SPEAKING_PREFERENCES = "kids_english_speaking_progress"
        private const val KEY_SPEAKING_COMPLETED = "completed_levels"
        const val LISTENING_LEVEL_COUNT = 50
        const val LISTENING_QUESTIONS_PER_LEVEL = 10
        const val LISTENING_REQUIRED_CORRECT = 8
        private const val LISTENING_MAX_DISTRACTORS = 7
        private const val LISTENING_PREFERENCES = "kids_english_listening_progress"
        private const val KEY_LISTENING_COMPLETED = "completed_levels"
        const val MATCH_LEVEL_COUNT = 50
        const val MATCH_PAIR_COUNT = 8
        const val MATCH_WRONG_FLASH_MILLIS = 650L
        private const val MATCH_PREFERENCES = "kids_english_match_progress"
        private const val KEY_MATCH_LEVEL = "match_game_level"
        private const val KEY_COMPLETED_MATCH_LEVELS = "completed_match_levels"
        private const val RUNNER_FINISH_DISTANCE = 300
        private const val RUNNER_ANSWER_SECONDS = 10
    }
}
