import type { ModulePayload } from "@/lib/types";
import { CTA_VERBS, KICKERS } from "@/data/language";
import { sentence, STANCE_WORDS, type Ctx } from "./ctx";

/* ---------------------------------------------------------------- */
/* Packaging                                                         */
/* ---------------------------------------------------------------- */

export function generatePackaging(ctx: Ctx): ModulePayload {
  const stance = ctx.brief.priceStance;

  const substrates: Record<typeof stance, string[]> = {
    accessible: ["300gsm uncoated white board, single-wall", "Recycled kraft, 250gsm, printed one colour", "Corrugated E-flute, unbleached"],
    considered: ["350gsm uncoated, 1/1 litho", "GF Smith Colorplan 270gsm", "Recycled board with a matte laminate on the outer only"],
    premium: ["Colorplan 540gsm, duplexed", "Rigid board, cloth-wrapped spine", "GF Smith Extract 380gsm, blind deboss"],
    luxury: ["Rigid set-up box, 1200 micron greyboard", "Fedrigoni Sirio Ultra Black 700gsm", "Hand-wrapped Japanese book cloth"],
  };

  const finishes: Record<typeof stance, string[]> = {
    accessible: ["No finish. The unprinted stock is the finish.", "Single-colour print, no varnish."],
    considered: ["Soft-touch matte on the lid only", "Spot UV on the mark, nothing else"],
    premium: ["Blind deboss on the mark, matte lamination", "Single foil in the primary, held to one impression"],
    luxury: ["Hot foil in matte black, edge-painted", "Deboss plus a single metallic foil, both under 40mm"],
  };

  return {
    kind: "packaging",
    data: {
      substrate: ctx.pick(substrates[stance]),
      finish: ctx.pick(finishes[stance]),
      unboxingNote: ctx.pick([
        "One reveal, not three. The lid comes off and the product is there — no tissue theatre, no card to fish out first.",
        "The message is printed on the inside of the lid, where it is read once and remembered. Nothing else inside carries type.",
        "Everything is one material. The saving from dropping the second substrate goes into a heavier board.",
      ]),
      items: [
        {
          name: "Primary carton",
          format: ctx.pick(["220 × 140 × 60mm", "180 × 180 × 45mm", "300 × 200 × 80mm"]),
          spec: [
            "Front panel: mark only, optically centred, at 25% of panel width.",
            "Back panel: contents, origin and one line of brand copy. Nothing else.",
            "Side panels: left carries the wordmark, right carries the legal block at Caption level.",
            "Base: batch code, recycling marks, and the site address.",
          ],
          copy: { headline: ctx.brief.brandName || "Brand", sub: ctx.brief.offering ? ctx.brief.offering.slice(0, 60) : undefined },
        },
        {
          name: "Sleeve",
          format: ctx.pick(["Wrap, 40mm deep", "Belly band, 60mm", "Full sleeve with a 15mm window"]),
          spec: [
            "Prints in the primary, knocked out to paper.",
            "One line of copy across the wrap, set at Headline level, tracked to the display value.",
            "Glue tab on the reverse — never on the face.",
          ],
          copy: { headline: ctx.pick(["Made properly.", "One job, done once.", "Open here.", "Nothing hidden."]) },
        },
        {
          name: "Insert card",
          format: "A6, 350gsm, printed 1/1",
          spec: [
            "Front: one sentence, set at Subheading level, centred with a 12mm margin.",
            "Reverse: what to do next, in three numbered lines.",
            "No QR code on the front face. If one is needed it goes bottom-right on the reverse.",
          ],
          copy: { headline: ctx.pick(["Thanks. Here is what happens next.", "You are set up.", "Start here."]) },
        },
        {
          name: "Shipping outer",
          format: ctx.pick(["Mailer box, E-flute", "Rigid mailer, 240 × 160mm"]),
          spec: [
            "Unprinted outside except for a 30mm mark in the primary, bottom-left.",
            "Interior printed full-bleed in the primary — the colour is the reveal.",
            "Tear strip aligned to the grid, never diagonal.",
          ],
        },
      ],
    },
  };
}

/* ---------------------------------------------------------------- */
/* Marketing material                                                */
/* ---------------------------------------------------------------- */

