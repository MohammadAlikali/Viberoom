package com.example.model

enum class GiftCategory(val labelEn: String, val labelFa: String, val labelPs: String, val emoji: String) {
    BASIC("Basic", "پایه", "بنسټیز", "🌹"),
    PREMIUM("Premium", "ویژه", "غوره", "💎"),
    VEHICLE("Vehicles", "وسایل نقلیه", "موټرې", "🏎️"),
    FANTASY("Fantasy", "فانتزی", "افسانوي", "🐉"),
    SPECIAL("Special", "شاهکار", "ځانګړي", "✨")
}

enum class GiftAnimationStyle {
    PETALS_BURST,
    CROWN_GLOW,
    LUXURY_CAR_DRIVE,
    MOTORCYCLE_DRIFT,
    HELICOPTER_FLYOVER,
    AIRPLANE_FLYBY,
    SPACESHIP_WARP,
    DRAGON_FLAME,
    PORTAL_VORTEX,
    FULLSCREEN_LEGENDARY
}

data class VoiceGift(
    val id: String,
    val nameEn: String,
    val nameFa: String,
    val namePs: String,
    val emoji: String,
    val category: GiftCategory,
    val xpCost: Int,
    val isFullScreen: Boolean,
    val priority: Int,
    val style: GiftAnimationStyle,
    val primaryColorHex: Long,
    val secondaryColorHex: Long,
    val soundEffectId: String
)

