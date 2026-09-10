/**
 * Human tone check.
 *
 * The brief is explicit that the brand story must read like a person wrote it.
 * The reliable way to hold that line in a generator is to name the words that
 * give the game away and refuse to ship them. Every string the studio produces
 * runs through `findJargon` before it renders, and the studio shows the result
 * on screen rather than hiding it.
 */

/** Words and phrases that make copy sound machine-written or hollow. */
export const JARGON = [
  // machine tells
  "ai-powered",
  "ai-driven",
  "powered by ai",
  "leverage",
  "leveraging",
  "utilise",
  "utilize",
  "delve",
  "in today's fast-paced world",
  "in the ever-evolving",
  "ever-evolving",
  "tapestry",
  "testament to",
  "it's important to note",
  "navigating the landscape",
  "at the end of the day",
  "when it comes to",
  // consultancy filler
  "synergy",
  "synergies",
  "best-in-class",
  "world-class",
  "cutting-edge",
  "state-of-the-art",
  "game-changing",
  "game changer",
  "revolutionise",
  "revolutionize",
  "disrupt the industry",
  "paradigm shift",
  "holistic solution",
  "end-to-end solution",
  "value-add",
  "value proposition",
  "core competency",
  "move the needle",
  "low-hanging fruit",
  "circle back",
  "unlock the power",
  "unlock your",
  "elevate your brand",
  "take it to the next level",
  "seamless experience",
  "seamlessly integrate",
  "robust solution",
  "bespoke solutions",
  "curated selection of",
  "passionate about delivering",
  "we pride ourselves",
  "one-stop shop",
  "thought leader",
  "innovative solutions",
  "solutions provider",
  "empowering businesses",
  "transformative journey",
  "embark on a journey",
  "unparalleled",
  "unrivalled",
  "unrivaled",
] as const;

export interface JargonHit {
  phrase: string;
  index: number;
}

export function findJargon(text: string): JargonHit[] {
  const haystack = text.toLowerCase();
  const hits: JargonHit[] = [];
  for (const phrase of JARGON) {
    let from = 0;
    for (;;) {
      const index = haystack.indexOf(phrase, from);
      if (index === -1) break;
      // Only count whole words, so "value" never trips "value-add".
      const before = index === 0 ? " " : haystack[index - 1];
      const afterIndex = index + phrase.length;
      const after = afterIndex >= haystack.length ? " " : haystack[afterIndex];
      if (!/[a-z0-9]/.test(before) && !/[a-z0-9]/.test(after)) {
        hits.push({ phrase, index });
      }
      from = index + phrase.length;
    }
  }
  return hits.sort((a, b) => a.index - b.index);
}

export function isHumanTone(text: string): boolean {
  return findJargon(text).length === 0;
}

/**
 * Paths that are supposed to contain bad language.
 *
 * A voice guideline is only useful if it shows the wrong version next to the
 * right one, and a banned-words list has to name the banned words. Both are
 * quotations, so the audit steps over them by path rather than pretending they
 * are not there.
 */
export const QUOTED_PATHS = /(^|\.)(counterExample|bannedWords)(\.|\[|$)/;

export interface AuditOptions {
  /** Return true to skip a path. Defaults to the quotation paths above. */
  ignore?: (path: string) => boolean;
}

export interface AuditHit {
  path: string;
  hit: JargonHit;
}

/** Walks any generated payload and collects every jargon hit it can find. */
export function auditStrings(value: unknown, options: AuditOptions = {}): AuditHit[] {
  const ignore = options.ignore ?? ((path: string) => QUOTED_PATHS.test(path));
  const out: AuditHit[] = [];

  const walk = (node: unknown, path: string) => {
    if (ignore(path)) return;
    if (typeof node === "string") {
      for (const hit of findJargon(node)) out.push({ path, hit });
      return;
    }
    if (Array.isArray(node)) {
      node.forEach((item, i) => walk(item, `${path}[${i}]`));
      return;
    }
    if (node && typeof node === "object") {
      for (const [key, child] of Object.entries(node)) {
        walk(child, path ? `${path}.${key}` : key);
      }
    }
  };

  walk(value, "");
  return out;
}

/**
 * Sentence-length check. Long average sentences are the other tell — real
 * brand writing runs short and varies.
 */
export function averageSentenceLength(text: string): number {
  const sentences = text
    .split(/(?<=[.!?])\s+/)
    .map((s) => s.trim())
    .filter(Boolean);
  if (sentences.length === 0) return 0;
  const words = sentences.reduce((n, s) => n + s.split(/\s+/).filter(Boolean).length, 0);
  return words / sentences.length;
}
