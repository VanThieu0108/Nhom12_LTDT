package com.example.englishapp.model

data class User(
    val explorerName: String,
    val level: Int = 1,
    val experience: Int = 0,
    val coins: Int = 150,
    val stamina: Int = 5,
    val streakDays: Int = 3,
    val wordsLearned: Int = 24,
    val perfectScores: Int = 8,
    val rank: Int = 12
)
