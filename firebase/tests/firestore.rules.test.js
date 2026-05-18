/**
 * Firestore Security Rules Tests — My Share
 *
 * Run with Firebase Emulator:
 *   cd firebase && firebase emulators:exec --only firestore,storage 'npm --prefix tests test'
 *
 * Or standalone after `firebase emulators:start --only firestore`:
 *   cd firebase/tests && npm test
 */

const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const fs = require("fs");
const path = require("path");

const RULES_PATH = path.join(__dirname, "../firestore.rules");
const STORAGE_RULES_PATH = path.join(__dirname, "../storage.rules");
let testEnv;

beforeAll(async () => {
  testEnv = await initializeTestEnvironment({
    projectId: "my-share-finance",
    firestore: {
      rules: fs.readFileSync(RULES_PATH, "utf8"),
      host: "localhost",
      port: 8080,
    },
    storage: {
      rules: fs.readFileSync(STORAGE_RULES_PATH, "utf8"),
      host: "localhost",
      port: 9199,
    },
  });
});

afterAll(async () => {
  await testEnv.cleanup();
});

afterEach(async () => {
  await testEnv.clearFirestore();
  await testEnv.clearStorage();
});

// ─────────────────────────────────────────────────────────────────────────────
// Helpers
// ─────────────────────────────────────────────────────────────────────────────

const USER_A = "userA";
const USER_B = "userB";

function authedDb(uid) {
  return testEnv.authenticatedContext(uid).firestore();
}

function anonDb() {
  return testEnv.unauthenticatedContext().firestore();
}

function authedStorage(uid) {
  return testEnv.authenticatedContext(uid).storage();
}

function anonStorage() {
  return testEnv.unauthenticatedContext().storage();
}

async function seedUserDoc(uid) {
  await testEnv.withSecurityRulesDisabled(async (ctx) => {
    await ctx.firestore().collection("users").doc(uid).set({ created: true });
  });
}

async function seedStorageObject(storagePath) {
  await testEnv.withSecurityRulesDisabled(async (ctx) => {
    await ctx.storage().ref(storagePath).putString("seed-data");
  });
}

// ─────────────────────────────────────────────────────────────────────────────
// Unauthenticated access
// ─────────────────────────────────────────────────────────────────────────────

describe("Unauthenticated access", () => {
  test("cannot read any user document", async () => {
    await seedUserDoc(USER_A);
    await assertFails(anonDb().collection("users").doc(USER_A).get());
  });

  test("cannot write any user document", async () => {
    await assertFails(
      anonDb().collection("users").doc(USER_A).set({ hack: true })
    );
  });
});

// ─────────────────────────────────────────────────────────────────────────────
// Public app config
// ─────────────────────────────────────────────────────────────────────────────

describe("Public app config", () => {
  test("any client can read Android update policy", async () => {
    await testEnv.withSecurityRulesDisabled(async (ctx) => {
      await ctx
        .firestore()
        .collection("app_config")
        .doc("android_update_policy")
        .set({
          minimumSupportedVersionCode: 8,
          immediateUpdateRequired: true,
          playStorePackageName: "pt.ms.myshare",
        });
    });

    await assertSucceeds(
      anonDb().collection("app_config").doc("android_update_policy").get()
    );
  });

  test("clients cannot write Android update policy", async () => {
    await assertFails(
      authedDb(USER_A)
        .collection("app_config")
        .doc("android_update_policy")
        .set({ minimumSupportedVersionCode: 8 })
    );
  });
});

// ─────────────────────────────────────────────────────────────────────────────
// Own user document
// ─────────────────────────────────────────────────────────────────────────────

describe("Authenticated user — own document", () => {
  test("can read own user document", async () => {
    await seedUserDoc(USER_A);
    await assertSucceeds(authedDb(USER_A).collection("users").doc(USER_A).get());
  });

  test("can write own user document", async () => {
    await assertSucceeds(
      authedDb(USER_A).collection("users").doc(USER_A).set({ name: "Alice" })
    );
  });

  test("can update own language and currency profile fields", async () => {
    await assertSucceeds(
      authedDb(USER_A)
        .collection("users")
        .doc(USER_A)
        .set({ languageTag: "pt-PT", currencyCode: "EUR" }, { merge: true })
    );
  });

  test("cannot update protected billing fields with profile preferences", async () => {
    await seedUserDoc(USER_A);
    await assertFails(
      authedDb(USER_A)
        .collection("users")
        .doc(USER_A)
        .set({ languageTag: "pt-PT", currencyCode: "EUR", isPro: true }, { merge: true })
    );
  });

  test("cannot create own user document with protected billing fields", async () => {
    await assertFails(
      authedDb(USER_A)
        .collection("users")
        .doc(USER_A)
        .set({
          languageTag: "pt-PT",
          entitlementState: "PRO",
          subscriptionState: "ACTIVE",
        })
    );
  });

  test.each([
    ["entitlementState", "PRO"],
    ["subscriptionState", "ACTIVE"],
    ["purchaseToken", "fake-token"],
    ["subscriptionId", "myshare_annual"],
    ["expiryTimeMillis", 1842300000000],
    ["paymentState", "RECEIVED"],
    ["proExpiry", 1842300000000],
    ["lastVerifiedAt", 1842300000000],
  ])("cannot update protected billing field %s", async (fieldName, fieldValue) => {
    await seedUserDoc(USER_A);
    await assertFails(
      authedDb(USER_A)
        .collection("users")
        .doc(USER_A)
        .set({ [fieldName]: fieldValue }, { merge: true })
    );
  });

  test("can read own plans sub-collection", async () => {
    await testEnv.withSecurityRulesDisabled(async (ctx) => {
      await ctx
        .firestore()
        .collection("users")
        .doc(USER_A)
        .collection("plans")
        .doc("plan1")
        .set({ income: 1500 });
    });
    await assertSucceeds(
      authedDb(USER_A).collection("users").doc(USER_A).collection("plans").doc("plan1").get()
    );
  });

  test("can write own plans sub-collection", async () => {
    await assertSucceeds(
      authedDb(USER_A)
        .collection("users")
        .doc(USER_A)
        .collection("plans")
        .doc("plan1")
        .set({ income: 2000 })
    );
  });

  test("can read own reminder settings document", async () => {
    await testEnv.withSecurityRulesDisabled(async (ctx) => {
      await ctx
        .firestore()
        .collection("users")
        .doc(USER_A)
        .collection("settings")
        .doc("reminders")
        .set({ enabled: true });
    });
    await assertSucceeds(
      authedDb(USER_A)
        .collection("users")
        .doc(USER_A)
        .collection("settings")
        .doc("reminders")
        .get()
    );
  });

  test("can read own reviews sub-collection", async () => {
    await testEnv.withSecurityRulesDisabled(async (ctx) => {
      await ctx
        .firestore()
        .collection("users")
        .doc(USER_A)
        .collection("reviews")
        .doc("rev1")
        .set({ week: "2026-03-23" });
    });
    await assertSucceeds(
      authedDb(USER_A)
        .collection("users")
        .doc(USER_A)
        .collection("reviews")
        .doc("rev1")
        .get()
    );
  });
});

