# Design Studio

The language a brand engagement is run in: what a studio and its client
decide, in what order, and what has to be signed off before it can be handed
over. This glossary is the vocabulary; it carries no implementation detail.

## The engagement

**Engagement**:
One piece of brand work for one client, from the first discovery question to
the handover files.
_Avoid_: project, campaign, job.

**Brief**:
The set of facts about the client that every generated draft is allowed to read
— name, sector, offering, traits, price stance, type class, and the workshop
answers.
_Avoid_: inputs, config, profile.

**Stage**:
One of the four passes an engagement moves through: Foundation, Position,
Identity, Application. A stage groups decisions; it does not gate them.
_Avoid_: phase (that word belongs to the workshop), step, milestone.

## Discovery

**Workshop**:
The guided intake: eight phases, twenty-seven steps, one question on screen at
a time.
_Avoid_: questionnaire, survey, onboarding.

**Phase**:
A named group of workshop questions with a stated intent (Origin, Growth
Timeline, Brand Personality…). Phases exist inside the workshop only.
_Avoid_: section, stage.

**Ballot**:
One participant's five trait picks and single starred favourite, submitted in
private and hidden until the reveal.
_Avoid_: vote, response, survey answer.

**Reveal**:
The moment every ballot becomes visible at once. Before the reveal nobody sees
anyone else's picks; that concealment is the exercise, not a feature of it.
_Avoid_: results, tally view.

## The work

**Deliverable**:
One of the eighteen things a brand system is made of — Brand Story, Colour,
Packaging, and so on. Each has its own generator, its own approval, and its own
place in the export.
_Avoid_: **module** (that word means a unit of code here — see
`docs/adr/0004-deliverable-not-module.md`), section, artefact, asset.

**Round**:
One generated take on a deliverable. Round 1 is the first draft; Refresh
produces round 2, and so on. Rounds are kept, never overwritten.
_Avoid_: variant, version, iteration, generation.

**Draft**:
A round nobody has approved. Drafts are visible in the studio and absent from
the hub and the export.
_Avoid_: pending, unapproved, WIP.

**Approved**:
The state of a deliverable whose currently selected round a person has signed
off. Approval names a specific round, not a deliverable in general — refreshing
withdraws it, because the approved thing is no longer what is on screen.
_Avoid_: signed off, final, locked, published.

**Direction**:
One of the named art-direction options inside Look & Feel (The Confident, The
Architect…), each carrying three adjectives that everything downstream is
judged against.
_Avoid_: moodboard, concept, style.

**Route**:
One of the three constructed logo options. A route is chosen, not approved —
approval belongs to the deliverable that contains it.
_Avoid_: option, concept, mark (a mark is what a route contains).

## Handover

**Brand Hub**:
The approved system presented as a numbered guide, in the order a printed brand
guide runs. It shows approved deliverables only.
_Avoid_: guidelines site, style guide, brand book.

**Handoff**:
The export screen, and the two files it produces: `Brand.md` (strategy,
position, language) and `Design.md` (the visual system).
_Avoid_: delivery, export, download page.

**Human tone check**:
The pass that reads every string in a round against a blocklist of machine
tells and consultancy filler, and reports what it finds rather than hiding it.
_Avoid_: lint, validation, AI check.
