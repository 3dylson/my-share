const {PACKAGE_NAME} = require('./config');
const {androidPublisherClient, fetchSubscriptionPurchase} = require('./playBillingApi');
const {writeBillingEvent} = require('./billingEventStore');
const {
  uidForObfuscatedAccountId,
  uidForPurchaseToken,
  writeVoidedEntitlement,
} = require('./entitlementStore');
const {externalAccountIdFromPurchaseInfo} = require('./obfuscatedAccountId');
const {writePurchaseInfoForUid} = require('./subscriptionVerificationService');

function decodePubSubJson(message) {
  if (message.json) {
    return message.json;
  }
  if (!message.data) {
    return null;
  }
  const rawJson = Buffer.from(message.data, 'base64').toString('utf8');
  return JSON.parse(rawJson);
}

async function processSubscriptionNotification(payload, context) {
  const notification = payload.subscriptionNotification;
  const purchaseToken = notification.purchaseToken;
  const subscriptionId = notification.subscriptionId || null;
  const notificationType = notification.notificationType || null;
  const androidPublisher = androidPublisherClient();
  const purchaseInfo = await fetchSubscriptionPurchase(
      purchaseToken,
      androidPublisher,
  );
  const linkedPurchaseToken = purchaseInfo.linkedPurchaseToken || null;
  const externalAccountId = externalAccountIdFromPurchaseInfo(purchaseInfo);
  const uid = await uidForPurchaseToken(purchaseToken) ||
      (linkedPurchaseToken ? await uidForPurchaseToken(linkedPurchaseToken) : null) ||
      (externalAccountId ? await uidForObfuscatedAccountId(externalAccountId) : null);

  if (!uid) {
    console.warn('RTDN subscription token has no known owner', {
      eventId: context.eventId,
      subscriptionId,
      subscriptionState: purchaseInfo.subscriptionState || null,
      notificationType,
      hasExternalAccountId: Boolean(externalAccountId),
    });
    await writeBillingEvent({
      eventId: context.eventId,
      eventType: 'unresolved_subscription_notification',
      purchaseToken,
      subscriptionId,
      notificationType,
      reason: 'No user mapping for purchase token.',
    });
    return;
  }

  const snapshot = await writePurchaseInfoForUid({
    uid,
    purchaseToken,
    subscriptionId,
    verificationSource: 'rtdn',
    notificationType,
    purchaseInfo,
  });
  console.log('RTDN subscription processed', {
    uid,
    subscriptionId: snapshot.subscriptionId,
    subscriptionState: snapshot.subscriptionState,
    entitlementState: snapshot.entitlementState,
    notificationType,
  });
}

async function processVoidedPurchaseNotification(payload, context) {
  const notification = payload.voidedPurchaseNotification;
  const purchaseToken = notification.purchaseToken;
  const subscriptionId = notification.subscriptionId || null;
  const notificationType = notification.notificationType || null;
  const uid = await uidForPurchaseToken(purchaseToken);

  if (!uid) {
    console.warn('RTDN voided purchase has no known owner', {
      eventId: context.eventId,
      subscriptionId,
      notificationType,
    });
    await writeBillingEvent({
      eventId: context.eventId,
      eventType: 'unresolved_voided_purchase',
      purchaseToken,
      subscriptionId,
      notificationType,
      reason: 'No user mapping for voided purchase token.',
    });
    return;
  }

  await writeVoidedEntitlement(uid, purchaseToken, notification);
  await writeBillingEvent({
    eventId: context.eventId,
    eventType: 'voided_purchase_processed',
    purchaseToken,
    subscriptionId,
    notificationType,
    reason: 'Purchase was voided by Google Play.',
  });
  console.log('RTDN voided purchase processed', {
    uid,
    subscriptionId,
    notificationType,
  });
}

async function handlePlayBillingNotification(message, context) {
  const payload = decodePubSubJson(message);
  if (!payload) {
    console.warn('RTDN message had no payload', {eventId: context.eventId});
    return;
  }
  if (payload.packageName && payload.packageName !== PACKAGE_NAME) {
    console.warn('RTDN ignored for another package', {
      eventId: context.eventId,
      packageName: payload.packageName,
    });
    return;
  }

  if (payload.testNotification) {
    console.log('RTDN test notification received', {
      eventId: context.eventId,
    });
    return;
  }
  if (payload.subscriptionNotification?.purchaseToken) {
    await processSubscriptionNotification(payload, context);
    return;
  }
  if (payload.voidedPurchaseNotification?.purchaseToken) {
    await processVoidedPurchaseNotification(payload, context);
    return;
  }

  console.warn('RTDN message type is unsupported', {
    eventId: context.eventId,
  });
  await writeBillingEvent({
    eventId: context.eventId,
    eventType: 'unsupported_rtdn_message',
    reason: 'RTDN payload did not include a supported notification type.',
  });
}

module.exports = {
  handlePlayBillingNotification,
};
