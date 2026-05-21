const crypto = require('crypto');

function purchaseTokenHash(purchaseToken) {
  return crypto.createHash('sha256').update(purchaseToken).digest('hex');
}

module.exports = {
  purchaseTokenHash,
};
