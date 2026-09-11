/**
 * The discovery workshop.
 *
 * Structure copied from the reference studio software: eight phases, 27 steps,
 * one question on screen at a time. The questions themselves are the ones a
 * brand strategist actually asks — open, specific, and impossible to answer
 * with a slogan.
 */

export interface WorkshopPhase {
  id: string;
  name: string;
  intent: string;
}

export interface WorkshopStep {
  id: string;
  phaseId: string;
  kind: "text" | "long" | "list" | "traits" | "choice";
  question: string;
  helper?: string;
  placeholder?: string;
  options?: string[];
  /**
   * Common answers offered as chips beside the field, so a question does not
   * have to be answered from a blank page. Suggestions never replace the
   * field: a text step fills it, a long or list step adds to what is there.
   * `"picked-traits"` resolves at render to the five traits chosen in the
   * ballot, which is what "those words" refers to in step 17.
   */
  suggest?: string[] | "picked-traits";
  /** Where this answer lands in the brief. */
  binds?:
    | "brandName"
    | "offering"
    | "sector"
    | "location"
    | "foundedYear"
    | "audienceNote"
    | "competitors"
    | "priceStance"
    | "fontClass";
}

export const PHASES: WorkshopPhase[] = [
  { id: "intro", name: "Introduction", intent: "Get the plain facts on the table before anyone gets clever." },
  { id: "origin", name: "Origin", intent: "Find the real reason this exists. It is almost never the one on the website." },
  { id: "growth", name: "Growth Timeline", intent: "Understand the ambition, so the identity has somewhere to grow into." },
  { id: "customer", name: "Customer", intent: "Get to one specific person, not a demographic." },
  { id: "personality", name: "Brand Personality", intent: "Agree how the brand behaves before arguing about how it looks." },
  { id: "market", name: "Market & Rivals", intent: "Map the room honestly, including where they are beaten." },
  { id: "voice", name: "Voice", intent: "Decide what the brand sounds like, and what it refuses to sound like." },
  { id: "craft", name: "Craft", intent: "Set the visual starting conditions without designing yet." },
];

export const TRAIT_BANK = [
  "Bold", "Playful", "Premium", "Relentless", "Warm", "Strategic",
  "Innovative", "Trusted", "Minimal", "Energetic", "Refined", "Authentic",
  "Disruptive", "Luxurious", "Approachable", "Systematic", "Creative", "Human",
  "Transparent", "Purpose-driven", "Resilient", "Partner-driven", "Scrappy",
  "Future-proof", "Visionary", "Empowering", "Provocative", "Collaborative",
  "Adventurous", "Precise", "Optimistic", "Proven",
] as const;

/**
 * The categories a stranger would actually file a company under. Broad on
 * purpose: the question asks what you are compared to, not what you are.
 */
export const SECTOR_SUGGESTIONS = [
  "Personal finance", "Fintech", "SaaS", "E-commerce", "Healthcare",
  "Education", "Professional services", "Hospitality", "Property",
  "Manufacturing", "Creative agency", "Non-profit",
] as const;

/**
 * Drawn from the same machine-tell and consultancy-filler vocabulary the
 * Human Tone Check scores generated copy against, so the two features agree
 * with each other rather than contradicting.
 */
export const BANNED_WORD_SUGGESTIONS = [
  "solutions", "leverage", "synergy", "journey", "empower", "seamless",
  "innovative", "world-class", "cutting-edge", "best-in-class",
  "game-changing", "disruptive", "holistic", "robust", "elevate",
] as const;

export const FONT_CLASS_OPTIONS = [
  { value: "geometric-sans", label: "Geometric sans", note: "Circles and straight lines. Reads modern, engineered, a little cool." },
  { value: "neo-grotesque", label: "Neo-grotesque", note: "Neutral and hard-working. Gets out of the way of the message." },
  { value: "humanist-sans", label: "Humanist sans", note: "Calligraphic bones. Warmer, easier to read in long runs." },
  { value: "transitional-serif", label: "Transitional serif", note: "Editorial and settled. Borrows the authority of print." },
  { value: "didone-serif", label: "Didone serif", note: "High contrast, thin hairlines. Fashion, beauty, luxury." },
  { value: "slab-serif", label: "Slab serif", note: "Blunt and confident. Good when the brand needs weight." },
  { value: "technical-mono", label: "Technical mono", note: "Fixed width, data-flavoured. Signals precision and tooling." },
] as const;

