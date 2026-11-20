package com.example.model

data class TranslationResult(
    val sourceText: String,
    val translatedText: String,
    val detectedSourceLanguage: String,
    val targetLanguage: String,
    val pronunciationNote: String = "",
    val usefulPhrase: String = "",
    val timestampMs: Long = System.currentTimeMillis()
)

data class LanguageExercise(
    val question: String,
    val targetLanguage: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String,
    val culturalTip: String = ""
)

data class AiCoachTurn(
    val id: String,
    val isUser: Boolean,
    val text: String,
    val isStreaming: Boolean = false
)

sealed interface AiTaskState<out T> {
    data object Idle : AiTaskState<Nothing>
    data object Loading : AiTaskState<Nothing>
    data class Streaming(val partialText: String) : AiTaskState<Nothing>
    data class Success<T>(val data: T) : AiTaskState<T>
    data class Error(val message: String) : AiTaskState<Nothing>
}
