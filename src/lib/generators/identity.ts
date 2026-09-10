import type { LogoRoute, MarkShape, DeliverablePayload, NamedStatement, Swatch } from "@/lib/types";
import { MOODBOARD_DIRECTIONS, VOICE_PILLARS } from "@/data/language";
import { recipesFor } from "@/data/palettes";
import { FONT_CLASSES, buildScale } from "@/data/typefaces";
import { contrastRatio, hslToHex, hexToRgb, rgbToCmyk } from "@/lib/color";
import { sentence, STANCE_WORDS, type Ctx } from "./ctx";

/* ---------------------------------------------------------------- */
/* Tone of voice                                                     */
/* ---------------------------------------------------------------- */

export function generateTone(ctx: Ctx): DeliverablePayload {
  const traits = ctx.brief.traits;
  const scored = VOICE_PILLARS.map((p) => ({
    pillar: p,
    score: p.traits.filter((t) => traits.includes(t)).length,
  })).sort((a, b) => b.score - a.score);

  // Take the best matches, then let the seed shuffle within the top band so a
  // refresh gives a genuinely different set rather than the same five reordered.
  const strong = scored.filter((s) => s.score > 0).map((s) => s.pillar);
  const rest = scored.filter((s) => s.score === 0).map((s) => s.pillar);
  const chosen = [...ctx.shuffle(strong), ...ctx.shuffle(rest)].slice(0, 5);

  const neverSay = ctx.brief.answers["never-say"]
    ? ctx.brief.answers["never-say"].split("\n").map((s) => s.trim()).filter(Boolean)
    : [];

  const items: NamedStatement[] = chosen.map((p) => ({
    label: p.name,
    statement: p.claim,
    detail: p.explain,
    sayThis: p.doThis,
    counterExample: p.notThis,
  }));

  if (neverSay.length > 0) {
    items.push({
      label: "Banned words",
      statement: "These words are out. All of them appear in the category and none of them survive being read aloud.",
      detail: "Taken straight from the workshop. Written into the guidelines as a hard rule, it survives three agency changes.",
      bannedWords: neverSay,
    });
  }

  const soundLike = ctx.brief.answers["sound-like"];

  return {
    kind: "statements",
    data: {
      intro: sentence(
        soundLike
          ? `The client's own reference point: ${soundLike.replace(/[.]$/, "")}. Five pillars below, each written as a claim with the thing it is not sitting underneath it`
          : "Five voice pillars. Each is a claim plus the thing it is not — a pillar without its opposite is not a rule, it is a mood",
      ),
      items,
    },
  };
}

/* ---------------------------------------------------------------- */
/* Look and feel                                                     */
/* ---------------------------------------------------------------- */

export function generateLookFeel(ctx: Ctx): DeliverablePayload {
  const directions = ctx.sample(MOODBOARD_DIRECTIONS, 3).map((d, i) => ({
    id: `direction-${i + 1}`,
    name: d.name,
    adjectives: d.adjectives,
    description: d.description,
    surfaces: d.surfaces,
    photography: d.photography,
    motion: d.motion,
  }));

  return {
    kind: "lookfeel",
    data: {
      directions,
      chosenId: directions[0].id,
      gridNote: ctx.pick([
        "Twelve columns on desktop, six on tablet, four on phone. Gutters stay at 24px at every breakpoint so the rhythm does not change with the screen.",
        "A four-column editorial grid with a wide outer margin. Text never crosses more than three columns, whatever the format.",
        "Baseline grid of 8px. Everything — type, spacing, component height — is a multiple of it, with no exceptions granted.",
      ]),
      marginRule: "Take the longest side of the format and divide by 25. That gives a modular margin that scales proportionally from a business card to a billboard. In extreme formats — very wide or very tall — adjust by eye for optical correctness.",
    },
  };
}

/* ---------------------------------------------------------------- */
/* Logo                                                              */
/* ---------------------------------------------------------------- */

const SHAPES: MarkShape[] = ["arc", "aperture", "monogram", "chevron", "orbit", "grid", "seal", "cut"];

