const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated user: cannot read user profiles list", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").get());
});

test("Unauthenticated user: cannot create user profile", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      username: "alice",
      createdAt: new Date(),
      updatedAt: new Date(),
    })
  );
});

test("Authenticated user: can create own user profile", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      username: "alice",
      displayName: "Alice Wonderland",
      email: "alice@example.com",
      createdAt: new Date(),
      updatedAt: new Date(),
    })
  );
});

test("Authenticated user: cannot create another user's profile", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(
    aliceDb.collection("users").doc(BOB_UID).set({
      userId: BOB_UID,
      username: "bob",
      createdAt: new Date(),
      updatedAt: new Date(),
    })
  );
});

test("Authenticated user: can read own profile via get", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      username: "alice",
      createdAt: new Date(),
      updatedAt: new Date(),
    });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).get());
});

test("Authenticated user: can query public user profiles via list", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("users").doc(BOB_UID).set({
      userId: BOB_UID,
      username: "bob",
      displayName: "Bob Smith",
      isOnline: true,
      createdAt: new Date(),
      updatedAt: new Date(),
    });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(aliceDb.collection("users").get());
});

test("Authenticated user: cannot change country once set", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      username: "alice",
      country: "Afghanistan",
      countryCode: "AF",
      createdAt: new Date(),
      updatedAt: new Date(),
    });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      country: "Germany",
      countryCode: "DE",
      updatedAt: new Date(),
    })
  );
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).update({
      bio: "Updated bio",
      country: "Afghanistan",
      countryCode: "AF",
      updatedAt: new Date(),
    })
  );
});

test("Chat: Authenticated user can create and join conversation", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const convId = "conv_alice_bob";
  await assertSucceeds(
    aliceDb.collection("conversations").doc(convId).set({
      participantIds: [ALICE_UID, BOB_UID],
      lastMessage: "Hey Bob!",
      lastSenderId: ALICE_UID,
      updatedAt: new Date(),
    })
  );
  await assertSucceeds(
    aliceDb.collection("conversations").where("participantIds", "array-contains", ALICE_UID).get()
  );
});

test("Chat: Non-participant cannot read conversation or its messages", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    const convRef = context.firestore().collection("conversations").doc("private_chat");
    await convRef.set({
      participantIds: [ALICE_UID, "charlie_789"],
      lastMessage: "Secret",
      updatedAt: new Date(),
    });
    await convRef.collection("messages").doc("msg_secret").set({
      conversationId: "private_chat",
      senderId: ALICE_UID,
      receiverId: "charlie_789",
      text: "Top secret",
      isRead: false,
      mediaType: "text",
      createdAt: new Date(),
    });
  });

  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertFails(bobDb.collection("conversations").doc("private_chat").get());
  await assertFails(bobDb.collection("conversations").doc("private_chat").collection("messages").get());
});

test("Chat: Sender can post text & voice messages and receiver can mark read", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const convId = "conv_alice_bob";
  await assertSucceeds(
    aliceDb.collection("conversations").doc(convId).set({
      participantIds: [ALICE_UID, BOB_UID],
      lastMessage: "Hello",
      lastSenderId: ALICE_UID,
      updatedAt: new Date(),
    })
  );
  await assertSucceeds(
    aliceDb.collection("conversations").doc(convId).collection("messages").doc("msg_1").set({
      conversationId: convId,
      senderId: ALICE_UID,
      receiverId: BOB_UID,
      text: "Hello from Alice!",
      isRead: false,
      mediaType: "text",
      createdAt: new Date(),
    })
  );
  await assertSucceeds(
    aliceDb.collection("conversations").doc(convId).collection("messages").doc("msg_voice").set({
      conversationId: convId,
      senderId: ALICE_UID,
      receiverId: BOB_UID,
      text: "Voice message (12s)",
      isRead: false,
      mediaType: "voice",
      audioDurationSec: 12,
      createdAt: new Date(),
    })
  );
  await assertSucceeds(
    bobDb.collection("conversations").doc(convId).collection("messages").get()
  );
  await assertSucceeds(
    bobDb.collection("conversations").doc(convId).collection("messages").doc("msg_1").update({
      isRead: true,
    })
  );
});

test("Chat: Cross-user spoofing message sender fails", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const convId = "conv_alice_bob";
  await assertFails(
    aliceDb.collection("conversations").doc(convId).collection("messages").doc("msg_spoof").set({
      conversationId: convId,
      senderId: BOB_UID, // Spoofing Bob while authenticated as Alice
      receiverId: ALICE_UID,
      text: "I am Bob (fake)",
      createdAt: new Date(),
    })
  );
});

