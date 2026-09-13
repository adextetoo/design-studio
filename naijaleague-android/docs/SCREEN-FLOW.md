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
| 3 | Onboarding | existed, now 4 steps | `OnboardingScreen.kt` |
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

Seven of fourteen were missing. All seven are built, and all fourteen are
reachable — `NaijaLeagueRoot` names every screen in the package, which is
checked rather than assumed.

## The order they run in

The launch sequence is a phase, not a boolean:

    SPLASH ──start──► ONBOARDING ──► SIGN UP ──► END CARD ──► APP
       └────sign in─────────────────────────────────────────►┘

Onboarding is four steps, not five. It used to open on a hook screen — mark,
headline, "Start building" or "I already have a squad" — which is what the
Splash already is. Two screens asking the same question is one screen too many
between a tap and a squad (§03), so the hook lives on the Splash and onboarding
starts where it should: on the club you support.

A returning manager skips the middle entirely. The end card closes the **first**
session only: showing it every visit would make leaving the app an event, and it
is not one.

Inside the app, four tabs (Home / Team / Leagues / Rules) and eleven overlays.
Notifications and profile sit in the Home header rather than the bottom bar —
the bar has four slots and thumb gravity belongs to what you do every visit
(§13). Picking your team is that; reading your alerts is not. Joining a league
sits in the Leagues header for the same reason: it is the one action on that
screen that is not reading a table.

Choosing players now ends at the transfer confirmation rather than returning
silently to the pitch. The rules page promises the app states the cost before
you confirm; stage 7 is where it also states it after.

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

## Backgrounds: flat, with the motifs shelved

The draft's hero screens sit on photographs of players celebrating. Those
photographs are of the internationals in point 3, and putting one behind an NPFL
app says what an invented player name says. So no photograph.

What was built instead — tiled motifs from the four traditions §01 names, plus
the pitch markings, shipping as Android vector drawables — was applied across
every surface and then **rolled back on a design call**. The app's grounds are
flat again, as they were before the illustration system landed.

Nothing was deleted. The five vectors are still in `res/drawable`, `colors.xml`
still carries the full palette, `IllustrationAssetTest` still guards them, and
`Illustration.kt` still holds `Motif`, `motifFor` and `motifField`. Screens call
`BrandBackdrop` / `HeroBackdrop` rather than painting `palette.ground`
themselves, so switching the treatment back on is two lines in one file instead
of a change to eighteen screens.

| Asset | Tradition | Intended surface |
| --- | --- | --- |
| `il_adire_eleko.xml` | Yoruba resist-dye grid | Adire Indigo |
| `il_uli_linework.xml` | Igbo curvilinear painting | never with Adire |
| `il_nsibidi_marks.xml` | Igbo ideographic strokes | Night Pitch |
| `il_arewa_lattice.xml` | Northern geometric interlace | Nzu Chalk |
| `il_pitch_arcs.xml` | Pitch markings | hero screens |

Every colour in them is a reference, never a literal — `android:tint="@color/uli_clay"`,
not `#A8552F`. `IllustrationAssetTest` fails the build on a literal hex, on a
tint naming a colour that does not exist, and on a background drawn strongly
enough to compete with a score. That guard holds whether or not the tiles are
currently drawn.

**Club badges went back to one mark too.** They briefly filled with each club's
own sourced kit colour. Fourteen clubs rendering fourteen colours puts a second
palette on every list row, competing with the one thing a row exists to show.
The colour data and its confidence stay on `NpflClubs`, and
`BrandColor.legibleInkOn` still picks legible text for an arbitrary fill, so
that decision is reversible in one function.

## Navigation and consistency audit

**Every screen has a way out.** The four tabs — Home, Team, Leagues, Rules —
carry the bottom bar. The fourteen launch screens and overlays deliberately do
not: a full-screen job with a bar on it invites you to abandon it half-done.
Ten of them carry a header chevron; Transfer Confirmed and the End Card are
terminal cards whose buttons are the way out; the Splash is the entry point. No
screen is a dead end.

Notifications, profile and join-a-league sit in headers rather than the bar.
The bar has four slots and thumb gravity belongs to what you do every visit
(§13) — picking your team is that; reading your alerts is not.

**Type and colour.** No raw `Color(0x…)` anywhere outside `brand/`. No
`TextStyle` built by hand. Every font family override is `NigerianText`, which
is the one §02 requires wherever a player's name can appear.

One violation found and fixed: a pitch pill set a player's surname at
`name.copy(fontSize = 15.sp)` — a hand-typed size on no step of the scale. It
now uses `nameCondensed`, which exists for exactly that case: a long name on a
tight row keeps its weight and its marks and narrows the face instead.

**Notes are contained, not railed.** `SourceNote` used to be a coloured bar down
the left of loose text. Two things were wrong. A card already means "a separate
thing" in this system (§13) and a note is one, so the rail was a second, weaker
way to say it — and the accent stripe made every caveat look like an alert when
most are simply context. It is now a `BrandCard` with a `SectionLabel`, the same
object "Add your people" uses, and the tone parameter is gone: a note that needs
to shout is a violation message, and those live on the control they block.

## A note on the tooling that was asked for

shadcn/ui is a React and Tailwind component library. This app is Jetpack Compose
and Kotlin; there is no shadcn for it, and no such skill is installed. The
equivalent role — a shared component layer that screens compose from rather than
restyling — is already filled by `ui/components/`, and every screen built here
draws from it rather than adding a second button or card.