object VoiceGiftCatalog {
    val gifts: List<VoiceGift> = listOf(
        // A. BASIC (Free & low earned points)
        VoiceGift("rose", "Rose", "گل رز", "ګلاب", "🌹", GiftCategory.BASIC, 0, false, 1, GiftAnimationStyle.PETALS_BURST, 0xFFF43F5E, 0xFFFB7185, "gift_sparkle"),
        VoiceGift("heart", "Neon Heart", "قلب نئونی", "نیون زړه", "❤️", GiftCategory.BASIC, 0, false, 1, GiftAnimationStyle.PETALS_BURST, 0xFFEC4899, 0xFFF472B6, "gift_sparkle"),
        VoiceGift("flowers", "Bouquet", "دسته گل", "ګلدان", "💐", GiftCategory.BASIC, 0, false, 1, GiftAnimationStyle.PETALS_BURST, 0xFFA855F7, 0xFFF472B6, "gift_sparkle"),
        VoiceGift("star", "Shooting Star", "ستاره درخشان", "ځلانده ستوری", "⭐", GiftCategory.BASIC, 5, false, 1, GiftAnimationStyle.CROWN_GLOW, 0xFFFACC15, 0xFFF59E0B, "gift_sparkle"),
        VoiceGift("clap", "Thunder Clap", "تشویق پرشور", "ګرمې چکچکې", "👏", GiftCategory.BASIC, 0, false, 1, GiftAnimationStyle.PETALS_BURST, 0xFF38BDF8, 0xFF818CF8, "applause"),
        VoiceGift("balloons", "Party Balloons", "بادکنک جشن", "د خوښۍ پوقانې", "🎈", GiftCategory.BASIC, 5, false, 1, GiftAnimationStyle.PETALS_BURST, 0xFF22D3EE, 0xFFF472B6, "celebration"),

        // B. PREMIUM
        VoiceGift("diamond", "Cosmic Diamond", "الماس کیهانی", "کیهاني الماس", "💎", GiftCategory.PREMIUM, 15, false, 3, GiftAnimationStyle.CROWN_GLOW, 0xFF22D3EE, 0xFF3B82F6, "gift_crystal"),
        VoiceGift("gold", "Pure Gold", "شمش طلا", "سوچه سره زر", "🪙", GiftCategory.PREMIUM, 20, false, 3, GiftAnimationStyle.CROWN_GLOW, 0xFFFACC15, 0xFFD97706, "gift_crystal"),
        VoiceGift("crown", "Royal Crown", "تاج شاهانه", "شاهي تاج", "👑", GiftCategory.PREMIUM, 30, true, 4, GiftAnimationStyle.CROWN_GLOW, 0xFFFDE047, 0xFFF59E0B, "vip_fanfare"),
        VoiceGift("butterfly", "Golden Butterfly", "پروانه طلایی", "زرینه پتنګه", "🦋", GiftCategory.PREMIUM, 25, false, 3, GiftAnimationStyle.PETALS_BURST, 0xFFFBBF24, 0xFFEC4899, "gift_crystal"),

        // C. VEHICLES
        VoiceGift("luxury_car", "Hyperion GT Car", "خودرو لوکس اسپرت", "عصري سپورټ موټر", "🏎️", GiftCategory.VEHICLE, 40, true, 5, GiftAnimationStyle.LUXURY_CAR_DRIVE, 0xFF06B6D4, 0xFF8B5CF6, "vehicle_rev"),
        VoiceGift("motorcycle", "Neon Superbike", "موتور نئونی", "نیون موټرسایکل", "🏍️", GiftCategory.VEHICLE, 30, true, 4, GiftAnimationStyle.MOTORCYCLE_DRIFT, 0xFFF97316, 0xFFEC4899, "vehicle_rev"),
        VoiceGift("helicopter", "Cyber Helicopter", "هلیکوپتر سایبری", "سایبري چورلکه", "🚁", GiftCategory.VEHICLE, 50, true, 6, GiftAnimationStyle.HELICOPTER_FLYOVER, 0xFF10B981, 0xFF06B6D4, "helicopter_rotor"),
        VoiceGift("airplane", "Private Jet", "جت شخصی", "شخصي الوتکه", "✈️", GiftCategory.VEHICLE, 60, true, 7, GiftAnimationStyle.AIRPLANE_FLYBY, 0xFF38BDF8, 0xFF6366F1, "jet_flyby"),
        VoiceGift("spaceship", "Starship Warp", "سفینه فضایی", "فضايي بیړۍ", "🚀", GiftCategory.VEHICLE, 80, true, 8, GiftAnimationStyle.SPACESHIP_WARP, 0xFF8B5CF6, 0xFF22D3EE, "spaceship_warp"),

        // D. FANTASY
        VoiceGift("dragon", "Celestial Dragon", "اژدهای آسمانی", "آسماني اژدها", "🐉", GiftCategory.FANTASY, 100, true, 9, GiftAnimationStyle.DRAGON_FLAME, 0xFFEF4444, 0xFFF59E0B, "dragon_roar"),
        VoiceGift("phoenix", "Solar Phoenix", "ققنوس آتشین", "لمریز ققنوس", "🔥", GiftCategory.FANTASY, 75, true, 8, GiftAnimationStyle.DRAGON_FLAME, 0xFFF97316, 0xFFFACC15, "cinematic_boom"),
        VoiceGift("portal", "Astral Portal", "دروازه کهکشانی", "کهکشاني دروازه", "🌀", GiftCategory.FANTASY, 65, true, 7, GiftAnimationStyle.PORTAL_VORTEX, 0xFFA855F7, 0xFF06B6D4, "spaceship_warp"),

        // E. SPECIAL
        VoiceGift("legendary_universe", "Vibe Supernova", "انفجار سوپرنوا", "د ستورو چاودنه", "🌌", GiftCategory.SPECIAL, 120, true, 10, GiftAnimationStyle.FULLSCREEN_LEGENDARY, 0xFFEC4899, 0xFF22D3EE, "vip_fanfare")
    )

    fun findById(id: String): VoiceGift? = gifts.firstOrNull { it.id == id }
}

