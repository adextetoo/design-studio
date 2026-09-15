# NaijaLeague Fantasy — Android app

A native Android app built to the NaijaLeague Fantasy brand system v1.1
(board-approved). Kotlin, Jetpack Compose, Material 3.

> **Name, tagline and endline are locked by board decision.** NaijaLeague
> Fantasy / "Build Your NPFL Dream Team" / "Fantasy football for real NPFL
> fans". "Table no dey lie" is the campaign line, not the tagline. The word
> "Draft" does not appear anywhere in the product — it is "Choose".

## Build

```
./gradlew -p core test           # 151 engine tests -- no Android SDK needed
./gradlew testDebugUnitTest      # 22 brand guard rails -- these need AndroidX
./gradlew assembleDebug          # APK at app/build/outputs/apk/debug/
```

`core/` is a **separate Gradle build**, not a subproject, and that is what lets
the first line run on a machine with no Android SDK at all. Gradle configures
every project in a build before it runs anything in one, so as `include(":core")`
a test run would still configure `:app`, and AGP would stop with "SDK location
not found" before a single test executed. The root build pulls it back in with
`includeBuild("core")`, so `:app` compiles against the same sources and there is
one copy of everything. See `core/settings.gradle.kts`.

**`testDebugUnitTest` no longer covers the engine.** It runs the 22 tests that
genuinely need AndroidX and nothing else, so run both.

**Or let CI build it.** `.github/workflows/android.yml` runs the unit tests,
lints, assembles the debug APK and uploads it as a workflow artifact on every
push that touches this directory. That workflow is also the first place the
Compose layer is compiled against real AndroidX rather than against stubs, and
the first place `BrandGuardrailsTest` and `BrandCopyTest` actually execute —
both need AndroidX, which the authoring environment could not reach.

Download the APK from the run's **Artifacts** section, or trigger a build by
hand from the Actions tab (`workflow_dispatch`).

**Or build it in a container.** `Dockerfile.build` is CI's environment on your
own machine: JDK 17, the Android SDK for compileSdk 35 with its licences
accepted, and the same three Gradle tasks in the same order — so a green build
here and a green build in Actions mean the same thing.

```sh
docker build -f Dockerfile.build -t naijaleague-android-build .
docker run --rm -v "$PWD/artifacts:/out" naijaleague-android-build
```

The APK and the test and lint reports land in `artifacts/`. Nothing is
installed on the host — no JDK, no SDK, no Gradle. The image builds *the app*;
it does not run it. An APK runs on a handset or an emulator, so there is
nothing here to "deploy in Docker", and the container's job ends when the file
is written.

Arguments after the image name go to every Gradle invocation, which is how the
console URL is set for a build:

```sh
docker run --rm -v "$PWD/artifacts:/out" naijaleague-android-build   -Png.naijaleague.catalogueUrl=https://api.example.ng
```

Requires JDK 17+ and the Android SDK (compileSdk 35). Gradle 8.9 via the
wrapper, AGP 8.7.3, Kotlin 2.0.21, Compose BOM 2024.10.01. `minSdk 26`.

### Why minSdk 26 and not 24

The display face is a **variable** font. The instance that makes Archivo read
as Druk Wide — `wght 900, wdth 118` — needs `fontVariationSettings`, which
arrived in API 26. Dropping to 24 would silently render every headline in
Archivo's default `wght 600 / wdth 100`, i.e. ordinary semibold, and the brand's
display voice would be gone. API 26 still covers the cheap-handset reality this
product is built for.

## What is verified, and what is not

Honesty matters more here than a clean-looking claim.

| Area | Status |
|---|---|
| Rules engine, data and catalogue (`core/`) | **Compiled and tested — 151 tests, 0 failures, with `ANDROID_HOME` unset.** Pure Kotlin in a build of its own; `./gradlew -p core test`. |
| Brand guard rails (`BrandGuardrailsTest`, `BrandCopyTest`) | **Executed — 22 tests, 0 failures** via `./gradlew testDebugUnitTest` against real AndroidX. |
| All Kotlin sources parse | **Verified — 0 syntax errors** via the Kotlin 2.1 compiler over every file. |
| Compose UI type-checks | **Verified against a hand-written stub of the AndroidX API** using the Kotlin 2.0.21 compiler — 0 errors. The stub is not the real library, so gaps are possible, but every call signature, scope receiver (`RowScope.weight`, `BoxScope.matchParentSize`), `R.font.*` reference and import path was checked. |
| Compose UI compiles against real AndroidX | **Verified — BUILD SUCCESSFUL**, debug APK at 10.7 MB, installed and run on an API 36 emulator. Finding this took one fix: an unescaped apostrophe in `strings.xml` that failed `mergeDebugResources`. |
| The app reads a live operator console | **Verified end to end.** Against the API from `infra/docker-compose.local.yml`, the Profile screen reports it is connected; an operator colour edit through `PATCH /admin/clubs/:id` changed that line to name the club colour it took. |

