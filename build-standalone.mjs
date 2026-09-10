/**
 * Builds the studio as one self-contained HTML file.
 *
 * Same source as the Next app — `next/link` and `next/navigation` are aliased
 * to hash-router shims, Tailwind is compiled ahead of time, and both are
 * inlined so the page has no runtime dependency except the font stylesheet.
 */
import { build } from "esbuild";
import { execFileSync } from "node:child_process";
import { readFileSync, writeFileSync, mkdirSync, rmSync } from "node:fs";

const TMP = "build-tmp";
rmSync(TMP, { recursive: true, force: true });
mkdirSync(TMP, { recursive: true });

// 1. Tailwind, compiled against the real source tree.
execFileSync(
  "npx",
  ["@tailwindcss/cli", "-i", "src/app/globals.css", "-o", `${TMP}/studio.css`, "--minify"],
  { stdio: "inherit" },
);

// 2. The app, bundled with React.
await build({
  entryPoints: ["src/standalone/main.tsx"],
  bundle: true,
  minify: true,
  format: "iife",
  target: ["es2022"],
  jsx: "automatic",
  outfile: `${TMP}/studio.js`,
  legalComments: "none",
  define: { "process.env.NODE_ENV": '"production"' },
  alias: {
    "next/link": "./src/standalone/next-link.tsx",
    "next/navigation": "./src/standalone/next-navigation.tsx",
  },
  loader: { ".css": "empty" },
  logLevel: "info",
});

const css = readFileSync(`${TMP}/studio.css`, "utf8");
const js = readFileSync(`${TMP}/studio.js`, "utf8");

/*
 * The brand-specimen typefaces. The typography module names real faces and a
 * specimen is the deliverable, so they are loaded rather than left to fall back
 * to whatever the viewer happens to have installed.
 */
const FONTS = [
  "Manrope:wght@200;400;500;700",
  "Space+Grotesk:wght@300;400;500;700",
  "Poppins:wght@300;400;500;600",
  "Inter:wght@300;400;500;600;700",
  "Work+Sans:wght@400;500;600",
  "Source+Sans+3:wght@400;600;700",
  "Lato:wght@300;400;700",
  "Source+Serif+4:wght@400;600;700",
  "Newsreader:wght@400;500;600",
  "Lora:wght@400;500",
  "Playfair+Display:wght@400;500;700",
  "Bodoni+Moda:wght@400;500;700",
  "Jost:wght@300;400",
  "Roboto+Slab:wght@400;500;700",
  "Zilla+Slab:wght@400;500;700",
  "IBM+Plex+Mono:wght@400;500;600",
  "JetBrains+Mono:wght@400;500;700",
]
  .map((f) => `family=${f}`)
  .join("&");

const html = `<title>Design Studio</title>
<meta name="description" content="Run a brand engagement end to end and export Brand.md and Design.md.">
<link rel="preconnect" href="https://fonts.googleapis.com">
<link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
<link rel="stylesheet" href="https://fonts.googleapis.com/css2?${FONTS}&display=swap">
<style>
${css}
/*
 * The studio chrome commits to one light world on purpose: it stays quiet so
 * the client's brand is the only colourful thing on screen. Background and
 * colour are painted explicitly so the page holds on any host ground.
 */
html, body { background: var(--color-canvas); color: var(--color-ink); }
#root { min-height: 100vh; }
</style>
<div id="root"></div>
<script>${js}</script>
`;

writeFileSync("dist/studio.html", html);
console.log(`\nstudio.html — ${(Buffer.byteLength(html) / 1024).toFixed(0)} KB`);
