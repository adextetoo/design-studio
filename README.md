# Design Studio

A workspace for running a brand engagement end to end — discovery, strategy,
visual identity, applications — and handing over a `Brand.md` and a `Design.md`
that contain only what a human actually approved.

It is modelled on how a brand studio really works, from four reference films
supplied with the brief (see `docs/RESEARCH.md`): a studio operating system with
a client workspace and a published brand hub, an 8-week identity engagement deck,
a printed brand guide, and a strategy workshop exercise.

```bash
npm install
npm run dev              # http://localhost:3000
npm test                 # generator + tone tests
npm run typecheck
npm run lint
npm run build            # Next.js production build
npm run build:standalone # dist/studio.html — the whole studio in one file
```

No API keys, no backend, no account. The project lives in `localStorage` and
nothing leaves the browser.

`npm run build:standalone` compiles the same source into a single self-contained
`dist/studio.html` — React and Tailwind inlined, `next/link` and `next/navigation`
aliased to a hash router in `src/standalone/`. That file opens from disk, and is
what gets published as an Artifact.

---

## The five screens

| Route | What it is |
|---|---|
| `/` | **Overview** — the engagement at a glance: four stages, what is drafted, what is waiting on you, what is approved. |
| `/discovery` | **Discovery & Strategy** — 27 questions across 8 phases, one on screen at a time, plus the brand-personality ballot. |
| `/studio` | **Design Studio** — the eighteen modules, each with Approve and Refresh. |
| `/hub` | **Brand Hub** — the approved system as a numbered, published guide. |
| `/handoff` | **Handoff** — `Brand.md` and `Design.md`, previewed and downloadable. |

## The eighteen modules

**Foundation** — Brand Story · Brand Mission · Brand Vision · Offering
**Position** — Market Research · Customer & Audience · Competition · Differentiation · Brand Exposé
**Identity** — Tone & Voice · Look & Feel · Logo · Colour · Typography
**Application** — Packaging · Marketing Material · Social Media · Website Copy

Each one generates a draft, and each one carries the same pair of controls.

## Approve and Refresh

This is the mechanic the whole app is arranged around.

- **Refresh** generates another round from the same brief. Rounds are never
  thrown away — they stack up in a rail on the right, so you can go back to the
  one from twenty minutes ago, which is usually the good one.
- **Approve** freezes the round you are looking at and marks which file it feeds.
  Refreshing an approved module drops it back to draft on purpose: the thing that
  was approved is no longer the thing on screen.
- Nothing reaches `Brand.md` or `Design.md` until it is approved. Unapproved
  modules are listed by name at the bottom of each file under *Not yet approved*,
  so whoever picks the file up can see how much of it a person has signed off.

## How generation works

There is no model call. Every round is a pure function of `(brief, seed)`:

```
seed = hash(brandName, sector, offering, priceStance, fontClass, traits, moduleId, round)
```

Same seed, same output — so a project file opens identically on someone else's
machine, and Refresh is simply "advance the round". Generators compose curated
corpora (palette recipes, typeface classes, voice pillars, archetypes, story
shapes) around the client's own workshop answers, which is why the output reads
specific rather than generic. A thin brief produces visibly thin work; that is
intended, and the studio says so on screen.

### Human tone, enforced

The brief asked for a brand story with no AI jargon. That is held by
`src/lib/jargon.ts`: a blocklist of machine tells and consultancy filler
(`leverage`, `cutting-edge`, `seamless experience`, `in today's fast-paced
world`, `unlock your`, …) checked against every string the studio produces.

- The studio shows the result on screen as a **Human tone check** panel rather
  than hiding it.
- A test asserts that no generator, across three briefs, eighteen modules and ten
  rounds each, emits a single flagged phrase.
- Quoted counter-examples are exempt by path: the voice guidelines are supposed
  to show the wrong version next to the right one, and a banned-words list has to
  name the banned words.

### Typography from a font class

You pick a *class* in discovery, not a typeface. The generator builds the
`LEVEL / WEIGHT / SIZE / LEADING / TRACKING / USE` hierarchy from that class —
modular scale ratio, display tracking, heading case and per-class rules — and
names real typefaces with their licences and a fallback stack. Set a Didone and
the scale opens to an augmented fourth and the rules warn you off small sizes;
set a mono and headings go uppercase with positive tracking.

### Colour with the maths done

Palettes are chosen by matching recipes against the personality traits and price
stance, then jittered within tolerance so a refresh reads as another take rather
than another idea. Every swatch ships HEX, RGB, CMYK and **measured WCAG 2.1
contrast against both white and ink** — reported per ground, because a colour
that clears AAA on ink and fails on white is not "AAA", and a guideline that says
otherwise is how body copy ends up illegible.

### The workshop ballot

The brand-personality exercise runs the way it runs in the reference film:
everyone on the call gets five picks and one starred favourite, **in private**,
and nothing is visible until someone hits Reveal. That is the whole point — with
the loudest person's picks on screen while everyone else chooses, you have
measured that person, not the brand. The tally ranks favourites first, and calls
out both suspicious unanimity and a room that has not actually agreed yet.

## Project structure

```
src/
  app/            five routes, all statically prerendered
  components/
    modules/      one renderer per payload kind, plus constructed logo marks
  data/           modules, workshop questions, palettes, typefaces, language
  lib/
    generators/   one file per stage — foundation, position, identity, application
    markdown/     Brand.md and Design.md builders
    color.ts      hex/rgb/cmyk, WCAG contrast
    jargon.ts     the human-tone checker
    store.tsx     reducer + localStorage
tests/            generator determinism, variance, tone and shape assertions
docs/
  RESEARCH.md            what was extracted from the reference films
  UI-FLOW-SOURCING.md    which Appllama flow was used, and why over the alternatives
```

## Where the UI patterns came from

The generate → review → **approve or regenerate** loop is not invented here. It
was sourced from the Appllama MCP library and modelled on the `Logo Creation`
flow in *Rebrand: AI Logo Maker*, backed by `Room Redesign` in *Arch: AI Home
Design*. The full reasoning — including why those beat the much larger
`Onboarding` flow, the Mobbin MCP server and the design skills — is in
[`docs/UI-FLOW-SOURCING.md`](docs/UI-FLOW-SOURCING.md).

## Saving files

`src/lib/download.ts` handles the one thing that differs between the two builds.
As an ordinary web page, a blob plus an anchor saves `Brand.md`. Published as an
Artifact the page is framed and that anchor is inert, so the save goes through
the platform's `downloads` capability, which asks the viewer to confirm. The
presence of `window.claude.use` decides which path is real — and when saving is
off in a view, the page says so and points at Copy instead of failing quietly.

## Known limits

- Market research figures are studio placeholders sized to be plausible for a
  sector. The app labels them as such on screen and in the file. Replace them
  with the client's own analyst data before anything goes to a board.
- Typefaces are named with licences and render through a fallback stack. Install
  the licensed font to see the real specimen; the scale and hierarchy are exact
  either way.
- One project at a time, in one browser. There is no sync and no server, which is
  a deliberate trade for having no account and no data leaving the machine.
