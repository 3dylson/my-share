module.exports = {
  root: true,
  env: {
    es2022: true,
    node: true,
  },
  extends: ['google'],
  parserOptions: {
    ecmaVersion: 2022,
  },
  rules: {
    'require-jsdoc': 'off',
    'valid-jsdoc': 'off',
    'max-len': ['error', {'code': 120, 'ignoreUrls': true}],
  },
};
