const admin = require('firebase-admin');

const CAMPAIGN_ID = 'legacy_app_user_2026_05';
const ANNUAL_SUBSCRIPTION_ID = 'myshare_annual';
const FOUNDER_OFFER_ID = 'founder-free-1y';
const FOUNDER_OFFER_TAG = 'founder-offer';
const MAX_GRANTS = 100;
const RESERVATION_MINUTES = 30;

function reservationExpiryFrom(now) {
  return new Date(now.getTime() + RESERVATION_MINUTES * 60 * 1000);
}

function lineItemsFrom(purchaseInfo) {
  return Array.isArray(purchaseInfo.lineItems) ? purchaseInfo.lineItems : [];
}

function founderOfferDetailsFrom(purchaseInfo) {
  return lineItemsFrom(purchaseInfo)
      .map((lineItem) => lineItem.offerDetails)
      .find((offerDetails) => {
        if (!offerDetails) return false;
        const tags = Array.isArray(offerDetails.offerTags) ?
          offerDetails.offerTags :
          [];
        return offerDetails.offerId === FOUNDER_OFFER_ID ||
          tags.includes(FOUNDER_OFFER_TAG);
      }) || null;
}

function isFounderOfferPurchase({purchaseInfo, subscriptionId}) {
  const productMatches = lineItemsFrom(purchaseInfo).some((lineItem) =>
    lineItem.productId === ANNUAL_SUBSCRIPTION_ID,
  ) || subscriptionId === ANNUAL_SUBSCRIPTION_ID;
  return productMatches && founderOfferDetailsFrom(purchaseInfo) !== null;
}

function numericConfig(configDoc, fieldName, fallback) {
  if (!configDoc.exists) return fallback;
  const value = Number(configDoc.get(fieldName));
  return Number.isFinite(value) ? value : fallback;
}

async function cleanupExpiredFounderReservations(firestore, now) {
  const expiredReservations = await firestore
      .collection('legacy_premium_grants')
      .where('status', '==', 'reserved')
      .limit(50)
      .get();

  if (expiredReservations.empty) return 0;

  const batch = firestore.batch();
  const expiredDocs = expiredReservations.docs.filter((doc) => {
    const expiresAt = doc.get('reservationExpiresAt');
    return expiresAt && expiresAt.toDate().getTime() <= now.getTime();
  });

  if (expiredDocs.length === 0) return 0;

  expiredDocs.forEach((doc) => {
    batch.set(doc.ref, {
      status: 'expired',
      expiredAt: admin.firestore.FieldValue.serverTimestamp(),
    }, {merge: true});
  });
  const configRef = firestore.collection('app_config')
      .doc('legacy_premium_grant');
  batch.set(configRef, {
    reservedCount: admin.firestore.FieldValue.increment(-expiredDocs.length),
    updatedAt: admin.firestore.FieldValue.serverTimestamp(),
  }, {merge: true});
  await batch.commit();
  return expiredDocs.length;
}

async function reserveLegacyPremiumFounderOffer({
  uid,
  campaignId,
  clientEligibility,
}) {
  if (campaignId !== CAMPAIGN_ID || clientEligibility !== true) {
    return {reserved: false, reason: 'not_eligible'};
  }

  const firestore = admin.firestore();
  const now = new Date();
  await cleanupExpiredFounderReservations(firestore, now);

  const grantRef = firestore.collection('legacy_premium_grants').doc(uid);
  const configRef = firestore.collection('app_config')
      .doc('legacy_premium_grant');
  let result = {
    reserved: false,
    reason: 'cap_reached',
    claimedCount: MAX_GRANTS,
    reservedCount: 0,
    maxClaims: MAX_GRANTS,
  };

  await firestore.runTransaction(async (transaction) => {
    const [grantDoc, configDoc] = await Promise.all([
      transaction.get(grantRef),
      transaction.get(configRef),
    ]);
    const claimedCount = numericConfig(configDoc, 'claimedCount', 0);
    const reservedCount = numericConfig(configDoc, 'reservedCount', 0);
    const maxClaims = numericConfig(configDoc, 'maxClaims', MAX_GRANTS);
    const active = configDoc.exists ? configDoc.get('active') !== false : true;
    const existingStatus = grantDoc.exists ? grantDoc.get('status') : null;
    const existingExpiry = grantDoc.exists ?
      grantDoc.get('reservationExpiresAt') :
      null;
    const existingReservationActive = existingStatus === 'reserved' &&
      existingExpiry &&
      existingExpiry.toDate().getTime() > now.getTime();

    if (existingStatus === 'claimed') {
      result = {
        reserved: false,
        reason: 'already_claimed',
        claimedCount,
        reservedCount,
        maxClaims,
      };
      return;
    }

    if (existingReservationActive) {
      result = {
        reserved: true,
        reason: 'already_reserved',
        claimedCount,
        reservedCount,
        maxClaims,
      };
      return;
    }

    if (!active || claimedCount + reservedCount >= maxClaims) {
      result = {
        reserved: false,
        reason: 'cap_reached',
        claimedCount,
        reservedCount,
        maxClaims,
      };
      return;
    }

    const reservationExpiresAt = reservationExpiryFrom(now);
    transaction.set(grantRef, {
      uid,
      campaignId,
      status: 'reserved',
      offerId: FOUNDER_OFFER_ID,
      productId: ANNUAL_SUBSCRIPTION_ID,
      reservedAt: admin.firestore.FieldValue.serverTimestamp(),
      reservationExpiresAt: admin.firestore.Timestamp.fromDate(reservationExpiresAt),
    }, {merge: true});
    transaction.set(configRef, {
      active: true,
      campaignId,
      claimedCount,
      reservedCount: reservedCount + 1,
      maxClaims,
      offerId: FOUNDER_OFFER_ID,
      productId: ANNUAL_SUBSCRIPTION_ID,
      updatedAt: admin.firestore.FieldValue.serverTimestamp(),
    }, {merge: true});
    result = {
      reserved: true,
      reason: 'reserved',
      claimedCount,
      reservedCount: reservedCount + 1,
      maxClaims,
      reservationExpiresAt: reservationExpiresAt.getTime(),
    };
  });

  return result;
}

