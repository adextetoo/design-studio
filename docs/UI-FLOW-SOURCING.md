# UI flow sourcing — why Appllama, and which flow

Question asked: *can this be achieved, and does the Appllama MCP server hold visual
flows that align with the Design Studio?* Short answer: yes, and one flow aligns almost
screen-for-screen.

## What the Design Studio actually needs

Strip the product back and there are four interaction shapes, not eighteen:

| # | Shape | Where it appears |
|---|---|---|
| A | Guided one-question-per-screen intake with phase + step progress | Discovery workshop (27 steps, 8 phases) |
| B | Constrained multi-select from a word bank | "Pick your top 5 traits", audience picks, tone pillars |
| C | Brief → direction cards → generate → review → **approve or regenerate** | All 18 brand modules |
| D | Left page list + centre canvas + right inspector | Brand Hub editor |

Shape **C** is the one the brief hangs on ("an approve or refresh button to enable more
inspiration"). So that is the flow to source properly.

## The Appllama catalogue, measured

`list_flows` → 3,274 flows. `list_ui_elements` → 38 element families with live counts.
The families that matter here:

| Element family | Screens | Apps | Why it matters |
|---|---:|---:|---|
| `question-headers` — *"the quiz grammar of modern onboarding, one question at a time"* | 1,690 | 344 | Shape A |
| `chips-pills` | 10,751 | 1,118 | Shape B |
| `progress` (Top/Step/Segmented Progress Bar) | 9,426 | 931 | `Step 17 of 27` |
| `cards` (Rounded Option Cards, Two-Column Card Grid) | 17,087 | 1,120 | Direction/route cards |
| `selected-states` (Selected Choice Border, Selected Checkmark) | 3,604 | 941 | Approving one route |

## The flow chosen

**`Logo Creation` — Rebrand: AI Logo Maker** (`app_id 6737123019`, $70K/mo, 4.47★ over
13,650 ratings, launched 2025-01-30). Four screens, walked in journey order:

| Position | Screen | UI elements Appllama reports |
|---|---|---|
| 1 | `Graphic Logo Creator` | Logo Type Segmented Tab · Dark Prompt Text Area · **Surprise Me Mini Pill** · Style Thumbnail Grid · Gray Disabled CTA |
| 2 | `Logo Generation Progress` | Dark Orb Background · Centered 10% Progress · Progress Status Caption |
| 3 | `Logo Result` | Rounded Logo Preview · **Variant Thumbnail Row** · **Outlined Selected Tile** · Circular Action Buttons · Dark Locked Tiles |
| 4 | `Text Logo Creator` | Rounded Brand Name Field · Dark Prompt Text Area · **Surprise Me Mini Pill** · Style Thumbnail Grid · White Rounded CTA |

That is shape **C** end to end, in a shipping product, in the exact subject matter
(brand identity): *brief → style grid → generate with visible progress → result with
selectable variants → keep one, or hit Surprise Me for another round.*

Its own onboarding (same app) supplies the setup half: `Business Category Selection`
(Three-Step Progress · Two-Column Card Grid · Emoji Category Cards · Selected Outline
Card · Sticky Rounded White CTA) → `Design Type Selection` (Image Option Card Grid ·
Circle Selection Marks · Selected White Outline) → `Brand Identity Processing`
(Centered Progress Ring · Blue Checklist Rows) → `Brand Identity Complete`
(Circular Success Mark · Blue Checklist Rows).

**Supporting flow: `Room Redesign` — Arch: AI Home Design** (`app_id 6446172225`,
$60K/mo, 4.63★ over 23,723 ratings, ranked #103 in Graphics & Design US). Five screens
that generalise the same loop across a *multi-step* setup, which is what an 18-module
studio needs:

`Room Photo Upload` (Rounded Upload Panel · Photo Tip Chip · Upload Photo Pill ·
Template Strip · Gray Continue CTA) → `Room Photo Selected` (Hero Image Panel · Image
Remove Chip) → `Room Type Selection` (Room Type Cards · Photo Tile Grid) →
`Design Style Selection` (Custom Style Card · Design Style Cards) →
`Color Palette Selection` (Palette Swatch Cards · **Surprise Me Card** · Color Band
Tiles), all under one persistent `Four Step Progress` and a `Close Icon Button`.

`get_screen` on `Color Palette Selection` returns a similarity cluster at 0.70–0.79
across four other apps (AI Garden Design, AI Home Design DecAI, SnapHome, Home AI) —
i.e. this is a *converged* pattern, not one team's idea. That is the strongest possible
argument for copying it.

### How it maps into this codebase

| Appllama screen | This app |
|---|---|
| Room Photo Upload / Photo Selected | `LogoModule` — upload panel, tip chip, remove chip, generated-mark strip |
| Business Category Selection | `MODULE_DIRECTIONS` two-column direction cards with selected outline |
| Design Style Selection | Moodboard route cards in Look & Feel |
| Color Palette Selection + Surprise Me | `PaletteModule` swatch cards + the **Refresh** control |
| Logo Generation Progress / Brand Identity Processing | `GeneratingOverlay` — progress ring plus checklist rows |
| Logo Result: variant row + outlined selected tile | `VariantRail` — every module keeps its previous rounds and lets you re-pick one |
| Sticky Rounded White CTA / Selected Checkmark | The sticky `Approve` bar and the module's approved state |
| Four Step Progress (persistent, with Close) | `StudioProgress` — 18 modules, N approved, always on screen |
| `question-headers` + `chips-pills` + Step Progress | `WorkshopQuestion` and `TraitPicker` |

## Why this over the alternatives

**1. Over Appllama's `Onboarding` flow (17,626 screens / 1,035 apps).** The obvious
pick by volume, and wrong. Onboarding is optimised for *acquisition* — its job is to
carry a stranger to a paywall, so its grammar is skip links, social proof carousels,
laurels, discount badges and one-way progress. The Design Studio's user is a paid
designer in a working session who needs to move backwards, disagree, regenerate and
leave things unapproved. I took the two element families from it that do transfer
(`question-headers`, `chips-pills`) for the workshop, and left the funnel behind.
`Logo Creation` is post-signup, work-shaped, and reversible — the right grammar.

**2. Over the Mobbin MCP server** (`search_flows` / `search_screens` /
`search_sections`, also connected in this session). Mobbin is the larger and more
design-respected library, and for a mood reference I would reach for it first. It loses
here on one specific thing: Appllama returns a **named UI-element inventory per screen**
(`"Surprise Me Card"`, `"Outlined Selected Tile"`, `"Variant Thumbnail Row"`,
`"Sticky Rounded White CTA"`) plus dominant colours, journey position, and a
`similar_screens` cluster with similarity scores. That is a *machine-readable component
list I can implement directly*, which is what the brief asked for — flows chosen for
build fidelity, not for a moodboard. Mobbin's strength is the image; in this
environment the image is exactly what I could not get (see caveat below), so the
metadata-first library won.

**3. Over the `design` / `canvas-design` skills.** Both are available and both are good
at what they do — producing static artboards and posters. Neither describes an
*interaction* flow: no progress model, no selected state, no regenerate affordance, no
evidence that a real product shipped it and holds a 4.5★ average. They are the right
tool for the Brand Hub's page artwork, not for deciding how Approve/Refresh behaves.

**4. Over designing it from memory.** Cheapest, and it would have produced a plausible
generic wizard. The Appllama pass changed three concrete decisions I would otherwise
have got wrong: (a) the regenerate control belongs *next to the options as a peer card*
("Surprise Me Card" sits in the palette grid), not hidden in a toolbar; (b) generated
rounds must persist as a **variant rail** so a designer can go back to round 2 —
tellingly, every one of these apps keeps the thumbnails; (c) progress is **persistent
and global** to the session, not per-step, so an 18-module studio still reads as one
piece of work.

## Caveat, stated plainly

This session's network policy returns `403` on `CONNECT mcp.appllama.io:443`, so the
screen **images** could not be downloaded — the analysis above is built from the
structured metadata the MCP tools return (screen names, per-screen UI-element
inventories, colour sets, journey positions, similarity clusters), not from looking at
the pixels. 31 credits of the 1,500 monthly allowance were used.
