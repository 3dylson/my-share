const test = require('node:test');
const assert = require('node:assert/strict');
const {
  externalAccountIdFromPurchaseInfo,
  obfuscatedAccountIdForUid,
} = require('../src/obfuscatedAccountId');

test('obfuscated account id matches Android SHA-256 uid hashing', () => {
  assert.equal(
      obfuscatedAccountIdForUid('uid-123'),
      '23481502b4881b15d1cc3216f86b19af5373e9a3c4ec2e91edd9dd0f00a8b414',
  );
});

test('resolves current external account identifier first', () => {
  const accountId = externalAccountIdFromPurchaseInfo({
    externalAccountIdentifiers: {
      obfuscatedExternalAccountId: 'current-account',
    },
    outOfAppPurchaseContext: {
      expiredExternalAccountIdentifiers: {
        obfuscatedExternalAccountId: 'expired-account',
      },
    },
  });

  assert.equal(accountId, 'current-account');
});

test('falls back to expired external account identifier for out of app purchase', () => {
  const accountId = externalAccountIdFromPurchaseInfo({
    outOfAppPurchaseContext: {
      expiredExternalAccountIdentifiers: {
        obfuscatedExternalAccountId: 'expired-account',
      },
    },
  });

  assert.equal(accountId, 'expired-account');
});
