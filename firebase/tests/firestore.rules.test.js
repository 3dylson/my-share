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
let testEnv;

beforeAll(async () => {
  testEnv = await initializeTestEnvironment({
    projectId: "my-share-finance",
    firestore: {
      rules: fs.readFileSync(RULES_PATH, "utf8"),
      host: "localhost",
      port: 8080,
    },
  });
});

afterAll(async () => {
  await testEnv.cleanup();
});

afterEach(async () => {
  await testEnv.clearFirestore();
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

async function seedUserDoc(uid) {
  await testEnv.withSecurityRulesDisabled(async (ctx) => {
    await ctx.firestore().collection("users").doc(uid).set({ created: true });
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
