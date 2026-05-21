const crypto = require('crypto');

function obfuscatedAccountIdForUid(uid) {
  return crypto.createHash('sha256').update(uid).digest('hex');
}

function externalAccountIdFromPurchaseInfo(purchaseInfo) {
  const identifiers = purchaseInfo.externalAccountIdentifiers || {};
  const expiredIdentifiers =
      purchaseInfo.outOfAppPurchaseContext?.expiredExternalAccountIdentifiers ||
      {};
  return identifiers.obfuscatedExternalAccountId ||
      identifiers.externalAccountId ||
      identifiers.obfuscatedAccountId ||
      expiredIdentifiers.obfuscatedExternalAccountId ||
      expiredIdentifiers.externalAccountId ||
      expiredIdentifiers.obfuscatedAccountId ||
      null;
}

module.exports = {
  externalAccountIdFromPurchaseInfo,
  obfuscatedAccountIdForUid,
};