test("Voice Rooms: Unauthenticated user cannot read voice rooms", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("voice_rooms").get());
});

test("Voice Rooms: Authenticated user can create active voice room", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const roomId = "room_global_tech";
  await assertSucceeds(
    aliceDb.collection("voice_rooms").doc(roomId).set({
      title: "Global Tech Jam",
      description: "Discussing latest AI & Android tech",
      category: "TECH_TALK",
      hostId: ALICE_UID,
      hostName: "Alice Wonderland",
      hostCountry: "United States",
      hostFlagEmoji: "🇺🇸",
      isLive: true,
      listenersCount: 5,
      speakersCount: 1,
      tags: ["Tech", "Android"],
      createdAt: new Date(),
      updatedAt: new Date(),
    })
  );
});

test("Voice Rooms: Authenticated user cannot spoof hostId when creating room", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const roomId = "room_fake_bob";
  await assertFails(
    aliceDb.collection("voice_rooms").doc(roomId).set({
      title: "Spoofed Room",
      category: "TECH_TALK",
      hostId: BOB_UID, // Alice trying to create room with Bob as host
      hostName: "Bob",
      isLive: true,
      listenersCount: 1,
      createdAt: new Date(),
      updatedAt: new Date(),
    })
  );
});

test("Voice Rooms: Authenticated user can list active rooms", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("voice_rooms").doc("room_active_1").set({
      title: "Language Cafe",
      category: "LANGUAGE_EXCHANGE",
      hostId: ALICE_UID,
      hostName: "Alice",
      isLive: true,
      listenersCount: 10,
      createdAt: new Date(),
      updatedAt: new Date(),
    });
  });

  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertSucceeds(bobDb.collection("voice_rooms").get());
});

test("Voice Rooms: Listener can join room by updating listenersCount", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("voice_rooms").doc("room_to_join").set({
      title: "Music Beats Lounge",
      category: "MUSIC_JAM",
      hostId: ALICE_UID,
      hostName: "Alice",
      isLive: true,
      listenersCount: 10,
      createdAt: new Date(),
      updatedAt: new Date(),
    });
  });

  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertSucceeds(
    bobDb.collection("voice_rooms").doc("room_to_join").update({
      listenersCount: 11,
      updatedAt: new Date(),
    })
  );
});

test("Voice Rooms: Non-host listener cannot change room title", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("voice_rooms").doc("room_locked").set({
      title: "Original Title",
      category: "MUSIC_JAM",
      hostId: ALICE_UID,
      hostName: "Alice",
      isLive: true,
      listenersCount: 10,
      createdAt: new Date(),
      updatedAt: new Date(),
    });
  });

  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertFails(
    bobDb.collection("voice_rooms").doc("room_locked").update({
      title: "Hacked by Bob",
      updatedAt: new Date(),
    })
  );
});

test("Voice Rooms: Host can delete own room", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("voice_rooms").doc("room_delete_me").set({
      title: "Temporary Room",
      category: "CHILL_VIBES",
      hostId: ALICE_UID,
      hostName: "Alice",
      isLive: true,
      listenersCount: 0,
      createdAt: new Date(),
      updatedAt: new Date(),
    });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(aliceDb.collection("voice_rooms").doc("room_delete_me").delete());
});

test("Posts: Authenticated non-author can like and increment comments on another user's post", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("posts").doc("post_alice_1").set({
      authorId: ALICE_UID,
      authorName: "Alice",
      authorFlag: "🇺🇸",
      authorCountry: "United States",
      content: "Looking for language exchange partners!",
      category: "Languages",
      likesCount: 0,
      commentsCount: 0,
      likedUserIds: [],
      createdAt: new Date(),
      updatedAt: new Date(),
    });
  });

  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertSucceeds(
    bobDb.collection("posts").doc("post_alice_1").update({
      likesCount: 1,
      likedUserIds: [BOB_UID],
      updatedAt: new Date(),
    })
  );
  await assertSucceeds(
    bobDb.collection("posts").doc("post_alice_1").collection("comments").doc("comment_1").set({
      postId: "post_alice_1",
      authorId: BOB_UID,
      authorName: "Bob",
      authorFlag: "🇩🇪",
      content: "Count me in!",
      createdAt: new Date(),
    })
  );
  await assertSucceeds(
    bobDb.collection("posts").doc("post_alice_1").update({
      commentsCount: 1,
      updatedAt: new Date(),
    })
  );
});

