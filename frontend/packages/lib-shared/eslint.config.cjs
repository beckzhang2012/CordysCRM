const { defineConfig } = require('eslint/config');
const { fixupConfigRules } = require('@eslint/compat');
const js = require('@eslint/js');
const { FlatCompat } = require('@eslint/eslintrc');
const path = require('path');
const simpleImportSort = require('eslint-plugin-simple-import-sort');

const compat = new FlatCompat({
  baseDirectory: __dirname,
  recommendedConfig: js.configs.recommended,
  allConfig: js.configs.all,
});

module.exports = defineConfig([
  {
    ignores: [
      'node_modules/',
      'dist/',
      '**/*.json',
      'postcss.config.js',
      '**/*.md',
    ],
  },
  ...fixupConfigRules(
    compat.extends(
      'airbnb-base',
      'plugin:@typescript-eslint/recommended',
      'plugin:import/recommended',
      'plugin:import/typescript',
      'plugin:prettier/recommended',
      './.eslintrc-auto-import.json'
    )
  ),
  {
    files: ['**/*.ts', '**/*.js'],
    plugins: {
      'simple-import-sort': simpleImportSort,
    },
    languageOptions: {
      parser: require('@typescript-eslint/parser'),
      parserOptions: {
        ecmaVersion: 2020,
        sourceType: 'module',
        project: path.resolve(__dirname, './tsconfig.json'),
      },
      globals: {
        ...require('globals').node,
        ...require('globals').browser,
        NodeJS: 'readonly',
      },
    },
    settings: {
      'import/resolver': {
        typescript: {
          project: path.resolve(__dirname, './tsconfig.json'),
        },
      },
    },
    rules: {
      'prettier/prettier': 1,
      '@typescript-eslint/ban-ts-comment': 0,
      '@typescript-eslint/no-unused-vars': 1,
      '@typescript-eslint/no-empty-function': 1,
      '@typescript-eslint/no-explicit-any': 0,
      '@typescript-eslint/no-duplicate-enum-values': 0,
      'consistent-return': 'off',
      'import/no-unresolved': [
        'error',
        { ignore: ['^@lib/shared', '^@web/'] },
      ],
      'import/extensions': [2, 'ignorePackages', { js: 'never', jsx: 'never', ts: 'never', tsx: 'never' }],
      'no-debugger': 2,
      'no-param-reassign': 0,
      'prefer-regex-literals': 0,
      'import/no-extraneous-dependencies': 0,
      'import/no-cycle': 'off',
      'import/order': 'off',
      'class-methods-use-this': 'off',
      'global-require': 0,
      'no-plusplus': 'off',
      'no-underscore-dangle': 'off',
      'simple-import-sort/exports': 'error',
      'no-case-declarations': 'off',
      'simple-import-sort/imports': [
        'error',
        {
          groups: [
            ['^vue$', '^vue-router$', '^vue-i18n$', '^pinia$', '^@vueuse/core$', '^naive-ui$', '^lodash-es$', '^axios$', '^dayjs$', '^jsencrypt$', '^echarts$', '^localforage$'],
            ['.*/assets/.*', '^@/assets$'],
            ['^@/components/pure/.*', '^@/components/business/.*', '.*\\.vue$'],
            ['^@/api($|/.*)', '^@/config($|/.*)', '^@/directive($|/.*)', '^@/hooks($|/.*)', '^@/locale($|/.*)', '^@/router($|/.*)', '^@/store($|/.*)', '^@/utils($|/.*)'],
            ['^@/models($|/.*)', '^@/enums($|/.*)'],
            ['^type'],
          ],
        },
      ],
    },
  },
  {
    files: ['**/enums/**/*.ts'],
    rules: { 'no-shadow': 'off' },
  },
]);
