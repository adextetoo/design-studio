import { test } from "node:test";
import assert from "node:assert/strict";

import { generate, seedFor } from "../src/lib/generators/index.ts";
import { MODULE_IDS } from "../src/data/modules.ts";
import { auditStrings, findJargon, averageSentenceLength } from "../src/lib/jargon.ts";
import type { Brief } from "../src/lib/types.ts";

const BRIEFS: Brief[] = [
  {
    brandName: "Monty",
    offering: "We help people close to retirement see everything they own in one place.",
    sector: "Personal finance",
    location: "Manchester",
    foundedYear: "2021",
    audienceNote: "Fifty-eight, sold a business, does not trust anyone in a suit.",
    competitors: ["Nutmeg", "Moneybox", "A spreadsheet"],
    priceStance: "considered",
    traits: ["Trusted", "Human", "Precise", "Transparent", "Warm"],
    fontClass: "geometric-sans",
    answers: {
      "why-name": "It was my grandfather's name. He kept every receipt he ever got.",
      annoyed: "Everyone I knew had four pensions and no idea what any of them were worth.",
      "first-client": "A retiring teacher. She said we were the first people who did not talk down to her.",
      "ten-years": "Forty people, still one product, known for being the honest one",
      refuse: "We will never sell customer data",
      success: "Our sales team stops apologising for the website.",
      before: "A spreadsheet their son-in-law built in 2016",
      objection: "It sounds expensive for something I could probably do myself.",
      moment: "the first time they open the app and every account is already there",
      "rival-good": "Their onboarding takes four minutes. Ours takes twenty.",
      "hard-truth": "If you are under forty, you probably do not need us yet.",
      "never-say": "solutions\nleverage\njourney",
      "sound-like": "The way a good GP explains a diagnosis",
    },
  },
  {
    brandName: "Titan Arc",
    offering: "Concierge longevity care for people who refuse to leave their healthspan to chance.",
    sector: "Preventative healthcare",
    location: "London",
    foundedYear: "2024",
    audienceNote: "Forty-five, runs a fund, measures everything.",
    competitors: ["Bupa", "Numan"],
    priceStance: "luxury",
    traits: ["Premium", "Precise", "Visionary", "Proven", "Refined"],
    fontClass: "didone-serif",
    answers: {},
  },
  {
    // Deliberately empty — the generators must not fall over on a blank brief.
    brandName: "",
    offering: "",
    sector: "",
    location: "",
    foundedYear: "",
    audienceNote: "",
    competitors: [],
    priceStance: "accessible",
    traits: [],
    fontClass: "technical-mono",
    answers: {},
  },
];

test("every module generates for every brief across many rounds", () => {
  for (const brief of BRIEFS) {
    for (const id of MODULE_IDS) {
      for (let round = 0; round < 12; round += 1) {
        const payload = generate(brief, id, seedFor(brief, id, round));
        assert.ok(payload && typeof payload.kind === "string", `${id} round ${round} produced nothing`);
      }
    }
  }
});

test("generation is deterministic for a given seed", () => {
  const brief = BRIEFS[0];
  for (const id of MODULE_IDS) {
    const seed = seedFor(brief, id, 3);
    assert.deepEqual(generate(brief, id, seed), generate(brief, id, seed), `${id} is not deterministic`);
  }
});

test("refresh actually changes the output", () => {
  const brief = BRIEFS[0];
  for (const id of MODULE_IDS) {
    const rounds = new Set<string>();
    for (let r = 0; r < 8; r += 1) {
      rounds.add(JSON.stringify(generate(brief, id, seedFor(brief, id, r))));
    }
    assert.ok(rounds.size >= 4, `${id} only produced ${rounds.size} distinct rounds out of 8`);
  }
});

test("no generated copy contains AI or consultancy jargon", () => {
  const failures: string[] = [];
  for (const brief of BRIEFS) {
    for (const id of MODULE_IDS) {
      for (let round = 0; round < 10; round += 1) {
        const payload = generate(brief, id, seedFor(brief, id, round));
        for (const { path, hit } of auditStrings(payload)) {
          failures.push(`${id} r${round} ${path}: "${hit.phrase}"`);
        }
      }
    }
  }
  assert.deepEqual(failures, [], `jargon found:\n${failures.slice(0, 20).join("\n")}`);
});

test("the jargon checker itself works", () => {
  assert.equal(findJargon("We leverage cutting-edge synergy").length, 3);
  assert.equal(findJargon("The value of the work is clear.").length, 0, "must not flag 'value' inside ordinary prose");
  assert.equal(findJargon("Our value-add is unclear.").length, 1);
});

test("brand story reads like a person wrote it", () => {
  for (const brief of BRIEFS) {
    for (let round = 0; round < 6; round += 1) {
      const payload = generate(brief, "story", seedFor(brief, "story", round));
      assert.equal(payload.kind, "prose");
      if (payload.kind !== "prose") return;
      const text = payload.data.paragraphs.join(" ");
      const avg = averageSentenceLength(text);
      assert.ok(avg > 4 && avg < 26, `average sentence length ${avg.toFixed(1)} is out of human range`);
      assert.ok(payload.data.paragraphs.length >= 3, "story should run at least three paragraphs");
    }
  }
});

test("palette swatches carry printable and measurable values", () => {
  for (const brief of BRIEFS) {
    const payload = generate(brief, "palette", seedFor(brief, "palette", 0));
    assert.equal(payload.kind, "palette");
    if (payload.kind !== "palette") return;
    for (const s of payload.data.swatches) {
      assert.match(s.hex, /^#[0-9A-F]{6}$/, `${s.name} hex malformed`);
      assert.equal(s.rgb.length, 3);
      assert.equal(s.cmyk.length, 4);
      assert.ok(s.contrastOnWhite >= 1 && s.contrastOnWhite <= 21);
    }
    const ink = payload.data.swatches.find((s) => s.role === "ink");
    assert.ok(ink && ink.contrastOnWhite >= 7, "ink must clear AAA on white — it sets every paragraph");
  }
});

test("typography hierarchy has every level the guide page needs", () => {
  const wanted = ["Display", "Headline", "Subheading", "Body", "Label", "Caption"];
  for (const brief of BRIEFS) {
    const payload = generate(brief, "typography", seedFor(brief, "typography", 1));
    assert.equal(payload.kind, "typography");
    if (payload.kind !== "typography") return;
    assert.deepEqual(payload.data.levels.map((l) => l.level), wanted);
    for (const level of payload.data.levels) {
      assert.match(level.size, /^\d+px$/, `${level.level} size malformed`);
      assert.ok(level.use.length > 10, `${level.level} needs a real usage note`);
    }
  }
});

test("audience always returns two fully populated personas", () => {
  for (const brief of BRIEFS) {
    const payload = generate(brief, "audience", seedFor(brief, "audience", 2));
    assert.equal(payload.kind, "audience");
    if (payload.kind !== "audience") return;
    assert.equal(payload.data.personas.length, 2);
    for (const p of payload.data.personas) {
      assert.ok(p.name && p.archetypeLabel);
      assert.equal(p.traits.length, 5);
      for (const field of ["motivations", "challenges", "whyThisBrand", "buyingBehaviour"] as const) {
        assert.ok(p[field].length >= 3, `${p.name} ${field} is thin`);
      }
    }
  }
});