test("Posts: Non-author cannot edit content, spoof authorId, or delete another user's post", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("posts").doc("post_alice_2").set({
      authorId: ALICE_UID,
      authorName: "Alice",
      content: "Original post content",
      category: "General",
      likesCount: 0,
      commentsCount: 0,
      likedUserIds: [],
      createdAt: new Date(),
      updatedAt: new Date(),
    });
  });

  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertFails(
    bobDb.collection("posts").doc("post_alice_2").update({
      content: "Tampered by Bob",
      updatedAt: new Date(),
    })
  );
  await assertFails(
    bobDb.collection("posts").doc("post_alice_2").update({
      authorId: BOB_UID,
      likesCount: 1,
      updatedAt: new Date(),
    })
  );
  await assertFails(
    bobDb.collection("posts").doc("post_alice_2").delete()
  );
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(aliceDb.collection("posts").doc("post_alice_2").delete());
});

test("Users: Owner can update language exchange and privacy toggles cleanly", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      username: "alice",
      nativeLanguages: ["English"],
      learningLanguages: ["Persian", "German"],
      proficiencyLevel: "Intermediate",
      communicationGoal: "Daily conversation",
      isDiscoveryVisible: true,
      allowDirectMessages: true,
      createdAt: new Date(),
      updatedAt: new Date(),
    })
  );
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).update({
      isDiscoveryVisible: false,
      allowDirectMessages: false,
      updatedAt: new Date(),
    })
  );
});

test("Follows: Authenticated user can follow and unfollow another user, but cannot spoof follow", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const followId = `${ALICE_UID}_${BOB_UID}`;

  await assertSucceeds(
    aliceDb.collection("follows").doc(followId).set({
      followerId: ALICE_UID,
      followingId: BOB_UID,
      createdAt: new Date(),
    })
  );
  await assertSucceeds(
    aliceDb.collection("follows").where("followerId", "==", ALICE_UID).get()
  );
  await assertSucceeds(
    bobDb.collection("follows").where("followingId", "==", BOB_UID).get()
  );
  await assertFails(
    bobDb.collection("follows").doc(followId).delete()
  );
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(
    unauthDb.collection("follows").get()
  );
  await assertSucceeds(
    bobDb.collection("follows").get()
  );
  await assertSucceeds(
    aliceDb.collection("follows").doc(followId).delete()
  );
});

test("Blocks & Reports: User-scoped blocked list and pending moderation reports enforce zero-trust isolation", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("blocked").doc(BOB_UID).set({
      blockedUserId: BOB_UID,
      reason: "Spam",
      createdAt: new Date(),
    })
  );
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("blocked").get()
  );
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("blocked").get()
  );
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("blocked").doc(BOB_UID).delete()
  );

  await assertSucceeds(
    aliceDb.collection("reports").doc("rep_1").set({
      reporterId: ALICE_UID,
      targetType: "user",
      targetId: BOB_UID,
      reason: "Harassment",
      details: "Sent unsolicited spam links",
      status: "pending",
      createdAt: new Date(),
    })
  );
  await assertFails(
    bobDb.collection("reports").doc("rep_1").get()
  );
  await assertFails(
    aliceDb.collection("reports").doc("rep_1").update({ status: "resolved" })
  );
});

test("Friend requests: Sender creates, receiver accepts, terminal state locks further updates", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const reqId = `${ALICE_UID}_${BOB_UID}`;

  await assertSucceeds(
    aliceDb.collection("friend_requests").doc(reqId).set({
      senderId: ALICE_UID,
      receiverId: BOB_UID,
      senderName: "Alice",
      receiverName: "Bob",
      status: "pending",
      createdAt: new Date(),
      updatedAt: new Date(),
    })
  );
  // Sender cannot accept their own request
  await assertFails(
    aliceDb.collection("friend_requests").doc(reqId).update({
      status: "accepted",
      updatedAt: new Date(),
    })
  );
  // Receiver can accept
  await assertSucceeds(
    bobDb.collection("friend_requests").doc(reqId).update({
      status: "accepted",
      updatedAt: new Date(),
    })
  );
  // Terminal state lock: cannot flip back to pending or rejected
  await assertFails(
    bobDb.collection("friend_requests").doc(reqId).update({
      status: "rejected",
      updatedAt: new Date(),
    })
  );
});

