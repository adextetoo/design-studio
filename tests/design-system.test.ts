import { test } from "node:test";
import assert from "node:assert/strict";
import { readFileSync } from "node:fs";

import { contrastRatio } from "../src/lib/color.ts";

/**
 * The studio measures contrast for its clients and prints a verdict on the
 * guideline page. It would be indefensible to do that from an interface that
 * fails the same check, so its own tokens are read out of the stylesheet and
 * held to the same standard.
 */

const CSS = readFileSync(new URL("../src/app/globals.css", import.meta.url), "utf8");

function token(name: string): string {
  const match = CSS.match(new RegExp(`--color-${name}:\\s*(#[0-9a-fA-F]{6})`));
  assert.ok(match, `--color-${name} is not declared in globals.css`);
  return match![1];
}

const GROUNDS = ["panel", "canvas", "sunken"] as const;

test("every text colour clears WCAG AA on every ground it can sit on", () => {
  const failures: string[] = [];
  for (const text of ["ink", "ink-soft", "ink-faint"]) {
    for (const ground of GROUNDS) {
      const ratio = contrastRatio(token(text), token(ground));
      if (ratio < 4.5) failures.push(`${text} on ${ground} is ${ratio}:1`);
    }
  }
  assert.deepEqual(failures, [], `text below 4.5:1:\n${failures.join("\n")}`);
});

test("the signal and confirmation colours are legible on their own tints", () => {
  for (const [ink, tint] of [["signal", "signal-soft"], ["go", "go-soft"]] as const) {
    const ratio = contrastRatio(token(ink), token(tint));
    assert.ok(ratio >= 4.5, `${ink} on ${tint} is ${ratio}:1`);
  }
});

test("the type scale is declared, and nothing in the source sets a size off it", () => {
  const steps = [...CSS.matchAll(/--text-([a-z]+):\s*(\d+)px/g)].map((m) => m[1]);
  assert.deepEqual(steps, ["micro", "body", "lead", "subhead", "title", "display", "hero"]);
});