enum class EntranceStyle(
    val id: String,
    val titleEn: String,
    val titleFa: String,
    val titlePs: String,
    val emoji: String,
    val accentHex: Long,
    val secondaryHex: Long,
    val soundEffectId: String,
    val isVip: Boolean
) {
    STANDARD("standard", "Neon Ripple", "ورود نئونی", "نیون ننوتل", "✨", 0xFF38BDF8, 0xFF818CF8, "welcome", false),
    LUXURY_CAR("luxury_car", "Luxury GT Arrival", "ورود با خودرو لوکس", "د لوکس موټر راتګ", "🚘", 0xFF06B6D4, 0xFF3B82F6, "vehicle_rev", true),
    RACING_CAR("racing_car", "Apex Racer", "ورود ماشین مسابقه‌ای", "د سیالۍ موټر", "🏎️", 0xFFEF4444, 0xFFF97316, "vehicle_rev", true),
    MOTORCYCLE("motorcycle", "Cyber Superbike", "ورود با موتور نئونی", "سایبري موټرسایکل", "🏍️", 0xFFEC4899, 0xFF8B5CF6, "vehicle_rev", true),
    HELICOPTER("helicopter", "Sky Chopper", "فرود هلیکوپتر", "د چورلکې ناسته", "🚁", 0xFF10B981, 0xFF06B6D4, "helicopter_rotor", true),
    AIRPLANE("airplane", "Private Aviation", "پرواز اختصاصی", "شخصي الوتنه", "🛩️", 0xFF60A5FA, 0xFFA855F7, "jet_flyby", true),
    JET("jet", "Supersonic Jet", "جت مافوق صوت", "چټک جیټ", "✈️", 0xFF22D3EE, 0xFF6366F1, "jet_flyby", true),
    SPACESHIP("spaceship", "Quantum Starship", "سفینه کوانتومی", "فضايي بیړۍ", "🛸", 0xFFA855F7, 0xFF22D3EE, "spaceship_warp", true),
    PORTAL("portal", "Dimensional Portal", "پورتال جادویی", "جادويي دروازه", "🌀", 0xFF8B5CF6, 0xFFEC4899, "spaceship_warp", true),
    ROYAL("royal", "Royal Carpet", "فرش سلطنتی", "شاهي هرکلی", "👑", 0xFFFACC15, 0xFFD97706, "vip_fanfare", true),
    VIP("vip", "VIP Spotlight", "نورپردازی VIP", "د VIP رڼا", "🌟", 0xFFFDE047, 0xFFEC4899, "vip_fanfare", true),
    LEGENDARY("legendary", "Dragon Sovereign", "فرمانروای اژدها", "د اژدها راتګ", "🐉", 0xFFF97316, 0xFFEF4444, "dragon_roar", true);

    companion object {
        fun fromId(id: String?): EntranceStyle =
            entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: STANDARD
    }
}

enum class RoomBackgroundTheme(
    val id: String,
    val titleEn: String,
    val titleFa: String,
    val titlePs: String,
    val emoji: String,
    val topHex: Long,
    val midHex: Long,
    val bottomHex: Long,
    val accentHex: Long
) {
    AURORA("aurora", "Aurora Borealis", "شفق قطبی", "قطبي شفق", "🌌", 0xFF060B1E, 0xFF0C2340, 0xFF0A1128, 0xFF2DD4BF),
    GALAXY("galaxy", "Deep Galaxy", "کهکشان عمیق", "ژور کهکشان", "🪐", 0xFF080518, 0xFF1A0F3B, 0xFF090617, 0xFFA855F7),
    NEON_CITY("neon_city", "Neon Skyline", "شهر نئونی", "نیون ښار", "🌆", 0xFF09081A, 0xFF1E103C, 0xFF0B1426, 0xFFEC4899),
    CYBERPUNK("cyberpunk", "Cyberpunk Grid", "سایبرپانک", "سایبرپنک", "⚡", 0xFF070B19, 0xFF13203A, 0xFF0A0716, 0xFF22D3EE),
    OCEAN_WAVES("ocean_waves", "Midnight Ocean", "امواج اقیانوس", "د شپې سمندر", "🌊", 0xFF04111E, 0xFF0B2E46, 0xFF051829, 0xFF38BDF8),
    FLOATING_PARTICLES("floating_particles", "Stardust Drift", "ذرات معلق", "ځلانده ذرې", "✨", 0xFF0A0D18, 0xFF161E34, 0xFF090B14, 0xFF818CF8),
    FIRE_ATMOSPHERE("fire_atmosphere", "Ember Lounge", "اتمسفر آتشین", "اورین فضا", "🔥", 0xFF160707, 0xFF34120B, 0xFF120608, 0xFFF97316),
    LUXURY_ROYAL("luxury_royal", "Royal Obsidian", "طلای سلطنتی", "شاهي زرین", "👑", 0xFF0D0A06, 0xFF271D0B, 0xFF100C05, 0xFFFACC15);

    companion object {
        fun fromId(id: String?): RoomBackgroundTheme =
            entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: AURORA
    }
}

