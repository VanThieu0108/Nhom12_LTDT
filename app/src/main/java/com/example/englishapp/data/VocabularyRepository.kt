package com.example.englishapp.data

import com.example.englishapp.BuildConfig
import com.example.englishapp.model.VocabularyWord
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.concurrent.Executors

object VocabularyRepository {
    private val executor = Executors.newSingleThreadExecutor()
    private val baseUrl = BuildConfig.VOCABULARY_API_BASE_URL.trimEnd('/')

    fun loadLevel(level: Int, callback: (Result<Pair<List<Int>, List<VocabularyWord>>>) -> Unit) {
        executor.execute {
            val result = runCatching {
                val levels = fetchLevels()
                if (level !in levels) {
                    throw IOException("Backend không có từ vựng cho cấp độ $level.")
                }
                levels to fetchWords(level)
            }
            callback(result)
        }
    }

    private fun fetchLevels(): List<Int> {
        val response = request("/api/v1/vocabulary/levels")
        val rows = response.getJSONArray("data")
        return (0 until rows.length())
            .map { rows.getJSONObject(it).getInt("level") }
            .distinct()
            .sorted()
            .also {
                if (it.isEmpty()) throw IOException("Backend chưa có cấp độ từ vựng nào.")
            }
    }

    private fun fetchWords(level: Int): List<VocabularyWord> {
        val encodedLevel = URLEncoder.encode(level.toString(), Charsets.UTF_8.name())
        val response = request("/api/v1/vocabulary?level=$encodedLevel&limit=50")
        val rows = response.getJSONArray("data")
        val words = (0 until rows.length())
            .map { index ->
                val row = rows.getJSONObject(index)
                VocabularyWord(
                    wordEn = row.getString("word_en").trim(),
                    meaningVi = row.getString("meaning_vi").trim(),
                    level = row.getInt("level"),
                    category = row.getString("category"),
                    phonetic = row.optionalString("phonetic"),
                    audioUrl = row.optionalString("audio_url"),
                    imageUrl = row.optionalString("image_url")
                )
            }
            .filter { it.wordEn.matches(Regex("[A-Za-z]{2,12}")) && it.meaningVi.isNotBlank() }
            .distinctBy { it.wordEn.lowercase() }
        if (words.size < 4) {
            throw IOException("Cấp độ $level cần ít nhất 4 từ ngắn để tạo lựa chọn trong trò chơi.")
        }
        return words
    }

    private fun request(path: String): JSONObject {
        val connection = URL("$baseUrl$path").openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.connectTimeout = 8_000
        connection.readTimeout = 8_000
        connection.setRequestProperty("Accept", "application/json")
        return try {
            val status = connection.responseCode
            val stream = if (status in 200..299) connection.inputStream else connection.errorStream
            val body = stream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
            if (status !in 200..299) {
                val message = runCatching {
                    JSONObject(body).getJSONObject("error").getString("message")
                }.getOrNull()
                throw IOException(message ?: "API từ vựng trả về HTTP $status.")
            }
            JSONObject(body)
        } finally {
            connection.disconnect()
        }
    }

    private fun JSONObject.optionalString(key: String): String? =
        optString(key).takeUnless { it.isBlank() || it == "null" }
}
