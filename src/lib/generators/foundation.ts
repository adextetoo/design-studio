import type { DeliverablePayload } from "@/lib/types";
import {
  MISSION_SHAPES, MISSION_VERBS, STORY_LANDINGS, STORY_OPENERS, STORY_TURNS,
  VISION_SHAPES,
} from "@/data/language";
import { fill, sentence, STANCE_WORDS, type Ctx } from "./ctx";

/* ---------------------------------------------------------------- */
/* Brand Story                                                       */
/* ---------------------------------------------------------------- */

export function generateStory(ctx: Ctx): DeliverablePayload {
  const { brandName, location, foundedYear } = ctx.brief;
  const annoyed = ctx.frag("annoyed");
  const firstClient = ctx.a("first-client");
  const whyName = ctx.a("why-name");
  const moment = ctx.frag("moment");
  const refuse = ctx.frag("refuse");

  const opener = fill(ctx.pick(STORY_OPENERS), {
    name: brandName || "The company",
    location: location || "a small office",
  });

  const paragraphs: string[] = [];

  // Paragraph one: the irritation. Always concrete, always the client's words.
  paragraphs.push(
    [
      opener,
      annoyed
        ? sentence(`The problem was simple enough to say out loud: ${annoyed}`)
        : "The problem was simple enough to say out loud, and nobody had bothered to fix it.",
      ctx.pick([
        "Everyone had a way around it. Nobody had a way through it.",
        "It was the kind of thing people had stopped noticing, which is usually the sign it is worth fixing.",
        "It was not a crisis. It was a small daily tax that never got questioned.",
      ]),
    ].join(" "),
  );

  // Paragraph two: the first yes, and the turn. The lead-in matters — a client
  // answer is usually a fragment, and a paragraph should not open on one.
  paragraphs.push(
    [
      firstClient
        ? `${ctx.pick([
            "The first yes came from somewhere unexpected.",
            "Then somebody paid.",
            "The first customer is worth naming.",
          ])} ${sentence(firstClient)}`
        : "The first customer said yes for reasons that had nothing to do with the pitch.",
      ctx.pick(STORY_TURNS),
      foundedYear
        ? `That was ${foundedYear}.`
        : "That was the beginning, though it did not look like one at the time.",
    ].join(" "),
  );

  // Paragraph three: the name, or the moment it clicks for a customer.
  if (whyName) {
    paragraphs.push(
      [`${ctx.pick([
        "The name came from somewhere specific.",
        "About the name.",
        "People ask about the name.",
      ])} ${sentence(whyName)}`, ctx.pick([
        "The name has done a lot of quiet work since.",
        "It has turned out to be a useful thing to be held to.",
        "It still gets explained at least once a week, and it is still worth it.",
      ])].join(" "),
    );
  } else if (moment) {
    paragraphs.push(
      sentence(`The moment it lands for someone is usually ${moment}`),
    );
  }

  // Paragraph four: what has not moved.
  paragraphs.push(
    [
      refuse
        ? sentence(`There are things ${brandName || "the company"} will not do — ${refuse}`)
        : "There are things the company will not do, and the list has never got shorter.",
      ctx.pick(STORY_LANDINGS),
    ].join(" "),
  );

  return {
    kind: "prose",
    data: { heading: `${brandName || "Brand"} — origin story`, paragraphs },
  };
}

/* ---------------------------------------------------------------- */
/* Mission                                                           */
/* ---------------------------------------------------------------- */

export function generateMission(ctx: Ctx): DeliverablePayload {
  const { offering, brandName } = ctx.brief;
  const objectPhrase = objectFromOffering(ctx);
  const statement = fill(ctx.pick(MISSION_SHAPES), {
    verb: ctx.pick(MISSION_VERBS),
    object: objectPhrase,
  });

  const supporting = [
    offering
      ? sentence(`In practice that means ${offering.replace(/^We\s+/i, "").replace(/[.]$/, "")}`)
      : "In practice that means doing one job properly rather than five adequately.",
    ctx.pick([
      "It is a job, not an aspiration. On any given week you can check whether it happened.",
      "The test is boring and useful: did a customer get a straight answer today.",
      "If a decision does not serve that sentence, it does not get made.",
    ]),
  ];

  return {
    kind: "statements",
    data: {
      intro: `The one sentence ${brandName || "the brand"} should be able to say in a lift, a pitch and a job advert without changing a word.`,
      items: [
        { label: "Mission", statement: sentence(statement), detail: supporting.join(" ") },
        {
          label: "The everyday version",
          statement: sentence(ctx.pick([
            "Do the difficult part so the customer does not have to.",
            "Say the true thing first, then help.",
            "Leave people better informed than we found them.",
            "Make the right choice the easy one.",
          ])),
          detail: "This is the line for the team, not the website. It is what someone repeats to themselves before they answer an email.",
        },
      ],
    },
  };
}

