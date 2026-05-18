const functions = require("firebase-functions");
const admin = require("firebase-admin");
const {google} = require("googleapis");

admin.initializeApp();

async function requireAuthenticatedUid(data, context) {
    if (context.auth && context.auth.uid) {
        return context.auth.uid;
    }

    const firebaseIdToken = data.firebaseIdToken;
    if (typeof firebaseIdToken !== "string" || firebaseIdToken.length === 0) {
        throw new functions.https.HttpsError(
            'unauthenticated',
            'User must be authenticated to verify a subscription.'
        );
    }

    try {
        const decodedToken = await admin.auth().verifyIdToken(firebaseIdToken);
        return decodedToken.uid;
    } catch (error) {
        console.error("Error verifying Firebase ID token:", error);
        throw new functions.https.HttpsError(
            'unauthenticated',
            'User must be authenticated to verify a subscription.'
        );
    }
}

/**
 * Validates a Google Play Purchase Token with the Google Play Developer API.
 * 
 * Required env vars or config:
 * - packageName: "pt.ms.myshare"
 * - serviceAccountEmail / privateKey configured in Firebase or Google Cloud
 */
exports.verifySubscription = functions
    .runWith({ serviceAccount: "564550726509-compute@developer.gserviceaccount.com" })
    .https.onCall(async (data, context) => {
    // 1. Authenticate caller
    const uid = await requireAuthenticatedUid(data, context);

    const purchaseToken = data.purchaseToken;
    const subscriptionId = data.subscriptionId; // e.g. "myshare_premium"
    const packageName = "pt.ms.myshare";

    if (!purchaseToken || !subscriptionId) {
        throw new functions.https.HttpsError(
            'invalid-argument', 
            'The function must be called with a purchaseToken and subscriptionId.'
        );
    }

    try {
        console.log("Verifying subscription", {uid, subscriptionId});

        // 2. Initialize the Google Play Developer API client
        const auth = new google.auth.GoogleAuth({
            scopes: ['https://www.googleapis.com/auth/androidpublisher']
        });
        
        const androidPublisher = google.androidpublisher({
            version: 'v3',
            auth: auth
        });

        // 3. Verify the purchase token
        const response = await androidPublisher.purchases.subscriptionsv2.get({
            packageName: packageName,
            token: purchaseToken
        });

        const purchaseInfo = response.data;
        
        // 4. Validate if the subscription is still active
        const subscriptionState = purchaseInfo.subscriptionState ?? null;
        const expiryTimeMillis = purchaseInfo.lineItems
            ?.map((lineItem) => Date.parse(lineItem.expiryTime))
            .filter((expiry) => !Number.isNaN(expiry))
            .sort((a, b) => b - a)[0];
        const now = Date.now();
        const isEntitledState = [
            "SUBSCRIPTION_STATE_ACTIVE",
            "SUBSCRIPTION_STATE_IN_GRACE_PERIOD",
            "SUBSCRIPTION_STATE_CANCELED"
        ].includes(subscriptionState);
        const isActive = isEntitledState && expiryTimeMillis > now;

        const entitlementState = isActive ?
            (subscriptionState === "SUBSCRIPTION_STATE_IN_GRACE_PERIOD" ? "GRACE_PERIOD" : "PRO") :
            "FREE";
        const entitlementSnapshot = {
            isPro: isActive,
            entitlementState,
            subscriptionState,
            subscriptionId,
            purchaseToken,
            proExpiry: isActive && expiryTimeMillis ? admin.firestore.Timestamp.fromMillis(expiryTimeMillis) : null,
            expiryTimeMillis: expiryTimeMillis ? String(expiryTimeMillis) : null,
            paymentState: null,
            lastVerifiedAt: admin.firestore.FieldValue.serverTimestamp()
        };

        // 5. Update the server entitlement snapshot.
        if (isActive) {
            await admin.firestore().collection('users').doc(uid).set({
                isPro: true,
                entitlementState,
                subscriptionState,
                proExpiry: admin.firestore.Timestamp.fromMillis(expiryTimeMillis),
                lastVerifiedAt: admin.firestore.FieldValue.serverTimestamp()
            }, { merge: true });
        } else {
            // Subscription expired
            await admin.firestore().collection('users').doc(uid).set({
                isPro: false,
                entitlementState,
                subscriptionState,
                lastVerifiedAt: admin.firestore.FieldValue.serverTimestamp()
            }, { merge: true });
        }
        await admin.firestore()
            .collection('users')
            .doc(uid)
            .collection('entitlements')
            .doc('current')
            .set(entitlementSnapshot, { merge: true });

        // 6. Return the source of truth to the client
        return {
            isValid: isActive,
            entitlementState,
            subscriptionState,
            expiryTimeMillis: expiryTimeMillis ? String(expiryTimeMillis) : null,
            paymentState: null
        };

    } catch (error) {
        console.error("Error verifying subscription:", error);
        throw new functions.https.HttpsError(
            'internal', 
            'Unable to verify subscription with Google Play.'
        );
    }
});
