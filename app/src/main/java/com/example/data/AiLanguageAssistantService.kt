package com.example.data

import android.util.Log
import com.example.model.LanguageExercise
import com.example.model.TranslationResult
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withTimeout
import org.json.JSONObject
import java.util.concurrent.ConcurrentHashMap

private const val AI_TAG = "AiLanguageService"
private const val AI_REQUEST_TIMEOUT_MS = 35_000L

/**
 * Clean abstraction for translation providers so UI layers never couple directly to a vendor SDK
 * and never fabricate translation outputs when requests fail.
 */
interface TranslationProvider {
    suspend fun translate(text: String, targetLanguage: String, sourceLanguageHint: String = "Auto-detect"): Result<TranslationResult>
}

class AiLanguageAssistantService : TranslationProvider {

    private val translationCache = ConcurrentHashMap<String, TranslationResult>()

    private val coachModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.5-flash-lite",
            generationConfig = generationConfig {
                temperature = 0.6f
                maxOutputTokens = 700
            }
        )
    }

    private val translationSchema = Schema.obj(
        properties = mapOf(
            "detectedSourceLanguage" to Schema.string(description = "Detected source language name in English"),
            "translatedText" to Schema.string(description = "Accurate natural translation into targetLanguage"),
            "pronunciationNote" to Schema.string(description = "Short phonetic/romanization or pronunciation tip"),
            "usefulPhrase" to Schema.string(description = "One natural idiom or alternative phrasing in targetLanguage")
        ),
        optionalProperties = listOf("pronunciationNote", "usefulPhrase")
    )

    private val translatorModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.5-flash-lite",
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = translationSchema
                temperature = 0.2f
                maxOutputTokens = 500
            }
        )
    }

    private val exerciseSchema = Schema.obj(
        properties = mapOf(
            "question" to Schema.string(description = "Interactive language or grammar practice question"),
            "options" to Schema.array(items = Schema.string(), description = "Exactly 4 plausible multiple-choice options"),
            "correctIndex" to Schema.integer(description = "Zero-based index (0 to 3) of the correct option"),
            "explanation" to Schema.string(description = "Clear concise grammar or vocabulary explanation"),
            "culturalTip" to Schema.string(description = "Brief cultural context note related to the phrase")
        ),
        optionalProperties = listOf("culturalTip")
    )

    private val exerciseModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.5-flash-lite",
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = exerciseSchema
                temperature = 0.5f
                maxOutputTokens = 500
            }
        )
    }

    private fun parseJsonSafely(rawText: String): JSONObject {
        val cleaned = rawText
            .trim()
            .removePrefix("```json")
            .removePrefix("```JSON")
            .removePrefix("```")
            .removeSuffix("```")
            .trim()
        val firstBrace = cleaned.indexOf('{')
        val lastBrace = cleaned.lastIndexOf('}')
        val jsonSlice = if (firstBrace >= 0 && lastBrace > firstBrace) {
            cleaned.substring(firstBrace, lastBrace + 1)
        } else {
            cleaned
        }
        return JSONObject(jsonSlice)
    }

    private fun JSONObject.optCleanString(key: String, fallback: String = ""): String {
        if (!has(key) || isNull(key)) return fallback
        val v = optString(key, "").trim()
        return if (v.isEmpty() || v.equals("null", ignoreCase = true)) fallback else v
    }

    private fun isAutoDetectHint(hint: String): Boolean {
        val clean = hint.trim().lowercase()
        return clean.isBlank() ||
            clean == "auto" ||
            clean == "auto-detect" ||
            clean == "تشخیص خودکار" ||
            clean == "اتومات پیژندنه"
    }

    fun streamCoachResponse(
        userPrompt: String,
        targetLanguage: String,
        proficiencyLevel: String,
        mode: String,
        learnerUiLanguage: String = "English"
    ): Flow<String> = flow {
        val levelText = proficiencyLevel.ifBlank { "Intermediate" }
        val systemInstruction = when (mode) {
            "grammar" -> "You are an encouraging bilingual language coach. Correct grammar mistakes gently, show the corrected sentence, explain why in brief bullet points (in $learnerUiLanguage or matching the user's message language), and give 2 natural alternatives in $targetLanguage."
            "pronunciation" -> "You are a pronunciation and speaking coach for $targetLanguage. Provide syllable breakdown, IPA/simple phonetic guide, stress tips, and a short practice tongue-twister or dialogue."
            "culture" -> "You are an international cultural exchange guide. Explain cultural etiquette, idioms, and conversational nuances related to $targetLanguage clearly."
            else -> "You are a warm, practical language exchange tutor helping a $levelText learner practice $targetLanguage. Respond naturally, correct small slips kindly, and ask a follow-up question."
        }
        val prompt = "$systemInstruction\n\nLearner request:\n$userPrompt"
        val accumulated = StringBuilder()
        withTimeout(AI_REQUEST_TIMEOUT_MS) {
            coachModel.generateContentStream(prompt).collect { chunk ->
                val part = chunk.text.orEmpty()
                if (part.isNotEmpty()) {
                    accumulated.append(part)
                    emit(accumulated.toString())
                }
            }
        }
        if (accumulated.isBlank()) {
            throw IllegalStateException("AI Coach returned an empty response.")
        }
    }

    override suspend fun translate(
        text: String,
        targetLanguage: String,
        sourceLanguageHint: String
    ): Result<TranslationResult> {
        val cleanText = text.trim()
        val cleanTarget = targetLanguage.trim().ifBlank { "English" }
        if (cleanText.isBlank()) return Result.failure(IllegalArgumentException("Text cannot be empty."))

        val normalizedHint = if (isAutoDetectHint(sourceLanguageHint)) "Auto-detect" else sourceLanguageHint.trim()
        val cacheKey = "${normalizedHint.lowercase()}|${cleanTarget.lowercase()}|$cleanText"
        translationCache[cacheKey]?.let { cached ->
            return Result.success(cached)
        }

        return try {
            val prompt = buildString {
                append("Translate the following text accurately into $cleanTarget.\n")
                if (normalizedHint != "Auto-detect") {
                    append("Source language hint: $normalizedHint.\n")
                } else {
                    append("Automatically detect the source language.\n")
                }
                append("Preserve emojis, tone, and meaning. Return ONLY JSON matching the schema with: ")
                append("detectedSourceLanguage (in English), translatedText (never empty), concise pronunciationNote (romanization/phonetics), and one natural usefulPhrase.\n\n")
                append("Text:\n$cleanText")
            }

            val response = withTimeout(AI_REQUEST_TIMEOUT_MS) {
                translatorModel.generateContent(prompt)
            }
            val rawJson = response.text ?: throw IllegalStateException("Empty translation response from AI service.")
            val json = parseJsonSafely(rawJson)
            val translatedText = json.optCleanString("translatedText")
            if (translatedText.isBlank()) {
                throw IllegalStateException("Translation returned empty output.")
            }

            val result = TranslationResult(
                sourceText = cleanText,
                translatedText = translatedText,
                detectedSourceLanguage = json.optCleanString("detectedSourceLanguage", normalizedHint),
                targetLanguage = cleanTarget,
                pronunciationNote = json.optCleanString("pronunciationNote", ""),
                usefulPhrase = json.optCleanString("usefulPhrase", "")
            )
            if (translationCache.size >= 200) {
                translationCache.clear()
            }
            translationCache[cacheKey] = result
            Result.success(result)
        } catch (e: TimeoutCancellationException) {
            Log.e(AI_TAG, "Translation timed out", e)
            Result.failure(IllegalStateException("Translation timed out. Please check your connection and try again."))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(AI_TAG, "Translation failed", e)
            Result.failure(e)
        }
    }

    suspend fun generatePracticeExercise(
        targetLanguage: String,
        proficiencyLevel: String,
        topic: String = "Daily Conversation",
        learnerUiLanguage: String = "English"
    ): Result<LanguageExercise> {
        return try {
            val prompt = buildString {
                append("Create 1 engaging multiple-choice $targetLanguage exercise for a ${proficiencyLevel.ifBlank { "Intermediate" }} learner ")
                append("focused on '$topic'. Provide 4 clear options, correctIndex (0-3), explanation (clear for $learnerUiLanguage speakers), and a brief culturalTip.")
            }
            val response = withTimeout(AI_REQUEST_TIMEOUT_MS) {
                exerciseModel.generateContent(prompt)
            }
            val rawJson = response.text ?: throw IllegalStateException("Empty exercise response from AI service.")
            val json = parseJsonSafely(rawJson)
            val optsArray = json.optJSONArray("options") ?: throw IllegalStateException("Exercise options missing.")
            val options = (0 until optsArray.length()).mapNotNull { i ->
                optsArray.optString(i, "").trim().takeIf { it.isNotEmpty() && !it.equals("null", ignoreCase = true) }
            }
            require(options.size >= 2) { "Exercise options missing" }
            val idx = json.optInt("correctIndex", 0).coerceIn(0, options.lastIndex)
            val questionText = json.optCleanString("question")
            require(questionText.isNotBlank()) { "Exercise question missing" }

            Result.success(
                LanguageExercise(
                    question = questionText,
                    targetLanguage = targetLanguage,
                    options = options,
                    correctIndex = idx,
                    explanation = json.optCleanString("explanation", "Practice regularly for best results."),
                    culturalTip = json.optCleanString("culturalTip", "")
                )
            )
        } catch (e: TimeoutCancellationException) {
            Log.e(AI_TAG, "Exercise generation timed out", e)
            Result.failure(IllegalStateException("Exercise generation timed out. Please try again."))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(AI_TAG, "Exercise generation failed", e)
            Result.failure(e)
        }
    }

    private val suggestionsSchema = Schema.obj(
        properties = mapOf(
            "suggestions" to Schema.array(
                items = Schema.string(),
                description = "Exactly 3 short natural conversational reply suggestions"
            )
        )
    )

    private val suggestionsModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.5-flash-lite",
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = suggestionsSchema
                temperature = 0.7f
                maxOutputTokens = 300
            }
        )
    }

    suspend fun suggestConversationReplies(
        recentMessages: List<String>,
        targetLanguage: String,
        learnerUiLanguage: String
    ): Result<List<String>> {
        return try {
            val contextBlock = recentMessages.takeLast(5).joinToString("\n")
            val prompt = buildString {
                append("Based on the recent chat messages below, suggest 3 natural, friendly, short replies ")
                append("written in $targetLanguage (appropriate for a user whose UI language is $learnerUiLanguage).\n\n")
                append("Recent messages:\n")
                append(contextBlock.ifBlank { "No prior messages yet — suggest friendly opening greetings." })
            }
            val response = withTimeout(AI_REQUEST_TIMEOUT_MS) {
                suggestionsModel.generateContent(prompt)
            }
            val raw = response.text ?: throw IllegalStateException("Empty reply suggestions from AI.")
            val json = parseJsonSafely(raw)
            val arr = json.optJSONArray("suggestions") ?: throw IllegalStateException("Missing suggestions array.")
            val items = (0 until arr.length())
                .mapNotNull { i -> arr.optString(i, "").trim().takeIf { it.isNotEmpty() } }
                .take(3)
            require(items.isNotEmpty()) { "No reply suggestions returned." }
            Result.success(items)
        } catch (e: TimeoutCancellationException) {
            Result.failure(IllegalStateException("Smart reply request timed out."))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(AI_TAG, "Smart reply suggestion failed", e)
            Result.failure(e)
        }
    }

    suspend fun generateRoomAssistantOutput(
        roomTitle: String,
        topic: String,
        speakerNames: List<String>,
        mode: String,
        uiLanguage: String
    ): Result<String> {
        return try {
            val speakers = speakerNames.joinToString(", ").ifBlank { "Room Members" }
            val prompt = when (mode) {
                "game" -> "Host a quick, fun 1-round voice party game (e.g. Two Truths & A Lie prompt, Rapid Word Association, or Story Chain) tailored for room '$roomTitle' ($topic) with speakers $speakers. Keep it energetic, clear, and written in $uiLanguage (max 80 words)."
                "summary" -> "Write a structured, friendly session summary and key takeaways for the live voice room '$roomTitle' ($topic) featuring speakers: $speakers. Write in $uiLanguage (max 120 words)."
                else -> "Suggest 2 engaging conversation icebreaker questions for voice room '$roomTitle' ($topic) in $uiLanguage (max 70 words)."
            }
            val response = withTimeout(AI_REQUEST_TIMEOUT_MS) {
                coachModel.generateContent(prompt)
            }
            val text = response.text?.trim().orEmpty()
            require(text.isNotBlank()) { "AI room assistant returned empty output." }
            Result.success(text)
        } catch (e: TimeoutCancellationException) {
            Result.failure(IllegalStateException("AI room assistant timed out."))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(AI_TAG, "Room assistant failed", e)
            Result.failure(e)
        }
    }

    suspend fun polishPostDraft(
        draft: String,
        category: String,
        uiLanguage: String
    ): Result<String> {
        val clean = draft.trim()
        if (clean.isBlank()) return Result.failure(IllegalArgumentException("Write a short draft first."))
        return try {
            val prompt = "Improve clarity, warmth, and engagement for this social community post in category '$category'. Keep the author's original language and meaning, add 2 relevant hashtags if appropriate, and return ONLY the polished post text without quotes or commentary:\n\n$clean"
            val response = withTimeout(AI_REQUEST_TIMEOUT_MS) {
                coachModel.generateContent(prompt)
            }
            val text = response.text?.trim().orEmpty()
            require(text.isNotBlank()) { "AI writing assistant returned empty output." }
            Result.success(text)
        } catch (e: TimeoutCancellationException) {
            Result.failure(IllegalStateException("AI writing assistant timed out."))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(AI_TAG, "Post polish failed", e)
            Result.failure(e)
        }
    }
}
