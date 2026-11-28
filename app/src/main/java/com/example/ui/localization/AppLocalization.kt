package com.example.ui.localization

import androidx.compose.ui.unit.LayoutDirection
import com.example.model.RoomCategory
import com.example.ui.theme.AppTheme

enum class AppLanguage(val code: String, val displayName: String, val nativeName: String, val isRtl: Boolean) {
    ENGLISH("en", "English", "English", false),
    DARI_PERSIAN("fa", "Dari / Persian", "فارسی / دری", true),
    PASHTO("ps", "Pashto", "پښتو", true);

    val layoutDirection: LayoutDirection
        get() = if (isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr
}

object AppStrings {
    fun t(key: String, lang: AppLanguage): String {
        return translations[key]?.get(lang) ?: translations[key]?.get(AppLanguage.ENGLISH) ?: key
    }

    fun roomCategory(cat: RoomCategory, lang: AppLanguage): String = when (cat) {
        RoomCategory.ALL -> t("cat_all", lang)
        RoomCategory.LANGUAGE_EXCHANGE -> t("cat_language_exchange", lang)
        RoomCategory.CHILL_VIBES -> t("cat_casual", lang)
        RoomCategory.MUSIC_JAM -> t("cat_music", lang)
        RoomCategory.TECH_TALK -> t("cat_tech", lang)
        RoomCategory.STORYTELLING -> t("cat_storytelling", lang)
        RoomCategory.CULTURE -> t("cat_culture", lang)
    }

    fun themeName(theme: AppTheme, lang: AppLanguage): String = when (theme) {
        AppTheme.PURE_WHITE -> t("theme_pure_white", lang)
        AppTheme.SOFT_LIGHT -> t("theme_soft_light", lang)
        AppTheme.MIDNIGHT_BLACK -> t("theme_midnight_black", lang)
        AppTheme.DEEP_NAVY -> t("theme_deep_navy", lang)
        AppTheme.GRAPHITE_DARK -> t("theme_graphite_dark", lang)
        AppTheme.AMOLED_BLACK -> t("theme_amoled_black", lang)
        AppTheme.LAVENDER -> t("theme_lavender", lang)
        AppTheme.OCEAN_BLUE -> t("theme_ocean_blue", lang)
        AppTheme.EMERALD -> t("theme_emerald", lang)
        AppTheme.ROSE -> t("theme_rose", lang)
    }

