package com.example.englishapp.model

data class Word(
    val english: String,
    val vietnamese: String,
    val distractors: List<String>,
    val scrambledLetters: List<Char>
)
