# Research notes — source material & UI-flow sourcing

These notes record what the build is derived from. They are working notes, not marketing copy.

## 1. Reference product (from `How we run our design studio from one software`)

The screen recording in that clip is **Designer HQ** (`app.designerhq.app`), the studio
operating system built by Nonstop Branding (Jack Watson + Leo Banzon). Frame-by-frame
extraction gives the following information architecture, which this project replicates
and then extends into a full design studio.

**Studio level**
- Workspace switcher, Search, Overview, Tasks, Calendar
- Clients list in the sidebar, each with a mark and a tag colour
- `Tools · Beta` group (Client Acquisition)
- Overview page: `Welcome back to your HQ, {name}`, action row (New client / Add event /
  Open tasks), four stat tiles (Clients, Active projects, Waiting on clients, Unpaid),
  Today panel with an "All caught up" empty state, Upcoming events panel, Clients table
  with progress `4/4` and engagement pills, and an Activity feed.
- New client modal: Company name, Logo (optional, any image up to 2 MB), Tag colour
  swatch row with an `Auto` option.

**Client level** (sidebar: All clients → client → Dashboard, Tasks, Documents, Library,
Discovery & Strategy, Brand guidelines, Client settings)
- Dashboard: phase stepper `Discovery — Strategy — Design — Delivery`, week timeline,
  Tasks, Documents (Proposal / Contract / Invoices with status pills), and a right rail
  with Contact, Value (Invoiced / Paid), Key dates, Brand guidelines status, Records.
- Discovery & Strategy: tabs `Workshop | Answers | Strategy`.
  - Workshop is one question per screen: `Phase 3 of 8 · Growth Timeline`,
    `Step 12 of 27`, a large centred question, a "Leave a note" field, Back / Next,
    a `Questions · 20 of 20` counter and a Saved/Saving indicator.
    Observed questions: *"Why did you choose your brand name?"*,
    *"In ten years, what does the company look like?"*
  - Strategy is a generated, approvable document: Origin Story, Mission, Vision,
    Brand Values (name / description / adjectives), Tone of Voice pillars
    (Direct, Confident, Warm, Sharp, Listening — each a claim plus an explanation),
    Audience, Positioning, From the client. Header pills: `Approved`,
    `Updated … · Sent … · Approved …`.
- Brand guidelines: a **Brand Hub** editor — left page list
  (`01 Strategy, 02 Gallery, 03 Logo, 04 Typography, 05 Colour, 06 Photography,
  07 Layout, 08 {Brand} Objects, 09 Resources`), centre canvas, right inspector with
  page name, block list and hub tokens (Surface, Panel, Text, Text muted, Accent,
  Divider, Corners Soft/Sharp). `Design | Files` tabs, Share and **Publish**.
  Published output is a live site (observed: `bloom-brandhub.vercel.app`).
- Resources page ships download packs (Logos, Fonts, Photography, Full Brand Kit) and
  a **For AI** block: *"A single Markdown brief covering strategy, voice, color, and
  type, built from this hub's content. Paste it into any AI assistant as a system
  prompt to brief it on the brand."* → this is the direct precedent for the
  `Brand.md` / `Design.md` requirement.

Narration (reconstructed from burned-in captions) confirms the intent: the strategy
draft is *"based off previous $20,000 brand identity strategies"*, and
*"this isn't [the software making the] strategy — you still make the decisions, but
[it beats] starting with a blank page and a messy pile of notes."*
That line is the design principle for every Approve / Refresh control in this app.

## 2. Deliverable set (from `Do you know what separates premium brands…`)

A full 8-week engagement deck for a brand called *Titan Arc*. Section title cards, in
order: **Company Profile, Brand Personality, Brand Positioning, Mission, Vision, Story,
Ideal Buyer Persona 1, Ideal Buyer Persona 2**, an offering/market-fit venn, then
**Moodboard 1: The Confident** (Warm · Cinematic · Intimate), **Moodboard 2: The
Architect** (Dynamic · Scientific · Precise), **Moodboard 3: The Optimist**
(Light · Elevated · Pristine), then three complete logo routes (`logo 1/2/3`), each with
mark, wordmark, palette, sub-brand lockups and applications — followed by a selection
screen where one route is ticked and the other two grey out.

Detail worth copying:
- Personas are structured: name + archetype label ("Robert | The Competitive Optimizer"),
  five numbered traits, Motivations, Challenges, Why this brand, Buying Behavior,
  Social Media Behavior, and a demographic block (Age, Gender, Marital Status, Location,
  Brands they already buy).
- Brand Personality uses an archetype pair (Sage + Magician), five brand adjectives,
  core values and brand voice, plus "other brands with this archetype".
- Positioning is written as What / How / Who / Where, then one positioning sentence.
- Market research pages: *The Growing Longevity Economy*, *Executive Summary $600B*,
  *50M — Current State of Healthcare*, *Market Segmentation* with percentage splits.
- Timeline artefact: `week 1 Discovery · week 3 Strategy · week 5 Design ·
  week 7 Design Refine`, with "(working)" in the even weeks.

## 3. Guideline page grammar (from `A closer look at the identity we created for Monty`)

Every page: running header `{brand}™ · A BRAND GUIDE FOR {BRAND}`, left column with a
two-line title and a short explanatory paragraph, right column with the artefact, and a
`COPYRIGHT ©{year}` footer. Pages observed: Primary Logo, Logomark, Primary Typeface,
**Typeface Hierarchy**, Color Palette, Graphic Patterns, Brand Imagery, Brand Billboard,
Brand Mockups, Social Media Creatives, Social Media Page Banners, Brand Stationery.

Two structures copied verbatim into the generators:
- **Typeface Hierarchy** table — `LEVEL | WEIGHT | USE`:
  Headlines/Bold/hero statements, cover titles, section dividers · Subheadings/Medium/
  section titles, slide subheads, eyebrows · Body Copy/Regular/paragraphs, descriptions,
  documents · Label/Medium-Regular/buttons, tags, nav labels, captions ·
  Caption/Light/footnotes, image credits, fine print.
- **Colour cards** — a large block per colour carrying name + HEX + RGB + CMYK
  (e.g. Electric Indigo, Midnight Navy, Powder Blue).

## 4. Workshop mechanic (from `How I run my Strategy Workshops`)

The Brand Personality exercise, captured on screen:

> **Pick your top 5 traits** — *Select the words that best describe your brand personality.*
> `Bold · Playful · Premium · Relentless · Warm · Strategic · Innovative · Trusted ·
> Minimal · Energetic · Refined · Authentic · Disruptive · Luxurious · Approachable ·
> Systematic · Creative · Human · Transparent · Purpose-driven · Resilient ·
> Partner-driven · Scrappy · Future-proof · Visionary · Empowering · Provocative ·
> Collaborative · Adventurous · Precise · Optimistic · Proven`
> Header: `Phase 5 of 8 · Brand Personality`, `Step 17 of 27`.

The narrated rule: *everyone on the call gets five picks, silently, and only at the end
does everyone reveal their favourite* — deliberately, so that the loudest person in the
room does not set the brand personality. The app implements this as per-participant
ballots that stay hidden until reveal.