test("Stories: Authenticated create, view mark, reaction, and owner-only delete", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const storyId = "story_alice_1";
  const expires = Date.now() + 24 * 60 * 60 * 1000;

  await assertSucceeds(
    aliceDb.collection("stories").doc(storyId).set({
      authorId: ALICE_UID,
      authorName: "Alice",
      authorFlag: "🇺🇸",
      authorCountry: "United States",
      text: "Practicing Persian phrases today!",
      category: "Languages",
      backgroundHex: 0xff6a1b9a,
      viewedByIds: [ALICE_UID],
      reactions: {},
      expiresAtMs: expires,
      createdAt: new Date(),
      updatedAt: new Date(),
    })
  );
  await assertSucceeds(bobDb.collection("stories").get());
  await assertSucceeds(
    bobDb.collection("stories").doc(storyId).update({
      viewedByIds: [ALICE_UID, BOB_UID],
      updatedAt: new Date(),
    })
  );
  await assertSucceeds(
    bobDb.collection("stories").doc(storyId).update({
      reactions: { [BOB_UID]: "🔥" },
      updatedAt: new Date(),
    })
  );
  await assertFails(bobDb.collection("stories").doc(storyId).delete());
  await assertSucceeds(aliceDb.collection("stories").doc(storyId).delete());
});

test("Vocabulary: Owner-only CRUD and strict cross-user isolation", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const vocabId = "vocab_1";

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("vocabulary").doc(vocabId).set({
      userId: ALICE_UID,
      wordOrPhrase: "سلام",
      translation: "Hello",
      language: "Persian (Dari)",
      exampleSentence: "سلام، حال شما چطور است؟",
      masteryLevel: 1,
      createdAt: new Date(),
      updatedAt: new Date(),
    })
  );
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("vocabulary").doc(vocabId).update({
      masteryLevel: 2,
      updatedAt: new Date(),
    })
  );
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("vocabulary").get());
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("vocabulary").doc(vocabId).delete());
  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).collection("vocabulary").doc(vocabId).delete());
});

test("Notifications: Cross-user delivery requires senderId == auth.uid; only recipient can read or mark read", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const notifId = "notif_1";

  await assertSucceeds(
    aliceDb.collection("users").doc(BOB_UID).collection("notifications").doc(notifId).set({
      recipientId: BOB_UID,
      senderId: ALICE_UID,
      senderName: "Alice",
      type: "post_like",
      title: "Alice liked your moment",
      body: "Great post!",
      isRead: false,
      createdAt: new Date(),
    })
  );
  // Spoofing senderId fails
  await assertFails(
    aliceDb.collection("users").doc(BOB_UID).collection("notifications").doc("notif_spoof").set({
      recipientId: BOB_UID,
      senderId: BOB_UID,
      senderName: "Bob",
      type: "system",
      title: "Spoof",
      body: "Fake",
      isRead: false,
      createdAt: new Date(),
    })
  );
  // Sender cannot read recipient's inbox
  await assertFails(aliceDb.collection("users").doc(BOB_UID).collection("notifications").get());
  // Recipient can read & mark read
  await assertSucceeds(bobDb.collection("users").doc(BOB_UID).collection("notifications").get());
  await assertSucceeds(bobDb.collection("users").doc(BOB_UID).collection("notifications").doc(notifId).update({ isRead: true }));
});

test("Saved Posts: Owner-only bookmark and unbookmark", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("saved_posts").doc("post_1").set({
      postId: "post_1",
      authorName: "Bob",
      contentPreview: "Hello world",
      savedAt: new Date(),
    })
  );
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("saved_posts").get());
  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).collection("saved_posts").doc("post_1").delete());
});

