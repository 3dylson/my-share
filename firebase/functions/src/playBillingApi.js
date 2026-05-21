const {google} = require('googleapis');
const {ANDROID_PUBLISHER_SCOPE, PACKAGE_NAME} = require('./config');

function androidPublisherClient() {
  const auth = new google.auth.GoogleAuth({
    scopes: [ANDROID_PUBLISHER_SCOPE],
  });
  return google.androidpublisher({
    version: 'v3',
    auth,
  });
}

async function fetchSubscriptionPurchase(purchaseToken, androidPublisher) {
  const client = androidPublisher || androidPublisherClient();
  const response = await client.purchases.subscriptionsv2.get({
    packageName: PACKAGE_NAME,
    token: purchaseToken,
  });
  return response.data;
}

function subscriptionProductIdForAcknowledgement(
    purchaseInfo,
    fallbackSubscriptionId,
) {
  const lineItems = Array.isArray(purchaseInfo.lineItems) ?
      purchaseInfo.lineItems :
      [];
  const productId = lineItems
      .map((lineItem) => lineItem.productId)
      .find((value) => typeof value === 'string' && value.length > 0);
  return productId || fallbackSubscriptionId || null;
}

function subscriptionNeedsAcknowledgement(purchaseInfo) {
  const acknowledgementState = purchaseInfo.acknowledgementState;
  return acknowledgementState === 0 ||
      acknowledgementState === '0' ||
      acknowledgementState === 'ACKNOWLEDGEMENT_STATE_PENDING' ||
      acknowledgementState === 'SUBSCRIPTION_ACKNOWLEDGEMENT_STATE_PENDING';
}

function subscriptionAlreadyAcknowledged(purchaseInfo) {
  const acknowledgementState = purchaseInfo.acknowledgementState;
  return acknowledgementState === 1 ||
      acknowledgementState === '1' ||
      acknowledgementState === 'ACKNOWLEDGEMENT_STATE_ACKNOWLEDGED' ||
      acknowledgementState ===
          'SUBSCRIPTION_ACKNOWLEDGEMENT_STATE_ACKNOWLEDGED';
}

function acknowledgementErrorMessage(error) {
  return error?.message || error?.response?.data?.error?.message ||
      'Unknown acknowledgement error';
}

async function acknowledgeSubscriptionIfNeeded({
  purchaseToken,
  subscriptionId,
  purchaseInfo,
  androidPublisher,
}) {
  const acknowledgementState = purchaseInfo.acknowledgementState ?? null;
  if (subscriptionAlreadyAcknowledged(purchaseInfo)) {
    return {
      status: 'already_acknowledged',
      acknowledgementState,
    };
  }

  if (!subscriptionNeedsAcknowledgement(purchaseInfo)) {
    return {
      status: 'not_required',
      acknowledgementState,
    };
  }

  const resolvedSubscriptionId = subscriptionProductIdForAcknowledgement(
      purchaseInfo,
      subscriptionId,
  );
  if (!resolvedSubscriptionId) {
    return {
      status: 'missing_subscription_id',
      acknowledgementState,
      errorMessage: 'Subscription product id was not present.',
    };
  }

  try {
    const client = androidPublisher || androidPublisherClient();
    await client.purchases.subscriptions.acknowledge({
      packageName: PACKAGE_NAME,
      subscriptionId: resolvedSubscriptionId,
      token: purchaseToken,
      requestBody: {},
    });
    return {
      status: 'acknowledged',
      acknowledgementState: 'ACKNOWLEDGEMENT_STATE_ACKNOWLEDGED',
    };
  } catch (error) {
    console.error('Server subscription acknowledgement failed', {
      subscriptionId: resolvedSubscriptionId,
      error,
    });
    return {
      status: 'failed',
      acknowledgementState,
      errorMessage: acknowledgementErrorMessage(error),
    };
  }
}

module.exports = {
  acknowledgeSubscriptionIfNeeded,
  androidPublisherClient,
  fetchSubscriptionPurchase,
  subscriptionAlreadyAcknowledged,
  subscriptionNeedsAcknowledgement,
  subscriptionProductIdForAcknowledgement,
};