const CONSTRUCTIONS: Record<MarkShape, string> = {
  arc: "A single arc struck from one radius, cut at 40°. The whole mark is one gesture, which keeps it legible at 16px.",
  aperture: "A square with one corner opened. The gap is exactly one stroke width, so the mark reads as a way in rather than a broken box.",
  monogram: "The initial set in the brand typeface, then redrawn — terminals cut flat, counters opened, one joint removed.",
  chevron: "Two strokes meeting at 60°, the upper stroke extended by a quarter. Movement without an arrow.",
  orbit: "Two ellipses on the same centre, rotated 30° apart. Overlap is knocked out rather than overprinted.",
  grid: "A 5×5 dot field with the centre nine weighted heavier. Reads as a system from far away and as a mark up close.",
  seal: "A circular lockup with the wordmark on the upper arc and the founding year on the lower. One heavy rule between them.",
  cut: "A solid block with a single diagonal removed at 22.5°. The negative space carries the name.",
};

export function generateLogo(ctx: Ctx): DeliverablePayload {
  const names = ctx.sample(
    ["The Signal", "The Mark", "The Anchor", "The Opening", "The Constant", "The Cut", "The Figure", "The Standard"],
    3,
  );
  const shapes = ctx.sample(SHAPES, 3);

  const routes: LogoRoute[] = shapes.map((shape, i) => ({
    id: `route-${i + 1}`,
    name: names[i],
    markShape: shape,
    construction: CONSTRUCTIONS[shape],
    clearspace: ctx.pick([
      "Clearspace equals the cap height of the wordmark on all four sides. Nothing enters it, including page edges.",
      "Clearspace equals the width of the mark's counter. Measure it, do not guess it.",
      "Clearspace equals half the mark's height. On small formats this is the first thing people break.",
    ]),
    minSize: ctx.pick(["16px digital / 8mm print", "20px digital / 10mm print", "24px digital / 12mm print"]),
    variants: ["Mark", "Wordmark", "Wordmark stacked", "Lockup horizontal", "Lockup stacked", "Reversed", "One colour"],
    rationale: ctx.pick([
      `Fits the personality without illustrating it. ${ctx.brief.traits[0] ?? "The brand"} is expressed through the construction, not through a picture of the thing.`,
      "The strongest option at small sizes, which is where the mark will spend most of its life — favicons, avatars, app icons.",
      "Holds up in one colour and reversed out, which rules out about half of what gets presented in this category.",
      "Distinct from every competitor mark reviewed in the competition deliverable, at a glance and in silhouette.",
    ]),
  }));

  return {
    kind: "logo",
    data: {
      routes,
      chosenRouteId: routes[0].id,
      wordmarkNote: sentence(
        `Wordmark set in ${FONT_CLASSES[ctx.brief.fontClass].primaries[0].name}, ` +
        `tracked at ${FONT_CLASSES[ctx.brief.fontClass].displayTracking}, with the joints redrawn by hand. ` +
        "Never re-typed from the font file — a wordmark is drawn once and then it is artwork",
      ),
    },
  };
}

/* ---------------------------------------------------------------- */
/* Colour                                                            */
/* ---------------------------------------------------------------- */

function makeSwatch(name: string, role: Swatch["role"], hex: string, usage: string): Swatch {
  const rgb = hexToRgb(hex);
  return {
    name, role, hex: hex.toUpperCase(), rgb,
    cmyk: rgbToCmyk(...rgb),
    usage,
    contrastOnWhite: contrastRatio(hex, "#FFFFFF"),
    contrastOnInk: contrastRatio(hex, "#101010"),
  };
}