test("Chat upgrades: Reply fields, sender edit & soft-delete, participant reactions, and pinned messages", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const convId = "conv_alice_bob_v2";

  await assertSucceeds(
    aliceDb.collection("conversations").doc(convId).set({
      participantIds: [ALICE_UID, BOB_UID],
      lastMessage: "Hey!",
      lastSenderId: ALICE_UID,
      updatedAt: new Date(),
    })
  );
  await assertSucceeds(
    aliceDb.collection("conversations").doc(convId).collection("messages").doc("m1").set({
      conversationId: convId,
      senderId: ALICE_UID,
      senderName: "Alice",
      receiverId: BOB_UID,
      text: "Hello with reply quote",
      replyToMessageId: "m0",
      replyToText: "Previous text",
      replyToSenderName: "Bob",
      reactions: {},
      isEdited: false,
      isDeleted: false,
      isRead: false,
      mediaType: "text",
      createdAt: new Date(),
    })
  );
  // Sender can edit own message; non-sender cannot edit
  await assertFails(
    bobDb.collection("conversations").doc(convId).collection("messages").doc("m1").update({
      text: "Hacked edit",
      isEdited: true,
    })
  );
  await assertSucceeds(
    aliceDb.collection("conversations").doc(convId).collection("messages").doc("m1").update({
      text: "Hello edited!",
      isEdited: true,
    })
  );
  // Participant can react
  await assertSucceeds(
    bobDb.collection("conversations").doc(convId).collection("messages").doc("m1").update({
      reactions: { [BOB_UID]: "🔥" },
    })
  );
  // Participant can pin message on conversation
  await assertSucceeds(
    bobDb.collection("conversations").doc(convId).update({
      pinnedMessageId: "m1",
      pinnedMessageText: "Hello edited!",
      updatedAt: new Date(),
    })
  );
  // Sender can soft-delete
  await assertSucceeds(
    aliceDb.collection("conversations").doc(convId).collection("messages").doc("m1").update({
      text: "Message removed",
      isDeleted: true,
    })
  );
});

test("Media: Chat image & voice messages and feed image & video posts enforce schema and ownership", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const convId = "conv_media_1";

  await assertSucceeds(
    aliceDb.collection("conversations").doc(convId).set({
      participantIds: [ALICE_UID, BOB_UID],
      lastMessage: "Photo",
      lastSenderId: ALICE_UID,
      updatedAt: new Date(),
    })
  );

  await assertSucceeds(
    aliceDb.collection("conversations").doc(convId).collection("messages").doc("img_msg").set({
      conversationId: convId,
      senderId: ALICE_UID,
      senderName: "Alice",
      receiverId: BOB_UID,
      text: "📷 Photo",
      isRead: false,
      mediaType: "image",
      mediaData: "data:image/jpeg;base64,/9j/4AAQSkZJRg==",
      createdAt: new Date(),
    })
  );

  await assertSucceeds(
    bobDb.collection("conversations").doc(convId).collection("messages").doc("voice_msg").set({
      conversationId: convId,
      senderId: BOB_UID,
      senderName: "Bob",
      receiverId: ALICE_UID,
      text: "🎙️ Voice message (4s)",
      isRead: false,
      mediaType: "voice",
      audioDurationSec: 4,
      mediaData: "data:audio/mp4;base64,AAAAFGZ0eXA=",
      createdAt: new Date(),
    })
  );

  // Invalid mediaType in chat rejected
  await assertFails(
    aliceDb.collection("conversations").doc(convId).collection("messages").doc("bad_msg").set({
      conversationId: convId,
      senderId: ALICE_UID,
      receiverId: BOB_UID,
      text: "Invalid type",
      mediaType: "executable",
      createdAt: new Date(),
    })
  );

  await assertSucceeds(
    aliceDb.collection("posts").doc("post_img_1").set({
      authorId: ALICE_UID,
      authorName: "Alice",
      content: "Sunset view!",
      category: "Culture",
      mediaType: "image",
      mediaData: "data:image/jpeg;base64,/9j/4AAQSkZJRg==",
      likesCount: 0,
      commentsCount: 0,
      likedUserIds: [],
      createdAt: new Date(),
      updatedAt: new Date(),
    })
  );

  await assertSucceeds(
    bobDb.collection("posts").doc("post_vid_1").set({
      authorId: BOB_UID,
      authorName: "Bob",
      content: "Short video clip!",
      category: "Music",
      mediaType: "video",
      mediaUrl: "https://example.com/sample.mp4",
      likesCount: 0,
      commentsCount: 0,
      likedUserIds: [],
      createdAt: new Date(),
      updatedAt: new Date(),
    })
  );
});

