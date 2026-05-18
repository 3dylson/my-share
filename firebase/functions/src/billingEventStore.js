const crypto = require('crypto');
const admin = require('firebase-admin');
const {purchaseTokenHash} = require('./purchaseTokenHasher');

function eventDocumentId(eventId) {
  return eventId ? String(eventId).replace(/\//g, '_') : crypto.randomUUID();
}

async function writeBillingEvent({
  eventId,
  eventType,
  purchaseToken,
  subscriptionId,
  notificationType,
  reason,
}) {
  await admin.firestore()
      .collection('billing_events')
      .doc(eventDocumentId(eventId))
      .set({
        eventType,
        purchaseTokenHash: purchaseToken ? purchaseTokenHash(purchaseToken) : null,
        subscriptionId: subscriptionId || null,
        notificationType: notificationType || null,
        reason,
        createdAt: admin.firestore.FieldValue.serverTimestamp(),
      }, {merge: true});
}

module.exports = {
  writeBillingEvent,
};
