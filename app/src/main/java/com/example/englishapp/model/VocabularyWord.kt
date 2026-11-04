package com.example.englishapp.model

data class VocabularyWord(
    val wordEn: String,
    val meaningVi: String,
    val level: Int,
    val category: String,
    val phonetic: String?,
    val audioUrl: String?,
    val imageUrl: String?
)
