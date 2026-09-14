# Where the data in this app comes from

This app supersedes an earlier NPFL Fantasy build (`adextetoo/npfl-fantasy`, a
Node/TypeScript monorepo). The brand system here is unchanged — NaijaLeague
Fantasy, its palette, type, chips and copy all stay as approved. What was taken
from the earlier build is the **research**: facts about the league that somebody
spent real time sourcing, and which would otherwise have to be found twice.

This file records what came across, what was corrected on the way, and what is
still open. It exists so that "where did you get that" is answerable without
archaeology — and so the next person does not repeat a search that already
failed.

## The league: 20 clubs, 2026/27

`data/NpflClubs.kt` merges three bodies of research:

| Fields | Source |
| --- | --- |
| Market value, registered squad size | Transfermarkt league table, 15/08/2026 snapshot |
| City, state, stadium, capacity, founding year, honours, coach | `research/clubs.json` (earlier repo), compiled 04/09/2026, per-field verification flags |
| Kit colours | `packages/core/src/clubIdentity.js` (earlier repo) — RSSSF's Nigeria colours page and club Wikipedia articles |

The transcription is checked against Transfermarkt's own published totals in
`SampleDataTest` — squad sizes must sum to exactly 829 and values to within
€100,000 of €25,650,000. A slipped digit fails the build.

### Corrections made while merging

- **Three clubs had no city.** The Transfermarkt table leaves Barau, Kun
  Khalifat and Ranchers Bees blank. The earlier repo's per-club research has all
  three: Barau is Kano State (traditional home Dambatta, matches at Sani Abacha
  Stadium), Kun Khalifat is Owerri in Imo State, Ranchers Bees is Kaduna.
- **Doma United was placed in Gombe.** Gombe is where they *play*; the club is
  from Doma in Nasarawa State. Both facts are now carried separately.
- **An id collision that would have been silent.** The earlier repo abbreviates
  Ranchers Bees as `RAN` and Rangers International as `ENR`. This app uses `RAN`
  for Rangers. Merging by abbreviation would have put a newly-promoted club's
  record on the defending champions and nothing downstream would have
  complained. There is a test for exactly this.
- **Capacities differed between the two earlier sources.** `clubs.json` (which
  carries per-field verification flags) was followed over `clubs.js` throughout:
  Rangers 22,000 not 28,000; Rivers United 38,000 not 40,000; Plateau United
  60,000 not 44,000; Warri Wolves at Warri Township Stadium, not Ozoro.

### Two clubs are not playing at home this season

Rangers International play every 2026/27 home fixture at the MKO Abiola Sports
Arena in Abeokuta — roughly 600km from Enugu — while Nnamdi Azikiwe Stadium is
renovated. Doma United play their Nasarawa home fixtures at Pantami Stadium in
Gombe.

This is not trivia in a fantasy game. The **Ground Man** chip pays a 1.5x
multiplier for playing at home, and a fixture list that calls Abeokuta a home
game for Enugu is wrong about the only thing that chip rewards. So
`Performance.atNeutralGround` is a field the scorer fills in, `Scoring` refuses
the multiplier on it, the fixture row names the actual ground, and the rules
page states the exception on Ground Man's own entry — derived from the club
table, so it disappears by itself the season Rangers get their ground back.

### Kit colours: two sources, compared rather than ranked

There is no current authoritative kit register for the NPFL. The best
colour-specific reference is RSSSF's Nigeria page, which carries the note "Last
updated: 29 Jan 2010" — sixteen years stale, from an era when two of these clubs
were not in this division. Wikipedia's articles are current but mention kits in
passing prose rather than as data. The rule applied:

- Both sources name the same pair → render it (11 clubs).
- Same pair, disagree on which is primary → render the more recent reading and
  record the swap (3SC, Kwara United, Niger Tornadoes).
- **Different colours → do not render.** RSSSF has Rangers in red and Enyimba
  in blue; the Wikipedia articles make both green. One reading is wrong and this
  is not the place to decide which, so the app falls back to its own generated
  mark (Rangers, Enyimba, Kano Pillars).
- No source at all → nulls, and the same fallback (Barau, Doma United, Inter
  Lagos — all recently promoted).

Hex values are a rendering choice, not a claim: the sources say "green", never
`#00843D`.

## Squads: two of twenty clubs, and the app says so

`data/NpflSquads.kt` holds 56 real players whose club *and* position both come
from a source, plus 13 more named in dated reporting that never says what they
play.

