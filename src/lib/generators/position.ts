import type { CompetitorRow, ModulePayload, Persona, PriceStance } from "@/lib/types";
import { ARCHETYPES } from "@/data/language";
import { list, sentence, STANCE_WORDS, type Ctx } from "./ctx";

/* ---------------------------------------------------------------- */
/* Market research                                                   */
/* ---------------------------------------------------------------- */

export function generateMarket(ctx: Ctx): ModulePayload {
  const sector = ctx.brief.sector || "the category";
  const region = ctx.brief.location ? `the ${ctx.brief.location} region` : "the domestic market";

  const size = ctx.pick(["£1.4bn", "£620m", "$4.8bn", "£310m", "$1.1bn"]);
  const growth = ctx.pick(["6.2%", "9.4%", "11.8%", "4.1%", "14.3%"]);
  const buyers = ctx.pick(["2.1m", "480,000", "36,000", "8.9m", "115,000"]);

  const segments = ctx.shuffle([
    { name: "Underserved mainstream", share: 41, note: "Wants the category but finds every existing option either patronising or overpriced." },
    { name: "Switchers", share: 26, note: "Already buying. Unhappy enough to move, not unhappy enough to research." },
    { name: "First-timers", share: 18, note: "Never bought in this category. Needs the whole thing explained without being made to feel stupid." },
    { name: "High-value few", share: 15, note: "Small in number, large in revenue, expensive to serve and worth it." },
  ]).slice(0, 4);

  return {
    kind: "market",
    data: {
      headline: ctx.pick([
        `${sector} is growing faster than its reputation suggests`,
        `The money in ${sector} is moving, and it is moving away from the incumbents`,
        `${sector} has a demand problem and a trust problem, and only one of them is being solved`,
      ]),
      summary: sentence(
        `${sector} in ${region} is worth roughly ${size} and growing at about ${growth} a year. ` +
        `The interesting number is not the size, it is the churn — ${buyers} buyers are in the market at any point, and most of them ` +
        ctx.pick([
          "are choosing between options they do not fully understand",
          "pick on price because nothing else is legible to them",
          "stay put out of inertia rather than satisfaction",
        ]),
      ),
      figures: [
        { value: size, label: "Market size", note: `Addressable spend across ${sector} in ${region}.` },
        { value: growth, label: "Annual growth", note: "Compound, three-year trailing. Faster than the wider economy." },
        { value: buyers, label: "Active buyers", note: "In market at any given point in the year." },
        { value: ctx.pick(["31%", "44%", "58%", "22%"]), label: "Switch intent", note: "Say they would change supplier if the alternative were clearer." },
      ],
      segments,
      shifts: ctx.sample([
        "Buyers now research alone and arrive late. By the time they make contact the decision is largely made.",
        "Price transparency has become table stakes. Hiding the number now reads as a red flag rather than a negotiating position.",
        "The incumbents are consolidating, which is opening a gap at the specialist end.",
        "Trust is being earned in public — reviews, forums and word of mouth outweigh anything the brand says about itself.",
        "Regulation is tightening, and the brands that get ahead of it are using it as proof rather than treating it as cost.",
        "Attention has moved to short video, and the categories that refused to show up there have quietly lost a generation.",
      ], 4),
      sources: [
        "Studio desk research — replace with the client's own analyst reports before this goes to the board.",
        "Customer interviews from the discovery phase.",
        "Public filings and pricing pages of the named competitors.",
      ],
    },
  };
}

/* ---------------------------------------------------------------- */
/* Audience                                                          */
/* ---------------------------------------------------------------- */

const FIRST_NAMES_A = ["Marcus", "Priya", "Robert", "Nadia", "Tom", "Elena", "Femi", "Claire"];
const FIRST_NAMES_B = ["Sarah", "James", "Aisha", "Daniel", "Ruth", "Omar", "Hannah", "Leo"];

const ARCHETYPE_LABELS_A = [
  "The High-Performing Leader", "The Careful Planner", "The Reluctant Buyer",
  "The Time-Poor Decider", "The First-Generation Owner",
];
const ARCHETYPE_LABELS_B = [
  "The Competitive Optimiser", "The Quiet Researcher", "The Sceptical Switcher",
  "The Delegator", "The Late Convert",
];

