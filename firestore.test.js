const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ADMIN_UID = "admin_wallx_1";
const ADMIN_EMAIL = "d99312907@gmail.com";
const ALICE_UID = "alice_user_123";
const BOB_UID = "bob_user_456";

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

function validWallpaperPayload(uploadedBy, overrides = {}) {
  const now = new Date();
  return {
    title: "Liquid Prism 8K",
    description: "Ultra-pure iOS 27 liquid glass refraction wallpaper.",
    category: "AMOLED",
    tags: ["amoled", "liquid", "8k"],
    resolution: "3840x2160 (4K)",
    fileSize: "4.2 MB",
    fileSizeBytes: 4200000,
    mimeType: "image/jpeg",
    imageUrl: "data:image/jpeg;base64,/9j/4AAQSkZJRgABAQAAAQABAAD/",
    thumbnailUrl: "data:image/jpeg;base64,/9j/4AAQSkZJRgABAQAAAQABAAD/",
    storagePath: "firestore_cloud/wall_1.jpg",
    status: "published",
    isFeatured: true,
    isTrending: true,
    downloadCount: 0,
    favoriteCount: 0,
    viewCount: 0,
    uploadedBy,
    createdAt: now,
    updatedAt: now,
    ...overrides,
  };
}

function validUserPayload(userId, email, overrides = {}) {
  const now = new Date();
  return {
    userId,
    email,
    displayName: "Test User",
    photoUrl: "https://example.com/avatar.png",
    role: "user",
    status: "active",
    downloadCount: 0,
    favoriteCount: 0,
    themePreference: "dark",
    createdAt: now,
    updatedAt: now,
    ...overrides,
  };
}

test("1. Uploaded wallpapers with Base64 data:image and viewCount are readable across all devices", async () => {
  const adminDb = testEnv.authenticatedContext(ADMIN_UID, {
    email: ADMIN_EMAIL,
    email_verified: true,
  }).firestore();

  await assertSucceeds(
    adminDb.collection("wallpapers").doc("wall_1").set(validWallpaperPayload(ADMIN_UID))
  );

  const clientDb = testEnv.unauthenticatedContext().firestore();
  await assertSucceeds(clientDb.collection("wallpapers").get());
  await assertSucceeds(clientDb.collection("wallpapers").doc("wall_1").get());
});

test("2. Admin and Creator can create, update, and delete valid wallpapers", async () => {
  const adminDb = testEnv.authenticatedContext(ADMIN_UID, {
    email: ADMIN_EMAIL,
    email_verified: true,
  }).firestore();

  await assertSucceeds(
    adminDb.collection("wallpapers").doc("wall_1").set(validWallpaperPayload(ADMIN_UID))
  );

  await assertSucceeds(
    adminDb.collection("wallpapers").doc("wall_1").update({
      title: "Updated Liquid Prism 8K",
      isFeatured: false,
      updatedAt: new Date(),
    })
  );

  await assertSucceeds(adminDb.collection("wallpapers").doc("wall_1").delete());
});

test("3. Invalid MIME or malformed wallpaper schemas are rejected", async () => {
  const adminDb = testEnv.authenticatedContext(ADMIN_UID, {
    email: ADMIN_EMAIL,
    email_verified: true,
  }).firestore();

  // Invalid MIME rejected
  await assertFails(
    adminDb.collection("wallpapers").doc("wall_exe").set(
      validWallpaperPayload(ADMIN_UID, { mimeType: "application/x-msdownload" })
    )
  );

  // Empty title rejected
  await assertFails(
    adminDb.collection("wallpapers").doc("wall_empty").set(
      validWallpaperPayload(ADMIN_UID, { title: "" })
    )
  );
});

test("4. PII Isolation & Privilege Escalation Prevention on users collection", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();

  // Alice cannot self-assign admin role
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).set(
      validUserPayload(ALICE_UID, "alice@example.com", { role: "admin" })
    )
  );

  // Alice creates valid user profile
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set(
      validUserPayload(ALICE_UID, "alice@example.com")
    )
  );

  // Bob cannot read Alice's PII profile
  await assertFails(bobDb.collection("users").doc(ALICE_UID).get());

  // Alice cannot escalate her own role via update
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      role: "admin",
      updatedAt: new Date(),
    })
  );
});

test("5. Favorites and Downloads enforce user ownership, query alignment, and wallpaper existence", async () => {
  await testEnv.withSecurityRulesDisabled(async (ctx) => {
    await ctx.firestore().collection("wallpapers").doc("wall_100").set(validWallpaperPayload(ADMIN_UID));
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();

  // Orphaned favorite (non-existent wallpaper) fails
  await assertFails(
    aliceDb.collection("favorites").doc("fav_orphan").set({
      userId: ALICE_UID,
      wallpaperId: "non_existent_wall",
      wallpaperTitle: "Ghost",
      wallpaperThumbnailUrl: "https://example.com/t.jpg",
      wallpaperCategory: "AMOLED",
      wallpaperResolution: "4K",
      createdAt: new Date(),
    })
  );

  // Valid favorite succeeds
  await assertSucceeds(
    aliceDb.collection("favorites").doc("fav_1").set({
      userId: ALICE_UID,
      wallpaperId: "wall_100",
      wallpaperTitle: "Liquid Prism 8K",
      wallpaperThumbnailUrl: "https://example.com/t.jpg",
      wallpaperCategory: "AMOLED",
      wallpaperResolution: "4K",
      createdAt: new Date(),
    })
  );

  // Query alignment: Alice can list her own favorites with userId filter
  await assertSucceeds(
    aliceDb.collection("favorites").where("userId", "==", ALICE_UID).get()
  );

  // Bob cannot list Alice's favorites
  await assertFails(
    bobDb.collection("favorites").where("userId", "==", ALICE_UID).get()
  );
});