test("WebRTC Calls: Caller/callee isolation, SDP exchange, ICE candidates, and third-party block", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const charlieDb = testEnv.authenticatedContext("charlie_789").firestore();
  const callId = "call_alice_bob_1";

  await assertSucceeds(
    aliceDb.collection("calls").doc(callId).set({
      callerId: ALICE_UID,
      callerName: "Alice",
      callerFlag: "🇺🇸",
      calleeId: BOB_UID,
      calleeName: "Bob",
      conversationId: "conv_alice_bob",
      status: "ringing",
      offerSdp: "v=0\r\no=- 123 2 IN IP4 127.0.0.1\r\n",
      createdAt: new Date(),
      updatedAt: new Date(),
    })
  );

  // Third party cannot read call or insert ICE candidate
  await assertFails(charlieDb.collection("calls").doc(callId).get());
  await assertFails(
    charlieDb.collection("calls").doc(callId).collection("candidates").doc("c_spoof").set({
      senderId: "charlie_789",
      sdpMid: "0",
      sdpMLineIndex: 0,
      sdp: "candidate:1 1 UDP 2122252543 192.0.2.1 54321 typ host",
      createdAt: new Date(),
    })
  );

  // Callee answers call
  await assertSucceeds(
    bobDb.collection("calls").doc(callId).update({
      status: "connected",
      answerSdp: "v=0\r\no=- 456 2 IN IP4 127.0.0.1\r\n",
      updatedAt: new Date(),
    })
  );

  // Both participants exchange ICE candidates
  await assertSucceeds(
    aliceDb.collection("calls").doc(callId).collection("candidates").doc("c_alice").set({
      senderId: ALICE_UID,
      sdpMid: "0",
      sdpMLineIndex: 0,
      sdp: "candidate:1 1 UDP 2122252543 192.0.2.1 50000 typ host",
      createdAt: new Date(),
    })
  );
  await assertSucceeds(
    bobDb.collection("calls").doc(callId).collection("candidates").doc("c_bob").set({
      senderId: BOB_UID,
      sdpMid: "0",
      sdpMLineIndex: 0,
      sdp: "candidate:2 1 UDP 2122252543 192.0.2.2 50001 typ host",
      createdAt: new Date(),
    })
  );

  // Caller ends call
  await assertSucceeds(
    aliceDb.collection("calls").doc(callId).update({
      status: "ended",
      updatedAt: new Date(),
    })
  );
});

test("Voice Room WebRTC Mesh: Owner-only peer presence and sender/recipient signal isolation", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const charlieDb = testEnv.authenticatedContext("charlie_789").firestore();
  const roomId = "room_webrtc_1";

  await assertSucceeds(
    aliceDb.collection("voice_rooms").doc(roomId).set({
      title: "Live Audio Practice",
      category: "LANGUAGE_EXCHANGE",
      hostId: ALICE_UID,
      hostName: "Alice",
      isLive: true,
      listenersCount: 1,
      createdAt: new Date(),
      updatedAt: new Date(),
    })
  );

  await assertSucceeds(
    aliceDb.collection("voice_rooms").doc(roomId).collection("peers").doc(ALICE_UID).set({
      userId: ALICE_UID,
      displayName: "Alice",
      isSpeaker: true,
      isMuted: false,
      updatedAt: new Date(),
    })
  );

  // Bob cannot overwrite Alice's peer presence
  await assertFails(
    bobDb.collection("voice_rooms").doc(roomId).collection("peers").doc(ALICE_UID).set({
      userId: ALICE_UID,
      displayName: "Hacked Alice",
      isSpeaker: true,
      isMuted: true,
      updatedAt: new Date(),
    })
  );

  await assertSucceeds(
    bobDb.collection("voice_rooms").doc(roomId).collection("peers").doc(BOB_UID).set({
      userId: BOB_UID,
      displayName: "Bob",
      isSpeaker: false,
      isMuted: true,
      updatedAt: new Date(),
    })
  );

  // Alice sends offer signal to Bob
  await assertSucceeds(
    aliceDb.collection("voice_rooms").doc(roomId).collection("signals").doc("sig_1").set({
      fromUserId: ALICE_UID,
      toUserId: BOB_UID,
      type: "offer",
      sdp: "v=0\r\no=- 789 2 IN IP4 127.0.0.1\r\n",
      createdAt: new Date(),
    })
  );

  // Charlie cannot read Alice->Bob signal
  await assertFails(
    charlieDb.collection("voice_rooms").doc(roomId).collection("signals").doc("sig_1").get()
  );

  // Bob can read and delete consumed signal
  await assertSucceeds(
    bobDb.collection("voice_rooms").doc(roomId).collection("signals").doc("sig_1").get()
  );
  await assertSucceeds(
    bobDb.collection("voice_rooms").doc(roomId).collection("signals").doc("sig_1").delete()
  );
});