export const PRICE_STANCE_OPTIONS = [
  { value: "accessible", label: "Accessible", note: "Priced to be a default choice. Volume matters more than margin." },
  { value: "considered", label: "Considered", note: "Costs more than the cheap option, and the buyer knows why." },
  { value: "premium", label: "Premium", note: "Top of the normal range. Sold on craft and outcome." },
  { value: "luxury", label: "Luxury", note: "Price is part of the signal. Scarcity is deliberate." },
] as const;

export const STEPS: WorkshopStep[] = [
  // Phase 1 — Introduction (4)
  { id: "name", phaseId: "intro", kind: "text", binds: "brandName", question: "What is the company called?", helper: "Exactly as it should be set in type, including any casing you care about.", placeholder: "Monty" },
  { id: "offering", phaseId: "intro", kind: "long", binds: "offering", question: "What do you sell, in your own words?", helper: "No positioning yet. Say it the way you would to a friend who does not work in your industry.", placeholder: "We help people close to retirement see everything they own in one place." },
  { id: "sector", phaseId: "intro", kind: "text", binds: "sector", question: "What sector would a stranger file you under?", helper: "Even if it is wrong. We need to know what you are being compared to.", placeholder: "Personal finance", suggest: [...SECTOR_SUGGESTIONS] },
  { id: "why-name", phaseId: "intro", kind: "long", question: "Why did you choose your brand name?", helper: "The story behind the name usually points at the thing the brand really cares about.", placeholder: "It was my grandfather's name. He kept every receipt he ever got." },

  // Phase 2 — Origin (4)
  { id: "founded", phaseId: "origin", kind: "text", binds: "foundedYear", question: "What year did the work start?", helper: "The year you started doing it, not the year you registered the company.", placeholder: "2021" },
  { id: "location", phaseId: "origin", kind: "text", binds: "location", question: "Where is the company based?", placeholder: "Manchester" },
  { id: "annoyed", phaseId: "origin", kind: "long", question: "What annoyed you enough to start this?", helper: "Frustration is more useful than inspiration. It is specific.", placeholder: "Everyone I knew had four pensions and no idea what any of them were worth." },
  { id: "first-client", phaseId: "origin", kind: "long", question: "Who paid you first, and why did they say yes?", helper: "The first yes tells you what the offer really is.", placeholder: "A retiring teacher. She said we were the first people who did not talk down to her." },

  // Phase 3 — Growth Timeline (3)
  { id: "ten-years", phaseId: "growth", kind: "long", question: "In ten years, what does the company look like?", helper: "Describe the room, the team, the work. Not the revenue.", placeholder: "Forty people. Still one product. Known for being the honest one." },
  { id: "refuse", phaseId: "growth", kind: "long", question: "What will you refuse to do, even when it would make money?", helper: "The refusals draw the brand's edges faster than the ambitions do.", placeholder: "We will never sell customer data, and we will never run a referral scheme." },
  { id: "success", phaseId: "growth", kind: "long", question: "A year from now, what has to be true for this rebrand to have worked?", placeholder: "Our sales team stops apologising for the website." },

  // Phase 4 — Customer (4)
  { id: "audience", phaseId: "customer", kind: "long", binds: "audienceNote", question: "Describe your best customer as if you were describing a person.", helper: "One person. Name them if it helps.", placeholder: "Fifty-eight, sold a business, does not trust anyone in a suit." },
  { id: "before", phaseId: "customer", kind: "long", question: "What were they doing before they found you?", helper: "This is the real competitor. Often it is a spreadsheet or nothing at all.", placeholder: "A spreadsheet their son-in-law built in 2016." },
  { id: "objection", phaseId: "customer", kind: "long", question: "What is the objection you hear most often?", placeholder: "It sounds expensive for something I could probably do myself." },
  { id: "moment", phaseId: "customer", kind: "long", question: "When does a customer first realise you were worth it?", helper: "Find the moment. It usually becomes the hero of the website.", placeholder: "The first time they open the app and every account is already there." },

  // Phase 5 — Brand Personality (3)
  { id: "traits", phaseId: "personality", kind: "traits", question: "Pick your top 5 traits", helper: "Select the words that best describe your brand personality. Everyone picks in private — nobody sees the room until the reveal." },
  { id: "not-traits", phaseId: "personality", kind: "long", question: "Which of those words would your competitors also claim?", helper: "Anything both of you can say is not a personality, it is a category requirement.", placeholder: "Trusted. Everyone in finance says trusted.", suggest: "picked-traits" },
  { id: "person", phaseId: "personality", kind: "long", question: "If the brand walked into a meeting, how would it behave?", placeholder: "Arrives early, says the uncomfortable thing kindly, leaves on time." },

  // Phase 6 — Market & Rivals (3)
  { id: "competitors", phaseId: "market", kind: "list", binds: "competitors", question: "Name the companies you lose deals to.", helper: "One per line. Real names, including the ones that sting.", placeholder: "Nutmeg\nMoneybox\nA spreadsheet" },
  { id: "rival-good", phaseId: "market", kind: "long", question: "What does your strongest rival genuinely do better than you?", helper: "Honest answers here make the differentiation defensible later.", placeholder: "Their onboarding takes four minutes. Ours takes twenty." },
  { id: "price", phaseId: "market", kind: "choice", binds: "priceStance", question: "Where do you sit on price?", options: ["accessible", "considered", "premium", "luxury"] },

  // Phase 7 — Voice (3)
  { id: "sound-like", phaseId: "voice", kind: "long", question: "Whose writing do you wish your brand sounded like?", helper: "A publication, a person, a product. Anything you can point at.", placeholder: "The way a good GP explains a diagnosis." },
  { id: "never-say", phaseId: "voice", kind: "list", question: "List words this brand will never use.", helper: "One per line. These go into the guidelines as a hard rule.", placeholder: "solutions\nleverage\njourney", suggest: [...BANNED_WORD_SUGGESTIONS] },
  { id: "hard-truth", phaseId: "voice", kind: "long", question: "What is the hardest true thing you tell customers?", helper: "A brand that can say one hard thing gets believed about everything else.", placeholder: "If you are under forty, you probably do not need us yet." },

  // Phase 8 — Craft (3)
  { id: "font-class", phaseId: "craft", kind: "choice", binds: "fontClass", question: "Which class of typeface feels right?", helper: "Class, not typeface. We generate the hierarchy from the class you pick.", options: ["geometric-sans", "neo-grotesque", "humanist-sans", "transitional-serif", "didone-serif", "slab-serif", "technical-mono"] },
  { id: "admire", phaseId: "craft", kind: "long", question: "Name a brand you admire that is nothing like yours.", helper: "Taste travels across categories. This is where the look and feel starts.", placeholder: "Aesop. Nothing to do with pensions, but they never shout." },
  { id: "inherit", phaseId: "craft", kind: "long", question: "What are we not allowed to change?", helper: "Existing equity: a colour, a mark, a name customers already know.", placeholder: "The blue. Twenty years of paperwork is that blue." },
];

export const TOTAL_STEPS = STEPS.length;

export function phaseOf(step: WorkshopStep): WorkshopPhase {
  const phase = PHASES.find((p) => p.id === step.phaseId);
  if (!phase) throw new Error(`Unknown phase ${step.phaseId}`);
  return phase;
}

export function phaseNumber(phaseId: string): number {
  return PHASES.findIndex((p) => p.id === phaseId) + 1;
}
