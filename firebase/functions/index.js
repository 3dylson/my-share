const functions = require("firebase-functions");
const admin = require("firebase-admin");
const {google} = require("googleapis");

admin.initializeApp();

/**
 * Validates a Google Play Purchase Token with the Google Play Developer API.
 * 
 * Required env vars or config:
 * - packageName: "pt.ms.myshare"
 * - serviceAccountEmail / privateKey configured in Firebase or Google Cloud
 */
exports.verifySubscription = functions.https.onCall(async (data, context) => {
    // 1. Authenticate caller
    if (!context.auth) {
        throw new functions.https.HttpsError(
            'unauthenticated', 
            'User must be authenticated to verify a subscription.'
        );
    }

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
        // 2. Initialize the Google Play Developer API client
        const auth = new google.auth.GoogleAuth({
            scopes: ['https://www.googleapis.com/auth/androidpublisher']
        });
        
        const androidPublisher = google.androidpublisher({
            version: 'v3',
            auth: auth
        });

        // 3. Verify the purchase token
        const response = await androidPublisher.purchases.subscriptions.get({
            packageName: packageName,
            subscriptionId: subscriptionId,
            token: purchaseToken
        });

        const purchaseInfo = response.data;
        
        // 4. Validate if the subscription is still active
        // paymentState 1 = Payment received. 
        // expiryTimeMillis must be in the future.
        const now = Date.now();
        const expiryTime = parseInt(purchaseInfo.expiryTimeMillis, 10);
        
        const isActive = expiryTime > now;

        // 5. Optionally, update the user's Firestore document
        if (isActive) {
            await admin.firestore().collection('users').doc(context.auth.uid).set({
                isPro: true,
                proExpiry: admin.firestore.Timestamp.fromMillis(expiryTime),
                lastVerifiedAt: admin.firestore.FieldValue.serverTimestamp()
            }, { merge: true });
        } else {
            // Subscription expired
            await admin.firestore().collection('users').doc(context.auth.uid).set({
                isPro: false
            }, { merge: true });
        }

        // 6. Return the source of truth to the client
        return {
            isValid: isActive,
            expiryTimeMillis: purchaseInfo.expiryTimeMillis,
            paymentState: purchaseInfo.paymentState
        };

    } catch (error) {
        console.error("Error verifying subscription:", error);
        throw new functions.https.HttpsError(
            'internal', 
            'Unable to verify subscription with Google Play.'
        );
    }
});