test("Profile Redesign: Owner can update avatarData, showOnlineStatus, and lastSeenVisibility", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      username: "alice_vibe",
      displayName: "Alice",
      avatarData: "data:image/jpeg;base64,/9j/4AAQSkZJRg==",
      showOnlineStatus: true,
      lastSeenVisibility: "everyone",
      lastSeenAtMs: Date.now(),
      createdAt: new Date(),
      updatedAt: new Date(),
    })
  );

  // Invalid lastSeenVisibility should fail
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      lastSeenVisibility: "invalid_mode",
      updatedAt: new Date(),
    })
  );

  // Valid privacy update & avatar removal should succeed
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).update({
      avatarData: null,
      avatarUrl: null,
      showOnlineStatus: false,
      lastSeenVisibility: "contacts",
      updatedAt: new Date(),
    })
  );

  // Owner can update avatarFrame
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).update({
      avatarFrame: "neon_pulse",
      updatedAt: new Date(),
    })
  );

  // Non-owner cannot alter Alice's avatar, bio, frame, or privacy
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).update({
      showOnlineStatus: true,
      updatedAt: new Date(),
    })
  );
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).update({
      bio: "Hacked bio by Bob",
      updatedAt: new Date(),
    })
  );
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).update({
      avatarFrame: "golden_halo",
      updatedAt: new Date(),
    })
  );
});

test("Chat Redesign: Participant can send forwarded message with isForwarded flag", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const convId = "conv_forward_1";

  await assertSucceeds(
    aliceDb.collection("conversations").doc(convId).set({
      participantIds: [ALICE_UID, BOB_UID],
      lastMessage: "Forwarded note",
      lastSenderId: ALICE_UID,
      updatedAt: new Date(),
    })
  );

  await assertSucceeds(
    aliceDb.collection("conversations").doc(convId).collection("messages").doc("msg_fwd_1").set({
      conversationId: convId,
      senderId: ALICE_UID,
      senderName: "Alice",
      receiverId: BOB_UID,
      text: "Hello from forwarded chat",
      isRead: false,
      mediaType: "text",
      isForwarded: true,
      createdAt: new Date(),
    })
  );

  // Non-boolean isForwarded must fail
  await assertFails(
    aliceDb.collection("conversations").doc(convId).collection("messages").doc("msg_fwd_bad").set({
      conversationId: convId,
      senderId: ALICE_UID,
      receiverId: BOB_UID,
      text: "Invalid flag",
      isForwarded: "yes",
      createdAt: new Date(),
    })
  );
});

test("Voice Room Moderation & Language Ranks: Host can promote/mute peer and owner can save learningLanguageLevels", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const charlieDb = testEnv.authenticatedContext("charlie_789").firestore();
  const roomId = "room_mod_1";

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      username: "alice_ranks",
      displayName: "Alice",
      learningLanguages: ["English", "Japanese"],
      learningLanguageLevels: { English: 4, Japanese: 2 },
      createdAt: new Date(),
      updatedAt: new Date(),
    })
  );

  // Non-owner cannot alter learningLanguageLevels
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).update({
      learningLanguageLevels: { English: 5 },
      updatedAt: new Date(),
    })
  );

  await assertSucceeds(
    aliceDb.collection("voice_rooms").doc(roomId).set({
      title: "Moderation Stage",
      category: "LANGUAGE_EXCHANGE",
      hostId: ALICE_UID,
      hostName: "Alice",
      isLive: true,
      listenersCount: 2,
      createdAt: new Date(),
      updatedAt: new Date(),
    })
  );

  await assertSucceeds(
    bobDb.collection("voice_rooms").doc(roomId).collection("peers").doc(BOB_UID).set({
      userId: BOB_UID,
      displayName: "Bob",
      isSpeaker: false,
      isMuted: true,
      isHandRaised: true,
      flagEmoji: "🇬🇧",
      avatarFrame: "neon_pulse",
      updatedAt: new Date(),
    })
  );

  // Host (Alice) can promote Bob to speaker
  await assertSucceeds(
    aliceDb.collection("voice_rooms").doc(roomId).collection("peers").doc(BOB_UID).update({
      isSpeaker: true,
      isMuted: false,
      isHandRaised: false,
      updatedAt: new Date(),
    })
  );

  // Non-host (Charlie) cannot promote Bob
  await assertFails(
    charlieDb.collection("voice_rooms").doc(roomId).collection("peers").doc(BOB_UID).update({
      isSpeaker: false,
      isMuted: true,
      isHandRaised: false,
      updatedAt: new Date(),
    })
  );
});

