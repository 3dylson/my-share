const test = require('node:test');
const assert = require('node:assert/strict');
const {
  acknowledgeSubscriptionIfNeeded,
  subscriptionAlreadyAcknowledged,
  subscriptionNeedsAcknowledgement,
  subscriptionProductIdForAcknowledgement,
} = require('../src/playBillingApi');

function purchaseInfo(acknowledgementState, productId = 'myshare_annual') {
  return {
    acknowledgementState,
    lineItems: [
      {
        productId,
      },
    ],
  };
}

test('resolves subscription product id from line item first', () => {
  assert.equal(
      subscriptionProductIdForAcknowledgement(
          purchaseInfo('ACKNOWLEDGEMENT_STATE_PENDING'),
          'fallback_monthly',
      ),
      'myshare_annual',
  );
});

test('detects pending and acknowledged subscription states', () => {
  assert.equal(
      subscriptionNeedsAcknowledgement(
          purchaseInfo('ACKNOWLEDGEMENT_STATE_PENDING'),
      ),
      true,
  );
  assert.equal(
      subscriptionAlreadyAcknowledged(
          purchaseInfo('ACKNOWLEDGEMENT_STATE_ACKNOWLEDGED'),
      ),
      true,
  );
});

test('acknowledges pending subscription with Android Publisher', async () => {
  const calls = [];
  const androidPublisher = {
    purchases: {
      subscriptions: {
        acknowledge: async (params) => calls.push(params),
      },
    },
  };

  const result = await acknowledgeSubscriptionIfNeeded({
    purchaseToken: 'purchase-token',
    subscriptionId: 'fallback_monthly',
    purchaseInfo: purchaseInfo('ACKNOWLEDGEMENT_STATE_PENDING'),
    androidPublisher,
  });

  assert.equal(result.status, 'acknowledged');
  assert.equal(
      result.acknowledgementState,
      'ACKNOWLEDGEMENT_STATE_ACKNOWLEDGED',
  );
  assert.equal(calls.length, 1);
  assert.deepEqual(calls[0], {
    packageName: 'pt.ms.myshare',
    subscriptionId: 'myshare_annual',
    token: 'purchase-token',
    requestBody: {},
  });
});

test('skips subscription that is already acknowledged', async () => {
  const androidPublisher = {
    purchases: {
      subscriptions: {
        acknowledge: async () => {
          throw new Error('acknowledge should not be called');
        },
      },
    },
  };

  const result = await acknowledgeSubscriptionIfNeeded({
    purchaseToken: 'purchase-token',
    subscriptionId: 'fallback_monthly',
    purchaseInfo: purchaseInfo('ACKNOWLEDGEMENT_STATE_ACKNOWLEDGED'),
    androidPublisher,
  });

  assert.equal(result.status, 'already_acknowledged');
});

test('returns recoverable status when subscription id is missing', async () => {
  const result = await acknowledgeSubscriptionIfNeeded({
    purchaseToken: 'purchase-token',
    subscriptionId: null,
    purchaseInfo: {acknowledgementState: 'ACKNOWLEDGEMENT_STATE_PENDING'},
    androidPublisher: {},
  });

  assert.equal(result.status, 'missing_subscription_id');
  assert.equal(typeof result.errorMessage, 'string');
});
