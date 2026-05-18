const test = require('node:test');
const assert = require('node:assert/strict');
const {buildEntitlementSnapshot} = require('../src/subscriptionEntitlementSnapshot');

function purchaseInfo(subscriptionState, expiryTime, productId = 'myshare_annual') {
  return {
    subscriptionState,
    latestOrderId: 'GPA.1234-5678',
    regionCode: 'PT',
    linkedPurchaseToken: 'linked-token',
    lineItems: [
      {
        productId,
        expiryTime,
      },
    ],
  };
}

test('maps active subscription with future expiry to pro', () => {
  const snapshot = buildEntitlementSnapshot({
    purchaseInfo: purchaseInfo(
        'SUBSCRIPTION_STATE_ACTIVE',
        '2099-01-01T00:00:00Z',
    ),
    purchaseToken: 'raw-token',
    subscriptionId: 'fallback_product',
    verificationSource: 'callable',
    notificationType: null,
  });

  assert.equal(snapshot.isPro, true);
  assert.equal(snapshot.entitlementState, 'PRO');
  assert.equal(snapshot.subscriptionId, 'myshare_annual');
  assert.equal(snapshot.purchaseTokenHash.length, 64);
  assert.notEqual(snapshot.purchaseTokenHash, 'raw-token');
  assert.equal(snapshot.linkedPurchaseTokenHash.length, 64);
});

test('maps grace-period subscription with future expiry to grace period', () => {
  const snapshot = buildEntitlementSnapshot({
    purchaseInfo: purchaseInfo(
        'SUBSCRIPTION_STATE_IN_GRACE_PERIOD',
        '2099-01-01T00:00:00Z',
        'myshare_monthly',
    ),
    purchaseToken: 'raw-token',
    subscriptionId: 'fallback_product',
    verificationSource: 'rtdn',
    notificationType: 6,
  });

  assert.equal(snapshot.isPro, true);
  assert.equal(snapshot.entitlementState, 'GRACE_PERIOD');
  assert.equal(snapshot.subscriptionId, 'myshare_monthly');
  assert.equal(snapshot.notificationType, 6);
});

test('maps expired subscription to free even when state is canceled', () => {
  const snapshot = buildEntitlementSnapshot({
    purchaseInfo: purchaseInfo(
        'SUBSCRIPTION_STATE_CANCELED',
        '2000-01-01T00:00:00Z',
    ),
    purchaseToken: 'raw-token',
    subscriptionId: 'fallback_product',
    verificationSource: 'scheduled',
    notificationType: null,
  });

  assert.equal(snapshot.isPro, false);
  assert.equal(snapshot.entitlementState, 'FREE');
  assert.equal(snapshot.proExpiry, null);
});

test('stores server acknowledgement result on entitlement snapshot', () => {
  const snapshot = buildEntitlementSnapshot({
    purchaseInfo: {
      ...purchaseInfo(
          'SUBSCRIPTION_STATE_ACTIVE',
          '2099-01-01T00:00:00Z',
      ),
      acknowledgementState: 'ACKNOWLEDGEMENT_STATE_PENDING',
    },
    purchaseToken: 'raw-token',
    subscriptionId: 'fallback_product',
    verificationSource: 'callable',
    notificationType: null,
    acknowledgementResult: {
      status: 'acknowledged',
      acknowledgementState: 'ACKNOWLEDGEMENT_STATE_ACKNOWLEDGED',
    },
  });

  assert.equal(
      snapshot.acknowledgementState,
      'ACKNOWLEDGEMENT_STATE_ACKNOWLEDGED',
  );
  assert.equal(snapshot.serverAcknowledgementStatus, 'acknowledged');
  assert.ok(snapshot.serverAcknowledgedAt);
  assert.equal(snapshot.serverAcknowledgementError, null);
});
