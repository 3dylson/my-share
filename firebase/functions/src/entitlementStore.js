const admin = require('firebase-admin');
const {MAX_REVALIDATIONS_PER_RUN} = require('./config');
const {obfuscatedAccountIdForUid} = require('./obfuscatedAccountId');
const {purchaseTokenHash} = require('./purchaseTokenHasher');

function userEntitlementFields(snapshot) {
  return {
    isPro: snapshot.isPro,
    entitlementState: snapshot.entitlementState,
    subscriptionState: snapshot.subscriptionState,
    proExpiry: snapshot.proExpiry,
    lastVerifiedAt: admin.firestore.FieldValue.serverTimestamp(),
  };
}

function tokenIndexFields(uid, purchaseToken, snapshot) {
  const obfuscatedAccountId = obfuscatedAccountIdForUid(uid);
  const fields = {
    uid,
    obfuscatedAccountId,
    purchaseToken,
    purchaseTokenHash: snapshot.purchaseTokenHash,
    subscriptionId: snapshot.subscriptionId,
    linkedPurchaseTokenHash: snapshot.linkedPurchaseTokenHash || null,
    isPro: snapshot.isPro,
    entitlementState: snapshot.entitlementState,
    subscriptionState: snapshot.subscriptionState,
    proExpiry: snapshot.proExpiry,
    expiryTimeMillis: snapshot.expiryTimeMillis,
    latestOrderId: snapshot.latestOrderId || null,
    regionCode: snapshot.regionCode || null,
    acknowledgementState: snapshot.acknowledgementState || null,
    serverAcknowledgementStatus:
        snapshot.serverAcknowledgementStatus || null,
    verificationSource: snapshot.verificationSource,
    notificationType: snapshot.notificationType || null,
    updatedAt: admin.firestore.FieldValue.serverTimestamp(),
  };
  if (snapshot.serverAcknowledgedAt) {
    fields.serverAcknowledgedAt = snapshot.serverAcknowledgedAt;
  }
  if (snapshot.serverAcknowledgementError !== undefined) {
    fields.serverAcknowledgementError = snapshot.serverAcknowledgementError;
  }
  if (snapshot.serverAcknowledgementFailedAt) {
    fields.serverAcknowledgementFailedAt =
        snapshot.serverAcknowledgementFailedAt;
  }
  return fields;
}

function accountLinkFields(uid) {
  return {
    uid,
    obfuscatedAccountId: obfuscatedAccountIdForUid(uid),
    updatedAt: admin.firestore.FieldValue.serverTimestamp(),
  };
}

async function writeEntitlementSnapshot(uid, purchaseToken, snapshot) {
  const firestore = admin.firestore();
  const userRef = firestore.collection('users').doc(uid);
  const entitlementRef = userRef.collection('entitlements').doc('current');
  const tokenRef = firestore
      .collection('billing_purchase_tokens')
      .doc(snapshot.purchaseTokenHash);
  const accountLinkRef = firestore
      .collection('billing_account_links')
      .doc(obfuscatedAccountIdForUid(uid));

  const batch = firestore.batch();
  batch.set(userRef, userEntitlementFields(snapshot), {merge: true});
  batch.set(entitlementRef, snapshot, {merge: true});
  batch.set(
      tokenRef,
      tokenIndexFields(uid, purchaseToken, snapshot),
      {merge: true},
  );
  batch.set(accountLinkRef, accountLinkFields(uid), {merge: true});
  await batch.commit();
}

async function uidFromIndexedToken(purchaseToken) {
  const tokenHash = purchaseTokenHash(purchaseToken);
  const tokenSnapshot = await admin.firestore()
      .collection('billing_purchase_tokens')
      .doc(tokenHash)
      .get();
  return tokenSnapshot.exists ? tokenSnapshot.get('uid') : null;
}

async function uidForPurchaseToken(purchaseToken) {
  return uidFromIndexedToken(purchaseToken);
}

