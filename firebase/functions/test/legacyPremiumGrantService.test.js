const test = require('node:test');
const assert = require('node:assert/strict');
const {
  isFounderOfferPurchase,
} = require('../src/legacyPremiumGrantService');

test('detects founder annual offer purchases by offer id', () => {
  const purchaseInfo = {
    lineItems: [
      {
        productId: 'myshare_annual',
        offerDetails: {
          basePlanId: 'annual',
          offerId: 'founder-free-1y',
          offerTags: [],
        },
      },
    ],
  };

  assert.equal(
      isFounderOfferPurchase({purchaseInfo, subscriptionId: 'myshare_annual'}),
      true,
  );
});

test('does not treat normal annual purchases as founder offers', () => {
  const purchaseInfo = {
    lineItems: [
      {
        productId: 'myshare_annual',
        offerDetails: {
          basePlanId: 'annual',
          offerId: 'trial-7d',
          offerTags: [],
        },
      },
    ],
  };

  assert.equal(
      isFounderOfferPurchase({purchaseInfo, subscriptionId: 'myshare_annual'}),
      false,
  );
});
