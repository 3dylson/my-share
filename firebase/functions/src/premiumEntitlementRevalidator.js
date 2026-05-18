const {activeTokenRecordsForRevalidation} = require('./entitlementStore');
const {processPurchaseForUid} = require('./subscriptionVerificationService');

async function revalidatePremiumEntitlements() {
  const tokenRecords = await activeTokenRecordsForRevalidation();
  let processedCount = 0;
  let failedCount = 0;

  for (const tokenRecord of tokenRecords) {
    const uid = tokenRecord.uid;
    const purchaseToken = tokenRecord.purchaseToken;
    if (!uid || !purchaseToken) {
      failedCount += 1;
      console.warn('Skipping token revalidation with missing fields', {
        tokenHash: tokenRecord.tokenHash,
        source: tokenRecord.source,
      });
      continue;
    }

    try {
      await processPurchaseForUid({
        uid,
        purchaseToken,
        subscriptionId: tokenRecord.subscriptionId || null,
        verificationSource: 'scheduled',
        notificationType: null,
      });
      processedCount += 1;
    } catch (error) {
      failedCount += 1;
      console.error('Scheduled entitlement revalidation failed', {
        tokenHash: tokenRecord.tokenHash,
        source: tokenRecord.source,
        error,
      });
    }
  }

  console.log('Scheduled entitlement revalidation completed', {
    processedCount,
    failedCount,
  });
  return {processedCount, failedCount};
}

module.exports = {
  revalidatePremiumEntitlements,
};
