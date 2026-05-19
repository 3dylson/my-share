const admin = require('firebase-admin');

const CAMPAIGN_ID = 'legacy_app_user_2026_05';
const GRANT_DURATION_DAYS = 365;
const MAX_GRANTS = 100;

function expiryDateFrom(now) {
  return new Date(now.getTime() + GRANT_DURATION_DAYS * 24 * 60 * 60 * 1000);
}

function grantSnapshot(expiryDate, campaignId) {
  return {
    isPro: true,
    entitlementState: 'PRO',
    subscriptionState: 'PROMOTIONAL_LEGACY_GRANT',
    subscriptionId: null,
    purchaseToken: admin.firestore.FieldValue.delete(),
    purchaseTokenHash: null,
    linkedPurchaseToken: admin.firestore.FieldValue.delete(),
    linkedPurchaseTokenHash: null,
    proExpiry: admin.firestore.Timestamp.fromDate(expiryDate),
    expiryTimeMillis: String(expiryDate.getTime()),
    paymentState: null,
    acknowledgementState: null,
    serverAcknowledgementStatus: null,
    latestOrderId: null,
    regionCode: null,
    verificationSource: 'legacy_premium_grant',
    notificationType: null,
    grantCampaignId: campaignId,
    lastVerifiedAt: admin.firestore.FieldValue.serverTimestamp(),
  };
}

async function claimLegacyPremiumGrant({uid, campaignId, clientEligibility}) {
  if (campaignId !== CAMPAIGN_ID || clientEligibility !== true) {
    return {granted: false, reason: 'not_eligible'};
  }

  const firestore = admin.firestore();
  const userRef = firestore.collection('users').doc(uid);
  const entitlementRef = userRef.collection('entitlements').doc('current');
  const grantRef = firestore.collection('legacy_premium_grants').doc(uid);
  const configRef = firestore.collection('app_config')
      .doc('legacy_premium_grant');
  const now = new Date();
  const expiryDate = expiryDateFrom(now);
  const expiryTimeMillis = String(expiryDate.getTime());
  let result = {
    granted: false,
    reason: 'cap_reached',
    claimedCount: MAX_GRANTS,
    maxClaims: MAX_GRANTS,
  };

  await firestore.runTransaction(async (transaction) => {
    const [grantDoc, configDoc] = await Promise.all([
      transaction.get(grantRef),
      transaction.get(configRef),
    ]);
    if (grantDoc.exists) {
      result = {
        granted: true,
        reason: 'already_claimed',
        expiryTimeMillis: grantDoc.get('expiryTimeMillis') || expiryTimeMillis,
        claimedCount: configDoc.get('claimedCount') || 0,
        maxClaims: configDoc.get('maxClaims') || MAX_GRANTS,
      };
      return;
    }

    const claimedCount = configDoc.exists ?
      Number(configDoc.get('claimedCount') || 0) :
      0;
    const maxClaims = configDoc.exists ?
      Number(configDoc.get('maxClaims') || MAX_GRANTS) :
      MAX_GRANTS;
    const active = configDoc.exists ? configDoc.get('active') !== false : true;
    if (!active || claimedCount >= maxClaims) {
      result = {
        granted: false,
        reason: 'cap_reached',
        claimedCount,
        maxClaims,
      };
      return;
    }

    const snapshot = grantSnapshot(expiryDate, campaignId);
    const nextCount = claimedCount + 1;
    const userFields = {
      isPro: true,
      entitlementState: 'PRO',
      subscriptionState: 'PROMOTIONAL_LEGACY_GRANT',
      proExpiry: admin.firestore.Timestamp.fromDate(expiryDate),
      lastVerifiedAt: admin.firestore.FieldValue.serverTimestamp(),
    };
    transaction.set(userRef, userFields, {merge: true});
    transaction.set(entitlementRef, snapshot, {merge: true});
    transaction.set(grantRef, {
      uid,
      campaignId,
      expiryTimeMillis,
      claimedAt: admin.firestore.FieldValue.serverTimestamp(),
    });
    transaction.set(configRef, {
      active: nextCount < maxClaims,
      claimedCount: nextCount,
      maxClaims,
      updatedAt: admin.firestore.FieldValue.serverTimestamp(),
    }, {merge: true});
    result = {
      granted: true,
      reason: 'claimed',
      expiryTimeMillis,
      claimedCount: nextCount,
      maxClaims,
    };
  });

  return result;
}

module.exports = {
  CAMPAIGN_ID,
  GRANT_DURATION_DAYS,
  MAX_GRANTS,
  claimLegacyPremiumGrant,
};
