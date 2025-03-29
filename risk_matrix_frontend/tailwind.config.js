module.exports = {
  content: [
    './index.html',
    './src/**/*.{vue,js,ts,jsx,tsx}', // Adjust paths if necessary
  ],
  safelist: [
    {
      pattern: /^bg-(blue|green|orange|red)-600$/,
    },
    {
      pattern: /^hover:bg-(blue|green|orange|red)-700$/,
    },
  ],
  theme: {
    extend: {},
  },
  plugins: [],
};