export function generateAudience(ctx: Ctx): ModulePayload {
  const note = ctx.a("audience");
  const before = ctx.frag("before");
  const objection = ctx.a("objection");
  const moment = ctx.frag("moment");

  const personaOne = buildPersona(ctx, {
    name: ctx.pick(FIRST_NAMES_A),
    label: ctx.pick(ARCHETYPE_LABELS_A),
    seedNote: note,
    before,
    objection,
    moment,
    primary: true,
  });

  const personaTwo = buildPersona(ctx, {
    name: ctx.pick(FIRST_NAMES_B),
    label: ctx.pick(ARCHETYPE_LABELS_B),
    seedNote: "",
    before,
    objection,
    moment,
    primary: false,
  });

  const read = note
    ? sentence(`The client describes their best customer as ${note.replace(/^\s*/, "").replace(/[.]$/, "").toLowerCase()}. Two personas below take that description and make it usable — one is the buyer we already win, the other is the one we keep losing.`)
    : "Two personas: the buyer this brand already wins, and the one it keeps losing. Design decisions get tested against both.";

  return { kind: "audience", data: { read, personas: [personaOne, personaTwo] } };
}

function buildPersona(
  ctx: Ctx,
  input: { name: string; label: string; seedNote: string; before: string; objection: string; moment: string; primary: boolean },
): Persona {
  const traits = ctx.sample(ctx.brief.traits.length >= 5 ? ctx.brief.traits : [
    "Careful", "Busy", "Sceptical", "Well-informed", "Loyal once convinced",
  ], 5);

  return {
    name: input.name,
    archetypeLabel: input.label,
    traits: input.primary
      ? traits.map(humaniseTrait)
      : ctx.sample(["Impatient", "Price-aware", "Comparison-driven", "Risk-averse", "Peer-influenced", "Detail-hungry"], 5),
    age: input.primary ? ctx.pick(["34 to 48", "45 to 60", "28 to 39", "50 to 65"]) : ctx.pick(["25 to 35", "38 to 52", "30 to 44"]),
    location: ctx.brief.location ? `${ctx.brief.location} and the surrounding commuter belt` : ctx.pick(["Major cities", "London, Manchester, Bristol", "Nationwide, skewing urban"]),
    alsoBuys: ctx.sample(["Apple", "Patagonia", "Monzo", "Ōura", "Aesop", "Tesla", "Whoop", "IKEA", "Muji", "Garmin"], 4),
    motivations: ctx.sample([
      input.seedNote ? sentence(input.seedNote) : "Wants the decision made properly once, not revisited every year.",
      "Would rather pay more than be sold to.",
      "Is protecting time more than money.",
      "Wants to look competent in front of someone whose opinion matters.",
      "Has been burned before and is quietly checking for the catch.",
      "Wants the thing handled without becoming an expert in it.",
    ], 4),
    challenges: ctx.sample([
      input.before ? sentence(`Currently coping with ${input.before}`) : "Coping with a workaround that mostly works.",
      "Too many options, none of them comparable on the same terms.",
      "Cannot tell the difference between the good and the merely confident.",
      "No time to research properly, and no appetite for a sales process.",
      "Has to justify the spend to someone who was not in the conversation.",
    ], 4),
    whyThisBrand: ctx.sample([
      input.moment ? sentence(`It clicks when ${input.moment}`) : "It clicks the first time something arrives already done.",
      "Straight answers, including the unflattering ones.",
      "The price is on the website.",
      "Feels built by people who have done the job, not people who sell the job.",
      "Small enough to reach someone who can actually decide.",
    ], 4),
    buyingBehaviour: ctx.sample([
      "Researches alone for weeks, then contacts one supplier ready to buy.",
      input.objection ? `Raises one objection early: "${input.objection.replace(/^"|"$/g, "")}"` : "Raises the price objection early and is testing the answer, not the number.",
      "Reads the terms. Actually reads them.",
      "Asks a peer before asking the company.",
      "Will pay annually for a discount, but will not sign a three-year term.",
    ], 4),
    socialBehaviour: ctx.sample([
      "LinkedIn for credibility checks, nothing else.",
      "Lurks in one specialist forum and trusts it more than any brand.",
      "Watches short video, never comments.",
      "Subscribes to two newsletters and reads one.",
      "Screenshots things to send to a WhatsApp group before deciding.",
    ], 3),
  };
}

function humaniseTrait(trait: string): string {
  const map: Record<string, string> = {
    Premium: "Buys at the top of the range", Minimal: "Wants fewer options, better explained",
    Bold: "Moves fast once convinced", Trusted: "Checks references first",
    Precise: "Notices the detail you got wrong", Warm: "Buys from people they like",
    Systematic: "Wants a process, in writing", Innovative: "Tries the new thing early",
  };
  return map[trait] ?? trait;
}

