import type { Brief, ModuleId, ModulePayload } from "@/lib/types";
import { hashString } from "@/lib/rng";
import { makeCtx } from "./ctx";
import { generateMission, generateOffering, generateStory, generateVision } from "./foundation";
import {
  generateAudience, generateCompetition, generateDifferentiation, generateExpose, generateMarket,
} from "./position";
import {
  generateLogo, generateLookFeel, generatePalette, generateTone, generateTypography,
} from "./identity";
import {
  generateMarketing, generatePackaging, generateSocial, generateWebsite,
} from "./application";

type Generator = (ctx: ReturnType<typeof makeCtx>) => ModulePayload;

const GENERATORS: Record<ModuleId, Generator> = {
  story: generateStory,
  mission: generateMission,
  vision: generateVision,
  offering: generateOffering,
  market: generateMarket,
  audience: generateAudience,
  competition: generateCompetition,
  differentiation: generateDifferentiation,
  expose: generateExpose,
  tone: generateTone,
  lookfeel: generateLookFeel,
  logo: generateLogo,
  palette: generatePalette,
  typography: generateTypography,
  packaging: generatePackaging,
  marketing: generateMarketing,
  social: generateSocial,
  website: generateWebsite,
};

/**
 * Seed for a module round.
 *
 * Mixing the brief into the seed means two different brands never draw the
 * same round-one output, and mixing the round in means Refresh always moves.
 */
export function seedFor(brief: Brief, moduleId: ModuleId, round: number): number {
  const fingerprint = [
    brief.brandName, brief.sector, brief.offering, brief.priceStance,
    brief.fontClass, brief.traits.join("|"), moduleId, String(round),
  ].join("::");
  return hashString(fingerprint);
}

export function generate(brief: Brief, moduleId: ModuleId, seed: number): ModulePayload {
  const generator = GENERATORS[moduleId];
  if (!generator) throw new Error(`No generator for module ${moduleId}`);
  return generator(makeCtx(brief, seed));
}
