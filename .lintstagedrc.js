module.exports = {
  '{,src/**/,webpack/}*.{html,css,scss,md,yml,json,java}': [
    'prettier --config .prettierrc --ignore-path .prettierignore --write',
  ],
  '{,src/**/,webpack/}*.{js,jsx,ts,tsx}': [
    'prettier --config .prettierrc --ignore-path .prettierignore --write',
    'eslint --config .eslintrc.json --ignore-path .eslintignore',
  ],
};
