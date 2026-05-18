const functions = require('firebase-functions');
const admin = require('firebase-admin');
const {PLAY_BILLING_RTDN_TOPIC} = require('./src/config');
const {PLAY_SERVICE_ACCOUNT} = require('./src/config');
const {requireAuthenticatedUid} = require('./src/authenticatedUid');
const {processPurchaseForUid} = require('./src/subscriptionVerificationService');
const {handlePlayBillingNotification} = require('./src/playBillingNotificationHandler');
const {revalidatePremiumEntitlements} = require('./src/premiumEntitlementRevalidator');

admin.initializeApp();

exports.verifySubscription = functions
    .runWith({
      serviceAccount: PLAY_SERVICE_ACCOUNT,
      minInstances: 0,
      maxInstances: 10,
    })
    .https.onCall(async (data, context) => {
      const uid = await requireAuthenticatedUid(data, context);
      const purchaseToken = data.purchaseToken;
      const subscriptionId = data.subscriptionId;

      if (!purchaseToken || !subscriptionId) {
        throw new functions.https.HttpsError(
            'invalid-argument',
            'The function must be called with a purchaseToken and subscriptionId.',
        );
      }

      try {
        const snapshot = await processPurchaseForUid({
          uid,
          purchaseToken,
          subscriptionId,
          verificationSource: 'callable',
          notificationType: null,
        });
        return {
          isValid: snapshot.isPro,
          entitlementState: snapshot.entitlementState,
          subscriptionState: snapshot.subscriptionState,
          expiryTimeMillis: snapshot.expiryTimeMillis,
          paymentState: snapshot.paymentState,
          acknowledgementState: snapshot.acknowledgementState,
          serverAcknowledgementStatus: snapshot.serverAcknowledgementStatus,
        };
      } catch (error) {
        console.error('Error verifying subscription:', error);
        throw new functions.https.HttpsError(
            'internal',
            'Unable to verify subscription with Google Play.',
        );
      }
    });

exports.handlePlayBillingNotification = functions
    .runWith({
      serviceAccount: PLAY_SERVICE_ACCOUNT,
      minInstances: 0,
      maxInstances: 3,
    })
    .pubsub.topic(PLAY_BILLING_RTDN_TOPIC)
    .onPublish(handlePlayBillingNotification);

exports.revalidatePremiumEntitlements = functions
    .runWith({
      serviceAccount: PLAY_SERVICE_ACCOUNT,
      timeoutSeconds: 540,
      memory: '256MB',
      minInstances: 0,
      maxInstances: 1,
    })
    .pubsub.schedule('every 24 hours')
    .timeZone('Etc/UTC')
    .onRun(revalidatePremiumEntitlements);