The rules engine is deliberately Android-free so the part of this product that
is *hard to get right* — and that carries the brand's whole argument — is
provable without an emulator. `core/` is what makes that claim true in practice
rather than only in principle: before it, those tests ran through AGP and a
contributor without the SDK could not execute them at all.

## Brand system → code

| Brand system | Where it lives |
|---|---|
| §01 Colour palette, measured contrast ratios, banned pairings | `brand/Color.kt` |
| §01 Three surfaces (Night Pitch / Adire Indigo / Nzu Chalk) | `brand/Theme.kt` — `BrandPalette`, `LocalBrandPalette` |
| §02 Type scale, the four font classes | `brand/Type.kt` |
| §02 Nigerian glyph requirement | `brand/Type.kt` → `NigerianText`; enforced in data by `SampleDataTest` |
| §02 v1.1 body-size floor (15sp) | `brand/Dimens.kt` → `MinBodySize` |
| §03 App icon: lime tick on Eagle Dark with Adire crackle | `res/drawable/ic_launcher_*.xml` |
| §03 Five-step onboarding, squad before account | `ui/screens/OnboardingScreen.kt` |
| §05 Scoring, Away Day Bonus, The Three | `rules/Scoring.kt` |
| §05 Transfers: 1 free, bank 5, −4 per extra | `rules/Transfers.kt` |
| §05 Chips: Jara, Owambe, Aso Ebi, Waka Pass, Ground Man | `rules/Model.kt` → `Chip` |
| §05 Rules page copy, on the chalk reading surface | `ui/screens/RulesScreen.kt` |
| §08 Differential multiplier (the template-tyranny fix) | `rules/Model.kt` → `DifferentialTier` |
| §08 Two-per-club cap | `rules/Squad.kt` → `SquadRules.MAX_PER_CLUB`, surfaced in `ChoosePlayersScreen` |
| §12 Voice: errors in clear English, no banter | `res/values/strings.xml`, `rules/Squad.kt` violation messages |
| §13 Motion: one Uli curve, 240ms ceiling, count-up | `brand/Motion.kt`, `components/PointsCounter` |
| §13 Deadline as permanent furniture | `components/DeadlineStrip` |
| §13 Offline as a visible state | `components/OfflineBadge` |
| §13 Nsibidi icon grammar (one blunt stroke) | `components/Chrome.kt` → `TabGlyph`, drawn not imported |
| §06 Gaffer Pass pricing and the "never sells points" promise | `ui/screens/GafferPassScreen.kt`, on the Adire Indigo surface |
| §04 The Monday Receipt share card, and the campaign line | `ui/screens/ReceiptScreen.kt` |
| §15 Three Pick, the F1-derived weekly predictor | `ui/screens/ThreePickScreen.kt` |
| §13 The away-bonus second beat | `components/AwayBonusBadge`, driven by `BrandMotion.standard()` |

## Decisions the brand system left open

Flagged rather than buried, because each is a real product call:

1. **Multiplier order.** `base → × differential → × Ground Man → round → ×
   captain`. Captain applies last to an already-rounded subtotal, so a captained
   score is always exactly double the number on the player's own card.
   Rounding is half *away from zero*, so a negative score is never quietly
   softened. Documented in `Scoring.kt` and locked by test.
2. **Primary action colour.** §01 gives Eagle Dark the "buttons" role, which is
   right on chalk (8.53:1) but disappears on Night Pitch. On dark surfaces the
   primary action is Jara Lime with Night Pitch text (11.23:1). See
   `BrandColor.primaryActionFill`.
3. **Bundled fonts, not downloadable.** Removes the Play Services dependency and
   means type renders with no network — the same principle as the rest of the
   product. Adds ~1.5MB; revisit against the 4MB Lite-mode target.
4. **No dynamic colour.** Material You would repaint a brand whose whole equity
   is one specific green in the wallpaper's colour.
5. **Three sizes pulled onto the scale.** An earlier pass had invented 34sp, 13sp
   and 38sp levels. They are now 28sp, 12sp and 40sp — all on §02's nine-level
   scale — and a test fails if a new one appears.
6. **The endline is not shown in onboarding.** §04 says the endline sits under
   the logo on every execution; §12 says "real fans" must never appear in
   onboarding, an empty state, or aimed at a user. §12 is the more specific rule
   and names onboarding directly, so the product carve-out wins: `EndlineLockup`
   is reserved for the store listing, an About screen and share cards.

## Where the reference mockups were overridden

The brand system wins, per the brief.