| Club | Players | Source | Confidence |
| --- | --- | --- | --- |
| Enyimba International | 21 | the club's own first-team page, read 07/09/2026 | medium |
| Ikorodu City | 30 | the club's own players page, read 08/09/2026 | low — the footer reads "Copyright (c) 2023" |
| Rivers United, Plateau United, Kun Khalifat | 5 | dated 2026 transfer reports | per player |

### The sourcing standard, which is narrower than it looks

A squad may be entered from a **club-official** source or a **dated report** that
names the player in the body of the story. Rejected, and recorded as rejected so
the search is not repeated:

- **Aggregators** (BeSoccer, FotMob, Sofascore, worldfootball, tribuna, AiScore,
  Goal.com). Complete lists, no timestamps, and a season label in a dropdown is
  a claim rather than a date. Checked against dated reporting they were wrong
  about positions, spellings, and which club a player was at.
- **Fan sites**, however good. Kano Pillars' best-looking site states in its own
  footer that it is not affiliated with the club.
- **Lapsed club domains.** The address Shooting Stars still publish for contact
  now serves an online casino. A domain that was once official is not
  permanently trustworthy.
- **Wikipedia squad sections.** Enyimba's says it is current as of February 2023.
- **"Notable players" sections**, which are overwhelmingly alumni. Okocha, Mikel,
  Ahmed Musa and Yekini are history. Putting a famous name in a fantasy pool
  because it appears on a club's Wikipedia page is how an app tells its most
  knowledgeable users it does not know the league.

### One place a dated report overrules a club's own site

Enyimba's undated first-team page still lists former captain **Ekene Awazie** at
30. Dated September 2026 reporting has him at Rivers United. He is recorded
against Rivers. The same reasoning moves **Hogan Umoh** to Kun Khalifat, which is
why Enyimba's midfield reads thin — that is the squad, not a gap in the
transcription.

### Position is a sourced field like any other

Thirteen real players are known by name and nothing else: eight Kano Pillars
signings, their captain Rabiu Ali, two Rangers signings, one Rivers United
signing, and Sporting Lagos' Ebenezer Harcourt. None of the reports says what
they play. They are recorded, they are **not** in the selection pool, and
`NpflSquads.awaitingPosition()` is the operator's queue. A pool that guessed
would be asking managers to pick a goalkeeper who is a winger.

### The other eighteen clubs get labelled stand-ins

Not invented names. The earlier build made the same choice and the reasoning
holds: an invented Nigerian name reads as real to exactly the users this product
exists for, and the moment one of them sees a stranger listed at their club, the
app has lost the only advantage it has over FPL. So stand-ins say what they are
(`Player.isPlaceholder`), the row carries a STAND-IN tag, the Choose Players
screen states the position in one line at the top, and the per-club provenance
sits at the bottom of the list it applies to.

The diacritic specimen that used to live in the player pool — invented names
carrying Yoruba under-dots, Igbo dotted vowels and Hausa hooked letters — moved
to `data/NigerianOrthographySpecimen.kt`. The §02 typography argument needs
strings, not a fake squad.

## Engine rules brought across

| Module | What it is for |
| --- | --- |
| `rules/AutoSubs.kt` | Bench substitutions, and the NPFL wrinkle: **a postponed fixture counts as a non-appearance.** Three fixtures were postponed in the first two rounds of this season. Treating one as "played, nil points" would leave a manager with nine men on a weekend they could not have planned for. |
| `rules/Formations.kt` | Legal formations *enumerated from* `SquadRules`, never listed by hand, so the picker cannot offer a shape the validator rejects. |
| `rules/Provenance.kt` | Four source tiers with authority modelled **per category**: a teamsheet is the best evidence about who played and no evidence at all about who assisted. This is the spine under the provisional-until-Monday challenge window. |
| `rules/ChipUsage.kt` | One of each chip per season, one chip per gameweek. A remaining-uses counter cannot express that. |
| `rules/Eligibility.kt` | Free entry always, non-cash prizes by default, cash double-gated, every prize requiring a valid permit. |

### A bug carried across as a fixed test

The earlier build guarded its permit check with a condition that was only ever
true for cash. Airtime, data bundles, jerseys and match tickets — the entire
non-cash schedule — were awardable with no permit at all, or an expired one, or
one that did not cover the winner's state. Every test happened to supply a valid
permit, so the suite passed over a control that never fired. The port runs the
check unconditionally and `EligibilityTest` asserts the no-permit case for every
non-cash kind.

## Still open

