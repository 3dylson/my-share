const functions = require('firebase-functions');
const admin = require('firebase-admin');

async function requireAuthenticatedUid(data, context) {
  if (context.auth && context.auth.uid) {
    return context.auth.uid;
  }

  const firebaseIdToken = data.firebaseIdToken;
  if (typeof firebaseIdToken !== 'string' || firebaseIdToken.length === 0) {
    throw new functions.https.HttpsError(
        'unauthenticated',
        'User must be authenticated to verify a subscription.',
    );
  }

  try {
    const decodedToken = await admin.auth().verifyIdToken(firebaseIdToken);
    return decodedToken.uid;
  } catch (error) {
    console.error('Error verifying Firebase ID token:', error);
    throw new functions.https.HttpsError(
        'unauthenticated',
        'User must be authenticated to verify a subscription.',
    );
  }
}

module.exports = {
  requireAuthenticatedUid,
};
