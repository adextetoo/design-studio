# The approved screen flow

The graphic designer's 14-stage draft is adopted as the app's screen inventory.
This file records what that means: which stages existed, which were missing and
have now been built, and the handful of places where the draft and the brand
system say different things.

**The brand system wins those.** The instruction was explicit — adopt the flow,
change nothing in the brand system — so the draft is read as *which screens the
app needs*, not as branding, copy, palette or data.

## Status of the 14 stages

| # | Stage | Status | Screen |
| --- | --- | --- | --- |
| 1 | Splash | **built** | `SplashScreen.kt` |
| 2 | Sign Up | **built**, repositioned | `SignUpScreen.kt` |
| 3 | Onboarding | existed | `OnboardingScreen.kt` |
| 4 | Home Dashboard | existed | `HomeScreen.kt` |
| 5 | Player Selection | existed | `ChoosePlayersScreen.kt` |
| 6 | Manage Squad | existed | `TeamScreen.kt` |
| 7 | Transfer Confirmation | **built** | `TransferConfirmedScreen.kt` |
| 8 | Join Leagues | **built** | `JoinLeaguesScreen.kt` |
| 9 | Leaderboard | existed | `LeaguesScreen.kt` (My league / Overall / My club) |
| 10 | Live Match Updates | existed | `LiveMatchScreen.kt` |
| 11 | Points & Results | **built** | `PointsScreen.kt` |
| 12 | Profile & Settings | **built** | `ProfileScreen.kt` |
| 13 | Notifications | **built** | `NotificationsScreen.kt` |
| 14 | End / Return Home | **built** | `EndCardScreen.kt` |

Seven of fourteen were missing. All seven are built.

### Four screens the draft does not have, which stay

| Screen | Why it stays |
| --- | --- |
| `RulesScreen.kt` | Every rule derives from the engine constants, so the page cannot promise something the app does not do. |
| `GafferPassScreen.kt` | §06. The revenue model, and the place the app promises you cannot buy points. |
| `ThreePickScreen.kt` | §15. The free, sponsor-funded weekly predictor for people with no squad. |
| `ReceiptScreen.kt` | §04. The Monday share card, and the only place the campaign line appears. |

The draft's stage 7 is a transfer receipt; `ReceiptScreen` is a different object
— the Monday morning share card — so stage 7 got its own screen rather than
being folded into it.

## Where the draft and the brand system disagree

These are recorded, not resolved by the draft.

**1. The draft is branded NaijaFantasy. The product is NaijaLeague Fantasy.**
Board-approved name, tagline and endline are unchanged. `BrandCopyTest` asserts
all five locked lines.

**2. The draft's strapline is "Draft · Score · Compete".** The board replaced
"Draft" with "Choose" everywhere. `BrandCopyTest` fails the build if the word
reaches a user. The draft's own player screen is headed "Select Your Players";
ours is "Choose your players".

**3. The draft's squads are Super Eagles internationals.** Osimhen, Lookman,
Iheanacho, Chukwueze, Boniface, Nwabali, Sadiq Umar — priced at "12.5M", listed
against NIG. None of them play in the NPFL. This is the exact mistake the app
exists not to make, and it is why `NpflSquads` carries 56 sourced players and
labelled stand-ins for everyone else. See `SOURCES.md`.

**4. The draft's leaderboard lists Wikki Tourists and Heartland FC.** Neither is
in the 2026/27 NPFL. `SampleDataTest` fails if a club that left the league
appears anywhere in the app.

**5. The draft asks for an account at stage 2, before anything is seen.** §03's
first four minutes says the opposite, and the board approved it: choose a club,
choose fifteen, see the pitch, *then* be asked for a name. So the Sign Up screen
was built, and sits at the end of onboarding rather than the start. The heading
says what is actually being saved. This is the one stage whose *position* moved;
its existence is adopted as drawn.

**6. The draft signs in with Google and Apple.** Apple's sign-in requirement
applies on iOS. This is an Android product, so the Apple button is a dead option
on a form, and it is dropped. Phone is first instead — it is how this market
signs in, and §05's SMS team news needs the number anyway. There is no password
field: a one-time code to that number is fewer fields, fewer resets, and one
less credential for this product to store.

**7. Prices are in Naira.** `Money.format` / `Money.compact`, never "12.5M".

## Backgrounds: drawn, not photographed

The draft's hero screens sit on photographs of players celebrating. Those
photographs are of the internationals in point 3, and putting one behind an NPFL
app says what an invented player name says.

So the illustration system in `ui/components/Illustration.kt` draws the grounds
instead, from the four motif traditions §01 names plus the pitch markings:

| Asset | Tradition | Used on |
| --- | --- | --- |
| `il_adire_eleko.xml` | Yoruba resist-dye grid | Adire Indigo surface |
| `il_uli_linework.xml` | Igbo curvilinear painting | available; never with Adire |
| `il_nsibidi_marks.xml` | Igbo ideographic strokes | Night Pitch surface |
| `il_arewa_lattice.xml` | Northern geometric interlace | Nzu Chalk surface |
| `il_pitch_arcs.xml` | Pitch markings | hero screens only |

They are Android vector drawables — the platform's own format — rather than
raster exports, so one file serves every density and all three surfaces.

**Every colour is a reference, never a literal.** `android:tint="@color/uli_clay"`,
not `#A8552F`. A baked hex is correct on one ground and invisible on the other
two, and nobody finds that by reading. `IllustrationAssetTest` fails the build
on a literal hex in any illustration, on a tint naming a colour that does not
exist, and on a background drawn strongly enough to compete with a score.

`colors.xml` is now the full palette rather than three entries, and the same
test asserts it against `brand/Color.kt` so the two copies cannot drift.

One rule inside the system: **Adire is a grid and Uli is freehand.** They are
different disciplines and they fight on the same surface, so `motifFor()` never
returns both and no screen asks for both.

## A note on the tooling that was asked for

shadcn/ui is a React and Tailwind component library. This app is Jetpack Compose
and Kotlin; there is no shadcn for it, and no such skill is installed. The
equivalent role — a shared component layer that screens compose from rather than
restyling — is already filled by `ui/components/`, and every screen built here
draws from it rather than adding a second button or card.
