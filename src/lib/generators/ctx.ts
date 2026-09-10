import { makeRng, pick, sample, shuffle, type Rng } from "@/lib/rng";
import type { Brief } from "@/lib/types";

export interface Ctx {
  brief: Brief;
  rng: Rng;
  /** A workshop answer, trimmed, or "" if the client skipped it. */
  a: (id: string) => string;
  /** A workshop answer as a first-person sentence fragment, safely lowercased. */
  frag: (id: string) => string;
  pick: <T>(items: readonly T[]) => T;
  sample: <T>(items: readonly T[], n: number) => T[];
  shuffle: <T>(items: readonly T[]) => T[];
}

export function makeCtx(brief: Brief, seed: number): Ctx {
  const rng = makeRng(seed);
  const a = (id: string) => (brief.answers[id] ?? "").trim();
  return {
    brief,
    rng,
    a,
    frag: (id: string) => {
      const value = a(id);
      if (!value) return "";
      const oneLine = value.replace(/\s+/g, " ").replace(/[.!?]+$/, "");
      return oneLine.charAt(0).toLowerCase() + oneLine.slice(1);
    },
    pick: (items) => pick(rng, items),
    sample: (items, n) => sample(rng, items, n),
    shuffle: (items) => shuffle(rng, items),
  };
}

export function fill(template: string, values: Record<string, string>): string {
  return template.replace(/\{(\w+)\}/g, (_, key: string) => values[key] ?? "");
}

/** Sentence case without mangling proper nouns already in the string. */
export function sentence(text: string): string {
  const trimmed = text.trim();
  if (!trimmed) return "";
  const withStop = /[.!?]$/.test(trimmed) ? trimmed : `${trimmed}.`;
  return withStop.charAt(0).toUpperCase() + withStop.slice(1);
}

export function list(items: string[], conjunction = "and"): string {
  if (items.length === 0) return "";
  if (items.length === 1) return items[0];
  if (items.length === 2) return `${items[0]} ${conjunction} ${items[1]}`;
  return `${items.slice(0, -1).join(", ")} ${conjunction} ${items[items.length - 1]}`;
}

export const STANCE_WORDS: Record<Brief["priceStance"], { adj: string; note: string; band: string }> = {
  accessible: { adj: "accessible", note: "priced to be the default choice rather than the careful one", band: "£" },
  considered: { adj: "considered", note: "more than the cheap option, and the buyer can say why", band: "££" },
  premium: { adj: "premium", note: "top of the normal range, sold on craft and outcome", band: "£££" },
  luxury: { adj: "luxury", note: "priced as part of the signal, with scarcity kept deliberate", band: "££££" },
};