    private val translations: Map<String, Map<AppLanguage, String>> = mapOf(
        // Navigation
        "nav_rooms" to mapOf(
            AppLanguage.ENGLISH to "Voice Rooms",
            AppLanguage.DARI_PERSIAN to "اتاق صوتی",
            AppLanguage.PASHTO to "غږیزې خونې"
        ),
        "nav_feed" to mapOf(
            AppLanguage.ENGLISH to "Feed",
            AppLanguage.DARI_PERSIAN to "پست‌ها",
            AppLanguage.PASHTO to "پوسټونه"
        ),
        "nav_messages" to mapOf(
            AppLanguage.ENGLISH to "Messages",
            AppLanguage.DARI_PERSIAN to "پیام‌ها",
            AppLanguage.PASHTO to "پیغامونه"
        ),
        "nav_worldwide" to mapOf(
            AppLanguage.ENGLISH to "Worldwide",
            AppLanguage.DARI_PERSIAN to "جهان",
            AppLanguage.PASHTO to "نړیوال"
        ),
        "nav_profile" to mapOf(
            AppLanguage.ENGLISH to "Profile",
            AppLanguage.DARI_PERSIAN to "پروفایل",
            AppLanguage.PASHTO to "پېژندڅېره"
        ),

        // Common
        "back" to mapOf(
            AppLanguage.ENGLISH to "Back",
            AppLanguage.DARI_PERSIAN to "بازگشت",
            AppLanguage.PASHTO to "شاته"
        ),
        "cancel" to mapOf(
            AppLanguage.ENGLISH to "Cancel",
            AppLanguage.DARI_PERSIAN to "لغو",
            AppLanguage.PASHTO to "لغوه کول"
        ),
        "save_changes" to mapOf(
            AppLanguage.ENGLISH to "Save Changes",
            AppLanguage.DARI_PERSIAN to "ذخیره تغییرات",
            AppLanguage.PASHTO to "بدلونونه خوندي کړئ"
        ),
        "close" to mapOf(
            AppLanguage.ENGLISH to "Close",
            AppLanguage.DARI_PERSIAN to "بستن",
            AppLanguage.PASHTO to "تړل"
        ),
        "done" to mapOf(
            AppLanguage.ENGLISH to "Done",
            AppLanguage.DARI_PERSIAN to "تکمیل",
            AppLanguage.PASHTO to "بشپړ"
        ),
        "send" to mapOf(
            AppLanguage.ENGLISH to "Send",
            AppLanguage.DARI_PERSIAN to "ارسال",
            AppLanguage.PASHTO to "استول"
        ),
        "or" to mapOf(
            AppLanguage.ENGLISH to "OR",
            AppLanguage.DARI_PERSIAN to "یا",
            AppLanguage.PASHTO to "یا"
        ),
        "just_now" to mapOf(
            AppLanguage.ENGLISH to "Just now",
            AppLanguage.DARI_PERSIAN to "هم‌اکنون",
            AppLanguage.PASHTO to "همدا اوس"
        ),
        "online_status" to mapOf(
            AppLanguage.ENGLISH to "Online",
            AppLanguage.DARI_PERSIAN to "آنلاین",
            AppLanguage.PASHTO to "آنلاین"
        ),
        "offline_status" to mapOf(
            AppLanguage.ENGLISH to "Offline",
            AppLanguage.DARI_PERSIAN to "آفلاین",
            AppLanguage.PASHTO to "آفلاین"
        ),
        "global_label" to mapOf(
            AppLanguage.ENGLISH to "Global",
            AppLanguage.DARI_PERSIAN to "جهانی",
            AppLanguage.PASHTO to "نړیوال"
        ),

        // Auth & Onboarding
        "splash_subtitle" to mapOf(
            AppLanguage.ENGLISH to "Global Social & Voice Discovery",
            AppLanguage.DARI_PERSIAN to "شبکه جهانی صوتی و گفتگو",
            AppLanguage.PASHTO to "نړیواله غږیزه او ټولنیزه شبکه"
        ),
        "welcome_title" to mapOf(
            AppLanguage.ENGLISH to "Welcome to VibeRoom",
            AppLanguage.DARI_PERSIAN to "به وایب‌روم خوش آمدید",
            AppLanguage.PASHTO to "وایبروم ته ښه راغلاست"
        ),
        "welcome_subtitle" to mapOf(
            AppLanguage.ENGLISH to "Connect with real people worldwide through live voice stages, posts, and cloud-saved chats.",
            AppLanguage.DARI_PERSIAN to "با افراد واقعی در سراسر جهان از طریق اتاق‌های صوتی زنده، پست‌ها و چت آنلاین گفتگو کنید.",
            AppLanguage.PASHTO to "د نړۍ له ریښتینو خلکو سره د ژوندیو غږیزو خونو، پوسټونو او آنلاین چټ له لارې اړیکه ونیسئ."
        ),
        "welcome_feat_countries_title" to mapOf(
            AppLanguage.ENGLISH to "Connect Worldwide",
            AppLanguage.DARI_PERSIAN to "ارتباط با سراسر جهان",
            AppLanguage.PASHTO to "له ټولې نړۍ سره اړیکه"
        ),
        "welcome_feat_countries_desc" to mapOf(
            AppLanguage.ENGLISH to "Real verified members with locked country badges.",
            AppLanguage.DARI_PERSIAN to "کاربران واقعی و تأییدشده با نشان کشور قفل‌شده.",
            AppLanguage.PASHTO to "ریښتیني تایید شوي غړي د قفل شوي هیواد نښان سره."
        ),
        "welcome_feat_voice_title" to mapOf(
            AppLanguage.ENGLISH to "Live Voice Rooms",
            AppLanguage.DARI_PERSIAN to "اتاق‌های صوتی زنده",
            AppLanguage.PASHTO to "ژوندۍ غږیزې خونې"
        ),
        "welcome_feat_voice_desc" to mapOf(
            AppLanguage.ENGLISH to "Create or join live rooms by topic or language.",
            AppLanguage.DARI_PERSIAN to "اتاق بسازید یا به گفتگوهای صوتی زنده بپیوندید.",
            AppLanguage.PASHTO to "خونې جوړې کړئ یا ژوندیو غږیزو بحثونو ته ورشئ."
        ),
        "welcome_feat_chat_title" to mapOf(
            AppLanguage.ENGLISH to "Cloud Chat History",
            AppLanguage.DARI_PERSIAN to "سابقه چت ابری و آنلاین",
            AppLanguage.PASHTO to "آنلاین خوندي شوي چټونه"
        ),
        "welcome_feat_chat_desc" to mapOf(
            AppLanguage.ENGLISH to "Your chats and profile restore whenever you log back in.",
            AppLanguage.DARI_PERSIAN to "با ورود مجدد، تمام پیام‌ها و پروفایل شما بازیابی می‌شوند.",
            AppLanguage.PASHTO to "کله چې بیا ننوځئ، ستاسو ټول پیغامونه او پروفایل بیرته راګرځي."
        ),
        "google_signin" to mapOf(
            AppLanguage.ENGLISH to "Continue with Google",
            AppLanguage.DARI_PERSIAN to "ورود / ثبت‌نام سریع با گوگل",
            AppLanguage.PASHTO to "د ګوګل حساب سره ننوتل"
        ),
        "create_account_email" to mapOf(
            AppLanguage.ENGLISH to "Sign Up with Email Verification",
            AppLanguage.DARI_PERSIAN to "ثبت‌نام با تأیید ایمیل",
            AppLanguage.PASHTO to "د برېښنالیک تایید سره نوم‌لیکنه"
        ),
        "already_have_account" to mapOf(
            AppLanguage.ENGLISH to "I Already Have an Account",
            AppLanguage.DARI_PERSIAN to "قبلاً حساب دارم (ورود مجدد)",
            AppLanguage.PASHTO to "زه دمخه حساب لرم (ننوتل)"
        ),
        "terms_footer" to mapOf(
            AppLanguage.ENGLISH to "By continuing, you agree to VibeRoom's Terms & Privacy Policy.",
            AppLanguage.DARI_PERSIAN to "با ادامه دادن، قوانین و حریم خصوصی وایب‌روم را می‌پذیرید.",
            AppLanguage.PASHTO to "په دوام ورکولو سره تاسو د وایبروم اصول او محرمیت منئ."
        ),
        "create_your_account" to mapOf(
            AppLanguage.ENGLISH to "Create Your Account",
            AppLanguage.DARI_PERSIAN to "ساخت حساب جدید",
            AppLanguage.PASHTO to "نوی حساب جوړ کړئ"
        ),
        "register_subtitle" to mapOf(
            AppLanguage.ENGLISH to "No password needed — verified securely via email link or Google.",
            AppLanguage.DARI_PERSIAN to "بدون نیاز به رمز عبور — ورود امن با لینک تأیید ایمیل یا گوگل.",
            AppLanguage.PASHTO to "پاسورډ ته اړتیا نشته — د برېښنالیک لینک یا ګوګل له لارې خوندي تایید."
        ),
        "or_email_register" to mapOf(
            AppLanguage.ENGLISH to "OR VERIFY WITH EMAIL",
            AppLanguage.DARI_PERSIAN to "یا تأیید با ایمیل",
            AppLanguage.PASHTO to "یا د برېښنالیک له لارې تایید"
        ),
        "first_name" to mapOf(
            AppLanguage.ENGLISH to "First Name",
            AppLanguage.DARI_PERSIAN to "نام",
            AppLanguage.PASHTO to "لومړی نوم"
        ),
        "last_name" to mapOf(
            AppLanguage.ENGLISH to "Last Name",
            AppLanguage.DARI_PERSIAN to "تخلص / نام خانوادگی",
            AppLanguage.PASHTO to "تخلص"
        ),
        "email_address" to mapOf(
            AppLanguage.ENGLISH to "Email Address",
            AppLanguage.DARI_PERSIAN to "نشانی ایمیل (جیمیل)",
            AppLanguage.PASHTO to "برېښنالیک پته"
        ),
        "country_region" to mapOf(
            AppLanguage.ENGLISH to "Country (Permanent)",
            AppLanguage.DARI_PERSIAN to "کشور (انتخاب دائمی)",
            AppLanguage.PASHTO to "هیواد (دایمي انتخاب)"
        ),
        "country_lock_warning" to mapOf(
            AppLanguage.ENGLISH to "Cannot be changed after registration",
            AppLanguage.DARI_PERSIAN to "پس از ثبت‌نام قابل تغییر نیست",
            AppLanguage.PASHTO to "له نوم‌لیکنې وروسته نه بدلیږي"
        ),
        "country_locked_note" to mapOf(
            AppLanguage.ENGLISH to "Country is locked to your account",
            AppLanguage.DARI_PERSIAN to "کشور به حساب شما قفل است و تغییر داده نمی‌شود",
            AppLanguage.PASHTO to "هیواد ستاسو په حساب کې قفل دی او نه بدلیږي"
        ),
        "phone_optional" to mapOf(
            AppLanguage.ENGLISH to "Phone Number (Optional)",
            AppLanguage.DARI_PERSIAN to "شماره تماس (اختیاری)",
            AppLanguage.PASHTO to "د تلیفون شمیره (اختیاري)"
        ),
        "terms_accept" to mapOf(
            AppLanguage.ENGLISH to "I am 18+ and accept the Terms & Privacy Policy.",
            AppLanguage.DARI_PERSIAN to "بالای ۱۸ سال هستم و قوانین برنامه را می‌پذیرم.",
            AppLanguage.PASHTO to "زه له ۱۸ کلونو پورته یم او اصول منم."
        ),
        "send_email_link_btn" to mapOf(
            AppLanguage.ENGLISH to "Send Verification Email",
            AppLanguage.DARI_PERSIAN to "ارسال لینک تأیید به ایمیل",
            AppLanguage.PASHTO to "برېښنالیک ته تایید لینک واستوئ"
        ),
        "err_enter_name_email" to mapOf(
            AppLanguage.ENGLISH to "Please enter your first name and a valid email.",
            AppLanguage.DARI_PERSIAN to "لطفاً نام و ایمیل معتبر خود را وارد کنید.",
            AppLanguage.PASHTO to "مهرباني وکړئ خپل نوم او سم برېښنالیک ولیکئ."
        ),
        "err_accept_terms" to mapOf(
            AppLanguage.ENGLISH to "Please accept the terms to continue.",
            AppLanguage.DARI_PERSIAN to "لطفاً قوانین برنامه را تأیید کنید.",
            AppLanguage.PASHTO to "مهرباني وکړئ د دوام لپاره اصول ومنئ."
        ),
        "err_invalid_email" to mapOf(
            AppLanguage.ENGLISH to "Please enter a valid email address.",
            AppLanguage.DARI_PERSIAN to "نشانی ایمیل معتبر نیست.",
            AppLanguage.PASHTO to "مهرباني وکړئ سم برېنالیک ولیکئ."
        ),
        "err_email_link_disabled" to mapOf(
            AppLanguage.ENGLISH to "Email link sign-in is not enabled on Firebase Console yet. Please use Google Sign-In above or enable Email Link in Firebase Authentication.",
            AppLanguage.DARI_PERSIAN to "ارسال لینک ایمیل هنوز در تنظیمات Firebase فعال نشده است. لطفاً از ورود سریع گوگل استفاده کنید یا روش Email Link را در Firebase فعال نمایید.",
            AppLanguage.PASHTO to "په Firebase کې د برېښنالیک لینک طریقه لا نه ده فعاله شوې. مهرباني وکړئ پورته د ګوګل ننوتل وکاروئ."
        ),
        "email_verify_title" to mapOf(
            AppLanguage.ENGLISH to "Check Your Email Inbox",
            AppLanguage.DARI_PERSIAN to "ایمیل خود را بررسی کنید",
            AppLanguage.PASHTO to "خپل برېښنالیک وګورئ"
        ),
        "email_verify_desc" to mapOf(
            AppLanguage.ENGLISH to "We sent a secure verification link to:",
            AppLanguage.DARI_PERSIAN to "یک لینک تأیید امن به نشانی زیر ارسال شد:",
            AppLanguage.PASHTO to "موږ لاندې پتې ته د تایید خوندي لینک واستاوه:"
        ),
        "email_verify_instructions" to mapOf(
            AppLanguage.ENGLISH to "Open the email on this device and tap the sign-in link to verify your account, or paste the link below.",
            AppLanguage.DARI_PERSIAN to "ایمیل را در همین دستگاه باز کرده و روی لینک تأیید کلیک کنید تا وارد شوید، یا لینک را در کادر زیر جای‌گذاری کنید.",
            AppLanguage.PASHTO to "په دې موبایل کې برېښنالیک پرانیزئ او پر لینک کلیک وکړئ، یا لینک لاندې ولیکئ."
        ),
        "paste_email_link_label" to mapOf(
            AppLanguage.ENGLISH to "Verification Link (if copied)",
            AppLanguage.DARI_PERSIAN to "لینک تأیید (در صورت کپی کردن)",
            AppLanguage.PASHTO to "د تایید لینک (که کاپي شوی وي)"
        ),
        "verify_and_continue" to mapOf(
            AppLanguage.ENGLISH to "Verify & Open Account",
            AppLanguage.DARI_PERSIAN to "تأیید و ورود به حساب",
            AppLanguage.PASHTO to "تایید او حساب پرانیستل"
        ),
        "resend_in" to mapOf(
            AppLanguage.ENGLISH to "Resend available in",
            AppLanguage.DARI_PERSIAN to "ارسال مجدد تا",
            AppLanguage.PASHTO to "بیا استول په"
        ),
        "resend_email_link" to mapOf(
            AppLanguage.ENGLISH to "Resend Verification Email",
            AppLanguage.DARI_PERSIAN to "ارسال دوباره ایمیل تأیید",
            AppLanguage.PASHTO to "د تایید برېښنالیک بیا واستوئ"
        ),
        "welcome_back" to mapOf(
            AppLanguage.ENGLISH to "Welcome Back",
            AppLanguage.DARI_PERSIAN to "خوش آمدید",
            AppLanguage.PASHTO to "بیا ښه راغلاست"
        ),
        "login_subtitle" to mapOf(
            AppLanguage.ENGLISH to "Reopen your account with Google or passwordless email verification.",
            AppLanguage.DARI_PERSIAN to "با حساب گوگل یا لینک تأیید ایمیل وارد اکانت قبلی تان شوید و چت‌ها را بازیابی کنید.",
            AppLanguage.PASHTO to "خپل مخکینی حساب د ګوګل یا برېښنالیک تایید له لارې بیرته پرانیزئ."
        ),
        "email_link_sent_info" to mapOf(
            AppLanguage.ENGLISH to "Verification link sent! Open your inbox and tap the link to sign in.",
            AppLanguage.DARI_PERSIAN to "لینک تأیید ارسال شد! ایمیل خود را باز کرده و روی لینک کلیک کنید.",
            AppLanguage.PASHTO to "د تایید لینک واستول شو! خپل برېښنالیک پرانیزئ او پر لینک کلیک وکړئ."
        ),
        "dont_have_account" to mapOf(
            AppLanguage.ENGLISH to "Don't have an account?",
            AppLanguage.DARI_PERSIAN to "حساب کاربری ندارید؟",
            AppLanguage.PASHTO to "حساب نه لرئ؟"
        ),
        "already_registered_signin" to mapOf(
            AppLanguage.ENGLISH to "Already have an account?",
            AppLanguage.DARI_PERSIAN to "قبلاً ثبت‌نام کرده‌اید؟",
            AppLanguage.PASHTO to "دمخه مو نوم‌لیکنه کړې؟"
        ),
        "sign_in" to mapOf(
            AppLanguage.ENGLISH to "Sign In",
            AppLanguage.DARI_PERSIAN to "ورود به حساب",
            AppLanguage.PASHTO to "ننوتل"
        ),
        "create_account" to mapOf(
            AppLanguage.ENGLISH to "Create Account",
            AppLanguage.DARI_PERSIAN to "ثبت‌نام",
            AppLanguage.PASHTO to "حساب جوړول"
        ),

        // Profile Setup Steps
        "setup_profile_title" to mapOf(
            AppLanguage.ENGLISH to "Complete Your Profile",
            AppLanguage.DARI_PERSIAN to "تکمیل پروفایل",
            AppLanguage.PASHTO to "خپل پروفایل بشپړ کړئ"
        ),
        "setup_profile_subtitle" to mapOf(
            AppLanguage.ENGLISH to "Set how others see you worldwide. Choose your country carefully — it locks once saved.",
            AppLanguage.DARI_PERSIAN to "نام و کشور خود را تنظیم کنید. کشور پس از ثبت، برای همیشه قفل می‌شود.",
            AppLanguage.PASHTO to "خپل نوم او هیواد وټاکئ. هیواد له خوندي کیدو وروسته قفل کیږي."
        ),
        "display_name" to mapOf(
            AppLanguage.ENGLISH to "Display Name",
            AppLanguage.DARI_PERSIAN to "نام نمایشی",
            AppLanguage.PASHTO to "ښکاره نوم"
        ),
        "unique_username" to mapOf(
            AppLanguage.ENGLISH to "Username",
            AppLanguage.DARI_PERSIAN to "نام کاربری",
            AppLanguage.PASHTO to "کارن نوم"
        ),
        "short_bio" to mapOf(
            AppLanguage.ENGLISH to "Short Bio",
            AppLanguage.DARI_PERSIAN to "معرفی کوتاه (بیو)",
            AppLanguage.PASHTO to "لنډه پېژندنه"
        ),
        "privacy_18_notice" to mapOf(
            AppLanguage.ENGLISH to "Your email is kept private and never shown on your public card.",
            AppLanguage.DARI_PERSIAN to "ایمیل شما محرمانه می‌ماند و در پروفایل عمومی نمایش داده نمی‌شود.",
            AppLanguage.PASHTO to "ستاسو برېښنالیک پټ ساتل کیږي او نورو ته نه ښکاري."
        ),
        "next_interests" to mapOf(
            AppLanguage.ENGLISH to "Next: Choose Interests",
            AppLanguage.DARI_PERSIAN to "بعدی: انتخاب علاقه‌مندی‌ها",
            AppLanguage.PASHTO to "بل: خوښې وټاکئ"
        ),
        "select_interests_title" to mapOf(
            AppLanguage.ENGLISH to "Select Your Interests",
            AppLanguage.DARI_PERSIAN to "علاقه‌مندی‌های تان را انتخاب کنید",
            AppLanguage.PASHTO to "خپلې خوښې وټاکئ"
        ),
        "select_interests_desc" to mapOf(
            AppLanguage.ENGLISH to "Pick 3 or more topics to connect with matching rooms and members",
            AppLanguage.DARI_PERSIAN to "حداقل ۳ موضوع انتخاب کنید تا افراد هم‌فکر را پیدا کنید",
            AppLanguage.PASHTO to "لږ تر لږه ۳ موضوعګانې وټاکئ"
        ),
        "next_languages" to mapOf(
            AppLanguage.ENGLISH to "Next: Spoken Languages",
            AppLanguage.DARI_PERSIAN to "بعدی: زبان‌های گفتاری",
            AppLanguage.PASHTO to "بل: ژبې"
        ),
        "choose_languages_title" to mapOf(
            AppLanguage.ENGLISH to "Languages You Speak",
            AppLanguage.DARI_PERSIAN to "زبان‌هایی که صحبت می‌کنید",
            AppLanguage.PASHTO to "هغه ژبې چې پرې غږیږئ"
        ),
        "choose_languages_desc" to mapOf(
            AppLanguage.ENGLISH to "Match with speakers around the world",
            AppLanguage.DARI_PERSIAN to "با هم‌زبانان و زبان‌آموزان در جهان آشنا شوید",
            AppLanguage.PASHTO to "په نړۍ کې له هم‌ژبو سره وصل شئ"
        ),
        "next_finish" to mapOf(
            AppLanguage.ENGLISH to "Next: Final Step",
            AppLanguage.DARI_PERSIAN to "بعدی: مرحله پایانی",
            AppLanguage.PASHTO to "بل: وروستی پړاو"
        ),
        "almost_ready" to mapOf(
            AppLanguage.ENGLISH to "Almost Ready!",
            AppLanguage.DARI_PERSIAN to "تقریباً آماده است!",
            AppLanguage.PASHTO to "نږدې چمتو دی!"
        ),
        "permissions_desc" to mapOf(
            AppLanguage.ENGLISH to "How VibeRoom protects your privacy:",
            AppLanguage.DARI_PERSIAN to "وایب‌روم چگونه از حریم خصوصی شما محافظت می‌کند:",
            AppLanguage.PASHTO to "وایبروم څنګه ستاسو محرمیت ساتي:"
        ),
        "enter_viberoom" to mapOf(
            AppLanguage.ENGLISH to "Enter VibeRoom 🚀",
            AppLanguage.DARI_PERSIAN to "ورود به وایب‌روم 🚀",
            AppLanguage.PASHTO to "وایبروم ته ننوتل 🚀"
        ),
        "saving_profile" to mapOf(
            AppLanguage.ENGLISH to "Saving to Cloud...",
            AppLanguage.DARI_PERSIAN to "در حال ذخیره آنلاین...",
            AppLanguage.PASHTO to "په کلاوډ کې خوندي کیږي..."
        ),

        // Country Picker
        "select_country" to mapOf(
            AppLanguage.ENGLISH to "Select Your Country",
            AppLanguage.DARI_PERSIAN to "کشور خود را انتخاب کنید",
            AppLanguage.PASHTO to "خپل هیواد وټاکئ"
        ),
        "countries_count" to mapOf(
            AppLanguage.ENGLISH to "Countries",
            AppLanguage.DARI_PERSIAN to "کشور",
            AppLanguage.PASHTO to "هیوادونه"
        ),
        "search_country" to mapOf(
            AppLanguage.ENGLISH to "Search country or code (e.g. +93, Afghanistan)",
            AppLanguage.DARI_PERSIAN to "جستجوی کشور یا کد (مثلاً Afghanistan یا +93)",
            AppLanguage.PASHTO to "هیواد یا کوډ ولټوئ (لکه Afghanistan یا +93)"
        ),

        // Voice Rooms
        "live_voice_rooms" to mapOf(
            AppLanguage.ENGLISH to "Live Voice Rooms",
            AppLanguage.DARI_PERSIAN to "اتاق‌های صوتی زنده",
            AppLanguage.PASHTO to "ژوندۍ غږیزې خونې"
        ),
        "voice_rooms_subtitle" to mapOf(
            AppLanguage.ENGLISH to "Create a room or join live audio discussions worldwide",
            AppLanguage.DARI_PERSIAN to "اتاق بسازید یا به گفتگوهای صوتی زنده در سراسر جهان بپیوندید",
            AppLanguage.PASHTO to "خونه جوړه کړئ یا نړیوالو غږیزو بحثونو ته ورشئ"
        ),
        "live_badge" to mapOf(
            AppLanguage.ENGLISH to "LIVE",
            AppLanguage.DARI_PERSIAN to "زنده",
            AppLanguage.PASHTO to "ژوندی"
        ),
        "start_room" to mapOf(
            AppLanguage.ENGLISH to "Start Room",
            AppLanguage.DARI_PERSIAN to "ساخت اتاق",
            AppLanguage.PASHTO to "خونه جوړول"
        ),
        "join_room" to mapOf(
            AppLanguage.ENGLISH to "Join",
            AppLanguage.DARI_PERSIAN to "ورود",
            AppLanguage.PASHTO to "گډون"
        ),
        "listening_count" to mapOf(
            AppLanguage.ENGLISH to "listening",
            AppLanguage.DARI_PERSIAN to "نفر شنونده",
            AppLanguage.PASHTO to "اورېدونکي"
        ),
        "host_label" to mapOf(
            AppLanguage.ENGLISH to "Host",
            AppLanguage.DARI_PERSIAN to "میزبان",
            AppLanguage.PASHTO to "کوربه"
        ),
        "no_rooms_yet" to mapOf(
            AppLanguage.ENGLISH to "No active voice rooms in this category. Tap Start Room to go live!",
            AppLanguage.DARI_PERSIAN to "در این دسته‌بندی اتاق فعالی نیست. دکمه ساخت اتاق را بزنید!",
            AppLanguage.PASHTO to "په دې برخه کې کومه فعاله خونه نشته. د نوې خونې تڼۍ کېکاږئ!"
        ),
        "create_room_title" to mapOf(
            AppLanguage.ENGLISH to "Start a Voice Room",
            AppLanguage.DARI_PERSIAN to "ساخت اتاق صوتی جدید",
            AppLanguage.PASHTO to "نوې غږیزه خونه جوړه کړئ"
        ),
        "room_title_label" to mapOf(
            AppLanguage.ENGLISH to "Room Title",
            AppLanguage.DARI_PERSIAN to "عنوان اتاق",
            AppLanguage.PASHTO to "د خونې سرلیک"
        ),
        "room_title_hint" to mapOf(
            AppLanguage.ENGLISH to "e.g. Evening Chill & Language Chat",
            AppLanguage.DARI_PERSIAN to "مثلاً گپ‌وگفت دوستانه و تمرین زبان",
            AppLanguage.PASHTO to "لکه د ماښام دوستانه بحث"
        ),
        "description_label" to mapOf(
            AppLanguage.ENGLISH to "Description",
            AppLanguage.DARI_PERSIAN to "توضیحات",
            AppLanguage.PASHTO to "تفصیل"
        ),
        "room_desc_hint" to mapOf(
            AppLanguage.ENGLISH to "What is this room about?",
            AppLanguage.DARI_PERSIAN to "موضوع گفتگو چیست؟",
            AppLanguage.PASHTO to "دا خونه د څه په اړه ده؟"
        ),
        "select_category" to mapOf(
            AppLanguage.ENGLISH to "Select Category",
            AppLanguage.DARI_PERSIAN to "انتخاب دسته‌بندی",
            AppLanguage.PASHTO to "کټګوري وټاکئ"
        ),
        "tags_label" to mapOf(
            AppLanguage.ENGLISH to "Tags (comma separated)",
            AppLanguage.DARI_PERSIAN to "برچسب‌ها (با ویرگول جدا کنید)",
            AppLanguage.PASHTO to "ټګونه (په کامه بیل کړئ)"
        ),
        "go_live" to mapOf(
            AppLanguage.ENGLISH to "Go Live",
            AppLanguage.DARI_PERSIAN to "شروع اتاق",
            AppLanguage.PASHTO to "پیل کړئ"
        ),
        "end_stage" to mapOf(
            AppLanguage.ENGLISH to "End Stage",
            AppLanguage.DARI_PERSIAN to "پایان اتاق",
            AppLanguage.PASHTO to "خونه پای ته رسول"
        ),
        "leave_stage" to mapOf(
            AppLanguage.ENGLISH to "Leave",
            AppLanguage.DARI_PERSIAN to "خروج",
            AppLanguage.PASHTO to "وتل"
        ),
        "stage_speakers" to mapOf(
            AppLanguage.ENGLISH to "STAGE SPEAKERS",
            AppLanguage.DARI_PERSIAN to "سخنرانان روی استیج",
            AppLanguage.PASHTO to "د سټېج ویناوال"
        ),
        "in_audience" to mapOf(
            AppLanguage.ENGLISH to "In Audience",
            AppLanguage.DARI_PERSIAN to "در میان شنوندگان",
            AppLanguage.PASHTO to "په اورېدونکو کې"
        ),
        "raise_hand" to mapOf(
            AppLanguage.ENGLISH to "Raise Hand",
            AppLanguage.DARI_PERSIAN to "بلند کردن دست",
            AppLanguage.PASHTO to "لاس پورته کول"
        ),
        "hand_raised" to mapOf(
            AppLanguage.ENGLISH to "Hand Raised",
            AppLanguage.DARI_PERSIAN to "دست بالاست",
            AppLanguage.PASHTO to "لاس پورته دی"
        ),
        "mic_off" to mapOf(
            AppLanguage.ENGLISH to "Mic Off",
            AppLanguage.DARI_PERSIAN to "میکروفون بسته",
            AppLanguage.PASHTO to "مایک بند دی"
        ),
        "speaking" to mapOf(
            AppLanguage.ENGLISH to "Speaking",
            AppLanguage.DARI_PERSIAN to "در حال صحبت",
            AppLanguage.PASHTO to "خبرې کوي"
        ),

        // Categories
        "cat_all" to mapOf(
            AppLanguage.ENGLISH to "All",
            AppLanguage.DARI_PERSIAN to "همه",
            AppLanguage.PASHTO to "ټول"
        ),
        "cat_language_exchange" to mapOf(
            AppLanguage.ENGLISH to "Languages",
            AppLanguage.DARI_PERSIAN to "تبادل زبان",
            AppLanguage.PASHTO to "ژبې"
        ),
        "cat_culture" to mapOf(
            AppLanguage.ENGLISH to "Culture",
            AppLanguage.DARI_PERSIAN to "فرهنگ",
            AppLanguage.PASHTO to "کلتور"
        ),
        "cat_music" to mapOf(
            AppLanguage.ENGLISH to "Music",
            AppLanguage.DARI_PERSIAN to "موسیقی",
            AppLanguage.PASHTO to "موسيقي"
        ),
        "cat_tech" to mapOf(
            AppLanguage.ENGLISH to "Tech",
            AppLanguage.DARI_PERSIAN to "تکنولوژی",
            AppLanguage.PASHTO to "ټکنالوژي"
        ),
        "cat_casual" to mapOf(
            AppLanguage.ENGLISH to "Chill & Chat",
            AppLanguage.DARI_PERSIAN to "گپ‌وگفت",
            AppLanguage.PASHTO to "عادي خبرې"
        ),
        "cat_storytelling" to mapOf(
            AppLanguage.ENGLISH to "Stories",
            AppLanguage.DARI_PERSIAN to "داستان و تجربه",
            AppLanguage.PASHTO to "کیسې"
        ),

        // Feed
        "feed_subtitle" to mapOf(
            AppLanguage.ENGLISH to "Real-time posts, likes & comments from members worldwide",
            AppLanguage.DARI_PERSIAN to "پست‌ها، لایک‌ها و دیدگاه‌های آنلاین کاربران",
            AppLanguage.PASHTO to "د نړیوالو غړو ژوندي پوسټونه، خوښونې او تبصرې"
        ),
        "create_post" to mapOf(
            AppLanguage.ENGLISH to "New Post",
            AppLanguage.DARI_PERSIAN to "پست جدید",
            AppLanguage.PASHTO to "نوی پوسټ"
        ),
        "post_placeholder" to mapOf(
            AppLanguage.ENGLISH to "Share your thoughts, music, or questions with everyone...",
            AppLanguage.DARI_PERSIAN to "افکار، موزیک یا سوالات تان را با دیگران شریک کنید...",
            AppLanguage.PASHTO to "خپل فکرونه یا نظر له نورو سره شریک کړئ..."
        ),
        "category_label" to mapOf(
            AppLanguage.ENGLISH to "Category",
            AppLanguage.DARI_PERSIAN to "دسته‌بندی",
            AppLanguage.PASHTO to "کټګوري"
        ),
        "no_posts_yet" to mapOf(
            AppLanguage.ENGLISH to "No posts yet. Be the first to share something with the community!",
            AppLanguage.DARI_PERSIAN to "هنوز پستی وجود ندارد. اولین نفری باشید که مطلبی به اشتراک می‌گذارد!",
            AppLanguage.PASHTO to "تر اوسه کوم پوسټ نشته. لومړی کس شئ چې څه شریکوي!"
        ),
        "like" to mapOf(
            AppLanguage.ENGLISH to "Like",
            AppLanguage.DARI_PERSIAN to "پسندیدن",
            AppLanguage.PASHTO to "خوښول"
        ),
        "comments" to mapOf(
            AppLanguage.ENGLISH to "Comments",
            AppLanguage.DARI_PERSIAN to "دیدگاه‌ها",
            AppLanguage.PASHTO to "تبصرې"
        ),
        "no_comments_yet" to mapOf(
            AppLanguage.ENGLISH to "No comments yet. Say hello!",
            AppLanguage.DARI_PERSIAN to "هنوز دیدگاهی ثبت نشده است.",
            AppLanguage.PASHTO to "تر اوسه تبصره نشته."
        ),
        "write_comment" to mapOf(
            AppLanguage.ENGLISH to "Write a comment...",
            AppLanguage.DARI_PERSIAN to "دیدگاه خود را بنویسید...",
            AppLanguage.PASHTO to "خپله تبصره ولیکئ..."
        ),

        // Chats
        "messages_subtitle" to mapOf(
            AppLanguage.ENGLISH to "Your chats are saved online to your account",
            AppLanguage.DARI_PERSIAN to "تمام گفتگوهای شما به‌صورت آنلاین ذخیره می‌شوند",
            AppLanguage.PASHTO to "ستاسو ټولې خبرې په آنلاین ډول خوندي کیږي"
        ),
        "search_conversations" to mapOf(
            AppLanguage.ENGLISH to "Search conversations...",
            AppLanguage.DARI_PERSIAN to "جستجو در پیام‌ها...",
            AppLanguage.PASHTO to "په پیغامونو کې لټون..."
        ),
        "no_chats_yet" to mapOf(
            AppLanguage.ENGLISH to "No conversations yet",
            AppLanguage.DARI_PERSIAN to "هنوز گفتگویی ندارید",
            AppLanguage.PASHTO to "تر اوسه کوم چټ نشته"
        ),
        "no_chats_hint" to mapOf(
            AppLanguage.ENGLISH to "Find people in Worldwide and send them a wave or message!",
            AppLanguage.DARI_PERSIAN to "در بخش جهان به افراد سلام بدهید یا پیام بفرستید!",
            AppLanguage.PASHTO to "په نړیواله برخه کې چا ته سلام یا پیغام واستوئ!"
        ),
        "find_people_btn" to mapOf(
            AppLanguage.ENGLISH to "Browse Worldwide",
            AppLanguage.DARI_PERSIAN to "مشاهده افراد جهان",
            AppLanguage.PASHTO to "نړیوال غړي وګورئ"
        ),
        "type_message" to mapOf(
            AppLanguage.ENGLISH to "Type a message...",
            AppLanguage.DARI_PERSIAN to "پیام بنویسید...",
            AppLanguage.PASHTO to "پیغام ولیکئ..."
        ),
        "voice_note" to mapOf(
            AppLanguage.ENGLISH to "Voice note",
            AppLanguage.DARI_PERSIAN to "پیام صوتی",
            AppLanguage.PASHTO to "غږیز پیغام"
        ),
        "playing_voice" to mapOf(
            AppLanguage.ENGLISH to "Playing voice note...",
            AppLanguage.DARI_PERSIAN to "در حال پخش پیام صوتی...",
            AppLanguage.PASHTO to "غږیز پیغام غږول کیږي..."
        ),
        "view_full_profile" to mapOf(
            AppLanguage.ENGLISH to "View Full Profile",
            AppLanguage.DARI_PERSIAN to "مشاهده پروفایل کامل",
            AppLanguage.PASHTO to "بشپړ پروفایل وګورئ"
        ),
        "report_user" to mapOf(
            AppLanguage.ENGLISH to "Report User",
            AppLanguage.DARI_PERSIAN to "گزارش کاربر",
            AppLanguage.PASHTO to "کارن راپور کړئ"
        ),
        "block_user" to mapOf(
            AppLanguage.ENGLISH to "Block User",
            AppLanguage.DARI_PERSIAN to "مسدود کردن کاربر",
            AppLanguage.PASHTO to "کارن بلاک کړئ"
        ),
        "icebreaker_1" to mapOf(
            AppLanguage.ENGLISH to "👋 Hey! Where are you connecting from?",
            AppLanguage.DARI_PERSIAN to "👋 سلام! از کدام شهر و کشور آنلاین هستید؟",
            AppLanguage.PASHTO to "👋 سلام! له کوم هیواد څخه یاست؟"
        ),
        "icebreaker_2" to mapOf(
            AppLanguage.ENGLISH to "🗣️ Would love to practice languages together!",
            AppLanguage.DARI_PERSIAN to "🗣️ دوست دارم با هم زبان تمرین کنیم!",
            AppLanguage.PASHTO to "🗣️ راځئ چې په ګډه ژبه تمرین کړو!"
        ),
        "icebreaker_3" to mapOf(
            AppLanguage.ENGLISH to "🎙️ Want to join a voice room later?",
            AppLanguage.DARI_PERSIAN to "🎙️ دوست داری در یک اتاق صوتی گپ بزنیم؟",
            AppLanguage.PASHTO to "🎙️ غواړې غږیزې خونې ته ولاړ شو؟"
        ),
        "wave_greeting" to mapOf(
            AppLanguage.ENGLISH to "👋 Hey! Nice to connect with you on VibeRoom.",
            AppLanguage.DARI_PERSIAN to "👋 سلام! خوشحالم در وایب‌روم با شما آشنا می‌شوم.",
            AppLanguage.PASHTO to "👋 سلام! خوښ شوم چې دلته مو ولیدل."
        ),

        // Worldwide / Discover
        "worldwide_subtitle" to mapOf(
            AppLanguage.ENGLISH to "Real members connected online",
            AppLanguage.DARI_PERSIAN to "اعضای واقعی متصل به دیتابیس آنلاین",
            AppLanguage.PASHTO to "ریښتیني آنلاین غړي"
        ),
        "hero_badge" to mapOf(
            AppLanguage.ENGLISH to "WORLDWIDE COMMUNITY",
            AppLanguage.DARI_PERSIAN to "جامعه جهانی واقعی",
            AppLanguage.PASHTO to "نړیواله ټولنه"
        ),
        "online_now_count" to mapOf(
            AppLanguage.ENGLISH to "online now",
            AppLanguage.DARI_PERSIAN to "نفر آنلاین",
            AppLanguage.PASHTO to "اوس آنلاین"
        ),
        "hero_title" to mapOf(
            AppLanguage.ENGLISH to "Meet People & Practice Languages",
            AppLanguage.DARI_PERSIAN to "آشنایی با افراد واقعی و تبادل فرهنگ",
            AppLanguage.PASHTO to "له خلکو سره پېژندګلوي او د ژبو زده‌کړه"
        ),
        "hero_desc" to mapOf(
            AppLanguage.ENGLISH to "Follow members, wave hello, or start a live chat backed by Firebase.",
            AppLanguage.DARI_PERSIAN to "کاربران را دنبال کنید، سلام بفرستید و چت آنلاین همیشگی را شروع کنید.",
            AppLanguage.PASHTO to "غړي تعقیب کړئ، سلام واستوئ او دایمي آنلاین چټ پیل کړئ."
        ),
        "search_people_hint" to mapOf(
            AppLanguage.ENGLISH to "Search by name, country, or interest...",
            AppLanguage.DARI_PERSIAN to "جستجو با نام، کشور یا علاقه‌مندی...",
            AppLanguage.PASHTO to "د نوم، هیواد یا خوښې له مخې لټون..."
        ),
        "members_discovered" to mapOf(
            AppLanguage.ENGLISH to "members registered",
            AppLanguage.DARI_PERSIAN to "کاربر ثبت‌نام شده",
            AppLanguage.PASHTO to "ثبت شوي غړي"
        ),
        "no_profiles_found" to mapOf(
            AppLanguage.ENGLISH to "No matching members yet",
            AppLanguage.DARI_PERSIAN to "هنوز کاربری با این مشخصات پیدا نشد",
            AppLanguage.PASHTO to "لا تر اوسه کوم غړی ونه موندل شو"
        ),
        "reset_filters" to mapOf(
            AppLanguage.ENGLISH to "Reset Filters",
            AppLanguage.DARI_PERSIAN to "پاک کردن فیلترها",
            AppLanguage.PASHTO to "فیلترونه پاک کړئ"
        ),

        // User Profile Card (keep English keys exact for UserProfileCardTest!)
        "followers" to mapOf(
            AppLanguage.ENGLISH to "Followers",
            AppLanguage.DARI_PERSIAN to "دنبال‌کننده",
            AppLanguage.PASHTO to "پالونکي"
        ),
        "following" to mapOf(
            AppLanguage.ENGLISH to "Following",
            AppLanguage.DARI_PERSIAN to "دنبال‌شده",
            AppLanguage.PASHTO to "پالل شوي"
        ),
        "follow" to mapOf(
            AppLanguage.ENGLISH to "Follow",
            AppLanguage.DARI_PERSIAN to "دنبال کردن",
            AppLanguage.PASHTO to "پالل"
        ),
        "rooms_hosted" to mapOf(
            AppLanguage.ENGLISH to "Rooms Hosted",
            AppLanguage.DARI_PERSIAN to "اتاق‌های ساخته",
            AppLanguage.PASHTO to "جوړې شوې خونې"
        ),
        "edit_profile" to mapOf(
            AppLanguage.ENGLISH to "Edit Profile",
            AppLanguage.DARI_PERSIAN to "ویرایش پروفایل",
            AppLanguage.PASHTO to "پېژندڅېره سمول"
        ),
        "wave" to mapOf(
            AppLanguage.ENGLISH to "Wave 👋",
            AppLanguage.DARI_PERSIAN to "سلام 👋",
            AppLanguage.PASHTO to "سلام 👋"
        ),
        "chat" to mapOf(
            AppLanguage.ENGLISH to "Chat",
            AppLanguage.DARI_PERSIAN to "چت",
            AppLanguage.PASHTO to "چټ"
        ),
        "languages_section" to mapOf(
            AppLanguage.ENGLISH to "🗣️ Languages",
            AppLanguage.DARI_PERSIAN to "🗣️ زبان‌ها",
            AppLanguage.PASHTO to "🗣️ ژبې"
        ),
        "interests_section" to mapOf(
            AppLanguage.ENGLISH to "🎯 Interests",
            AppLanguage.DARI_PERSIAN to "🎯 علاقه‌مندی‌ها",
            AppLanguage.PASHTO to "🎯 خوښې"
        ),

        // Profile Screen & Settings
        "my_profile" to mapOf(
            AppLanguage.ENGLISH to "My Profile",
            AppLanguage.DARI_PERSIAN to "پروفایل من",
            AppLanguage.PASHTO to "زما پېژندڅېره"
        ),
        "preferences_title" to mapOf(
            AppLanguage.ENGLISH to "Appearance & Language",
            AppLanguage.DARI_PERSIAN to "ظاهر و زبان برنامه",
            AppLanguage.PASHTO to "بڼه او ژبه"
        ),
        "select_theme" to mapOf(
            AppLanguage.ENGLISH to "App Theme",
            AppLanguage.DARI_PERSIAN to "تم برنامه",
            AppLanguage.PASHTO to "د اپلیکیشن تم"
        ),
        "theme_neon" to mapOf(
            AppLanguage.ENGLISH to "Neon",
            AppLanguage.DARI_PERSIAN to "نئون",
            AppLanguage.PASHTO to "نیون"
        ),
        "theme_sunset" to mapOf(
            AppLanguage.ENGLISH to "Sunset",
            AppLanguage.DARI_PERSIAN to "غروب",
            AppLanguage.PASHTO to "ماښام"
        ),
        "theme_ocean" to mapOf(
            AppLanguage.ENGLISH to "Ocean",
            AppLanguage.DARI_PERSIAN to "اقیانوس",
            AppLanguage.PASHTO to "سمندر"
        ),
        "theme_emerald" to mapOf(
            AppLanguage.ENGLISH to "Emerald",
            AppLanguage.DARI_PERSIAN to "زمرد",
            AppLanguage.PASHTO to "زمرد"
        ),
        "select_language" to mapOf(
            AppLanguage.ENGLISH to "Language / زبان / ژبه",
            AppLanguage.DARI_PERSIAN to "زبان / Language / ژبه",
            AppLanguage.PASHTO to "ژبه / زبان / Language"
        ),
        "privacy_safety" to mapOf(
            AppLanguage.ENGLISH to "Privacy & Safety",
            AppLanguage.DARI_PERSIAN to "حریم خصوصی و امنیت",
            AppLanguage.PASHTO to "محرمیت او خوندیتوب"
        ),
        "global_visibility_title" to mapOf(
            AppLanguage.ENGLISH to "Worldwide Discovery",
            AppLanguage.DARI_PERSIAN to "نمایش در جستجوی جهانی",
            AppLanguage.PASHTO to "په نړیوال لټون کې ښودل"
        ),
        "global_visibility_desc" to mapOf(
            AppLanguage.ENGLISH to "Allow members to discover your profile",
            AppLanguage.DARI_PERSIAN to "اجازه دهید دیگران پروفایل شما را ببینند",
            AppLanguage.PASHTO to "نورو غړو ته اجازه ورکړئ ستاسو پروفایل وګوري"
        ),
        "dm_allowed_title" to mapOf(
            AppLanguage.ENGLISH to "Direct Messages",
            AppLanguage.DARI_PERSIAN to "پیام‌های مستقیم",
            AppLanguage.PASHTO to "مستقیم پیغامونه"
        ),
        "dm_allowed_desc" to mapOf(
            AppLanguage.ENGLISH to "Allow members to message you privately",
            AppLanguage.DARI_PERSIAN to "دریافت پیام خصوصی از کاربران",
            AppLanguage.PASHTO to "د نورو غړو څخه شخصي پیغامونه ترلاسه کړئ"
        ),
        "account_actions" to mapOf(
            AppLanguage.ENGLISH to "Account",
            AppLanguage.DARI_PERSIAN to "حساب کاربری",
            AppLanguage.PASHTO to "حساب"
        ),
        "sign_out" to mapOf(
            AppLanguage.ENGLISH to "Sign Out",
            AppLanguage.DARI_PERSIAN to "خروج از حساب",
            AppLanguage.PASHTO to "له حساب څخه وتل"
        ),
        "languages_comma" to mapOf(
            AppLanguage.ENGLISH to "Languages (comma separated)",
            AppLanguage.DARI_PERSIAN to "زبان‌ها (با ویرگول جدا کنید)",
            AppLanguage.PASHTO to "ژبې (په کامه بیل کړئ)"
        ),
        "interests_comma" to mapOf(
            AppLanguage.ENGLISH to "Interests (comma separated)",
            AppLanguage.DARI_PERSIAN to "علاقه‌مندی‌ها (با ویرگول جدا کنید)",
            AppLanguage.PASHTO to "خوښې (په کامه بیل کړئ)"
        ),

        // Notifications / Feedback
        "notif_post_published" to mapOf(
            AppLanguage.ENGLISH to "Post published to the community feed!",
            AppLanguage.DARI_PERSIAN to "پست شما در دیتابیس آنلاین منتشر شد!",
            AppLanguage.PASHTO to "ستاسو پوسټ په آنلاین ډول خپور شو!"
        ),
        "notif_comment_posted" to mapOf(
            AppLanguage.ENGLISH to "Comment posted!",
            AppLanguage.DARI_PERSIAN to "دیدگاه شما ثبت شد!",
            AppLanguage.PASHTO to "ستاسو تبصره خپره شوه!"
        ),
        "notif_wave_sent" to mapOf(
            AppLanguage.ENGLISH to "Wave sent to",
            AppLanguage.DARI_PERSIAN to "سلام ارسال شد به",
            AppLanguage.PASHTO to "سلام واستول شو ورته:"
        ),
        "notif_profile_saved" to mapOf(
            AppLanguage.ENGLISH to "Profile updated and synced to cloud!",
            AppLanguage.DARI_PERSIAN to "پروفایل شما ذخیره و به‌روزرسانی شد!",
            AppLanguage.PASHTO to "پروفایل خوندي او تازه شو!"
        ),
        "notif_account_restored" to mapOf(
            AppLanguage.ENGLISH to "Welcome back! Your profile and chat history were restored.",
            AppLanguage.DARI_PERSIAN to "خوش آمدید! پروفایل و تمام سابقه چت شما بازیابی شد.",
            AppLanguage.PASHTO to "بیا ښه راغلاست! ستاسو پروفایل او چټونه بیرته راغلل."
        ),

        // Phase 1 Social Persistence, Discovery Filters, Friendships & Safety
        "online_only_filter" to mapOf(
            AppLanguage.ENGLISH to "Online Only",
            AppLanguage.DARI_PERSIAN to "فقط آنلاین‌ها",
            AppLanguage.PASHTO to "یوازې آنلاین"
        ),
        "friend_requests" to mapOf(
            AppLanguage.ENGLISH to "Friend Requests",
            AppLanguage.DARI_PERSIAN to "درخواست‌های دوستی",
            AppLanguage.PASHTO to "د ملګرتیا غوښتنې"
        ),
        "add_friend" to mapOf(
            AppLanguage.ENGLISH to "Add Friend",
            AppLanguage.DARI_PERSIAN to "درخواست دوستی",
            AppLanguage.PASHTO to "ملګری اضافه کړئ"
        ),
        "friend_request_sent" to mapOf(
            AppLanguage.ENGLISH to "Request Sent",
            AppLanguage.DARI_PERSIAN to "درخواست ارسال شد",
            AppLanguage.PASHTO to "غوښتنه واستول شوه"
        ),
        "friends_badge" to mapOf(
            AppLanguage.ENGLISH to "Friends",
            AppLanguage.DARI_PERSIAN to "دوست هستید",
            AppLanguage.PASHTO to "ملګري یاست"
        ),
        "accept" to mapOf(
            AppLanguage.ENGLISH to "Accept",
            AppLanguage.DARI_PERSIAN to "پذیرفتن",
            AppLanguage.PASHTO to "منل"
        ),
        "decline" to mapOf(
            AppLanguage.ENGLISH to "Decline",
            AppLanguage.DARI_PERSIAN to "رد کردن",
            AppLanguage.PASHTO to "ردول"
        ),
        "blocked_users_title" to mapOf(
            AppLanguage.ENGLISH to "Blocked Users",
            AppLanguage.DARI_PERSIAN to "کاربران مسدودشده",
            AppLanguage.PASHTO to "بلاک شوي کارونکي"
        ),
        "blocked_users_desc" to mapOf(
            AppLanguage.ENGLISH to "Manage accounts you have blocked",
            AppLanguage.DARI_PERSIAN to "مدیریت حساب‌هایی که مسدود کرده‌اید",
            AppLanguage.PASHTO to "بلاک شوي حسابونه اداره کړئ"
        ),
        "no_blocked_users" to mapOf(
            AppLanguage.ENGLISH to "You have not blocked any users.",
            AppLanguage.DARI_PERSIAN to "هیچ کاربری را مسدود نکرده‌اید.",
            AppLanguage.PASHTO to "تاسو هیڅ کارن نه دی بلاک کړی."
        ),
        "unblock" to mapOf(
            AppLanguage.ENGLISH to "Unblock",
            AppLanguage.DARI_PERSIAN to "رفع مسدودیت",
            AppLanguage.PASHTO to "بلاک لرې کړئ"
        ),
        "report_room" to mapOf(
            AppLanguage.ENGLISH to "Report Room",
            AppLanguage.DARI_PERSIAN to "گزارش اتاق",
            AppLanguage.PASHTO to "خونه راپور کړئ"
        ),
        "delete_post" to mapOf(
            AppLanguage.ENGLISH to "Delete",
            AppLanguage.DARI_PERSIAN to "حذف",
            AppLanguage.PASHTO to "ړنګول"
        ),
        "notif_report_submitted" to mapOf(
            AppLanguage.ENGLISH to "Report submitted securely for moderation.",
            AppLanguage.DARI_PERSIAN to "گزارش شما با موفقیت ثبت و ارسال شد.",
            AppLanguage.PASHTO to "ستاسو راپور په بریالیتوب سره ثبت شو."
        ),
        "notif_user_blocked" to mapOf(
            AppLanguage.ENGLISH to "User blocked and hidden from your feed & chats.",
            AppLanguage.DARI_PERSIAN to "کاربر مسدود شد و دیگر پیام‌ها و پست‌هایش نمایش داده نمی‌شود.",
            AppLanguage.PASHTO to "کارن بلاک شو او له لیدلوری پټ شو."
        ),
        "notif_user_unblocked" to mapOf(
            AppLanguage.ENGLISH to "User unblocked.",
            AppLanguage.DARI_PERSIAN to "مسدودیت کاربر برداشته شد.",
            AppLanguage.PASHTO to "کارن بېرته خلاص شو."
        ),
        "notif_friend_request_sent" to mapOf(
            AppLanguage.ENGLISH to "Friend request sent!",
            AppLanguage.DARI_PERSIAN to "درخواست دوستی ارسال شد!",
            AppLanguage.PASHTO to "د ملګرتیا غوښتنه واستول شوه!"
        ),
        "notif_friend_accepted" to mapOf(
            AppLanguage.ENGLISH to "You are now friends!",
            AppLanguage.DARI_PERSIAN to "درخواست دوستی پذیرفته شد!",
            AppLanguage.PASHTO to "تاسو اوس ملګري شوئ!"
        ),
        "notif_post_deleted" to mapOf(
            AppLanguage.ENGLISH to "Post deleted.",
            AppLanguage.DARI_PERSIAN to "پست حذف شد.",
            AppLanguage.PASHTO to "پوسټ ړنګ شو."
        ),
        "dm_disabled_notice" to mapOf(
            AppLanguage.ENGLISH to "This member has disabled new direct messages.",
            AppLanguage.DARI_PERSIAN to "این کاربر دریافت پیام مستقیم جدید را غیرفعال کرده است.",
            AppLanguage.PASHTO to "دې غړي نوي مستقیم پیغامونه بند کړي دي."
        ),
        "native_languages_label" to mapOf(
            AppLanguage.ENGLISH to "Native Languages (comma separated)",
            AppLanguage.DARI_PERSIAN to "زبان‌های مادری (با ویرگول جدا کنید)",
            AppLanguage.PASHTO to "مورنۍ ژبې (په کامه بیل کړئ)"
        ),
        "learning_languages_label" to mapOf(
            AppLanguage.ENGLISH to "Learning Languages (comma separated)",
            AppLanguage.DARI_PERSIAN to "زبان‌های در حال یادگیری (با ویرگول جدا کنید)",
            AppLanguage.PASHTO to "د زده‌کړې ژبې (په کامه بیل کړئ)"
        ),
        "proficiency_label" to mapOf(
            AppLanguage.ENGLISH to "Proficiency Level",
            AppLanguage.DARI_PERSIAN to "سطح زبان",
            AppLanguage.PASHTO to "د ژبې کچه"
        ),
        "city_label" to mapOf(
            AppLanguage.ENGLISH to "City (Optional)",
            AppLanguage.DARI_PERSIAN to "شهر (اختیاری)",
            AppLanguage.PASHTO to "ښار (اختیاري)"
        ),

        // Phase 2 & 3: Messaging Upgrades, Stories, Feed Filters, Notifications, AI Language Center & Account Deletion
        "reply_label" to mapOf(
            AppLanguage.ENGLISH to "Reply",
            AppLanguage.DARI_PERSIAN to "پاسخ",
            AppLanguage.PASHTO to "ځواب"
        ),
        "edit_label" to mapOf(
            AppLanguage.ENGLISH to "Edit",
            AppLanguage.DARI_PERSIAN to "ویرایش",
            AppLanguage.PASHTO to "سمول"
        ),
        "pin_label" to mapOf(
            AppLanguage.ENGLISH to "Pin",
            AppLanguage.DARI_PERSIAN to "سنجاق کردن",
            AppLanguage.PASHTO to "پین کول"
        ),
        "unpin_label" to mapOf(
            AppLanguage.ENGLISH to "Unpin",
            AppLanguage.DARI_PERSIAN to "برداشتن سنجاق",
            AppLanguage.PASHTO to "پین لرې کول"
        ),
        "pinned_message" to mapOf(
            AppLanguage.ENGLISH to "Pinned",
            AppLanguage.DARI_PERSIAN to "سنجاق‌شده",
            AppLanguage.PASHTO to "پین شوی"
        ),
        "edited_badge" to mapOf(
            AppLanguage.ENGLISH to "edited",
            AppLanguage.DARI_PERSIAN to "ویرایش‌شده",
            AppLanguage.PASHTO to "سمون شوی"
        ),
        "translate_label" to mapOf(
            AppLanguage.ENGLISH to "Translate",
            AppLanguage.DARI_PERSIAN to "ترجمه",
            AppLanguage.PASHTO to "ژباړه"
        ),
        "translating_label" to mapOf(
            AppLanguage.ENGLISH to "Translating...",
            AppLanguage.DARI_PERSIAN to "در حال ترجمه...",
            AppLanguage.PASHTO to "ژباړه روانه ده..."
        ),
        "search_in_chat" to mapOf(
            AppLanguage.ENGLISH to "Search in chat...",
            AppLanguage.DARI_PERSIAN to "جستجو در چت...",
            AppLanguage.PASHTO to "په چټ کې لټون..."
        ),
        "stories_title" to mapOf(
            AppLanguage.ENGLISH to "24h Stories",
            AppLanguage.DARI_PERSIAN to "استوری‌های ۲۴ ساعته",
            AppLanguage.PASHTO to "۲۴ ساعته کیسې"
        ),
        "add_story" to mapOf(
            AppLanguage.ENGLISH to "Your Story",
            AppLanguage.DARI_PERSIAN to "استوری شما",
            AppLanguage.PASHTO to "ستاسو کیسه"
        ),
        "create_story" to mapOf(
            AppLanguage.ENGLISH to "Share a 24h Story",
            AppLanguage.DARI_PERSIAN to "انتشار استوری ۲۴ ساعته",
            AppLanguage.PASHTO to "۲۴ ساعته کیسه شریک کړئ"
        ),
        "story_placeholder" to mapOf(
            AppLanguage.ENGLISH to "What's happening in your world today?",
            AppLanguage.DARI_PERSIAN to "امروز چه چیزی می‌خواهید با جهان به اشتراک بگذارید؟",
            AppLanguage.PASHTO to "نن غواړئ له نړۍ سره څه شریک کړئ؟"
        ),
        "story_views" to mapOf(
            AppLanguage.ENGLISH to "views",
            AppLanguage.DARI_PERSIAN to "بازدید",
            AppLanguage.PASHTO to "لیدنې"
        ),
        "feed_global" to mapOf(
            AppLanguage.ENGLISH to "Global",
            AppLanguage.DARI_PERSIAN to "جهانی",
            AppLanguage.PASHTO to "نړیوال"
        ),
        "feed_following" to mapOf(
            AppLanguage.ENGLISH to "Following",
            AppLanguage.DARI_PERSIAN to "دنبال‌شده‌ها",
            AppLanguage.PASHTO to "تعقیب شوي"
        ),
        "feed_saved" to mapOf(
            AppLanguage.ENGLISH to "Saved",
            AppLanguage.DARI_PERSIAN to "ذخیره‌شده‌ها",
            AppLanguage.PASHTO to "خوندي شوي"
        ),
        "search_feed_hint" to mapOf(
            AppLanguage.ENGLISH to "Search posts or #hashtags...",
            AppLanguage.DARI_PERSIAN to "جستجوی پست‌ها یا #هشتگ‌ها...",
            AppLanguage.PASHTO to "پوسټونه یا #هشتګونه ولټوئ..."
        ),
        "language_match_filter" to mapOf(
            AppLanguage.ENGLISH to "Partner Match",
            AppLanguage.DARI_PERSIAN to "هم‌زبان مناسب",
            AppLanguage.PASHTO to "د ژبې ملګري"
        ),
        "country_filter_all" to mapOf(
            AppLanguage.ENGLISH to "All Countries",
            AppLanguage.DARI_PERSIAN to "همه کشورها",
            AppLanguage.PASHTO to "ټول هیوادونه"
        ),
        "notifications_title" to mapOf(
            AppLanguage.ENGLISH to "Notifications",
            AppLanguage.DARI_PERSIAN to "اعلان‌ها",
            AppLanguage.PASHTO to "خبرتیاوې"
        ),
        "mark_all_read" to mapOf(
            AppLanguage.ENGLISH to "Mark all read",
            AppLanguage.DARI_PERSIAN to "خواندن همه",
            AppLanguage.PASHTO to "ټول لوستل شوي کول"
        ),
        "no_notifications" to mapOf(
            AppLanguage.ENGLISH to "You're all caught up! No notifications yet.",
            AppLanguage.DARI_PERSIAN to "هیچ اعلان جدیدی وجود ندارد.",
            AppLanguage.PASHTO to "لا تر اوسه کومه نوې خبرتیا نشته."
        ),
        "notifications_toggle" to mapOf(
            AppLanguage.ENGLISH to "In-App Notifications",
            AppLanguage.DARI_PERSIAN to "اعلان‌های درون‌برنامه",
            AppLanguage.PASHTO to "د اپلیکیشن دننه خبرتیاوې"
        ),
        "notifications_toggle_desc" to mapOf(
            AppLanguage.ENGLISH to "Receive alerts for friend requests, likes & comments",
            AppLanguage.DARI_PERSIAN to "دریافت اطلاعیه برای دوستی، پسند و دیدگاه‌ها",
            AppLanguage.PASHTO to "د ملګرتیا غوښتنو، لایک او تبصرو خبرتیاوې ترلاسه کړئ"
        ),
        "learning_hub_title" to mapOf(
            AppLanguage.ENGLISH to "Language Studio & AI Coach",
            AppLanguage.DARI_PERSIAN to "مرکز یادگیری زبان و مربی هوش مصنوعی",
            AppLanguage.PASHTO to "د ژبې زده‌کړې مرکز او AI لارښود"
        ),
        "learning_hub_subtitle" to mapOf(
            AppLanguage.ENGLISH to "AI Grammar Coach • Two-Way Translator • Flashcards",
            AppLanguage.DARI_PERSIAN to "مربی گرامر هوشمند • مترجم دوطرفه • فلش‌کارت واژگان",
            AppLanguage.PASHTO to "هوښیار ګرامر لارښود • دوه اړخیزه ژباړونکی • لغت کارتونه"
        ),
        "tab_ai_coach" to mapOf(
            AppLanguage.ENGLISH to "AI Coach",
            AppLanguage.DARI_PERSIAN to "مربی هوشمند",
            AppLanguage.PASHTO to "AI لارښود"
        ),
        "tab_translator" to mapOf(
            AppLanguage.ENGLISH to "Translator",
            AppLanguage.DARI_PERSIAN to "مترجم",
            AppLanguage.PASHTO to "ژباړونکی"
        ),
        "tab_flashcards" to mapOf(
            AppLanguage.ENGLISH to "Flashcards",
            AppLanguage.DARI_PERSIAN to "واژگان",
            AppLanguage.PASHTO to "لغتونه"
        ),
        "practice_quiz_btn" to mapOf(
            AppLanguage.ENGLISH to "Generate Practice Quiz",
            AppLanguage.DARI_PERSIAN to "ساخت تمرین هوشمند",
            AppLanguage.PASHTO to "تمرین پوښتنه جوړه کړئ"
        ),
        "save_phrase" to mapOf(
            AppLanguage.ENGLISH to "Save Phrase",
            AppLanguage.DARI_PERSIAN to "ذخیره عبارت",
            AppLanguage.PASHTO to "عبارت خوندي کړئ"
        ),
        "daily_goal_title" to mapOf(
            AppLanguage.ENGLISH to "Daily Study Goal",
            AppLanguage.DARI_PERSIAN to "هدف مطالعه روزانه",
            AppLanguage.PASHTO to "د ورځنۍ مطالعې هدف"
        ),
        "study_streak_label" to mapOf(
            AppLanguage.ENGLISH to "Day Streak",
            AppLanguage.DARI_PERSIAN to "روز پیاپی",
            AppLanguage.PASHTO to "پرله‌پسې ورځې"
        ),
        "delete_account" to mapOf(
            AppLanguage.ENGLISH to "Delete Account",
            AppLanguage.DARI_PERSIAN to "حذف دائمی حساب",
            AppLanguage.PASHTO to "حساب دایمي ړنګول"
        ),
        "delete_account_confirm" to mapOf(
            AppLanguage.ENGLISH to "Are you sure you want to permanently delete your VibeRoom profile and account? This cannot be undone.",
            AppLanguage.DARI_PERSIAN to "آیا مطمئن هستید که می‌خواهید پروفایل و حساب خود را برای همیشه حذف کنید؟ این کار قابل بازگشت نیست.",
            AppLanguage.PASHTO to "ایا ډاډه یاست چې غواړئ خپل وایبروم حساب د تل لپاره ړنګ کړئ؟ دا عمل بېرته نه راګرځي."
        ),
        "reauth_required_delete" to mapOf(
            AppLanguage.ENGLISH to "For security, please sign out and sign back in right before deleting your account.",
            AppLanguage.DARI_PERSIAN to "برای امنیت بیشتر، لطفاً یک بار خارج شوید و دوباره وارد شوید، سپس حساب را حذف کنید.",
            AppLanguage.PASHTO to "د امنیت لپاره، مهرباني وکړئ له حساب ووځئ او بیا ننوځئ، بیا حساب ړنګ کړئ."
        ),
        "voice_call" to mapOf(
            AppLanguage.ENGLISH to "Voice Call",
            AppLanguage.DARI_PERSIAN to "تماس صوتی",
            AppLanguage.PASHTO to "غږیزه اړیکه"
        ),
        "incoming_call" to mapOf(
            AppLanguage.ENGLISH to "Incoming Voice Call",
            AppLanguage.DARI_PERSIAN to "تماس صوتی ورودی",
            AppLanguage.PASHTO to "راتلونکې غږیزه اړیکه"
        ),
        "calling_status" to mapOf(
            AppLanguage.ENGLISH to "Ringing...",
            AppLanguage.DARI_PERSIAN to "در حال زنگ خوردن...",
            AppLanguage.PASHTO to "زنګ وهل کېږي..."
        ),
        "connected_status" to mapOf(
            AppLanguage.ENGLISH to "Live Audio Connected",
            AppLanguage.DARI_PERSIAN to "تماس صوتی زنده برقرار است",
            AppLanguage.PASHTO to "ژوندی غږیز تماس وصل شو"
        ),
        "end_call" to mapOf(
            AppLanguage.ENGLISH to "End Call",
            AppLanguage.DARI_PERSIAN to "قطع تماس",
            AppLanguage.PASHTO to "اړیکه پرې کول"
        ),
        "speakerphone" to mapOf(
            AppLanguage.ENGLISH to "Speaker",
            AppLanguage.DARI_PERSIAN to "بلندگو",
            AppLanguage.PASHTO to "سپیکر"
        ),
        "mute_label" to mapOf(
            AppLanguage.ENGLISH to "Mute",
            AppLanguage.DARI_PERSIAN to "بی‌صدا",
            AppLanguage.PASHTO to "غلی"
        ),
        "recording_voice" to mapOf(
            AppLanguage.ENGLISH to "Recording voice...",
            AppLanguage.DARI_PERSIAN to "در حال ضبط صدا...",
            AppLanguage.PASHTO to "غږ ثبتېږي..."
        ),
        "mic_permission_needed" to mapOf(
            AppLanguage.ENGLISH to "Microphone permission is required for voice audio.",
            AppLanguage.DARI_PERSIAN to "برای انتقال یا ضبط صدا اجازه دسترسی به مایکروفون لازم است.",
            AppLanguage.PASHTO to "د غږ لېږدولو یا ثبتولو لپاره د مایکروفون اجازه اړینه ده."
        ),
        "attach_photo" to mapOf(
            AppLanguage.ENGLISH to "Photo",
            AppLanguage.DARI_PERSIAN to "عکس",
            AppLanguage.PASHTO to "انځور"
        ),
        "attach_video" to mapOf(
            AppLanguage.ENGLISH to "Video",
            AppLanguage.DARI_PERSIAN to "ویدیو",
            AppLanguage.PASHTO to "ویډیو"
        ),
        "video_url_optional" to mapOf(
            AppLanguage.ENGLISH to "Or paste MP4 video URL (optional)",
            AppLanguage.DARI_PERSIAN to "یا لینک ویدیو (MP4) وارد کنید (اختیاری)",
            AppLanguage.PASHTO to "یا د ویډیو لینک (MP4) ولیکئ (اختیاري)"
        ),
        "webrtc_live_badge" to mapOf(
            AppLanguage.ENGLISH to "WebRTC Audio",
            AppLanguage.DARI_PERSIAN to "صدای زنده WebRTC",
            AppLanguage.PASHTO to "ژوندی WebRTC غږ"
        ),
        // 10 Themes + Mode + Accent Studio
        "theme_pure_white" to mapOf(
            AppLanguage.ENGLISH to "Pure White",
            AppLanguage.DARI_PERSIAN to "سفید خالص",
            AppLanguage.PASHTO to "خالص سپین"
        ),
        "theme_soft_light" to mapOf(
            AppLanguage.ENGLISH to "Soft Light",
            AppLanguage.DARI_PERSIAN to "روشن ملایم",
            AppLanguage.PASHTO to "نرم روښانه"
        ),
        "theme_midnight_black" to mapOf(
            AppLanguage.ENGLISH to "Midnight Black",
            AppLanguage.DARI_PERSIAN to "سیاه نیمه‌شب",
            AppLanguage.PASHTO to "د نیمې شپې تور"
        ),
        "theme_deep_navy" to mapOf(
            AppLanguage.ENGLISH to "Deep Navy",
            AppLanguage.DARI_PERSIAN to "سرمه‌ای عمیق",
            AppLanguage.PASHTO to "ژور نیلي"
        ),
        "theme_graphite_dark" to mapOf(
            AppLanguage.ENGLISH to "Graphite Dark",
            AppLanguage.DARI_PERSIAN to "گرافیتی تیره",
            AppLanguage.PASHTO to "تیاره ګرافایټ"
        ),
        "theme_amoled_black" to mapOf(
            AppLanguage.ENGLISH to "AMOLED Black",
            AppLanguage.DARI_PERSIAN to "سیاه AMOLED",
            AppLanguage.PASHTO to "AMOLED تور"
        ),
        "theme_lavender" to mapOf(
            AppLanguage.ENGLISH to "Lavender",
            AppLanguage.DARI_PERSIAN to "اسطوخودوس",
            AppLanguage.PASHTO to "لاوینډر"
        ),
        "theme_ocean_blue" to mapOf(
            AppLanguage.ENGLISH to "Ocean Blue",
            AppLanguage.DARI_PERSIAN to "آبی اقیانوسی",
            AppLanguage.PASHTO to "سمندري آبي"
        ),
        "theme_rose" to mapOf(
            AppLanguage.ENGLISH to "Rose",
            AppLanguage.DARI_PERSIAN to "رز",
            AppLanguage.PASHTO to "ګلابي"
        ),
        "theme_mode_label" to mapOf(
            AppLanguage.ENGLISH to "Appearance Mode",
            AppLanguage.DARI_PERSIAN to "حالت نمایش",
            AppLanguage.PASHTO to "د بڼې حالت"
        ),
        "theme_mode_light" to mapOf(
            AppLanguage.ENGLISH to "Light",
            AppLanguage.DARI_PERSIAN to "روشن",
            AppLanguage.PASHTO to "روښانه"
        ),
        "theme_mode_dark" to mapOf(
            AppLanguage.ENGLISH to "Dark",
            AppLanguage.DARI_PERSIAN to "تیره",
            AppLanguage.PASHTO to "تیاره"
        ),
        "theme_mode_system" to mapOf(
            AppLanguage.ENGLISH to "System",
            AppLanguage.DARI_PERSIAN to "سیستم",
            AppLanguage.PASHTO to "سیسټم"
        ),
        "accent_color_label" to mapOf(
            AppLanguage.ENGLISH to "Main Accent Color",
            AppLanguage.DARI_PERSIAN to "رنگ اصلی برنامه",
            AppLanguage.PASHTO to "اصلي رنګ"
        ),
        "accent_custom" to mapOf(
            AppLanguage.ENGLISH to "Custom Accent Hue",
            AppLanguage.DARI_PERSIAN to "تنظیم رنگ دلخواه",
            AppLanguage.PASHTO to "خپل رنګ ټاکل"
        ),
        "accent_default" to mapOf(
            AppLanguage.ENGLISH to "Theme Default",
            AppLanguage.DARI_PERSIAN to "پیش‌فرض تم",
            AppLanguage.PASHTO to "د تم اصلي رنګ"
        ),
        "accent_violet" to mapOf(
            AppLanguage.ENGLISH to "Violet",
            AppLanguage.DARI_PERSIAN to "بنفش",
            AppLanguage.PASHTO to "بنفش"
        ),
        "accent_sky" to mapOf(
            AppLanguage.ENGLISH to "Sky Blue",
            AppLanguage.DARI_PERSIAN to "آبی آسمانی",
            AppLanguage.PASHTO to "اسماني آبي"
        ),
        "accent_emerald" to mapOf(
            AppLanguage.ENGLISH to "Emerald",
            AppLanguage.DARI_PERSIAN to "زمردی",
            AppLanguage.PASHTO to "زمرد"
        ),
        "accent_rose" to mapOf(
            AppLanguage.ENGLISH to "Rose",
            AppLanguage.DARI_PERSIAN to "سرخابی",
            AppLanguage.PASHTO to "ګلابي"
        ),
        "accent_amber" to mapOf(
            AppLanguage.ENGLISH to "Amber",
            AppLanguage.DARI_PERSIAN to "کهربایی",
            AppLanguage.PASHTO to "امبر"
        ),
        "accent_indigo" to mapOf(
            AppLanguage.ENGLISH to "Indigo",
            AppLanguage.DARI_PERSIAN to "نیلی",
            AppLanguage.PASHTO to "نیلي"
        ),
        "accent_teal" to mapOf(
            AppLanguage.ENGLISH to "Teal",
            AppLanguage.DARI_PERSIAN to "سبز آبی",
            AppLanguage.PASHTO to "شنه آبي"
        ),
        // Profile, Avatar Crop & Privacy
        "profile_photo_title" to mapOf(
            AppLanguage.ENGLISH to "Profile Photo",
            AppLanguage.DARI_PERSIAN to "عکس پروفایل",
            AppLanguage.PASHTO to "د پېژندڅېرې انځور"
        ),
        "change_avatar" to mapOf(
            AppLanguage.ENGLISH to "Choose / Change Photo",
            AppLanguage.DARI_PERSIAN to "انتخاب / تغییر عکس",
            AppLanguage.PASHTO to "انځور ټاکل / بدلول"
        ),
        "remove_avatar" to mapOf(
            AppLanguage.ENGLISH to "Remove Profile Photo",
            AppLanguage.DARI_PERSIAN to "حذف عکس پروفایل",
            AppLanguage.PASHTO to "د پېژندڅېرې انځور لرې کول"
        ),
        "crop_avatar_title" to mapOf(
            AppLanguage.ENGLISH to "Crop & Reposition",
            AppLanguage.DARI_PERSIAN to "برش و تنظیم موقعیت",
            AppLanguage.PASHTO to "انځور سمول او کټ کول"
        ),
        "zoom_label" to mapOf(
            AppLanguage.ENGLISH to "Zoom",
            AppLanguage.DARI_PERSIAN to "بزرگ‌نمایی",
            AppLanguage.PASHTO to "زوم"
        ),
        "preview_save" to mapOf(
            AppLanguage.ENGLISH to "Live Preview",
            AppLanguage.DARI_PERSIAN to "پیش‌نمایش زنده",
            AppLanguage.PASHTO to "ژوندۍ مخکتنه"
        ),
        "save" to mapOf(
            AppLanguage.ENGLISH to "Save",
            AppLanguage.DARI_PERSIAN to "ذخیره",
            AppLanguage.PASHTO to "خوندي کول"
        ),
        "err_load_image" to mapOf(
            AppLanguage.ENGLISH to "Unable to open selected image.",
            AppLanguage.DARI_PERSIAN to "باز کردن عکس انتخاب‌شده ممکن نشد.",
            AppLanguage.PASHTO to "ټاکل شوی انځور پرانیستل نشو."
        ),
        "username_label" to mapOf(
            AppLanguage.ENGLISH to "Username",
            AppLanguage.DARI_PERSIAN to "نام کاربری",
            AppLanguage.PASHTO to "کارن نوم"
        ),
        "username_invalid_hint" to mapOf(
            AppLanguage.ENGLISH to "Use 3–24 letters, numbers, or underscores.",
            AppLanguage.DARI_PERSIAN to "از ۳ تا ۲۴ حرف، عدد یا _ استفاده کنید.",
            AppLanguage.PASHTO to "له ۳ تر ۲۴ توري، شمېرې یا _ وکاروئ."
        ),
        "online_privacy_title" to mapOf(
            AppLanguage.ENGLISH to "Show Online Status",
            AppLanguage.DARI_PERSIAN to "نمایش وضعیت آنلاین",
            AppLanguage.PASHTO to "آنلاین حالت ښودل"
        ),
        "online_privacy_desc" to mapOf(
            AppLanguage.ENGLISH to "Let others see when you are active on VibeRoom.",
            AppLanguage.DARI_PERSIAN to "دیگران متوجه حضور آنلاین شما در برنامه شوند.",
            AppLanguage.PASHTO to "نور کسان وویني چې تاسو آنلاین یاست."
        ),
        "last_seen_title" to mapOf(
            AppLanguage.ENGLISH to "Last Seen Visibility",
            AppLanguage.DARI_PERSIAN to "نمایش آخرین بازدید",
            AppLanguage.PASHTO to "د وروستي لیدنې محرمیت"
        ),
        "last_seen_everyone" to mapOf(
            AppLanguage.ENGLISH to "Everyone",
            AppLanguage.DARI_PERSIAN to "همه",
            AppLanguage.PASHTO to "هرڅوک"
        ),
        "last_seen_contacts" to mapOf(
            AppLanguage.ENGLISH to "Followers",
            AppLanguage.DARI_PERSIAN to "دنبال‌کنندگان",
            AppLanguage.PASHTO to "څارونکي"
        ),
        "last_seen_nobody" to mapOf(
            AppLanguage.ENGLISH to "Nobody",
            AppLanguage.DARI_PERSIAN to "هیچ‌کس",
            AppLanguage.PASHTO to "هیڅوک"
        ),
        "last_seen_recently" to mapOf(
            AppLanguage.ENGLISH to "last seen recently",
            AppLanguage.DARI_PERSIAN to "آخرین بازدید به‌تازگی",
            AppLanguage.PASHTO to "تازه آنلاین و"
        ),
        "online" to mapOf(
            AppLanguage.ENGLISH to "online",
            AppLanguage.DARI_PERSIAN to "آنلاین",
            AppLanguage.PASHTO to "آنلاین"
        ),
        // Chat filters, dates, context menu & forwarding
        "filter_all" to mapOf(
            AppLanguage.ENGLISH to "All",
            AppLanguage.DARI_PERSIAN to "همه",
            AppLanguage.PASHTO to "ټول"
        ),
        "filter_unread" to mapOf(
            AppLanguage.ENGLISH to "Unread",
            AppLanguage.DARI_PERSIAN to "خوانده‌نشده",
            AppLanguage.PASHTO to "نالوستي"
        ),
        "filter_online" to mapOf(
            AppLanguage.ENGLISH to "Online",
            AppLanguage.DARI_PERSIAN to "آنلاین",
            AppLanguage.PASHTO to "آنلاین"
        ),
        "date_today" to mapOf(
            AppLanguage.ENGLISH to "Today",
            AppLanguage.DARI_PERSIAN to "امروز",
            AppLanguage.PASHTO to "نن"
        ),
        "date_yesterday" to mapOf(
            AppLanguage.ENGLISH to "Yesterday",
            AppLanguage.DARI_PERSIAN to "دیروز",
            AppLanguage.PASHTO to "پرون"
        ),
        "copy_label" to mapOf(
            AppLanguage.ENGLISH to "Copy",
            AppLanguage.DARI_PERSIAN to "کپی",
            AppLanguage.PASHTO to "کاپي"
        ),
        "copied_notice" to mapOf(
            AppLanguage.ENGLISH to "Copied to clipboard",
            AppLanguage.DARI_PERSIAN to "در حافظه کپی شد",
            AppLanguage.PASHTO to "کاپي شو"
        ),
        "forward_label" to mapOf(
            AppLanguage.ENGLISH to "Forward",
            AppLanguage.DARI_PERSIAN to "ارسال به دیگری",
            AppLanguage.PASHTO to "بل چاته لېږل"
        ),
        "forwarded_badge" to mapOf(
            AppLanguage.ENGLISH to "Forwarded",
            AppLanguage.DARI_PERSIAN to "هدایت‌شده",
            AppLanguage.PASHTO to "لېږل شوی"
        ),
        "forward_to_title" to mapOf(
            AppLanguage.ENGLISH to "Forward Message To",
            AppLanguage.DARI_PERSIAN to "ارسال پیام به",
            AppLanguage.PASHTO to "پیغام چاته ولېږل شي"
        ),
        "notif_forwarded" to mapOf(
            AppLanguage.ENGLISH to "Message forwarded",
            AppLanguage.DARI_PERSIAN to "پیام ارسال شد",
            AppLanguage.PASHTO to "پیغام ولېږل شو"
        ),
        // Avatar Studio & Live Frames
        "avatar_studio_title" to mapOf(
            AppLanguage.ENGLISH to "Avatar & Frame Studio",
            AppLanguage.DARI_PERSIAN to "استودیوی آواتار و فریم",
            AppLanguage.PASHTO to "د آواتار او چوکاټ سټوډیو"
        ),
        "avatar_frames_tab" to mapOf(
            AppLanguage.ENGLISH to "Live Frames",
            AppLanguage.DARI_PERSIAN to "فریم‌های زنده",
            AppLanguage.PASHTO to "ژوندي چوکاټونه"
        ),
        "create_avatar_tab" to mapOf(
            AppLanguage.ENGLISH to "Create Avatar",
            AppLanguage.DARI_PERSIAN to "ساخت آواتار",
            AppLanguage.PASHTO to "آواتار جوړول"
        ),
        "live_frames_subtitle" to mapOf(
            AppLanguage.ENGLISH to "Choose a glowing animated frame around your profile avatar:",
            AppLanguage.DARI_PERSIAN to "یک فریم متحرک و جذاب برای دور عکس پروفایل خود انتخاب کنید:",
            AppLanguage.PASHTO to "د خپل پېژندڅېرې شاوخوا یو خوځنده چوکاټ وټاکئ:"
        ),
        "pick_backdrop" to mapOf(
            AppLanguage.ENGLISH to "Background Style",
            AppLanguage.DARI_PERSIAN to "رنگ و طرح پس‌زمینه",
            AppLanguage.PASHTO to "د شالید رنګ"
        ),
        "pick_symbol_or_initials" to mapOf(
            AppLanguage.ENGLISH to "Symbol or Initials",
            AppLanguage.DARI_PERSIAN to "نماد یا حروف اول اسم",
            AppLanguage.PASHTO to "نښان یا د نوم لومړي توري"
        ),
        "use_emoji_symbol" to mapOf(
            AppLanguage.ENGLISH to "Use Symbol",
            AppLanguage.DARI_PERSIAN to "انتخاب نماد",
            AppLanguage.PASHTO to "نښان کارول"
        ),
        "use_initials_label" to mapOf(
            AppLanguage.ENGLISH to "Use Initials",
            AppLanguage.DARI_PERSIAN to "حروف اسم",
            AppLanguage.PASHTO to "د نوم توري"
        ),
        "apply_custom_avatar" to mapOf(
            AppLanguage.ENGLISH to "Apply Custom Avatar",
            AppLanguage.DARI_PERSIAN to "ثبت آواتار سفارشی",
            AppLanguage.PASHTO to "دودیز آواتار پلي کول"
        ),
        "frame_none" to mapOf(
            AppLanguage.ENGLISH to "Classic",
            AppLanguage.DARI_PERSIAN to "ساده",
            AppLanguage.PASHTO to "ساده"
        ),
        "frame_neon_pulse" to mapOf(
            AppLanguage.ENGLISH to "Neon Pulse",
            AppLanguage.DARI_PERSIAN to "نئون تپنده",
            AppLanguage.PASHTO to "نیون رڼا"
        ),
        "frame_golden_halo" to mapOf(
            AppLanguage.ENGLISH to "Golden Halo",
            AppLanguage.DARI_PERSIAN to "هاله طلایی",
            AppLanguage.PASHTO to "زرین چوکاټ"
        ),
        "frame_aurora_wave" to mapOf(
            AppLanguage.ENGLISH to "Aurora Wave",
            AppLanguage.DARI_PERSIAN to "موج شفق",
            AppLanguage.PASHTO to "آسماني څپه"
        ),
        "frame_cyber_prism" to mapOf(
            AppLanguage.ENGLISH to "Cyber Prism",
            AppLanguage.DARI_PERSIAN to "منشور سایبری",
            AppLanguage.PASHTO to "سایبر منشور"
        ),
        "frame_emerald_flame" to mapOf(
            AppLanguage.ENGLISH to "Emerald Flame",
            AppLanguage.DARI_PERSIAN to "شعله زمردی",
            AppLanguage.PASHTO to "زمرد لمبه"
        ),
        "frame_blossom_glow" to mapOf(
            AppLanguage.ENGLISH to "Blossom Glow",
            AppLanguage.DARI_PERSIAN to "درخشش گل",
            AppLanguage.PASHTO to "ګلابي رڼا"
        ),
        "frame_cosmic_ring" to mapOf(
            AppLanguage.ENGLISH to "Cosmic Ring",
            AppLanguage.DARI_PERSIAN to "حلقه کیهانی",
            AppLanguage.PASHTO to "کهکشاني کړۍ"
        ),
        // Account details & followers list
        "recent_posts_label" to mapOf(
            AppLanguage.ENGLISH to "Recent Public Posts",
            AppLanguage.DARI_PERSIAN to "پست‌های عمومی اخیر",
            AppLanguage.PASHTO to "وروستي عامه پوسټونه"
        ),
        "no_follow_users_yet" to mapOf(
            AppLanguage.ENGLISH to "No users in this list yet.",
            AppLanguage.DARI_PERSIAN to "هنوز کسی در این لیست نیست.",
            AppLanguage.PASHTO to "په دې لیست کې لا څوک نشته."
        ),
        // Chat translation, Education & Auth
        "chat_translate_to" to mapOf(
            AppLanguage.ENGLISH to "Translate to",
            AppLanguage.DARI_PERSIAN to "ترجمه به",
            AppLanguage.PASHTO to "ژباړه په"
        ),
        "translate_incoming" to mapOf(
            AppLanguage.ENGLISH to "Translate message",
            AppLanguage.DARI_PERSIAN to "ترجمه پیام",
            AppLanguage.PASHTO to "پیغام ژباړل"
        ),
        "clear_chat" to mapOf(
            AppLanguage.ENGLISH to "Clear chat",
            AppLanguage.DARI_PERSIAN to "پاک کردن گفتگو",
            AppLanguage.PASHTO to "خبرې پاکول"
        ),
        "quiz_topic_label" to mapOf(
            AppLanguage.ENGLISH to "Topic",
            AppLanguage.DARI_PERSIAN to "موضوع تمرین",
            AppLanguage.PASHTO to "د تمرین موضوع"
        ),
        "next_question" to mapOf(
            AppLanguage.ENGLISH to "Next Question",
            AppLanguage.DARI_PERSIAN to "سؤال بعدی",
            AppLanguage.PASHTO to "بله پوښتنه"
        ),
        "auto_signing_in" to mapOf(
            AppLanguage.ENGLISH to "Checking saved device account...",
            AppLanguage.DARI_PERSIAN to "در حال بررسی حساب ذخیره شده...",
            AppLanguage.PASHTO to "خوندي شوی اکونټ کتل کېږي..."
        ),
        // Additional live avatar frames & gallery
        "frame_royal_crown_orb" to mapOf(
            AppLanguage.ENGLISH to "Royal Crown",
            AppLanguage.DARI_PERSIAN to "تاج سلطنتی",
            AppLanguage.PASHTO to "شاهي تاج"
        ),
        "frame_solar_flare" to mapOf(
            AppLanguage.ENGLISH to "Solar Flare",
            AppLanguage.DARI_PERSIAN to "شراره خورشیدی",
            AppLanguage.PASHTO to "لمریز ځلا"
        ),
        "live_frames_gallery_title" to mapOf(
            AppLanguage.ENGLISH to "Live Profile Frames",
            AppLanguage.DARI_PERSIAN to "فریم‌های زنده پروفایل",
            AppLanguage.PASHTO to "د پروفایل ژوندي چوکاټونه"
        ),
        "live_frames_gallery_desc" to mapOf(
            AppLanguage.ENGLISH to "Choose from 9+ animated live frames for your avatar",
            AppLanguage.DARI_PERSIAN to "از میان بیش از ۹ فریم زنده و متحرک انتخاب کنید",
            AppLanguage.PASHTO to "له ۹ څخه ډېر ژوندي چوکاټونه غوره کړئ"
        ),
        "all_frames_cta" to mapOf(
            AppLanguage.ENGLISH to "All Frames",
            AppLanguage.DARI_PERSIAN to "همه فریم‌ها",
            AppLanguage.PASHTO to "ټول چوکاټونه"
        ),
        // Translation settings & inline error handling
        "translation_settings_title" to mapOf(
            AppLanguage.ENGLISH to "Translation Settings",
            AppLanguage.DARI_PERSIAN to "تنظیمات ترجمه",
            AppLanguage.PASHTO to "د ژباړې ترتیبات"
        ),
        "translation_settings_desc" to mapOf(
            AppLanguage.ENGLISH to "Choose default target language and auto-translation behavior",
            AppLanguage.DARI_PERSIAN to "زبان مقصد پیش‌فرض و ترجمه خودکار گفتگوها را تنظیم کنید",
            AppLanguage.PASHTO to "خپله اصلي ژبه او اتومات ژباړه وټاکئ"
        ),
        "default_translate_target" to mapOf(
            AppLanguage.ENGLISH to "Default target language",
            AppLanguage.DARI_PERSIAN to "زبان پیش‌فرض ترجمه",
            AppLanguage.PASHTO to "د ژباړې اصلي ژبه"
        ),
        "auto_translate_chat_title" to mapOf(
            AppLanguage.ENGLISH to "Auto-translate incoming chat",
            AppLanguage.DARI_PERSIAN to "ترجمه خودکار پیام‌های دریافتی",
            AppLanguage.PASHTO to "د راتلونکو پیغامونو اتومات ژباړه"
        ),
        "auto_translate_chat_desc" to mapOf(
            AppLanguage.ENGLISH to "Automatically translate incoming chat messages",
            AppLanguage.DARI_PERSIAN to "پیام‌های ورودی چت را به صورت خودکار به زبان شما ترجمه می‌کند",
            AppLanguage.PASHTO to "نوي راتلونکي پیغامونه په اتومات ډول ستاسو ژبې ته ژباړي"
        ),
        "show_translation_notes_title" to mapOf(
            AppLanguage.ENGLISH to "Show pronunciation & phrase notes",
            AppLanguage.DARI_PERSIAN to "نمایش تلفظ و نکات کاربردی",
            AppLanguage.PASHTO to "د تلفظ او جملې لارښوونې ښودل"
        ),
        "show_translation_notes_desc" to mapOf(
            AppLanguage.ENGLISH to "Display pronunciation hints under translated text",
            AppLanguage.DARI_PERSIAN to "راهنمای تلفظ و مثال را زیر ترجمه نشان می‌دهد",
            AppLanguage.PASHTO to "د ژباړې لاندې د تلفظ لارښود ښيي"
        ),
        "auto_translate_short" to mapOf(
            AppLanguage.ENGLISH to "Auto",
            AppLanguage.DARI_PERSIAN to "خودکار",
            AppLanguage.PASHTO to "اتومات"
        ),
        "translation_error_retry" to mapOf(
            AppLanguage.ENGLISH to "Translation failed. Tap Retry.",
            AppLanguage.DARI_PERSIAN to "خطا در ترجمه. دوباره تلاش کنید.",
            AppLanguage.PASHTO to "ژباړه ناکامه شوه. بیا هڅه وکړئ."
        ),
        "retry_label" to mapOf(
            AppLanguage.ENGLISH to "Retry",
            AppLanguage.DARI_PERSIAN to "تلاش مجدد",
            AppLanguage.PASHTO to "بیا هڅه"
        ),
        "hide_translation" to mapOf(
            AppLanguage.ENGLISH to "Hide",
            AppLanguage.DARI_PERSIAN to "بستن",
            AppLanguage.PASHTO to "پټول"
        ),
        "swap_languages" to mapOf(
            AppLanguage.ENGLISH to "Swap languages",
            AppLanguage.DARI_PERSIAN to "جابجایی زبان‌ها",
            AppLanguage.PASHTO to "ژبې بدلول"
        ),
        // 5-rank learning language proficiency & structured menu pickers
        "rank_1_label" to mapOf(
            AppLanguage.ENGLISH to "Rank 1 • A1 Beginner",
            AppLanguage.DARI_PERSIAN to "رتبه ۱ • مبتدی (A1)",
            AppLanguage.PASHTO to "لومړۍ درجه • پیل (A1)"
        ),
        "rank_1_short" to mapOf(
            AppLanguage.ENGLISH to "Beginner",
            AppLanguage.DARI_PERSIAN to "مبتدی",
            AppLanguage.PASHTO to "پیل"
        ),
        "rank_2_label" to mapOf(
            AppLanguage.ENGLISH to "Rank 2 • A2 Elementary",
            AppLanguage.DARI_PERSIAN to "رتبه ۲ • پایه (A2)",
            AppLanguage.PASHTO to "دویمه درجه • بنسټیز (A2)"
        ),
        "rank_2_short" to mapOf(
            AppLanguage.ENGLISH to "Elementary",
            AppLanguage.DARI_PERSIAN to "پایه",
            AppLanguage.PASHTO to "بنسټیز"
        ),
        "rank_3_label" to mapOf(
            AppLanguage.ENGLISH to "Rank 3 • B1 Intermediate",
            AppLanguage.DARI_PERSIAN to "رتبه ۳ • متوسط (B1)",
            AppLanguage.PASHTO to "درېیمه درجه • منځنی (B1)"
        ),
        "rank_3_short" to mapOf(
            AppLanguage.ENGLISH to "Intermediate",
            AppLanguage.DARI_PERSIAN to "متوسط",
            AppLanguage.PASHTO to "منځنی"
        ),
        "rank_4_label" to mapOf(
            AppLanguage.ENGLISH to "Rank 4 • B2 Upper-Inter",
            AppLanguage.DARI_PERSIAN to "رتبه ۴ • نیمه‌پیشرفته (B2)",
            AppLanguage.PASHTO to "څلورمه درجه • پورته منځنی (B2)"
        ),
        "rank_4_short" to mapOf(
            AppLanguage.ENGLISH to "Upper-Inter",
            AppLanguage.DARI_PERSIAN to "نیمه‌پیشرفته",
            AppLanguage.PASHTO to "پورته منځنی"
        ),
        "rank_5_label" to mapOf(
            AppLanguage.ENGLISH to "Rank 5 • C1-C2 Fluent",
            AppLanguage.DARI_PERSIAN to "رتبه ۵ • روان / مسلط (C1-C2)",
            AppLanguage.PASHTO to "پنځمه درجه • روان (C1-C2)"
        ),
        "rank_5_short" to mapOf(
            AppLanguage.ENGLISH to "Fluent",
            AppLanguage.DARI_PERSIAN to "روان",
            AppLanguage.PASHTO to "روان"
        ),
        "add_language_from_menu" to mapOf(
            AppLanguage.ENGLISH to "Add language from menu",
            AppLanguage.DARI_PERSIAN to "افزودن زبان از فهرست",
            AppLanguage.PASHTO to "له لیست څخه ژبه زیاتول"
        ),
        "learning_languages_ranks_hint" to mapOf(
            AppLanguage.ENGLISH to "Select each language and pick its 5-level rank (A1–C2)",
            AppLanguage.DARI_PERSIAN to "زبان‌ها را از منو انتخاب کرده و رتبه ۱ تا ۵ هر زبان را بزنید",
            AppLanguage.PASHTO to "ژبه له مینو وټاکئ او له ۵ درجو یوه غوره کړئ"
        ),
        "pick_interests_from_list" to mapOf(
            AppLanguage.ENGLISH to "Select interests from list",
            AppLanguage.DARI_PERSIAN to "انتخاب علایق از لیست",
            AppLanguage.PASHTO to "له لیست څخه خوښې وټاکئ"
        ),
        "spoken_languages_menu" to mapOf(
            AppLanguage.ENGLISH to "Spoken languages",
            AppLanguage.DARI_PERSIAN to "زبان‌های گفتاری",
            AppLanguage.PASHTO to "د خبرو ژبې"
        ),
        "native_languages_menu" to mapOf(
            AppLanguage.ENGLISH to "Native languages",
            AppLanguage.DARI_PERSIAN to "زبان‌های مادری",
            AppLanguage.PASHTO to "مورنۍ ژبې"
        ),
        "no_languages_added" to mapOf(
            AppLanguage.ENGLISH to "Tap the menu above to pick languages you are learning.",
            AppLanguage.DARI_PERSIAN to "از منوی بالا زبان‌هایی که یاد می‌گیرید را انتخاب کنید.",
            AppLanguage.PASHTO to "د زده کړې ژبې له پورته مینو وټاکئ."
        ),
        "interest_cat_culture" to mapOf(
            AppLanguage.ENGLISH to "Culture & Learning",
            AppLanguage.DARI_PERSIAN to "فرهنگ و یادگیری",
            AppLanguage.PASHTO to "کلتور او زده کړه"
        ),
        "interest_cat_media" to mapOf(
            AppLanguage.ENGLISH to "Arts & Media",
            AppLanguage.DARI_PERSIAN to "هنر و رسانه",
            AppLanguage.PASHTO to "هنر او رسنۍ"
        ),
        "interest_cat_tech" to mapOf(
            AppLanguage.ENGLISH to "Tech & Science",
            AppLanguage.DARI_PERSIAN to "فناوری و علم",
            AppLanguage.PASHTO to "ټکنالوژي او ساینس"
        ),
        "interest_cat_lifestyle" to mapOf(
            AppLanguage.ENGLISH to "Lifestyle",
            AppLanguage.DARI_PERSIAN to "سبک زندگی",
            AppLanguage.PASHTO to "د ژوند بڼه"
        ),
        // Voice rooms redesign & WebRTC moderation
        "create_room_cta" to mapOf(
            AppLanguage.ENGLISH to "Create Room",
            AppLanguage.DARI_PERSIAN to "ساخت روم",
            AppLanguage.PASHTO to "خونه جوړول"
        ),
        "search_rooms_hint" to mapOf(
            AppLanguage.ENGLISH to "Search rooms, hosts or tags...",
            AppLanguage.DARI_PERSIAN to "جستجوی روم‌ها، میزبان یا برچسب...",
            AppLanguage.PASHTO to "خونې، کوربه یا ټګونه ولټوئ..."
        ),
        "webrtc_connecting_badge" to mapOf(
            AppLanguage.ENGLISH to "Connecting Audio...",
            AppLanguage.DARI_PERSIAN to "در حال اتصال صوتی...",
            AppLanguage.PASHTO to "غږ نښلېږي..."
        ),
        "webrtc_peers_connected" to mapOf(
            AppLanguage.ENGLISH to "connected audio",
            AppLanguage.DARI_PERSIAN to "نفر متصل صوتی",
            AppLanguage.PASHTO to "ژوندي غږونه"
        ),
        "raised_hands_queue" to mapOf(
            AppLanguage.ENGLISH to "Raised Hands",
            AppLanguage.DARI_PERSIAN to "دست‌های بالا برای صحبت",
            AppLanguage.PASHTO to "پورته شوي لاسونه"
        ),
        "allow_to_speak" to mapOf(
            AppLanguage.ENGLISH to "Invite to Stage",
            AppLanguage.DARI_PERSIAN to "دعوت به صحنه",
            AppLanguage.PASHTO to "سټیج ته راوستل"
        ),
        "dismiss_hand" to mapOf(
            AppLanguage.ENGLISH to "Dismiss",
            AppLanguage.DARI_PERSIAN to "رد",
            AppLanguage.PASHTO to "ردول"
        ),
        "mute_speaker" to mapOf(
            AppLanguage.ENGLISH to "Mute Mic",
            AppLanguage.DARI_PERSIAN to "قطع میکروفون",
            AppLanguage.PASHTO to "مایک بندول"
        ),
        "unmute_speaker" to mapOf(
            AppLanguage.ENGLISH to "Unmute Mic",
            AppLanguage.DARI_PERSIAN to "وصل میکروفون",
            AppLanguage.PASHTO to "مایک پرانیستل"
        ),
        "move_to_audience" to mapOf(
            AppLanguage.ENGLISH to "Move to Audience",
            AppLanguage.DARI_PERSIAN to "انتقال به شنوندگان",
            AppLanguage.PASHTO to "اورېدونکو ته استول"
        ),
        "request_stage" to mapOf(
            AppLanguage.ENGLISH to "Request Mic",
            AppLanguage.DARI_PERSIAN to "درخواست میکروفون",
            AppLanguage.PASHTO to "مایک غوښتل"
        ),
        "raise_hand_to_speak_hint" to mapOf(
            AppLanguage.ENGLISH to "Hand raised! The host can bring you up to speak.",
            AppLanguage.DARI_PERSIAN to "دست شما بالا رفت؛ میزبان می‌تواند شما را به صحنه بیاورد.",
            AppLanguage.PASHTO to "لاس مو پورته شو؛ کوربه مو سټیج ته راوستلی شي."
        ),
        "room_host_actions" to mapOf(
            AppLanguage.ENGLISH to "Host Controls",
            AppLanguage.DARI_PERSIAN to "کنترل میزبان",
            AppLanguage.PASHTO to "د کوربه کنټرول"
        ),
        "composer_more_tools" to mapOf(
            AppLanguage.ENGLISH to "Attachments & voice note",
            AppLanguage.DARI_PERSIAN to "ارسال عکس و پیام صوتی",
            AppLanguage.PASHTO to "انځور او غږیز پیغام"
        ),
        "pick_room_tags" to mapOf(
            AppLanguage.ENGLISH to "Select room topics",
            AppLanguage.DARI_PERSIAN to "موضوعات روم را انتخاب کنید",
            AppLanguage.PASHTO to "د خونې موضوعګانې وټاکئ"
        ),
        // Vibe DNA, Gamification & Leaderboard
        "vibe_match_label" to mapOf(
            AppLanguage.ENGLISH to "Vibe Match",
            AppLanguage.DARI_PERSIAN to "تطابق وایب",
            AppLanguage.PASHTO to "وایب مطابقت"
        ),
        "vibe_level_label" to mapOf(
            AppLanguage.ENGLISH to "Level",
            AppLanguage.DARI_PERSIAN to "سطح",
            AppLanguage.PASHTO to "کچه"
        ),
        "vibe_xp_label" to mapOf(
            AppLanguage.ENGLISH to "Vibe XP",
            AppLanguage.DARI_PERSIAN to "امتیاز وایب",
            AppLanguage.PASHTO to "وایب XP"
        ),
        "leaderboard_title" to mapOf(
            AppLanguage.ENGLISH to "Top Vibe Explorers",
            AppLanguage.DARI_PERSIAN to "برترین اعضای وایب",
            AppLanguage.PASHTO to "غوره غړي"
        ),
        "daily_missions_title" to mapOf(
            AppLanguage.ENGLISH to "Daily Missions",
            AppLanguage.DARI_PERSIAN to "ماموریت‌های روزانه",
            AppLanguage.PASHTO to "ورځني ماموریتونه"
        ),
        "mission_practice_ai" to mapOf(
            AppLanguage.ENGLISH to "Practice with AI Coach or Flashcards",
            AppLanguage.DARI_PERSIAN to "تمرین با مربی هوشمند یا فلش‌کارت",
            AppLanguage.PASHTO to "له هوشیار روزونکي یا فلش کارډ سره تمرین"
        ),
        "mission_share_moment" to mapOf(
            AppLanguage.ENGLISH to "Share a Moment, Poll, or Q&A",
            AppLanguage.DARI_PERSIAN to "اشتراک‌گذاری پست، نظرسنجی یا پرسش",
            AppLanguage.PASHTO to "پوسټ، نظرپوښتنه یا پوښتنه خپره کړئ"
        ),
        "badges_title" to mapOf(
            AppLanguage.ENGLISH to "Achievements & Badges",
            AppLanguage.DARI_PERSIAN to "دستاوردها و نشان‌ها",
            AppLanguage.PASHTO to "لاسته راوړنې او نښانونه"
        ),
        // Feed Polls, Q&A & AI Writing Assistant
        "post_type_standard" to mapOf(
            AppLanguage.ENGLISH to "Moment",
            AppLanguage.DARI_PERSIAN to "پست عادی",
            AppLanguage.PASHTO to "پوسټ"
        ),
        "post_type_poll" to mapOf(
            AppLanguage.ENGLISH to "Poll",
            AppLanguage.DARI_PERSIAN to "نظرسنجی",
            AppLanguage.PASHTO to "نظرپوښتنه"
        ),
        "post_type_qa" to mapOf(
            AppLanguage.ENGLISH to "Q&A",
            AppLanguage.DARI_PERSIAN to "پرسش و پاسخ",
            AppLanguage.PASHTO to "پوښتنه او ځواب"
        ),
        "poll_option_hint" to mapOf(
            AppLanguage.ENGLISH to "Poll option",
            AppLanguage.DARI_PERSIAN to "گزینه نظرسنجی",
            AppLanguage.PASHTO to "د نظرپوښتنې اختیار"
        ),
        "add_poll_option" to mapOf(
            AppLanguage.ENGLISH to "Add Option",
            AppLanguage.DARI_PERSIAN to "افزودن گزینه",
            AppLanguage.PASHTO to "اختیار زیات کړئ"
        ),
        "total_votes_label" to mapOf(
            AppLanguage.ENGLISH to "votes",
            AppLanguage.DARI_PERSIAN to "رای",
            AppLanguage.PASHTO to "رایې"
        ),
        "ai_polish_draft" to mapOf(
            AppLanguage.ENGLISH to "AI Polish",
            AppLanguage.DARI_PERSIAN to "بهبود متن با هوش مصنوعی",
            AppLanguage.PASHTO to "د AI له لارې اصلاح"
        ),
        // Chat Disappearing Timer & AI Smart Replies
        "disappearing_label" to mapOf(
            AppLanguage.ENGLISH to "Timer",
            AppLanguage.DARI_PERSIAN to "محوشونده",
            AppLanguage.PASHTO to "ورکېدونکی"
        ),
        "timer_off" to mapOf(
            AppLanguage.ENGLISH to "Off",
            AppLanguage.DARI_PERSIAN to "خاموش",
            AppLanguage.PASHTO to "بند"
        ),
        "ai_smart_replies" to mapOf(
            AppLanguage.ENGLISH to "AI Replies",
            AppLanguage.DARI_PERSIAN to "پیشنهاد هوشمند",
            AppLanguage.PASHTO to "هوشیار ځوابونه"
        ),
        // Voice Room Private Mode, Co-hosts, Party Game, Captions & Consent Summary
        "private_room_toggle" to mapOf(
            AppLanguage.ENGLISH to "Private / Invite-only stage",
            AppLanguage.DARI_PERSIAN to "روم خصوصی (فقط با دعوت)",
            AppLanguage.PASHTO to "ځانګړې خونه (یوازې بلنه)"
        ),
        "private_badge" to mapOf(
            AppLanguage.ENGLISH to "Private",
            AppLanguage.DARI_PERSIAN to "خصوصی",
            AppLanguage.PASHTO to "ځانګړې"
        ),
        "cohost_badge" to mapOf(
            AppLanguage.ENGLISH to "CO-HOST",
            AppLanguage.DARI_PERSIAN to "هم‌میزبان",
            AppLanguage.PASHTO to "مرستیال کوربه"
        ),
        "make_cohost" to mapOf(
            AppLanguage.ENGLISH to "Make Co-Host",
            AppLanguage.DARI_PERSIAN to "ارتقا به هم‌میزبان",
            AppLanguage.PASHTO to "مرستیال کوربه کول"
        ),
        "remove_cohost" to mapOf(
            AppLanguage.ENGLISH to "Remove Co-Host",
            AppLanguage.DARI_PERSIAN to "حذف هم‌میزبان",
            AppLanguage.PASHTO to "مرستیال لرې کول"
        ),
        "party_game_cta" to mapOf(
            AppLanguage.ENGLISH to "Party Game",
            AppLanguage.DARI_PERSIAN to "بازی صوتی",
            AppLanguage.PASHTO to "غږیزه لوبه"
        ),
        "live_captions_cta" to mapOf(
            AppLanguage.ENGLISH to "Captions",
            AppLanguage.DARI_PERSIAN to "زیرنویس زنده",
            AppLanguage.PASHTO to "ژوندي سرلیکونه"
        ),
        "ai_summary_cta" to mapOf(
            AppLanguage.ENGLISH to "AI Summary",
            AppLanguage.DARI_PERSIAN to "خلاصه هوشمند",
            AppLanguage.PASHTO to "د AI لنډیز"
        ),
        "ai_summary_consent_title" to mapOf(
            AppLanguage.ENGLISH to "Generate AI Room Summary?",
            AppLanguage.DARI_PERSIAN to "تولید خلاصه هوشمند روم؟",
            AppLanguage.PASHTO to "د خونې هوشیار لنډیز جوړ شي؟"
        ),
        "ai_summary_consent_body" to mapOf(
            AppLanguage.ENGLISH to "With your consent, Vibe AI will summarize this room's topic and participating speakers.",
            AppLanguage.DARI_PERSIAN to "با رضایت شما، موضوع و نکات کلیدی این جلسه صوتی خلاصه می‌شود.",
            AppLanguage.PASHTO to "ستاسو په خوښه به د دې خونې موضوع او لنډیز جوړ شي."
        ),
        // Privacy Control Center
        "privacy_center_summary" to mapOf(
            AppLanguage.ENGLISH to "Manage visibility, messaging, presence & safety controls",
            AppLanguage.DARI_PERSIAN to "مدیریت نمایش پروفایل، پیام‌ها، وضعیت آنلاین و امنیت",
            AppLanguage.PASHTO to "د لید، پیغامونو، آنلاین حالت او خوندیتوب کنټرول"
        ),
        // Voice Room Path A Rebuild
        "vr_gifts_title" to mapOf(
            AppLanguage.ENGLISH to "Gift Studio",
            AppLanguage.DARI_PERSIAN to "مرکز هدایا",
            AppLanguage.PASHTO to "د ډالیو سټوډیو"
        ),
        "vr_soundboard_title" to mapOf(
            AppLanguage.ENGLISH to "Live Soundboard",
            AppLanguage.DARI_PERSIAN to "افکت‌های صوتی زنده",
            AppLanguage.PASHTO to "ژوندي غږیز افکتونه"
        ),
        "vr_room_chat_title" to mapOf(
            AppLanguage.ENGLISH to "Stage Chat & Polls",
            AppLanguage.DARI_PERSIAN to "چت اتاق و نظرسنجی",
            AppLanguage.PASHTO to "د خونې چټ او نظرپوښتنې"
        ),
        "vr_moderator_title" to mapOf(
            AppLanguage.ENGLISH to "Stage & Moderator Deck",
            AppLanguage.DARI_PERSIAN to "پنل مدیریت و تنظیمات صحنه",
            AppLanguage.PASHTO to "د سټیج او مدیریت پینل"
        ),
        "vr_background_themes" to mapOf(
            AppLanguage.ENGLISH to "Stage Atmosphere",
            AppLanguage.DARI_PERSIAN to "پس‌زمینه زنده صحنه",
            AppLanguage.PASHTO to "د سټیج شالید"
        ),
        "vr_entrance_styles" to mapOf(
            AppLanguage.ENGLISH to "VIP Entrance Animation",
            AppLanguage.DARI_PERSIAN to "انیمیشن ورود به اتاق",
            AppLanguage.PASHTO to "د ننوتلو انیمیشن"
        ),
        "vr_stage_lock" to mapOf(
            AppLanguage.ENGLISH to "Lock Stage (Invite Only)",
            AppLanguage.DARI_PERSIAN to "قفل صحنه (فقط با دعوت)",
            AppLanguage.PASHTO to "سټیج قلفول (یوازې بلنه)"
        ),
        "vr_mute_all" to mapOf(
            AppLanguage.ENGLISH to "Mute All Speakers",
            AppLanguage.DARI_PERSIAN to "قطع میکروفون همه",
            AppLanguage.PASHTO to "ټول مایکونه بندول"
        ),
        "vr_kick" to mapOf(
            AppLanguage.ENGLISH to "Kick from Room",
            AppLanguage.DARI_PERSIAN to "اخراج موقت",
            AppLanguage.PASHTO to "له خونې ایستل"
        ),
        "vr_ban" to mapOf(
            AppLanguage.ENGLISH to "Ban from Room",
            AppLanguage.DARI_PERSIAN to "مسدودیت از اتاق",
            AppLanguage.PASHTO to "له خونې بندول"
        ),
        "vr_make_moderator" to mapOf(
            AppLanguage.ENGLISH to "Make Moderator",
            AppLanguage.DARI_PERSIAN to "ارتقا به ناظر",
            AppLanguage.PASHTO to "مدیر ټاکل"
        ),
        "vr_remove_moderator" to mapOf(
            AppLanguage.ENGLISH to "Remove Moderator",
            AppLanguage.DARI_PERSIAN to "لغو نظارت",
            AppLanguage.PASHTO to "مدیریت لرې کول"
        ),
        "vr_mod_badge" to mapOf(
            AppLanguage.ENGLISH to "MOD",
            AppLanguage.DARI_PERSIAN to "ناظر",
            AppLanguage.PASHTO to "مدیر"
        ),
        "vr_successor_host" to mapOf(
            AppLanguage.ENGLISH to "Successor Host on Leave",
            AppLanguage.DARI_PERSIAN to "جانشین میزبان هنگام خروج",
            AppLanguage.PASHTO to "د وتلو پر مهال ځای ناستی کوربه"
        ),
        "vr_low_power_mode" to mapOf(
            AppLanguage.ENGLISH to "Low-Power Canvas Mode",
            AppLanguage.DARI_PERSIAN to "حالت کم‌مصرف انیمیشن",
            AppLanguage.PASHTO to "د بیټرۍ سپما حالت"
        ),
        "vr_disable_gift_fx" to mapOf(
            AppLanguage.ENGLISH to "Show Gift Animations",
            AppLanguage.DARI_PERSIAN to "نمایش انیمیشن هدایا",
            AppLanguage.PASHTO to "د ډالیو انیمیشن ښودل"
        ),
        "vr_disable_entrance_fx" to mapOf(
            AppLanguage.ENGLISH to "Show Entrance Animations",
            AppLanguage.DARI_PERSIAN to "نمایش انیمیشن ورود",
            AppLanguage.PASHTO to "د ننوتلو انیمیشن ښودل"
        ),
        "vr_sound_volume" to mapOf(
            AppLanguage.ENGLISH to "Soundboard Volume",
            AppLanguage.DARI_PERSIAN to "صدای افکت‌ها",
            AppLanguage.PASHTO to "د غږونو اندازه"
        ),
        "vr_stop_sounds" to mapOf(
            AppLanguage.ENGLISH to "Stop Audio",
            AppLanguage.DARI_PERSIAN to "توقف صدا",
            AppLanguage.PASHTO to "غږ درول"
        ),
        "vr_audio_route" to mapOf(
            AppLanguage.ENGLISH to "Audio Output",
            AppLanguage.DARI_PERSIAN to "خروجی صدا",
            AppLanguage.PASHTO to "د غږ لاره"
        ),
        "vr_send_gift" to mapOf(
            AppLanguage.ENGLISH to "Send Gift",
            AppLanguage.DARI_PERSIAN to "ارسال هدیه",
            AppLanguage.PASHTO to "ډالۍ استول"
        ),
        "vr_free_badge" to mapOf(
            AppLanguage.ENGLISH to "FREE",
            AppLanguage.DARI_PERSIAN to "رایگان",
            AppLanguage.PASHTO to "وړیا"
        ),
        "vr_leaderboard_tab" to mapOf(
            AppLanguage.ENGLISH to "Supporters",
            AppLanguage.DARI_PERSIAN to "حامیان برتر",
            AppLanguage.PASHTO to "غوره ملاتړي"
        ),
        "vr_create_poll" to mapOf(
            AppLanguage.ENGLISH to "Create Live Poll",
            AppLanguage.DARI_PERSIAN to "ایجاد نظرسنجی زنده",
            AppLanguage.PASHTO to "ژوندۍ نظرپوښتنه جوړول"
        ),
        "vr_pinned_notice" to mapOf(
            AppLanguage.ENGLISH to "Pinned Stage Notice",
            AppLanguage.DARI_PERSIAN to "اعلان سنجاق‌شده صحنه",
            AppLanguage.PASHTO to "پین شوی خبرتیا"
        ),
        "vr_chat_placeholder" to mapOf(
            AppLanguage.ENGLISH to "Say something to the room...",
            AppLanguage.DARI_PERSIAN to "پیامی به اتاق بنویسید...",
            AppLanguage.PASHTO to "خونې ته یو پیغام ولیکئ..."
        ),
        "vr_banned_notice" to mapOf(
            AppLanguage.ENGLISH to "You are banned from joining this voice room.",
            AppLanguage.DARI_PERSIAN to "دسترسی شما به این اتاق صوتی مسدود شده است.",
            AppLanguage.PASHTO to "تاسو له دې خونې څخه بند شوي یاست."
        ),
        "vr_room_ended_notice" to mapOf(
            AppLanguage.ENGLISH to "This voice room has ended.",
            AppLanguage.DARI_PERSIAN to "این اتاق صوتی به پایان رسید.",
            AppLanguage.PASHTO to "دا غږیزه خونه پای ته ورسېده."
        ),
        "vr_stage_locked_notice" to mapOf(
            AppLanguage.ENGLISH to "Stage is locked by the host (invite only).",
            AppLanguage.DARI_PERSIAN to "صحنه توسط میزبان قفل شده است.",
            AppLanguage.PASHTO to "سټیج د کوربه لخوا قلف دی."
        ),
        "vr_stage_capacity_reached" to mapOf(
            AppLanguage.ENGLISH to "Stage reached maximum 6 P2P audio slots.",
            AppLanguage.DARI_PERSIAN to "ظرفیت ۶ نفر همزمان صوتی تکمیل است.",
            AppLanguage.PASHTO to "د ۶ غږیزو ګډونوالو حد پوره شو."
        ),
        "vr_all_muted_notice" to mapOf(
            AppLanguage.ENGLISH to "All speakers have been muted.",
            AppLanguage.DARI_PERSIAN to "میکروفون همه سخنرانان قطع شد.",
            AppLanguage.PASHTO to "د ټولو ویناوالو مایکونه بند شول."
        ),
        "vr_insufficient_xp" to mapOf(
            AppLanguage.ENGLISH to "Not enough earned Vibe XP for that gift yet.",
            AppLanguage.DARI_PERSIAN to "امتیاز وایب کافی برای این هدیه ندارید؛ از هدایای رایگان استفاده کنید.",
            AppLanguage.PASHTO to "کافي Vibe XP نشته؛ وړیا ډالۍ وکاروئ."
        ),
        "vr_sound_cooldown" to mapOf(
            AppLanguage.ENGLISH to "Wait a second before triggering another sound.",
            AppLanguage.DARI_PERSIAN to "کمی صبر کنید و دوباره پخش کنید.",
            AppLanguage.PASHTO to "لږ صبر وکړئ بیا یې ولګوئ."
        ),
        "vr_retry_audio" to mapOf(
            AppLanguage.ENGLISH to "Retry Audio",
            AppLanguage.DARI_PERSIAN to "تلاش مجدد صوت",
            AppLanguage.PASHTO to "بیا نښلول"
        ),
        "vr_nat_warning" to mapOf(
            AppLanguage.ENGLISH to "Using free public STUN. Strict mobile/NAT firewalls may block P2P audio.",
            AppLanguage.DARI_PERSIAN to "اتصال P2P با STUN رایگان است؛ در برخی شبکه‌های محدود ممکن است نیاز به تغییر شبکه یا ریترای باشد.",
            AppLanguage.PASHTO to "وړیا STUN کارول کېږي؛ ځینې شبکې ممکن P2P غږ بند کړي."
        ),
        "vr_mic_permission_banner_title" to mapOf(
            AppLanguage.ENGLISH to "Microphone permission required to speak",
            AppLanguage.DARI_PERSIAN to "برای صحبت در اتاق دسترسی میکروفون لازم است",
            AppLanguage.PASHTO to "د خبرو کولو لپاره د مایکروفون اجازه اړینه ده"
        ),
        "vr_mic_permission_banner_desc" to mapOf(
            AppLanguage.ENGLISH to "Allow microphone access so other participants can hear your voice.",
            AppLanguage.DARI_PERSIAN to "دسترسی میکروفون را بدهید تا سایر اعضای اتاق صدای شما را بشنوند.",
            AppLanguage.PASHTO to "مایکروفون ته اجازه ورکړئ ترڅو نور ګډونوال ستاسو غږ واوري."
        ),
        "vr_grant_mic_btn" to mapOf(
            AppLanguage.ENGLISH to "Allow Mic",
            AppLanguage.DARI_PERSIAN to "اجازه میکروفون",
            AppLanguage.PASHTO to "مایک اجازه"
        ),
        "vr_open_settings_btn" to mapOf(
            AppLanguage.ENGLISH to "App Settings",
            AppLanguage.DARI_PERSIAN to "تنظیمات برنامه",
            AppLanguage.PASHTO to "تنظیمات"
        ),
        "vr_mic_live" to mapOf(
            AppLanguage.ENGLISH to "Mic Live",
            AppLanguage.DARI_PERSIAN to "میکروفون روشن",
            AppLanguage.PASHTO to "مایک فعال"
        ),
        "vr_mic_needed_badge" to mapOf(
            AppLanguage.ENGLISH to "Mic Denied",
            AppLanguage.DARI_PERSIAN to "نیاز به اجازه میکروفون",
            AppLanguage.PASHTO to "مایک بند دی"
        ),
        "vr_audio_failed" to mapOf(
            AppLanguage.ENGLISH to "Audio Failed",
            AppLanguage.DARI_PERSIAN to "خطای اتصال صوت",
            AppLanguage.PASHTO to "غږ ونښت"
        ),
        "vr_audio_ready" to mapOf(
            AppLanguage.ENGLISH to "Audio Ready",
            AppLanguage.DARI_PERSIAN to "آماده صوت",
            AppLanguage.PASHTO to "غږ چمتو دی"
        ),
        "vr_join_mic_seat" to mapOf(
            AppLanguage.ENGLISH to "Join Mic",
            AppLanguage.DARI_PERSIAN to "گرفتن مایک",
            AppLanguage.PASHTO to "مایک اخیستل"
        ),
        "vr_seat_label" to mapOf(
            AppLanguage.ENGLISH to "Mic Seat",
            AppLanguage.DARI_PERSIAN to "جایگاه مایک",
            AppLanguage.PASHTO to "د مایک چوکۍ"
        ),
        "vr_step_down" to mapOf(
            AppLanguage.ENGLISH to "Audience",
            AppLanguage.DARI_PERSIAN to "رفتن به شنوندگان",
            AppLanguage.PASHTO to "اورېدونکو ته"
        ),
        "vr_host_muted_badge" to mapOf(
            AppLanguage.ENGLISH to "Host Muted",
            AppLanguage.DARI_PERSIAN to "قطع توسط میزبان",
            AppLanguage.PASHTO to "کوربه بند کړی"
        ),
        "vr_host_muted_banner" to mapOf(
            AppLanguage.ENGLISH to "The host restricted your microphone. Only a moderator can re-enable mic permission.",
            AppLanguage.DARI_PERSIAN to "میزبان دسترسی میکروفون شما را محدود کرده است. تا زمان رفع محدودیت امکان باز کردن مایک ندارید.",
            AppLanguage.PASHTO to "کوربه ستاسو مایکروفون محدود کړی دی. تر هغې چې اجازه درکړل شي مایک نشئ خلاصولی."
        ),
        "vr_allow_mic" to mapOf(
            AppLanguage.ENGLISH to "Allow Mic Access",
            AppLanguage.DARI_PERSIAN to "اجازه دادن به مایک",
            AppLanguage.PASHTO to "مایک اجازه ورکول"
        ),
        "vr_mic_allowed_notice" to mapOf(
            AppLanguage.ENGLISH to "Host allowed your mic. Tap Unmute when you want to speak.",
            AppLanguage.DARI_PERSIAN to "میزبان اجازه مایک را داد؛ هر وقت آماده بودید مایک را روشن کنید.",
            AppLanguage.PASHTO to "کوربه د مایک اجازه درکړه؛ کله چې چمتو یاست مایک چالان کړئ."
        ),
        "vr_cannot_force_unmute" to mapOf(
            AppLanguage.ENGLISH to "Hosts cannot force-unmute another participant's mic; you can grant permission so they can unmute themselves.",
            AppLanguage.DARI_PERSIAN to "میزبان نمی‌تواند مایک شخص دیگری را به اجبار روشن کند؛ فقط می‌تواند محدودیت را بردارد.",
            AppLanguage.PASHTO to "کوربه نشي کولی د بل چا مایک په زوره چالان کړي؛ یوازې اجازه ورکولی شي."
        ),
        "vr_studio_tools" to mapOf(
            AppLanguage.ENGLISH to "Tools",
            AppLanguage.DARI_PERSIAN to "ابزارها",
            AppLanguage.PASHTO to "وسایل"
        ),
        "vr_audience_empty" to mapOf(
            AppLanguage.ENGLISH to "No listeners in audience yet",
            AppLanguage.DARI_PERSIAN to "هنوز شنونده‌ای در سالن نیست",
            AppLanguage.PASHTO to "لا تر اوسه اورېدونکی نشته"
        ),
        "vr_room_ended_title" to mapOf(
            AppLanguage.ENGLISH to "Room Ended",
            AppLanguage.DARI_PERSIAN to "پایان اتاق صوتی",
            AppLanguage.PASHTO to "غږیزه خونه پای ته ورسېده"
        ),
        "vr_room_ended_polite_body" to mapOf(
            AppLanguage.ENGLISH to "The host has ended this voice room. Thank you for joining! You will now return to Voice Rooms.",
            AppLanguage.DARI_PERSIAN to "میزبان این اتاق صوتی را به پایان رساند. سپاس از حضور شما! اکنون به فهرست اتاق‌ها بازمی‌گردید.",
            AppLanguage.PASHTO to "کوربه دا غږیزه خونه پای ته ورسوله. ستاسو د ګډون مننه! اوس بېرته د خونو لېست ته ستنېږئ."
        ),
        "vr_ban_15m" to mapOf(
            AppLanguage.ENGLISH to "Ban 15m",
            AppLanguage.DARI_PERSIAN to "مسدود ۱۵ د",
            AppLanguage.PASHTO to "بند ۱۵ د"
        ),
        "vr_ban_1h" to mapOf(
            AppLanguage.ENGLISH to "Ban 1h",
            AppLanguage.DARI_PERSIAN to "مسدود ۱ س",
            AppLanguage.PASHTO to "بند ۱ س"
        ),
        "vr_ban_perm" to mapOf(
            AppLanguage.ENGLISH to "Perm Ban",
            AppLanguage.DARI_PERSIAN to "مسدود دایم",
            AppLanguage.PASHTO to "دایمي بند"
        ),
        "vr_temp_banned_notice" to mapOf(
            AppLanguage.ENGLISH to "You are temporarily banned from this voice room.",
            AppLanguage.DARI_PERSIAN to "شما به صورت موقت از این اتاق صوتی مسدود شده‌اید.",
            AppLanguage.PASHTO to "تاسو د لنډ وخت لپاره له دې خونې بند شوي یاست."
        ),
        "vr_duplicate_room_blocked" to mapOf(
            AppLanguage.ENGLISH to "Creating room, please wait...",
            AppLanguage.DARI_PERSIAN to "در حال ساخت اتاق، لطفاً صبر کنید...",
            AppLanguage.PASHTO to "خونه جوړېږي، لږ صبر وکړئ..."
        ),
        "vr_join_stage_failed" to mapOf(
            AppLanguage.ENGLISH to "Could not join mic seat right now.",
            AppLanguage.DARI_PERSIAN to "امکان گرفتن جایگاه مایک وجود نداشت.",
            AppLanguage.PASHTO to "د مایک چوکۍ ونشو نیول."
        ),
        "vr_ok_return" to mapOf(
            AppLanguage.ENGLISH to "OK",
            AppLanguage.DARI_PERSIAN to "باشه",
            AppLanguage.PASHTO to "سمه ده"
        ),
        "voice_note_too_long" to mapOf(
            AppLanguage.ENGLISH to "Voice note exceeded the 30-second size limit. Please record a shorter clip.",
            AppLanguage.DARI_PERSIAN to "پیام صوتی بیش از حد مجاز (۳۰ ثانیه) بود. لطفاً کوتاه‌تر ضبط کنید.",
            AppLanguage.PASHTO to "غږیز پیغام له ۳۰ ثانیو څخه اوږد شو. مهرباني وکړئ لنډ یې ثبت کړئ."
        ),
        "voice_note_failed" to mapOf(
            AppLanguage.ENGLISH to "Could not save voice note. Hold or tap again to record.",
            AppLanguage.DARI_PERSIAN to "ذخیره پیام صوتی انجام نشد. لطفاً دوباره ضبط کنید.",
            AppLanguage.PASHTO to "غږیز پیغام ثبت نه شو. مهرباني وکړئ بیا هڅه وکړئ."
        ),
        "live_captions_mic_conflict" to mapOf(
            AppLanguage.ENGLISH to "Live captions paused while your WebRTC microphone is live to avoid audio conflict.",
            AppLanguage.DARI_PERSIAN to "زیرنویس زنده هنگام روشن بودن میکروفون اتاق متوقف شد تا تداخل صوتی رخ ندهد.",
            AppLanguage.PASHTO to "کله چې ستاسو مایکروفون په خونه کې چالان وي، ژوندۍ ژباړه د غږ د ټکر مخنیوي لپاره ودرېده."
        ),
        "live_captions_unavailable" to mapOf(
            AppLanguage.ENGLISH to "Live captions unavailable right now. Check offline speech recognition or network.",
            AppLanguage.DARI_PERSIAN to "زیرنویس زنده اکنون در دسترس نیست. اتصال یا تشخیص گفتار دستگاه را بررسی کنید.",
            AppLanguage.PASHTO to "ژوندۍ لیکنه اوس مهال شتون نلري. د وسیلې غږ پېژندنه یا انټرنېټ وګورئ."
        ),
        "select_chat_hint_title" to mapOf(
            AppLanguage.ENGLISH to "Select a Conversation",
            AppLanguage.DARI_PERSIAN to "یک گفتگو را انتخاب کنید",
            AppLanguage.PASHTO to "یو مجلس وټاکئ"
        ),
        "select_chat_hint_desc" to mapOf(
            AppLanguage.ENGLISH to "Choose any friend or language partner on the left to open your chat here.",
            AppLanguage.DARI_PERSIAN to "برای شروع گفتگو، یکی از مخاطبین سمت راست را انتخاب کنید.",
            AppLanguage.PASHTO to "د خبرو پیل کولو لپاره له لیست څخه یو ملګری غوره کړئ."
        ),
        "max_peers_badge" to mapOf(
            AppLanguage.ENGLISH to "Max 6 Speakers",
            AppLanguage.DARI_PERSIAN to "حداکثر ۶ سخنران",
            AppLanguage.PASHTO to "تر ۶ ویناوالو"
        ),
        // Yeetalk-style Profile & Centralized Settings
        "settings_title" to mapOf(
            AppLanguage.ENGLISH to "Settings",
            AppLanguage.DARI_PERSIAN to "تنظیمات",
            AppLanguage.PASHTO to "ترتیبات"
        ),
        "settings_section_account" to mapOf(
            AppLanguage.ENGLISH to "Account Settings",
            AppLanguage.DARI_PERSIAN to "تنظیمات حساب کاربری",
            AppLanguage.PASHTO to "د حساب ترتیبات"
        ),
        "settings_section_privacy" to mapOf(
            AppLanguage.ENGLISH to "Privacy & Security",
            AppLanguage.DARI_PERSIAN to "حریم خصوصی و امنیت",
            AppLanguage.PASHTO to "محرمیت او امنیت"
        ),
        "settings_section_notifications" to mapOf(
            AppLanguage.ENGLISH to "Notifications",
            AppLanguage.DARI_PERSIAN to "اعلان‌ها",
            AppLanguage.PASHTO to "خبرتیاوې"
        ),
        "settings_section_language" to mapOf(
            AppLanguage.ENGLISH to "Language & Translation",
            AppLanguage.DARI_PERSIAN to "زبان و ترجمه",
            AppLanguage.PASHTO to "ژبه او ژباړه"
        ),
        "settings_section_appearance" to mapOf(
            AppLanguage.ENGLISH to "Appearance & Theme",
            AppLanguage.DARI_PERSIAN to "ظاهر و تم برنامه",
            AppLanguage.PASHTO to "بڼه او تم"
        ),
        "settings_section_audio" to mapOf(
            AppLanguage.ENGLISH to "Audio & Microphone",
            AppLanguage.DARI_PERSIAN to "صدا و میکروفون",
            AppLanguage.PASHTO to "غږ او مایکروفون"
        ),
        "settings_section_permissions" to mapOf(
            AppLanguage.ENGLISH to "Permissions",
            AppLanguage.DARI_PERSIAN to "دسترسی‌ها و مجوزها",
            AppLanguage.PASHTO to "اجازې"
        ),
        "settings_section_about" to mapOf(
            AppLanguage.ENGLISH to "About VibeRoom",
            AppLanguage.DARI_PERSIAN to "درباره وایب‌روم",
            AppLanguage.PASHTO to "د وایبروم په اړه"
        ),
        "settings_user_id_label" to mapOf(
            AppLanguage.ENGLISH to "User ID",
            AppLanguage.DARI_PERSIAN to "شناسه کاربری",
            AppLanguage.PASHTO to "د کارن پېژند"
        ),
        "settings_copy_id" to mapOf(
            AppLanguage.ENGLISH to "Copy ID",
            AppLanguage.DARI_PERSIAN to "کپی شناسه",
            AppLanguage.PASHTO to "پېژند کاپي کول"
        ),
        "settings_open_notifications" to mapOf(
            AppLanguage.ENGLISH to "Notification Inbox",
            AppLanguage.DARI_PERSIAN to "صندوق اعلان‌ها",
            AppLanguage.PASHTO to "د خبرتیاوو صندوق"
        ),
        "settings_mic_permission" to mapOf(
            AppLanguage.ENGLISH to "Microphone Permission",
            AppLanguage.DARI_PERSIAN to "دسترسی میکروفون",
            AppLanguage.PASHTO to "د مایکروفون اجازه"
        ),
        "settings_permission_granted" to mapOf(
            AppLanguage.ENGLISH to "Granted",
            AppLanguage.DARI_PERSIAN to "مجاز",
            AppLanguage.PASHTO to "اجازه شته"
        ),
        "settings_permission_needed" to mapOf(
            AppLanguage.ENGLISH to "Tap to Allow",
            AppLanguage.DARI_PERSIAN to "برای اجازه ضربه بزنید",
            AppLanguage.PASHTO to "د اجازې لپاره کېکاږئ"
        ),
        "settings_system_settings" to mapOf(
            AppLanguage.ENGLISH to "Open Android App Settings",
            AppLanguage.DARI_PERSIAN to "باز کردن تنظیمات سیستم برنامه",
            AppLanguage.PASHTO to "د سیسټم ترتیبات پرانیستل"
        ),
        "settings_app_version" to mapOf(
            AppLanguage.ENGLISH to "Version",
            AppLanguage.DARI_PERSIAN to "نسخه برنامه",
            AppLanguage.PASHTO to "نسخه"
        ),
        "settings_visual_fx_title" to mapOf(
            AppLanguage.ENGLISH to "Visual & Stage Effects",
            AppLanguage.DARI_PERSIAN to "انیمیشن و جلوه‌های صحنه",
            AppLanguage.PASHTO to "بصري او سټېج اغېزې"
        ),
        "settings_low_power_desc" to mapOf(
            AppLanguage.ENGLISH to "Reduce animated stage graphics to save battery",
            AppLanguage.DARI_PERSIAN to "کاهش انیمیشن‌های پس‌زمینه برای مصرف کمتر باتری",
            AppLanguage.PASHTO to "د بیټرۍ د سپما لپاره د شالید انیمیشن کمول"
        ),
        "share_profile" to mapOf(
            AppLanguage.ENGLISH to "Share Profile",
            AppLanguage.DARI_PERSIAN to "اشتراک‌گذاری پروفایل",
            AppLanguage.PASHTO to "پېژندڅېره شریکول"
        ),
        "profile_posts_title" to mapOf(
            AppLanguage.ENGLISH to "Moments",
            AppLanguage.DARI_PERSIAN to "پست‌ها و لحظه‌ها",
            AppLanguage.PASHTO to "پوسټونه او شېبې"
        ),
        "profile_posts_empty" to mapOf(
            AppLanguage.ENGLISH to "No moments shared yet. Your published posts will appear here.",
            AppLanguage.DARI_PERSIAN to "هنوز پستی منتشر نکرده‌اید. پست‌های شما اینجا نمایش داده می‌شوند.",
            AppLanguage.PASHTO to "لا تر اوسه کوم پوسټ نشته. ستاسو خپاره شوي پوسټونه به دلته ښکاري."
        ),
        "profile_bio_empty" to mapOf(
            AppLanguage.ENGLISH to "No bio yet — tap Edit Profile to introduce yourself.",
            AppLanguage.DARI_PERSIAN to "هنوز معرفی کوتاهی ثبت نشده است — برای افزودن، ویرایش پروفایل را بزنید.",
            AppLanguage.PASHTO to "لا تر اوسه لنډه پېژندنه نشته — د لیکلو لپاره سمول کېکاږئ."
        ),
        "posts_count_label" to mapOf(
            AppLanguage.ENGLISH to "Moments",
            AppLanguage.DARI_PERSIAN to "پست‌ها",
            AppLanguage.PASHTO to "پوسټونه"
        ),
        "profile_info_section" to mapOf(
            AppLanguage.ENGLISH to "About Me",
            AppLanguage.DARI_PERSIAN to "اطلاعات فردی",
            AppLanguage.PASHTO to "زما په اړه"
        ),
        "coach_thinking" to mapOf(
            AppLanguage.ENGLISH to "Thinking...",
            AppLanguage.DARI_PERSIAN to "در حال پاسخ‌گویی...",
            AppLanguage.PASHTO to "ځواب چمتو کوي..."
        ),
        "coach_stop" to mapOf(
            AppLanguage.ENGLISH to "Stop",
            AppLanguage.DARI_PERSIAN to "توقف",
            AppLanguage.PASHTO to "ودرول"
        ),
        "coach_clear" to mapOf(
            AppLanguage.ENGLISH to "New Chat",
            AppLanguage.DARI_PERSIAN to "گفتگوی جدید",
            AppLanguage.PASHTO to "نوی بحث"
        ),
        "coach_mode_conversation" to mapOf(
            AppLanguage.ENGLISH to "Chat",
            AppLanguage.DARI_PERSIAN to "گفتگو",
            AppLanguage.PASHTO to "خبرې"
        ),
        "coach_mode_grammar" to mapOf(
            AppLanguage.ENGLISH to "Grammar",
            AppLanguage.DARI_PERSIAN to "گرامر",
            AppLanguage.PASHTO to "ګرامر"
        ),
        "coach_mode_pronunciation" to mapOf(
            AppLanguage.ENGLISH to "Speaking",
            AppLanguage.DARI_PERSIAN to "تلفظ",
            AppLanguage.PASHTO to "تلفظ"
        ),
        "coach_mode_culture" to mapOf(
            AppLanguage.ENGLISH to "Culture",
            AppLanguage.DARI_PERSIAN to "فرهنگ",
            AppLanguage.PASHTO to "کلتور"
        ),
        "create_room_subtitle" to mapOf(
            AppLanguage.ENGLISH to "Up to 6 live speakers on stage",
            AppLanguage.DARI_PERSIAN to "تا ۶ سخنران هم‌زمان روی صحنه",
            AppLanguage.PASHTO to "په سټېج تر ۶ ویناوالو پورې"
        ),
        "create_room_settings_section" to mapOf(
            AppLanguage.ENGLISH to "Room Settings",
            AppLanguage.DARI_PERSIAN to "تنظیمات اتاق",
            AppLanguage.PASHTO to "د خونې ترتیبات"
        ),
        "vr_listeners_label" to mapOf(
            AppLanguage.ENGLISH to "Listeners",
            AppLanguage.DARI_PERSIAN to "شنوندگان",
            AppLanguage.PASHTO to "اورېدونکي"
        )
    )
}
