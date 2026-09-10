import type { FontClass } from "@/lib/types";

/**
 * Typeface classes.
 *
 * A brand guide names a licence, not a CSS file — so each entry carries the
 * real typeface name plus a stack that degrades to something with the same
 * skeleton if the licensed font is not installed. The scale, tracking and
 * hierarchy rules are the part that actually gets specified, and those are
 * exact regardless of what renders.
 */

export interface Typeface {
  name: string;
  stack: string;
  weights: string[];
  licence: string;
}

export interface FontClassSpec {
  id: FontClass;
  label: string;
  /** What choosing this class commits the brand to. */
  note: string;
  /** Ratio for the modular scale. Tighter classes take a smaller step. */
  scale: { ratio: number; label: string };
  /** Display tracking at large sizes, in em. Cold geometry needs negative tracking. */
  displayTracking: string;
  bodyTracking: string;
  headingCase: "sentence" | "title" | "upper";
  primaries: Typeface[];
  secondaries: Typeface[];
  rules: string[];
}

const SANS_FALLBACK = `ui-sans-serif, system-ui, -apple-system, "Segoe UI", Roboto, Arial, sans-serif`;
const SERIF_FALLBACK = `"Iowan Old Style", Palatino, Georgia, ui-serif, serif`;
const MONO_FALLBACK = `ui-monospace, "SF Mono", Menlo, Consolas, monospace`;

