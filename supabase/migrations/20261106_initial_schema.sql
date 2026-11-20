-- ==========================================================
-- VIBE ROOM 1.0 - Production Database Schema & Security
-- ==========================================================

-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- 1. PROFILES TABLE
CREATE TABLE IF NOT EXISTS public.profiles (
    id UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    username TEXT UNIQUE NOT NULL,
    display_name TEXT NOT NULL,
    avatar_url TEXT,
    bio TEXT DEFAULT '',
    country_name TEXT NOT NULL DEFAULT 'United States',
    country_code TEXT NOT NULL DEFAULT 'US',
    flag_emoji TEXT NOT NULL DEFAULT '🇺🇸',
    vibes_score INT NOT NULL DEFAULT 100,
    connections_count INT NOT NULL DEFAULT 0,
    rooms_joined_count INT NOT NULL DEFAULT 0,
    is_online BOOLEAN NOT NULL DEFAULT FALSE,
    last_seen TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    languages_spoken TEXT[] DEFAULT ARRAY['English'],
    interests TEXT[] DEFAULT ARRAY['Music', 'Technology'],
    is_verified BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Index profiles for search
CREATE INDEX IF NOT EXISTS idx_profiles_username ON public.profiles(username);
CREATE INDEX IF NOT EXISTS idx_profiles_display_name ON public.profiles(display_name);
CREATE INDEX IF NOT EXISTS idx_profiles_country ON public.profiles(country_name);

-- 2. USER LOCATIONS (Nearby Discovery)
CREATE TABLE IF NOT EXISTS public.user_locations (
    user_id UUID PRIMARY KEY REFERENCES public.profiles(id) ON DELETE CASCADE,
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,
    is_sharing_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    discovery_radius_km INT NOT NULL DEFAULT 25,
    city_name TEXT,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 3. FOLLOWS TABLE
CREATE TABLE IF NOT EXISTS public.follows (
    follower_id UUID REFERENCES public.profiles(id) ON DELETE CASCADE,
    following_id UUID REFERENCES public.profiles(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    PRIMARY KEY (follower_id, following_id)
);

-- 4. BLOCKS TABLE
CREATE TABLE IF NOT EXISTS public.blocks (
    blocker_id UUID REFERENCES public.profiles(id) ON DELETE CASCADE,
    blocked_id UUID REFERENCES public.profiles(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    PRIMARY KEY (blocker_id, blocked_id)
);

-- 5. CONVERSATIONS TABLE
CREATE TABLE IF NOT EXISTS public.conversations (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    is_group BOOLEAN NOT NULL DEFAULT FALSE,
    last_message TEXT DEFAULT '',
    last_message_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 6. CONVERSATION MEMBERS
CREATE TABLE IF NOT EXISTS public.conversation_members (
    conversation_id UUID REFERENCES public.conversations(id) ON DELETE CASCADE,
    user_id UUID REFERENCES public.profiles(id) ON DELETE CASCADE,
    joined_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    PRIMARY KEY (conversation_id, user_id)
);

-- 7. MESSAGES TABLE
CREATE TABLE IF NOT EXISTS public.messages (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    conversation_id UUID REFERENCES public.conversations(id) ON DELETE CASCADE,
    sender_id UUID REFERENCES public.profiles(id) ON DELETE CASCADE,
    text TEXT,
    media_url TEXT,
    media_type TEXT DEFAULT 'text', -- 'text', 'image', 'voice'
    is_edited BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_messages_conversation ON public.messages(conversation_id, created_at DESC);

-- 8. MESSAGE REACTIONS
CREATE TABLE IF NOT EXISTS public.message_reactions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    message_id UUID REFERENCES public.messages(id) ON DELETE CASCADE,
    user_id UUID REFERENCES public.profiles(id) ON DELETE CASCADE,
    emoji TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE(message_id, user_id, emoji)
);

-- 9. VOICE ROOMS TABLE
CREATE TABLE IF NOT EXISTS public.voice_rooms (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    title TEXT NOT NULL,
    description TEXT DEFAULT '',
    category TEXT NOT NULL DEFAULT 'CHILL_VIBES',
    host_id UUID REFERENCES public.profiles(id) ON DELETE CASCADE,
    is_live BOOLEAN NOT NULL DEFAULT TRUE,
    listeners_count INT NOT NULL DEFAULT 1,
    tags TEXT[] DEFAULT ARRAY[]::TEXT[],
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 10. VOICE ROOM PARTICIPANTS
CREATE TABLE IF NOT EXISTS public.voice_room_participants (
    room_id UUID REFERENCES public.voice_rooms(id) ON DELETE CASCADE,
    user_id UUID REFERENCES public.profiles(id) ON DELETE CASCADE,
    role TEXT NOT NULL DEFAULT 'listener', -- 'host', 'speaker', 'listener', 'moderator'
    is_muted BOOLEAN NOT NULL DEFAULT TRUE,
    hand_raised BOOLEAN NOT NULL DEFAULT FALSE,
    joined_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    PRIMARY KEY (room_id, user_id)
);

-- 11. REPORTS & MODERATION
CREATE TABLE IF NOT EXISTS public.reports (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    reporter_id UUID REFERENCES public.profiles(id) ON DELETE SET NULL,
    reported_user_id UUID REFERENCES public.profiles(id) ON DELETE CASCADE,
    room_id UUID REFERENCES public.voice_rooms(id) ON DELETE SET NULL,
    reason TEXT NOT NULL,
    details TEXT,
    status TEXT NOT NULL DEFAULT 'PENDING', -- 'PENDING', 'RESOLVED', 'DISMISSED'
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 12. DEVICE TOKENS (Push Notifications)
CREATE TABLE IF NOT EXISTS public.device_tokens (
    user_id UUID REFERENCES public.profiles(id) ON DELETE CASCADE,
    token TEXT NOT NULL,
    platform TEXT NOT NULL DEFAULT 'android',
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    PRIMARY KEY (user_id, token)
);

-- ==========================================================
-- ROW LEVEL SECURITY (RLS) POLICIES
-- ==========================================================

ALTER TABLE public.profiles ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.user_locations ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.follows ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.blocks ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.conversations ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.conversation_members ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.messages ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.message_reactions ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.voice_rooms ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.voice_room_participants ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.reports ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.device_tokens ENABLE ROW LEVEL SECURITY;

-- Profiles: Anyone authenticated can read profiles (excluding blocked users)
CREATE POLICY "Profiles readable by authenticated users"
    ON public.profiles FOR SELECT
    TO authenticated
    USING (
        id NOT IN (SELECT blocker_id FROM public.blocks WHERE blocked_id = auth.uid())
        AND id NOT IN (SELECT blocked_id FROM public.blocks WHERE blocker_id = auth.uid())
    );

CREATE POLICY "Users can update own profile"
    ON public.profiles FOR UPDATE
    TO authenticated
    USING (id = auth.uid());

-- Locations: Only readable if user opted into sharing
CREATE POLICY "Shared locations viewable by radius"
    ON public.user_locations FOR SELECT
    TO authenticated
    USING (is_sharing_enabled = TRUE);

CREATE POLICY "Users can manage own location"
    ON public.user_locations FOR ALL
    TO authenticated
    USING (user_id = auth.uid());

-- Follows: Users manage their own follows
CREATE POLICY "Users can follow others"
    ON public.follows FOR ALL
    TO authenticated
    USING (follower_id = auth.uid());

CREATE POLICY "Follows visible to authenticated"
    ON public.follows FOR SELECT
    TO authenticated
    USING (TRUE);

-- Blocks: Users manage their own block list
CREATE POLICY "Users manage own blocks"
    ON public.blocks FOR ALL
    TO authenticated
    USING (blocker_id = auth.uid());

-- Conversations: Members can read conversation details
CREATE POLICY "Members can view conversations"
    ON public.conversations FOR SELECT
    TO authenticated
    USING (
        id IN (SELECT conversation_id FROM public.conversation_members WHERE user_id = auth.uid())
    );

CREATE POLICY "Members can view conversation membership"
    ON public.conversation_members FOR SELECT
    TO authenticated
    USING (
        conversation_id IN (SELECT conversation_id FROM public.conversation_members WHERE user_id = auth.uid())
    );

-- Messages: Only conversation participants can view & send messages
CREATE POLICY "Participants can view messages"
    ON public.messages FOR SELECT
    TO authenticated
    USING (
        conversation_id IN (SELECT conversation_id FROM public.conversation_members WHERE user_id = auth.uid())
    );

CREATE POLICY "Participants can insert messages"
    ON public.messages FOR INSERT
    TO authenticated
    WITH CHECK (
        sender_id = auth.uid() AND
        conversation_id IN (SELECT conversation_id FROM public.conversation_members WHERE user_id = auth.uid())
    );

-- Voice Rooms: Public rooms readable by all authenticated users
CREATE POLICY "Voice rooms viewable by all"
    ON public.voice_rooms FOR SELECT
    TO authenticated
    USING (TRUE);

CREATE POLICY "Host can manage room"
    ON public.voice_rooms FOR ALL
    TO authenticated
    USING (host_id = auth.uid());

CREATE POLICY "Participants manage own voice presence"
    ON public.voice_room_participants FOR ALL
    TO authenticated
    USING (user_id = auth.uid() OR room_id IN (SELECT id FROM public.voice_rooms WHERE host_id = auth.uid()));

-- Reports: Authenticated users can insert reports
CREATE POLICY "Users can submit reports"
    ON public.reports FOR INSERT
    TO authenticated
    WITH CHECK (reporter_id = auth.uid());

-- Device Tokens: Users manage their own tokens
CREATE POLICY "Users manage own device tokens"
    ON public.device_tokens FOR ALL
    TO authenticated
    USING (user_id = auth.uid());
