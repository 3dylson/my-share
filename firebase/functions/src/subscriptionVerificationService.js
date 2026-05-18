const {fetchSubscriptionPurchase} = require('./playBillingApi');
const {writeEntitlementSnapshot} = require('./entitlementStore');
const {buildEntitlementSnapshot} = require('./subscriptionEntitlementSnapshot');

async function writePurchaseInfoForUid({
  uid,
  purchaseToken,
  subscriptionId,
  verificationSource,
  notificationType,
  purchaseInfo,
}) {
  const snapshot = buildEntitlementSnapshot({
    purchaseInfo,
    purchaseToken,
    subscriptionId,
    verificationSource,
    notificationType,
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
  const purchaseInfo = await fetchSubscriptionPurchase(purchaseToken);
  return writePurchaseInfoForUid({
    uid,
    purchaseToken,
    subscriptionId,
    verificationSource,
    notificationType,
    purchaseInfo,
  });
}

module.exports = {
  processPurchaseForUid,
  writePurchaseInfoForUid,
};