export function generatePalette(ctx: Ctx): DeliverablePayload {
  const shortlist = recipesFor(ctx.brief.traits, ctx.brief.priceStance);
  const recipe = ctx.pick(shortlist.slice(0, Math.max(4, Math.min(8, shortlist.length))));

  /*
   * Within a recipe there is still room to move. Jitter the anchor by a few
   * degrees and a few points of lightness so a refresh reads as a different
   * take on the same idea rather than a different idea — which is what a
   * designer means when they ask to see it again.
   */
  const hueShift = (ctx.rng() - 0.5) * 14;
  const lightShift = (ctx.rng() - 0.5) * 0.09;
  const satShift = (ctx.rng() - 0.5) * 0.12;

  const hue = recipe.hue + hueShift;
  const sat = Math.max(0.05, Math.min(1, recipe.saturation + satShift));
  const light = Math.max(0.16, Math.min(0.7, recipe.lightness + lightShift));

  const primary = hslToHex(hue, sat, light);
  const support = hslToHex(hue + recipe.spread, sat * 0.72, Math.min(0.86, light + 0.3));
  const accent = hslToHex(recipe.accentHue + hueShift * 0.5, sat * 0.9, 0.5);
  const deep = hslToHex(hue, Math.min(1, sat + 0.06), Math.max(0.13, light - 0.22));

  const swatches: Swatch[] = [
    makeSwatch(recipe.name, "primary", primary, "The brand colour. Used for the mark, primary buttons and any full-bleed brand moment."),
    makeSwatch(`${recipe.name} Deep`, "support", deep, "Panels, footers and anywhere the primary would be too bright to sit behind long text."),
    makeSwatch("Ink", "ink", recipe.ink, "All body copy and headlines. Never pure black — pure black on a screen is a shadow, not a colour."),
    makeSwatch("Paper", "surface", recipe.surface, "The default page. Everything is set on this, not on white."),
    makeSwatch("Support", "support", support, "Backgrounds for cards, quiet states, and the second colour in any chart."),
    makeSwatch("Accent", "accent", accent, "Reserved. One use per screen, for the single thing that needs attention."),
  ];

  return {
    kind: "palette",
    data: {
      name: recipe.name,
      rationale: sentence(
        `${recipe.rationale} Chosen against the personality — ${ctx.brief.traits.slice(0, 3).join(", ").toLowerCase() || "the traits picked in the workshop"} — ` +
        `and a ${STANCE_WORDS[ctx.brief.priceStance].adj} price position`,
      ),
      swatches,
      pairingRule: recipe.pairingRule,
    },
  };
}

/* ---------------------------------------------------------------- */
/* Typography                                                        */
/* ---------------------------------------------------------------- */

export function generateTypography(ctx: Ctx): DeliverablePayload {
  const spec = FONT_CLASSES[ctx.brief.fontClass];
  const primary = ctx.pick(spec.primaries);
  const secondary = ctx.pick(spec.secondaries);
  const scale = buildScale(spec.scale.ratio);

  // The LEVEL / WEIGHT / USE table, generated from the class rather than typed by hand.
  const levels = [
    { level: "Display", weight: heaviest(primary.weights), size: `${scale[6]}px`, lineHeight: "0.95", tracking: spec.displayTracking, use: "Cover titles and one-line brand statements. One per surface, never two." },
    { level: "Headline", weight: heaviest(primary.weights), size: `${scale[4]}px`, lineHeight: "1.05", tracking: spec.displayTracking, use: "Hero statements, section dividers, campaign headlines." },
    { level: "Subheading", weight: middle(primary.weights), size: `${scale[3]}px`, lineHeight: "1.2", tracking: "0em", use: "Section titles, slide subheads, eyebrows above a block." },
    { level: "Body", weight: lightestUsable(primary.weights), size: `${scale[1]}px`, lineHeight: "1.6", tracking: spec.bodyTracking, use: "Paragraphs, descriptions, documents. The size everything else is judged against." },
    { level: "Label", weight: middle(secondary.weights), size: `${scale[0]}px`, lineHeight: "1.3", tracking: "0.04em", use: "Buttons, tags, navigation labels, table headers." },
    { level: "Caption", weight: lightestUsable(secondary.weights), size: `${Math.max(12, scale[0] - 2)}px`, lineHeight: "1.4", tracking: "0.01em", use: "Footnotes, image credits, legal lines, fine print." },
  ];

  return {
    kind: "typography",
    data: {
      className: spec.label,
      classNote: spec.note,
      primary: { name: primary.name, stack: primary.stack, weights: primary.weights },
      secondary: { name: secondary.name, stack: secondary.stack, weights: secondary.weights },
      scaleRatio: `${spec.scale.label} — ${spec.scale.ratio} from a 16px base`,
      levels,
      rules: [
        ...spec.rules,
        `Licence: ${primary.licence} for ${primary.name}; ${secondary.licence} for ${secondary.name}. Check seat counts before the first campaign, not after.`,
      ],
    },
  };
}

function heaviest(weights: string[]): string {
  return weights[weights.length - 1];
}
function middle(weights: string[]): string {
  return weights[Math.min(weights.length - 1, Math.max(0, Math.floor(weights.length / 2)))];
}
function lightestUsable(weights: string[]): string {
  const regular = weights.find((w) => /Regular|Book|400/.test(w));
  return regular ?? weights[0];
}
