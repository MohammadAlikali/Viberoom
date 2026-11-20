package com.example.model

import com.example.ui.localization.AppLanguage
import com.example.ui.localization.AppStrings

enum class LanguageProficiencyRank(
    val rank: Int,
    val cefr: String,
    val labelKey: String,
    val shortKey: String
) {
    RANK_1(1, "A1", "rank_1_label", "rank_1_short"),
    RANK_2(2, "A2", "rank_2_label", "rank_2_short"),
    RANK_3(3, "B1", "rank_3_label", "rank_3_short"),
    RANK_4(4, "B2", "rank_4_label", "rank_4_short"),
    RANK_5(5, "C1-C2", "rank_5_label", "rank_5_short");

    companion object {
        fun fromRank(rank: Int?): LanguageProficiencyRank =
            entries.firstOrNull { it.rank == rank } ?: RANK_1

        fun fromLegacyString(raw: String?): LanguageProficiencyRank {
            val clean = raw.orEmpty().lowercase()
            return when {
                "5" in clean || "fluent" in clean || "c1" in clean || "c2" in clean || "روان" in clean -> RANK_5
                "4" in clean || "upper" in clean || "b2" in clean || "پیشرفته" in clean -> RANK_4
                "3" in clean || "inter" in clean || "b1" in clean || "متوسط" in clean -> RANK_3
                "2" in clean || "elem" in clean || "a2" in clean || "پایه" in clean -> RANK_2
                else -> RANK_1
            }
        }
    }
}

data class CatalogLanguage(
    val id: String,
    val canonicalName: String,
    val nativeName: String,
    val flagEmoji: String
)

data class CatalogInterest(
    val canonicalName: String,
    val emoji: String,
    val categoryKey: String
)

object VibeCatalogs {
    val languages: List<CatalogLanguage> = listOf(
        CatalogLanguage("fa_af", "Dari / Persian", "دری / فارسی", "🇦🇫"),
        CatalogLanguage("ps", "Pashto", "پښتو", "🇦🇫"),
        CatalogLanguage("en", "English", "English", "🇺🇸"),
        CatalogLanguage("ar", "Arabic", "العربية", "🇸🇦"),
        CatalogLanguage("tr", "Turkish", "Türkçe", "🇹🇷"),
        CatalogLanguage("ur", "Urdu", "اردو", "🇵🇰"),
        CatalogLanguage("de", "German", "Deutsch", "🇩🇪"),
        CatalogLanguage("fr", "French", "Français", "🇫🇷"),
        CatalogLanguage("es", "Spanish", "Español", "🇪🇸"),
        CatalogLanguage("hi", "Hindi", "हिन्दी", "🇮🇳"),
        CatalogLanguage("ru", "Russian", "Русский", "🇷🇺"),
        CatalogLanguage("zh", "Chinese", "中文", "🇨🇳"),
        CatalogLanguage("ja", "Japanese", "日本語", "🇯🇵"),
        CatalogLanguage("ko", "Korean", "한국어", "🇰🇷"),
        CatalogLanguage("it", "Italian", "Italiano", "🇮🇹"),
        CatalogLanguage("pt", "Portuguese", "Português", "🇧🇷"),
        CatalogLanguage("uz", "Uzbek", "Oʻzbek", "🇺🇿"),
        CatalogLanguage("nl", "Dutch", "Nederlands", "🇳🇱"),
        CatalogLanguage("sv", "Swedish", "Svenska", "🇸🇪"),
        CatalogLanguage("id", "Indonesian", "Bahasa Indonesia", "🇮🇩")
    )

    val translateTargetLanguages: List<String> = listOf(
        "Persian (Dari)",
        "Pashto",
        "English",
        "Arabic",
        "Turkish",
        "Urdu",
        "German",
        "French",
        "Spanish",
        "Russian",
        "Chinese",
        "Japanese",
        "Korean"
    )