export const FONT_CLASSES: Record<FontClass, FontClassSpec> = {
  "geometric-sans": {
    id: "geometric-sans",
    label: "Geometric sans",
    note: "Built from circles and straight lines. Reads engineered and current, and goes cold if you set it too small.",
    scale: { ratio: 1.25, label: "Major third" },
    displayTracking: "-0.02em",
    bodyTracking: "0em",
    headingCase: "sentence",
    primaries: [
      { name: "Manrope", stack: `Manrope, ${SANS_FALLBACK}`, weights: ["ExtraLight 200", "Regular 400", "Medium 500", "Bold 700"], licence: "SIL Open Font Licence" },
      { name: "Poppins", stack: `Poppins, ${SANS_FALLBACK}`, weights: ["Light 300", "Regular 400", "Medium 500", "SemiBold 600"], licence: "SIL Open Font Licence" },
      { name: "Space Grotesk", stack: `"Space Grotesk", ${SANS_FALLBACK}`, weights: ["Light 300", "Regular 400", "Medium 500", "Bold 700"], licence: "SIL Open Font Licence" },
    ],
    secondaries: [
      { name: "Inter", stack: `Inter, ${SANS_FALLBACK}`, weights: ["Regular 400", "Medium 500"], licence: "SIL Open Font Licence" },
      { name: "IBM Plex Mono", stack: `"IBM Plex Mono", ${MONO_FALLBACK}`, weights: ["Regular 400", "Medium 500"], licence: "SIL Open Font Licence" },
    ],
    rules: [
      "Set display sizes at -0.02em. Geometric letterforms open up as they grow and the word stops holding together.",
      "Never letterspace lowercase. If a line needs air, add leading instead.",
      "Keep body copy at 400. The lighter weights look elegant on a poster and disappear on a screen.",
    ],
  },
  "neo-grotesque": {
    id: "neo-grotesque",
    label: "Neo-grotesque",
    note: "The neutral workhorse. It carries information without adding an accent of its own, which is exactly the point.",
    scale: { ratio: 1.2, label: "Minor third" },
    displayTracking: "-0.015em",
    bodyTracking: "0em",
    headingCase: "sentence",
    primaries: [
      { name: "Inter", stack: `Inter, ${SANS_FALLBACK}`, weights: ["Regular 400", "Medium 500", "SemiBold 600", "Bold 700"], licence: "SIL Open Font Licence" },
      { name: "Helvetica Now Display", stack: `"Helvetica Now Display", "Helvetica Neue", ${SANS_FALLBACK}`, weights: ["Regular 400", "Medium 500", "Bold 700"], licence: "Monotype, per-seat" },
      { name: "Suisse Int'l", stack: `"Suisse Intl", ${SANS_FALLBACK}`, weights: ["Regular 400", "Medium 500", "Bold 700"], licence: "Swiss Typefaces, commercial" },
    ],
    secondaries: [
      { name: "Suisse Int'l Mono", stack: `"Suisse Intl Mono", ${MONO_FALLBACK}`, weights: ["Regular 400"], licence: "Swiss Typefaces, commercial" },
      { name: "Source Serif 4", stack: `"Source Serif 4", ${SERIF_FALLBACK}`, weights: ["Regular 400", "SemiBold 600"], licence: "SIL Open Font Licence" },
    ],
    rules: [
      "One weight jump per level. Regular to Medium to Bold, nothing in between.",
      "Optical size matters more than weight here — set headlines large before you set them heavy.",
      "Numerals: use tabular figures in tables and proportional everywhere else.",
    ],
  },
  "humanist-sans": {
    id: "humanist-sans",
    label: "Humanist sans",
    note: "Calligraphic bones under a sans skin. The warmest sans option, and the easiest to read at length.",
    scale: { ratio: 1.2, label: "Minor third" },
    displayTracking: "-0.01em",
    bodyTracking: "0.005em",
    headingCase: "sentence",
    primaries: [
      { name: "Work Sans", stack: `"Work Sans", ${SANS_FALLBACK}`, weights: ["Regular 400", "Medium 500", "SemiBold 600"], licence: "SIL Open Font Licence" },
      { name: "Source Sans 3", stack: `"Source Sans 3", ${SANS_FALLBACK}`, weights: ["Regular 400", "SemiBold 600", "Bold 700"], licence: "SIL Open Font Licence" },
      { name: "Lato", stack: `Lato, ${SANS_FALLBACK}`, weights: ["Light 300", "Regular 400", "Bold 700"], licence: "SIL Open Font Licence" },
    ],
    secondaries: [
      { name: "Lora", stack: `Lora, ${SERIF_FALLBACK}`, weights: ["Regular 400", "Medium 500"], licence: "SIL Open Font Licence" },
      { name: "Inter", stack: `Inter, ${SANS_FALLBACK}`, weights: ["Regular 400"], licence: "SIL Open Font Licence" },
    ],
    rules: [
      "Let the leading breathe: 1.6 on body copy, not 1.4. The open apertures want the room.",
      "Sentence case everywhere. Humanist caps read as shouting because the letterforms are so friendly.",
      "Long-form is the strength — do not undercut it by setting body copy below 16px.",
    ],
  },
  "transitional-serif": {
    id: "transitional-serif",
    label: "Transitional serif",
    note: "Settled, editorial, quietly authoritative. Borrows four hundred years of print credibility without costing anything.",
    scale: { ratio: 1.333, label: "Perfect fourth" },
    displayTracking: "-0.01em",
    bodyTracking: "0em",
    headingCase: "sentence",
    primaries: [
      { name: "Source Serif 4", stack: `"Source Serif 4", ${SERIF_FALLBACK}`, weights: ["Regular 400", "SemiBold 600", "Bold 700"], licence: "SIL Open Font Licence" },
      { name: "Freight Text", stack: `"Freight Text Pro", ${SERIF_FALLBACK}`, weights: ["Book 400", "Medium 500", "Bold 700"], licence: "Commercial, per-seat" },
      { name: "Newsreader", stack: `Newsreader, ${SERIF_FALLBACK}`, weights: ["Regular 400", "Medium 500", "SemiBold 600"], licence: "SIL Open Font Licence" },
    ],
    secondaries: [
      { name: "Inter", stack: `Inter, ${SANS_FALLBACK}`, weights: ["Regular 400", "Medium 500"], licence: "SIL Open Font Licence" },
      { name: "Work Sans", stack: `"Work Sans", ${SANS_FALLBACK}`, weights: ["Regular 400", "Medium 500"], licence: "SIL Open Font Licence" },
    ],
    rules: [
      "Serif for anything anyone has to read; sans for anything anyone has to click.",
      "Measure of 62–72 characters. A serif set too wide loses the reader on the return sweep.",
      "Use real small caps for eyebrows. Faked small caps are visible from across the room.",
    ],
  },
  "didone-serif": {
    id: "didone-serif",
    label: "Didone serif",
    note: "Hairline thins against heavy stems. It is the fashion register, and it punishes small sizes and cheap printing.",
    scale: { ratio: 1.414, label: "Augmented fourth" },
    displayTracking: "0em",
    bodyTracking: "0.01em",
    headingCase: "title",
    primaries: [
      { name: "Playfair Display", stack: `"Playfair Display", ${SERIF_FALLBACK}`, weights: ["Regular 400", "Medium 500", "Bold 700"], licence: "SIL Open Font Licence" },
      { name: "Bodoni Moda", stack: `"Bodoni Moda", ${SERIF_FALLBACK}`, weights: ["Regular 400", "Medium 500", "Bold 700"], licence: "SIL Open Font Licence" },
    ],
    secondaries: [
      { name: "Inter", stack: `Inter, ${SANS_FALLBACK}`, weights: ["Light 300", "Regular 400"], licence: "SIL Open Font Licence" },
      { name: "Jost", stack: `Jost, ${SANS_FALLBACK}`, weights: ["Light 300", "Regular 400"], licence: "SIL Open Font Licence" },
    ],
    rules: [
      "Never below 24px. The hairlines break up and the whole thing looks broken rather than delicate.",
      "Pair with a quiet sans for body. Didone body copy is unreadable past a paragraph.",
      "In print, specify a coated stock. Uncoated stock lets the thins fill in.",
    ],
  },
  "slab-serif": {
    id: "slab-serif",
    label: "Slab serif",
    note: "Blunt rectangular serifs. Reads as sturdy and a little industrial — good when the brand needs to feel built.",
    scale: { ratio: 1.25, label: "Major third" },
    displayTracking: "-0.01em",
    bodyTracking: "0em",
    headingCase: "sentence",
    primaries: [
      { name: "Roboto Slab", stack: `"Roboto Slab", ${SERIF_FALLBACK}`, weights: ["Regular 400", "Medium 500", "Bold 700"], licence: "Apache 2.0" },
      { name: "Zilla Slab", stack: `"Zilla Slab", ${SERIF_FALLBACK}`, weights: ["Regular 400", "Medium 500", "Bold 700"], licence: "SIL Open Font Licence" },
    ],
    secondaries: [
      { name: "Inter", stack: `Inter, ${SANS_FALLBACK}`, weights: ["Regular 400", "Medium 500"], licence: "SIL Open Font Licence" },
      { name: "IBM Plex Mono", stack: `"IBM Plex Mono", ${MONO_FALLBACK}`, weights: ["Regular 400"], licence: "SIL Open Font Licence" },
    ],
    rules: [
      "Slabs are heavy by default — set headlines at Regular before reaching for Bold.",
      "Give it flat, generous margins. Slab type in a tight box looks cramped rather than solid.",
      "Avoid pairing with another serif. The slab already is the texture.",
    ],
  },
  "technical-mono": {
    id: "technical-mono",
    label: "Technical mono",
    note: "Fixed width. Every character on a grid, which reads as measured, precise and slightly machine-adjacent.",
    scale: { ratio: 1.2, label: "Minor third" },
    displayTracking: "0.02em",
    bodyTracking: "0em",
    headingCase: "upper",
    primaries: [
      { name: "IBM Plex Mono", stack: `"IBM Plex Mono", ${MONO_FALLBACK}`, weights: ["Regular 400", "Medium 500", "SemiBold 600"], licence: "SIL Open Font Licence" },
      { name: "JetBrains Mono", stack: `"JetBrains Mono", ${MONO_FALLBACK}`, weights: ["Regular 400", "Medium 500", "Bold 700"], licence: "SIL Open Font Licence" },
    ],
    secondaries: [
      { name: "Inter", stack: `Inter, ${SANS_FALLBACK}`, weights: ["Regular 400", "Medium 500"], licence: "SIL Open Font Licence" },
      { name: "Work Sans", stack: `"Work Sans", ${SANS_FALLBACK}`, weights: ["Regular 400"], licence: "SIL Open Font Licence" },
    ],
    rules: [
      "Uppercase headings with +0.02em tracking. Mono lowercase at display size reads as code, not as a brand.",
      "Mono for headings and labels, a proportional sans for body. All-mono is a costume.",
      "Lean on the grid: align everything to the character width and the layout does the work for you.",
    ],
  },
};

/** Modular scale, rounded to whole pixels the way a real spec sheet is written. */
export function buildScale(ratio: number, base = 16): number[] {
  const steps = [-1, 0, 1, 2, 3, 4, 5];
  return steps.map((s) => Math.round(base * ratio ** s));
}