/* ---------------------------------------------------------------- */
/* Competition                                                       */
/* ---------------------------------------------------------------- */

export function generateCompetition(ctx: Ctx): ModulePayload {
  const named = ctx.brief.competitors.filter(Boolean);
  const rivalGood = ctx.a("rival-good");
  const before = ctx.a("before");

  const pool = named.length > 0 ? named : ["The incumbent", "The cheap option", "The in-house build"];
  const stances: PriceStance[] = ["accessible", "considered", "premium", "luxury"];

  const rows: CompetitorRow[] = pool.slice(0, 4).map((name, index) => ({
    name,
    position: ctx.pick([
      "Owns the category default. Most buyers start here.",
      "Wins on price and loses on care.",
      "Best-known name, slowest to change anything.",
      "Newer, sharper, thinner on delivery.",
      "Not a company at all — a habit, a spreadsheet, or doing nothing.",
    ]),
    doesWell: index === 0 && rivalGood ? sentence(rivalGood) : ctx.pick([
      "Onboarding takes minutes and feels effortless.",
      "Distribution. They are simply in more places.",
      "Pricing anyone can understand at a glance.",
      "A support team that answers on the first ring.",
      "Brand recognition earned over twenty years.",
    ]),
    leavesOpen: ctx.pick([
      "Nobody there knows your name after the sale.",
      "Everything is fast and nothing is explained.",
      "The good service is reserved for the biggest accounts.",
      "It works until something goes wrong, and then it does not.",
      "No point of view. They will build whatever you ask for.",
    ]),
    priceBand: stances[(index + (ctx.brief.priceStance === "luxury" ? 2 : 1)) % stances.length],
  }));

  if (before) {
    rows.push({
      name: "Doing nothing",
      position: "The real competitor, and the one that usually wins.",
      doesWell: sentence(`It costs nothing and nobody has to make a decision. Today it looks like ${before.toLowerCase().replace(/[.]$/, "")}`),
      leavesOpen: "It quietly gets more expensive every year, and nobody sends an invoice for it.",
      priceBand: "accessible",
    });
  }

  const axes = ctx.pick([
    { x: ["Mass", "Specialist"] as [string, string], y: ["Cheap", "Expensive"] as [string, string] },
    { x: ["Cautious", "Outspoken"] as [string, string], y: ["Transactional", "Relationship"] as [string, string] },
    { x: ["Legacy", "New"] as [string, string], y: ["Self-serve", "Done for you"] as [string, string] },
  ]);

  const plot = rows.map((row, i) => ({
    name: row.name,
    x: 0.15 + ((i * 0.27 + ctx.rng() * 0.12) % 0.7),
    y: 0.15 + ((i * 0.19 + ctx.rng() * 0.18) % 0.7),
    isUs: false,
  }));

  plot.push({
    name: ctx.brief.brandName || "Us",
    x: ctx.brief.priceStance === "luxury" || ctx.brief.priceStance === "premium" ? 0.76 : 0.34,
    y: ctx.brief.priceStance === "luxury" ? 0.82 : ctx.brief.priceStance === "premium" ? 0.7 : 0.38,
    isUs: true,
  });

  return {
    kind: "competition",
    data: {
      rows,
      axes,
      plot,
      read: sentence(
        rows.length > 0
          ? `Everyone in this list is competent. The gap is not capability, it is ${ctx.pick([
              "the willingness to say something specific",
              "care after the sale",
              "being legible to a buyer who is not an expert",
              "having a point of view worth disagreeing with",
            ])}`
          : "Name the rivals in discovery and this map becomes useful. Without names it is decoration",
      ),
    },
  };
}

/* ---------------------------------------------------------------- */
/* Differentiation                                                   */
/* ---------------------------------------------------------------- */