export function generateMarketing(ctx: Ctx): ModulePayload {
  const name = ctx.brief.brandName || "The brand";
  const headlines = [
    ctx.a("hard-truth") ? shorten(ctx.a("hard-truth")) : `${name}. One job, done properly.`,
    ctx.pick([`The version of this that tells you the truth.`, `Everything in one place. Finally.`, `Ask us the awkward question.`, `Built by the people who do the work.`]),
    ctx.pick([`Start with one.`, `No tie-in. No small print.`, `The price is on the website.`, `Twenty minutes, then decide.`]),
  ];

  return {
    kind: "marketing",
    data: {
      layouts: [
        {
          name: "48-sheet billboard",
          ratio: "6096 × 3048mm (2:1)",
          grid: "Three columns. Type occupies the left two, image bleeds off the right.",
          hierarchy: ["Headline at Display level, maximum six words", "Mark bottom-left at 8% of the height", "No body copy — a billboard has three seconds"],
          headline: headlines[0],
          sub: "",
        },
        {
          name: "Editorial spread",
          ratio: "420 × 297mm, A3 landscape",
          grid: "Four-column editorial grid, wide outer margin, text held to three columns.",
          hierarchy: ["Kicker at Label level", "Headline at Headline level, ranged left", "Two columns of body at 16px / 1.6", "Full-bleed image on the right page only"],
          headline: headlines[1],
          sub: ctx.brief.offering || "What this is, in one paragraph, without a sales voice.",
        },
        {
          name: "Conference stand",
          ratio: "3000 × 2250mm backwall",
          grid: "Single field of primary colour. One element per third.",
          hierarchy: ["Mark at eye height, 1600mm from the floor", "Headline above at Display level", "One line of contact detail at Label level, low right"],
          headline: headlines[2],
          sub: "",
        },
        {
          name: "Direct mail",
          ratio: "DL, 210 × 99mm, 350gsm",
          grid: "Two panels: a colour field and a paper field, split on the vertical centre.",
          hierarchy: ["Colour panel carries the headline only", "Paper panel carries three lines of copy and one action", "Mark on the reverse, bottom-right"],
          headline: ctx.pick(["Open this one.", "Two minutes.", "About the thing you keep meaning to sort out."]),
          sub: "One paragraph, then a single action. Two actions on a mailer is zero actions.",
        },
        {
          name: "Digital display set",
          ratio: "300×250, 728×90, 160×600, 1080×1080",
          grid: "Safe area of 8% on every edge. The mark never crosses it.",
          hierarchy: ["Headline, maximum eight words", "One action, always the same words across the set", "Mark, fixed corner across all sizes"],
          headline: headlines[1],
          sub: "",
        },
      ],
      rules: [
        "Margins come from the layout rule: longest side divided by 25. It scales from a DL card to a 48-sheet without being re-decided.",
        "One idea per surface. If a layout has two headlines it has none.",
        "Colour blocks are full-bleed or they are not there. Half a colour field reads as a mistake.",
        `The mark sits in a fixed corner across a campaign. Moving it between formats costs recognition and buys nothing.`,
      ],
    },
  };
}

function shorten(text: string): string {
  const clean = text.replace(/\s+/g, " ").trim();
  const first = clean.split(/(?<=[.!?])\s/)[0];
  return first.length > 68 ? `${first.slice(0, 64).trim()}…` : first;
}

/* ---------------------------------------------------------------- */
/* Social media                                                      */
/* ---------------------------------------------------------------- */

export function generateSocial(ctx: Ctx): ModulePayload {
  const name = ctx.brief.brandName || "brand";
  const handle = `@${name.toLowerCase().replace(/[^a-z0-9]/g, "")}`;

  const pillars = ctx.shuffle([
    { name: "The work", share: 40, example: "One project, one decision, shown properly. Before, the constraint, and after." },
    { name: "Straight answers", share: 25, example: "The awkward customer question, answered in full, in public." },
    { name: "How it is made", share: 20, example: "Process, tools, mistakes. The bench, not the showroom." },
    { name: "The people", share: 15, example: "Who did it and why they cared. Names and faces, never stock." },
    { name: "Proof", share: 20, example: "A real number with the method next to it." },
    { name: "The category", share: 15, example: "Something true about the industry that others will not say." },
  ]).slice(0, 4);

  const total = pillars.reduce((n, p) => n + p.share, 0);
  const normalised = pillars.map((p) => ({ ...p, share: Math.round((p.share / total) * 100) }));

  return {
    kind: "social",
    data: {
      handle,
      bio: sentence(
        ctx.brief.offering
          ? `${ctx.brief.offering.replace(/^we\s+/i, "").replace(/[.]$/, "")}. ${ctx.brief.location || ""}`.trim()
          : `${name}. One job, done properly. ${ctx.brief.location || ""}`.trim(),
      ),
      pillars: normalised,
      posts: [
        {
          format: "Single image, 4:5",
          headline: ctx.pick(["Master Your Retirement. Simply.", "One screen. Everything you own.", "The bit nobody explains."]),
          caption: sentence(
            `${ctx.a("objection") ? `“${shorten(ctx.a("objection"))}” — the honest answer` : "The question we get most, answered properly"}. ` +
            "No link in this one. Just the answer",
          ),
        },
        {
          format: "Data card, 1:1",
          headline: ctx.pick(["Stop Tracking. Start Deciding.", "Four accounts. One number.", "What it actually costs."]),
          caption: "One chart, one sentence under it, method in the first comment. If the number needs three sentences of context it is the wrong number.",
        },
        {
          format: "Carousel, 4:5, six frames",
          headline: ctx.pick(["Your Net Worth. Live.", "How this actually works", "Six things nobody tells you"]),
          caption: "Frame one is the whole idea. Frames two to five are the working. Frame six is what to do next — and only frame six has an action on it.",
        },
      ],
      bannerNote: sentence(
        "Page banner: primary colour field, mark ranged left at 40% of banner height, one line of copy at Subheading level. " +
        "Safe area is tighter than the platform claims — keep everything 12% in from every edge so nothing is cropped on mobile",
      ),
      cadence: ctx.pick([
        "Three posts a week, one of them the awkward one. Better to hold at three than promise five and drop to one.",
        "Two a week, every week, for a year. Consistency beats volume in a category where nobody posts at all.",
        "Four a week: two work, one answer, one person. Reviewed monthly against the pillars, not against the likes.",
      ]),
    },
  };
}

