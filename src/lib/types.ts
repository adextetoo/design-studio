/**
 * Domain model for the design studio.
 *
 * One rule runs through all of this: nothing reaches Brand.md or Design.md
 * until a human has pressed Approve on it. Generated content is a draft with
 * a status, never a fact.
 */

export type DeliverableId =
  | "story"
  | "mission"
  | "vision"
  | "expose"
  | "tone"
  | "lookfeel"
  | "offering"
  | "market"
  | "competition"
  | "differentiation"
  | "audience"
  | "logo"
  | "palette"
  | "typography"
  | "packaging"
  | "marketing"
  | "social"
  | "website";

export type Stage = "foundation" | "position" | "identity" | "application";

export interface DeliverableMeta {
  id: DeliverableId;
  title: string;
  stage: Stage;
  /** One line, written for a designer, explaining what this deliverable decides. */
  purpose: string;
  /** What lands in the exported markdown when this deliverable is approved. */
  exports: "brand" | "design" | "both";
}

/* ------------------------------------------------------------------ */
/* Brief — everything the generators are allowed to read               */
/* ------------------------------------------------------------------ */

export type FontClass =
  | "geometric-sans"
  | "neo-grotesque"
  | "humanist-sans"
  | "transitional-serif"
  | "didone-serif"
  | "slab-serif"
  | "technical-mono";

export type PriceStance = "accessible" | "considered" | "premium" | "luxury";

export interface Brief {
  brandName: string;
  /** What they sell, in the client's own words. Drives Offering + Website copy. */
  offering: string;
  sector: string;
  location: string;
  foundedYear: string;
  /** The person the brand serves, in the client's own words. */
  audienceNote: string;
  competitors: string[];
  priceStance: PriceStance;
  /** Top five personality traits from the workshop ballot. */
  traits: string[];
  fontClass: FontClass;
  /** Free-text workshop answers, keyed by question id. */
  answers: Record<string, string>;
}

/* ------------------------------------------------------------------ */
/* Deliverable payloads                                                     */
/* ------------------------------------------------------------------ */

export interface Prose {
  heading?: string;
  paragraphs: string[];
}

export interface NamedStatement {
  label: string;
  statement: string;
  detail: string;
  /** The line that gets this right. */
  sayThis?: string;
  /**
   * The line that gets it wrong. Deliberately full of the language the brand
   * avoids, so it is exempt from the human-tone audit by path.
   */
  counterExample?: string;
  /** Words the client has banned. Quoted, therefore also exempt. */
  bannedWords?: string[];
}

export interface Swatch {
  name: string;
  role: "primary" | "ink" | "surface" | "support" | "accent";
  hex: string;
  rgb: [number, number, number];
  cmyk: [number, number, number, number];
  usage: string;
  contrastOnWhite: number;
  contrastOnInk: number;
}

export interface PalettePayload {
  name: string;
  rationale: string;
  swatches: Swatch[];
  pairingRule: string;
}

export interface TypeLevel {
  level: string;
  weight: string;
  size: string;
  lineHeight: string;
  tracking: string;
  use: string;
}

export interface TypographyPayload {
  className: string;
  classNote: string;
  primary: { name: string; stack: string; weights: string[] };
  secondary: { name: string; stack: string; weights: string[] };
  scaleRatio: string;
  levels: TypeLevel[];
  rules: string[];
}

export interface LogoRoute {
  id: string;
  name: string;
  construction: string;
  markShape: MarkShape;
  clearspace: string;
  minSize: string;
  variants: string[];
  rationale: string;
}

export type MarkShape =
  | "arc"
  | "aperture"
  | "monogram"
  | "chevron"
  | "orbit"
  | "grid"
  | "seal"
  | "cut";

export interface LogoPayload {
  routes: LogoRoute[];
  chosenRouteId: string;
  /** Data URL of an uploaded mark, when the studio is working from client artwork. */
  uploadedMark?: string;
  uploadedName?: string;
  wordmarkNote: string;
}

export interface Persona {
  name: string;
  archetypeLabel: string;
  traits: string[];
  age: string;
  location: string;
  alsoBuys: string[];
  motivations: string[];
  challenges: string[];
  whyThisBrand: string[];
  buyingBehaviour: string[];
  socialBehaviour: string[];
}

export interface MarketPayload {
  headline: string;
  summary: string;
  figures: { value: string; label: string; note: string }[];
  segments: { name: string; share: number; note: string }[];
  shifts: string[];
  sources: string[];
}

export interface CompetitorRow {
  name: string;
  position: string;
  doesWell: string;
  leavesOpen: string;
  priceBand: PriceStance;
}

