// svelte.config.js
import adapter from '@sveltejs/adapter-cloudflare';
import { vitePreprocess } from '@sveltejs/kit/vite';

const config = {
  preprocess: vitePreprocess(),
  kit: {
    adapter: adapter(),
    // optional: fallback for SPA routing
    // paths: { base: '' },
  }
};

export default config;
