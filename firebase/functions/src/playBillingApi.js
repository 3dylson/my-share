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

module.exports = {
  androidPublisherClient,
  fetchSubscriptionPurchase,
};
