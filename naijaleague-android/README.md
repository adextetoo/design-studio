# NaijaLeague Fantasy — Android app

A native Android app built to the NaijaLeague Fantasy brand system v1.1
(board-approved). Kotlin, Jetpack Compose, Material 3.

> **Name, tagline and endline are locked by board decision.** NaijaLeague
> Fantasy / "Build Your NPFL Dream Team" / "Fantasy football for real NPFL
> fans". "Table no dey lie" is the campaign line, not the tagline. The word
> "Draft" does not appear anywhere in the product — it is "Choose".

## Build

```
./gradlew assembleDebug          # APK at app/build/outputs/apk/debug/
./gradlew testDebugUnitTest      # the rules engine: 49 tests
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
| Rules engine + example data (`rules/`, `data/`) | **Compiled and tested — 49 tests, 0 failures.** Pure Kotlin, no Android dependencies, runs on the JVM. |
| All Kotlin sources parse | **Verified — 0 syntax errors** via the Kotlin 2.1 compiler over every file. |
| Compose UI compiles and renders | **Not verified in this environment.** `dl.google.com` and `maven.google.com` are blocked by egress policy here, so AndroidX/Compose could not be resolved and no APK could be produced. Open in Android Studio and run `./gradlew assembleDebug` — expect to fix small API details, not structure. |

The rules engine is deliberately Android-free so the part of this product that
is *hard to get right* — and that carries the brand's whole argument — is
provable without an emulator.

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

## Licensed typefaces

Ships with the brand system's named open-source fallbacks — Inter and Archivo
Expanded — bundled as variable fonts with the axes pinned in the `res/font` XML
resources. To install the licensed pairing: drop `sohne_*.otf` and `druk_*.otf`
into `app/src/main/res/font/`, point the families in `brand/Type.kt` at them, and
delete the variation XML. See `licenses/` for the OFL.

**Player names must never be set in the display family.** Ten names in the
example set use stacked combining marks (`Ọ̀gbọ́nna`, `Ìfẹ́anyì Ụ̀zọ̀`) that Druk has
no glyphs for; they render through `BrandType.NigerianText`. There is a test
that fails if the example set stops exercising this.

## Example data

`data/SampleData.kt` is example data, not real. Running it through the engine
gives Gameweek 12 = **93 points**, where the 1.8%-owned away forward scores
**29** and the 61.4%-owned captain scores **4** — the product's argument,
demonstrated by its own rules.