test("Evolution: Gamification XP, Disappearing Messages, Private/Co-Host Voice Rooms, and Community Polls", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const charlieDb = testEnv.authenticatedContext("charlie_789").firestore();

  // 1. User gamification stats (vibeXp, vibeLevel, streakDays, unlockedBadges)
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      username: "alice_xp",
      displayName: "Alice",
      vibeXp: 150,
      vibeLevel: 2,
      streakDays: 4,
      unlockedBadges: ["first_room", "polyglot"],
      createdAt: new Date(),
      updatedAt: new Date(),
    })
  );

  // Non-owner cannot tamper with vibeXp
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).update({
      vibeXp: 9999,
      updatedAt: new Date(),
    })
  );

  // Invalid negative vibeXp fails validation
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      vibeXp: -10,
      updatedAt: new Date(),
    })
  );

  // 2. Disappearing message TTL (expiresAtMs)
  const convId = "conv_ttl_1";
  await assertSucceeds(
    aliceDb.collection("conversations").doc(convId).set({
      participantIds: [ALICE_UID, BOB_UID],
      lastMessage: "Ephemeral ping",
      lastSenderId: ALICE_UID,
      updatedAt: new Date(),
    })
  );
  await assertSucceeds(
    aliceDb.collection("conversations").doc(convId).collection("messages").doc("msg_ttl_ok").set({
      conversationId: convId,
      senderId: ALICE_UID,
      senderName: "Alice",
      receiverId: BOB_UID,
      text: "Disappears in 1 hour",
      expiresAtMs: Date.now() + 3600000,
      createdAt: new Date(),
    })
  );
  // Non-integer expiresAtMs fails
  await assertFails(
    aliceDb.collection("conversations").doc(convId).collection("messages").doc("msg_ttl_bad").set({
      conversationId: convId,
      senderId: ALICE_UID,
      receiverId: BOB_UID,
      text: "Invalid TTL",
      expiresAtMs: "never",
      createdAt: new Date(),
    })
  );

  // 3. Private Voice Room, Co-Host Moderation, and AI Summary
  const roomId = "room_evolved_1";
  await assertSucceeds(
    aliceDb.collection("voice_rooms").doc(roomId).set({
      title: "Private Founders Lounge",
      category: "TECH_TALK",
      hostId: ALICE_UID,
      hostName: "Alice",
      isLive: true,
      listenersCount: 1,
      isPrivate: true,
      coHostIds: [BOB_UID],
      createdAt: new Date(),
      updatedAt: new Date(),
    })
  );

  // Co-host Bob can save aiSummary
  await assertSucceeds(
    bobDb.collection("voice_rooms").doc(roomId).update({
      aiSummary: "Discussed Kotlin architecture and WebRTC stability.",
      updatedAt: new Date(),
    })
  );

  // Non-host/non-cohost Charlie cannot alter coHostIds
  await assertFails(
    charlieDb.collection("voice_rooms").doc(roomId).update({
      coHostIds: ["charlie_789"],
      updatedAt: new Date(),
    })
  );

  // 4. Community Poll Posts & Cross-User Voting
  const postId = "post_poll_1";
  await assertSucceeds(
    aliceDb.collection("posts").doc(postId).set({
      authorId: ALICE_UID,
      authorName: "Alice",
      content: "Which topic for tonight's voice room?",
      likesCount: 0,
      commentsCount: 0,
      postType: "poll",
      pollOptions: ["AI & Kotlin", "Dari Poetry", "Music Jam"],
      pollVotes: {},
      createdAt: new Date(),
      updatedAt: new Date(),
    })
  );

  // Bob can vote on the poll
  await assertSucceeds(
    bobDb.collection("posts").doc(postId).update({
      pollVotes: { [BOB_UID]: 0 },
      updatedAt: new Date(),
    })
  );

  // Invalid postType fails
  await assertFails(
    aliceDb.collection("posts").doc("post_poll_invalid").set({
      authorId: ALICE_UID,
      authorName: "Alice",
      content: "Invalid type",
      likesCount: 0,
      commentsCount: 0,
      postType: "advertisement",
      createdAt: new Date(),
      updatedAt: new Date(),
    })
  );
});