- Reference art used **"Draft NPFL players"** and the names *NaijaDraft* /
  *NaijaFantasy* / *IDAN FC*. All replaced with NaijaLeague Fantasy and "Choose".
- Reference art put **real internationals** (Osimhen, Iheanacho, Chukwueze,
  Nwabali) into NPFL squads. Those players do not play in the NPFL, and a
  product whose credibility rests on knowing the league cannot ship that. The
  clubs here are the real NPFL; the players are invented and marked as example
  data.
- Reference palette leaned on a brighter mint green. Tokens come from §01 only.
- Reference "Key features" panel (Intelligence / Supply / Research / Analytics)
  is generic B2B copy and is not in the brand system's voice (§12). Not carried.

## Brand compliance

`BrandGuardrailsTest` turns the brand system's numeric rules into something CI
catches, rather than prose nobody re-reads:

- every palette's ink, dim ink, accent and negative clear 4.5:1 on their own ground
- the five ratios §01 publishes still measure what §01 says they measure
- the banned pairings stay banned (Eagle Dark on Live Green under 3:1; lime can
  never become the chalk accent)
- no `TextStyle` used for body, rules, money or names drops below the 15sp floor
- no size drifts off §02's nine-level scale
- anything that can carry a name uses the Noto-backed family, and `NigerianText`
  is never `FontFamily.Default`
- no interaction animation exceeds the 240ms ceiling

`BrandCopyTest` guards the copy the same way. `strings.xml` is now the single
source of truth for the board-locked lines, the voice-critical copy and anything
stating a rule — 51 strings, all referenced, none duplicated as a literal. The
tests fail if a locked line changes, if a composable hardcodes copy the file
already owns, if a string goes unused, if "Draft" reappears, or if the endline
turns up in onboarding.

Incidental interface labels ("Bench", "Match stats") stay inline on purpose:
they carry no brand risk, and moving them would add indirection without adding
safety. Copy for screens that do not exist yet is deliberately **not** parked in
the file — that is how it went dead and drifted the first time.

The first time these ran, they caught a contrast bug nothing else had: Ivie
Coral Lit measures 4.93:1 on Night Pitch but only **3.99:1 on Adire Indigo**,
because Indigo is the lighter ground. §01 already defines two coral steps for
two ground luminances; Indigo needed a third, `IvieCoralOnIndigo`. Neither I nor
the audit caught it by reading — it took executing the assertion.

These exist because an independent audit of this code found the guard rails were
declared and enforcing nothing: a 13sp money label, a 12sp player name, brass on
a sync dot, and `NigerianText` silently resolving to the OEM system font. All
fixed; the tests stop them coming back.

## Licensed typefaces

Ships with the brand system's named open-source fallbacks — Inter and Archivo
Expanded — plus a subset of **Noto Sans** for names, all bundled as variable
fonts with the axes pinned in the `res/font` XML resources. To install the licensed pairing: drop `sohne_*.otf` and `druk_*.otf`
into `app/src/main/res/font/`, point the families in `brand/Type.kt` at them, and
delete the variation XML. See `licenses/` for the OFL.

**Player names must never be set in the display family.** Ten names in the
example set use stacked combining marks (`Ọ̀gbọ́nna`, `Ìfẹ́anyì Ụ̀zọ̀`) that Druk has
no glyphs for; they render through `BrandType.NigerianText`. There is a test
that fails if the example set stops exercising this.

`NigerianText` is a **bundled 537KB subset of Noto Sans** (Latin + Latin Ext-A/B
+ IPA + combining marks + Latin Extended Additional + ₦), carrying the GPOS and
GDEF tables that position a tone mark over a dotted vowel. It is deliberately not
`FontFamily.Default`, which resolves to SamsungOne, MiSans or OnePlus Sans on the
handsets this product targets — an unaudited face deciding how Nigerian names
render. Long club names use `NigerianTextCondensed`, narrowed on Noto's own
`wdth` axis rather than switched to the display face, so "Bendel Insurance" fits
beside "3SC" with the marks intact.

## League data and example data

The **clubs are real**: the twenty-club 2026/27 NPFL field, transcribed from the
Transfermarkt league table with each club's market value and squad size. The
transcription is testable rather than trusted — `SampleDataTest` sums the squad
sizes and fails unless they come to 829, which is the total the source publishes,
and checks the market values against the published €25.65m within the rounding
the source itself applies.

Three clubs — Barau FC, Kun Khalifat FC and Rancher's Bees FC — carry no city,
because the source table does not state one and the club name does not imply it.
The UI omits the line rather than rendering it blank. Fill them in when known.

The **players are still invented** and marked as such. Running it through the engine
gives Gameweek 12 = **93 points**, where the 1.8%-owned away forward scores
**29** and the 61.4%-owned captain scores **4** — the product's argument,
demonstrated by its own rules.
