import type { PriceStance } from "@/lib/types";

/**
 * Palette recipes.
 *
 * Not random colour. Each recipe is a named position — a hue family, a
 * temperature and a level of saturation that a studio would actually reach for
 * — tagged with the traits and price stance it suits. The generator narrows by
 * tag, then picks, then derives the tints. Refresh moves you through the
 * shortlist rather than to somewhere unrelated.
 */

export interface PaletteRecipe {
  id: string;
  name: string;
  rationale: string;
  /** Anchor hue in degrees, and how far the support colour sits from it. */
  hue: number;
  spread: number;
  saturation: number;
  /** Lightness of the primary. Darker anchors read more expensive. */
  lightness: number;
  ink: string;
  surface: string;
  accentHue: number;
  pairingRule: string;
  traits: string[];
  stances: PriceStance[];
}

export const PALETTE_RECIPES: PaletteRecipe[] = [
  {
    id: "electric-indigo",
    name: "Electric Indigo",
    rationale: "A saturated blue that behaves like a primary rather than a corporate default. Reads decisive on a screen and survives being printed badly.",
    hue: 248, spread: 34, saturation: 0.92, lightness: 0.55,
    ink: "#0B1020", surface: "#F7F8FC", accentHue: 190,
    pairingRule: "Indigo on white for anything transactional. Ink on indigo for anything emotional. Never indigo on ink.",
    traits: ["Bold", "Innovative", "Future-proof", "Energetic", "Empowering"],
    stances: ["accessible", "considered", "premium"],
  },
  {
    id: "signal-orange",
    name: "Signal Orange",
    rationale: "One hot colour against a near-monochrome field. The orange is a pointer, not a wash — it should never cover more than a tenth of a layout.",
    hue: 18, spread: 24, saturation: 0.86, lightness: 0.52,
    ink: "#141210", surface: "#FAF8F5", accentHue: 200,
    pairingRule: "Ten per cent rule: the orange marks the one thing you want touched on the page, and nothing else.",
    traits: ["Bold", "Disruptive", "Energetic", "Provocative", "Scrappy"],
    stances: ["accessible", "considered"],
  },
  {
    id: "quiet-forest",
    name: "Quiet Forest",
    rationale: "Deep green with a warm neutral underneath. Reads considered and grown-up without going near the sustainability clichés.",
    hue: 156, spread: 30, saturation: 0.44, lightness: 0.28,
    ink: "#12180F", surface: "#F4F3EC", accentHue: 40,
    pairingRule: "Green is the ground, not the highlight. Set type in ink on the paper tone, and let the green hold whole panels.",
    traits: ["Trusted", "Authentic", "Resilient", "Purpose-driven", "Human"],
    stances: ["considered", "premium"],
  },
  {
    id: "graphite-bone",
    name: "Graphite & Bone",
    rationale: "Near-black against a bone paper tone, with one cool accent held in reserve. The most disciplined option here, and the hardest to get wrong.",
    hue: 220, spread: 12, saturation: 0.08, lightness: 0.32,
    ink: "#101112", surface: "#F2F0EB", accentHue: 12,
    pairingRule: "Colour is rationed. If a layout needs the accent twice, the layout is wrong.",
    traits: ["Minimal", "Refined", "Precise", "Systematic", "Premium"],
    stances: ["premium", "luxury"],
  },
  {
    id: "oxblood-sand",
    name: "Oxblood & Sand",
    rationale: "A deep red that has had the shout taken out of it, warmed by sand. Expensive without being cold about it.",
    hue: 356, spread: 28, saturation: 0.52, lightness: 0.32,
    ink: "#1A0F0E", surface: "#F6F0E7", accentHue: 32,
    pairingRule: "Oxblood carries the covers and the packaging. Sand carries the reading. Keep them apart at small sizes.",
    traits: ["Premium", "Luxurious", "Refined", "Proven", "Trusted"],
    stances: ["premium", "luxury"],
  },
  {
    id: "cobalt-chalk",
    name: "Cobalt & Chalk",
    rationale: "A clean institutional blue on chalk white. Familiar enough to be trusted on a form, sharp enough not to feel like a bank from 2003.",
    hue: 214, spread: 26, saturation: 0.78, lightness: 0.44,
    ink: "#0D1522", surface: "#FBFBFD", accentHue: 158,
    pairingRule: "Cobalt for structure, chalk for space, green accent only for confirmation states.",
    traits: ["Trusted", "Systematic", "Strategic", "Transparent", "Proven"],
    stances: ["accessible", "considered", "premium"],
  },
  {
    id: "warm-clay",
    name: "Warm Clay",
    rationale: "Terracotta and unbleached paper. Handmade without being rustic, which is the hardest line in this whole palette set to walk.",
    hue: 20, spread: 30, saturation: 0.46, lightness: 0.48,
    ink: "#20160F", surface: "#F7F1E8", accentHue: 168,
    pairingRule: "Let the paper tone show. Full-bleed clay reads as an error, not a decision.",
    traits: ["Warm", "Human", "Approachable", "Authentic", "Collaborative"],
    stances: ["accessible", "considered"],
  },
  {
    id: "midnight-lime",
    name: "Midnight & Lime",
    rationale: "Near-black with one acid green. Reads technical and current, and gives an otherwise sober system somewhere to be loud.",
    hue: 84, spread: 40, saturation: 0.88, lightness: 0.52,
    ink: "#0A0C0A", surface: "#F3F5F1", accentHue: 210,
    pairingRule: "Lime on midnight only. On white it turns yellow and stops carrying the brand.",
    traits: ["Disruptive", "Innovative", "Future-proof", "Provocative", "Precise"],
    stances: ["considered", "premium"],
  },
  {
    id: "ivory-navy",
    name: "Ivory & Navy",
    rationale: "The classical pairing, kept honest by a very dark navy rather than a soft one. Quiet, legible, and unfashionable in a way that lasts.",
    hue: 222, spread: 18, saturation: 0.56, lightness: 0.22,
    ink: "#0A1024", surface: "#FBF8F1", accentHue: 36,
    pairingRule: "Navy sets the type, ivory holds the page, gold appears once per document at most.",
    traits: ["Refined", "Trusted", "Proven", "Strategic", "Premium"],
    stances: ["premium", "luxury"],
  },
  {
    id: "coral-slate",
    name: "Coral & Slate",
    rationale: "A soft coral against cool slate. Friendly without being childish, which is where most consumer palettes fall over.",
    hue: 6, spread: 34, saturation: 0.72, lightness: 0.62,
    ink: "#1B1F24", surface: "#F5F6F7", accentHue: 196,
    pairingRule: "Coral for people and moments, slate for numbers and structure. Do not swap them.",
    traits: ["Warm", "Playful", "Optimistic", "Approachable", "Creative"],
    stances: ["accessible", "considered"],
  },
  {
    id: "violet-steel",
    name: "Violet & Steel",
    rationale: "A cooled violet with a steel neutral. Distinct in a category full of blues, without leaving the professional register.",
    hue: 268, spread: 30, saturation: 0.58, lightness: 0.46,
    ink: "#14111C", surface: "#F6F5F9", accentHue: 172,
    pairingRule: "Violet takes the brand moments, steel takes the interface. The teal accent is for data only.",
    traits: ["Creative", "Visionary", "Innovative", "Strategic", "Empowering"],
    stances: ["considered", "premium"],
  },
  {
    id: "sun-ink",
    name: "Sun & Ink",
    rationale: "Warm yellow against true black. Impossible to ignore on a shelf or a feed, and cheap to print well.",
    hue: 44, spread: 22, saturation: 0.94, lightness: 0.56,
    ink: "#0C0B08", surface: "#FFFDF6", accentHue: 348,
    pairingRule: "Yellow never carries type. Ink on yellow, always — the reverse fails every contrast check there is.",
    traits: ["Optimistic", "Playful", "Bold", "Energetic", "Adventurous"],
    stances: ["accessible", "considered"],
  },
];

export function recipesFor(traits: string[], stance: PriceStance): PaletteRecipe[] {
  const scored = PALETTE_RECIPES.map((recipe) => {
    const traitHits = recipe.traits.filter((t) => traits.includes(t)).length;
    const stanceHit = recipe.stances.includes(stance) ? 2 : 0;
    return { recipe, score: traitHits * 3 + stanceHit };
  });
  const best = scored.filter((s) => s.score > 0).sort((a, b) => b.score - a.score);
  // Always leave the generator something to choose from, even for odd combinations.
  return (best.length >= 3 ? best : scored.sort((a, b) => b.score - a.score)).map((s) => s.recipe);
}
