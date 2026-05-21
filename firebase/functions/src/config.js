const PACKAGE_NAME = 'pt.ms.myshare';
const PLAY_SERVICE_ACCOUNT = '564550726509-compute@developer.gserviceaccount.com';
const ANDROID_PUBLISHER_SCOPE =
    'https://www.googleapis.com/auth/androidpublisher';
const PLAY_BILLING_RTDN_TOPIC =
    process.env.GOOGLE_PLAY_RTDN_TOPIC || 'play-billing-rtdn';
const MAX_REVALIDATIONS_PER_RUN = 500;

module.exports = {
  ANDROID_PUBLISHER_SCOPE,
  MAX_REVALIDATIONS_PER_RUN,
  PACKAGE_NAME,
  PLAY_BILLING_RTDN_TOPIC,
  PLAY_SERVICE_ACCOUNT,
};