async function releaseLegacyPremiumFounderOffer({uid, campaignId}) {
  if (campaignId !== CAMPAIGN_ID) {
    return {released: false, reason: 'not_eligible'};
  }

  const firestore = admin.firestore();
  const grantRef = firestore.collection('legacy_premium_grants').doc(uid);
  const configRef = firestore.collection('app_config')
      .doc('legacy_premium_grant');
  let result = {released: false, reason: 'not_reserved'};

  await firestore.runTransaction(async (transaction) => {
    const [grantDoc, configDoc] = await Promise.all([
      transaction.get(grantRef),
      transaction.get(configRef),
    ]);
    if (!grantDoc.exists || grantDoc.get('status') !== 'reserved') {
      return;
    }
    const reservedCount = numericConfig(configDoc, 'reservedCount', 0);
    transaction.set(grantRef, {
      status: 'released',
      releasedAt: admin.firestore.FieldValue.serverTimestamp(),
    }, {merge: true});
    transaction.set(configRef, {
      reservedCount: Math.max(0, reservedCount - 1),
      updatedAt: admin.firestore.FieldValue.serverTimestamp(),
    }, {merge: true});
    result = {released: true, reason: 'released'};
  });

  return result;
}

async function recordLegacyPremiumFounderPurchase({
  uid,
  purchaseInfo,
  subscriptionId,
}) {
  if (!isFounderOfferPurchase({purchaseInfo, subscriptionId})) {
    return {claimed: false, reason: 'not_founder_offer'};
  }

  const firestore = admin.firestore();
  const grantRef = firestore.collection('legacy_premium_grants').doc(uid);
  const configRef = firestore.collection('app_config')
      .doc('legacy_premium_grant');
  let result = {claimed: false, reason: 'cap_reached'};

  await firestore.runTransaction(async (transaction) => {
    const [grantDoc, configDoc] = await Promise.all([
      transaction.get(grantRef),
      transaction.get(configRef),
    ]);
    const claimedCount = numericConfig(configDoc, 'claimedCount', 0);
    const reservedCount = numericConfig(configDoc, 'reservedCount', 0);
    const maxClaims = numericConfig(configDoc, 'maxClaims', MAX_GRANTS);
    const existingStatus = grantDoc.exists ? grantDoc.get('status') : null;

    if (existingStatus === 'claimed') {
      result = {
        claimed: true,
        reason: 'already_claimed',
        claimedCount,
        reservedCount,
        maxClaims,
      };
      return;
    }

    if (existingStatus !== 'reserved' && claimedCount >= maxClaims) {
      result = {
        claimed: false,
        reason: 'cap_reached',
        claimedCount,
        reservedCount,
        maxClaims,
      };
      return;
    }

    const nextClaimedCount = claimedCount + 1;
    const nextReservedCount = existingStatus === 'reserved' ?
      Math.max(0, reservedCount - 1) :
      reservedCount;
    transaction.set(grantRef, {
      uid,
      campaignId: CAMPAIGN_ID,
      status: 'claimed',
      offerId: FOUNDER_OFFER_ID,
      productId: ANNUAL_SUBSCRIPTION_ID,
      claimedAt: admin.firestore.FieldValue.serverTimestamp(),
      latestOrderId: purchaseInfo.latestOrderId || null,
    }, {merge: true});
    transaction.set(configRef, {
      active: nextClaimedCount < maxClaims,
      campaignId: CAMPAIGN_ID,
      claimedCount: nextClaimedCount,
      reservedCount: nextReservedCount,
      maxClaims,
      offerId: FOUNDER_OFFER_ID,
      productId: ANNUAL_SUBSCRIPTION_ID,
      updatedAt: admin.firestore.FieldValue.serverTimestamp(),
    }, {merge: true});
    result = {
      claimed: true,
      reason: 'claimed',
      claimedCount: nextClaimedCount,
      reservedCount: nextReservedCount,
      maxClaims,
    };
  });

  return result;
}

module.exports = {
  ANNUAL_SUBSCRIPTION_ID,
  CAMPAIGN_ID,
  FOUNDER_OFFER_ID,
  FOUNDER_OFFER_TAG,
  MAX_GRANTS,
  RESERVATION_MINUTES,
  isFounderOfferPurchase,
  recordLegacyPremiumFounderPurchase,
  releaseLegacyPremiumFounderOffer,
  reserveLegacyPremiumFounderOffer,
};
