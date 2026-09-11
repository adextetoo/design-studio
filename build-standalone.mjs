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
import { createRequire } from "node:module";
import { fileURLToPath, pathToFileURL } from "node:url";

const TMP = "build-tmp";
rmSync(TMP, { recursive: true, force: true });
mkdirSync(TMP, { recursive: true });

// 1. Tailwind, compiled against the real source tree.
//
// The CLI's own JS entry point is run with this same Node binary rather than
// going through npx. On Windows the launchers are .cmd shims, and since the
// fix for CVE-2024-27980 Node refuses to spawn those without a shell, so
// `npx`/`tailwindcss` both fail there with EINVAL. Resolving the entry point
// keeps one code path on every platform and skips the shell entirely.
const require = createRequire(import.meta.url);
const tailwind = fileURLToPath(
  new URL("./dist/index.mjs", pathToFileURL(require.resolve("@tailwindcss/cli/package.json"))),
);
execFileSync(
  process.execPath,
  [tailwind, "-i", "src/app/globals.css", "-o", `${TMP}/studio.css`, "--minify"],
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
  // The chrome is set in the system face, which needs no webfont on Apple
  // hardware. Inter is the fallback everywhere else, loaded with the
  // optical-size axis so it tracks the way SF does across the scale.
  "Inter:opsz,wght@14..32,300..800",
  // Then every face the Typography deliverable can name.
  "Newsreader:wght@400;500;600",
  "Work+Sans:wght@400;500;600",
  "IBM+Plex+Mono:wght@400;500;600",
  "Manrope:wght@200;400;500;700",
  "Space+Grotesk:wght@300;400;500;700",
  "Poppins:wght@300;400;500;600",
  "Source+Sans+3:wght@400;600;700",
  "Lato:wght@300;400;700",
  "Source+Serif+4:wght@400;600;700",
  "Lora:wght@400;500",
  "Playfair+Display:wght@400;500;700",
  "Bodoni+Moda:wght@400;500;700",
  "Jost:wght@300;400",
  "Roboto+Slab:wght@400;500;700",
  "Zilla+Slab:wght@400;500;700",
  "JetBrains+Mono:wght@400;500;700",
]
  .map((f) => `family=${f}`)
  .join("&");

const head = `<title>Design Studio</title>
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
</style>`;

const mount = `<div id="root"></div>
<script>${js}</script>`;

/*
 * Two outputs from one bundle.
 *
 * `dist/studio.html` is a fragment: an artifact host supplies the document
 * around it, so it must not carry a doctype or a <head> of its own.
 *
 * `index.html` is the same payload as a complete document, for opening from
 * disk or serving from any static host. It is committed, so the repository
 * runs straight after a clone with no toolchain and no build step.
 */
const fragment = `${head}\n${mount}\n`;
const page = `<!doctype html>
<html lang="en">
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
${head}
</head>
<body>
${mount}
</body>
</html>
`;

mkdirSync("dist", { recursive: true });
writeFileSync("dist/studio.html", fragment);
writeFileSync("index.html", page);

const kb = (s) => `${(Buffer.byteLength(s) / 1024).toFixed(0)} KB`;
console.log(`\ndist/studio.html  ${kb(fragment)}  (artifact fragment)`);
console.log(`index.html        ${kb(page)}  (standalone, for local hosting)`);