enum class SoundCategory(val labelEn: String, val labelFa: String) {
    WELCOME("Welcome", "خوشامد"),
    APPLAUSE("Applause", "تشویق"),
    CELEBRATION("Celebration", "جشن"),
    FUNNY("Funny", "سرگرمی"),
    DRAMATIC("Dramatic", "دراماتیک"),
    VEHICLE("Vehicle", "وسایل"),
    CINEMATIC("Cinematic", "سینمایی"),
    VIP_ENTRANCE("VIP", "VIP"),
    GIFT("Gifts", "هدیه")
}

data class SoundEffectItem(
    val id: String,
    val titleEn: String,
    val titleFa: String,
    val emoji: String,
    val category: SoundCategory
)

object SoundboardCatalog {
    val effects: List<SoundEffectItem> = listOf(
        SoundEffectItem("welcome", "Warm Welcome Chime", "خوشامدگویی ملایم", "👋", SoundCategory.WELCOME),
        SoundEffectItem("applause", "Stage Applause", "تشویق حضار", "👏", SoundCategory.APPLAUSE),
        SoundEffectItem("celebration", "Victory Fanfare", "جشن پیروزی", "🎉", SoundCategory.CELEBRATION),
        SoundEffectItem("funny_boing", "Playful Pop", "صدای طنز", "😜", SoundCategory.FUNNY),
        SoundEffectItem("cinematic_boom", "Dramatic Impact", "ضربه دراماتیک", "🥁", SoundCategory.DRAMATIC),
        SoundEffectItem("vehicle_rev", "Supercar Rev", "غرش خودرو", "🏎️", SoundCategory.VEHICLE),
        SoundEffectItem("helicopter_rotor", "Chopper Rotor", "ملخ هلیکوپتر", "🚁", SoundCategory.VEHICLE),
        SoundEffectItem("jet_flyby", "Jet Whoosh", "عبور جت", "✈️", SoundCategory.CINEMATIC),
        SoundEffectItem("spaceship_warp", "Warp Drive", "پرش فضایی", "🚀", SoundCategory.CINEMATIC),
        SoundEffectItem("dragon_roar", "Dragon Pulse", "غرش اژدها", "🐉", SoundCategory.CINEMATIC),
        SoundEffectItem("vip_fanfare", "VIP Royal Horn", "شیپور VIP", "👑", SoundCategory.VIP_ENTRANCE),
        SoundEffectItem("gift_sparkle", "Gift Shimmer", "درخشش هدیه", "✨", SoundCategory.GIFT),
        SoundEffectItem("gift_crystal", "Crystal Bell", "زنگ الماس", "💎", SoundCategory.GIFT)
    )
}

enum class AudioRouteMode(val labelEn: String, val labelFa: String) {
    SPEAKER("Speaker", "بلندگو"),
    EARPIECE("Earpiece", "گوشی"),
    BLUETOOTH("Bluetooth", "بلوتوث")
}

data class VoiceRoomLiveEvent(
    val id: String,
    val roomId: String,
    val type: String, // "entrance", "gift", "sound", "reaction", "chat", "system"
    val senderId: String,
    val senderName: String,
    val senderAvatarUrl: String? = null,
    val senderFlag: String = "🌐",
    val senderVipLevel: Int = 1,
    val payloadId: String = "",
    val text: String = "",
    val recipientId: String? = null,
    val recipientName: String? = null,
    val giftValueXp: Int = 0,
    val timestampMs: Long = System.currentTimeMillis()
)

data class VoiceRoomPoll(
    val id: String,
    val roomId: String,
    val question: String,
    val options: List<String>,
    val votes: Map<String, Int> = emptyMap(),
    val createdById: String,
    val createdByName: String,
    val isClosed: Boolean = false,
    val createdAtMs: Long = System.currentTimeMillis()
)

data class SupporterRankEntry(
    val userId: String,
    val userName: String,
    val flagEmoji: String,
    val totalXpGifted: Int,
    val giftsCount: Int
)

data class ActiveGiftVisual(
    val eventId: String,
    val gift: VoiceGift,
    val senderName: String,
    val senderFlag: String,
    val recipientName: String
)

data class ActiveEntranceVisual(
    val eventId: String,
    val userName: String,
    val userFlag: String,
    val userLevel: Int,
    val style: EntranceStyle
)
