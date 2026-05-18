const {
  acknowledgeSubscriptionIfNeeded,
  androidPublisherClient,
  fetchSubscriptionPurchase,
} = require('./playBillingApi');
const {writeEntitlementSnapshot} = require('./entitlementStore');
const {buildEntitlementSnapshot} = require('./subscriptionEntitlementSnapshot');

async function writePurchaseInfoForUid({
  uid,
  purchaseToken,
  subscriptionId,
  verificationSource,
  notificationType,
  purchaseInfo,
  acknowledgementResult = null,
}) {
  const snapshot = buildEntitlementSnapshot({
    purchaseInfo,
    purchaseToken,
    subscriptionId,
    verificationSource,
    notificationType,
    acknowledgementResult,
  });
  await writeEntitlementSnapshot(uid, purchaseToken, snapshot);
  return snapshot;
}

async function processPurchaseForUid({
  uid,
  purchaseToken,
  subscriptionId,
  verificationSource,
  notificationType,
}) {
  console.log('Verifying subscription', {
    uid,
    subscriptionId,
    verificationSource,
  });
  const androidPublisher = androidPublisherClient();
  const purchaseInfo = await fetchSubscriptionPurchase(
      purchaseToken,
      androidPublisher,
  );
  const acknowledgementResult = verificationSource === 'callable' ?
      await acknowledgeSubscriptionIfNeeded({
        purchaseToken,
        subscriptionId,
        purchaseInfo,
        androidPublisher,
      }) :
      null;

  if (acknowledgementResult) {
    console.log('Subscription acknowledgement evaluated', {
      uid,
      subscriptionId,
      status: acknowledgementResult.status,
    });
  }

  return writePurchaseInfoForUid({
    uid,
    purchaseToken,
    subscriptionId,
    verificationSource,
    notificationType,
    purchaseInfo,
    acknowledgementResult,
  });
}

module.exports = {
  processPurchaseForUid,
  writePurchaseInfoForUid,
};