function objectFromOffering(ctx: Ctx): string {
  const raw = ctx.frag("offering") || ctx.brief.offering.toLowerCase();
  if (!raw) return "the complicated thing simple";
  const cleaned = raw.replace(/^we\s+(help|make|build|give|sell|offer)\s+/i, "");
  return cleaned.split(/[.,]/)[0].trim() || "the complicated thing simple";
}

/* ---------------------------------------------------------------- */
/* Vision                                                            */
/* ---------------------------------------------------------------- */

export function generateVision(ctx: Ctx): DeliverablePayload {
  const tenYears = ctx.frag("ten-years");
  const sector = ctx.brief.sector || "this category";

  const claim = tenYears
    ? tenYears
    : ctx.pick([
        `nobody in ${sector} has to guess`,
        "the honest option is also the obvious one",
        "the standard is set by the work, not by the marketing",
        "this stops being remarkable and starts being normal",
      ]);

  const statement = fill(ctx.pick(VISION_SHAPES), { claim });

  return {
    kind: "statements",
    data: {
      intro: "Vision is the argument the brand is making about the future. It should be big enough to take ten years and specific enough to be wrong.",
      items: [
        { label: "Vision", statement: sentence(statement), detail: ctx.pick([
          "It is written as a description of the world, not a description of the company. That is the test: if the sentence only makes sense with the logo attached, it is a slogan.",
          "Deliberately outside what one company can deliver alone. A vision you can complete on your own is a roadmap.",
          "It should still read correctly if a competitor got there first — that is what makes it a belief rather than a plan.",
        ]) },
        { label: "What has to be true in a year", statement: sentence(ctx.a("success") || "The sales team stops apologising for the website."), detail: "The near-term proof. If this has not happened twelve months from now, the vision is decoration." },
      ],
    },
  };
}

/* ---------------------------------------------------------------- */
/* Offering                                                          */
/* ---------------------------------------------------------------- */

export function generateOffering(ctx: Ctx): DeliverablePayload {
  const stance = STANCE_WORDS[ctx.brief.priceStance];
  const offering = ctx.brief.offering || "the core service";
  const sector = ctx.brief.sector || "the category";

  const tierNames = ctx.pick([
    ["Start", "Standard", "Full"],
    ["Essential", "Complete", "Partner"],
    ["One project", "Ongoing", "Embedded"],
    ["Entry", "Core", "Signature"],
  ]);

  const priceLadder: Record<typeof ctx.brief.priceStance, string[]> = {
    accessible: ["Free", "£19 / month", "£49 / month"],
    considered: ["£40 / month", "£120 / month", "£350 / month"],
    premium: ["£2,500 one-off", "£6,000 / quarter", "£9,500 / month"],
    luxury: ["By application", "£25,000 / engagement", "Retained, annually"],
  };

  const prices = priceLadder[ctx.brief.priceStance];

  return {
    kind: "offering",
    data: {
      line: sentence(`${offering.replace(/[.]$/, "")} — ${stance.note}`),
      tiers: [
        {
          name: tierNames[0], price: prices[0],
          forWho: ctx.pick(["Someone testing whether this is real.", "One person, one problem, no commitment.", "The first look, without a contract."]),
          includes: ctx.sample([
            "One account or one project set up properly",
            "A written summary you keep either way",
            "Email support, answered by a person",
            "Everything exportable from day one",
            "No tie-in",
          ], 3),
        },
        {
          name: tierNames[1], price: prices[1],
          forWho: ctx.pick(["The main offer. Most customers land here.", "Where the work actually happens.", "The version this was designed around."]),
          includes: ctx.sample([
            "The full service, end to end",
            "A named contact who knows the account",
            "Quarterly review with the numbers in front of you",
            "Priority turnaround on anything urgent",
            "Onboarding done with you, not sent to you",
            "Everything from the tier below",
          ], 4),
        },
        {
          name: tierNames[2], price: prices[2],
          forWho: ctx.pick(["Organisations who want us in the room.", "When it needs to be part of how the team works.", "For the accounts where this is load-bearing."]),
          includes: ctx.sample([
            "Direct line to the people doing the work",
            "Custom reporting built around your board pack",
            "Two days on site per quarter",
            "Roadmap input before anything ships",
            "Everything from the tier below",
          ], 4),
        },
      ],
      boundaries: [
        ctx.a("refuse") ? sentence(ctx.a("refuse")) : `We do not take work outside ${sector}.`,
        ctx.pick([
          "We do not discount to win. The price is the price, and it is the same for everyone.",
          "We do not take on work we cannot staff properly. That sometimes means a wait.",
          "We do not sell anything the customer could reasonably do themselves in an afternoon.",
        ]),
        ctx.pick([
          "We say no to work that needs us to overstate what it will do.",
          "If a smaller supplier is the right answer, we will name them.",
          "We will tell you when you do not need us yet.",
        ]),
      ],
    },
  };
}
