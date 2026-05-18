const admin = require('firebase-admin');
const {purchaseTokenHash} = require('./purchaseTokenHasher');

const ENTITLED_SUBSCRIPTION_STATES = new Set([
  'SUBSCRIPTION_STATE_ACTIVE',
  'SUBSCRIPTION_STATE_IN_GRACE_PERIOD',
  'SUBSCRIPTION_STATE_CANCELED',
]);

function latestExpiryTimeMillis(purchaseInfo) {
  const lineItems = Array.isArray(purchaseInfo.lineItems) ?
      purchaseInfo.lineItems :
      [];
  const expiryTimes = lineItems
      .map((lineItem) => Date.parse(lineItem.expiryTime))
      .filter((expiryTime) => !Number.isNaN(expiryTime))
      .sort((a, b) => b - a);
  return expiryTimes[0] || null;
}

function subscriptionIdFrom(purchaseInfo, fallbackSubscriptionId) {
  const lineItems = Array.isArray(purchaseInfo.lineItems) ?
      purchaseInfo.lineItems :
      [];
  const productId = lineItems
      .map((lineItem) => lineItem.productId)
      .find((value) => typeof value === 'string' && value.length > 0);
  return productId || fallbackSubscriptionId || null;
}

function entitlementStateFor(subscriptionState, expiryTimeMillis) {
  const isEntitledState = ENTITLED_SUBSCRIPTION_STATES.has(subscriptionState);
  const hasFutureExpiry =
      typeof expiryTimeMillis === 'number' && expiryTimeMillis > Date.now();
  if (!isEntitledState || !hasFutureExpiry) {
    return 'FREE';
  }
  return subscriptionState === 'SUBSCRIPTION_STATE_IN_GRACE_PERIOD' ?
      'GRACE_PERIOD' :
      'PRO';
}

function buildEntitlementSnapshot({
  purchaseInfo,
  purchaseToken,
  subscriptionId,
  verificationSource,
  notificationType,
}) {
  const subscriptionState = purchaseInfo.subscriptionState || null;
  const expiryTimeMillis = latestExpiryTimeMillis(purchaseInfo);
  const entitlementState =
      entitlementStateFor(subscriptionState, expiryTimeMillis);
  const isPro = entitlementState === 'PRO' ||
      entitlementState === 'GRACE_PERIOD';
  const linkedPurchaseToken = purchaseInfo.linkedPurchaseToken || null;
  const tokenHash = purchaseTokenHash(purchaseToken);

  return {
    isPro,
    entitlementState,
    subscriptionState,
    subscriptionId: subscriptionIdFrom(purchaseInfo, subscriptionId),
    purchaseToken: admin.firestore.FieldValue.delete(),
    purchaseTokenHash: tokenHash,
    linkedPurchaseToken: admin.firestore.FieldValue.delete(),
    linkedPurchaseTokenHash: linkedPurchaseToken ?
        purchaseTokenHash(linkedPurchaseToken) :
        null,
    proExpiry: isPro && expiryTimeMillis ?
        admin.firestore.Timestamp.fromMillis(expiryTimeMillis) :
        null,
    expiryTimeMillis: expiryTimeMillis ? String(expiryTimeMillis) : null,
    paymentState: null,
    latestOrderId: purchaseInfo.latestOrderId || null,
    regionCode: purchaseInfo.regionCode || null,
    verificationSource,
    notificationType: notificationType || null,
    lastVerifiedAt: admin.firestore.FieldValue.serverTimestamp(),
  };
}

module.exports = {
  buildEntitlementSnapshot,
};