// ─────────────────────────────────────────────────────────────────────────────
// Cross-user access (MUST BE DENIED)
// ─────────────────────────────────────────────────────────────────────────────

describe("Authenticated user — cross-user access denied", () => {
  test("userB cannot read userA document", async () => {
    await seedUserDoc(USER_A);
    await assertFails(authedDb(USER_B).collection("users").doc(USER_A).get());
  });

  test("userB cannot write userA document", async () => {
    await assertFails(
      authedDb(USER_B).collection("users").doc(USER_A).set({ hack: true })
    );
  });

  test("userB cannot read userA plans", async () => {
    await testEnv.withSecurityRulesDisabled(async (ctx) => {
      await ctx
        .firestore()
        .collection("users")
        .doc(USER_A)
        .collection("plans")
        .doc("p1")
        .set({ income: 1000 });
    });
    await assertFails(
      authedDb(USER_B)
        .collection("users")
        .doc(USER_A)
        .collection("plans")
        .doc("p1")
        .get()
    );
  });
});

// ─────────────────────────────────────────────────────────────────────────────
// Entitlements sub-collection — client writes must be DENIED
// Writes are handled server-side by Cloud Function (Admin SDK bypasses rules)
// ─────────────────────────────────────────────────────────────────────────────

describe("Entitlements sub-collection", () => {
  test("user can read own entitlement", async () => {
    await testEnv.withSecurityRulesDisabled(async (ctx) => {
      await ctx
        .firestore()
        .collection("users")
        .doc(USER_A)
        .collection("entitlements")
        .doc("sub")
        .set({ isPro: true });
    });
    await assertSucceeds(
      authedDb(USER_A)
        .collection("users")
        .doc(USER_A)
        .collection("entitlements")
        .doc("sub")
        .get()
    );
  });

  test("user CANNOT write own entitlement (server-only)", async () => {
    await assertFails(
      authedDb(USER_A)
        .collection("users")
        .doc(USER_A)
        .collection("entitlements")
        .doc("sub")
        .set({ isPro: true })
    );
  });

  test("userB CANNOT read userA entitlement", async () => {
    await testEnv.withSecurityRulesDisabled(async (ctx) => {
      await ctx
        .firestore()
        .collection("users")
        .doc(USER_A)
        .collection("entitlements")
        .doc("sub")
        .set({ isPro: true });
    });
    await assertFails(
      authedDb(USER_B)
        .collection("users")
        .doc(USER_A)
        .collection("entitlements")
        .doc("sub")
        .get()
    );
  });
});

// ─────────────────────────────────────────────────────────────────────────────
// Storage — user-owned files only
// ─────────────────────────────────────────────────────────────────────────────

describe("Storage user files", () => {
  test("user can write under own storage prefix", async () => {
    await assertSucceeds(
      authedStorage(USER_A).ref(`users/${USER_A}/avatar.txt`).putString("avatar")
    );
  });

  test("user can read under own storage prefix", async () => {
    const storagePath = `users/${USER_A}/exports/plan.txt`;
    await seedStorageObject(storagePath);

    await assertSucceeds(
      authedStorage(USER_A).ref(storagePath).getDownloadURL()
    );
  });

  test("unauthenticated client cannot write user storage files", async () => {
    await assertFails(
      anonStorage().ref(`users/${USER_A}/avatar.txt`).putString("avatar")
    );
  });

  test("user cannot write another user's storage prefix", async () => {
    await assertFails(
      authedStorage(USER_B).ref(`users/${USER_A}/avatar.txt`).putString("avatar")
    );
  });

  test("user cannot write outside user-owned storage paths", async () => {
    await assertFails(
      authedStorage(USER_A).ref("public/avatar.txt").putString("avatar")
    );
  });
});