    val interests: List<CatalogInterest> = listOf(
        CatalogInterest("Languages", "🗣️", "interest_cat_culture"),
        CatalogInterest("Culture", "🌍", "interest_cat_culture"),
        CatalogInterest("Travel", "✈️", "interest_cat_lifestyle"),
        CatalogInterest("History", "🏛️", "interest_cat_culture"),
        CatalogInterest("Books", "📚", "interest_cat_culture"),
        CatalogInterest("Storytelling", "✨", "interest_cat_culture"),
        CatalogInterest("Music", "🎵", "interest_cat_media"),
        CatalogInterest("Podcasts", "🎙️", "interest_cat_media"),
        CatalogInterest("Photography", "📸", "interest_cat_media"),
        CatalogInterest("Art", "🎨", "interest_cat_media"),
        CatalogInterest("Design", "🖌️", "interest_cat_media"),
        CatalogInterest("Cinema", "🎬", "interest_cat_media"),
        CatalogInterest("Technology", "💻", "interest_cat_tech"),
        CatalogInterest("Coding", "🧑‍💻", "interest_cat_tech"),
        CatalogInterest("AI & Startups", "🚀", "interest_cat_tech"),
        CatalogInterest("Science", "🔬", "interest_cat_tech"),
        CatalogInterest("Gaming", "🎮", "interest_cat_tech"),
        CatalogInterest("Coffee Chat", "☕", "interest_cat_lifestyle"),
        CatalogInterest("Food & Cooking", "🍲", "interest_cat_lifestyle"),
        CatalogInterest("Sports", "⚽", "interest_cat_lifestyle"),
        CatalogInterest("Fitness", "🏃", "interest_cat_lifestyle"),
        CatalogInterest("Poetry", "🪶", "interest_cat_culture"),
        CatalogInterest("Architecture", "🕌", "interest_cat_media"),
        CatalogInterest("Voice Rooms", "🎧", "interest_cat_media")
    )

    val roomTags: List<String> = listOf(
        "Dari / Persian",
        "Pashto",
        "English",
        "Arabic",
        "Turkish",
        "Pronunciation",
        "Daily Practice",
        "Cultural Exchange",
        "Storytelling",
        "Music & Chill",
        "Tech & AI",
        "Q&A"
    )

    fun defaultTranslateLanguageFor(appLanguage: AppLanguage): String = when (appLanguage) {
        AppLanguage.DARI_PERSIAN -> "Persian (Dari)"
        AppLanguage.PASHTO -> "Pashto"
        AppLanguage.ENGLISH -> "English"
    }

    fun rankStars(rank: Int): String {
        val clamped = rank.coerceIn(1, 5)
        return "★".repeat(clamped) + "☆".repeat(5 - clamped)
    }

    fun rankShortLabel(rank: Int, appLanguage: AppLanguage): String {
        val entry = LanguageProficiencyRank.fromRank(rank)
        return "${entry.cefr} • ${AppStrings.t(entry.shortKey, appLanguage)}"
    }

    fun summarizeProficiency(levels: Map<String, Int>, appLanguage: AppLanguage): String {
        val topRank = levels.values.maxOrNull() ?: return ""
        val entry = LanguageProficiencyRank.fromRank(topRank)
        return "${entry.cefr} (${AppStrings.t(entry.shortKey, appLanguage)})"
    }

    fun findLanguage(name: String): CatalogLanguage? {
        val clean = name.trim().lowercase()
        return languages.firstOrNull {
            it.canonicalName.lowercase() == clean ||
                it.nativeName.lowercase() == clean ||
                (clean.contains("dari") && it.id == "fa_af") ||
                (clean.contains("persian") && it.id == "fa_af") ||
                (clean.contains("pashto") && it.id == "ps")
        }
    }

    fun levelFromXp(xp: Int): Int = (xp.coerceAtLeast(0) / 100) + 1

    fun xpProgressInLevel(xp: Int): Float = ((xp.coerceAtLeast(0) % 100) / 100f).coerceIn(0f, 1f)

    /**
     * Computes deterministic Social Intelligence Vibe DNA match percentage (0..99)
     * from shared interests, complementary native/spoken/learning languages, and cultural diversity.
     */
    fun computeVibeMatchPercent(me: UserProfile, other: UserProfile): Int {
        if (me.id == other.id) return 100
        val myInterests = me.interests.map { it.trim().lowercase() }.filter { it.isNotEmpty() }.toSet()
        val otherInterests = other.interests.map { it.trim().lowercase() }.filter { it.isNotEmpty() }.toSet()
        val sharedInterests = myInterests.intersect(otherInterests).size

        val myLearning = me.learningLanguages.map { it.trim().lowercase() }.toSet()
        val myNative = (me.nativeLanguages + me.languagesSpoken).map { it.trim().lowercase() }.toSet()
        val otherLearning = other.learningLanguages.map { it.trim().lowercase() }.toSet()
        val otherNative = (other.nativeLanguages + other.languagesSpoken).map { it.trim().lowercase() }.toSet()

        val mutualLanguageExchanges =
            myLearning.intersect(otherNative).size + otherLearning.intersect(myNative).size
        val sharedSpoken = myNative.intersect(otherNative).size

        var score = 48
        score += (sharedInterests * 9).coerceAtMost(27)
        score += (mutualLanguageExchanges * 12).coerceAtMost(20)
        score += (sharedSpoken * 4).coerceAtMost(8)
        if (me.countryCode.isNotBlank() && other.countryCode.isNotBlank() &&
            !me.countryCode.equals(other.countryCode, ignoreCase = true)
        ) {
            score += 4 // Cross-cultural discovery bonus
        }
        return score.coerceIn(50, 99)
    }
}