/* ---------------------------------------------------------------- */
/* Website copy                                                      */
/* ---------------------------------------------------------------- */

export function generateWebsite(ctx: Ctx): ModulePayload {
  const name = ctx.brief.brandName || "Brand";
  const stance = STANCE_WORDS[ctx.brief.priceStance];

  const heroHeadline = ctx.pick([
    ctx.brief.offering ? titleish(ctx.brief.offering) : `${name}. One job, done properly.`,
    ctx.a("hard-truth") ? shorten(ctx.a("hard-truth")) : "The straightforward version of a complicated thing.",
    `Everything in one place. Nothing hidden.`,
    `Built by the people who do the work.`,
  ]);

  return {
    kind: "website",
    data: {
      navigation: ctx.pick([
        ["Approach", "What you get", "Pricing", "About", "Contact"],
        ["How it works", "Who it is for", "Pricing", "Journal", "Talk to us"],
        ["Work", "Services", "Studio", "Prices", "Get in touch"],
      ]),
      hero: {
        eyebrow: ctx.brief.sector ? ctx.brief.sector : "Independent",
        headline: heroHeadline,
        sub: sentence(
          ctx.brief.audienceNote
            ? `For ${ctx.brief.audienceNote.replace(/^\s*/, "").replace(/[.]$/, "").toLowerCase()}`
            : `${stance.note.charAt(0).toUpperCase()}${stance.note.slice(1)}`,
        ),
        primaryCta: ctx.pick(CTA_VERBS),
        secondaryCta: ctx.pick(["See the pricing", "Read how it works", "Look at the work"]),
      },
      sections: [
        {
          kicker: ctx.pick(KICKERS),
          heading: ctx.pick(["What this actually is", "The short version", "Here is the whole thing"]),
          body: sentence(
            ctx.brief.offering
              ? `${ctx.brief.offering.replace(/[.]$/, "")}. That is the entire offer. There is no second product and no upsell waiting at the end`
              : "One service, described in plain words, with the price attached. There is no second product waiting at the end",
          ),
        },
        {
          kicker: "Who it is for",
          heading: ctx.pick(["It suits some people and not others", "This is not for everyone"]),
          body: sentence(
            `${ctx.brief.audienceNote ? sentence(ctx.brief.audienceNote) : "People who would rather pay once and stop thinking about it."} ` +
            `If you want the cheapest option, there are better places to look, and we will point you at them`,
          ),
        },
        {
          kicker: ctx.pick(["What we will not do", "The boundaries"]),
          heading: ctx.pick(["The things we say no to", "What you will not get from us"]),
          body: sentence(
            ctx.a("refuse")
              ? ctx.a("refuse").replace(/[.]$/, "")
              : "No discounting to win work, no overstating what it will do, and no taking on more than we can staff",
          ),
        },
        {
          kicker: "Pricing",
          heading: ctx.pick(["What it costs", "The number, up front"]),
          body: "The price is on this page because a price you have to ask for is a price that changes. Three tiers, one page, no sales call required to see them.",
        },
      ],
      proof: sentence(
        ctx.a("first-client")
          ? `${shorten(ctx.a("first-client"))} — still a customer`
          : "Replace this with one real customer sentence. A specific quote from a named person outperforms any number of logos",
      ),
      footerLine: sentence(
        `${name}${ctx.brief.foundedYear ? `, since ${ctx.brief.foundedYear}` : ""}${ctx.brief.location ? `. ${ctx.brief.location}` : ""}`,
      ),
    },
  };
}

function titleish(text: string): string {
  const clean = text.replace(/^we\s+/i, "").replace(/\s+/g, " ").trim();
  const first = clean.split(/(?<=[.!?])\s/)[0].replace(/[.]$/, "");
  const capped = first.charAt(0).toUpperCase() + first.slice(1);
  return capped.length > 72 ? `${capped.slice(0, 68).trim()}…` : `${capped}.`;
}
