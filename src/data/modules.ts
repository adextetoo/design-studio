import type { ModuleId, ModuleMeta, Stage } from "@/lib/types";

/**
 * The eighteen decisions a brand system is made of, in the order a studio
 * actually takes them: understand the business, place it against the market,
 * design the identity, then prove the identity survives contact with real
 * surfaces.
 */
export const MODULES: ModuleMeta[] = [
  // Foundation — what the business is
  { id: "story", title: "Brand Story", stage: "foundation", purpose: "Where this came from, told the way a founder tells it over coffee.", exports: "brand" },
  { id: "mission", title: "Brand Mission", stage: "foundation", purpose: "What the company is here to do, in a sentence anyone can repeat.", exports: "brand" },
  { id: "vision", title: "Brand Vision", stage: "foundation", purpose: "The world this brand is arguing for, ten years out.", exports: "brand" },
  { id: "offering", title: "Offering", stage: "foundation", purpose: "What is actually sold, at what level, and where the edges are.", exports: "brand" },

  // Position — where it sits
  { id: "market", title: "Market Research", stage: "position", purpose: "The size and shape of the market, and which way it is moving.", exports: "brand" },
  { id: "audience", title: "Customer & Audience", stage: "position", purpose: "Two named buyers with motivations, blockers and buying habits.", exports: "brand" },
  { id: "competition", title: "Competition", stage: "position", purpose: "Who else is in the room, what they own, and what they leave open.", exports: "brand" },
  { id: "differentiation", title: "Differentiation", stage: "position", purpose: "The claim only this company can make, with the proof under it.", exports: "brand" },
  { id: "expose", title: "Brand Exposé", stage: "position", purpose: "The public-facing write-up: press, deck intro, about page.", exports: "brand" },

  // Identity — how it looks and sounds
  { id: "tone", title: "Tone & Voice", stage: "identity", purpose: "Five voice pillars, each with the thing it is not.", exports: "brand" },
  { id: "lookfeel", title: "Look & Feel", stage: "identity", purpose: "Three art directions with adjectives, surfaces and a grid rule.", exports: "design" },
  { id: "logo", title: "Logo", stage: "identity", purpose: "Upload the client's mark, or take one of three constructed routes.", exports: "design" },
  { id: "palette", title: "Colour", stage: "identity", purpose: "A working palette with HEX, RGB, CMYK and measured contrast.", exports: "design" },
  { id: "typography", title: "Typography", stage: "identity", purpose: "A hierarchy generated from the chosen font class, level by level.", exports: "design" },

  // Application — where it has to work
  { id: "packaging", title: "Packaging", stage: "application", purpose: "Substrate, finish and the panel specs for each physical item.", exports: "design" },
  { id: "marketing", title: "Marketing Material", stage: "application", purpose: "Modern layouts at real ratios, with the hierarchy written down.", exports: "design" },
  { id: "social", title: "Social Media", stage: "application", purpose: "Bio, content pillars, three posts and a cadence you can hold.", exports: "both" },
  { id: "website", title: "Website Copy", stage: "application", purpose: "Navigation, hero and section copy, ready to paste into a build.", exports: "both" },
];

export const MODULE_IDS: ModuleId[] = MODULES.map((m) => m.id);

export const MODULE_BY_ID: Record<ModuleId, ModuleMeta> = Object.fromEntries(
  MODULES.map((m) => [m.id, m]),
) as Record<ModuleId, ModuleMeta>;

export const STAGES: { id: Stage; label: string; note: string }[] = [
  { id: "foundation", label: "Foundation", note: "What the business is" },
  { id: "position", label: "Position", note: "Where it sits" },
  { id: "identity", label: "Identity", note: "How it looks and sounds" },
  { id: "application", label: "Application", note: "Where it has to work" },
];

export function modulesInStage(stage: Stage): ModuleMeta[] {
  return MODULES.filter((m) => m.stage === stage);
}