async function uidForObfuscatedAccountId(obfuscatedAccountId) {
  if (!obfuscatedAccountId) return null;
  const accountSnapshot = await admin.firestore()
      .collection('billing_account_links')
      .doc(obfuscatedAccountId)
      .get();
  return accountSnapshot.exists ? accountSnapshot.get('uid') : null;
}

async function writeVoidedEntitlement(uid, purchaseToken, notification) {
  const tokenHash = purchaseTokenHash(purchaseToken);
  const obfuscatedAccountId = obfuscatedAccountIdForUid(uid);
  const subscriptionId = notification.subscriptionId || null;
  const notificationType = notification.notificationType || null;
  const firestore = admin.firestore();
  const userRef = firestore.collection('users').doc(uid);
  const entitlementRef = userRef.collection('entitlements').doc('current');
  const tokenRef = firestore.collection('billing_purchase_tokens').doc(tokenHash);
  const serverTime = admin.firestore.FieldValue.serverTimestamp();
  const userFields = {
    isPro: false,
    entitlementState: 'FREE',
    subscriptionState: 'VOIDED_PURCHASE',
    proExpiry: null,
    lastVerifiedAt: serverTime,
  };
  const entitlementFields = {
    ...userFields,
    subscriptionId,
    purchaseToken: admin.firestore.FieldValue.delete(),
    purchaseTokenHash: tokenHash,
    linkedPurchaseToken: admin.firestore.FieldValue.delete(),
    linkedPurchaseTokenHash: null,
    expiryTimeMillis: null,
    paymentState: null,
    verificationSource: 'rtdn_voided',
    notificationType,
    latestOrderId: notification.orderId || null,
    regionCode: null,
  };
  const tokenFields = {
    uid,
    obfuscatedAccountId,
    purchaseToken,
    purchaseTokenHash: tokenHash,
    subscriptionId,
    isPro: false,
    entitlementState: 'FREE',
    subscriptionState: 'VOIDED_PURCHASE',
    proExpiry: null,
    expiryTimeMillis: null,
    latestOrderId: notification.orderId || null,
    verificationSource: 'rtdn_voided',
    notificationType,
    voidedAt: serverTime,
    updatedAt: serverTime,
  };
  const accountLinkRef = firestore
      .collection('billing_account_links')
      .doc(obfuscatedAccountId);

  const batch = firestore.batch();
  batch.set(userRef, userFields, {merge: true});
  batch.set(entitlementRef, entitlementFields, {merge: true});
  batch.set(tokenRef, tokenFields, {merge: true});
  batch.set(accountLinkRef, accountLinkFields(uid), {merge: true});
  await batch.commit();
}

function addIndexedRevalidationRecord(records, seenTokenHashes, doc) {
  const tokenData = doc.data();
  if (!tokenData.purchaseToken || !tokenData.uid) {
    records.push({
      tokenHash: doc.id,
      source: 'token_index',
    });
    return;
  }

  seenTokenHashes.add(doc.id);
  records.push({
    uid: tokenData.uid,
    purchaseToken: tokenData.purchaseToken,
    subscriptionId: tokenData.subscriptionId || null,
    tokenHash: doc.id,
    source: 'token_index',
  });
}

async function activeTokenRecordsForRevalidation() {
  const firestore = admin.firestore();
  const records = [];
  const seenTokenHashes = new Set();
  const indexedTokens = await firestore
      .collection('billing_purchase_tokens')
      .where('isPro', '==', true)
      .limit(MAX_REVALIDATIONS_PER_RUN)
      .get();

  indexedTokens.docs.forEach((doc) => {
    addIndexedRevalidationRecord(records, seenTokenHashes, doc);
  });

  return records;
}

module.exports = {
  activeTokenRecordsForRevalidation,
  uidForObfuscatedAccountId,
  uidForPurchaseToken,
  writeEntitlementSnapshot,
  writeVoidedEntitlement,
};
