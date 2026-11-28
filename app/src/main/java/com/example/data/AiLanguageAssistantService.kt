package com.example.data

import android.util.Log
import com.example.model.AiCoachTurn
import com.example.model.LanguageExercise
import com.example.model.TranslationResult
import com.google.firebase.Firebase
import com.google.firebase.ai.GenerativeModel
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Content
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.json.JSONObject
import java.util.concurrent.ConcurrentHashMap

private const val AI_TAG = "AiLanguageService"
private const val AI_REQUEST_TIMEOUT_MS = 35_000L
private const val MAX_COACH_HISTORY_TURNS = 6
private const val MAX_HISTORY_TURN_CHARS = 450

/**
 * Clean abstraction for translation providers so UI layers never couple directly to a vendor SDK
 * and never fabricate translation outputs when requests fail.
 */
interface TranslationProvider {
    suspend fun translate(text: String, targetLanguage: String, sourceLanguageHint: String = "Auto-detect"): Result<TranslationResult>
}

class AiLanguageAssistantService(
    private val coachStreamOverride: ((
        userPrompt: String,
        targetLanguage: String,
        proficiencyLevel: String,
        mode: String,
        learnerUiLanguage: String,
        history: List<AiCoachTurn>
    ) -> Flow<String>)? = null
) : TranslationProvider {

    private val translationCache = ConcurrentHashMap<String, TranslationResult>()
    private val coachModelCache = ConcurrentHashMap<String, GenerativeModel>()

    private val coachGenerationConfig by lazy {
        generationConfig {
            temperature = 0.55f
            maxOutputTokens = 480
        }
    }

    private val coachModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.5-flash-lite",
            generationConfig = coachGenerationConfig
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
                maxOutputTokens = 420
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
                maxOutputTokens = 450
            }
        )
    }

    fun warmup() {
        if (coachStreamOverride != null) return
        try {
            coachModel
            translatorModel
        } catch (_: Throwable) {
            // Safe no-op when FirebaseApp is unavailable in isolated unit tests.
        }
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

    private fun buildCacheKey(text: String, targetLanguage: String, sourceLanguageHint: String): String {
        val cleanText = text.trim()
        val cleanTarget = targetLanguage.trim().ifBlank { "English" }
        val normalizedHint = if (isAutoDetectHint(sourceLanguageHint)) "Auto-detect" else sourceLanguageHint.trim()
        return "${normalizedHint.lowercase()}|${cleanTarget.lowercase()}|$cleanText"
    }

    fun getCachedTranslation(
        text: String,
        targetLanguage: String,
        sourceLanguageHint: String = "Auto-detect"
    ): TranslationResult? {
        val cleanText = text.trim()
        if (cleanText.isBlank()) return null
        return translationCache[buildCacheKey(cleanText, targetLanguage, sourceLanguageHint)]
    }

    private fun getOrCreateCoachModel(
        targetLanguage: String,
        proficiencyLevel: String,
        mode: String,
        learnerUiLanguage: String
    ): GenerativeModel {
        val levelText = proficiencyLevel.ifBlank { "Intermediate" }
        val cleanTarget = targetLanguage.trim().ifBlank { "English" }
        val cleanUi = learnerUiLanguage.trim().ifBlank { "English" }
        val key = "$mode|${cleanTarget.lowercase()}|${levelText.lowercase()}|${cleanUi.lowercase()}"
        return coachModelCache.getOrPut(key) {
            if (coachModelCache.size >= 12) {
                coachModelCache.clear()
            }
            val systemText = when (mode) {
                "grammar" -> "Concise bilingual $cleanTarget grammar coach ($levelText learner, UI $cleanUi). Show the corrected sentence, 2 short reason bullets, and 2 natural alternatives."
                "pronunciation" -> "Concise $cleanTarget speaking coach ($levelText). Give syllable breakdown, simple phonetics, stress tip, and 1 short practice phrase."
                "culture" -> "Concise $cleanTarget cultural guide. Explain etiquette, idioms, and conversational nuance clearly in 3-5 sentences."
                else -> "Warm, concise $cleanTarget tutor for a $levelText learner (UI $cleanUi). Answer naturally in 2-4 sentences, gently correct slips, and ask 1 short follow-up question."
            }
            Firebase.ai.generativeModel(
                modelName = "gemini-3.5-flash-lite",
                generationConfig = coachGenerationConfig,
                systemInstruction = content { text(systemText) }
            )
        }
    }

    internal fun buildBoundedChatHistory(history: List<AiCoachTurn>): List<Content> {
        return history
            .asSequence()
            .filter { !it.isStreaming && it.text.isNotBlank() }
            .toList()
            .takeLast(MAX_COACH_HISTORY_TURNS)
            .map { turn ->
                val clipped = turn.text.trim().let { raw ->
                    if (raw.length > MAX_HISTORY_TURN_CHARS) raw.take(MAX_HISTORY_TURN_CHARS) + "…" else raw
                }
                content(role = if (turn.isUser) "user" else "model") {
                    text(clipped)
                }
            }
    }

    fun streamCoachResponse(
        userPrompt: String,
        targetLanguage: String,
        proficiencyLevel: String,
        mode: String,
        learnerUiLanguage: String = "English",
        history: List<AiCoachTurn> = emptyList()
    ): Flow<String> {
        val override = coachStreamOverride
        if (override != null) {
            return override(userPrompt, targetLanguage, proficiencyLevel, mode, learnerUiLanguage, history)
        }
        return flow {
            val model = getOrCreateCoachModel(targetLanguage, proficiencyLevel, mode, learnerUiLanguage)
            val chatHistory = buildBoundedChatHistory(history)
            val accumulated = StringBuilder()
            val responseStream = if (chatHistory.isEmpty()) {
                model.generateContentStream(userPrompt)
            } else {
                model.startChat(history = chatHistory).sendMessageStream(userPrompt)
            }
            withTimeout(AI_REQUEST_TIMEOUT_MS) {
                responseStream.collect { chunk ->
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
        }.flowOn(Dispatchers.IO)
    }

    override suspend fun translate(
        text: String,
        targetLanguage: String,
        sourceLanguageHint: String
    ): Result<TranslationResult> = withContext(Dispatchers.IO) {
        val cleanText = text.trim()
        val cleanTarget = targetLanguage.trim().ifBlank { "English" }
        if (cleanText.isBlank()) return@withContext Result.failure(IllegalArgumentException("Text cannot be empty."))

        val normalizedHint = if (isAutoDetectHint(sourceLanguageHint)) "Auto-detect" else sourceLanguageHint.trim()
        val cacheKey = buildCacheKey(cleanText, cleanTarget, normalizedHint)
        translationCache[cacheKey]?.let { cached ->
            return@withContext Result.success(cached)
        }

        try {
            val prompt = buildString {
                append("Translate into $cleanTarget")
                if (normalizedHint != "Auto-detect") {
                    append(" (from $normalizedHint)")
                }
                append(". Preserve tone and emojis. Return JSON: detectedSourceLanguage (English), translatedText, concise pronunciationNote, usefulPhrase.\n\n")
                append(cleanText)
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
    ): Result<LanguageExercise> = withContext(Dispatchers.IO) {
        try {
            val prompt = "1 multiple-choice $targetLanguage quiz (${proficiencyLevel.ifBlank { "Intermediate" }}) on '$topic': 4 options, correctIndex (0-3), concise explanation for $learnerUiLanguage speakers, brief culturalTip."
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
                maxOutputTokens = 260
            }
        )
    }

    suspend fun suggestConversationReplies(
        recentMessages: List<String>,
        targetLanguage: String,
        learnerUiLanguage: String
    ): Result<List<String>> = withContext(Dispatchers.IO) {
        try {
            val contextBlock = recentMessages.takeLast(5).joinToString("\n")
            val prompt = buildString {
                append("Suggest 3 short natural replies in $targetLanguage (UI: $learnerUiLanguage):\n")
                append(contextBlock.ifBlank { "No prior messages — suggest friendly opening greetings." })
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
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val speakers = speakerNames.joinToString(", ").ifBlank { "Room Members" }
            val prompt = when (mode) {
                "game" -> "Host a quick 1-round voice party game for room '$roomTitle' ($topic) with $speakers in $uiLanguage (max 70 words)."
                "summary" -> "Write a concise friendly summary for voice room '$roomTitle' ($topic) with speakers: $speakers, in $uiLanguage (max 100 words)."
                else -> "Suggest 2 engaging icebreaker questions for voice room '$roomTitle' ($topic) in $uiLanguage (max 60 words)."
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
    ): Result<String> = withContext(Dispatchers.IO) {
        val clean = draft.trim()
        if (clean.isBlank()) return@withContext Result.failure(IllegalArgumentException("Write a short draft first."))
        try {
            val prompt = "Improve clarity and warmth for this '$category' post in its original language, add 2 relevant hashtags if appropriate, return ONLY the polished text:\n\n$clean"
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