1. **The age gate.** The board approved a core user of 16 to 60, and the approved
   Three Pick copy promises that under-18 winners take airtime, data or a match
   ticket. That is what is implemented: 16 plays, under-18 wins non-cash, 18 wins
   cash, and `BrandCopyTest` reads the copy so the engine cannot drift from the
   promise.

   The earlier build's compliance work reached a **stricter** conclusion: a hard
   18+ gate at signup, not at redemption, because the gaming statute makes it an
   offence to knowingly let a minor take part in a promotional competition and
   NDPA/GAID treats under-18s as children whose consent basis is unsettled — so
   letting minors play but not win still means processing a child's personal data
   under that regime. Not implemented, because the board owns this call. Moving
   the gate is one line: `Eligibility.MINIMUM_PLAYING_AGE`.

2. **Brand book §10 cites Remo Stars as its proof case.** Remo Stars were
   relegated at the end of 2025/26 and are not in this division. The copy is
   board-approved, so it has not been changed.

3. **Kun Khalifat's capacity** is reported as 10,000 in some places and 12,000 in
   others. Unresolved; 12,000 is carried.

4. **Warri Wolves' founding year** was not found. Carried as null rather than
   guessed — they were Nigeria Port Authority FC until 2007.

5. **Eighteen squads, three kit colour sets, and three contested ones** are
   operator work, not research problems. Somebody who has seen these teams play
   can supply in ten seconds what a week of searching did not.

## Not brought across

The earlier repo's API, admin console, database schema, import pipeline and
Transfermarkt scraper are server concerns and have no place in the Android app.
Its `research/` and `docs/` directories remain the fuller record; this file
covers what reached the app.

## Reading the operator console

The app is now a READER of that API — never a writer. `catalogue/` fetches
`GET /clubs` and `GET /players`, neither of which requires a credential, and
folds an operator's work into the researched data above. Nothing in the app can
sign a request, so creating a player or correcting a fixture stays behind the
console's own login where it belongs.

**The researched data is the floor.** A server row wins only where it carries
provenance saying an operator put it there:

| Field | Taken when | Otherwise |
| --- | --- | --- |
| Club colours | `colour_confidence = 'admin_verified'` | the reading above stands, contested ones included |
| Club identity | `identity_admin_edited = true` | the researched record stands |
| Players | `is_placeholder = false` **and** `data_source` is `admin_verified` or `league_feed` | not imported; the app has its own labelled stand-ins |

Anything else the server sends is declined, because it was seeded from the same
`clubIdentity.js` these constants were checked against — accepting it would
launder a guess into a fact by routing it through a database. Item 5 under
*Unresolved* is what this is for: an operator who has seen Barau, Doma United or
Inter Lagos play can supply in ten seconds what a week of searching did not, and
can settle the three contested kits that this file refuses to pick a side on.

**Three things a real server taught this client**, none of which reading the
schema would have:

1. The console keys clubs by slug (`enugu-rangers`), not by this app's
   three-letter code, and its `abbr` column disagrees with the app for five of
   the twenty — `IKO`/`IKC`, `INT`/`INL`, `KAN`/`KNP`, `3SC`/`SSC`, `RAB`/`RBE`.
   Joining on either alone silently drops a quarter of the division. See
   `catalogue/ClubIds.kt`.
2. `players` has no shirt-number column. The importer parses one out of a
   spreadsheet and nothing stores it, so a console player reaches the pitch
   without the number a researched player carries.
3. `strength` and `market_value_eur` arrive as JSON **strings** — node-postgres
   returns NUMERIC and BIGINT that way to avoid precision loss.

**A player is held back rather than guessed at.** `selected_by_percent` is null
until a gameweek is open with entries in it, and §08 pays a 1.5x multiplier on a
player under 2% owned — so defaulting a null to zero would hand out a scoring
bonus nothing measured. A console player with no ownership figure, no position,
no usable price, or a non-`active` status waits, and the Profile screen says how
many are waiting and why.

**Pointing the app at a console.** There is no deployment of that API; the
earlier repo ships a docker-compose for running it locally. The base URL is a
Gradle property, never a committed host:

    ./gradlew assembleRelease -Png.naijaleague.catalogueUrl=https://api.example.ng

Unset is a supported state, not a broken one: the app ships a complete
catalogue and says on Profile that it is not connected to a console. A debug
build defaults to `http://10.0.2.2:4000`, the host machine as the emulator sees
it. Cleartext is permitted to that address and the two loopback names in debug
builds only — `src/main/res/xml/network_security_config.xml` refuses it
everywhere else, on every supported API level.
