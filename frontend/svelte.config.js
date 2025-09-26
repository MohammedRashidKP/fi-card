import adapter from '@sveltejs/adapter-static';
import { vitePreprocess } from '@sveltejs/kit/vite';

const config = {
  preprocess: vitePreprocess(),

  kit: {
    adapter: adapter({
      pages: 'build',
      assets: 'build',
      fallback: 'app.html' // needed for SPA routing
    }),
    paths: {
      base: '', // leave empty unless deploying under subpath
    }
  }
};

export default config;