export interface CompetitionPayload {
  rows: CompetitorRow[];
  axes: { x: [string, string]; y: [string, string] };
  plot: { name: string; x: number; y: number; isUs: boolean }[];
  read: string;
}

export interface DifferentiationPayload {
  claim: string;
  proofs: { proof: string; evidence: string }[];
  onlyWeCan: string;
  notFor: string[];
}

export interface OfferingPayload {
  line: string;
  tiers: { name: string; price: string; forWho: string; includes: string[] }[];
  boundaries: string[];
}

export interface LookFeelPayload {
  directions: {
    id: string;
    name: string;
    adjectives: string[];
    description: string;
    surfaces: string[];
    photography: string;
    motion: string;
  }[];
  chosenId: string;
  gridNote: string;
  marginRule: string;
}

export interface ArtifactSpec {
  name: string;
  format: string;
  spec: string[];
  copy?: { headline: string; sub?: string };
}

export interface PackagingPayload {
  substrate: string;
  finish: string;
  items: ArtifactSpec[];
  unboxingNote: string;
}

export interface MarketingPayload {
  layouts: {
    name: string;
    ratio: string;
    grid: string;
    hierarchy: string[];
    headline: string;
    sub: string;
  }[];
  rules: string[];
}

export interface SocialPayload {
  bio: string;
  handle: string;
  pillars: { name: string; share: number; example: string }[];
  posts: { format: string; headline: string; caption: string }[];
  bannerNote: string;
  cadence: string;
}

export interface WebsiteCopyPayload {
  navigation: string[];
  hero: { eyebrow: string; headline: string; sub: string; primaryCta: string; secondaryCta: string };
  sections: { kicker: string; heading: string; body: string }[];
  proof: string;
  footerLine: string;
}

export interface ExposePayload {
  oneLiner: string;
  paragraphs: string[];
  atAGlance: { label: string; value: string }[];
  pressLine: string;
}

export type DeliverablePayload =
  | { kind: "prose"; data: Prose }
  | { kind: "statements"; data: { intro: string; items: NamedStatement[] } }
  | { kind: "palette"; data: PalettePayload }
  | { kind: "typography"; data: TypographyPayload }
  | { kind: "logo"; data: LogoPayload }
  | { kind: "audience"; data: { read: string; personas: Persona[] } }
  | { kind: "market"; data: MarketPayload }
  | { kind: "competition"; data: CompetitionPayload }
  | { kind: "differentiation"; data: DifferentiationPayload }
  | { kind: "offering"; data: OfferingPayload }
  | { kind: "lookfeel"; data: LookFeelPayload }
  | { kind: "packaging"; data: PackagingPayload }
  | { kind: "marketing"; data: MarketingPayload }
  | { kind: "social"; data: SocialPayload }
  | { kind: "website"; data: WebsiteCopyPayload }
  | { kind: "expose"; data: ExposePayload };

/* ------------------------------------------------------------------ */
/* Session state                                                       */
/* ------------------------------------------------------------------ */

export interface Variant {
  id: string;
  round: number;
  seed: number;
  createdAt: string;
  payload: DeliverablePayload;
}

export type DeliverableStatus = "empty" | "draft" | "approved";

export interface DeliverableState {
  status: DeliverableStatus;
  variants: Variant[];
  activeVariantId: string | null;
  approvedVariantId: string | null;
  approvedAt: string | null;
  note: string;
}

export interface Ballot {
  participant: string;
  picks: string[];
  favourite: string | null;
}

export interface WorkshopState {
  stepIndex: number;
  answers: Record<string, string>;
  ballots: Ballot[];
  revealed: boolean;
}

export interface Project {
  id: string;
  brief: Brief;
  workshop: WorkshopState;
  deliverables: Record<DeliverableId, DeliverableState>;
  createdAt: string;
  updatedAt: string;
}

/* ------------------------------------------------------------------ */
/* Payload kinds                                                       */
/* ------------------------------------------------------------------ */

/** Every shape a generated round can take. */
export type PayloadKind = DeliverablePayload["kind"];

/** The `data` carried by one payload kind. */
export type PayloadOf<K extends PayloadKind> = Extract<DeliverablePayload, { kind: K }>["data"];

/*
 * Two things render a payload — the studio screen and the exported Markdown —
 * and they live apart so the Markdown builder stays free of React. Each
 * declares its registry as `{ [K in PayloadKind]: … }`, so the compiler, not a
 * reviewer, notices when a new kind reaches only one of them.
 */