export function generateDifferentiation(ctx: Ctx): ModulePayload {
  const stance = STANCE_WORDS[ctx.brief.priceStance];
  const traits = ctx.brief.traits;
  const hardTruth = ctx.a("hard-truth");
  const refuse = ctx.a("refuse");
  const firstClient = ctx.a("first-client");

  const claim = ctx.pick([
    `The only ${ctx.brief.sector || "supplier"} that will tell you when you do not need us.`,
    `${ctx.brief.brandName || "We"} ${stance.adj === "luxury" ? "sell less on purpose" : "do one job properly and refuse the rest"}.`,
    `Built by the people who do the work, not the people who sell it.`,
    `The answer arrives before the invoice does.`,
  ]);

  return {
    kind: "differentiation",
    data: {
      claim: sentence(claim),
      proofs: [
        {
          proof: ctx.pick(["We publish the price.", "We answer in a working day.", "We name the alternative when it is better.", "The person who sold it stays on the account."]),
          evidence: hardTruth ? sentence(`Backed in practice: "${hardTruth.replace(/^"|"$/g, "")}"`) : "Needs a real number from the client before this goes public. A claim without evidence becomes a liability at the first bad review.",
        },
        {
          proof: refuse ? sentence(`We hold a line others do not — ${refuse.toLowerCase().replace(/[.]$/, "")}`) : "We turn down work we cannot staff.",
          evidence: "A refusal is the cheapest proof there is. It costs revenue, which is exactly why it is believed.",
        },
        {
          proof: firstClient ? "The first customer is still a customer." : `Personality the category does not have: ${ctx.brief.traits.slice(0, 2).join(" and ").toLowerCase() || "an opinion"}.`,
          evidence: firstClient ? sentence(firstClient) : "Traits chosen in the workshop, filtered to remove anything a competitor could equally claim.",
        },
      ],
      onlyWeCan: sentence(
        traits.length > 0
          ? `Being ${list(traits.slice(0, 3).map((t) => t.toLowerCase()))} at once is the position. Any one of those alone is available to anyone in ${ctx.brief.sector || "the category"}`
          : "Run the personality exercise to fill this in properly",
      ),
      notFor: [
        ctx.pick(["Buyers who need the cheapest option.", "Anyone shopping on price alone.", "Organisations that want a supplier, not a partner."]),
        ctx.pick(["Projects that need to start next week.", "Work that requires overstating the outcome.", "Clients who will not share the numbers."]),
        ctx.pick(["People who want the category's usual theatre.", "Anyone who wants us to say what everyone else says.", "Teams looking to tick a box."]),
      ],
    },
  };
}

/* ---------------------------------------------------------------- */
/* Brand exposé                                                      */
/* ---------------------------------------------------------------- */

export function generateExpose(ctx: Ctx): ModulePayload {
  const { brandName, sector, location, foundedYear } = ctx.brief;
  const archetype = ctx.pick(ARCHETYPES.filter((a) => a.traits.some((t) => ctx.brief.traits.includes(t))).length > 0
    ? ARCHETYPES.filter((a) => a.traits.some((t) => ctx.brief.traits.includes(t)))
    : ARCHETYPES);

  const oneLiner = sentence(
    `${brandName || "The company"} is ${aOrAn(sector || "a company")}${sector ? ` company` : ""} in ${location || "the UK"} ` +
    (ctx.brief.offering ? `that ${ctx.brief.offering.replace(/^we\s+/i, "").replace(/[.]$/, "")}` : "with one job and no intention of adding a second"),
  );

  return {
    kind: "expose",
    data: {
      oneLiner,
      paragraphs: [
        sentence(
          `Founded ${foundedYear ? `in ${foundedYear}` : "a few years ago"}${location ? ` in ${location}` : ""}, ${brandName || "the company"} ` +
          (ctx.a("annoyed") ? `exists because ${ctx.frag("annoyed")}` : "exists because the obvious version of this did not exist yet"),
        ),
        sentence(
          `It behaves like ${archetype.name.toLowerCase()}: ${archetype.behaves.toLowerCase().replace(/[.]$/, "")}. ` +
          `What it will not do is as defined as what it will — ${ctx.a("refuse") ? ctx.frag("refuse") : "no discounting, no overstating, no work it cannot staff"}`,
        ),
        sentence(
          ctx.a("moment")
            ? `Customers tend to describe the same turning point: ${ctx.frag("moment")}`
            : "Customers tend to describe the same turning point — the first time something arrived already handled",
        ),
      ],
      atAGlance: [
        { label: "Founded", value: foundedYear || "—" },
        { label: "Based", value: location || "—" },
        { label: "Sector", value: sector || "—" },
        { label: "Price position", value: STANCE_WORDS[ctx.brief.priceStance].adj },
        { label: "Archetype", value: `${archetype.name} × ${ctx.pick(archetype.pairsWith)}` },
        { label: "Personality", value: ctx.brief.traits.slice(0, 5).join(", ") || "—" },
      ],
      pressLine: sentence(
        `For press enquiries and brand assets, contact ${brandName ? `${brandName.toLowerCase().replace(/\s+/g, "")}` : "the studio"}. ` +
        "Logo files, colour values and type licences are in the brand hub",
      ),
    },
  };
}

function aOrAn(word: string): string {
  return /^[aeiou]/i.test(word.trim()) ? "an " : "a ";
}
